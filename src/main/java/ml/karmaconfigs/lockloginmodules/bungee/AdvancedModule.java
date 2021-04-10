package ml.karmaconfigs.lockloginmodules.bungee;

import ml.karmaconfigs.api.bungee.timer.AdvancedPluginTimer;
import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;

import java.io.File;

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
     * Get the module data folder
     *
     * @return the module data folder
     */
    public final File getDataFolder() {
        return new File(LockLoginBungee.getJar().getParentFile() + File.separator + "LockLogin" + File.separator + "modules", this.name());
    }

    /**
     * Get the module file
     *
     * @param name the file name
     * @param path the file sub-directory
     * @return the module file
     */
    public final File getFile(final String name, final String... path) {
        if (path.length > 0) {
            StringBuilder path_builder = new StringBuilder();
            for (String sub_path : path)
                path_builder.append(File.separator).append(sub_path);

            return new File(getDataFolder().getAbsolutePath().replace("%20", " ") + path_builder, name);
        } else {
            return new File(getDataFolder(), name);
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
        return new AdvancedPluginTimer(LockLoginBungee.plugin, period, repeat);
    }

    /**
     * Get the plugin scheduler
     *
     * @param period the timer period
     * @return the custom plugin scheduler
     */
    public final AdvancedPluginTimer getScheduler(final int period) {
        return new AdvancedPluginTimer(LockLoginBungee.plugin, period);
    }
}
