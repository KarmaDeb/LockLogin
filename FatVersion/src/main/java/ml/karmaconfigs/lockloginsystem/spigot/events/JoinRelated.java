package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.metadata.FixedMetadataValue;

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

public final class JoinRelated implements Listener, LockLoginSpigot, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void playerLogEvent(PlayerLoginEvent e) {
        if (!config.isBungeeCord()) {
            Player player = e.getPlayer();

            if (config.isYaml()) {
                User user = new User(player);

                user.setupFile();
            } else {
                String UUID = player.getUniqueId().toString().replace("-", "");

                FileManager manager = new FileManager(UUID + ".yml", "playerdata");
                manager.setInternal("auto-generated/userTemplate.yml");

                Utils sql = new Utils(player.getUniqueId());

                sql.createUser();

                if (manager.getManaged().exists()) {
                    if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                        if (manager.isSet("Password")) {
                            if (!manager.isEmpty("Password")) {
                                new AccountMigrate(sql, Migrate.MySQL, Platform.SPIGOT);
                                Console.send(plugin, messages.Migrating(player.getUniqueId().toString()), Level.INFO);
                            }
                        }
                    }
                }

                if (sql.getName() == null || sql.getName().isEmpty())
                    sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!player.hasMetadata("LockLoginUser")) {
            player.setMetadata("LockLoginUser", new FixedMetadataValue(plugin, player.getUniqueId()));
        }

        if (config.ClearChat()) {
            for (int i = 0; i < 150; i++) {
                player.sendMessage(" ");
            }
        }

        user.setLogStatus(false);
        user.checkStatus();

        if (config.HandleSpawn()) {
            Spawn spawn = new Spawn();

            user.Teleport(spawn.getSpawn());
        }
    }
}
