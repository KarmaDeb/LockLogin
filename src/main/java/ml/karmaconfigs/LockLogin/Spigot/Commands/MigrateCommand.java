package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.MySQL.AccountMigrate;
import ml.karmaconfigs.LockLogin.MySQL.Bucket;
import ml.karmaconfigs.LockLogin.MySQL.Migrate;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.MySQLData;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.sql.Connection;

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

public final class MigrateCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private final Permission migratePermission = new Permission("locklogin.migrate", PermissionDefault.FALSE);

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (player.hasPermission(migratePermission)) {
                    if (config.isMySQL()) {
                        Utils sql = new Utils();

                        user.Message(messages.Prefix() + messages.MigratingAll());

                        for (String id : sql.getUUIDs()) {
                            Utils sqlUUID = new Utils(id);

                            new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

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

                                new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

                                user.Message(messages.Prefix() + messages.MigratingYaml(id));
                            }
                            user.Message(messages.Prefix() + messages.Migrated());
                        } else {
                            user.Message(messages.Prefix() + messages.MigrationConnectionError());
                        }
                    }
                } else {
                    user.Message(messages.Prefix() + messages.PermissionError(migratePermission.getName()));
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

                    new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

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

                        new AccountMigrate(sqlUUID, Migrate.YAML, Platform.SPIGOT);

                        out.Message(messages.Prefix() + messages.MigratingYaml(id));
                    }
                    out.Message(messages.Prefix() + messages.Migrated());
                } else {
                    out.Message(messages.Prefix() + messages.MigrationConnectionError());
                }
            }
        }
        return false;
    }
}
