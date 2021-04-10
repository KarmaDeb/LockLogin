package ml.karmaconfigs.lockloginmodules.bungee;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.shared.NoJarException;
import ml.karmaconfigs.lockloginmodules.shared.NoModuleException;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.Main;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
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
public final class AdvancedModuleLoader {

    private final static Map<File, AdvancedModule> modules = new HashMap<>();
    private final File jar_module;

    /**
     * Initialize the LockLogin's module loader
     *
     * @param moduleJar the module to inject into
     *               LockLogin
     */
    public AdvancedModuleLoader(final File moduleJar) {
        jar_module = moduleJar;
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
        if (jar_module.getName().endsWith(".jar")) {
            JarFile jar = new JarFile(jar_module);
            JarEntry plugin = jar.getJarEntry("module.yml");

            if (plugin != null) {
                Yaml yaml = new Yaml();
                Map<String, Object> values = yaml.load(jar.getInputStream(plugin));

                String class_name = values.getOrDefault("loader_bungee", null).toString();
                if (class_name != null) {
                    try {
                        URLClassLoader child = new URLClassLoader(
                                new URL[]{new URL("file:///" + jar_module.getAbsolutePath())}, Main.class.getClassLoader());
                        Class<?> loader = Class.forName(class_name, true, child);
                        Class<? extends AdvancedModule> moduleLoader = loader.asSubclass(AdvancedModule.class);
                        child.close();

                        AdvancedModule module = moduleLoader.getDeclaredConstructor().newInstance();

                        if (!manager.isLoaded(module)) {
                            modules.put(jar_module, module);
                            
                            module.onEnable();

                            Console.send(LockLoginBungee.plugin, "Loading module {0} [ {1} ] by {2}", Level.INFO,
                                    module.name(),
                                    module.version(),
                                    module.author());

                            LockLoginBungee.logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " hooked into LockLogin");
                        } else {
                            Console.send(LockLoginBungee.plugin, "Module {0} [ {1} ] by {2} tried to inject to LockLogin but he's already injected", Level.GRAVE,
                                    module.name(),
                                    module.version(),
                                    module.author());

                            LockLoginBungee.logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " tried to hook into LockLogin but he's already in");
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        throw new NoModuleException(jar_module, "Invalid module file, couldn't find main class: " + class_name);
                    }
                } else {
                    throw new NoModuleException(jar_module, "Invalid module.yml, loader not found (loader: org.example.Main)");
                }
            } else {
                throw new NoModuleException(jar_module, "plugin.yml or module.yml not found");
            }
        } else {
            throw new NoJarException(jar_module);
        }
    }

    /**
     * Get the jar file as module
     *
     * @return the jar file advanced module
     */
    @Nullable
    public final AdvancedModule getAsModule() {
        if (!modules.containsKey(jar_module) || modules.getOrDefault(jar_module, null) == null) {
            if (jar_module.getName().endsWith(".jar")) {
                try {
                    JarFile jar = new JarFile(jar_module);
                    JarEntry plugin = jar.getJarEntry("module.yml");

                    if (plugin != null) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> values = yaml.load(jar.getInputStream(plugin));

                        String class_name = values.getOrDefault("loader_bungee", null).toString();
                        if (class_name != null) {
                            URLClassLoader child = new URLClassLoader(
                                    new URL[]{new URL("file:///" + jar_module.getAbsolutePath())}, Main.class.getClassLoader());
                            Class<?> loader = Class.forName(class_name, true, child);
                            Class<? extends AdvancedModule> moduleLoader = loader.asSubclass(AdvancedModule.class);
                            child.close();

                            return moduleLoader.getDeclaredConstructor().newInstance();
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            return modules.get(jar_module);
        }

        return null;
    }

    /**
     * Get the jar file as module
     *
     * @return the jar file advanced module
     */
    @Nullable
    public final Class<? extends AdvancedModule> getMainClass() {
        if (!modules.containsKey(jar_module) || modules.getOrDefault(jar_module, null) == null) {
            if (jar_module.getName().endsWith(".jar")) {
                try {
                    JarFile jar = new JarFile(jar_module);
                    JarEntry plugin = jar.getJarEntry("module.yml");

                    if (plugin != null) {
                        Yaml yaml = new Yaml();
                        Map<String, Object> values = yaml.load(jar.getInputStream(plugin));

                        String class_name = values.getOrDefault("loader_bungee", null).toString();
                        if (class_name != null) {
                            URLClassLoader child = new URLClassLoader(
                                    new URL[]{new URL("file:///" + jar_module.getAbsolutePath())}, Main.class.getClassLoader());
                            Class<?> loader = Class.forName(class_name, true, child);
                            child.close();

                            return loader.asSubclass(AdvancedModule.class);
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            return modules.get(jar_module).getClass();
        }

        return null;
    }

    /**
     * Uninject the LockLogin module
     */
    public final void uninject() {
        if (modules.containsKey(jar_module) && modules.getOrDefault(jar_module, null) != null) {
            AdvancedModule module = modules.remove(jar_module);
            assert module != null;

            Console.send(LockLoginBungee.plugin, "Module {0} [ {1} ] by {1} un-injected from LockLogin", Level.GRAVE,
                    module.name(),
                    module.version(),
                    module.author());

            module.onDisable();
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
        static boolean isLoaded(AdvancedModule _module) {
            String check_name = _module.name();
            String check_author = _module.author();
            String check_version = _module.version();
            String check_description = _module.description();

            for (File moduleJar : modules.keySet()) {
                AdvancedModule module = modules.getOrDefault(moduleJar, null);
                if (module != null) {
                    String name = module.name();
                    String author = module.author();
                    String version = module.version();
                    String description = module.description();

                    if (name.equals(check_name) && author.equals(check_author) && version.equals(check_version) && description.equals(check_description)) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Get the modules of a plugin
         *
         * @param jarFile the jar file that
         *                owns the module
         * @return a list of modules owned
         * by a jar file
         */
        @Nullable
        static AdvancedModule getByJar(final File jarFile) {
            return modules.getOrDefault(jarFile, null);
        }

        /**
         * Get a new map with the loaded modules and their
         * jar
         *
         * @return the loaded modules
         */
        static Map<File, AdvancedModule> getModules() {
            return new HashMap<>(modules);
        }
    }
}
