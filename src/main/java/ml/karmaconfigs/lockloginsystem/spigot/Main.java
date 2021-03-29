package ml.karmaconfigs.lockloginsystem.spigot;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.JarInjector;
import ml.karmaconfigs.api.common.KarmaPlugin;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.dependencies.Dependency;
import ml.karmaconfigs.lockloginsystem.spigot.utils.PluginManagerSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.IPStorager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
@KarmaPlugin(plugin_name = "LockLogin", plugin_version = "1.0.7.4", plugin_update_url = "https://karmaconfigs.github.io/updates/LockLogin/latest.txt")
public final class Main extends JavaPlugin {

    @Override
    public final void onEnable() {
        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.BUKKIT);

        try {
            Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
            Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
            Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

            for (Dependency dependency : Dependency.values()) {
                File target = new File(getDataFolder() + File.separator + "libraries", dependency.fileName());

                JarInjector injector = new JarInjector(target);
                if (!injector.isDownloaded())
                    injector.download(dependency.downloadURL());

                if (injector.inject(this))
                    LockLoginSpigot.logger.scheduleLog(Level.INFO, "Injected dependency " + dependency.name());
            }
        } catch (Throwable e) {
            LockLoginSpigot.logger.scheduleLog(Level.GRAVE, e);
            LockLoginSpigot.logger.scheduleLog(Level.INFO, "An internal error occurred while trying to load LockLogin");

            Console.send(this, "An error occurred while trying to enable LockLogin, check /LockLogin/logs for more info", Level.GRAVE);
        }

        IPStorager.migrateFromV2();
        IPStorager.migrateFromV3();
        IPStorager.migrateFromV4();

        PluginManagerSpigot ms = new PluginManagerSpigot();
        ms.enable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        PluginManagerSpigot ms = new PluginManagerSpigot();
        ms.disable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }
}
