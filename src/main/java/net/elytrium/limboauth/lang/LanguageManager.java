/*
 * Copyright (C) 2021 - 2024 Elytrium
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

package net.elytrium.limboauth.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class LanguageManager {

  private final File langFolder;
  private final Logger logger;
  private Messages currentMessages;
  private String currentLanguage;

  public LanguageManager(File dataFolder, Logger logger, String language) {
    this.langFolder = new File(dataFolder, "lang");
    this.logger = logger;
    this.currentLanguage = language.toLowerCase();
    
    if (!this.langFolder.exists()) {
      this.langFolder.mkdirs();
    }
    
    loadLanguage();
  }

  public void loadLanguage() {
    // Copy default language files from resources
    copyDefaultLanguageFile("messages_tr.yml");
    copyDefaultLanguageFile("messages_en.yml");
    copyDefaultLanguageFile("messages_de.yml");
    copyDefaultLanguageFile("messages_es.yml");
    copyDefaultLanguageFile("messages_fr.yml");
    
    // Load the selected language file
    String fileName = "messages_" + this.currentLanguage + ".yml";
    File langFile = new File(this.langFolder, fileName);
    
    if (!langFile.exists()) {
      this.logger.warn("Language file '{}' not found, falling back to English", fileName);
      this.currentLanguage = "en";
      langFile = new File(this.langFolder, "messages_en.yml");
    }
    
    try {
      Yaml yaml = new Yaml(new Constructor(Messages.class));
      try (InputStream inputStream = Files.newInputStream(langFile.toPath())) {
        this.currentMessages = yaml.load(inputStream);
        this.logger.info("Loaded language: {}", this.currentLanguage.toUpperCase());
      }
    } catch (Exception e) {
      this.logger.error("Failed to load language file: {}", fileName, e);
      this.logger.error("Creating default English messages...");
      this.currentMessages = new Messages();
    }
  }

  private void copyDefaultLanguageFile(String fileName) {
    File targetFile = new File(this.langFolder, fileName);
    Path targetPath = targetFile.toPath();
    
    if (!Files.exists(targetPath)) {
      try (InputStream in = getClass().getResourceAsStream("/lang/" + fileName)) {
        if (in != null) {
          Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
          this.logger.info("Created default language file: {}", fileName);
        }
      } catch (IOException e) {
        this.logger.error("Failed to copy default language file: {}", fileName, e);
      }
    }
  }

  public Messages getMessages() {
    return this.currentMessages;
  }

  public String getCurrentLanguage() {
    return this.currentLanguage.toUpperCase();
  }
}
