package ml.karmaconfigs.lockloginsystem.spigot.commands;

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
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                user.Message(messages.Prefix() + "&cSpecify an action &7( &e/locklogin migrate &7|| &e/locklogin applyUpdates &7)");
            } else {
                if (args[0] != null) {
                    if (args[0].equals("migrate")) {
                        if (player.hasPermission(migratePermission)) {
                            if (migrating_owner == null) {
                                if (!config.isBungeeCord()) {
                                    if (args.length == 1) {
                                        user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                    } else {
                                        if (args.length == 2) {
                                            String method = args[1];
                                            switch (method) {
                                                case "MySQL":
                                                    migrateMySQL(player);
                                                    break;
                                                case "AuthMe":
                                                    user.Message(messages.Prefix() + "&cPlease, specify database name and table name (must exist in plugins/AuthMe folder)");
                                                    break;
                                                default:
                                                    user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                    break;
                                            }
                                        } else {
                                            if (args.length == 3) {
                                                String method = args[1];
                                                switch (method) {
                                                    case "MySQL":
                                                        user.Message(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                        break;
                                                    case "AuthMe":
                                                        user.Message(messages.Prefix() + "&cPlease, specify table name");
                                                        break;
                                                    case "UserLogin":
                                                        user.Message(messages.Prefix() + "&cToo many args, please, use /locklogin migrate UserLogin");
                                                        break;
                                                    default:
                                                        user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                        break;
                                                }
                                            } else {
                                                if (args.length == 4) {
                                                    String method = args[1];
                                                    switch (method) {
                                                        case "MySQL":
                                                            user.Message(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                            break;
                                                        case "AuthMe":
                                                            user.Message(messages.Prefix() + "&cPlease specify the 'realname' column");
                                                            break;
                                                        default:
                                                            user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                            break;
                                                    }
                                                } else {
                                                    if (args.length == 5) {
                                                        String method = args[1];
                                                        switch (method) {
                                                            case "MySQL":
                                                                user.Message(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                                break;
                                                            case "AuthMe":
                                                                user.Message(messages.Prefix() + "&cPlease specify the 'password' column");
                                                                break;
                                                            default:
                                                                user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                                break;
                                                        }
                                                    } else {
                                                        if (args.length == 6) {
                                                            String method = args[1];
                                                            switch (method) {
                                                                case "MySQL":
                                                                    user.Message(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                                    break;
                                                                case "AuthMe":
                                                                    user.Message(messages.Prefix() + "&aMigrating from AuthMe sqlite");
                                                                    BarMessage message = new BarMessage(player, "&eMigrating progress:&c Starting");
                                                                    message.send(true);
                                                                    if (migrateAuthMe(sender, args[2], args[3], args[4], args[5])) {
                                                                        new BukkitRunnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                if (migrating_owner == null) {
                                                                                    cancel();
                                                                                    message.setMessage("&8Migration progress:&a Complete");
                                                                                    message.stop();
                                                                                } else {
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
                                                                        user.Message(messages.Prefix() + "&aMigration successfully");
                                                                    } else {
                                                                        user.Message(messages.Prefix() + "&cSome error occurred while migrating");
                                                                    }
                                                                    break;
                                                                default:
                                                                    user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                                    break;
                                                            }
                                                        } else {
                                                            user.Message(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    user.Message(messages.Prefix() + "&cNot allowed in BungeeCord mode!");
                                }
                            } else {
                                user.Message(messages.Prefix() + "&cMigration already in progress by: " + migrating_owner.getName());
                            }
                        }
                    } else {
                        if (args[0].equals("applyUpdates")) {
                            if (player.hasPermission(applyUpdatePermission)) {
                                if (new LockLoginSpigotManager().applyUpdate()) {
                                    user.Message(messages.Prefix() + "&aLockLogin have been reloaded and its updates have been applied");
                                } else {
                                    user.Message(messages.Prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                                    if (ConfigGetter.manager.reload())
                                        user.Message(messages.Prefix() + "&aConfig file reloaded!");
                                    if (MessageGetter.manager.reload())
                                        user.Message(messages.Prefix() + "&aMessages file reloaded!");
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.PermissionError(applyUpdatePermission.getName()));
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
                                Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                            } else {
                                if (args.length == 2) {
                                    String method = args[1];
                                    switch (method) {
                                        case "MySQL":
                                            migrateMySQL(sender);
                                            break;
                                        case "AuthMe":
                                            Console.send(messages.Prefix() + "&cPlease, specify database name, table name, realname and password column name (must exist in plugins/AuthMe folder)");
                                            break;
                                        default:
                                            Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                            break;
                                    }
                                } else {
                                    if (args.length == 3) {
                                        String method = args[1];
                                        switch (method) {
                                            case "MySQL":
                                                Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                break;
                                            case "AuthMe":
                                                Console.send(messages.Prefix() + "&cPlease, specify table name");
                                                break;
                                            default:
                                                Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                break;
                                        }
                                    } else {
                                        if (args.length == 4) {
                                            String method = args[1];
                                            switch (method) {
                                                case "MySQL":
                                                    Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                    break;
                                                case "AuthMe":
                                                    Console.send(messages.Prefix() + "&cPlease specify the 'realname' column");
                                                    break;
                                                default:
                                                    Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                    break;
                                            }
                                        } else {
                                            if (args.length == 5) {
                                                String method = args[1];
                                                switch (method) {
                                                    case "MySQL":
                                                        Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                        break;
                                                    case "AuthMe":
                                                        Console.send(messages.Prefix() + "&cPlease specify the 'password' column");
                                                        break;
                                                    default:
                                                        Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                        break;
                                                }
                                            } else {
                                                if (args.length == 6) {
                                                    String method = args[1];
                                                    switch (method) {
                                                        case "MySQL":
                                                            Console.send(messages.Prefix() + "&cToo many args, please, use /locklogin migrate MySQL");
                                                            break;
                                                        case "AuthMe":
                                                            Console.send(messages.Prefix() + "&aMigrating from AuthMe sqlite");
                                                            if (migrateAuthMe(sender, args[2], args[3], args[4], args[5])) {
                                                                Console.send(messages.Prefix() + "&aMigration successfully");
                                                            } else {
                                                                Console.send(messages.Prefix() + "&cSome error occurred while migrating");
                                                            }
                                                            break;
                                                        default:
                                                            Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
                                                            break;
                                                    }
                                                } else {
                                                    Console.send(messages.Prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe>");
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
                        Console.send(messages.Prefix() + "&cMigration already in progress by: " + migrating_owner.getName());
                    }
                } else {
                    if (args[0].equals("applyUpdates")) {
                        if (new LockLoginSpigotManager().applyUpdate()) {
                            Console.send(messages.Prefix() + "&aLockLogin have been reloaded and its updates have been applied");
                        } else {
                            Console.send(messages.Prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                            if (ConfigGetter.manager.reload())
                                Console.send(messages.Prefix() + "&aConfig file reloaded!");
                            if (MessageGetter.manager.reload())
                                Console.send(messages.Prefix() + "&aMessages file reloaded");
                        }
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

                user.Message(messages.Prefix() + messages.MigratingAll());

                List<String> uuids = sql.getUUIDs();
                max_migrations = uuids.size();
                for (String id : uuids) {
                    Utils sqlUUID = new Utils(id);

                    new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

                    user.Message(messages.Prefix() + messages.MigratingYaml(id));
                    passed_migration = passed_migration + 1;
                }
                migrating_owner = null;

                user.Message(messages.Prefix() + messages.Migrated());
            } else {
                user.Message(messages.Prefix() + "&bTrying to establish a connection with MySQL");
                MySQLData SQLData = new MySQLData();

                Bucket bucket = new Bucket(
                        SQLData.getHost(),
                        SQLData.getDatabase(),
                        SQLData.getTable(),
                        SQLData.getUser(),
                        SQLData.getPassword(),
                        SQLData.getPort(),
                        SQLData.useSSL());

                bucket.setOptions(SQLData.getMaxConnections(), SQLData.getMinConnections(), SQLData.getTimeOut(), SQLData.getLifeTime());

                Connection connection = null;
                try {
                    connection = Bucket.getBucket().getConnection();
                } catch (Throwable ignore) {
                }

                if (connection != null) {
                    user.Message(messages.Prefix() + messages.MigratingAll());
                    bucket.prepareTables();

                    Utils sql = new Utils();

                    List<String> uuids = sql.getUUIDs();
                    max_migrations = uuids.size();
                    for (String id : uuids) {
                        Utils sqlUUID = new Utils(id);

                        new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

                        user.Message(messages.Prefix() + messages.MigratingYaml(id));
                        passed_migration = passed_migration + 1;
                    }
                    migrating_owner = null;

                    user.Message(messages.Prefix() + messages.Migrated());
                } else {
                    migrating_owner = null;
                    user.Message(messages.Prefix() + messages.MigrationConnectionError());
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

                    new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

                    Console.send(messages.Prefix() + messages.MigratingYaml(id));
                    passed_migration = passed_migration + 1;
                }
                migrating_owner = null;

                Console.send(messages.Prefix() + messages.Migrated());
            } else {
                Console.send(messages.Prefix() + "&bTrying to establish a connection with MySQL");
                MySQLData SQLData = new MySQLData();

                Bucket bucket = new Bucket(
                        SQLData.getHost(),
                        SQLData.getDatabase(),
                        SQLData.getTable(),
                        SQLData.getUser(),
                        SQLData.getPassword(),
                        SQLData.getPort(),
                        SQLData.useSSL());

                bucket.setOptions(SQLData.getMaxConnections(), SQLData.getMinConnections(), SQLData.getTimeOut(), SQLData.getLifeTime());

                Connection connection = null;
                try {
                    connection = Bucket.getBucket().getConnection();
                } catch (Exception | Error ignore) {
                }

                if (connection != null) {
                    Console.send(messages.Prefix() + messages.MigratingAll());
                    bucket.prepareTables();

                    Utils sql = new Utils();

                    sql.checkTables();

                    List<String> uuids = sql.getUUIDs();
                    max_migrations = uuids.size();
                    for (String id : uuids) {
                        Utils sqlUUID = new Utils(id);

                        new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

                        Console.send(messages.Prefix() + messages.MigratingYaml(id));
                        passed_migration = passed_migration + 1;
                    }
                    migrating_owner = null;

                    Console.send(messages.Prefix() + messages.Migrated());
                } else {
                    migrating_owner = null;
                    Console.send(messages.Prefix() + messages.MigrationConnectionError());
                }
            }
        });
    }

    /**
     * Do an AuthMe sqlite migration
     *
     * @param database the database name
     * @param table    the database table where the info is
     */
    private boolean migrateAuthMe(CommandSender sender, String database, String table, String realnameColumn, String passwordColumn) {
        migrating_owner = sender;

        File authMe = new File(plugin.getDataFolder().getParentFile().getAbsolutePath(), "AuthMe");
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

        return false;
    }
}