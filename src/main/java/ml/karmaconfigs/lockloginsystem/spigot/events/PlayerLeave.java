package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;

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

public final class PlayerLeave implements Listener, LockLoginSpigot, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (config.takeBack()) {
            if (user.isLogged() && !user.isTempLog()) {
                LastLocation lastLoc = new LastLocation(player);
                lastLoc.saveLocation();
            }
        }

        user.setFly(player.getAllowFlight());

        if (!config.isBungeeCord()) {
            TempModule temp_module = new TempModule();
            ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
            try {
                if (!ModuleLoader.manager.isLoaded(temp_module)) {
                    spigot_module_loader.inject();
                }
            } catch (Throwable ignored) {
            }

            InetSocketAddress ip = player.getAddress();
            if (ip != null) {
                IpData data = new IpData(temp_module, ip.getAddress());
                data.delIP();
            }

            user.setLogged(false);
        }

        user.removeBlindEffect();

        if (player.hasMetadata("LockLoginUser")) {
            player.removeMetadata("LockLoginUser", plugin);
        }
    }
}