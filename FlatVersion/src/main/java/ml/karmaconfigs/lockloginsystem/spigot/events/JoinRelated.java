package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.IPStorager;
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
            if (e.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
                Player player = e.getPlayer();

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    TempModule temp_module = new TempModule();
                    ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
                    try {
                        if (!ModuleLoader.manager.isLoaded(temp_module)) {
                            spigot_module_loader.inject();
                        }
                    } catch (Throwable ignored) {
                    }

                    if (config.MaxRegisters() > 0) {
                        try {
                            IPStorager storager = new IPStorager(temp_module, e.getAddress());
                            if (storager.canJoin(player.getUniqueId(), config.MaxRegisters())) {
                                storager.save(player.getUniqueId());
                            } else {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.MaxRegisters()));
                                    try {
                                        User user = new User(player);
                                        user.Kick("&eLockLogin\n\n" + messages.MaxRegisters());
                                    } catch (Throwable ignored) {
                                    }
                                });
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    if (config.AccountsPerIp() != 0) {
                        IpData data = new IpData(temp_module, e.getAddress());

                        data.fetch(Platform.SPIGOT);

                        if (data.getConnections() > config.AccountsPerIp()) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, StringUtils.toColor("&eLockLogin\n\n" + messages.MaxIp()));
                                try {
                                    User user = new User(player);
                                    user.Kick("&eLockLogin\n\n" + messages.MaxIp());
                                } catch (Throwable ignored) {
                                }
                            });
                        } else {
                            if (!plugin.getServer().getOfflinePlayer(e.getPlayer().getUniqueId()).isBanned()) {
                                data.addIP();
                            }
                        }
                    }

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
                });
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
            if (player.isDead())
                player.spigot().respawn();

            Spawn spawn = new Spawn();

            user.Teleport(spawn.getSpawn());
        }
    }
}
