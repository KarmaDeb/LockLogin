package ml.karmaconfigs.lockloginsystem.bukkit.events;

import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserJoinEvent;
import ml.karmaconfigs.lockloginmodules.bukkit.ModuleUtil;
import ml.karmaconfigs.lockloginmodules.bukkit.PluginModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

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
public class PreJoinRelated implements Listener, LockLoginSpigot, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onLogin(PlayerLoginEvent e) {
        if (!config.isBungeeCord()) {
            if (e.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    Player player = e.getPlayer();
                    User user = new User(player);

                    TempModule temp_module = new TempModule();
                    PluginModuleLoader spigot_module_loader = new PluginModuleLoader(temp_module);
                    try {
                        if (!ModuleUtil.isLoaded(temp_module)) {
                            spigot_module_loader.inject();
                        }
                    } catch (Throwable ignored) {}

                    try {
                        IPStorager storager = new IPStorager(temp_module, e.getAddress());

                        if (config.maxRegister() > 0) {
                            if (storager.canJoin(player.getUniqueId(), e.getAddress(), config.maxRegister())) {
                                storager.save(player.getUniqueId(), player.getName());

                                if (storager.hasAltAccounts(player.getUniqueId(), e.getAddress())) {
                                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                                        if (online.hasPermission("locklogin.playerinfo") && !online.getUniqueId().equals(player.getUniqueId()))
                                            user.send(messages.prefix() + messages.altFound(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName(), storager.getAltsAmount(player.getUniqueId(), e.getAddress())));
                                    }
                                }
                            } else {
                                user.kick("&eLockLogin\n\n" + messages.maxRegisters());
                            }
                        } else {
                            storager.save(player.getUniqueId(), player.getName());
                        }
                    } catch (Throwable ignored) {
                    }

                    if (config.accountsPerIp() != 0) {
                        IpData data = new IpData(temp_module, e.getAddress());
                        data.fetch(Platform.BUKKIT);

                        if (data.getConnections() > config.accountsPerIp()) {
                            user.kick("&eLockLogin\n\n" + messages.maxIp());
                        } else {
                            if (!player.isBanned()) {
                                data.addIP();
                            }
                        }
                    }

                    UserJoinEvent event = new UserJoinEvent(player);
                    LockLoginListener.callEvent(event);
                });
            }
        }
    }
}
