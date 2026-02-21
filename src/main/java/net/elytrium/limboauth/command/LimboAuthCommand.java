// ...existing code...
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

package net.elytrium.limboauth.command;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.elytrium.limboauth.LimboAuth;
import net.elytrium.limboauth.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LimboAuthCommand extends RatelimitedCommand {
  // /crafterauth discordtest <discordId> komutu
  private void handleDiscordTest(CommandSource source, String[] args) {
    if (args.length < 2) {
      source.sendMessage(Component.text("Kullanım: /crafterauth discordtest <discordId>", NamedTextColor.RED));
      return;
    }
    String discordId = args[1];
    if (plugin != null && plugin.getDiscordBot() != null) {
      plugin.getDiscordBot().sendTestDM(discordId, "CrafterAuth test mesajı: Discord botunuz başarıyla çalışıyor!");
      source.sendMessage(Component.text("Test mesajı gönderildi (ID: " + discordId + ")", NamedTextColor.GREEN));
    } else {
      source.sendMessage(Component.text("Discord botu başlatılamadı veya etkin değil!", NamedTextColor.RED));
    }
  }

  private static final List<Component> HELP_MESSAGE = List.of(
      Component.text("This server is using CrafterAuth (based on LimboAuth).", NamedTextColor.YELLOW),
      Component.text("(C) 2025 Crafter CMS - Original by Elytrium", NamedTextColor.YELLOW),
      Component.text("https://crafter.net.tr/", NamedTextColor.GREEN),
      Component.empty()
  );

  private static final Component AVAILABLE_SUBCOMMANDS_MESSAGE = Component.text("Kullanılabilir komutlar:", NamedTextColor.WHITE);
  private static final Component NO_AVAILABLE_SUBCOMMANDS_MESSAGE = Component.text("Kullanabileceğiniz bir komut bulunmuyor.", NamedTextColor.WHITE);

  private final LimboAuth plugin;

  public LimboAuthCommand(LimboAuth plugin) {
    super(plugin);
    this.plugin = plugin;
  }

  @Override
  public List<String> suggest(SimpleCommand.Invocation invocation) {
    CommandSource source = invocation.source();
    String[] args = invocation.arguments();

    if (args.length == 0) {
      return Arrays.stream(Subcommand.values())
          .filter(command -> command.hasPermission(source))
          .map(Subcommand::getCommand)
          .collect(Collectors.toList());
    } else if (args.length == 1) {
      String argument = args[0];
      return Arrays.stream(Subcommand.values())
          .filter(command -> command.hasPermission(source))
          .map(Subcommand::getCommand)
          .filter(str -> str.regionMatches(true, 0, argument, 0, argument.length()))
          .collect(Collectors.toList());
    } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
      // /crafterauth reload <tab> için "lang" önerisi
      return ImmutableList.of("lang");
    } else {
      return ImmutableList.of();
    }
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    int argsAmount = args.length;
    if (argsAmount > 0) {
      if (args[0].equalsIgnoreCase("discordtest")) {
        handleDiscordTest(source, args);
        return;
      }
      try {
        Subcommand subcommand = Subcommand.valueOf(args[0].toUpperCase(Locale.ROOT));
        if (!subcommand.hasPermission(source)) {
          this.showHelp(source);
          return;
        }
        subcommand.executor.execute(this, source, args);
      } catch (IllegalArgumentException e) {
        this.showHelp(source);
      }
    } else {
      this.showHelp(source);
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.HELP
        .hasPermission(invocation.source(), "limboauth.commands.help");
  }

  private void showHelp(CommandSource source) {
    HELP_MESSAGE.forEach(source::sendMessage);

    List<Subcommand> availableSubcommands = Arrays.stream(Subcommand.values())
        .filter(command -> command.hasPermission(source))
        .collect(Collectors.toList());

    if (availableSubcommands.size() > 0) {
      source.sendMessage(AVAILABLE_SUBCOMMANDS_MESSAGE);
      availableSubcommands.forEach(command -> source.sendMessage(command.getMessageLine()));
    } else {
      source.sendMessage(NO_AVAILABLE_SUBCOMMANDS_MESSAGE);
    }
  }

  private enum Subcommand {
    RELOAD("Ayarları yeniden yükle. 'reload lang' ile sadece dil dosyalarını yükle.", Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.RELOAD,
        (LimboAuthCommand parent, CommandSource source, String[] args) -> {
          // /crafterauth reload lang - sadece dil dosyalarını yükle
          if (args.length > 1 && args[1].equalsIgnoreCase("lang")) {
            try {
              parent.plugin.getLanguageManager().loadLanguage();
              source.sendMessage(Component.text("✅ Dil dosyaları başarıyla yeniden yüklendi! Aktif dil: " 
                  + parent.plugin.getLanguageManager().getCurrentLanguage(), NamedTextColor.GREEN));
            } catch (Exception e) {
              source.sendMessage(Component.text("❌ Dil dosyaları yüklenirken hata oluştu: " + e.getMessage(), NamedTextColor.RED));
            }
          } else {
            // /crafterauth reload - tüm plugin'i reload et
            parent.plugin.reload();
            source.sendMessage(LimboAuth.getSerializer().deserialize(parent.plugin.getLanguageManager().getMessages().reload));
          }
        }),
    STAFF("Staff yönetimi (add/remove/list/reload).", Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.RELOAD,
        (LimboAuthCommand parent, CommandSource source, String[] args) -> {
          if (args.length < 2) {
            source.sendMessage(Component.text("Kullanım: /crafterauth staff <add|remove|list|reload> ...", NamedTextColor.RED));
            return;
          }
          switch (args[1].toLowerCase(java.util.Locale.ROOT)) {
            case "add":
              if (args.length < 4) {
                source.sendMessage(Component.text("Kullanım: /crafterauth staff add <discordid> <username>", NamedTextColor.RED));
                return;
              }
              String discordId = args[2];
              String username = args[3];
              boolean added = parent.plugin.getStaffDatabase().addStaff(discordId, username);
              if (added) {
                source.sendMessage(Component.text("Staff eklendi: " + username + " <" + discordId + ">", NamedTextColor.GREEN));
              } else {
                source.sendMessage(Component.text("Bu kullanıcı zaten staff olarak ekli.", NamedTextColor.YELLOW));
              }
              break;
            case "remove":
              if (args.length < 3) {
                source.sendMessage(Component.text("Kullanım: /crafterauth staff remove <username>", NamedTextColor.RED));
                return;
              }
              String removeUsername = args[2];
              boolean removed = parent.plugin.getStaffDatabase().removeStaff(removeUsername);
              if (removed) {
                source.sendMessage(Component.text("Staff silindi: " + removeUsername, NamedTextColor.GREEN));
              } else {
                source.sendMessage(Component.text("Bu kullanıcı staff listesinde yok.", NamedTextColor.YELLOW));
              }
              break;
            case "list":
              java.util.List<net.elytrium.limboauth.staff.StaffDatabase.StaffEntry> staffList = parent.plugin.getStaffDatabase().getStaffList();
              if (staffList.isEmpty()) {
                source.sendMessage(Component.text("Staff listesi boş.", NamedTextColor.YELLOW));
              } else {
                source.sendMessage(Component.text("Staff Listesi:", NamedTextColor.AQUA));
                for (net.elytrium.limboauth.staff.StaffDatabase.StaffEntry entry : staffList) {
                  String uuidInfo = entry.uuid != null ? " [UUID: " + entry.uuid + "]" : " [UUID: Henüz giriş yapmadı]";
                  boolean isOnline = entry.uuid != null && parent.plugin.getServer().getPlayer(java.util.UUID.fromString(entry.uuid)).isPresent();
                  String status = isOnline ? " §a[Online]" : " §7[Offline]";
                  source.sendMessage(Component.text("- " + entry.username + " <" + entry.id + ">" + uuidInfo + status, NamedTextColor.WHITE));
                }
              }
              break;
            case "reload":
              parent.plugin.getStaffDatabase().reload();
              source.sendMessage(Component.text("Staff veritabanı yeniden yüklendi.", NamedTextColor.GREEN));
              break;
            default:
              source.sendMessage(Component.text("Kullanım: /crafterauth staff <add|remove|list|reload>", NamedTextColor.RED));
          }
        }),
    STATUS("API durumunu ve lisans bilgilerini göster.", Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.RELOAD,
        (LimboAuthCommand parent, CommandSource source, String[] args) -> {
          if (parent.plugin.getCrafterAPIClient() != null) {
            source.sendMessage(Component.text(" ⏳  API bağlantısı kontrol ediliyor...", NamedTextColor.YELLOW));
            source.sendMessage(Component.empty());
            
            parent.plugin.getCrafterAPIClient().testConnection().thenAccept(result -> {
              boolean isSuccess = result.contains("SUCCESS");
              
              if (isSuccess) {
                source.sendMessage(Component.text(" ╔═══════════════════════════════════════════════════════╗", NamedTextColor.GREEN));
                source.sendMessage(Component.text(" ║                                                       ║", NamedTextColor.GREEN));
                source.sendMessage(Component.text(" ║              ✅  API BAĞLANTISI BAŞARILI  ✅          ║", NamedTextColor.GREEN));
                source.sendMessage(Component.text(" ║                                                       ║", NamedTextColor.GREEN));
                source.sendMessage(Component.text(" ╚═══════════════════════════════════════════════════════╝", NamedTextColor.GREEN));
                source.sendMessage(Component.empty());
                
                // Website info
                var website = parent.plugin.getCrafterAPIClient().getWebsite();
                if (website != null) {
                  String websiteId = website.getId();
                  String maskedId = websiteId.length() > 7 ? websiteId.substring(0, 7) + "****" : websiteId;
                  
                  source.sendMessage(Component.text(" 🏢  Website: " + website.getName(), NamedTextColor.WHITE));
                  source.sendMessage(Component.text(" 🆔  Site ID: " + maskedId, NamedTextColor.GRAY));
                  source.sendMessage(Component.text(" ✅  Lisans : Aktif", NamedTextColor.GREEN));
                  
                  // License expiration
                  String expiresAt = website.getLicenseExpiresAt();
                  if (expiresAt != null && !expiresAt.isEmpty()) {
                    try {
                      java.time.Instant expirationInstant = java.time.Instant.parse(expiresAt);
                      java.time.Instant now = java.time.Instant.now();
                      long daysRemaining = java.time.Duration.between(now, expirationInstant).toDays();
                      
                      if (daysRemaining > 0) {
                        source.sendMessage(Component.text(" 📅  Kalan Süre: " + daysRemaining + " gün", NamedTextColor.YELLOW));
                      } else if (daysRemaining == 0) {
                        source.sendMessage(Component.text(" ⚠️  Lisans bugün sona eriyor!", NamedTextColor.RED));
                      } else {
                        source.sendMessage(Component.text(" ❌  Lisans süresi dolmuş!", NamedTextColor.RED));
                      }
                    } catch (Exception e) {
                      // Ignore parsing errors
                    }
                  }
                }
              } else {
                source.sendMessage(Component.text(" ╔═══════════════════════════════════════════════════════╗", NamedTextColor.RED));
                source.sendMessage(Component.text(" ║                                                       ║", NamedTextColor.RED));
                source.sendMessage(Component.text(" ║              ❌  API BAĞLANTISI BAŞARISIZ  ❌         ║", NamedTextColor.RED));
                source.sendMessage(Component.text(" ║                                                       ║", NamedTextColor.RED));
                source.sendMessage(Component.text(" ╚═══════════════════════════════════════════════════════╝", NamedTextColor.RED));
                source.sendMessage(Component.empty());
                source.sendMessage(Component.text(" ⚠️  Lütfen ayarları kontrol edin!", NamedTextColor.YELLOW));
              }
              source.sendMessage(Component.empty());
            });
          } else {
            source.sendMessage(Component.text(" ╔═══════════════════════════════════════════════════════╗", NamedTextColor.RED));
            source.sendMessage(Component.text(" ║                                                       ║", NamedTextColor.RED));
            source.sendMessage(Component.text(" ║                 ❌  API MEVCUT DEĞİL  ❌             ║", NamedTextColor.RED));
            source.sendMessage(Component.text(" ║                                                       ║", NamedTextColor.RED));
            source.sendMessage(Component.text(" ╚═══════════════════════════════════════════════════════╝", NamedTextColor.RED));
            source.sendMessage(Component.empty());
            source.sendMessage(Component.text(" ℹ️  Crafter CMS modu aktif değil.", NamedTextColor.GRAY));
            source.sendMessage(Component.empty());
          }
        });

    private final String command;
    private final String description;
    private final CommandPermissionState permissionState;
    private final SubcommandExecutor executor;

    Subcommand(String description, CommandPermissionState permissionState, SubcommandExecutor executor) {
      this.permissionState = permissionState;
      this.command = this.name().toLowerCase(Locale.ROOT);
      this.description = description;
      this.executor = executor;
    }

    public boolean hasPermission(CommandSource source) {
      return this.permissionState.hasPermission(source, "limboauth.admin." + this.command);
    }

    public Component getMessageLine() {
      return Component.textOfChildren(
          Component.text("  /crafterauth " + this.command, NamedTextColor.GREEN),
          Component.text(" - ", NamedTextColor.DARK_GRAY),
          Component.text(this.description, NamedTextColor.YELLOW)
      );
    }

    public String getCommand() {
      return this.command;
    }
  }

  private interface SubcommandExecutor {
    void execute(LimboAuthCommand parent, CommandSource source, String[] args);
  }
}
