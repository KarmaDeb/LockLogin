package ml.karmaconfigs.lockloginmodules.bukkit;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.timer.AdvancedPluginTimer;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * LockLogin advanced module
 */
public abstract class AdvancedModule extends Module {

    /**
     * On module enable logic
     */
    public abstract void onEnable();

    /**
     * On module disable logic
     */
    public abstract void onDisable();

    /**
     * Reload the module
     */
    public final void reload() {
        if (unload())
            load();
    }

    /**
     * Unload the module
     *
     * @return if the module could be unloaded
     */
    public final boolean unload() {
        try {
            AdvancedModuleLoader module = null;
            for (File jarFile : AdvancedModuleLoader.manager.getModules().keySet()) {
                AdvancedModuleLoader loader = new AdvancedModuleLoader(jarFile);
                Class<? extends AdvancedModule> moduleClass = loader.getMainClass();

                if (moduleClass != null) {
                    if (moduleClass == this.getClass()) {
                        AdvancedModule advModule = loader.getAsModule();

                        if (advModule == null || AdvancedModuleLoader.manager.isLoaded(advModule)) {
                            Console.send(LockLoginSpigot.plugin, "Tried to unload a null or not loaded module!", Level.WARNING);
                        } else {
                            module = loader;
                        }

                        break;
                    }
                }
            }

            if (module != null) {
                module.uninject();
                return true;
            }
        } catch (Throwable ignored) {}

        return false;
    }

    /**
     * Load the module
     *
     * @return if the module could be loaded
     */
    public final boolean load() {
        try {
            AdvancedModuleLoader module = null;
            for (File jarFile : AdvancedModuleLoader.manager.getModules().keySet()) {
                AdvancedModuleLoader loader = new AdvancedModuleLoader(jarFile);
                Class<? extends AdvancedModule> moduleClass = loader.getMainClass();

                if (moduleClass != null) {
                    if (moduleClass == this.getClass()) {
                        AdvancedModule advModule = loader.getAsModule();

                        if (advModule == null || AdvancedModuleLoader.manager.isLoaded(advModule)) {
                            Console.send(LockLoginSpigot.plugin, "Tried to load a null or already loaded module!", Level.WARNING);
                        } else {
                            module = loader;
                        }

                        break;
                    }
                }
            }

            if (module != null) {
                module.inject();
                return true;
            }
        } catch (Throwable ignored) {}

        return false;
    }

    /**
     * Get the module name
     *
     * @return the module name
     */
    @Override
    public final @NotNull String name() {
        String name = "name not set in module.yml";

        try {
            String flName = new java.io.File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()).getName();

            JarFile jar = new JarFile(new File(LockLoginSpigot.plugin.getDataFolder() + File.separator + "modules", flName));
            ZipEntry moduleEntry = jar.getEntry("module.yml");

            if (moduleEntry != null) {
                InputStream module_yml = jar.getInputStream(moduleEntry);
                if (module_yml != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> values = yaml.load(module_yml);

                    name = values.getOrDefault("name", "name not set in module.yml").toString();
                    module_yml.close();
                }
            }

            jar.close();
        } catch (Throwable ignored) {}

