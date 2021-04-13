package ml.karmaconfigs.lockloginsystem.bungee.events;

import ml.karmaconfigs.lockloginmodules.bungee.PluginModuleLoader;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserQuitEvent;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

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
public final class PlayerLeave implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();

        LockLoginBungee.plugin.getProxy().getScheduler().schedule(LockLoginBungee.plugin, () -> {
            TempPluginModule temp_module = new TempPluginModule();
            try {
                PluginModuleLoader loader = new PluginModuleLoader(temp_module);
                if (!PluginModuleLoader.manager.isLoaded(temp_module)) {
                    loader.inject();
                }
            } catch (Throwable ignored) {
            }

            User user = new User(player);

            IpData data = new IpData(temp_module, user.getIp());
            data.delIP();

            user.setLogged(false);

            UserQuitEvent event = new UserQuitEvent(player, this);
            LockLoginListener.callEvent(event);
        }, 2, TimeUnit.SECONDS);
    }
}