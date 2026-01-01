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

import net.elytrium.limboauth.LimboAuth;

public class MessageConfig {
    private final LimboAuth plugin;

    public MessageConfig(LimboAuth plugin) {
        this.plugin = plugin;
    }

    public String getStaffLoginTitle() {
        return plugin.getLanguageManager().getMessages().discordEmbedTitle;
    }

    public String getStaffLoginDescription() {
        return plugin.getLanguageManager().getMessages().discordEmbedDescription;
    }

    public String getApproveButton() {
        return plugin.getLanguageManager().getMessages().discordButtonApprove;
    }

    public String getDenyButton() {
        return plugin.getLanguageManager().getMessages().discordButtonDeny;
    }

    public String getTimeoutMessage() {
        return plugin.getLanguageManager().getMessages().discordTimeoutMessage;
    }

    public String getApprovedMessage() {
        return plugin.getLanguageManager().getMessages().discordApprovedMessage;
    }

    public String getDeniedMessage() {
        return plugin.getLanguageManager().getMessages().discordDeniedMessage;
    }

    public String getIpMismatchMessage() {
        return plugin.getLanguageManager().getMessages().discordIpMismatchMessage;
    }

    public String formatDescription(String username) {
        return getStaffLoginDescription().replace("{username}", username);
    }
}
