/*
 * Copyright (C) 2021 - 2025 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboauth.listener;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.UuidUtils;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.elytrium.commons.utils.reflection.ReflectionException;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.elytrium.limboauth.LimboAuth;
import net.elytrium.limboauth.LimboAuth.CachedPremiumUser;
import net.elytrium.limboauth.LimboAuth.PremiumState;
import net.elytrium.limboauth.Settings;
import net.elytrium.limboauth.floodgate.FloodgateApiHolder;
import net.elytrium.limboauth.handler.AuthSessionHandler;
import net.elytrium.limboauth.model.RegisteredPlayer;
import net.elytrium.limboauth.model.SQLRuntimeException;
import net.elytrium.limboauth.event.PostAuthorizationEvent;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.elytrium.limboauth.staff.StaffDatabase;

// TODO: Customizable events priority

public class AuthListener {

  private static final MethodHandle DELEGATE_FIELD;
  //private static final MethodHandle LOGIN_FIELD;

  private final LimboAuth plugin;
  private final Dao<RegisteredPlayer, String> playerDao;
  private final FloodgateApiHolder floodgateApi;
  private final Component errorOccurred;

  public AuthListener(LimboAuth plugin, Dao<RegisteredPlayer, String> playerDao, FloodgateApiHolder floodgateApi) {
    this.plugin = plugin;
    this.playerDao = playerDao;
    this.floodgateApi = floodgateApi;

    this.errorOccurred = LimboAuth.getSerializer().deserialize(plugin.getLanguageManager().getMessages().errorOccurred);
  }

  @Subscribe(order = PostOrder.LATE)
  public void onPreLoginEvent(PreLoginEvent event) {
    // Ignore this event if it is was denied by other plugin
    if (!event.getResult().isAllowed()) {
      return;
    }

    try {
      String username = event.getUsername();
      if (!event.getResult().isForceOfflineMode()) {
        if (this.plugin.isPremium(username)) {
          event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());

          try {
            if (!Settings.IMP.MAIN.ONLINE_MODE_NEED_AUTH_STRICT) {
              CachedPremiumUser premiumUser = this.plugin.getPremiumCache(username);
              MinecraftConnection connection = this.getConnection(event.getConnection());
              if (!connection.isClosed() && premiumUser != null && !premiumUser.isForcePremium()
                  && this.plugin.isPremiumInternal(username.toLowerCase(Locale.ROOT)).getState() == PremiumState.UNKNOWN) {
                this.plugin.getPendingLogins().add(username);

                // As Velocity doesnt have any events for our usecase, just inject into netty
                connection.getChannel().closeFuture().addListener(future -> {
                  // Player has failed premium verfication client-side, mark as offline-mode
                  if (this.plugin.getPendingLogins().remove(username)) {
                    this.plugin.setPremiumCacheLowercased(username.toLowerCase(Locale.ROOT), false);
                  }
                });
              }
            }
          } catch (Throwable throwable) {
            throw new IllegalStateException("failed to track authentication process", throwable);
          }
        } else {
          event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
        }
      } else {
        try {
          MinecraftConnection connection = this.getConnection(event.getConnection());
          if (!connection.isClosed()) {
            this.plugin.saveForceOfflineMode(username);

            // As Velocity doesnt have any events for our usecase, just inject into netty
            connection.getChannel().closeFuture().addListener(future -> {
              this.plugin.unsetForcedPreviously(username);
            });
          }
        } catch (Throwable throwable) {
          throw new IllegalStateException("failed to track client disconnection", throwable);
        }
      }
    } catch (Throwable throwable) {
      event.setResult(PreLoginComponentResult.denied(this.errorOccurred));
      throw throwable;
    }
  }

  private MinecraftConnection getConnection(InboundConnection inbound) throws Throwable {
    LoginInboundConnection inboundConnection = (LoginInboundConnection) inbound;
    InitialInboundConnection initialInbound = (InitialInboundConnection) DELEGATE_FIELD.invokeExact(inboundConnection);
    return initialInbound.getConnection();
  }

  // Temporarily disabled because some clients send UUID version 4 (random UUID) even if the player is cracked
  /*
  private boolean isPremiumByIdentifiedKey(InboundConnection inbound) throws Throwable {
    LoginInboundConnection inboundConnection = (LoginInboundConnection) inbound;
    InitialInboundConnection initialInbound = (InitialInboundConnection) DELEGATE_FIELD.invokeExact(inboundConnection);
    MinecraftConnection connection = initialInbound.getConnection();
    InitialLoginSessionHandler handler = (InitialLoginSessionHandler) connection.getSessionHandler();

    ServerLogin packet = (ServerLogin) LOGIN_FIELD.invokeExact(handler);
    if (packet == null) {
      return false;
    }

    UUID holder = packet.getHolderUuid();
    if (holder == null) {
      return false;
    }

    return holder.version() != 3;
  }
  */

  @Subscribe
  public void onPostLogin(PostLoginEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    String username = event.getPlayer().getUsername();
    String ip = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
    
    // Staff kontrolü - UUID'yi kaydet ama henüz Discord DM gönderme
    StaffDatabase staffDb = this.plugin.getStaffDatabase();
    if (staffDb != null && staffDb.isStaff(username)) {
      // Sadece UUID'yi kaydet
      staffDb.setUUID(username, uuid);
      
      // Staff için session'ı temizle - Her girişte şifre + 2FA zorunlu
      this.plugin.removeCachedAuthCheck(username);
    }

    Runnable postLoginTask = this.plugin.getPostLoginTasks().remove(uuid);
    if (postLoginTask != null) {
      // We need to delay for player's client to finish switching the server, it takes a little time.
      this.plugin.getServer().getScheduler()
          .buildTask(this.plugin, postLoginTask)
          .delay(Settings.IMP.MAIN.PREMIUM_AND_FLOODGATE_MESSAGES_DELAY, TimeUnit.MILLISECONDS)
          .schedule();
    }
  }

  @Subscribe
  public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();
    String username = player.getUsername();
    
    // Normal oyuncu için mevcut akış
    if (player.isOnlineMode()) {
      CachedPremiumUser premiumUser = this.plugin.getPremiumCache(username);
      if (premiumUser != null) {
        premiumUser.setForcePremium(true);
      }

      this.plugin.getPendingLogins().remove(username);
    }

    if (this.plugin.needAuth(player)) {
      event.addOnJoinCallback(() -> this.plugin.authPlayer(player));
    }
  }

  @Subscribe
  public void onPostAuthorization(PostAuthorizationEvent event) {
    // Auth başarılı olduktan sonra - sunucuya gitmeden ÖNCE
    net.elytrium.limboapi.api.player.LimboPlayer limboPlayer = event.getPlayer();
    Player player = limboPlayer.getProxyPlayer();
    UUID uuid = player.getUniqueId();
    String username = player.getUsername();
    
    // Staff kontrolü - Discord 2FA gerekli mi?
    StaffDatabase staffDb = this.plugin.getStaffDatabase();
    if (staffDb != null && staffDb.isStaff(username)) {
      String discordId = staffDb.getDiscordId(uuid);
      if (discordId != null && this.plugin.getStaffAuthHandler() != null) {
        // Login boss bar'ını gizle
        net.elytrium.limboauth.handler.AuthSessionHandler.hideLoginBossBar(uuid);
        
        // Auth başarılı ama Discord onayı bekle
        event.setResult(net.elytrium.limboauth.event.TaskEvent.Result.WAIT);
        
        String ip = player.getRemoteAddress().getAddress().getHostAddress();
        
        // Discord 2FA Boss Bar göster
        net.kyori.adventure.bossbar.BossBar twoFABossBar = net.kyori.adventure.bossbar.BossBar.bossBar(
          net.kyori.adventure.text.Component.text("§eDiscord doğrulama için kalan süre: §660 saniye"),
          1.0f,
          net.kyori.adventure.bossbar.BossBar.Color.YELLOW,
          net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS
        );
        limboPlayer.getProxyPlayer().showBossBar(twoFABossBar);
        
        // Countdown scheduler
        final int[] remainingSeconds = {60};
        com.velocitypowered.api.scheduler.ScheduledTask[] countdownTask = new com.velocitypowered.api.scheduler.ScheduledTask[1];
        
        countdownTask[0] = this.plugin.getServer().getScheduler()
          .buildTask(this.plugin, () -> {
            remainingSeconds[0]--;
            if (remainingSeconds[0] > 0) {
              twoFABossBar.name(net.kyori.adventure.text.Component.text("§eDiscord doğrulama için kalan süre: §6" + remainingSeconds[0] + " saniye"));
              twoFABossBar.progress((float) remainingSeconds[0] / 60.0f);
            } else {
              limboPlayer.getProxyPlayer().hideBossBar(twoFABossBar);
              if (countdownTask[0] != null) {
                countdownTask[0].cancel();
              }
            }
          })
          .repeat(1, java.util.concurrent.TimeUnit.SECONDS)
          .schedule();
        
        // Discord DM gönder ve onay bekle
        this.plugin.getStaffAuthHandler().verifyBeforeServerTransfer(
          player, 
          ip,
          (approved) -> {
            // Boss bar'ı kapat
            limboPlayer.getProxyPlayer().hideBossBar(twoFABossBar);
            if (countdownTask[0] != null) {
              countdownTask[0].cancel();
            }
            
            if (approved) {
              // Onaylandı, sunucuya gönder
              event.complete(net.elytrium.limboauth.event.TaskEvent.Result.NORMAL);
            } else {
              // Reddedildi, disconnect
              event.completeAndCancel(net.kyori.adventure.text.Component.text("§cDiscord doğrulaması reddedildi veya zaman aşımına uğradı."));
            }
          }
        );
        return;
      }
    }
    
    // Normal oyuncu - direkt geç (default Result.NORMAL zaten set)
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onGameProfileRequest(GameProfileRequestEvent event) {
    // For Crafter CMS, skip traditional database operations
    if (this.playerDao == null) {
      Logger logger = LoggerFactory.getLogger(AuthListener.class);
      logger.debug("Skipping GameProfileRequest database operations for Crafter CMS");
      return;
    }

    if (Settings.IMP.MAIN.SAVE_UUID && (this.floodgateApi == null || !this.floodgateApi.isFloodgatePlayer(event.getOriginalProfile().getId()))) {
      RegisteredPlayer registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, event.getOriginalProfile().getId());

      if (registeredPlayer != null && !registeredPlayer.getUuid().isEmpty()) {
        event.setGameProfile(event.getOriginalProfile().withId(UUID.fromString(registeredPlayer.getUuid())));
        return;
      }
      registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, event.getUsername());

      if (registeredPlayer != null) {
        String currentUuid = registeredPlayer.getUuid();

        if (currentUuid.isEmpty()) {
          try {
            registeredPlayer.setUuid(event.getGameProfile().getId().toString());
            this.playerDao.update(registeredPlayer);
          } catch (SQLException e) {
            throw new SQLRuntimeException(e);
          }
        } else {
          event.setGameProfile(event.getOriginalProfile().withId(UUID.fromString(currentUuid)));
        }
      }
    } else if (event.isOnlineMode()) {
      try {
        UpdateBuilder<RegisteredPlayer, String> updateBuilder = this.playerDao.updateBuilder();
        updateBuilder.where().eq(RegisteredPlayer.LOWERCASE_NICKNAME_FIELD, event.getUsername().toLowerCase(Locale.ROOT));
        updateBuilder.updateColumnValue(RegisteredPlayer.HASH_FIELD, "");
        updateBuilder.update();
      } catch (SQLException e) {
        throw new SQLRuntimeException(e);
      }
    }

    if (Settings.IMP.MAIN.FORCE_OFFLINE_UUID) {
      event.setGameProfile(event.getOriginalProfile().withId(UuidUtils.generateOfflinePlayerUuid(event.getUsername())));
    }

    if (!event.isOnlineMode() && !Settings.IMP.MAIN.OFFLINE_MODE_PREFIX.isEmpty()) {
      event.setGameProfile(event.getOriginalProfile().withName(Settings.IMP.MAIN.OFFLINE_MODE_PREFIX + event.getUsername()));
    }

    if (event.isOnlineMode() && !Settings.IMP.MAIN.ONLINE_MODE_PREFIX.isEmpty()) {
      event.setGameProfile(event.getOriginalProfile().withName(Settings.IMP.MAIN.ONLINE_MODE_PREFIX + event.getUsername()));
    }
  }

  static {
    try {
      DELEGATE_FIELD = MethodHandles.privateLookupIn(LoginInboundConnection.class, MethodHandles.lookup())
          .findGetter(LoginInboundConnection.class, "delegate", InitialInboundConnection.class);
      //LOGIN_FIELD = MethodHandles.privateLookupIn(InitialLoginSessionHandler.class, MethodHandles.lookup())
      //    .findGetter(InitialLoginSessionHandler.class, "login",ServerLoginPacket.class);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ReflectionException(e);
    }
  }
}
