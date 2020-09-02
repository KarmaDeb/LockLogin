package ml.karmaconfigs.LockLogin.BungeeCord.Utils;

import ml.karmaconfigs.LockLogin.Alerts.LockLoginAlerts;
import ml.karmaconfigs.LockLogin.BungeeCord.Commands.*;
import ml.karmaconfigs.LockLogin.BungeeCord.Events.*;
import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Main;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.DataFiles.AllowedCommands;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.DataFiles.MySQLData;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.ConfigGetter;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.MessageGetter;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.PluginManager.LockLoginBungeeManager;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Metrics.BungeeMetrics;
import ml.karmaconfigs.LockLogin.MySQL.Bucket;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Version.DownloadLatest;
import ml.karmaconfigs.LockLogin.Version.LockLoginVersion;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class PluginManagerBungee implements LockLoginBungee {

    /**
     * The onEnable actions the
     * plugin will perform
     */
    public final void enable() {
        Bucket.terminateMySQL();

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
        plugin.getProxy().registerChannel("ll:info");
        if (new ConfigGetter().CheckForUpdates()) {
            startVersionChecker();
        } else {
            doVersionCheck();
        }
        startAlertChecker();
        registerMetrics();
        reHookPlayers();
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
        plugin.getProxy().unregisterChannel("ll:info");
        Bucket.terminateMySQL();
        unHookPlayers();
    }

    /**
     * Setup the plugin files
     */
    private void setupFiles(boolean onEnable) {
        FileCreator config = new FileCreator("config.yml", true);
        config.createFile();
        config.setDefaults();
        config.saveFile();

        String random = StringUtils.randomString(5);

        FileManager manager = new FileManager("config.yml");

        if (manager.isEmpty("ServerName")) {
            manager.set("ServerName", random);
        }

        ConfigGetter cfg = new ConfigGetter();

        if (cfg.MainLobby().equals(cfg.AuthLobby()) || cfg.MainLobby().equals(cfg.FallBackAuth())) {
            out.Alert("You mustn't set the lobby as auth lobby, if you don't have auth lobby," +
                    " LockLogin will detect it automatically and use the player server as auth server " +
                    "( REMEMBER TO INSTALL LOCKLOGIN IN EACH SERVER SO THIS WILL HAPPEN )", WarningLevel.ERROR);
        }

        FileCreator messages = new FileCreator("messages_en.yml", true);
        if (cfg.langValid()) {
            if (cfg.isEnglish()) {
                messages.createFile();
                messages.setDefaults();
                messages.saveFile();
            } else {
                if (cfg.isSpanish()) {
                    messages = new FileCreator("messages_es.yml", true);
                    messages.createFile();
                    messages.setDefaults();
                    messages.saveFile();
                } else {
                    if (cfg.isSimplifiedChinese()) {
                        messages = new FileCreator("messages_zh.yml", true);
                        messages.createFile();
                        messages.setDefaults();
                        messages.saveFile();
                    } else {
                        if (cfg.isItalian()) {
                            messages = new FileCreator("messages_it.yml", true);
                            messages.createFile();
                            messages.setDefaults();
                            messages.saveFile();
                        } else {
                            if (cfg.isPolish()) {
                                messages = new FileCreator("messages_pl.yml", true);
                                messages.createFile();
                                messages.setDefaults();
                                messages.saveFile();
                            } else {
                                if (cfg.isFrench()) {
                                    messages = new FileCreator("messages_fr.yml", true);
                                    messages.createFile();
                                    messages.setDefaults();
                                    messages.saveFile();
                                }
                            }
                        }
                    }
                }
            }
        } else {
            out.Alert("Invalid lang &f( " + cfg.MSGLang() + " &f) &c.Valid langs are: &ben_EN&7, &ees_ES&7, &ezh_CN&7, &eit_IT&7, &epl_PL&7, &efr_FR", WarningLevel.ERROR);
            FileManager configManager = new FileManager("config.yml");
            configManager.set("Lang", "en_EN");
            messages = new FileCreator("messages_en.yml", true);
            messages.createFile();
            messages.setDefaults();
            messages.saveFile();
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
        spawn.saveFile();

        FileCreator allowedCMDs = new FileCreator("allowed.yml", true);
        allowedCMDs.createFile();
        allowedCMDs.setDefaults();
        allowedCMDs.saveFile();

        FileManager allowed = new FileManager("allowed.yml");

        AllowedCommands commands = new AllowedCommands();
        commands.addAll(allowed.getList("AllowedCommands"));
    }

    /**
     * Register the plugin commands
     */
    private void registerCommands() {
        plugin.getProxy().getPluginManager().registerCommand(plugin, new RegisterCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new LoginCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new UnlogCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new GoogleAuthResetCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new GoogleAuthCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new ChangePassword());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new DelAccountCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new MigrateCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CheckPlayerCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new LookUpCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new ApplyUpdateCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new SetPinCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new ResetPinCommand());
    }

    /**
     * Setup mysql connection if the config
     * defines to use mysql
     */
    private void setupMySQL() {
        FileCreator mysql = new FileCreator("mysql.yml", true);
        mysql.createFile();
        mysql.setDefaults();
        mysql.saveFile();

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

    /**
     * Register the plugin events
     */
    private void registerEvents() {
        plugin.getProxy().getPluginManager().registerListener(plugin, new JoinRelated());
        plugin.getProxy().getPluginManager().registerListener(plugin, new PlayerLeave());
        plugin.getProxy().getPluginManager().registerListener(plugin, new ChatRelatedEvents());
        plugin.getProxy().getPluginManager().registerListener(plugin, new PlayerKick());
        plugin.getProxy().getPluginManager().registerListener(plugin, new ServerMessage());
    }

    /**
     * Do the plugin version check
     */
    public final void doVersionCheck() {
        out.Alert("Checking for updates at least once since you don't want to check constantly for updates", WarningLevel.WARNING);
        if (CheckerBungee.isOutdated()) {
            out.Message("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + LockLoginVersion.version + " &f)");
            if (new ConfigGetter().UpdateSelf()) {
                String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.getJarName());

                if (!updatedLockLogin.exists()) {
                    try {
                        DownloadLatest latest = new DownloadLatest();
                        latest.download();
                    } catch (Throwable e) {
                        Logger.log(Platform.ANY, "ERROR WHILE STARTING LATEST LOCKLOGIN VERSION DOWNLOADER" + ": " + e.fillInStackTrace(), e);
                    }
                } else {
                    if (plugin.getProxy().getPlayers().isEmpty()) {
                        Main.updatePending = true;
                        out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                        new LockLoginBungeeManager().applyUpdate();
                    } else {
                        out.Alert("LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                    }
                }
            } else {
                CheckerBungee.sendChangeLog();
                out.Message("&3You can download latest version from &dhttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
            }
        } else {
            out.Message("&eLockLogin &7>> &aYou are running the latest version of LockLogin &f( &3" + LockLoginVersion.version + " &f)");
        }
    }

    /**
     * Start the version checker for bungee
     */
    private void startVersionChecker() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            out.Alert("Checking for updates...", WarningLevel.WARNING);
            if (CheckerBungee.isOutdated()) {
                out.Message("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + LockLoginVersion.version + " &f)");
                if (new ConfigGetter().UpdateSelf()) {
                    String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                    File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                    File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.getJarName());

                    if (!updatedLockLogin.exists()) {
                        try {
                            DownloadLatest latest = new DownloadLatest();
                            latest.download();
                        } catch (Throwable e) {
                            Logger.log(Platform.ANY, "ERROR WHILE STARTING LATEST LOCKLOGIN VERSION DOWNLOADER" + ": " + e.fillInStackTrace(), e);
                        }
                    } else {
                        if (plugin.getProxy().getPlayers().isEmpty()) {
                            Main.updatePending = true;
                            out.Alert("LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", WarningLevel.WARNING);
                            new LockLoginBungeeManager().applyUpdate();
                        } else {
                            out.Alert("LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", WarningLevel.WARNING);
                        }
                    }
                } else {
                    CheckerBungee.sendChangeLog();
                    out.Message("&3You can download latest version from &dhttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
                }
            } else {
                out.Message("&eLockLogin &7>> &aYou are running the latest version of LockLogin &f( &3" + LockLoginVersion.version + " &f)");
            }
        }, new ConfigGetter().UpdateCheck(), TimeUnit.MINUTES);
    }

    /**
     * Start the alert checker for bungeecord
     */
    private void startAlertChecker() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (LockLoginAlerts.AlertAvailable()) {
                LockLoginAlerts.sendAlert(Platform.BUNGEE);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * Register the metrics
     */
    private void registerMetrics() {
        BungeeMetrics metrics = new BungeeMetrics(plugin, 6512);

        metrics.addCustomChart(new BungeeMetrics.SimplePie("used_locale", () -> new ConfigGetter().MSGLang()));
        metrics.addCustomChart(new BungeeMetrics.SimplePie("country_protect", () -> "Removed in 3.0.2"));
        metrics.addCustomChart(new BungeeMetrics.SimplePie("clear_chat", () -> String.valueOf(new ConfigGetter().ClearChat())
                .replace("true", "Clear chat")
                .replace("false", "Don't clear chat")));
        metrics.addCustomChart(new BungeeMetrics.SimplePie("file_system", () -> new ConfigGetter().FileSys()
                .replace("file", "File")
                .replace("mysql", "MySQL")));
    }

    /**
     * Hook all the players again
     */
    private void reHookPlayers() {
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            User user = new User(player);
            user.removeServerInfo();
            user.setLogStatus(false);

            ConfigGetter config = new ConfigGetter();
            MessageGetter messages = new MessageGetter();

            if (config.AccountsPerIp() != 0) {
                IpData data = new IpData(player.getAddress().getAddress());
                data.fetch(Platform.BUNGEE);

                if (data.getConnections() + 1 > config.AccountsPerIp()) {
                    user.Kick("&eLockLogin\n\n" + messages.MaxIp());
                }
            }

            user.checkServer();
            if (config.ClearChat()) {
                for (int i = 0; i < 150; i++) {
                    user.Message(" ");
                }
            }

            if (!user.isRegistered()) {
                new StartCheck(player, CheckType.REGISTER);
            } else {
                new StartCheck(player, CheckType.LOGIN);
            }

            dataSender.sendAccountStatus(player);
            dataSender.sendUUID(player.getUniqueId(), player.getServer());
        }
    }

    /**
     * Unhook the players and their data
     * from the plugin
     */
    private void unHookPlayers() {
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            IpData data = new IpData(player.getAddress().getAddress());
            data.delIP();

            User user = new User(player);
            user.removeServerInfo();
            user.setLogStatus(false);

            MessageGetter messages = new MessageGetter();

            user.Message(messages.Prefix() + "&cPlugin update, your account have been un-auth to avoid errors, wait to 5 seconds before trying to logging again...");
            dataSender.sendAccountStatus(player);
        }
    }
}
