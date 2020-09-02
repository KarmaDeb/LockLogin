package ml.karmaconfigs.LockLogin.BungeeCord.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.IpData;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class PlayerKick implements Listener {

    /**
     * This event will be executed when a player
     * gets kicked so the IP manager will be
     * able to remove his IP
     *
     * @param e the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onKick(ServerKickEvent e) {
        IpData data = new IpData(e.getPlayer().getAddress().getAddress());

        if (!e.isCancelled()) {
            data.delIP();

            new User(e.getPlayer()).removeServerInfo();
        }
    }
}
