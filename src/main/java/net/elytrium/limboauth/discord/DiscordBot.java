
package net.elytrium.limboauth.discord;

import javax.annotation.Nonnull;

// ...existing code...
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import javax.security.auth.login.LoginException;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DiscordBot {
    private JDA jda;
    private final String token;
    private boolean isReconnecting = false;
    private Consumer<String> logger;
    private String logChannelId;
    
    // Button callback'lerini sakla
    private final Map<String, java.util.function.BiConsumer<String, String>> buttonCallbacks = new ConcurrentHashMap<>();
    private boolean listenerRegistered = false;

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public void setLogChannelId(String logChannelId) {
        this.logChannelId = logChannelId;
    }

    private void log(String message) {
        if (logger != null) {
            logger.accept("[DiscordBot] " + message);
        }
    }
    // 2FA mesajı embed ve buton ile gönder
    public void send2FAMessageWithButton(String userId, MessageEmbed embed, Button button) {
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            log("Bot bağlı değil, mesaj gönderilemedi.");
            return;
        }
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        
        RestAction.setDefaultTimeout(10, TimeUnit.SECONDS);
        
        jda.retrieveUserById(userId).queue(
            user -> {
                if (embed == null) throw new IllegalArgumentException("embed cannot be null");
                user.openPrivateChannel().queue(
                    channel -> channel.sendMessageEmbeds(embed).setActionRow(button).queue(
                        success -> log("2FA mesajı gönderildi: " + userId),
                        error -> log("Mesaj gönderme hatası: " + error.getMessage())
                    ),
                    error -> log("DM kanalı açma hatası: " + error.getMessage())
                );
            },
            error -> log("Kullanıcı bulunamadı: " + userId)
        );
    }

    // Discord buton tıklama eventini dinle
    public void registerButtonListener(java.util.function.BiConsumer<String, String> onVerify) {
        if (jda == null) return;
        
        // Listener sadece BİR KEZ kaydedilmeli
        if (!listenerRegistered) {
            listenerRegistered = true;
            jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
                    String id = event.getButton().getId();
                    if (id == null) return;
                    
                    // Callback'i bul ve çağır
                    for (Map.Entry<String, java.util.function.BiConsumer<String, String>> entry : buttonCallbacks.entrySet()) {
                        String prefix = entry.getKey();
                        if (id.startsWith(prefix)) {
                            // Önce acknowledge et
                            if (id.startsWith("crafterauth:approve:")) {
                                event.reply("✅ Giris onaylandi!").setEphemeral(true).queue();
                            } else if (id.startsWith("crafterauth:deny:")) {
                                event.reply("❌ Giris reddedildi.").setEphemeral(true).queue();
                            } else if (id.startsWith("crafterauth:verify:")) {
                                event.reply("✅ Giris dogrulandi!").setEphemeral(true).queue();
                            }
                            
                            // Callback'i çağır
                            entry.getValue().accept(id, "button");
                            return;
                        }
                    }
                }

                @Override
                public void onReady(@Nonnull ReadyEvent event) {
                    log("Discord bot başarıyla bağlandı: " + event.getJDA().getSelfUser().getName());
                    isReconnecting = false;
                }

                @Override
                public void onSessionDisconnect(@Nonnull SessionDisconnectEvent event) {
                    log("Discord bot bağlantısı kesildi. Otomatik yeniden bağlanma başlatılıyor...");
                    isReconnecting = true;
                }

                @Override
                public void onSessionRecreate(@Nonnull SessionRecreateEvent event) {
                    log("Discord bot yeniden bağlandı.");
                    isReconnecting = false;
                }
            });
        }
        
        // Callback'i kaydet
        buttonCallbacks.put("crafterauth:", onVerify);
    }

    public DiscordBot(String token) {
        this.token = token;
    }

    public void start() throws LoginException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Discord bot token is not set!");
        }
        
        log("Discord bot başlatılıyor...");
        
        // Discord botunu başlat - Rate limit friendly yapılandırma
        this.jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .setAutoReconnect(true)
                .setRequestTimeoutRetry(true)
                .setMaxReconnectDelay(32)
                .build();
        
        log("Discord bot yapılandırması tamamlandı, bağlantı bekleniyor...");
    }

    public boolean isConnected() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public boolean isReconnecting() {
        return isReconnecting;
    }

    public void shutdown() {
        if (jda != null) {
            log("Discord bot kapatılıyor...");
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
                    log("Graceful shutdown başarısız, force shutdown yapılıyor.");
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                log("Shutdown interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Basit test: Discord ID'ye DM gönder
     */
    public void sendTestDM(String userId, String message) {
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            log("Bot bağlı değil, test mesajı gönderilemedi.");
            return;
        }
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        
        jda.retrieveUserById(userId).queue(
            user -> {
                if (message == null) throw new IllegalArgumentException("message cannot be null");
                user.openPrivateChannel().queue(
                    channel -> channel.sendMessage(message).queue(
                        success -> log("Test mesajı gönderildi: " + userId),
                        error -> log("Test mesajı hatası: " + error.getMessage())
                    )
                );
            },
            error -> log("Kullanıcı bulunamadı: " + userId)
        );
    }

    @Nullable
    public JDA getJDA() {
        return jda;
    }

    public void sendLogToChannel(String message) {
        if (logChannelId == null || logChannelId.isEmpty()) {
            return;
        }
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            log("Bot bağlı değil, log kanalına mesaj gönderilemedi.");
            return;
        }

        try {
            TextChannel channel = jda.getTextChannelById(Objects.requireNonNull(logChannelId, "logChannelId cannot be null"));
            if (channel != null) {
                channel.sendMessage(Objects.requireNonNull(message, "message cannot be null")).queue(
                    success -> {},
                    error -> log("Log kanalına mesaj gönderme hatası: " + error.getMessage())
                );
            } else {
                log("Log kanalı bulunamadı: " + logChannelId);
            }
        } catch (Exception e) {
            log("Log kanalı hatası: " + e.getMessage());
        }
    }

    public void sendLogEmbed(MessageEmbed embed) {
        if (logChannelId == null || logChannelId.isEmpty()) {
            return;
        }
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            log("Bot bağlı değil, log kanalına embed gönderilemedi.");
            return;
        }

        try {
            TextChannel channel = jda.getTextChannelById(Objects.requireNonNull(logChannelId, "logChannelId cannot be null"));
            if (channel != null) {
                channel.sendMessageEmbeds(Objects.requireNonNull(embed, "embed cannot be null")).queue(
                    success -> {},
                    error -> log("Log kanalına embed gönderme hatası: " + error.getMessage())
                );
            } else {
                log("Log kanalı bulunamadı: " + logChannelId);
            }
        } catch (Exception e) {
            log("Log kanalı hatası: " + e.getMessage());
        }
    }
}
