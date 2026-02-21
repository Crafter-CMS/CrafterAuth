package net.elytrium.limboauth.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.elytrium.limboauth.LimboAuth;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.List;
import net.elytrium.limboauth.staff.StaffDatabase;

public class StaffCommand implements SimpleCommand {
    private final LimboAuth plugin;

    public StaffCommand(LimboAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 1) {
            source.sendMessage(Component.text("Kullanım: /crafterauth staff <add|remove|list|reload> ...", NamedTextColor.RED));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Kullanım: /crafterauth staff add <discordid> <username>", NamedTextColor.RED));
                    return;
                }
                String discordId = args[1];
                String username = args[2];
                boolean added = plugin.getStaffDatabase().addStaff(discordId, username);
                if (added) {
                    source.sendMessage(Component.text("Staff eklendi: " + username + " <" + discordId + ">", NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("Bu kullanıcı zaten staff olarak ekli.", NamedTextColor.YELLOW));
                }
                break;
            case "remove":
                if (args.length < 2) {
                    source.sendMessage(Component.text("Kullanım: /crafterauth staff remove <username>", NamedTextColor.RED));
                    return;
                }
                String removeUsername = args[1];
                boolean removed = plugin.getStaffDatabase().removeStaff(removeUsername);
                if (removed) {
                    source.sendMessage(Component.text("Staff silindi: " + removeUsername, NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("Bu kullanıcı staff listesinde yok.", NamedTextColor.YELLOW));
                }
                break;
            case "list":
                List<StaffDatabase.StaffEntry> staffList = plugin.getStaffDatabase().getStaffList();
                if (staffList.isEmpty()) {
                    source.sendMessage(Component.text("Staff listesi boş.", NamedTextColor.YELLOW));
                } else {
                    source.sendMessage(Component.text("Staff Listesi:", NamedTextColor.AQUA));
                    for (StaffDatabase.StaffEntry entry : staffList) {
                        String uuidInfo = entry.uuid != null ? " [UUID: " + entry.uuid + "]" : " [UUID: Henüz giriş yapmadı]";
                        boolean isOnline = entry.uuid != null && plugin.getServer().getPlayer(java.util.UUID.fromString(entry.uuid)).isPresent();
                        String status = isOnline ? " §a[Online]" : " §7[Offline]";
                        source.sendMessage(Component.text("- " + entry.username + " <" + entry.id + ">" + uuidInfo + status, NamedTextColor.WHITE));
                    }
                }
                break;
            case "reload":
                plugin.getStaffDatabase().reload();
                source.sendMessage(Component.text("Staff veritabanı yeniden yüklendi.", NamedTextColor.GREEN));
                break;
            default:
                source.sendMessage(Component.text("Kullanım: /crafterauth staff <add|remove|list|reload>", NamedTextColor.RED));
        }
    }
}
