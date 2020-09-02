package ml.karmaconfigs.LockLogin.BungeeCord.Utils.PluginManager;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Main;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
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
                                .newInstance(proxyserver, desc, new URL[] {pluginfile.toURI().toURL()})
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
     *
     * Private GSA code
     *
     * The use of this code
     * without GSA team authorization
     * will be a violation of
     * terms of use determined
     * in <a href="https://karmaconfigs.ml/license/"> here </a>
     *
     * @param readFrom the file to read from
     * @return a String
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
        File lockLogin = new File(pluginsFolder, LockLoginBungee.getJarName());
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.getJarName());
        try {
            boolean unloaded = false;

            if (updatedLockLogin.exists()) {
                out.Alert("Updating LockLogin, checking new LockLogin.jar info...", WarningLevel.WARNING);
                String newVersion = getJarVersion(updatedLockLogin);
                String thisVersion = getJarVersion(lockLogin);

                if (newVersion != null && !newVersion.isEmpty() && thisVersion != null && !thisVersion.isEmpty()) {
                    int nVer = Integer.parseInt(newVersion.replaceAll("[aA-zZ]", "").replace(".", ""));
                    int aVer = Integer.parseInt(thisVersion.replaceAll("[aA-zZ]", "").replace(".", ""));

                    if (nVer > aVer) {
                        unloadPlugin();
                        unloaded = true;
                        if (lockLogin.delete()) {
                            if (updatedLockLogin.renameTo(lockLogin)) {
                                updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.getJarName());

                                if (updatedLockLogin.delete()) {
                                    Logger.log(Platform.BUNGEE, "INFO", "UPDATED LOCKLOGIN SUCCESSFULLY");
                                }
                                Main.updatePending = false;
                            }
                        }
                    } else {
                        out.Alert("Update have been cancelled due the /update/" + LockLoginBungee.getJarName() + " LockLogin version is lower than the running one", WarningLevel.ERROR);
                        if (updatedLockLogin.delete()) {
                            String path = updatedLockLogin.getPath().replaceAll("\\\\", "/");
                            out.Alert("Older LockLogin.jar ( " + path + " ) have been removed", WarningLevel.WARNING);
                        }
                    }
                } else {
                    out.Alert("New LockLogin version bungee.yml seems to be not valid, download it manually from https://www.spigotmc.org/resources/gsa-locklogin.75156/", WarningLevel.ERROR);
                    if (updatedLockLogin.delete()) {
                        String path = updatedLockLogin.getPath().replaceAll("\\\\", "/");
                        out.Alert("Removed corrupted LockLogin.jar ( " + path + " ) have been removed", WarningLevel.WARNING);
                    }
                }
            } else {
                out.Alert("Initializing LockLogin update as reload method, please wait, this process will take 5 seconds", WarningLevel.WARNING);
                unloadPlugin();
                unloaded = true;
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
            Logger.log(Platform.BUNGEE, "ERROR WHILE UPDATING LOCKLOGIN" + ": " + e.fillInStackTrace(), e);
        }
    }
}
