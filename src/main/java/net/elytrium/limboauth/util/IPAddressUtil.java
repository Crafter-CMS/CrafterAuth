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

package net.elytrium.limboauth.util;

import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for extracting real IP addresses from players.
 * Handles both direct connections and proxied connections.
 */
public class IPAddressUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPAddressUtil.class);

    /**
     * Get the real IP address of a player.
     * This method extracts the IP from the remote address and logs it for
     * debugging.
     *
     * @param player The player to get the IP address for
     * @return The IP address as a string
     */
    public static String getRealIP(Player player) {
        String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();

        // Log the IP address for debugging purposes
        LOGGER.info("Player {} connecting from IP: {}", player.getUsername(), ipAddress);

        // Additional debug info
        LOGGER.debug("Full remote address for {}: {}", player.getUsername(), player.getRemoteAddress());

        return ipAddress;
    }

    /**
     * Get the real IP address of a player without logging (for
     * performance-sensitive operations).
     *
     * @param player The player to get the IP address for
     * @return The IP address as a string
     */
    public static String getRealIPQuiet(Player player) {
        return player.getRemoteAddress().getAddress().getHostAddress();
    }
}
