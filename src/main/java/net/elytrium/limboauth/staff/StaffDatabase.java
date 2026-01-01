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

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.UUID;

public class StaffDatabase {
    private final File discordYmlFile;
    private List<StaffEntry> staffList = new ArrayList<>();

    public static class StaffEntry {
        public String id;
        public String username;
        public String uuid;
        public StaffEntry() {}
        public StaffEntry(String id, String username) {
            this.id = id;
            this.username = username;
            this.uuid = null;
        }
        public StaffEntry(String id, String username, String uuid) {
            this.id = id;
            this.username = username;
            this.uuid = uuid;
        }
    }

    public StaffDatabase(File discordYmlFile) {
        this.discordYmlFile = discordYmlFile;
        load();
    }

    public synchronized boolean addStaff(String discordId, String username) {
        return addStaff(discordId, username, null);
    }

    public synchronized boolean addStaff(String discordId, String username, String uuid) {
        for (StaffEntry entry : staffList) {
            if (entry.id.equals(discordId) || entry.username.equalsIgnoreCase(username)) return false;
        }
        staffList.add(new StaffEntry(discordId, username, uuid));
        save();
        return true;
    }

    public synchronized void setUUID(String username, UUID uuid) {
        for (StaffEntry entry : staffList) {
            if (entry.username.equalsIgnoreCase(username)) {
                entry.uuid = uuid.toString();
                save();
                break;
            }
        }
    }

    public synchronized String getDiscordId(UUID uuid) {
        if (uuid == null) return null;
        for (StaffEntry entry : staffList) {
            if (entry.uuid != null && entry.uuid.equals(uuid.toString())) {
                return entry.id;
            }
        }
        return null;
    }

    public synchronized boolean isStaff(UUID uuid) {
        return getDiscordId(uuid) != null;
    }

    public synchronized boolean isStaff(String username) {
        for (StaffEntry entry : staffList) {
            if (entry.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void reload() {
        load();
    }

    public synchronized boolean removeStaff(String username) {
        boolean removed = staffList.removeIf(entry -> entry.username.equalsIgnoreCase(username));
        if (removed) save();
        return removed;
    }

    public synchronized List<StaffEntry> getStaffList() {
        return Collections.unmodifiableList(staffList);
    }

    private void load() {
        if (!discordYmlFile.exists()) return;
        try (FileReader reader = new FileReader(discordYmlFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(reader);
            staffList = new ArrayList<>();
            if (config != null && config.containsKey("staff")) {
                Object staffObj = config.get("staff");
                if (staffObj instanceof List) {
                    for (Object o : (List<?>) staffObj) {
                        if (o instanceof Map) {
                            Map<?,?> m = (Map<?,?>) o;
                            String id = String.valueOf(m.get("id"));
                            String username = String.valueOf(m.get("username"));
                            String uuid = m.containsKey("uuid") ? String.valueOf(m.get("uuid")) : null;
                            staffList.add(new StaffEntry(id, username, uuid));
                        }
                    }
                }
            }
        } catch (IOException e) {
            staffList = new ArrayList<>();
        }
    }

    private void save() {
        try (FileReader reader = new FileReader(discordYmlFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(reader);
            if (config == null) config = new LinkedHashMap<>();
            List<Map<String, String>> staffYaml = new ArrayList<>();
            for (StaffEntry entry : staffList) {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("id", entry.id);
                map.put("username", entry.username);
                if (entry.uuid != null) {
                    map.put("uuid", entry.uuid);
                }
                staffYaml.add(map);
            }
            config.put("staff", staffYaml);
            try (FileWriter writer = new FileWriter(discordYmlFile)) {
                yaml.dump(config, writer);
            }
        } catch (IOException ignored) {}
    }
}
