package ml.karmaconfigs.LockLogin.Spigot.Events;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

public final class BungeeJoinEventHandler implements Listener, LockLoginSpigot, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!player.hasMetadata("LockLoginUser")) {
            player.setMetadata("LockLoginUser", new FixedMetadataValue(plugin, player.getUniqueId()));
        }

        if (config.HandleSpawn()) {
            Spawn spawn = new Spawn();

            user.Teleport(spawn.getSpawn());
        }

        player.setAllowFlight(user.hasFly());
    }
}
