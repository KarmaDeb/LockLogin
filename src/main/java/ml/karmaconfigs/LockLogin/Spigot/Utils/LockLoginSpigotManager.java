package ml.karmaconfigs.LockLogin.Spigot.Utils;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.WarningLevel;
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
            Logger.log(Platform.SPIGOT, "ERROR WHILE UNLOADING LOCKLOGIN", e);
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
            Logger.log(Platform.SPIGOT, "ERROR WHILE LOADING LOCKLOGIN", e);
        }
    }

    /**
     * Get the .jar version
     *
     * @param readFrom the file to read from
     * @return a String
     *
     * Private GSA code
     *
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
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
    public final void applyUpdate() {
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File lockLogin = new File(pluginsFolder, LockLoginSpigot.getJarName());
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJarName());
        try {
            boolean unloaded = false;

            if (updatedLockLogin.exists()) {
                out.Alert("Updating LockLogin, checking new LockLogin.jar info...", WarningLevel.WARNING);
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
                                updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJarName());

                                if (updatedLockLogin.delete()) {
                                    Logger.log(Platform.SPIGOT, "INFO", "UPDATED LOCKLOGIN SUCCESSFULLY");
                                }
                            }
                        }
                    } else {
                        out.Alert("Update have been cancelled due the /update/" + LockLoginSpigot.getJarName() + " LockLogin version is lower than the running one", WarningLevel.ERROR);
                        if (updatedLockLogin.delete()) {
                            String path = updatedLockLogin.getPath().replaceAll("\\\\", "/");
                            out.Alert("Older LockLogin.jar ( " + path + " ) have been removed", WarningLevel.WARNING);
                        }
                    }
                } else {
                    out.Alert("New LockLogin version plugin.yml seems to be not valid, download it manually from https://www.spigotmc.org/resources/gsa-locklogin.75156/", WarningLevel.ERROR);
                    if (updatedLockLogin.delete()) {
                        String path = updatedLockLogin.getPath().replaceAll("\\\\", "/");
                        out.Alert("Removed corrupted LockLogin.jar ( " + path + " ) have been removed", WarningLevel.WARNING);
                    }
                }
            } else {
                out.Alert("Initializing LockLogin update as reload method, please wait, this process will take 5 seconds", WarningLevel.WARNING);
                unload();
                unloaded = true;
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
            Logger.log(Platform.SPIGOT, "ERROR WHILE UPDATING LOCKLOGIN" + ": " + e.fillInStackTrace(), e);
        }
    }
}
