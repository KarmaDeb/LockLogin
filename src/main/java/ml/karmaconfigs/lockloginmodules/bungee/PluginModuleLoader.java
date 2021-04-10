package ml.karmaconfigs.lockloginmodules.bungee;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.shared.NoJarException;
import ml.karmaconfigs.lockloginmodules.shared.NoModuleException;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
public final class PluginModuleLoader {

    private final static HashSet<PluginModule> modules = new HashSet<>();
    private final PluginModule plugin_module;

    /**
     * Initialize the LockLogin's module loader
     *
     * @param module the module to inject into
     *               LockLogin
     */
    public PluginModuleLoader(final PluginModule module) {
        plugin_module = module;
    }

    /**
     * Inject the module into LockLogin
     *
     * @throws NoJarException    if LockLogin founds that the module plugin
     *                           is not a jar file
     * @throws NoModuleException if LockLogin founds that the module plugin
     *                           is not a valid plugin file
     * @throws IOException       if java couldn't load the file as JarFile
     */
    public final void inject() throws NoJarException, NoModuleException, IOException {
        Class<? extends Plugin> main = plugin_module.owner().getClass();

        File jar_file = new File(main
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath().replaceAll("%20", " "));

        if (!manager.isLoaded(plugin_module)) {
            if (jar_file.getName().endsWith(".jar")) {
                JarFile jar = new JarFile(jar_file);
                JarEntry plugin = jar.getJarEntry("bungee.yml");

                if (plugin == null)
                    plugin = jar.getJarEntry("plugin.yml");

                if (plugin != null) {
                    Console.send(LockLoginBungee.plugin, "Loading module {0} [ {1} ] by {2}", Level.INFO,
                            plugin_module.name(),
                            plugin_module.version(),
                            plugin_module.author());

                    modules.add(plugin_module);

                    LockLoginBungee.logger.scheduleLog(Level.INFO, "Module " + plugin_module.name() + " by " + plugin_module.author() + " hooked into LockLogin");
                }
            } else {
                throw new NoJarException(jar_file);
            }
        } else {
            Console.send(LockLoginBungee.plugin, "Module {0} [ {1} ] by {2} tried to inject to LockLogin but he's already injected", Level.GRAVE,
                    plugin_module.name(),
                    plugin_module.version(),
                    plugin_module.author());

            LockLoginBungee.logger.scheduleLog(Level.INFO, "Module " + plugin_module.name() + " by " + plugin_module.author() + " tried to hook into LockLogin but he's already in");
        }
    }

    /**
     * Uninject the LockLogin module
     */
    public final void uninject() {
        PluginModule module = manager.getByInfo(plugin_module);

        if (module != null) {
            modules.remove(module);

            Console.send(LockLoginBungee.plugin, "Module {0} [ {1} ] by {1} un-injected from LockLogin", Level.GRAVE,
                    module.name(),
                    module.version(),
                    module.author());
        }
    }

    /**
     * Module loader manager
     * utilities
     */
    public interface manager {

        /**
         * Check if the specified module is loaded
         *
         * @param _module the module
         * @return if the module is loaded
         */
        static boolean isLoaded(PluginModule _module) {
            String check_name = _module.name();
            String check_author = _module.author();
            String check_version = _module.version();
            String check_description = _module.description();

            for (PluginModule module : modules) {
                String name = module.name();
                String author = module.author();
                String version = module.version();
                String description = module.description();

                if (name.equals(check_name) && author.equals(check_author) && version.equals(check_version) && description.equals(check_description)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Get the modules of a plugin
         *
         * @param plugin the plugin that
         *               owns the modules
         * @return a list of modules owned
         * by a plugin
         */
        static HashSet<PluginModule> getByPlugin(final Plugin plugin) {
            HashSet<PluginModule> list = new HashSet<>();

            for (PluginModule module : modules) {
                if (module.owner().getDescription().getName().equals(plugin.getDescription().getName()))
                    list.add(module);
            }

            return list;
        }

        /**
         * Get a module by a module instance
         *
         * @param _module the new module instance
         * @return the main module instance
         */
        static PluginModule getByInfo(final PluginModule _module) {
            String check_name = _module.name();
            String check_author = _module.author();
            String check_version = _module.version();
            String check_description = _module.description();

            for (PluginModule module : modules) {
                String name = module.name();
                String author = module.author();
                String version = module.version();
                String description = module.description();

                if (name.equals(check_name) && author.equals(check_author) && version.equals(check_version) && description.equals(check_description)) {
                    return module;
                }
            }

            return null;
        }
    }
}
