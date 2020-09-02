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
