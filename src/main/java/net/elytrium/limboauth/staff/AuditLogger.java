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

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.file.Path;

public class AuditLogger {
    private static final ConcurrentLinkedQueue<AuditEntry> auditLog = new ConcurrentLinkedQueue<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault());
    private final Path logFilePath;

    public static class AuditEntry {
        public final long timestamp;
        public final String action;
        public final UUID playerUuid;
        public final String username;
        public final String discordId;
        public final String ipAddress;
        public final String result;
        public final String details;

        public AuditEntry(String action, UUID playerUuid, String username, String discordId, 
                         String ipAddress, String result, String details) {
            this.timestamp = System.currentTimeMillis();
            this.action = action;
            this.playerUuid = playerUuid;
            this.username = username;
            this.discordId = discordId;
            this.ipAddress = ipAddress;
            this.result = result;
            this.details = details;
        }

        public String toLogString() {
            return String.format("[%s] %s | User: %s (%s) | Discord: %s | IP: %s | Result: %s | Details: %s",
                DATE_FORMAT.format(Instant.ofEpochMilli(timestamp)),
                action,
                username,
                playerUuid != null ? playerUuid.toString() : "N/A",
                discordId != null ? discordId : "N/A",
                ipAddress != null ? ipAddress : "N/A",
                result,
                details != null ? details : ""
            );
        }

        public String toCsvString() {
            return String.format("%d,%s,%s,%s,%s,%s,%s,\"%s\"",
                timestamp,
                action,
                username != null ? username : "",
                playerUuid != null ? playerUuid.toString() : "",
                discordId != null ? discordId : "",
                ipAddress != null ? ipAddress : "",
                result,
                details != null ? details.replace("\"", "\"\"") : ""
            );
        }
    }

    public AuditLogger(Path logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void log(String action, UUID playerUuid, String username, String discordId, 
                   String ipAddress, String result, String details) {
        AuditEntry entry = new AuditEntry(action, playerUuid, username, discordId, ipAddress, result, details);
        auditLog.add(entry);
        
        // Asenkron dosyaya yazma
        writeToFile(entry);
    }

    public void logStaffLogin(UUID playerUuid, String username, String discordId, String ipAddress, 
                             boolean approved, String reason) {
        String result = approved ? "APPROVED" : "DENIED";
        log("STAFF_LOGIN", playerUuid, username, discordId, ipAddress, result, reason);
    }

    public void logStaffLoginTimeout(UUID playerUuid, String username, String discordId, String ipAddress) {
        log("STAFF_LOGIN_TIMEOUT", playerUuid, username, discordId, ipAddress, "TIMEOUT", "User did not respond in time");
    }

    public void logParallelLoginAttempt(UUID playerUuid, String username, String discordId, String ipAddress) {
        log("PARALLEL_LOGIN_BLOCKED", playerUuid, username, discordId, ipAddress, "BLOCKED", "Active login request already exists");
    }

    public void logIpMismatch(UUID playerUuid, String username, String discordId, String originalIp, String currentIp) {
        String details = String.format("Original IP: %s, Current IP: %s", originalIp, currentIp);
        log("IP_MISMATCH", playerUuid, username, discordId, currentIp, "BLOCKED", details);
    }

    public void logFailSafeMode(UUID playerUuid, String username, String ipAddress) {
        log("FAIL_SAFE_MODE", playerUuid, username, null, ipAddress, "ALLOWED", "Discord bot offline");
    }

    private void writeToFile(AuditEntry entry) {
        try (FileWriter writer = new FileWriter(logFilePath.toFile(), true)) {
            writer.write(entry.toLogString() + "\n");
        } catch (IOException e) {
            // Log silently to prevent recursion
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    public static ConcurrentLinkedQueue<AuditEntry> getAuditLog() {
        return auditLog;
    }

    public void exportToCsv(Path csvPath) throws IOException {
        try (FileWriter writer = new FileWriter(csvPath.toFile())) {
            writer.write("Timestamp,Action,Username,UUID,DiscordID,IPAddress,Result,Details\n");
            for (AuditEntry entry : auditLog) {
                writer.write(entry.toCsvString() + "\n");
            }
        }
    }
}