        return name;
    }

    /**
     * Get the module version
     *
     * @return the module version
     */
    @Override
    public final @NotNull String version() {
        String version = "version not set in module.yml";

        try {
            String flName = new java.io.File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()).getName();

            JarFile jar = new JarFile(new File(LockLoginSpigot.plugin.getDataFolder() + File.separator + "modules", flName));
            ZipEntry moduleEntry = jar.getEntry("module.yml");

            if (moduleEntry != null) {
                InputStream module_yml = jar.getInputStream(moduleEntry);
                if (module_yml != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> values = yaml.load(module_yml);

                    version = values.getOrDefault("version", "version not set in module.yml").toString();
                    module_yml.close();
                }
            }

            jar.close();
        } catch (Throwable ignored) {}

        return version;
    }

    /**
     * Get the module description
     * as string
     *
     * @return the module description as string
     */
    @Override
    public @NotNull String description() {
        String description = "LockLogin module ( " + this.getClass().getPackage().getName() + " )";

        try {
            String flName = new java.io.File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()).getName();

            JarFile jar = new JarFile(new File(LockLoginSpigot.plugin.getDataFolder() + File.separator + "modules", flName));
            ZipEntry moduleEntry = jar.getEntry("module.yml");

            if (moduleEntry != null) {
                InputStream module_yml = jar.getInputStream(moduleEntry);
                if (module_yml != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> values = yaml.load(module_yml);

                    Object desc = values.getOrDefault("description", "LockLogin module ( " + this.getClass().getPackage().getName() + " )");
                    if (desc instanceof List) {
                        List<?> list = (List<?>) desc;
                        StringBuilder descBuilder = new StringBuilder();

                        for (Object obj : list) {
                            descBuilder.append(obj).append(" ");
                        }

                        description = StringUtils.replaceLast(descBuilder.toString(), " ", "");
                    } else {
                        if (desc.getClass().isArray()) {
                            Object[] array = (Object[]) desc;

                            StringBuilder descBuilder = new StringBuilder();

                            for (Object obj : array) {
                                descBuilder.append(obj).append(" ");
                            }

                            description = StringUtils.replaceLast(descBuilder.toString(), " ", "");
                        } else {
                            description = desc.toString();
                        }
                    }

                    module_yml.close();
                }
            }

            jar.close();
        } catch (Throwable ignored) {}

        return description;
    }

    /**
     * Get the module auth
     *
     * @return the module author
     */
    @Override
    public @NotNull String author() {
        String author = "KarmaDev";

        try {
            String flName = new java.io.File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()).getName();

            JarFile jar = new JarFile(new File(LockLoginSpigot.plugin.getDataFolder() + File.separator + "modules", flName));
            ZipEntry moduleEntry = jar.getEntry("module.yml");

            if (moduleEntry != null) {
                InputStream module_yml = jar.getInputStream(moduleEntry);
                if (module_yml != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> values = yaml.load(module_yml);

                    Object desc = values.getOrDefault("authors", "KarmaDev");
                    if (desc instanceof List) {
                        List<?> list = (List<?>) desc;
                        StringBuilder descBuilder = new StringBuilder();

                        for (Object obj : list) {
                            descBuilder.append(obj).append(" - ");
                        }

                        author = StringUtils.replaceLast(descBuilder.toString(), " - ", "");
                    } else {
                        if (desc.getClass().isArray()) {
                            Object[] array = (Object[]) desc;

                            StringBuilder descBuilder = new StringBuilder();

                            for (Object obj : array) {
                                descBuilder.append(obj).append(" - ");
                            }

                            author = StringUtils.replaceLast(descBuilder.toString(), " - ", "");
                        } else {
                            author = desc.toString();
                        }
                    }

                    module_yml.close();
                }
            }

            jar.close();
        } catch (Throwable ignored) {}

        return author;
    }

    /**
     * Get module update url
     *
     * @return the module update url
     */
    @Override
    public @NotNull String update_url() {
        String url = "";

        try {
            String flName = new java.io.File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()).getName();

            JarFile jar = new JarFile(new File(LockLoginSpigot.plugin.getDataFolder() + File.separator + "modules", flName));
            ZipEntry moduleEntry = jar.getEntry("module.yml");

            if (moduleEntry != null) {
                InputStream module_yml = jar.getInputStream(moduleEntry);
                if (module_yml != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> values = yaml.load(module_yml);

                    url = values.getOrDefault("update_url", "").toString();
                    module_yml.close();
                }
            }

            jar.close();
        } catch (Throwable ignored) {}

        return url;
    }

    /**
     * Get the module data folder
     *
     * @return the module data folder
     */
    public final File getDataFolder() {
        File jar = new File(this.getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());

        return jar.getParentFile();
    }

    /**
     * Get the module file
     *
     * @param name the file name
     * @param path the file sub-directory
     * @return the module file
     */
    public final File getFile(final String name, final String... path) {
        if (path.length > 1) {
            StringBuilder pathBuilder = new StringBuilder();

            for (String dir : path) {
                pathBuilder.append(dir).append(File.separator);
            }

            String finalPath = StringUtils.replaceLast(pathBuilder.toString(), File.separator, "");

            File jar = new File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());
            File dataFolder = jar.getParentFile();
            File directory = new File(dataFolder, finalPath);

            return new File(directory, name);
        } else {
            File jar = new File(this.getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());

            File dataFolder = jar.getParentFile();
            return new File(dataFolder, name);
        }
    }

    /**
     * Get the plugin scheduler
     *
     * @param period the timer period
     * @param repeat repeat the timer on end
     * @return the custom plugin scheduler
     */
    public final AdvancedPluginTimer getScheduler(final int period, final boolean repeat) {
        return new AdvancedPluginTimer(LockLoginSpigot.plugin, period, repeat);
    }

    /**
     * Get the plugin scheduler
     *
     * @param period the timer period
     * @return the custom plugin scheduler
     */
    public final AdvancedPluginTimer getScheduler(final int period) {
        return new AdvancedPluginTimer(LockLoginSpigot.plugin, period);
    }
}
