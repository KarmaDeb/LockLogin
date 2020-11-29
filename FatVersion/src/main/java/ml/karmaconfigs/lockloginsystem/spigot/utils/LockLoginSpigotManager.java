package ml.karmaconfigs.lockloginsystem.spigot.utils;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

public final class LockLoginSpigotManager implements LockLoginSpigot {

    /**
     * Unload LockLogin
     */
    public final void unload() {
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
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while unloading LockLogin");
        }

        System.gc();
    }

    /**
     * Load LockLogin
     *
     * @param pluginFile LockLogin .jar
     */
    public final void load(File pluginFile) {
        try {
            Plugin target = Bukkit.getPluginManager().loadPlugin(pluginFile);
            target.onLoad();
            Bukkit.getPluginManager().enablePlugin(target);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while loading LockLogin");
        }
    }

    /**
     * Get the .jar version
     *
     * @param readFrom the file to read from
     *
     * Private GSA code
     *
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
     *
     * @return the .jar version
     */
    private String getJarVersion(File readFrom) {
        try {
            JarFile newLockLogin = new JarFile(readFrom);
            JarEntry pluginYML = newLockLogin.getJarEntry("plugin.yml");
            if (pluginYML != null) {
                InputStream pluginInfo = newLockLogin.getInputStream(pluginYML);
                InputStreamReader reader = new InputStreamReader(pluginInfo, StandardCharsets.UTF_8);

                YamlConfiguration desc = YamlConfiguration.loadConfiguration(reader);

                newLockLogin.close();
                pluginInfo.close();
                reader.close();
                return desc.getString("version");
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Private GSA code
     *
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
     *
     * @param readFrom the .jar to read from
     * @return if the .jar specified to ignore version differences
     */
    private boolean ignoredUpdateVersion(File readFrom) {
        try {
            JarFile newLockLogin = new JarFile(readFrom);
            JarEntry pluginYML = newLockLogin.getJarEntry("plugin.yml");
            if (pluginYML != null) {
                InputStream pluginInfo = newLockLogin.getInputStream(pluginYML);
                InputStreamReader reader = new InputStreamReader(pluginInfo, StandardCharsets.UTF_8);

                YamlConfiguration desc = YamlConfiguration.loadConfiguration(reader);

                newLockLogin.close();
                pluginInfo.close();
                reader.close();
                return desc.getBoolean("ignoreUpdateVersion");
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Apply LockLogin pending updates
     * or reload the plugin
     *
     * Private GSA code
     *
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
     */
    public final boolean applyUpdate() {
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File lockLogin = new File(pluginsFolder, LockLoginSpigot.jar);
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);
        try {
            boolean unloaded = false;

            if (updatedLockLogin.exists()) {
                Console.send(plugin, "Updating LockLogin, checking new LockLogin.jar info...", Level.INFO);
                String newVersion = getJarVersion(updatedLockLogin);
                String thisVersion = getJarVersion(lockLogin);

                if (newVersion != null && !newVersion.isEmpty() && thisVersion != null && !thisVersion.isEmpty()) {
                    int nVer = Integer.parseInt(newVersion.replaceAll("[aA-zZ]", "").replace(".", ""));
                    int aVer = Integer.parseInt(thisVersion.replaceAll("[aA-zZ]", "").replace(".", ""));

                    boolean shouldUpdate = ignoredUpdateVersion(updatedLockLogin);
                    if (!shouldUpdate) {
                        shouldUpdate = nVer > aVer;
                    }

                    if (shouldUpdate) {
                        unload();
                        unloaded = true;
                        if (lockLogin.delete()) {
                            if (updatedLockLogin.renameTo(lockLogin)) {
                                updatedLockLogin = new File(pluginsFolder + "/update/", jar);

                                if (updatedLockLogin.delete()) {
                                    logger.scheduleLog(Level.INFO, "LockLogin updated");
                                    Console.send(plugin, "LockLogin updated successfully", Level.INFO);
                                    return true;
                                }
                            }
                        }
                    } else {
                        Console.send(plugin, "Updated cancelled due the plugins/update/{0} LockLogin instance version is lower than the actual", Level.GRAVE, jar);
                        if (updatedLockLogin.delete()) {
                            Console.send(plugin, "Old LockLogin instance removed", Level.INFO);
                        }
                    }
                } else {
                    Console.send(plugin, "New LockLogin instance plugin.yml is not valid, download the latest version manually from {0}", Level.GRAVE, "https://www.spigotmc.org/resources/gsa-locklogin.75156/");
                    if (updatedLockLogin.delete()) {
                        Console.send(plugin, "Corrupt LockLogin instance removed", Level.INFO);
                    }
                }
            }

            if (unloaded) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        load(lockLogin);
                    }
                }, 5000);
            }
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while updating LockLogin");
        }

        return false;
    }
}