package ml.karmaconfigs.lockloginsystem.spigot.utils;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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

        List<?> plugins = null;

        Map<?, ?> names = null;
        Map<String, Command> commands = new HashMap<>();

        try {
            pluginManager.disablePlugin(JavaPlugin.getProvidingPlugin(Class.forName(plugin.getDescription().getMain())));

            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            if (pluginsField.get(pluginManager) instanceof List) {
                plugins = (List<?>) pluginsField.get(pluginManager);
            }

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            if (lookupNamesField.get(pluginManager) instanceof Map) {
                names = (Map<?, ?>) lookupNamesField.get(pluginManager);
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            if (knownCommandsField.get(commandMap) instanceof Map) {
                Map<?, ?> cmdField = (Map<?, ?>) knownCommandsField.get(commandMap);
                for (Object key : cmdField.keySet()) {
                    if (key instanceof String) {
                        String cmdKey = (String) key;
                        if (cmdField.get(key) instanceof Command) {
                            Command cmd = (Command) cmdField.get(key);
                            commands.put(cmdKey, cmd);
                        }
                    }
                }
            }

            pluginManager.disablePlugin(JavaPlugin.getProvidingPlugin(Class.forName(plugin.getDescription().getMain())));

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
            Bukkit.getPluginManager().loadPlugin(pluginFile);
            Bukkit.getPluginManager().enablePlugin(plugin);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while loading LockLogin");
        }
    }

    /**
     * Get the .jar version
     *
     * @param readFrom the file to read from
     *                 <p>
     *                 Private GSA code
     *                 <p>
     *                 The use of this code
     *                 without GSA team authorization
     *                 will be a violation of
     *                 terms of use determined
     *                 in <a href="https://karmaconfigs.ml/license/"> here </a>
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
     * <p>
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
     * <p>
     * Private GSA code
     * <p>
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
     *
     * @param user the issuer
     */
    public final void applyUpdate(@Nullable User user) {
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File lockLogin = new File(pluginsFolder, LockLoginSpigot.jar);
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);

        if (user != null) {
            try {
                boolean unloaded = false;

                if (updatedLockLogin.exists()) {
                    user.Message("&eUpdating LockLogin, checking new LockLogin.jar info...");
                    String newVersion = getJarVersion(updatedLockLogin);
                    String thisVersion = getJarVersion(lockLogin);

                    if (newVersion != null && !newVersion.isEmpty() && thisVersion != null && !thisVersion.isEmpty()) {
                        int nVer = Integer.parseInt(newVersion.replaceAll("[aA-zZ]", "").replace(".", ""));
                        int aVer = Integer.parseInt(thisVersion.replaceAll("[aA-zZ]", "").replace(".", ""));

                        boolean shouldUpdate = ignoredUpdateVersion(updatedLockLogin);
                        if (!shouldUpdate) {
                            shouldUpdate = nVer > aVer;
                        }

                        if (shouldUpdate && PluginManagerSpigot.manager.isReadyToUpdate()) {
                            unload();
                            unloaded = true;
                            if (lockLogin.delete()) {
                                if (updatedLockLogin.renameTo(lockLogin)) {
                                    if (!updatedLockLogin.delete()) {
                                        updatedLockLogin.deleteOnExit();
                                    }

                                    logger.scheduleLog(Level.INFO, "LockLogin updated");
                                    user.Message("&aLockLogin updated successfully");
                                    PluginManagerSpigot.manager.setReadyToUpdate(false);
                                }
                            } else {
                                load(lockLogin);
                                user.Message("&cLockLogin update failed");
                                return;
                            }
                        } else {
                            if (PluginManagerSpigot.manager.isReadyToUpdate()) {
                                user.Message("&cUpdated cancelled due the plugins/update/" + jar + " LockLogin instance version is lower than the actual");
                                if (updatedLockLogin.delete()) {
                                    user.Message("&aOld LockLogin instance removed");
                                }
                            } else {
                                user.Message("&cUpdate cancelled due LockLogin update is still downloading");
                            }
                        }
                    } else {
                        user.Message("&cNew LockLogin instance plugin.yml is not valid, download the latest version manually from &ehttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
                        if (updatedLockLogin.delete()) {
                            user.Message("&aCorrupt LockLogin instance removed");
                        }
                    }
                } else {
                    user.Message(SpigotFiles.messages.Prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                    if (ConfigGetter.manager.reload())
                        user.Message(SpigotFiles.messages.Prefix() + "&aConfig file reloaded!");
                    if (MessageGetter.manager.reload())
                        user.Message(SpigotFiles.messages.Prefix() + "&aMessages file reloaded!");
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
                user.Message("&cError while updating LockLogin");
            }
        } else {
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

                        if (shouldUpdate && PluginManagerSpigot.manager.isReadyToUpdate()) {
                            unload();
                            unloaded = true;
                            if (lockLogin.delete()) {
                                if (updatedLockLogin.renameTo(lockLogin)) {
                                    if (!updatedLockLogin.delete()) {
                                        updatedLockLogin.deleteOnExit();
                                    }

                                    logger.scheduleLog(Level.INFO, "LockLogin updated");
                                    Console.send(plugin, "LockLogin updated successfully", Level.INFO);
                                    PluginManagerSpigot.manager.setReadyToUpdate(false);
                                }
                            } else {
                                load(lockLogin);
                                Console.send(plugin, "LockLogin update failed", Level.WARNING);
                                return;
                            }
                        } else {
                            if (PluginManagerSpigot.manager.isReadyToUpdate()) {
                                Console.send(plugin, "Updated cancelled due the plugins/update/{0} LockLogin instance version is lower than the actual", Level.GRAVE, jar);
                                if (updatedLockLogin.delete()) {
                                    Console.send(plugin, "Old LockLogin instance removed", Level.INFO);
                                }
                            } else {
                                Console.send("&cUpdate cancelled due LockLogin update is still downloading");
                            }
                        }
                    } else {
                        Console.send(plugin, "New LockLogin instance plugin.yml is not valid, download the latest version manually from {0}", Level.GRAVE, "https://www.spigotmc.org/resources/gsa-locklogin.75156/");
                        if (updatedLockLogin.delete()) {
                            Console.send(plugin, "Corrupt LockLogin instance removed", Level.INFO);
                        }
                    }
                } else {
                    Console.send(SpigotFiles.messages.Prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                    if (ConfigGetter.manager.reload())
                        Console.send(SpigotFiles.messages.Prefix() + "&aConfig file reloaded!");
                    if (MessageGetter.manager.reload())
                        Console.send(SpigotFiles.messages.Prefix() + "&aMessages file reloaded");
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
                Console.send(plugin, "An error occurred while updating LockLogin, check logs for more info", Level.GRAVE);
            }
        }
    }
}