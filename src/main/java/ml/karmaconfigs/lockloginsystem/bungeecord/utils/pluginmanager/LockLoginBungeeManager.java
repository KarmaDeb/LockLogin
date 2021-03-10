package ml.karmaconfigs.lockloginsystem.bungeecord.utils.pluginmanager;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.Main;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;

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

@SuppressWarnings("unused")
public final class LockLoginBungeeManager implements LockLoginBungee {

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

                org.bukkit.configuration.file.YamlConfiguration desc = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(reader);

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
        InterfaceUtils utils = new InterfaceUtils();

        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File lockLogin = new File(pluginsFolder, LockLoginBungee.jar);
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

        if (user != null) {
            try {
                boolean unloaded = false;

                if (updatedLockLogin.exists()) {
                    user.send("&eUpdating LockLogin, checking new LockLogin.jar info...");
                    String newVersion = getJarVersion(updatedLockLogin);
                    String thisVersion = getJarVersion(lockLogin);

                    if (newVersion != null && !newVersion.isEmpty() && thisVersion != null && !thisVersion.isEmpty()) {
                        int nVer = Integer.parseInt(newVersion.replaceAll("[aA-zZ]", "").replace(".", ""));
                        int aVer = Integer.parseInt(thisVersion.replaceAll("[aA-zZ]", "").replace(".", ""));

                        boolean shouldUpdate = ignoredUpdateVersion(updatedLockLogin);
                        if (!shouldUpdate) {
                            shouldUpdate = nVer > aVer;
                        }

                        if (shouldUpdate && utils.isReadyToUpdate()) {
                            unloadPlugin();
                            unloaded = true;
                            if (lockLogin.delete()) {
                                if (updatedLockLogin.renameTo(lockLogin)) {
                                    if (!updatedLockLogin.delete()) {
                                        updatedLockLogin.deleteOnExit();
                                    }

                                    logger.scheduleLog(Level.INFO, "LockLogin updated");
                                    user.send("&aLockLogin updated successfully");
                                    utils.setReadyToUpdate(false);
                                }
                            } else {
                                loadPlugin(lockLogin);
                                user.send("&cLockLogin update failed");
                                return;
                            }
                        } else {
                            if (utils.isReadyToUpdate()) {
                                user.send("&cUpdated cancelled due the plugins/update/" + jar + " LockLogin instance version is lower than the actual");
                                if (updatedLockLogin.delete()) {
                                    user.send("&aOld LockLogin instance removed");
                                }
                            } else {
                                user.send("&cUpdate cancelled due LockLogin update is still downloading");
                            }
                        }
                    } else {
                        user.send("&cNew LockLogin instance plugin.yml is not valid, download the latest version manually from &ehttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
                        if (updatedLockLogin.delete()) {
                            user.send("&aCorrupt LockLogin instance removed");
                        }
                    }
                } else {
                    user.send(BungeeFiles.messages.prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                    if (ConfigGetter.manager.reload())
                        user.send(BungeeFiles.messages.prefix() + "&aConfig file reloaded!");
                    if (MessageGetter.manager.reload())
                        user.send(BungeeFiles.messages.prefix() + "&aMessages file reloaded!");
                }

                if (unloaded) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            loadPlugin(lockLogin);
                        }
                    }, 5000);
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while updating LockLogin");
                user.send("&cError while updating LockLogin");
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

                        if (shouldUpdate && utils.isReadyToUpdate()) {
                            unloadPlugin();
                            unloaded = true;
                            if (lockLogin.delete()) {
                                if (updatedLockLogin.renameTo(lockLogin)) {
                                    if (!updatedLockLogin.delete()) {
                                        updatedLockLogin.deleteOnExit();
                                    }

                                    logger.scheduleLog(Level.INFO, "LockLogin updated");
                                    Console.send(plugin, "LockLogin updated successfully", Level.INFO);
                                    utils.setReadyToUpdate(false);
                                }
                            } else {
                                loadPlugin(lockLogin);
                                Console.send(plugin, "LockLogin update failed", Level.WARNING);
                                return;
                            }
                        } else {
                            if (utils.isReadyToUpdate()) {
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
                    Console.send(BungeeFiles.messages.prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                    if (ConfigGetter.manager.reload())
                        Console.send(BungeeFiles.messages.prefix() + "&aConfig file reloaded!");
                    if (MessageGetter.manager.reload())
                        Console.send(BungeeFiles.messages.prefix() + "&aMessages file reloaded");
                }

                if (unloaded) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            loadPlugin(lockLogin);
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
