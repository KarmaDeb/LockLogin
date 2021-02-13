package ml.karmaconfigs.lockloginsystem.bungeecord.events;

import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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

public final class PlayerKick implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onKick(ServerKickEvent e) {
        TempModule temp_module = new TempModule();
        try {
            ModuleLoader loader = new ModuleLoader(temp_module);
            if (!ModuleLoader.manager.isLoaded(temp_module)) {
                loader.inject();
            }
        } catch (Throwable ignored) {
        }

        IpData data = new IpData(temp_module, User.external.getIp(e.getPlayer().getSocketAddress()));

        if (!e.isCancelled()) {
            data.delIP();
        }
    }
}
