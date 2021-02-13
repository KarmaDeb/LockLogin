package ml.karmaconfigs.lockloginmodules.bungee;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.shared.NoJarException;
import ml.karmaconfigs.lockloginmodules.shared.NoPluginException;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ModuleLoader {

    private final static HashSet<Module> modules = new HashSet<>();
    private final Module plugin_module;

    /**
     * Initialize the LockLogin's module loader
     *
     * @param module the module to inject into
     *               LockLogin
     */
    public ModuleLoader(final Module module) {
        plugin_module = module;
    }

    /**
     * Inject the module into LockLogin
     *
     * @throws NoJarException    if LockLogin founds that the module plugin
     *                           is not a jar file
     * @throws NoPluginException if LockLogin founds that the module plugin
     *                           is not a valid plugin file
     * @throws IOException       if java couldn't load the file as JarFile
     */
    public final void inject() throws NoJarException, NoPluginException, IOException {
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
                } else {
                    throw new NoPluginException(jar_file, "plugin.yml or bungee.yml not found");
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
        Module module = manager.getByInfo(plugin_module);

        if (module != null) {
            modules.remove(module);

            Console.send(LockLoginBungee.plugin, "Module {0} [ {1} ] by {1} un-injected from LockLogin", Level.GRAVE,
                    module.name(),
                    module.version(),
                    module.author());
        }
    }

    public interface manager {

        /**
         * Check if the specified module is loaded
         *
         * @param _module the module
         * @return if the module is loaded
         */
        static boolean isLoaded(Module _module) {
            String check_name = _module.name();
            String check_author = _module.author();
            String check_version = _module.author();
            String check_description = _module.author();

            for (Module module : modules) {
                String name = module.name();
                String author = module.author();
                String version = module.author();
                String description = module.author();

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
        static HashSet<Module> getByPlugin(final Plugin plugin) {
            HashSet<Module> list = new HashSet<>();

            for (Module module : modules) {
                if (module.owner().getDescription().getName().equals(plugin.getDescription().getName()))
                    list.add(module);
            }

            return list;
        }

        static Module getByInfo(final Module _module) {
            String check_name = _module.name();
            String check_author = _module.author();
            String check_version = _module.author();
            String check_description = _module.author();

            for (Module module : modules) {
                String name = module.name();
                String author = module.author();
                String version = module.author();
                String description = module.author();

                if (name.equals(check_name) && author.equals(check_author) && version.equals(check_version) && description.equals(check_description)) {
                    return module;
                }
            }

            return null;
        }
    }
}
