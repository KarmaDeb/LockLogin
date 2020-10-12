package ml.karmaconfigs.LockLogin.Spigot.Utils;

import ml.karmaconfigs.LockLogin.Alerts.LockLoginAlerts;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.Metrics.SpigotMetrics;
import ml.karmaconfigs.LockLogin.MySQL.Bucket;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.Commands.*;
import ml.karmaconfigs.LockLogin.Spigot.Events.*;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.AllowedCommands;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.MySQLData;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.ConfigGetter;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.MessageGetter;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory.PinInventory;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.Version.DownloadLatest;
import ml.karmaconfigs.LockLogin.Version.LockLoginVersion;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

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

public final class PluginManagerSpigot implements LockLoginSpigot {

    /**
     * The onEnable actions the
     * plugin will perform
     */
    public final void enable() {
        if (!new ConfigGetter().isBungeeCord()) {
            Bucket.terminateMySQL();
        }

        Logger coreLogger = (Logger) LogManager.getRootLogger();
        coreLogger.addFilter(new ConsoleFilter());
        out.Message("--------------------");
        out.Message(" ");
        out.Message("&bEnabling {0} &bversion {1}");
        out.Message("&aInitializing files...");
        setupFiles(true);
        out.Message("&aRegistering commands....");
        registerCommands();
        out.Message("&aRegistering events...");
        registerEvents();
        out.Message(" ");
        out.Message("--------------------");
        if (!new ConfigGetter().isBungeeCord()) {
            if (new ConfigGetter().CheckForUpdates()) {
                startVersionChecker();
            } else {
                doVersionCheck();
            }
            startAlertChecker();
            setupPlayers();
            registerMetrics();
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!plugin.getServer().getMessenger().isIncomingChannelRegistered(plugin, "ll:info")) {
                        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "ll:info", new BungeeListener());
                    }
                    if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "ll:info")) {
                        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "ll:info");
                    }

                    if (plugin.getServer().getMessenger().isIncomingChannelRegistered(plugin, "ll:info") && plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "ll:info")) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        }

        out.Alert("LockLogin will search for updates and will be updated automatically when no players are in server", WarningLevel.WARNING);
        startVersionCheckBungee();
    }

    /**
     * The onDisable actions the
     * plugin will perform
     */
    public final void disable() {
        out.Message("--------------------");
        out.Message(" ");
        out.Message("&bDisabling {0}");
        out.Message("&aChecking files...");
        setupFiles(false);
        out.Message(" ");
        out.Message("--------------------");
        unsetPlayers();
        if (!new ConfigGetter().isBungeeCord()) {
            Bucket.terminateMySQL();
        }
        if (plugin.getServer().getMessenger().isIncomingChannelRegistered(plugin, "ll:info")) {
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "ll:info");
        }
        PinInventory.clearVerifiedList();
    }

    /**
     * Setup the plugin files
     */
    private void setupFiles(boolean onEnable) {
        FileCreator config = new FileCreator("config.yml", "config_spigot.yml");

        config.createFile();
        config.setDefaults();

        String random = StringUtils.randomString(5);

        FileManager manager = new FileManager("config.yml");

        if (manager.isEmpty("ServerName")) {
            manager.set("ServerName", random);
        }

        ConfigGetter cfg = new ConfigGetter();

        FileCreator messages = new FileCreator("messages_en.yml", true);
        if (cfg.langValid()) {
            if (cfg.isEnglish()) {
                messages.createFile();
                messages.setDefaults();
            } else {
                if (cfg.isSpanish()) {
                    messages = new FileCreator("messages_es.yml", true);
                    messages.createFile();
                    messages.setDefaults();
                } else {
                    if (cfg.isSimplifiedChinese()) {
                        messages = new FileCreator("messages_zh.yml", true);
                        messages.createFile();
                        messages.setDefaults();
                    } else {
                        if (cfg.isItalian()) {
                            messages = new FileCreator("messages_it.yml", true);
                            messages.createFile();
                            messages.setDefaults();
                        } else {
                                if (cfg.isPolish()) {
                                    messages = new FileCreator("messages_pl.yml", true);
                                    messages.createFile();
                                    messages.setDefaults();
                                } else {
                                    if (cfg.isFrench()) {
                                        messages = new FileCreator("messages_fr.yml", true);
                                        messages.createFile();
                                        messages.setDefaults();
                                    }
                                }
                            }
                        }
                }
            }
        } else {
            out.Alert("Invalid lang &f( " + cfg.getLang() + " &f) &c.Valid langs are: &ben_EN&7, &ees_ES&7, &ezh_CN&7, &eit_IT&7, &epl_PL&7, &efr_FR", WarningLevel.ERROR);
            FileManager configManager = new FileManager("config.yml");
            configManager.set("Lang", "en_EN");
            messages = new FileCreator("messages_en.yml", true);
            messages.createFile();
            messages.setDefaults();
        }

        if (cfg.FileSysValid()) {
            if (cfg.isMySQL()) {
                if (onEnable) {
                    setupMySQL();
                }
            }
        } else {
            FileManager configManager = new FileManager("config.yml");
            configManager.set("AccountSys", "File");
        }

        FileCreator spawn = new FileCreator("spawn.yml", true);
        spawn.createFile();
        spawn.setDefaults();
        //spawn.saveFile();

        FileCreator allowedCMDs = new FileCreator("allowed.yml", true);
        allowedCMDs.createFile();
        allowedCMDs.setDefaults();
        //allowedCMDs.saveFile();

        FileManager allowed = new FileManager("allowed.yml");

        AllowedCommands commands = new AllowedCommands();
        commands.addAll(allowed.getList("AllowedCommands"));
    }

    /**
     * Setup mysql connection if the config
     * defines to use mysql
     */
    private void setupMySQL() {
        if (!new ConfigGetter().isBungeeCord()) {
            FileCreator mysql = new FileCreator("mysql.yml", true);
            mysql.createFile();
            mysql.setDefaults();

            MySQLData SQLData = new MySQLData();

            Bucket bucket = new Bucket(
                    SQLData.getHost(),
                    SQLData.getDatabase(),
                    SQLData.getTable(),
                    SQLData.getUser(),
                    SQLData.getPassword(),
                    SQLData.getPort(),
                    SQLData.useSSL());

            bucket.setOptions(SQLData.getMaxConnections(), SQLData.getMinConnections(), SQLData.getTimeOut(), SQLData.getLifeTime());

            bucket.prepareTables();
        }
    }

    /**
     * Register the plugin commands
     */
    private void registerCommands() {
        if (!new ConfigGetter().isBungeeCord()) {
            plugin.getCommand("register").setExecutor(new RegisterCommand());
            plugin.getCommand("login").setExecutor(new LoginCommand());
            plugin.getCommand("unlog").setExecutor(new UnlogCommand());
            plugin.getCommand("2fa").setExecutor(new GoogleAuthCommand());
            plugin.getCommand("resetfa").setExecutor(new GoogleAuthResetCommand());
            plugin.getCommand("resetlastloc").setExecutor(new GoogleAuthCommand());
            plugin.getCommand("change").setExecutor(new ChangePassword());
            plugin.getCommand("delaccount").setExecutor(new DelAccountCommand());
            plugin.getCommand("playerinf").setExecutor(new CheckPlayerCommand());
            plugin.getCommand("lookup").setExecutor(new LookUpCommand());
            plugin.getCommand("pin").setExecutor(new SetPinCommand());
            plugin.getCommand("resetpin").setExecutor(new ResetPinCommand());
        } else {
            out.Alert("BungeeCord mode detected, the plugin will register only the setspawn and reset locations command", WarningLevel.WARNING);
        }
        plugin.getCommand("resetlastloc").setExecutor(new ResetLastLoc());
        plugin.getCommand("setloginspawn").setExecutor(new SetSpawnCommand());
        plugin.getCommand("locklogin").setExecutor(new LockLoginCommand());
    }

    /**
     * Register the plugin events
     */
    private void registerEvents() {
        ConfigGetter cfg = new ConfigGetter();
        if (!cfg.isBungeeCord()) {
            plugin.getServer().getPluginManager().registerEvents(new JoinRelated(), plugin);
            plugin.getServer().getPluginManager().registerEvents(new PlayerKick(), plugin);
        } else {
            plugin.getServer().getPluginManager().registerEvents(new BungeeJoinEventHandler(), plugin);
            out.Alert("BungeeCord mode detected, the plugin will register only the blocked events for non-logged players", WarningLevel.WARNING);
        }
        plugin.getServer().getPluginManager().registerEvents(new PlayerLeave(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockedEvents(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventHandler(), plugin);
    }

    /**
     * Do the plugin version check
     */
    private void doVersionCheck() {
        out.Alert("Checking for updates at least once since you don't want to check constantly for updates", WarningLevel.WARNING);
        if (CheckerSpigot.isOutdated()) {
            out.Message("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + LockLoginVersion.version + " &f)");
            if (new ConfigGetter().UpdateSelf()) {
                String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJarName());

                if (!updatedLockLogin.exists()) {
                    try {
                        DownloadLatest latest = new DownloadLatest();
                        latest.download();

                        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                            out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                            new LockLoginSpigotManager().applyUpdate();
                        } else {
                            out.Alert("LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                        }
                    } catch (Throwable e) {
                        ml.karmaconfigs.LockLogin.Logs.Logger.log(Platform.ANY, "ERROR WHILE STARTING LATEST LOCKLOGIN VERSION DOWNLOADER" + ": " + e.fillInStackTrace(), e);
                    }
                } else {
                    if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                        out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                        new LockLoginSpigotManager().applyUpdate();
                    } else {
                        out.Alert("LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                    }
                }
            } else {
                CheckerSpigot.sendChangeLog();
                out.Message("&3You can download latest version from &dhttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
            }
        } else {
            out.Message("&eLockLogin &7>> &aYou are running the latest version of LockLogin &f( &3" + LockLoginVersion.version + " &f)");
        }
    }

    /**
     * Start the version checker for spigot
     */
    private void startVersionChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            out.Alert("Checking for updates...", WarningLevel.WARNING);
            if (CheckerSpigot.isOutdated()) {
                out.Message("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + LockLoginVersion.version + " &f)");
                if (new ConfigGetter().UpdateSelf()) {
                    String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                    File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                    File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJarName());

                    if (!updatedLockLogin.exists()) {
                        try {
                            DownloadLatest latest = new DownloadLatest();
                            latest.download();

                            if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                                out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                                new LockLoginSpigotManager().applyUpdate();
                            } else {
                                out.Alert("LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                            }
                        } catch (Throwable e) {
                            ml.karmaconfigs.LockLogin.Logs.Logger.log(Platform.ANY, "ERROR WHILE STARTING LATEST LOCKLOGIN VERSION DOWNLOADER" + ": " + e.fillInStackTrace(), e);
                        }
                    } else {
                        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                            out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                            new LockLoginSpigotManager().applyUpdate();
                        } else {
                            out.Alert("LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                        }
                    }
                } else {
                    CheckerSpigot.sendChangeLog();
                    out.Message("&3You can download latest version from &dhttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
                }
            } else {
                out.Message("&eLockLogin &7>> &aYou are running the latest version of LockLogin &f( &3" + LockLoginVersion.version + " &f)");
            }
        }, 0, 20 * new ConfigGetter().UpdateCheck());
    }

    /**
     * Start the version check for BungeeCord
     */
    private void startVersionCheckBungee() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (CheckerSpigot.isOutdated()) {
                String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJarName());

                if (!updatedLockLogin.exists()) {
                    try {
                        DownloadLatest latest = new DownloadLatest();
                        latest.download();

                        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                            out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                            new LockLoginSpigotManager().applyUpdate();
                        } else {
                            out.Alert("LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                        }
                    } catch (Throwable e) {
                        ml.karmaconfigs.LockLogin.Logs.Logger.log(Platform.ANY, "ERROR WHILE STARTING LATEST LOCKLOGIN VERSION DOWNLOADER" + ": " + e.fillInStackTrace(), e);
                    }
                } else {
                    if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                        out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                        new LockLoginSpigotManager().applyUpdate();
                    } else {
                        out.Alert("LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                    }
                }
            }
        }, 0, 20 * 60);
    }

    /**
     * Start the alert checker for spigot
     */
    private void startAlertChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (LockLoginAlerts.AlertAvailable()) {
                LockLoginAlerts.sendAlert(Platform.SPIGOT);
            }
        }, 0, 20 * 30);
    }

    /**
     * Setup the players if they are connected while
     * the plugin is loading
     */
    private void setupPlayers() {
        ConfigGetter config = new ConfigGetter();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            User user = new User(player);

            if (!player.hasMetadata("LockLoginUser")) {
                player.setMetadata("LockLoginUser", new FixedMetadataValue(plugin, player.getUniqueId()));
            }

            if (user.isRegistered()) {
                new StartCheck(player, CheckType.LOGIN);
                if (config.LoginBlind()) {
                    user.saveCurrentEffects();
                    user.applyBlindEffect(config.LoginNausea());
                }
            } else {
                new StartCheck(player, CheckType.REGISTER);
                if (config.RegisterBlind()) {
                    user.saveCurrentEffects();
                    user.applyBlindEffect(config.RegisterNausea());
                }
            }

            IpData data = new IpData(player.getAddress().getAddress());

            if (new ConfigGetter().AccountsPerIp() != 0) {
                if (data.getConnections() + 1 > new ConfigGetter().AccountsPerIp()) {
                    user.Kick(new MessageGetter().MaxIp());
                } else {
                    data.addIP();
                }
            }
        }
    }

    /**
     * Restore player profile stats
     */
    private void unsetPlayers() {
        ConfigGetter config = new ConfigGetter();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            User user = new User(player);

            if (player.hasMetadata("LockLoginUser")) {
                player.removeMetadata("LockLoginUser", plugin);
            }

            if (!user.isLogged()) {
                if (user.isRegistered()) {
                    if (config.LoginBlind()) {
                        user.removeBlindEffect(config.LoginNausea());
                    }
                } else {
                    if (config.RegisterBlind()) {
                        user.removeBlindEffect(config.RegisterNausea());
                    }
                }
            }
        }
    }

    /**
     * Register the metrics
     */
    private void registerMetrics() {
        SpigotMetrics metrics = new SpigotMetrics(plugin);

        metrics.addCustomChart(new SpigotMetrics.SimplePie("used_locale", () -> String.valueOf(new ConfigGetter().getLang())));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("country_protect", () -> "Removed in 3.0.2"));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("clear_chat", () -> String.valueOf(new ConfigGetter().ClearChat())
                .replace("true", "Clear chat")
                .replace("false", "Don't clear chat")));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("file_system", () -> new ConfigGetter().FileSys()
                .replace("file", "File")
                .replace("mysql", "MySQL")));
    }
}
