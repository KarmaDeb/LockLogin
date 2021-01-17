package ml.karmaconfigs.lockloginsystem.spigot;

import ml.karmaconfigs.api.KarmaPlugin;
import ml.karmaconfigs.api.shared.JarInjector;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.dependencies.Dependency;
import ml.karmaconfigs.lockloginsystem.spigot.utils.PluginManagerSpigot;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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

@KarmaPlugin(plugin_name = "LockLogin", plugin_version = "1.0.3.9", plugin_update_url = "https://karmaconfigs.github.io/updates/LockLogin/latest.txt")
public final class Main extends JavaPlugin {

    @Override
    public final void onEnable() {
        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.SPIGOT);

        try {
            Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
            Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
            Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

            File libs_folder = new File(getDataFolder(), "libraries");
            File hikari = new File(libs_folder, "HikariCP.jar");
            File codecs = new File(libs_folder, "CommonsCodec.jar");
            File goauth = new File(libs_folder, "GoogleAuth.jar");
            File slf4j = new File(libs_folder, "slf4j.jar");

            JarInjector hikari_injector = new JarInjector(hikari);
            JarInjector codecs_injector = new JarInjector(codecs);
            JarInjector goauth_injector = new JarInjector(goauth);
            JarInjector slf4j_injector = new JarInjector(slf4j);

            hikari_injector.download(Dependency.hikari);
            codecs_injector.download(Dependency.commons);
            goauth_injector.download(Dependency.google);
            slf4j_injector.download(Dependency.slf4j);

            if (hikari_injector.isDownloaded() && codecs_injector.isDownloaded() && goauth_injector.isDownloaded() && slf4j_injector.isDownloaded()) {
                hikari_injector.inject(this);
                codecs_injector.inject(this);
                goauth_injector.inject(this);
                slf4j_injector.inject(this);

                new PluginManagerSpigot().enable();
                LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin initialized");
            } else {
                LockLoginSpigot.logger.scheduleLog(Level.GRAVE, "An error occurred while trying to load LockLogin");
                Console.send(this, "An error occurred while trying to enable LockLogin, check /LockLogin/logs for more info", Level.GRAVE);
            }
        } catch (Throwable e) {
            LockLoginSpigot.logger.scheduleLog(Level.GRAVE, e);
            LockLoginSpigot.logger.scheduleLog(Level.INFO, "An internal error occurred while trying to load LockLogin");

            Console.send(this, "An error occurred while trying to enable LockLogin, check /LockLogin/logs for more info", Level.GRAVE);
        }
    }

    @Override
    public final void onDisable() {
        new PluginManagerSpigot().disable();
        LockLoginSpigot.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }
}
