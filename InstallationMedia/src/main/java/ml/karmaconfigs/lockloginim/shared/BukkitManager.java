package ml.karmaconfigs.lockloginim.shared;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class BukkitManager {


    /**
     * Unload a plugin
     *
     * @param plugin the plugin to unload
     */
    public final void unload(final JavaPlugin plugin) {
        String name = plugin.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap;

        List<Plugin> plugins;

        Map<String, Plugin> names;
        Map<String, Command> commands;

        pluginManager.disablePlugin(plugin);

        try {
            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);

            pluginManager.disablePlugin(plugin);

            if (plugins != null)
                plugins.remove(plugin);

            if (names != null)
                names.remove(name);

            if (commandMap != null) {
                for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Command> entry = it.next();
                    if (entry.getValue() instanceof PluginCommand) {
                        PluginCommand c = (PluginCommand) entry.getValue();
                        if (c.getPlugin() == plugin) {
                            c.unregister(commandMap);
                            it.remove();
                        }
                    }
                }
            }

            ClassLoader cl = plugin.getClass().getClassLoader();

            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getClass().getClassLoader() == cl) {
                    try {
                        thread.interrupt();
                        thread.join(2000);
                        if (thread.isAlive()) {
                            thread.interrupt();
                        }
                    } catch (Throwable ignore) {
                    }
                }
            }

            if (cl instanceof URLClassLoader) {
                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

                ((URLClassLoader) cl).close();
            }
        } catch (Throwable e) {
            plugin.getServer().getLogger().log(Level.SEVERE, "ERROR WHILE UNLOADING PLUGIN " + plugin.getName(), e);
        }

        System.gc();
    }

    /**
     * Load a plugin file
     *
     * @param pluginFile the plugin file
     */
    public final void load(File pluginFile) {
        try {
            Plugin target = Bukkit.getPluginManager().loadPlugin(pluginFile);
            target.onLoad();
            Bukkit.getPluginManager().enablePlugin(target);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
