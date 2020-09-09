package ml.karmaconfigs.LockLogin.Spigot;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.Utils.PluginManagerSpigot;
import org.bukkit.plugin.java.JavaPlugin;

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

public final class Main extends JavaPlugin {

    @Override
    public final void onEnable() {
        new PluginManagerSpigot().enable();
        Logger.log(Platform.SPIGOT, "INFO", "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        new PluginManagerSpigot().disable();
        Logger.log(Platform.SPIGOT, "INFO", "LockLogin stopped");
    }
}
