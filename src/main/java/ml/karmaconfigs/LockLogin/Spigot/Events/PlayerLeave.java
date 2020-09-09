package ml.karmaconfigs.LockLogin.Spigot.Events;

import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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

    /**
     * This event will be executed when the player leaves
     * the server
     *
     * @param e the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (config.TakeBack()) {
            if (user.isLogged() && !user.isTempLog()) {
                LastLocation lastLoc = new LastLocation(player);
                lastLoc.saveLocation();
            }
        }

        user.setFly(player.getAllowFlight());

        if (!config.isBungeeCord()) {
            IpData data = new IpData(player.getAddress().getAddress());

            data.delIP();

            if (!user.isLogged() || !user.isRegistered()) {
                if (config.RegisterBlind() || config.LoginBlind()) {
                    user.removeBlindEffect();
                }
            }

            user.setLogStatus(false);
        } else {
            user.removeBlindEffect();
        }

        if (player.hasMetadata("LockLoginUser")) {
            player.removeMetadata("LockLoginUser", plugin);
        }
    }
}
