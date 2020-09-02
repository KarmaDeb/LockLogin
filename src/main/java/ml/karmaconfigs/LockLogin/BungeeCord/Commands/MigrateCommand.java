package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.DataFiles.MySQLData;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.MySQL.AccountMigrate;
import ml.karmaconfigs.LockLogin.MySQL.Bucket;
import ml.karmaconfigs.LockLogin.MySQL.Migrate;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import ml.karmaconfigs.LockLogin.Platform;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;

public final class MigrateCommand extends Command implements LockLoginBungee, BungeeFiles {

    public MigrateCommand() {
        super("migrate", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final String migratePermission = "locklogin.migrate";

        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (player.hasPermission(migratePermission)) {
                    if (config.isMySQL()) {
                        Utils sql = new Utils();

                        user.Message(messages.Prefix() + messages.MigratingAll());

                        for (String id : sql.getUUIDs()) {
                            Utils sqlUUID = new Utils(id);

                            new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);

                            user.Message(messages.Prefix() + messages.MigratingYaml(id));
                        }
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
                        } catch (Exception | Error ignore) {}

                        if (connection != null) {
                            user.Message(messages.Prefix() + messages.MigratingAll());
                            bucket.prepareTables();

                            Utils sql = new Utils();

                            for (String id : sql.getUUIDs()) {
                                Utils sqlUUID = new Utils(id);

                                new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);

                                user.Message(messages.Prefix() + messages.MigratingYaml(id));
                            }
                            user.Message(messages.Prefix() + messages.Migrated());
                        } else {
                            user.Message(messages.Prefix() + messages.MigrationConnectionError());
                        }
                    }
                } else {
                    user.Message(messages.Prefix() + messages.PermissionError(migratePermission));
                }
            } else {
                user.Message(messages.Prefix() + messages.MigrationUsage());
            }
        } else {
            if (config.isMySQL()) {
                Utils sql = new Utils();

                out.Message(messages.Prefix() + messages.MigratingAll());

                for (String id : sql.getUUIDs()) {
                    Utils sqlUUID = new Utils(id);

                    new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);

                    out.Message(messages.Prefix() + messages.MigratingYaml(id));
                }
                out.Message(messages.Prefix() + messages.Migrated());
            } else {
                out.Message(messages.Prefix() + "&bTrying to establish a connection with MySQL");
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
                } catch (Exception | Error ignore) {}

                if (connection != null) {
                    out.Message(messages.Prefix() + messages.MigratingAll());
                    bucket.prepareTables();

                    Utils sql = new Utils();

                    for (String id : sql.getUUIDs()) {
                        Utils sqlUUID = new Utils(id);

                        new AccountMigrate(sqlUUID, Migrate.YAML, Platform.BUNGEE);

                        out.Message(messages.Prefix() + messages.MigratingYaml(id));
                    }
                    out.Message(messages.Prefix() + messages.Migrated());
                } else {
                    out.Message(messages.Prefix() + messages.MigrationConnectionError());
                }
            }
        }
    }
}
