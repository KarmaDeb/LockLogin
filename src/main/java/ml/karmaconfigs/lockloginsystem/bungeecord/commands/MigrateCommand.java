package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.MySQLData;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class MigrateCommand extends Command implements BungeeFiles {

    private static CommandSender migrating_owner = null;
    private static int passed_migration = 0;
    private static int max_migrations = 0;

    public MigrateCommand() {
        super("migrate");
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            User user = new User(player);

            if (player.hasPermission("locklogin.migrate")) {
                if (migrating_owner == null) {
                    if (config.isMySQL()) {
                        Utils sql = new Utils();

                        List<String> ids = sql.getUUIDs();
                        max_migrations = ids.size();

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (passed_migration == max_migrations) {
                                    timer.cancel();
                                    migrating_owner.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.Prefix() + messages.Migrated())));
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

                                    player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                                            StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")
                                    ));
                                }
                            }
                        }, 0, TimeUnit.SECONDS.toMillis(0));
                        user.Message(messages.Prefix() + messages.MigratingAll());

                        for (String id : ids) {
                            Utils sqlUUID = new Utils(id);

                            AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                            migrate.start();

                            passed_migration = passed_migration + 1;
                        }
                    } else {
                        try {
                            user.Message(messages.Prefix() + "&bTrying to establish a connection with MySQL");
                            MySQLData data = new MySQLData();

                            Bucket bucket = new Bucket(
                                    data.getHost(),
                                    data.getDatabase(),
                                    data.getTable(),
                                    data.getUser(),
                                    data.getPassword(),
                                    data.getPort(),
                                    data.useSSL(),
                                    data.ignoreCertificates());

                            bucket.setOptions(data.getMaxConnections(), data.getMinConnections(), data.getTimeOut(), data.getLifeTime());

                            user.Message(messages.Prefix() + messages.MigratingAll());
                            bucket.prepareTables(data.ignoredColumns());

                            Utils sql = new Utils();
                            sql.checkTables();

                            List<String> ids = sql.getUUIDs();
                            max_migrations = ids.size();

                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (passed_migration == max_migrations) {
                                        timer.cancel();
                                        migrating_owner.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.Prefix() + messages.Migrated())));
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

                                        player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                                                StringUtils.toColor("&8Migrating progress:" + colour + " " + fPart + "%")
                                        ));
                                    }
                                }
                            }, 0, TimeUnit.SECONDS.toMillis(0));
                            user.Message(messages.Prefix() + messages.MigratingAll());

                            for (String id : ids) {
                                Utils sqlUUID = new Utils(id);

                                AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                                migrate.start();

                                passed_migration = passed_migration + 1;
                            }
                        } catch (Throwable ex) {
                            user.Message(messages.Prefix() + messages.MigrationConnectionError());
                        }
                    }
                } else {
                    user.Message(messages.Prefix() + "&cMigration already in progress by: &7" + migrating_owner.getName());
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError("locklogin.migrate"));
            }
        } else {
            if (migrating_owner == null) {
                if (config.isMySQL()) {
                    Utils sql = new Utils();

                    List<String> ids = sql.getUUIDs();
                    max_migrations = ids.size();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (passed_migration == max_migrations) {
                                timer.cancel();
                                migrating_owner.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.Prefix() + messages.Migrated())));
                                migrating_owner = null;
                            }
                        }
                    }, 0, TimeUnit.SECONDS.toMillis(0));
                    Console.send(messages.Prefix() + messages.MigratingAll());

                    for (String id : ids) {
                        Utils sqlUUID = new Utils(id);

                        AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                        migrate.start();

                        passed_migration = passed_migration + 1;
                    }
                } else {
                    try {
                        Console.send(messages.Prefix() + "&bTrying to establish a connection with MySQL");
                        MySQLData data = new MySQLData();

                        Bucket bucket = new Bucket(
                                data.getHost(),
                                data.getDatabase(),
                                data.getTable(),
                                data.getUser(),
                                data.getPassword(),
                                data.getPort(),
                                data.useSSL(),
                                data.ignoreCertificates());

                        bucket.setOptions(data.getMaxConnections(), data.getMinConnections(), data.getTimeOut(), data.getLifeTime());

                        Console.send(messages.Prefix() + messages.MigratingAll());
                        bucket.prepareTables(data.ignoredColumns());

                        Utils sql = new Utils();
                        sql.checkTables();

                        List<String> ids = sql.getUUIDs();
                        max_migrations = ids.size();

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (passed_migration == max_migrations) {
                                    timer.cancel();
                                    migrating_owner.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(messages.Prefix() + messages.Migrated())));
                                    migrating_owner = null;
                                }
                            }
                        }, 0, TimeUnit.SECONDS.toMillis(0));
                        Console.send(messages.Prefix() + messages.MigratingAll());

                        for (String id : ids) {
                            Utils sqlUUID = new Utils(id);

                            AccountMigrate migrate = new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);
                            migrate.start();

                            passed_migration = passed_migration + 1;
                        }
                    } catch (Throwable ex) {
                        Console.send(messages.Prefix() + messages.MigrationConnectionError());
                    }
                }
            } else {
                Console.send(messages.Prefix() + "&cMigration already in progress by: &7" + migrating_owner.getName());
            }
        }
    }
}
