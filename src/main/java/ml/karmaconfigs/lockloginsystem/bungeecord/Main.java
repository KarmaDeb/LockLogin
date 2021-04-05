package ml.karmaconfigs.lockloginsystem.bungeecord;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.common.JarInjector;
import ml.karmaconfigs.api.common.KarmaPlugin;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.PluginManagerBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.dependencies.Dependency;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
@KarmaPlugin(plugin_update_url = "https://karmaconfigs.github.io/updates/LockLogin/latest.txt")
public final class Main extends Plugin {

    @Override
    public final void onEnable() {
        new InterfaceUtils().setMain(this);

        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.BUNGEE);

        LockLoginBungee.logger.setMaxSize(20);

        boolean injected = true;
        Set<Dependency> success = new HashSet<>();
        Set<Dependency> error = new HashSet<>(Arrays.asList(Dependency.values()));
        try {
            Console.setOkPrefix(this, "&8[ &eLockLogin &8] &aOK &f>> &7");
            Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
            Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
            Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

            for (Dependency dependency : Dependency.values()) {
                File target = new File(getDataFolder() + File.separator + "libraries", dependency.fileName());

                JarInjector injector = new JarInjector(target);
                if (injector.isDownloaded() && injector.inject(this)) {
                    success.add(dependency);
                    error.remove(dependency);
                } else {
                    injected = false;
                }
            }
        } catch (Throwable ex) {
            LockLoginBungee.logger.scheduleLog(Level.GRAVE, ex);
            LockLoginBungee.logger.scheduleLog(Level.INFO, "An internal error occurred while trying to load LockLogin");

            Console.send(this, "An error occurred while trying to enable LockLogin, but the plugin may work anyway, check /LockLogin/logs for more info", Level.GRAVE);
        } finally {
            if (!injected) {
                try {
                    Set<Dependency> copyError = new HashSet<>(error);

                    for (Dependency dependency : copyError) {
                        File target = new File(getDataFolder() + File.separator + "libraries", dependency.fileName());

                        JarInjector injector = new JarInjector(target);
                        injector.download(dependency.downloadURL());

                        if (injector.inject(this)) {
                            success.add(dependency);
                            error.remove(dependency);
                        }
                    }
                } catch (Throwable ex) {
                    LockLoginBungee.logger.scheduleLog(Level.GRAVE, ex);
                    LockLoginBungee.logger.scheduleLog(Level.INFO, "An internal error occurred while trying to load LockLogin");

                    Console.send(this, "An error occurred while trying to enable LockLogin and it may not work, check /LockLogin/logs for more info", Level.GRAVE);
                }
            }
        }

        if (!success.isEmpty())
            LockLoginBungee.logger.scheduleLog(Level.INFO, "Successfully injected dependencies: " + success.toString().replace("[", "").replaceAll("]", ""));
        if (!error.isEmpty())
            LockLoginBungee.logger.scheduleLog(Level.INFO, "Failed dependency injections: " + error.toString().replace("[", "").replace("]", ""));

        IPStorager.migrateFromV2();
        IPStorager.migrateFromV3();
        IPStorager.migrateFromV4();

        PluginManagerBungee manager = new PluginManagerBungee();
        manager.enable();

        Logger logger = new Logger(Main.this);
        logger.scheduleLog(Level.GRAVE, "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        new PluginManagerBungee().disable();
        LockLoginBungee.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }
}
