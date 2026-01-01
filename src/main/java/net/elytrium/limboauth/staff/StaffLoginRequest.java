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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StaffLoginRequest {
    private static final ConcurrentHashMap<String, LoginRequest> pendingRequests = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> activeDiscordLogins = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static class LoginRequest {
        public final UUID playerUuid;
        public final String username;
        public final String requestId;
        public final String discordId;
        public final String ipAddress;
        public final long createdAt;
        public final Runnable onTimeout;

        public LoginRequest(UUID playerUuid, String username, String requestId, String discordId, String ipAddress, Runnable onTimeout) {
            this.playerUuid = playerUuid;
            this.username = username;
            this.requestId = requestId;
            this.discordId = discordId;
            this.ipAddress = ipAddress;
            this.createdAt = System.currentTimeMillis();
            this.onTimeout = onTimeout;
        }

        public boolean isExpired(long timeoutMillis) {
            return System.currentTimeMillis() - createdAt > timeoutMillis;
        }
    }

    public static String createRequest(UUID playerUuid, String username, String discordId, String ipAddress, long timeoutSeconds, Runnable onTimeout) {
        // Paralel login kontrolü
        if (activeDiscordLogins.containsKey(discordId)) {
            return null; // Aynı Discord hesabından zaten bir login request var
        }

        String requestId = UUID.randomUUID().toString();
        LoginRequest request = new LoginRequest(playerUuid, username, requestId, discordId, ipAddress, onTimeout);
        pendingRequests.put(requestId, request);
        activeDiscordLogins.put(discordId, requestId);

        // Timeout scheduler
        scheduler.schedule(() -> {
            LoginRequest req = pendingRequests.remove(requestId);
            if (req != null) {
                activeDiscordLogins.remove(req.discordId, requestId);
                if (req.onTimeout != null) {
                    req.onTimeout.run();
                }
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return requestId;
    }

    public static LoginRequest getRequest(String requestId) {
        return pendingRequests.get(requestId);
    }

    public static boolean verifyAndRemove(String requestId) {
        LoginRequest request = pendingRequests.remove(requestId);
        if (request != null) {
            activeDiscordLogins.remove(request.discordId, requestId);
            return true;
        }
        return false;
    }

    public static boolean verifyWithIpCheck(String requestId, String currentIp) {
        LoginRequest request = pendingRequests.get(requestId);
        if (request != null && request.ipAddress.equals(currentIp)) {
            return verifyAndRemove(requestId);
        }
        return false;
    }

    public static void cancelRequest(String requestId) {
        LoginRequest request = pendingRequests.remove(requestId);
        if (request != null) {
            activeDiscordLogins.remove(request.discordId, requestId);
        }
    }

    public static boolean hasActiveLogin(String discordId) {
        return activeDiscordLogins.containsKey(discordId);
    }

    public static void cancelRequestByPlayerUuid(UUID playerUuid) {
        // UUID'ye göre request bul ve iptal et
        pendingRequests.entrySet().removeIf(entry -> {
            LoginRequest request = entry.getValue();
            if (request.playerUuid.equals(playerUuid)) {
                activeDiscordLogins.remove(request.discordId, entry.getKey());
                return true;
            }
            return false;
        });
    }

    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
