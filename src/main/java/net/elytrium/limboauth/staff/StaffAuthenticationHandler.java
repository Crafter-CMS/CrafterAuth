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

package net.elytrium.limboauth.staff;

import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.elytrium.limboauth.discord.DiscordBot;

import java.awt.Color;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.Objects;

public class StaffAuthenticationHandler {
    private final DiscordBot discordBot;
    private final StaffDatabase staffDatabase;
    private final long timeoutSeconds;
    private Consumer<String> logger;
    private AuditLogger auditLogger;
    private MessageConfig messageConfig;
    
    // Bekleyen oyuncular - Discord onayından sonra sunucuya gönderilecek
    private final ConcurrentHashMap<UUID, Player> pendingPlayers = new ConcurrentHashMap<>();

    public StaffAuthenticationHandler(DiscordBot discordBot, StaffDatabase staffDatabase, long timeoutSeconds) {
        this.discordBot = discordBot;
        this.staffDatabase = staffDatabase;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public void setAuditLogger(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public void setMessageConfig(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    private void log(String message) {
        if (logger != null) {
            logger.accept("[StaffAuth] " + message);
        }
    }

    /**
     * Auth başarılı olduktan sonra, sunucuya transfer edilmeden ÖNCE Discord doğrulama yap
     */
    public void verifyBeforeServerTransfer(Player player, String ipAddress, java.util.function.Consumer<Boolean> callback) {
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();
        
        // Oyuncuyu bekletme listesine ekle
        pendingPlayers.put(uuid, player);
        
        // Oyuncuya mesaj göster
        player.sendMessage(net.kyori.adventure.text.Component.text("§e§lDiscord DM'inizi kontrol edin!"));
        player.sendMessage(net.kyori.adventure.text.Component.text("§7Discord'dan gelen onay mesajına cevap verin."));
        player.sendMessage(net.kyori.adventure.text.Component.text("§760 saniye içinde onaylamazsanız bağlantı kesilecek."));
        
        // Discord'da onay istemi başlat
        String requestId = initiateStaffLoginWithCallback(uuid, username, ipAddress, callback);
        
        if (requestId == null) {
            // Başarısız - direkt reddet
            callback.accept(false);
        }
    }

    /**
     * Callback ile staff login başlat
     */
    private String initiateStaffLoginWithCallback(UUID playerUuid, String username, String ipAddress, java.util.function.Consumer<Boolean> callback) {
        // Discord bot kontrolü
        if (!discordBot.isConnected()) {
            log("Discord bot offline - Staff girişi fail-safe modda izin veriliyor: " + username);
            if (auditLogger != null) {
                auditLogger.logFailSafeMode(playerUuid, username, ipAddress);
            }
            callback.accept(true); // Fail-safe: izin ver
            return "fail-safe";
        }

        // Staff kontrolü
        if (!staffDatabase.isStaff(username)) {
            log("Oyuncu staff değil: " + username);
            callback.accept(false);
            return null;
        }

        // Discord ID'yi al
        String discordId = staffDatabase.getDiscordId(playerUuid);
        if (discordId == null) {
            log("Staff için Discord ID bulunamadı: " + username);
            callback.accept(false);
            return null;
        }

        // Paralel login kontrolü
        if (StaffLoginRequest.hasActiveLogin(discordId)) {
            log("Discord hesabı zaten aktif bir login isteğine sahip: " + username);
            if (auditLogger != null) {
                auditLogger.logParallelLoginAttempt(playerUuid, username, discordId, ipAddress);
            }
            callback.accept(false);
            return null;
        }

        // Login request oluştur
        String requestId = StaffLoginRequest.createRequest(playerUuid, username, discordId, ipAddress, timeoutSeconds, () -> {
            // Timeout
            if (auditLogger != null) {
                auditLogger.logStaffLoginTimeout(playerUuid, username, discordId, ipAddress);
            }
            callback.accept(false);
        });
        
        if (requestId == null) {
            log("Login request oluşturulamadı: " + username);
            callback.accept(false);
            return null;
        }

        // Discord'a mesaj gönder
        sendDiscordVerificationWithCallback(discordId, username, requestId, ipAddress, playerUuid, callback);

        log("Staff login isteği oluşturuldu: " + username + " (Request ID: " + requestId + ")");
        return requestId;
    }

    /**
     * Oyuncu sunucuya girdikten SONRA Discord doğrulama gönder (ESKİ METOD)
     */
    public void sendVerificationAfterJoin(Player player, String ipAddress) {
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();
        
        // Oyuncuyu bekletme listesine ekle
        pendingPlayers.put(uuid, player);
        
        // Oyuncuya mesaj göster
        player.sendMessage(net.kyori.adventure.text.Component.text("§e§lDiscord DM'inizi kontrol edin!"));
        player.sendMessage(net.kyori.adventure.text.Component.text("§7Discord'dan gelen onay mesajına cevap verin."));
        player.sendMessage(net.kyori.adventure.text.Component.text("§760 saniye içinde onaylamazsanız kick atılacak."));
        
        // Discord'da onay istemi başlat
        initiateStaffLogin(uuid, username, ipAddress);
    }

    /**
     * Oyuncuyu limbo'da tutar ve Discord onayı bekler (ESKİ METOD - kullanılmıyor)
     */
    public void holdPlayerForVerification(Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();
        String ip = player.getRemoteAddress().getAddress().getHostAddress();
        
        // Oyuncuyu bekletme listesine ekle
        pendingPlayers.put(uuid, player);
        
        // Oyuncuya mesaj göster
        player.sendMessage(net.kyori.adventure.text.Component.text("§e§lDiscord DM'inizi kontrol edin!"));
        player.sendMessage(net.kyori.adventure.text.Component.text("§7Discord'dan gelen onay mesajına cevap verin."));
        player.sendMessage(net.kyori.adventure.text.Component.text("§760 saniye içinde onaylamazsanız bağlantı kesilecek."));
        
        // Discord'da onay istemi başlat
        initiateStaffLogin(uuid, username, ip);
    }

    /**
     * Staff girişi başlat - Discord DM gönder ve onay bekle
     */
    public String initiateStaffLogin(UUID playerUuid, String username, String ipAddress) {
        // Discord bot kontrolü - Fail-safe mode
        if (!discordBot.isConnected()) {
            log("Discord bot offline - Staff girişi fail-safe modda izin veriliyor: " + username);
            if (auditLogger != null) {
                auditLogger.logFailSafeMode(playerUuid, username, ipAddress);
            }
            // Fail-safe: Discord offline ise oyuncuyu limbodan çıkar, normal auth'a geç
            Player player = pendingPlayers.remove(playerUuid);
            if (player != null) {
                player.sendMessage(net.kyori.adventure.text.Component.text("§cDiscord botu offline. Normal girişe yönlendiriliyorsunuz."));
            }
            return "fail-safe-mode";
        }

        // Staff kontrolü
        if (!staffDatabase.isStaff(username)) {
            log("Oyuncu staff değil: " + username);
            releasePlayer(playerUuid, false);
            return null;
        }

        // UUID'yi kaydet
        staffDatabase.setUUID(username, playerUuid);

        // Discord ID'yi al
        String discordId = staffDatabase.getDiscordId(playerUuid);
        if (discordId == null) {
            log("Staff için Discord ID bulunamadı: " + username);
            releasePlayer(playerUuid, false);
            return null;
        }

        // Paralel login kontrolü
        if (StaffLoginRequest.hasActiveLogin(discordId)) {
            log("Discord hesabı zaten aktif bir login isteğine sahip: " + username);
            if (auditLogger != null) {
                auditLogger.logParallelLoginAttempt(playerUuid, username, discordId, ipAddress);
            }
            releasePlayer(playerUuid, false);
            return null;
        }

        // Login request oluştur
        String requestId = StaffLoginRequest.createRequest(playerUuid, username, discordId, ipAddress, timeoutSeconds, () -> {
            // Timeout: oyuncuyu kick at
            if (auditLogger != null) {
                auditLogger.logStaffLoginTimeout(playerUuid, username, discordId, ipAddress);
            }
            releasePlayer(playerUuid, false);
        });
        
        if (requestId == null) {
            log("Login request oluşturulamadı (paralel login): " + username);
            releasePlayer(playerUuid, false);
            return null;
        }

        // Discord'a mesaj gönder
        sendDiscordVerification(discordId, username, requestId, ipAddress, playerUuid);

        log("Staff login isteği oluşturuldu: " + username + " (Request ID: " + requestId + ", IP: " + ipAddress + ")");
        return requestId;
    }

    /**
     * Oyuncuyu serbest bırak veya kick at
     */
    private void releasePlayer(UUID playerUuid, boolean allow) {
        Player player = pendingPlayers.remove(playerUuid);
        if (player == null || !player.isActive()) {
            return;
        }
        
        if (allow) {
            // Onaylandı - oyuncu sunucuda kalabilir
            player.sendMessage(net.kyori.adventure.text.Component.text("§a§l✓ Discord doğrulaması başarılı!"));
            player.sendMessage(net.kyori.adventure.text.Component.text("§7Sunucuda kalabilirsiniz."));
        } else {
            // Reddedildi veya timeout - kick at
            player.disconnect(net.kyori.adventure.text.Component.text("§c§l✗ Discord doğrulaması reddedildi veya zaman aşımına uğradı."));
        }
    }

    public String initiateStaffLogin(UUID playerUuid, String username, String ipAddress, Runnable onTimeout, Runnable onSuccess) {
        // Discord bot kontrolü - Fail-safe mode
        if (!discordBot.isConnected()) {
            log("Discord bot offline - Staff girişi fail-safe modda izin veriliyor: " + username);
            if (auditLogger != null) {
                auditLogger.logFailSafeMode(playerUuid, username, ipAddress);
            }
            // Fail-safe: Discord offline ise admin loglarını kaydet ama girişe izin ver
            if (onSuccess != null) {
                onSuccess.run();
            }
            return "fail-safe-mode";
        }

        // Staff kontrolü
        if (!staffDatabase.isStaff(username)) {
            log("Oyuncu staff değil: " + username);
            return null;
        }

        // UUID'yi kaydet
        staffDatabase.setUUID(username, playerUuid);

        // Discord ID'yi al
        String discordId = staffDatabase.getDiscordId(playerUuid);
        if (discordId == null) {
            log("Staff için Discord ID bulunamadı: " + username);
            return null;
        }

        // Paralel login kontrolü
        if (StaffLoginRequest.hasActiveLogin(discordId)) {
            log("Discord hesabı zaten aktif bir login isteğine sahip: " + username);
            if (auditLogger != null) {
                auditLogger.logParallelLoginAttempt(playerUuid, username, discordId, ipAddress);
            }
            return null;
        }

        // Login request oluştur
        String requestId = StaffLoginRequest.createRequest(playerUuid, username, discordId, ipAddress, timeoutSeconds, () -> {
            if (onTimeout != null) {
                onTimeout.run();
            }
            if (auditLogger != null) {
                auditLogger.logStaffLoginTimeout(playerUuid, username, discordId, ipAddress);
            }
        });
        
        if (requestId == null) {
            log("Login request oluşturulamadı (paralel login): " + username);
            return null;
        }

        // Discord'a mesaj gönder
        sendDiscordVerification(discordId, username, requestId, ipAddress, playerUuid);

        log("Staff login isteği oluşturuldu: " + username + " (Request ID: " + requestId + ", IP: " + ipAddress + ")");
        return requestId;
    }

    private void sendDiscordVerification(String discordId, String username, String requestId, String ipAddress, UUID playerUuid) {
        EmbedBuilder embed = new EmbedBuilder();
        
        // MessageConfig'den mesajları al, yoksa default değerleri kullan
        String title = (messageConfig != null) ? messageConfig.getStaffLoginTitle() : "CrafterAuth Staff Girisi";
        String description = (messageConfig != null) ? messageConfig.formatDescription(username) : "**" + username + "** isimli kullanici sunucuya giris yapmak istiyor.";
        String approveButtonText = (messageConfig != null) ? messageConfig.getApproveButton() : "Onayla";
        
        embed.setTitle(title);
        embed.setDescription(description);
        embed.addField("Kullanici Adi", username, true);
        embed.addField("IP Adresi", ipAddress, true);
        embed.addField("Request ID", requestId.substring(0, 8) + "...", true);
        embed.addField("Sure", timeoutSeconds + " saniye", false);
        embed.setColor(Color.ORANGE);
        embed.setTimestamp(Instant.now());
        embed.setFooter("CrafterAuth Discord Dogrulama", null);

        Button approveButton = Button.success(
            Objects.requireNonNull("crafterauth:approve:" + requestId),
            Objects.requireNonNull(approveButtonText)
        );

        discordBot.send2FAMessageWithButton(discordId, embed.build(), approveButton);
        
        // Buton listener'ı kaydet
        registerVerificationListener(requestId, ipAddress, playerUuid);
    }

    private void sendDiscordVerificationWithCallback(String discordId, String username, String requestId, String ipAddress, UUID playerUuid, java.util.function.Consumer<Boolean> callback) {
        EmbedBuilder embed = new EmbedBuilder();
        
        // MessageConfig'den mesajları al, yoksa default değerleri kullan
        String title = (messageConfig != null) ? messageConfig.getStaffLoginTitle() : "CrafterAuth Staff Girisi";
        String description = (messageConfig != null) ? messageConfig.formatDescription(username) : "**" + username + "** isimli kullanici sunucuya giris yapmak istiyor.";
        String approveButtonText = (messageConfig != null) ? messageConfig.getApproveButton() : "Onayla";
        
        embed.setTitle(title);
        embed.setDescription(description);
        embed.addField("Kullanici Adi", username, true);
        embed.addField("IP Adresi", ipAddress, true);
        embed.addField("Request ID", requestId.substring(0, 8) + "...", true);
        embed.addField("Sure", timeoutSeconds + " saniye", false);
        embed.setColor(Color.ORANGE);
        embed.setTimestamp(Instant.now());
        embed.setFooter("CrafterAuth Discord Dogrulama", null);

        Button approveButton = Button.success(
            Objects.requireNonNull("crafterauth:approve:" + requestId),
            Objects.requireNonNull(approveButtonText)
        );

        discordBot.send2FAMessageWithButton(discordId, embed.build(), approveButton);
        
        // Buton listener'ı kaydet - callback ile
        registerVerificationListenerWithCallback(requestId, ipAddress, playerUuid, callback);
    }

    private void registerVerificationListenerWithCallback(String requestId, String ipAddress, UUID playerUuid, java.util.function.Consumer<Boolean> callback) {
        discordBot.registerButtonListener((buttonId, action) -> {
            if (buttonId.equals("crafterauth:approve:" + requestId)) {
                StaffLoginRequest.LoginRequest request = StaffLoginRequest.getRequest(requestId);
                
                // IP kontrolü ile doğrulama
                boolean verified = StaffLoginRequest.verifyWithIpCheck(requestId, ipAddress);
                if (verified) {
                    log("Staff girişi onaylandı (IP doğrulandı): " + requestId);
                    if (auditLogger != null && request != null) {
                        auditLogger.logStaffLogin(request.playerUuid, request.username, 
                            request.discordId, request.ipAddress, true, "Approved by Discord user");
                    }
                    sendDiscordLogNotification(request, "APPROVED");
                    
                    // Callback - onaylandı
                    callback.accept(true);
                    pendingPlayers.remove(playerUuid);
                } else {
                    log("Staff girişi reddedildi (IP uyuşmazlığı): " + requestId);
                    if (auditLogger != null && request != null) {
                        auditLogger.logIpMismatch(request.playerUuid, request.username, 
                            request.discordId, request.ipAddress, ipAddress);
                    }
                    sendDiscordLogNotification(request, "IP_MISMATCH");
                    
                    // Callback - reddedildi
                    callback.accept(false);
                    pendingPlayers.remove(playerUuid);
                }
            } else if (buttonId.equals("crafterauth:deny:" + requestId)) {
                StaffLoginRequest.LoginRequest request = StaffLoginRequest.getRequest(requestId);
                StaffLoginRequest.cancelRequest(requestId);
                log("Staff girişi reddedildi: " + requestId);
                if (auditLogger != null && request != null) {
                    auditLogger.logStaffLogin(request.playerUuid, request.username, 
                        request.discordId, request.ipAddress, false, "Denied by Discord user");
                }
                sendDiscordLogNotification(request, "DENIED");
                
                // Callback - reddedildi
                callback.accept(false);
                pendingPlayers.remove(playerUuid);
            }
        });
    }

    private void registerVerificationListener(String requestId, String ipAddress, UUID playerUuid) {
        discordBot.registerButtonListener((buttonId, action) -> {
            if (buttonId.equals("crafterauth:approve:" + requestId)) {
                StaffLoginRequest.LoginRequest request = StaffLoginRequest.getRequest(requestId);
                
                // IP kontrolü ile doğrulama
                boolean verified = StaffLoginRequest.verifyWithIpCheck(requestId, ipAddress);
                if (verified) {
                    log("Staff girişi onaylandı (IP doğrulandı): " + requestId);
                    if (auditLogger != null && request != null) {
                        auditLogger.logStaffLogin(request.playerUuid, request.username, 
                            request.discordId, request.ipAddress, true, "Approved by Discord user");
                    }
                    // Discord log channel'a bildirim gönder
                    sendDiscordLogNotification(request, "APPROVED");
                    // Oyuncuyu serbest bırak
                    releasePlayer(playerUuid, true);
                } else {
                    log("Staff girişi reddedildi (IP uyuşmazlığı): " + requestId);
                    if (auditLogger != null && request != null) {
                        auditLogger.logIpMismatch(request.playerUuid, request.username, 
                            request.discordId, request.ipAddress, ipAddress);
                    }
                    sendDiscordLogNotification(request, "IP_MISMATCH");
                    releasePlayer(playerUuid, false);
                }
            } else if (buttonId.equals("crafterauth:deny:" + requestId)) {
                StaffLoginRequest.LoginRequest request = StaffLoginRequest.getRequest(requestId);
                StaffLoginRequest.cancelRequest(requestId);
                log("Staff girişi reddedildi: " + requestId);
                if (auditLogger != null && request != null) {
                    auditLogger.logStaffLogin(request.playerUuid, request.username, 
                        request.discordId, request.ipAddress, false, "Denied by Discord user");
                }
                sendDiscordLogNotification(request, "DENIED");
                releasePlayer(playerUuid, false);
            }
        });
    }

    private void sendDiscordLogNotification(StaffLoginRequest.LoginRequest request, String status) {
        if (request == null) return;
        
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📋 Staff Login Audit Log");
        embed.addField("Status", Objects.requireNonNull(status), true);
        embed.addField("Username", Objects.requireNonNull(request.username), true);
        embed.addField("UUID", Objects.requireNonNull(request.playerUuid.toString()), false);
        embed.addField("Discord ID", Objects.requireNonNull(request.discordId), true);
        embed.addField("IP Address", Objects.requireNonNull(request.ipAddress), true);
        embed.setTimestamp(Instant.now());
        
        if (status.equals("APPROVED")) {
            embed.setColor(Color.GREEN);
        } else if (status.equals("DENIED")) {
            embed.setColor(Color.RED);
        } else {
            embed.setColor(Color.ORANGE);
        }
        
        discordBot.sendLogEmbed(embed.build());
    }

    public boolean isStaffMember(UUID playerUuid) {
        return staffDatabase.isStaff(playerUuid);
    }

    public boolean isStaffMember(String username) {
        return staffDatabase.isStaff(username);
    }

    /**
     * Oyuncu disconnect olduğunda pending request'leri temizle
     */
    public void cleanupPlayer(UUID playerUuid) {
        pendingPlayers.remove(playerUuid);
        StaffLoginRequest.cancelRequestByPlayerUuid(playerUuid);
    }
}
