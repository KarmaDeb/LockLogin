package ml.karmaconfigs.lockloginsystem.spigot;

import ml.karmaconfigs.api.KarmaPlugin;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.spigot.utils.PluginManagerSpigot;
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

@KarmaPlugin(plugin_name = "LockLogin", plugin_version = "1.0.3.2", plugin_update_url = "https://karmaconfigs.github.io/updates/LockLogin/latest.txt")
public final class Main extends JavaPlugin {

    @Override
    public final void onEnable() {
        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.SPIGOT);

        Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
        Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
        Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

        new PluginManagerSpigot().enable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        new PluginManagerSpigot().disable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }
}
