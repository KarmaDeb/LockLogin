package ml.karmaconfigs.lockloginsystem.bungee.utils.pluginmanager;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.utils.FileUtilities;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin.PluginStatusChangeEvent;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.Main;
import ml.karmaconfigs.lockloginsystem.bungee.utils.PluginManagerBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import net.md_5.bungee.api.ProxyServer;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
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
@SuppressWarnings("unused")
public final class LockLoginBungeeManager implements LockLoginBungee, BungeeFiles {

    @SuppressWarnings("deprecation")
    public static void unload() {
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

    private void load(File pluginFile) {
        ProxyServer proxyserver = ProxyServer.getInstance();
        PluginManager pluginmanager = proxyserver.getPluginManager();

        try (JarFile jar = new JarFile(pluginFile)) {
            JarEntry pdf = jar.getJarEntry("bungee.yml");
            if (pdf == null) {
                pdf = jar.getJarEntry("plugin.yml");
            }
            try (InputStream in = jar.getInputStream(pdf)) {
                PluginDescription desc = new Yaml().loadAs(in, PluginDescription.class);
                desc.setFile(pluginFile);
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
                                .newInstance(proxyserver, desc, new URL[]{pluginFile.toURI().toURL()})
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
                            Configuration plugin_yml = YamlConfiguration.getProvider(YamlConfiguration.class).load(stream);

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

        return new File(FileUtilities.getPluginsFolder() + File.separator + "update", LockLoginBungee.getJar().getName());
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
        File lockLogin = LockLoginBungee.getJar();
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
                    PluginStatusChangeEvent update_start = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_START, null);
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

                                PluginStatusChangeEvent update_end = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_END, null);
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
                    PluginStatusChangeEvent update_start = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_START, null);
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

                                PluginStatusChangeEvent update_end = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UPDATE_END, null);
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
        PluginStatusChangeEvent reload_start = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.RELOAD_START, null);
        LockLoginListener.callEvent(reload_start);

        if (user != null) {
            if (ConfigGetter.manager.reload())
                user.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                user.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerBungee b_manager = new PluginManagerBungee();
            b_manager.setupFiles();
        } else {
            if (ConfigGetter.manager.reload())
                Console.send(messages.prefix() + "&aConfiguration file reloaded");
            if (MessageGetter.manager.reload())
                Console.send(messages.prefix() + "&aMessages file reloaded");

            PluginManagerBungee b_manager = new PluginManagerBungee();
            b_manager.setupFiles();
        }

        PluginStatusChangeEvent reload_end = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.RELOAD_END, null);
        LockLoginListener.callEvent(reload_end);
    }
}
