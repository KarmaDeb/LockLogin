package ml.karmaconfigs.lockloginsystem.bukkit.commands;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.reflections.BarMessage;
import ml.karmaconfigs.api.common.utils.FileUtilities;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin.MigrationRequestEvent;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountID;
import ml.karmaconfigs.lockloginsystem.shared.account.AzuriomId;
import ml.karmaconfigs.lockloginsystem.shared.llsql.sqlite.SQLiteReader;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.LockLoginBukkitManager;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.inventory.ModuleListInventory;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.PlayerFile;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class LockLoginCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private static CommandSender migrating_owner = null;
    private static int passed_migration = 0;
    private static int max_migrations = 0;

    private final Permission migratePermission = new Permission("locklogin.migrate", PermissionDefault.FALSE);
    private final Permission applyUpdatePermission = new Permission("locklogin.update", PermissionDefault.FALSE);
    private final Permission modulePermission = new Permission("locklogin.modules", PermissionDefault.FALSE);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                user.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload&e, &7modules");
            } else {
                switch (args[0].toLowerCase()) {
                    case "migrate":
                        if (player.hasPermission(migratePermission)) {
                            if (migrating_owner == null) {
                                MigrationRequestEvent event = new MigrationRequestEvent(arg, args, sender);
                                LockLoginListener.callEvent(event);

                                if (args.length == 1)
                                    user.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <AuthMe, LoginSecurity, [Specified by an external account manager]>");
                                else {
                                    String sub_arg = args[1];
                                    switch (args.length) {
                                        case 2:
                                            switch (sub_arg.toLowerCase()) {
                                                case "authme":
                                                    user.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                    break;
                                                case "loginsecurity":
                                                    user.send(messages.prefix() + "&cCorrect usage: /locklogin migrate loginsecurity <table name>");
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                        case 3:
                                        case 4:
                                        case 5:
                                            switch (sub_arg.toLowerCase()) {
                                                case "authme":
                                                    user.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                    break;
                                                case "loginsecurity":
                                                    String table = args[2];

                                                    user.send(messages.prefix() + "&aMigrating from LoginSecurity");

                                                    BarMessage message = new BarMessage(player, "&eMigrating progress:&c Starting");
                                                    message.send(true);

                                                    if (migrateLoginSecurity(sender, table)) {
                                                        new BukkitRunnable() {
                                                            @Override
                                                            public void run() {
                                                                if (passed_migration == max_migrations) {
                                                                    cancel();
                                                                    migrating_owner.sendMessage(StringUtils.toColor(messages.prefix() + messages.migrated()));
                                                                    message.setMessage("&8Migrating progress: &aComplete");
                                                                    message.stop();
                                                                    migrating_owner = null;
                                                                }

                                                                if (migrating_owner != null) {
                                                                    double division = (double) passed_migration / max_migrations;
                                                                    long iPart = (long) division;
                                                                    double fPart = division - iPart;

                                                                    String colour = "&c";
                                                                    if (fPart >= 37.5)
                                                                        colour = "&e";
                                                                    if (fPart >= 75)
                                                                        colour = "&a";

                                                                    message.setMessage("&8Migrating progress:" + colour + " " + fPart + "%");
                                                                }
                                                            }
                                                        }.runTaskTimer(plugin, 0, 20);
                                                    } else {
                                                        user.send(messages.prefix() + "&cSome error occurred while migrating");
                                                        migrating_owner = null;
                                                    }
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                        case 6:
                                        default:
                                            switch (sub_arg.toLowerCase()) {
                                                case "authme":
                                                    String database_file = args[2];
                                                    String table_name = args[3];
                                                    String real_name = args[4];
                                                    String passwords = args[5];

                                                    user.send(messages.prefix() + "&aMigrating from authme");

                                                    BarMessage authmeMessage = new BarMessage(player, "&eMigrating progress:&c Starting");
                                                    authmeMessage.send(true);

                                                    if (migrateAuthMe(sender, database_file, table_name, real_name, passwords)) {
                                                        new BukkitRunnable() {
                                                            @Override
                                                            public void run() {
                                                                if (passed_migration == max_migrations) {
                                                                    cancel();
                                                                    migrating_owner.sendMessage(StringUtils.toColor(messages.prefix() + messages.migrated()));
                                                                    authmeMessage.setMessage("&8Migrating progress: &aComplete");
                                                                    authmeMessage.stop();
                                                                    migrating_owner = null;
                                                                }

                                                                if (migrating_owner != null) {
                                                                    double division = (double) passed_migration / max_migrations;
                                                                    long iPart = (long) division;
                                                                    double fPart = division - iPart;

                                                                    String colour = "&c";
                                                                    if (fPart >= 37.5)
                                                                        colour = "&e";
                                                                    if (fPart >= 75)
                                                                        colour = "&a";

                                                                    authmeMessage.setMessage("&8Migrating progress:" + colour + " " + fPart + "%");
                                                                }
                                                            }
                                                        }.runTaskTimer(plugin, 0, 20);
                                                    } else {
                                                        user.send(messages.prefix() + "&cSome error occurred while migrating");
                                                        migrating_owner = null;
                                                    }
                                                    break;
                                                case "loginsecurity":
                                                    String table = args[2];

                                                    user.send(messages.prefix() + "&aMigrating from LoginSecurity");

                                                    BarMessage lsMessage = new BarMessage(player, "&eMigrating progress:&c Starting");
                                                    lsMessage.send(true);

                                                    if (migrateLoginSecurity(sender, table)) {
                                                        new BukkitRunnable() {
                                                            @Override
                                                            public void run() {
                                                                if (passed_migration == max_migrations) {
                                                                    cancel();
                                                                    migrating_owner.sendMessage(StringUtils.toColor(messages.prefix() + messages.migrated()));
                                                                    lsMessage.setMessage("&8Migrating progress: &aComplete");
                                                                    lsMessage.stop();
                                                                    migrating_owner = null;
                                                                }

                                                                if (migrating_owner != null) {
                                                                    double division = (double) passed_migration / max_migrations;
                                                                    long iPart = (long) division;
                                                                    double fPart = division - iPart;

                                                                    String colour = "&c";
                                                                    if (fPart >= 37.5)
                                                                        colour = "&e";
                                                                    if (fPart >= 75)
                                                                        colour = "&a";

                                                                    lsMessage.setMessage("&8Migrating progress:" + colour + " " + fPart + "%");
                                                                }
                                                            }
                                                        }.runTaskTimer(plugin, 0, 20);
                                                    } else {
                                                        user.send(messages.prefix() + "&cSome error occurred while migrating");
                                                        migrating_owner = null;
                                                    }
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                    }
                                }
                            } else {
                                user.send(messages.prefix() + "&cMigration already in progress by " + migrating_owner.getName());
                            }
                        } else {
                            user.send(messages.prefix() + messages.permission(migratePermission.getName()));
                        }
                        break;
                    case "applyupdates":
                        if (player.hasPermission(applyUpdatePermission)) {
                            LockLoginBukkitManager s_manager = new LockLoginBukkitManager();
                            s_manager.applyUpdate(user);
                        } else {
                            user.send(messages.prefix() + messages.permission(applyUpdatePermission.getName()));
                        }
                        break;
                    case "reload":
                        if (player.hasPermission(applyUpdatePermission)) {
                            LockLoginBukkitManager s_manager = new LockLoginBukkitManager();
                            s_manager.reload(user);
                        } else {
                            user.send(messages.prefix() + messages.permission(applyUpdatePermission.getName()));
                        }
                        break;
                    case "modules":
                        if (player.hasPermission(modulePermission)) {
                            ModuleListInventory inv = new ModuleListInventory(player);
                            inv.openPage(0);
                        } else {
                            user.send(messages.prefix() + messages.permission(modulePermission.getName()));
                        }
                        break;
                    default:
                        user.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload&e, &7modules");
                        break;
                }
            }
        } else {
            if (args.length == 0) {
                Console.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload");
            } else {
                switch (args[0].toLowerCase()) {
                    case "migrate":
                        if (migrating_owner == null) {
                            MigrationRequestEvent event = new MigrationRequestEvent(arg, args, sender);
                            LockLoginListener.callEvent(event);

                            if (args.length == 1) {
                                Console.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <AuthMe, LoginSecurity, [Specified by an external account manager]>");
                            } else {
                                String sub_arg = args[1];
                                switch (args.length) {
                                    case 2:
                                        switch (sub_arg.toLowerCase()) {
                                            case "authme":
                                                Console.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                break;
                                            case "loginsecurity":
                                                Console.send(messages.prefix() + "&cCorrect usage: /locklogin migrate loginsecurity <table name>");
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                    case 3:
                                    case 4:
                                    case 5:
                                        switch (sub_arg.toLowerCase()) {
                                            case "authme":
                                                Console.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                break;
                                            case "loginsecurity":
                                                String table = args[2];

                                                if (migrateLoginSecurity(sender, table)) {
                                                    Console.send(messages.prefix() + messages.migrated());
                                                } else {
                                                    Console.send(messages.prefix() + "&cSome error occurred while migrating");
                                                    migrating_owner = null;
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                    case 6:
                                    default:
                                        switch (sub_arg.toLowerCase()) {
                                            case "authme":
                                                String database_file = args[2];
                                                String table_name = args[3];
                                                String real_name = args[4];
                                                String passwords = args[5];

                                                Console.send(messages.prefix() + "&aMigrating from authme sqlite");

                                                if (migrateAuthMe(sender, database_file, table_name, real_name, passwords)) {
                                                    Console.send(messages.prefix() + messages.migrated());
                                                } else {
                                                    Console.send(messages.prefix() + "&cSome error occurred while migrating");
                                                    migrating_owner = null;
                                                }
                                                break;
                                            case "loginsecurity":
                                                String table = args[2];

                                                if (migrateLoginSecurity(sender, table)) {
                                                    Console.send(messages.prefix() + messages.migrated());
                                                } else {
                                                    Console.send(messages.prefix() + "&cSome error occurred while migrating");
                                                    migrating_owner = null;
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                }
                            }
                        } else {
                            Console.send(messages.prefix() + "&cMigration already in progress by " + migrating_owner.getName());
                        }
                        break;
                    case "applyupdates":
                        LockLoginBukkitManager s_manager = new LockLoginBukkitManager();
                        s_manager.applyUpdate(null);
                        break;
                    case "reload":
                        LockLoginBukkitManager r_manager = new LockLoginBukkitManager();
                        r_manager.reload(null);
                        break;
                    default:
                        Console.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload");
                        break;
                }
            }
        }

        return false;
    }

    /**
     * Do an authme sqlite migration
     *
     * @param database the database name
     * @param table    the database table where the info is
     */
    private boolean migrateAuthMe(CommandSender sender, String database, String table, String realnameColumn, String passwordColumn) {
        migrating_owner = sender;

        File authMe = new File(FileUtilities.getPluginsFolder(), "AuthMe");
        if (!authMe.exists())
            authMe = new File(LockLoginSpigot.plugin.getDataFolder().getParentFile(), "AuthMe");

        if (authMe.exists()) {
            sender.sendMessage(StringUtils.toColor(messages.prefix() + "&aAuthMe folder found"));

            File data = new File(authMe, database + ".db");

            if (data.exists()) {
                sender.sendMessage(StringUtils.toColor(messages.prefix() + "&aAuthMe database file &7( &f" + data.getName() + " &7)&a found"));

                SQLiteReader reader = new SQLiteReader(data, table, realnameColumn, passwordColumn);
                if (reader.tryConnection()) {
                    sender.sendMessage(StringUtils.toColor(messages.prefix() + "&aConnection to sqlite successfully"));

                    LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                        Set<String> players = reader.getPlayers();
                        max_migrations = players.size();
                        for (String name : players) {
                            String password = reader.getPassword(name);

                            if (password != null && !password.isEmpty()) {
                                UUID id;
                                if (PlatformUtils.isPremium()) {
                                    id = retrieveUUID(name);
                                } else {
                                    id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                                }

                                AzuriomId storage = new AzuriomId(AccountID.fromUUID(id));
                                storage.assignTo(name);

                                PlayerFile pf = new PlayerFile(storage.getAccountFile());
                                if (!pf.exists())
                                    pf.create();

                                pf.saveUUID(AccountID.fromUUID(id));
                                pf.setName(name);
                                pf.setPassword(password);
                            }

                            passed_migration = passed_migration + 1;
                        }

                        migrating_owner = null;
                    });

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Do an login security sqlite migration
     */
    private boolean migrateLoginSecurity(CommandSender sender, final String table) {
        migrating_owner = sender;

        File loginsecurity = new File(FileUtilities.getPluginsFolder(), "LoginSecurity");

        if (!loginsecurity.exists())
            loginsecurity = new File(LockLoginSpigot.plugin.getDataFolder().getParentFile(), "LoginSecurity");

        if (loginsecurity.exists()) {
            File data = new File(loginsecurity, "LoginSecurity.db");

            if (data.exists()) {
                SQLiteReader reader = new SQLiteReader(data, table, "last_name", "password");
                LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                    Set<String> players = reader.getPlayers();
                    max_migrations = players.size();
                    for (String name : players) {
                        String password = reader.getPassword(name);

                        if (password != null && !password.isEmpty()) {
                            UUID id;
                            if (PlatformUtils.isPremium()) {
                                id = retrieveUUID(name);
                            } else {
                                id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                            }

                            AzuriomId storage = new AzuriomId(AccountID.fromUUID(id));
                            storage.assignTo(name);

                            PlayerFile pf = new PlayerFile(storage.getAccountFile());
                            if (!pf.exists())
                                pf.create();

                            pf.saveUUID(AccountID.fromUUID(id));
                            pf.setName(name);
                            pf.setPassword(password);
                        }

                        passed_migration = passed_migration + 1;
                    }

                    migrating_owner = null;
                });

                return true;
            }
        }

        return false;
    }

    /**
     * Get the mojang player uuid
     *
     * @param name the player name
     * @return the mojang uuid from the specified player name
     */
    private UUID retrieveUUID(String name) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

            String UUIDJson = IOUtils.toString(new URL(url));

            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);

            try {
                return UUID.fromString(UUIDObject.get("id").toString());
            } catch (Throwable ex) {
                return fixUUID(UUIDObject.get("id").toString());
            }
        } catch (Throwable e) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Fix the trimmed UUID
     *
     * @param id the trimmed UUID
     * @return the full UUID
     * @throws IllegalArgumentException if the UUID is invalid ( not an UUID )
     */
    private UUID fixUUID(String id) throws IllegalArgumentException {
        if (id == null) throw new IllegalArgumentException();
        if (!id.contains("-")) {
            StringBuilder builder = new StringBuilder(id.trim());
            try {
                builder.insert(20, "-");
                builder.insert(16, "-");
                builder.insert(12, "-");
                builder.insert(8, "-");
            } catch (StringIndexOutOfBoundsException e) {
                throw new IllegalArgumentException();
            }

            return UUID.fromString(builder.toString());
        }

        return UUID.fromString(id);
    }
}
