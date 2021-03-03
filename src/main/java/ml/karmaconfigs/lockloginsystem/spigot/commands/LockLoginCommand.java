package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.FileUtilities;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.reflections.BarMessage;
import ml.karmaconfigs.lockloginsystem.shared.InsertInfo;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.filemigration.FileInserter;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Insert.BucketInserter;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.SQLite.SQLiteReader;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.LockLoginSpigotManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.MySQLData;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.ModuleListInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
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
                user.send(messages.Prefix() + "&cSpecify an action &7( &e/locklogin migrate &7|| &e/locklogin applyUpdates &7)");
            } else {
                if (args[0] != null) {
                    if (args[0].equals("migrate")) {
                        if (player.hasPermission(migratePermission)) {
                            if (migrating_owner == null) {
                                if (!config.isBungeeCord()) {
                                    if (args.length == 1) {
                                        user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                    } else {
                                        if (args.length == 2) {
                                            String method = args[1];
                                            switch (method.toLowerCase()) {
                                                case "mysql":
                                                    migrateMySQL(player);
                                                    break;
                                                case "loginsecurity":
                                                case "ls":
                                                    user.send(messages.Prefix() + "&cPlease specify table name");
                                                    break;
                                                case "authme":
                                                    user.send(messages.Prefix() + "&cPlease, specify database name and table name (must exist in plugins/authme folder)");
                                                    break;
                                                default:
                                                    user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                    break;
                                            }
                                        } else {
                                            if (args.length == 3) {
                                                String method = args[1];
                                                switch (method.toLowerCase()) {
                                                    case "mysql":
                                                        user.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                        break;
                                                    case "loginsecurity":
                                                    case "ls":
                                                        user.send(messages.Prefix() + "&aMigrating from LoginSecurity");
                                                        BarMessage message = new BarMessage(player, "&eMigrating progress:&c Starting");
                                                        message.send(true);
                                                        if (migrateLoginSecurity(sender, args[2])) {
                                                            new BukkitRunnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (passed_migration == max_migrations) {
                                                                        cancel();
                                                                        migrating_owner.sendMessage(StringUtils.toColor(messages.Prefix() + messages.Migrated()));
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
                                                            user.send(messages.Prefix() + "&cSome error occurred while migrating");
                                                        }
                                                        break;
                                                    case "authme":
                                                        user.send(messages.Prefix() + "&cPlease, specify table name");
                                                        break;
                                                    default:
                                                        user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                        break;
                                                }
                                            } else {
                                                if (args.length == 4) {
                                                    String method = args[1];
                                                    switch (method.toLowerCase()) {
                                                        case "mysql":
                                                            user.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                            break;
                                                        case "loginsecurity":
                                                        case "ls":
                                                            user.send(messages.Prefix() + "&cToo many args, please use /locklogin migrate " + method + " <table>");
                                                            break;
                                                        case "authme":
                                                            user.send(messages.Prefix() + "&cPlease specify the 'realname' column");
                                                            break;
                                                        default:
                                                            user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                            break;
                                                    }
                                                } else {
                                                    if (args.length == 5) {
                                                        String method = args[1];
                                                        switch (method.toLowerCase()) {
                                                            case "mysql":
                                                                user.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                                break;
                                                            case "loginsecurity":
                                                            case "ls":
                                                                user.send(messages.Prefix() + "&cToo many args, please use /locklogin migrate " + method + " <table>");
                                                                break;
                                                            case "authme":
                                                                user.send(messages.Prefix() + "&cPlease specify the 'password' column");
                                                                break;
                                                            default:
                                                                user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                                break;
                                                        }
                                                    } else {
                                                        if (args.length == 6) {
                                                            String method = args[1];
                                                            switch (method.toLowerCase()) {
                                                                case "mysql":
                                                                    user.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                                    break;
                                                                case "loginsecurity":
                                                                case "ls":
                                                                    user.send(messages.Prefix() + "&cToo many args, please use /locklogin migrate " + method + " <table>");
                                                                    break;
                                                                case "authme":
                                                                    user.send(messages.Prefix() + "&aMigrating from authme");
                                                                    BarMessage message = new BarMessage(player, "&eMigrating progress:&c Starting");
                                                                    message.send(true);
                                                                    if (migrateAuthMe(sender, args[2], args[3], args[4], args[5])) {
                                                                        new BukkitRunnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                if (passed_migration == max_migrations) {
                                                                                    cancel();
                                                                                    migrating_owner.sendMessage(StringUtils.toColor(messages.Prefix() + messages.Migrated()));
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
                                                                        user.send(messages.Prefix() + "&cSome error occurred while migrating");
                                                                    }
                                                                    break;
                                                                default:
                                                                    user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                                    break;
                                                            }
                                                        } else {
                                                            user.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    user.send(messages.Prefix() + "&cNot allowed in BungeeCord mode!");
                                }
                            } else {
                                user.send(messages.Prefix() + "&cMigration already in progress by: " + migrating_owner.getName());
                            }
                        }
                    } else {
                        if (args[0].equals("applyUpdates")) {
                            if (player.hasPermission(applyUpdatePermission)) {
                                LockLoginSpigotManager s_manager = new LockLoginSpigotManager();
                                s_manager.applyUpdate(user);
                            } else {
                                user.send(messages.Prefix() + messages.PermissionError(applyUpdatePermission.getName()));
                            }
                        } else {
                            if (args[0].equals("modules")) {
                                if (player.hasPermission(modulePermission)) {
                                    ModuleListInventory inv = new ModuleListInventory(player);
                                    inv.openPage(0);
                                } else {
                                    user.send(messages.Prefix() + messages.PermissionError(modulePermission.getName()));
                                }
                            } else {
                                user.send(messages.Prefix() + "&cUnknown sub-command, /locklogin [migrate|applyUpdates|modules]");
                            }
                        }
                    }
                }
            }
        } else {
            if (args.length == 0) {
                Console.send(messages.Prefix() + "&cSpecify an action &7( &e/locklogin migrate &7|| &e/locklogin applyUpdates &7)");
            } else {
                if (args[0].equals("migrate")) {
                    if (migrating_owner == null) {
                        if (!config.isBungeeCord()) {
                            if (args.length == 1) {
                                Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                            } else {
                                if (args.length == 2) {
                                    String method = args[1];
                                    switch (method.toLowerCase()) {
                                        case "mysql":
                                            migrateMySQL(sender);
                                            break;
                                        case "loginsecurty":
                                        case "ls":
                                            Console.send(messages.Prefix() + "&cPlease specify table name");
                                            break;
                                        case "authme":
                                            Console.send(messages.Prefix() + "&cPlease, specify database name, table name, realname and password column name (must exist in plugins/authme folder)");
                                            break;
                                        default:
                                            Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                            break;
                                    }
                                } else {
                                    if (args.length == 3) {
                                        String method = args[1];
                                        switch (method.toLowerCase()) {
                                            case "mysql":
                                                Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                break;
                                            case "loginsecurity":
                                            case "ls":
                                                if (migrateLoginSecurity(sender, args[2])) {
                                                    Console.send(messages.Prefix() + messages.Migrated());
                                                } else {
                                                    Console.send(messages.Prefix() + "&cSome error occurred while migrating");
                                                }
                                            case "authme":
                                                Console.send(messages.Prefix() + "&cPlease, specify table name");
                                                break;
                                            default:
                                                Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                break;
                                        }
                                    } else {
                                        if (args.length == 4) {
                                            String method = args[1];
                                            switch (method.toLowerCase()) {
                                                case "mysql":
                                                    Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                    break;
                                                case "loginsecurity":
                                                case "ls":
                                                    Console.send(messages.Prefix() + "&cToo many args, please use /locklogin migrate " + method + " <table>");
                                                    break;
                                                case "authme":
                                                    Console.send(messages.Prefix() + "&cPlease specify the 'realname' column");
                                                    break;
                                                default:
                                                    Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                    break;
                                            }
                                        } else {
                                            if (args.length == 5) {
                                                String method = args[1];
                                                switch (method.toLowerCase()) {
                                                    case "mysql":
                                                        Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                        break;
                                                    case "loginsecurity":
                                                    case "ls":
                                                        Console.send(messages.Prefix() + "&cToo many args, please use /locklogin migrate " + method + " <table>");
                                                        break;
                                                    case "authme":
                                                        Console.send(messages.Prefix() + "&cPlease specify the 'password' column");
                                                        break;
                                                    default:
                                                        Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                        break;
                                                }
                                            } else {
                                                if (args.length == 6) {
                                                    String method = args[1];
                                                    switch (method.toLowerCase()) {
                                                        case "mysql":
                                                            Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                            break;
                                                        case "loginsecurity":
                                                        case "ls":
                                                            Console.send(messages.Prefix() + "&cToo many args, please use /locklogin migrate " + method + " <table>");
                                                            break;
                                                        case "authme":
                                                            Console.send(messages.Prefix() + "&aMigrating from authme sqlite");
                                                            if (migrateAuthMe(sender, args[2], args[3], args[4], args[5])) {
                                                                Console.send(messages.Prefix() + messages.Migrated());
                                                            } else {
                                                                Console.send(messages.Prefix() + "&cSome error occurred while migrating");
                                                            }
                                                            break;
                                                        default:
                                                            Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                            break;
                                                    }
                                                } else {
                                                    Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, authme>");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Console.send(messages.Prefix() + "&cNot allowed in BungeeCord mode!");
                        }
                    } else {
                        Console.send(messages.Prefix() + "&cMigration already in progress by: &7" + migrating_owner.getName());
                    }
                } else {
                    if (args[0].equals("applyUpdates")) {
                        LockLoginSpigotManager s_manager = new LockLoginSpigotManager();
                        s_manager.applyUpdate(null);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Do a mysql migration
     *
     * @param player the executor
     */
    private void migrateMySQL(Player player) {
        migrating_owner = player;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            User user = new User(player);

            if (config.isMySQL()) {
                Utils sql = new Utils();

                user.send(messages.Prefix() + messages.MigratingAll());

                List<String> uuids = sql.getUUIDs();
                max_migrations = uuids.size();

                BarMessage message = new BarMessage(player, "&eMigrating progress:&c Starting");
                message.send(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (passed_migration == max_migrations) {
                            cancel();
                            migrating_owner.sendMessage(StringUtils.toColor(messages.Prefix() + messages.Migrated()));
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

                for (String id : uuids) {
                    Utils sqlUUID = new Utils(id);

                    AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);
                    migrate.start();

                    passed_migration++;
                }
            } else {
                user.send(messages.Prefix() + "&bTrying to establish a connection with MySQL");
                try {
                    MySQLData SQLData = new MySQLData();

                    Bucket bucket = new Bucket(
                            SQLData.getHost(),
                            SQLData.getDatabase(),
                            SQLData.getTable(),
                            SQLData.getUser(),
                            SQLData.getPassword(),
                            SQLData.getPort(),
                            SQLData.useSSL(),
                            SQLData.ignoreCertificates());

                    bucket.setOptions(SQLData.getMaxConnections(), SQLData.getMinConnections(), SQLData.getTimeOut(), SQLData.getLifeTime());

                    user.send(messages.Prefix() + messages.MigratingAll());
                    bucket.prepareTables(SQLData.ignoredColumns());

                    Utils sql = new Utils();

                    List<String> uuids = sql.getUUIDs();
                    max_migrations = uuids.size();

                    BarMessage message = new BarMessage(player, "&eMigrating progress:&c Starting");
                    message.send(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (passed_migration == max_migrations) {
                                cancel();
                                migrating_owner.sendMessage(StringUtils.toColor(messages.Prefix() + messages.Migrated()));
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

                    for (String id : uuids) {
                        Utils sqlUUID = new Utils(id);

                        AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);
                        migrate.start();

                        passed_migration++;
                    }
                } catch (Throwable ex) {
                    migrating_owner = null;
                    user.send(messages.Prefix() + messages.MigrationConnectionError());
                }
            }
        });
    }

    /**
     * Do a mysql migration
     */
    private void migrateMySQL(CommandSender sender) {
        migrating_owner = sender;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (config.isMySQL()) {
                Utils sql = new Utils();

                Console.send(messages.Prefix() + messages.MigratingAll());

                sql.checkTables();

                List<String> uuids = sql.getUUIDs();
                max_migrations = uuids.size();
                for (String id : uuids) {
                    Utils sqlUUID = new Utils(id);

                    AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);
                    migrate.start();

                    passed_migration++;
                }
                migrating_owner = null;

                Console.send(messages.Prefix() + messages.Migrated());
            } else {
                try {
                    Console.send(messages.Prefix() + "&bTrying to establish a connection with MySQL");
                    MySQLData SQLData = new MySQLData();

                    Bucket bucket = new Bucket(
                            SQLData.getHost(),
                            SQLData.getDatabase(),
                            SQLData.getTable(),
                            SQLData.getUser(),
                            SQLData.getPassword(),
                            SQLData.getPort(),
                            SQLData.useSSL(),
                            SQLData.ignoreCertificates());

                    bucket.setOptions(SQLData.getMaxConnections(), SQLData.getMinConnections(), SQLData.getTimeOut(), SQLData.getLifeTime());

                    Console.send(messages.Prefix() + messages.MigratingAll());
                    bucket.prepareTables(SQLData.ignoredColumns());

                    Utils sql = new Utils();
                    sql.checkTables();

                    List<String> uuids = sql.getUUIDs();
                    max_migrations = uuids.size();

                    for (String id : uuids) {
                        Utils sqlUUID = new Utils(id);

                        AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);
                        migrate.start();

                        passed_migration = passed_migration + 1;
                    }
                    migrating_owner = null;

                    Console.send(messages.Prefix() + messages.Migrated());
                } catch (Throwable ex) {
                    migrating_owner = null;
                    Console.send(messages.Prefix() + messages.MigrationConnectionError());
                }
            }
        });
    }

    /**
     * Do an authme sqlite migration
     *
     * @param database the database name
     * @param table    the database table where the info is
     */
    private boolean migrateAuthMe(CommandSender sender, String database, String table, String realnameColumn, String passwordColumn) {
        migrating_owner = sender;

        if (config.isYaml()) {
            File authMe = new File(FileUtilities.getPluginsFolder(), "AuthMe");
            if (authMe.exists()) {
                File data = new File(authMe, database + ".db");

                if (data.exists()) {
                    SQLiteReader reader = new SQLiteReader(data, table, realnameColumn, passwordColumn);
                    if (config.isMySQL()) {
                        if (reader.tryConnection()) {
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                                HashSet<String> players = reader.getPlayers();
                                max_migrations = players.size();
                                for (String name : players) {
                                    String password = reader.getPassword(name);

                                    if (password != null && !password.isEmpty()) {
                                        try {
                                            InsertInfo insert = new InsertInfo(name);
                                            insert.setPassword(password);
                                            insert.setFly(false);
                                            insert.setGAuthStatus(false);
                                            insert.setGauthToken("");
                                            insert.setPin("");

                                            BucketInserter inserter = new BucketInserter(insert);
                                            inserter.insert();
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    passed_migration = passed_migration + 1;
                                }

                                migrating_owner = null;
                            });
                            return true;
                        }
                    } else {
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                            HashSet<String> players = reader.getPlayers();
                            max_migrations = players.size();
                            for (String name : players) {
                                String password = reader.getPassword(name);

                                if (password != null && !password.isEmpty()) {
                                    try {
                                        InsertInfo insert = new InsertInfo(name);
                                        insert.setPassword(password);
                                        insert.setFly(false);
                                        insert.setGAuthStatus(false);
                                        insert.setGauthToken("");
                                        insert.setPin("");

                                        FileInserter inserter = new FileInserter(insert);
                                        inserter.insert();
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }

                                passed_migration = passed_migration + 1;
                            }

                            migrating_owner = null;
                        });
                        return true;
                    }
                }
            }
        } else {
            max_migrations = 1;
            passed_migration = 1;
            Utils utils = new Utils();

            return utils.migrateAuthMe(realnameColumn, passwordColumn);
        }

        return false;
    }

    /**
     * Do an login security sqlite migration
     */
    private boolean migrateLoginSecurity(CommandSender sender, final String table) {
        migrating_owner = sender;

        if (config.isYaml()) {
            File loginsecurity = new File(FileUtilities.getPluginsFolder(), "LoginSecurity");
            if (loginsecurity.exists()) {
                File data = new File(loginsecurity, "LoginSecurity.db");

                if (data.exists()) {
                    SQLiteReader reader = new SQLiteReader(data, table, "last_name", "password");
                    if (config.isMySQL()) {
                        if (reader.tryConnection()) {
                            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                                HashSet<String> players = reader.getPlayers();
                                max_migrations = players.size();
                                for (String name : players) {
                                    String password = reader.getPassword(name);

                                    if (password != null && !password.isEmpty()) {
                                        try {
                                            InsertInfo insert = new InsertInfo(name);
                                            insert.setPassword(password);
                                            insert.setFly(false);
                                            insert.setGAuthStatus(false);
                                            insert.setGauthToken("");
                                            insert.setPin("");

                                            BucketInserter inserter = new BucketInserter(insert);
                                            inserter.insert();
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    passed_migration = passed_migration + 1;
                                }

                                migrating_owner = null;
                            });
                            return true;
                        }
                    } else {
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                            HashSet<String> players = reader.getPlayers();
                            max_migrations = players.size();
                            for (String name : players) {
                                String password = reader.getPassword(name);

                                if (password != null && !password.isEmpty()) {
                                    try {
                                        InsertInfo insert = new InsertInfo(name);
                                        insert.setPassword(password);
                                        insert.setFly(false);
                                        insert.setGAuthStatus(false);
                                        insert.setGauthToken("");
                                        insert.setPin("");

                                        FileInserter inserter = new FileInserter(insert);
                                        inserter.insert();
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }

                                passed_migration = passed_migration + 1;
                            }

                            migrating_owner = null;
                        });
                        return true;
                    }
                }
            }
        } else {
            max_migrations = 1;
            passed_migration = 1;
            Utils utils = new Utils();

            return utils.migrateLoginSecurity();
        }

        return false;
    }
}
