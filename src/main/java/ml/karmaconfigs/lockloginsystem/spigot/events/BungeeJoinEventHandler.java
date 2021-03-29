package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
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
public final class BungeeJoinEventHandler implements Listener, LockLoginSpigot, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = new User(player);

        if (!player.hasMetadata("LockLoginUser")) {
            player.setMetadata("LockLoginUser", new FixedMetadataValue(plugin, player.getUniqueId()));
        }

        if (config.enableSpawn()) {
            Spawn spawn = new Spawn();

            user.teleport(spawn.getSpawn());
        }

        player.setAllowFlight(user.hasFly());
    }
}
