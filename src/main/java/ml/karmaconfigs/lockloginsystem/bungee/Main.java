package ml.karmaconfigs.lockloginsystem.bungee;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.common.JarInjector;
import ml.karmaconfigs.api.common.KarmaPlugin;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginmodules.bungee.AdvancedModuleLoader;
import ml.karmaconfigs.lockloginmodules.shared.NoJarException;
import ml.karmaconfigs.lockloginmodules.shared.NoModuleException;
import ml.karmaconfigs.lockloginsystem.bungee.utils.PluginManagerBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.PlayerFile;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.dependencies.Dependency;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;

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
        File modulesFolder = new File(getDataFolder(), "modules");
        if (!modulesFolder.exists()) {
            try {
                Files.createDirectories(modulesFolder.toPath());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        new InterfaceUtils().setMain(this);

        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.BUNGEE);

        LockLoginBungee.logger.setMaxSize(FileInfo.logFileSize(LockLoginBungee.getJar()));

        boolean injected = true;
        Set<Dependency> success = new HashSet<>();
        Set<Dependency> error = new HashSet<>(Arrays.asList(Dependency.values()));
        Set<AdvancedModuleLoader> loaders = new LinkedHashSet<>();
        try {
            Console.setOkPrefix(this, "&8[ &eLockLogin &8] &aOK &f>> &7");
            Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
            Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
            Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

            File[] modules = modulesFolder.listFiles();
            if (modules != null) {
                Injector injector = new Injector(LockLoginBungee.getJar());
                
                for (File module : modules) {
                    AdvancedModuleLoader loader = new AdvancedModuleLoader(module);
                    injector.inject(loader.getMainClass());
                    loaders.add(loader);

                    JarInjector subInjector = new JarInjector(module);
                    subInjector.inject(this);
                }
            }

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

        for (AdvancedModuleLoader loader : loaders) {
            Module module = loader.getAsModule();
            if (module != null) {
                try {
                    loader.inject();
                } catch (NoJarException | NoModuleException ex) {
                    ex.printStackTrace();
                    Console.send(this, "Failed to inject module " + module.name() + " ( " + ex.fillInStackTrace() + " )", Level.GRAVE);
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Console.send(this, "Failed to inject module " + module.name() + " ( " + ex.fillInStackTrace() + " )", Level.WARNING);
                    ex.printStackTrace();
                }
            }
        }
        
        PluginManagerBungee manager = new PluginManagerBungee();
        manager.enable();

        Logger logger = new Logger(Main.this);
        logger.scheduleLog(Level.GRAVE, "LockLogin initialized");

        PlayerFile.migrateV1();
        PlayerFile.migrateV2();

        IPStorager.migrateFromV2();
        IPStorager.migrateFromV3();
        IPStorager.migrateFromV4();
    }

    @Override
    public final void onDisable() {
        new PluginManagerBungee().disable();
        LockLoginBungee.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }
}

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.github.io/page/license"> here </a>
 */
class Injector {

    private final File jarFile;

    /**
     * Initialize the injector class with
     * the specified file
     *
     * @param file the file
     */
    public Injector(@NotNull final File file) {
        jarFile = file;
    }

    /**
     * Inject the jar into the plugin
     */
    public final void inject(final Class<?> module) {
        URLClassLoader cl = null;
        try {
            // Get the ClassLoader class
            cl = (URLClassLoader) module.getClassLoader();
            Class<?> clazz = URLClassLoader.class;

            // Get the protected addURL method from the parent URLClassLoader class
            Method method = clazz.getDeclaredMethod("addURL", URL.class);

            // Run projected addURL method to add JAR to classpath
            method.setAccessible(true);
            method.invoke(cl, jarFile.toURI().toURL());
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (cl != null)
                try {
                    cl.close();
                } catch (Throwable ignored) {}
        }
    }
}