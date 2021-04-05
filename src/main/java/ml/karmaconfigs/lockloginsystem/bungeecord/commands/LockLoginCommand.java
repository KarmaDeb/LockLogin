package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.utils.FileUtilities;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.MySQLData;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.pluginmanager.LockLoginBungeeManager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.InsertInfo;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.filemigration.FileInserter;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Insert.BucketInserter;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.SQLite.SQLiteReader;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
public class LockLoginCommand extends Command implements LockLoginBungee, BungeeFiles {

    private static CommandSender migrating_owner = null;
    private static int passed_migration = 0;
    private static int max_migrations = 0;

    /**
     * Initialize LockLogin command
     */
    public LockLoginCommand() {
        super("locklogin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 0) {
                user.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload&e, &emodules");
            } else {
                switch (args[0].toLowerCase()) {
                    case "migrate":
                        if (player.hasPermission("locklogin.migrate")) {
                            if (migrating_owner == null) {
                                if (args.length == 1)
                                    user.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                                else {
                                    String sub_arg = args[1];
                                    switch (args.length) {
                                        case 2:
                                            switch (sub_arg.toLowerCase()) {
                                                case "mysql":
                                                    migrateMySQL(player);
                                                    break;
                                                case "authme":
                                                    user.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                    break;
                                                case "loginsecurity":
                                                    user.send(messages.prefix() + "&cCorrect usage: /locklogin migrate loginsecurity <table name>");
                                                    break;
                                                default:
                                                    user.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                                                    break;
                                            }
                                            break;
                                        case 3:
                                        case 4:
                                        case 5:
                                            switch (sub_arg.toLowerCase()) {
                                                case "mysql":
                                                    migrateMySQL(player);
                                                    break;
                                                case "authme":
                                                    user.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                    break;
                                                case "loginsecurity":
                                                    String table = args[2];

                                                    user.send(messages.prefix() + "&aMigrating from LoginSecurity");

                                                    if (migrateLoginSecurity(sender, table)) {
                                                        Timer timer = new Timer();
                                                        timer.schedule(new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                if (passed_migration == max_migrations) {
                                                                    user.send(StringUtils.toColor(messages.prefix() + messages.migrated()));
                                                                    migrating_owner = null;
                                                                    cancel();
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

                                                                    player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")));
                                                                }
                                                            }
                                                        }, 0, 1000);
                                                    } else {
                                                        user.send(messages.prefix() + "&cSome error occurred while migrating");
                                                        migrating_owner = null;
                                                    }
                                                    break;
                                                default:
                                                    user.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                                                    break;
                                            }
                                            break;
                                        case 6:
                                        default:
                                            switch (sub_arg.toLowerCase()) {
                                                case "mysql":
                                                    migrateMySQL(player);
                                                    break;
                                                case "authme":
                                                    String database_file = args[2];
                                                    String table_name = args[3];
                                                    String real_name = args[4];
                                                    String passwords = args[5];

                                                    user.send(messages.prefix() + "&aMigrating from authme");

                                                    if (migrateAuthMe(sender, database_file, table_name, real_name, passwords)) {
                                                        Timer timer = new Timer();
                                                        timer.schedule(new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                if (passed_migration == max_migrations) {
                                                                    cancel();
                                                                    user.send((messages.prefix() + messages.migrated()));
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

                                                                    player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")));
                                                                }
                                                            }
                                                        }, 0, 1000);
                                                    } else {
                                                        user.send(messages.prefix() + "&cSome error occurred while migrating");
                                                        migrating_owner = null;
                                                    }
                                                    break;
                                                case "loginsecurity":
                                                    String table = args[2];

                                                    user.send(messages.prefix() + "&aMigrating from LoginSecurity");

                                                    if (migrateLoginSecurity(sender, table)) {
                                                        Timer timer = new Timer();
                                                        timer.schedule(new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                if (passed_migration == max_migrations) {
                                                                    user.send(StringUtils.toColor(messages.prefix() + messages.migrated()));
                                                                    migrating_owner = null;
                                                                    cancel();
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

                                                                    player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")));
                                                                }
                                                            }
                                                        }, 0, 1000);
                                                    } else {
                                                        user.send(messages.prefix() + "&cSome error occurred while migrating");
                                                        migrating_owner = null;
                                                    }
                                                    break;
                                                default:
                                                    user.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                                                    break;
                                            }
                                            break;
                                    }
                                }
                            } else {
                                user.send(messages.prefix() + "&cMigration already in progress by " + migrating_owner.getName());
                            }
                        } else {
                            user.send(messages.prefix() + messages.permission("locklogin.migrate"));
                        }
                        break;
                    case "applyupdates":
                        if (player.hasPermission("locklogin.update")) {
                            LockLoginBungeeManager b_manager = new LockLoginBungeeManager();
                            b_manager.applyUpdate(user);
                        } else {
                            user.send(messages.prefix() + messages.permission("locklogin.update"));
                        }
                        break;
                    case "reload":
                        if (player.hasPermission("locklogin.update")) {
                            LockLoginBungeeManager r_manager = new LockLoginBungeeManager();
                            r_manager.reload(user);
                        } else {
                            user.send(messages.prefix() + messages.permission("locklogin.update"));
                        }
                        break;
                    case "modules":
                        if (player.hasPermission("locklogin.modules")) {
                            dataSender.openModulesGUI(player);
                        } else {
                            user.send(messages.prefix() + messages.permission("locklogin.modules"));
                        }
                        break;
                    default:
                        user.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload&e, &emodules");
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
                            if (args.length == 1)
                                Console.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                            else {
                                String sub_arg = args[1];
                                switch (args.length) {
                                    case 2:
                                        switch (sub_arg.toLowerCase()) {
                                            case "mysql":
                                                migrateMySQL(sender);
                                                break;
                                            case "authme":
                                                Console.send(messages.prefix() + "&cCorrect usage: /locklogin migrate AuthMe <database file name> <table name> <real name column> <password column>");
                                                break;
                                            case "loginsecurity":
                                                Console.send(messages.prefix() + "&cCorrect usage: /locklogin migrate loginsecurity <table name>");
                                                break;
                                            default:
                                                Console.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                                                break;
                                        }
                                        break;
                                    case 3:
                                    case 4:
                                    case 5:
                                        switch (sub_arg.toLowerCase()) {
                                            case "mysql":
                                                migrateMySQL(sender);
                                                break;
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
                                                Console.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
                                                break;
                                        }
                                        break;
                                    case 6:
                                    default:
                                        switch (sub_arg.toLowerCase()) {
                                            case "mysql":
                                                migrateMySQL(sender);
                                                break;
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
                                                Console.send(messages.prefix() + "&cPlease specify the migration: &7/locklogin migrate <MySQL, AuthMe, LoginSecurity>");
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
                        LockLoginBungeeManager b_manager = new LockLoginBungeeManager();
                        b_manager.applyUpdate(null);
                        break;
                    case "reload":
                        LockLoginBungeeManager r_manager = new LockLoginBungeeManager();
                        r_manager.reload(null);
                        break;
                    default:
                        Console.send(messages.prefix() + "&cAvailable sub commands: &7migrate&e, &7applyUpdates&e, &7reload");
                        break;
                }
            }
        }
    }

    /**
     * Do a mysql migration
     *
     * @param player the executor
     */
    private void migrateMySQL(ProxiedPlayer player) {
        migrating_owner = player;

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            User user = new User(player);

            if (config.isMySQL()) {
                Utils sql = new Utils();

                user.send(messages.prefix() + messages.migratingAll());

                List<String> uuids = sql.getUUIDs();
                max_migrations = uuids.size();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (passed_migration == max_migrations) {
                            cancel();
                            user.send(messages.prefix() + messages.migrated());
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

                            player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")));
                        }
                    }
                }, 0, 1000);

                for (String id : uuids) {
                    Utils sqlUUID = new Utils(id, sql.fetchName(id));

                    AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                    migrate.start();

                    passed_migration++;
                }
            } else {
                user.send(messages.prefix() + "&bTrying to establish a connection with MySQL");
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

                    user.send(messages.prefix() + messages.migratingAll());
                    bucket.prepareTables(SQLData.ignoredColumns());

                    Utils sql = new Utils();

                    List<String> uuids = sql.getUUIDs();
                    max_migrations = uuids.size();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (passed_migration == max_migrations) {
                                cancel();
                                user.send(messages.prefix() + messages.migrated());
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

                                player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")));
                            }
                        }
                    }, 0, 1000);

                    for (String id : uuids) {
                        Utils sqlUUID = new Utils(id, sql.fetchName(id));

                        AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                        migrate.start();

                        passed_migration++;
                    }
                } catch (Throwable ex) {
                    migrating_owner = null;
                    user.send(messages.prefix() + messages.migrationError());
                }
            }
        });
    }

    /**
     * Do a mysql migration
     */
    private void migrateMySQL(CommandSender sender) {
        migrating_owner = sender;

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            if (config.isMySQL()) {
                Utils sql = new Utils();

                Console.send(messages.prefix() + messages.migratingAll());

                List<String> uuids = sql.getUUIDs();
                max_migrations = uuids.size();
                for (String id : uuids) {
                    Utils sqlUUID = new Utils(id, sql.fetchName(id));

                    AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                    migrate.start();

                    passed_migration++;
                }
                migrating_owner = null;

                Console.send(messages.prefix() + messages.migrated());
            } else {
                try {
                    Console.send(messages.prefix() + "&bTrying to establish a connection with MySQL");
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

                    Console.send(messages.prefix() + messages.migratingAll());
                    bucket.prepareTables(SQLData.ignoredColumns());

                    Utils sql = new Utils();

                    List<String> uuids = sql.getUUIDs();
                    max_migrations = uuids.size();

                    for (String id : uuids) {
                        Utils sqlUUID = new Utils(id, sql.fetchName(id));

                        AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                        migrate.start();

                        passed_migration = passed_migration + 1;
                    }
                    migrating_owner = null;

                    Console.send(messages.prefix() + messages.migrated());
                } catch (Throwable ex) {
                    migrating_owner = null;
                    Console.send(messages.prefix() + messages.migrationError());
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
            if (!authMe.exists())
                authMe = new File(plugin.getDataFolder().getParentFile(), "AuthMe");

            if (authMe.exists()) {
                sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&aAuthMe folder found")));

                File data = new File(authMe, database + ".db");

                if (data.exists()) {
                    sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&aAuthMe database file &7( &f" + data.getName() + " &7)&a found")));

                    SQLiteReader reader = new SQLiteReader(data, table, realnameColumn, passwordColumn);
                    if (reader.tryConnection()) {
                        sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&aConnection to SQLite successfully")));

                        if (config.isMySQL()) {
                            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
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
                        } else {
                            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
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
                        }

                        return true;
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&cAuthMe database file doesn't exist ( &7" + data.getName() + " &c)")));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&cCouldn't find AuthMe folder")));
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

            if (!loginsecurity.exists())
                loginsecurity = new File(plugin.getDataFolder().getParentFile(), "LoginSecurity");

            if (loginsecurity.exists()) {
                File data = new File(loginsecurity, "LoginSecurity.db");

                if (data.exists()) {
                    SQLiteReader reader = new SQLiteReader(data, table, "last_name", "password");
                    if (config.isMySQL()) {
                        if (reader.tryConnection()) {
                            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
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
                        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
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
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&cLoginSecurity database file doesn't exist ( &7" + data.getName() + " &c)")));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.prefix() + "&cCouldn't find LoginSecurity folder")));
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
