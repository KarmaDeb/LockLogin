package ml.karmaconfigs.lockloginsystem.bukkit.utils;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.FileUtilities;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin.PluginStatusChangeEvent;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserJoinEvent;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
public final class LockLoginBukkitManager implements LockLoginSpigot, SpigotFiles {

    /**
     * Unload LockLogin
     */
    @SuppressWarnings("all")
    private void unload() {
        String name = plugin.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap = null;

        List<Plugin> plugins = null;

        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadlisteners = true;

        pluginManager.disablePlugin(plugin);

        try {
            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            } catch (Exception e) {
                reloadlisteners = false;
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(name))
            names.remove(name);

        if (listeners != null && reloadlisteners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                set.removeIf(value -> value.getPlugin() == plugin);
            }
        }

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

        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if (cl instanceof URLClassLoader) {
            try {
                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            try {
                ((URLClassLoader) cl).close();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        System.gc();
    }

    /**
     * Load LockLogin
     *
     * @param pluginFile LockLogin .jar
     */
    private void load(File pluginFile) {
        try {
            Plugin plugin = Bukkit.getPluginManager().loadPlugin(pluginFile);
            if (plugin != null)
                Bukkit.getPluginManager().enablePlugin(plugin);

        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while loading LockLogin");
        }
    }

    /**
     * Tries to localize LockLogin.jar plugin
     * file if provided does not exist or is null
     *
     * @return the LockLogin.jar update file
     */
    private File detectLockLogin() {
        File[] updates = new File(FileUtilities.getPluginsFolder(), "update").listFiles();
        if (updates != null) {
            for (File file : updates) {
                try {
                    JarFile jar = new JarFile(file);

                    ZipEntry entry = jar.getEntry("plugin.yml");
                    if (entry != null) {
                        InputStream stream = jar.getInputStream(entry);
                        if (stream != null) {
                            YamlConfiguration plugin_yml = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));

                            String name = plugin_yml.getString("name", "");
                            assert name != null;
                            if (!name.replaceAll("\\s", "").isEmpty()) {
                                name = name.replace("\"", "");
                                if (name.equals("LockLogin"))
                                    return file;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        }

        return new File(FileUtilities.getPluginsFolder() + File.separator + "update", LockLoginSpigot.getJar().getName());
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
    public final void applyUpdate(@Nullable final User user) {
        File lockLogin = LockLoginSpigot.getJar();
        File updatedLockLogin = detectLockLogin();

        int new_version = Integer.parseInt(FileInfo.getJarVersion(updatedLockLogin).replaceAll("[aA-zZ]", "").replace(".", ""));
        int cur_version = Integer.parseInt(FileInfo.getJarVersion(lockLogin).replaceAll("[aA-zZ]", "").replace(".", ""));

        VersionChannel new_channel = FileInfo.getChannel(updatedLockLogin);
        VersionChannel curr_channel = FileInfo.getChannel(lockLogin);

        boolean update;
        if (user != null) {
            if (updatedLockLogin.exists()) {
                user.send(messages.prefix() + "&7Checking update target LockLogin version");

                if (new_version > cur_version) {
                    update = true;
                } else {
                    update = FileInfo.unsafeUpdates(updatedLockLogin);
                    if (update)
                        user.send(messages.prefix() + "&7Target LockLogin version specifies to ignore age difference, this time is legal :)");
                }

                if (!update) {
                    switch (curr_channel) {
                        case SNAPSHOT:
                            switch (new_channel) {
                                case RC:
                                case RELEASE:
                                    update = true;
                            }
                        case RC:
                            if (new_channel == VersionChannel.RELEASE) {
                                update = true;
                            }
                        case RELEASE:
                        default:
                            break;
                    }
                }

                if (update) {
                    PluginStatusChangeEvent update_start = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_START);
                    LockLoginListener.callEvent(update_start);

                    unload();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Files.move(updatedLockLogin.toPath(), lockLogin.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                user.send(messages.prefix() + "&aMoved new locklogin instance to current LockLogin instance");
                            } catch (Throwable ex) {
                                ex.printStackTrace();
                                user.send(messages.prefix() + "&cLockLogin update failed");
                            }
                        }
                    }, 3000);

                    Timer load_timer = new Timer();
                    load_timer.schedule(new TimerTask() {
                        int second = 7;

                        @Override
                        public void run() {
                            if (second <= 5)
                                user.send(messages.prefix() + "&7LockLogin load in " + second + " seconds");

                            if (second <= 0) {
                                File new_locklogin = new File(FileUtilities.getPluginsFolder(), updatedLockLogin.getName());

                                load(new_locklogin);
                                user.send(messages.prefix() + "&7Update process finished");
                                load_timer.cancel();

                                PluginStatusChangeEvent update_end = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_END);
                                LockLoginListener.callEvent(update_end);
                            }
                            second--;
                        }
                    }, 0, 1000);
                } else {
                    user.send(messages.prefix() + "&cOld LockLogin instance removed ( didn't update because current version is older than the new )");
                }
            } else {
                user.send(messages.prefix() + "&cCouldn't update LockLogin because update file couldn't be found");
            }
        } else {
            if (updatedLockLogin.exists()) {
                Console.send(messages.prefix() + "&7Checking update target LockLogin version");

                if (new_version > cur_version) {
                    update = true;
                } else {
                    update = FileInfo.unsafeUpdates(updatedLockLogin);
                    if (update)
                        Console.send(messages.prefix() + "&7Target LockLogin version specifies to ignore age difference, this time is legal :)");
                }

                if (!update) {
                    switch (curr_channel) {
                        case SNAPSHOT:
                            switch (new_channel) {
                                case RC:
                                case RELEASE:
                                    update = true;
                            }
                        case RC:
                            if (new_channel == VersionChannel.RELEASE) {
                                update = true;
                            }
                        case RELEASE:
                        default:
                            break;
                    }
                }

                if (update) {
                    PluginStatusChangeEvent update_start = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_START);
                    LockLoginListener.callEvent(update_start);

                    unload();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Files.move(updatedLockLogin.toPath(), lockLogin.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                Console.send(messages.prefix() + "&aMoved new locklogin instance to current LockLogin instance");
                            } catch (Throwable ex) {
                                ex.printStackTrace();
                                Console.send(messages.prefix() + "&cLockLogin update failed");
                            }
                        }
                    }, 3000);

                    Timer load_timer = new Timer();
                    load_timer.schedule(new TimerTask() {
                        int second = 7;

                        @Override
                        public void run() {
                            if (second <= 5)
                                Console.send(messages.prefix() + "&7LockLogin load in " + second + " seconds");

                            if (second <= 0) {
                                File new_locklogin = new File(FileUtilities.getPluginsFolder(), updatedLockLogin.getName());

                                load(new_locklogin);
                                Console.send(messages.prefix() + "&7Update process finished");
                                load_timer.cancel();

                                PluginStatusChangeEvent update_end = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_END);
                                LockLoginListener.callEvent(update_end);
                            }
                            second--;
                        }
                    }, 0, 1000);
                } else {
                    Console.send(messages.prefix() + "&cOld LockLogin instance removed ( didn't update because current version is older than the new )");
                }
            } else {
                Console.send(messages.prefix() + "&cCouldn't update LockLogin because update file couldn't be found");
            }
        }
    }

    /**
     * Reload the plugin
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
    public final void reload(@Nullable final User user) {
        PluginStatusChangeEvent reload_start = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.RELOAD_START);
        LockLoginListener.callEvent(reload_start);

        if (user != null) {
            if (ConfigGetter.manager.reload())
                user.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                user.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerBukkit s_manager = new PluginManagerBukkit();
            s_manager.setupFiles();
        } else {
            if (ConfigGetter.manager.reload())
                Console.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                Console.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerBukkit s_manager = new PluginManagerBukkit();
            s_manager.setupFiles();

            if (config.isMySQL()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    User playerUser = new User(player);

                    UserJoinEvent event = new UserJoinEvent(playerUser);
                    LockLoginListener.callEvent(event);
                }
            }
        }

        PluginStatusChangeEvent reload_end = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.RELOAD_END);
        LockLoginListener.callEvent(reload_end);
    }
}