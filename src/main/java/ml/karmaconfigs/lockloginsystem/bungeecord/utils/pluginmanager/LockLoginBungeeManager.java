package ml.karmaconfigs.lockloginsystem.bungeecord.utils.pluginmanager;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.FileUtilities;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.Main;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.PluginManagerBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;

/**
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
@SuppressWarnings("unused")
public final class LockLoginBungeeManager implements LockLoginBungee, BungeeFiles {

    @SuppressWarnings("deprecation")
    public final void unloadPlugin() {
        IllegalStateException error = new IllegalStateException("Errors occurred while unloading plugin " + plugin.getDescription().getName()) {
            private static final long serialVersionUID = 1L;

            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        };

        PluginManager pluginmanager = ProxyServer.getInstance().getPluginManager();
        ClassLoader pluginclassloader = plugin.getClass().getClassLoader();

        try {
            plugin.onDisable();
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        try {
            for (Handler handler : plugin.getLogger().getHandlers()) {
                handler.close();
            }
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        try {
            pluginmanager.unregisterListeners(plugin);
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        try {
            pluginmanager.unregisterCommands(plugin);
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        try {
            ProxyServer.getInstance().getScheduler().cancel(plugin);
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        try {
            plugin.getExecutorService().shutdownNow();
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getClass().getClassLoader() == pluginclassloader) {
                try {
                    thread.interrupt();
                    thread.join(2000);
                    if (thread.isAlive()) {
                        thread.stop();
                    }
                } catch (Throwable t) {
                    error.addSuppressed(t);
                }
            }
        }

        EventBusManager.completeIntents(plugin);

        try {
            Map<String, Command> commandMap = Reflections.getFieldValue(pluginmanager, "commandMap");
            commandMap.entrySet().removeIf(entry -> entry.getValue().getClass().getClassLoader() == pluginclassloader);
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        try {
            Reflections.<Map<String, Plugin>>getFieldValue(pluginmanager, "plugins").values().remove(plugin);
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        if (pluginclassloader instanceof URLClassLoader) {
            try {
                ((URLClassLoader) pluginclassloader).close();
            } catch (Throwable t) {
                error.addSuppressed(t);
            }
        }

        try {
            Reflections.<Set<ClassLoader>>getStaticFieldValue(pluginclassloader.getClass(), "allLoaders").remove(pluginclassloader);
        } catch (Throwable t) {
            error.addSuppressed(t);
        }

        if (error.getSuppressed().length > 0) {
            error.printStackTrace();
        }
    }

    public final void loadPlugin(File pluginfile) {
        ProxyServer proxyserver = ProxyServer.getInstance();
        PluginManager pluginmanager = proxyserver.getPluginManager();

        try (JarFile jar = new JarFile(pluginfile)) {
            JarEntry pdf = jar.getJarEntry("bungee.yml");
            if (pdf == null) {
                pdf = jar.getJarEntry("plugin.yml");
            }
            try (InputStream in = jar.getInputStream(pdf)) {
                PluginDescription desc = new Yaml().loadAs(in, PluginDescription.class);
                desc.setFile(pluginfile);
                HashSet<String> plugins = new HashSet<>();
                for (Plugin plugin : pluginmanager.getPlugins()) {
                    plugins.add(plugin.getDescription().getName());
                }
                for (String dependency : desc.getDepends()) {
                    if (!plugins.contains(dependency)) {
                        throw new IllegalArgumentException(MessageFormat.format("Missing plugin dependency {0}", dependency));
                    }
                }
                Plugin plugin = (Plugin)
                        Reflections.setAccessible(
                                Main.class.getClassLoader().getClass()
                                        .getDeclaredConstructor(ProxyServer.class, PluginDescription.class, URL[].class)
                        )
                                .newInstance(proxyserver, desc, new URL[]{pluginfile.toURI().toURL()})
                                .loadClass(desc.getMain()).getDeclaredConstructor()
                                .newInstance();
                Reflections.invokeMethod(plugin, "init", proxyserver, desc);
                Reflections.<Map<String, Plugin>>getFieldValue(pluginmanager, "plugins").put(desc.getName(), plugin);

                plugin.onEnable();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Get the .jar version
     * <p>
     * Private GSA code
     * <p>
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
     *
     * @param readFrom the file to read from
     * @return the .jar version
     */
    private String getJarVersion(File readFrom) {
        try {
            JarFile newLockLogin = new JarFile(readFrom);
            JarEntry pluginYML = newLockLogin.getJarEntry("bungee.yml");
            if (pluginYML != null) {
                InputStream pluginInfo = newLockLogin.getInputStream(pluginYML);
                InputStreamReader reader = new InputStreamReader(pluginInfo, StandardCharsets.UTF_8);

                Configuration desc = YamlConfiguration.getProvider(YamlConfiguration.class).load(reader);

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

                Configuration desc = YamlConfiguration.getProvider(YamlConfiguration.class).load(reader);

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
        File lockLogin = new File(pluginsFolder, LockLoginBungee.jar);
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

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
                    unloadPlugin();

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

                                loadPlugin(new_locklogin);
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

                PluginManagerBungee manager = new PluginManagerBungee();
                manager.setupFiles();

                if (config.isMySQL()) {
                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                        User logged_user = new User(player);

                        if (!logged_user.isRegistered()) {
                            if (config.registerRestricted()) {
                                logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                                return;
                            }
                        }

                        Utils sql = new Utils(player.getUniqueId(), player.getName());
                        sql.createUser();

                        String UUID = player.getUniqueId().toString().replace("-", "");

                        FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                        fm.setInternal("auto-generated/userTemplate.yml");

                        if (fm.getManaged().exists()) {
                            if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                                if (fm.isSet("Password")) {
                                    if (!fm.isEmpty("Password")) {
                                        AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                                        migrate.start();

                                        Console.send(plugin, messages.migrating(player.getUniqueId().toString()), Level.INFO);
                                        fm.delete();
                                    }
                                }
                            }
                        }

                        if (sql.getName() == null || sql.getName().isEmpty())
                            sql.setName(player.getName());
                    }
                } else {
                    Utils utils = new Utils();
                    for (String id : utils.getUUIDs()) {
                        utils = new Utils(id, utils.fetchName(id));

                        AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.BUNGEE);
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
                    unloadPlugin();

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

                                loadPlugin(new_locklogin);
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

                PluginManagerBungee manager = new PluginManagerBungee();
                manager.setupFiles();

                if (config.isMySQL()) {
                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                        User logged_user = new User(player);

                        if (!logged_user.isRegistered()) {
                            if (config.registerRestricted()) {
                                logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                                return;
                            }
                        }

                        Utils sql = new Utils(player.getUniqueId(), player.getName());
                        sql.createUser();

                        String UUID = player.getUniqueId().toString().replace("-", "");

                        FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                        fm.setInternal("auto-generated/userTemplate.yml");

                        if (fm.getManaged().exists()) {
                            if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                                if (fm.isSet("Password")) {
                                    if (!fm.isEmpty("Password")) {
                                        AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                                        migrate.start();

                                        Console.send(plugin, messages.migrating(player.getUniqueId().toString()), Level.INFO);
                                        fm.delete();
                                    }
                                }
                            }
                        }

                        if (sql.getName() == null || sql.getName().isEmpty())
                            sql.setName(player.getName());
                    }
                } else {
                    Utils utils = new Utils();
                    for (String id : utils.getUUIDs()) {
                        utils = new Utils(id, utils.fetchName(id));

                        AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.BUNGEE);
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

            PluginManagerBungee manager = new PluginManagerBungee();
            manager.setupFiles();

            if (config.isMySQL()) {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    User logged_user = new User(player);

                    if (!logged_user.isRegistered()) {
                        if (config.registerRestricted()) {
                            logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                            return;
                        }
                    }

                    Utils sql = new Utils(player.getUniqueId(), player.getName());
                    sql.createUser();

                    String UUID = player.getUniqueId().toString().replace("-", "");

                    FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                    fm.setInternal("auto-generated/userTemplate.yml");

                    if (fm.getManaged().exists()) {
                        if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                            if (fm.isSet("Password")) {
                                if (!fm.isEmpty("Password")) {
                                    AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                                    migrate.start();

                                    Console.send(plugin, messages.migrating(player.getUniqueId().toString()), Level.INFO);
                                    fm.delete();
                                }
                            }
                        }
                    }

                    if (sql.getName() == null || sql.getName().isEmpty())
                        sql.setName(player.getName());
                }
            } else {
                Utils utils = new Utils();
                for (String id : utils.getUUIDs()) {
                    utils = new Utils(id, utils.fetchName(id));

                    AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.BUNGEE);
                    migrate.start();
                }

                Bucket.terminateMySQL();
            }
        } else {
            if (ConfigGetter.manager.reload())
                Console.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                Console.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerBungee manager = new PluginManagerBungee();
            manager.setupFiles();

            if (config.isMySQL()) {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    User logged_user = new User(player);

                    if (!logged_user.isRegistered()) {
                        if (config.registerRestricted()) {
                            logged_user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                            return;
                        }
                    }

                    Utils sql = new Utils(player.getUniqueId(), player.getName());
                    sql.createUser();

                    String UUID = player.getUniqueId().toString().replace("-", "");

                    FileManager fm = new FileManager(UUID + ".yml", "playerdata");
                    fm.setInternal("auto-generated/userTemplate.yml");

                    if (fm.getManaged().exists()) {
                        if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                            if (fm.isSet("Password")) {
                                if (!fm.isEmpty("Password")) {
                                    AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                                    migrate.start();

                                    Console.send(plugin, messages.migrating(player.getUniqueId().toString()), Level.INFO);
                                    fm.delete();
                                }
                            }
                        }
                    }

                    if (sql.getName() == null || sql.getName().isEmpty())
                        sql.setName(player.getName());
                }
            } else {
                Utils utils = new Utils();
                for (String id : utils.getUUIDs()) {
                    utils = new Utils(id, utils.fetchName(id));

                    AccountMigrate migrate = new AccountMigrate(utils, Migrate.YAML, Platform.BUNGEE);
                    migrate.start();
                }

                Bucket.terminateMySQL();
            }
        }
    }
}
