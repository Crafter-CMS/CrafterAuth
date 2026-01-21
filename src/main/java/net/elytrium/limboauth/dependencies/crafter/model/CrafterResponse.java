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

package net.elytrium.limboauth.dependencies.crafter.model;

import com.google.gson.JsonObject;

/**
 * Response object from Crafter CMS API.
 */
public class CrafterResponse {
    private final boolean success;
    private final String message;
    private final String type;
    private final JsonObject data;

    /**
     * Creates a new CrafterResponse with success status and message.
     *
     * @param success Whether the request was successful
     * @param message The response message
     */
    public CrafterResponse(boolean success, String message) {
        this(success, message, null, null);
    }

    /**
     * Creates a new CrafterResponse with success, message and type (for errors).
     *
     * @param success Whether the request was successful
     * @param message The response message
     * @param type    The specific error type (e.g. AUTHME_USER_ALREADY_EXISTS)
     */
    public CrafterResponse(boolean success, String message, String type) {
        this(success, message, type, null);
    }

    /**
     * Creates a new CrafterResponse with success, message, type and data.
     *
     * @param success Whether the request was successful
     * @param message The response message
     * @param type    The error type (may be null)
     * @param data    Additional response data (may be null)
     */
    public CrafterResponse(boolean success, String message, String type, JsonObject data) {
        this.success = success;
        this.message = message;
        this.type = type;
        this.data = data;
    }

    /**
     * Creates a new CrafterResponse with success status, message, and data.
     *
     * @param success Whether the request was successful
     * @param message The response message
     * @param data    Additional response data
     */
    public CrafterResponse(boolean success, String message, JsonObject data) {
        this(success, message, null, data);
    }

    /**
     * Check if the request was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * Get the error type.
     *
     * @return The error type or null
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the response message.
     *
     * @return The response message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the response data.
     *
     * @return The response data as JsonObject, or null if not present
     */
    public JsonObject getData() {
        return this.data;
    }

    /**
     * Check if the response contains user data.
     *
     * @return true if data is present and not null, false otherwise
     */
    public boolean hasUserData() {
        return this.data != null;
    }

    @Override
    public String toString() {
        return "CrafterResponse{success=" + this.success + ", message='" + this.message + "', data=" + this.data + "}";
    }
}
