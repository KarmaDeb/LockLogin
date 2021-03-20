package ml.karmaconfigs.lockloginsystem.spigot.utils;

import ml.karmaconfigs.api.shared.FileUtilities;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

public final class LockLoginSpigotManager implements LockLoginSpigot, SpigotFiles {

    /**
     * Unload LockLogin
     */
    @SuppressWarnings("all")
    public final void unload() {
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
    public final void load(File pluginFile) {
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
    public final void applyUpdate(@Nullable final User user) {
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File lockLogin = new File(pluginsFolder, LockLoginSpigot.jar);
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);

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
                    update = ignoredUpdateVersion(updatedLockLogin);
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
                            }
                            second--;
                        }
                    }, 0, 1000);
                } else {
                    user.send(messages.prefix() + "&cOld LockLogin instance removed ( didn't update because current version is older than the new )");
                }
            } else {
                if (ConfigGetter.manager.reload())
                    user.send(messages.prefix() + "&aConfiguration file reloaded");
                if (MessageGetter.manager.reload())
                    user.send(messages.prefix() + "&aMessages file reloaded");

                PluginManagerSpigot manager = new PluginManagerSpigot();
                manager.setupFiles();

                if (config.isMySQL()) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        User logged_user = new User(player);

                        if (!logged_user.isRegistered()) {
                            if (config.registerRestricted()) {
                                logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                                return;
                            }
                        }

                        Utils sql = new Utils(player.getUniqueId(), plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                        sql.createUser();

                        String UUID = player.getUniqueId().toString().replace("-", "");

                        FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                        fm.setInternal("auto-generated/userTemplate.yml");

                        if (fm.getManaged().exists()) {
                            if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                                if (fm.isSet("Password")) {
                                    if (!fm.isEmpty("Password")) {
                                        AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.SPIGOT);
                                        migrate.start();

                                        Console.send(plugin, messages.migratingAccount(player.getUniqueId().toString()), Level.INFO);
                                        fm.delete();
                                    }
                                }
                            }
                        }

                        if (sql.getName() == null || sql.getName().isEmpty())
                            sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                    }
                } else {
                    Utils utils = new Utils();
                    for (String id : utils.getUUIDs()) {
                        utils = new Utils(id, utils.fetchName(id));

                        AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.SPIGOT);
                        migrate.start();
                    }

                    Bucket.terminateMySQL();
                }
            }
        } else {
            if (updatedLockLogin.exists()) {
                Console.send(messages.prefix() + "&7Checking update target LockLogin version");

                if (new_version > cur_version) {
                    update = true;
                } else {
                    update = ignoredUpdateVersion(updatedLockLogin);
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
                            }
                            second--;
                        }
                    }, 0, 1000);
                } else {
                    Console.send(messages.prefix() + "&cOld LockLogin instance removed ( didn't update because current version is older than the new )");
                }
            } else {
                if (ConfigGetter.manager.reload())
                    Console.send(messages.prefix() + "&aConfiguration file reloaded");
                if (MessageGetter.manager.reload())
                    Console.send(messages.prefix() + "&aMessages file reloaded");

                PluginManagerSpigot manager = new PluginManagerSpigot();
                manager.setupFiles();

                if (config.isMySQL()) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        User logged_user = new User(player);

                        if (!logged_user.isRegistered()) {
                            if (config.registerRestricted()) {
                                logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                                return;
                            }
                        }

                        Utils sql = new Utils(player.getUniqueId(), plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                        sql.createUser();

                        String UUID = player.getUniqueId().toString().replace("-", "");

                        FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                        fm.setInternal("auto-generated/userTemplate.yml");

                        if (fm.getManaged().exists()) {
                            if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                                if (fm.isSet("Password")) {
                                    if (!fm.isEmpty("Password")) {
                                        AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.SPIGOT);
                                        migrate.start();

                                        Console.send(plugin, messages.migratingAccount(player.getUniqueId().toString()), Level.INFO);
                                        fm.delete();
                                    }
                                }
                            }
                        }

                        if (sql.getName() == null || sql.getName().isEmpty())
                            sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                    }
                } else {
                    Utils utils = new Utils();
                    for (String id : utils.getUUIDs()) {
                        utils = new Utils(id, utils.fetchName(id));

                        AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.SPIGOT);
                        migrate.start();
                    }

                    Bucket.terminateMySQL();
                }
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
        if (user != null) {
            if (ConfigGetter.manager.reload())
                user.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                user.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerSpigot manager = new PluginManagerSpigot();
            manager.setupFiles();

            if (config.isMySQL()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    User logged_user = new User(player);

                    if (!logged_user.isRegistered()) {
                        if (config.registerRestricted()) {
                            logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                            return;
                        }
                    }

                    Utils sql = new Utils(player.getUniqueId(), plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                    sql.createUser();

                    String UUID = player.getUniqueId().toString().replace("-", "");

                    FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                    fm.setInternal("auto-generated/userTemplate.yml");

                    if (fm.getManaged().exists()) {
                        if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                            if (fm.isSet("Password")) {
                                if (!fm.isEmpty("Password")) {
                                    AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.SPIGOT);
                                    migrate.start();

                                    Console.send(plugin, messages.migratingAccount(player.getUniqueId().toString()), Level.INFO);
                                    fm.delete();
                                }
                            }
                        }
                    }

                    if (sql.getName() == null || sql.getName().isEmpty())
                        sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                }
            } else {
                Utils utils = new Utils();
                for (String id : utils.getUUIDs()) {
                    utils = new Utils(id, utils.fetchName(id));

                    AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.SPIGOT);
                    migrate.start();
                }

                Bucket.terminateMySQL();
            }
        } else {
            if (ConfigGetter.manager.reload())
                Console.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                Console.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerSpigot manager = new PluginManagerSpigot();
            manager.setupFiles();

            if (config.isMySQL()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    User logged_user = new User(player);

                    if (!logged_user.isRegistered()) {
                        if (config.registerRestricted()) {
                            logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                            return;
                        }
                    }

                    Utils sql = new Utils(player.getUniqueId(), plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                    sql.createUser();

                    String UUID = player.getUniqueId().toString().replace("-", "");

                    FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                    fm.setInternal("auto-generated/userTemplate.yml");

                    if (fm.getManaged().exists()) {
                        if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                            if (fm.isSet("Password")) {
                                if (!fm.isEmpty("Password")) {
                                    AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.SPIGOT);
                                    migrate.start();

                                    Console.send(plugin, messages.migratingAccount(player.getUniqueId().toString()), Level.INFO);
                                    fm.delete();
                                }
                            }
                        }
                    }

                    if (sql.getName() == null || sql.getName().isEmpty())
                        sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                }
            } else {
                Utils utils = new Utils();
                for (String id : utils.getUUIDs()) {
                    utils = new Utils(id, utils.fetchName(id));

                    AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.SPIGOT);
                    migrate.start();
                }

                Bucket.terminateMySQL();
            }
        }
    }
}