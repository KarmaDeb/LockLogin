package ml.karmaconfigs.LockLogin.BungeeCord.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.IpData;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class PlayerLeave implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onDisconnect(PlayerDisconnectEvent e) {
        IpData data = new IpData(e.getPlayer().getAddress().getAddress());
        data.delIP();

        User user = new User(e.getPlayer());
        user.removeServerInfo();
    }
}
