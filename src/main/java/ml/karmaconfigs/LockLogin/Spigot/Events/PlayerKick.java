package ml.karmaconfigs.LockLogin.Spigot.Events;

import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public final class PlayerKick implements Listener, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onKick(PlayerKickEvent e) {
        if (!config.isBungeeCord()) {
            Player player = e.getPlayer();

            IpData data = new IpData(player.getAddress().getAddress());

            data.delIP();
        }
    }
}
