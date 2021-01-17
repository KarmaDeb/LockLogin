package ml.karmaconfigs.lockloginsystem.bungeecord.utils;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.karmayaml.FileCopy;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.Main;
import ml.karmaconfigs.lockloginsystem.bungeecord.commands.*;
import ml.karmaconfigs.lockloginsystem.bungeecord.events.*;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.AllowedCommands;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.MySQLData;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.pluginmanager.LockLoginBungeeManager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.alerts.LockLoginAlerts;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.metrics.BungeeMetrics;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.LockLoginVersion;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

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

public final class PluginManagerBungee implements LockLoginBungee {

    private static String last_changelog = "";
    private static int checks = 0;

    /**
     * The onEnable actions the
     * plugin will perform
     */
    public final void enable() {
        Bucket.terminateMySQL();

        Console.send("--------------------");
        Console.send(" ");
        Console.send("&bEnabling {0} &bversion {1}", name, version);
        Console.send("&aInitializing files...");
        setupFiles();
        Console.send("&aRegistering commands....");
        registerCommands();
        Console.send("&aRegistering events...");
        registerEvents();
        Console.send(" ");
        Console.send("--------------------");
        plugin.getProxy().registerChannel("ll:info");
        if (new ConfigGetter().CheckForUpdates()) {
            doVersionCheck();
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
        Console.send("--------------------");
        Console.send(" ");
        Console.send("&bDisabling {0}", name);
        Console.send("&aChecking files...");
        Console.send(" ");
        Console.send("--------------------");
        plugin.getProxy().unregisterChannel("ll:info");
        Bucket.terminateMySQL();
        unHookPlayers();
    }

    /**
     * Setup the plugin files
     */
    private void setupFiles() {
        File config_file = new File(plugin.getDataFolder(), "config.yml");
        FileCopy config = new FileCopy(plugin, "configs/config.yml");
        config.copy(config_file);

        Configuration configuration = null;
        try {
            configuration = YamlConfiguration.getProvider(YamlConfiguration.class).load(config_file);
        } catch (Throwable ignored) {
        }

        if (configuration != null) {
            String random = StringUtils.randomString(5);
            if (configuration.getString("ServerName", "").isEmpty()) {
                configuration.set("ServerName", random);
            }
        }

        ConfigGetter cfg = new ConfigGetter();
        if (cfg.MainLobby().equals(cfg.AuthLobby()) || cfg.MainLobby().equals(cfg.FallBackAuth())) {
            Console.send(plugin, "Your lobby and auth lobby are the same, if you don't have auth lobby," +
                    " LockLogin will detect it automatically and use the player server as auth server " +
                    "( REMEMBER TO INSTALL LOCKLOGIN IN EACH SERVER SO THIS WILL HAPPEN )", Level.WARNING);
        }

        File msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
        switch (cfg.getLang()) {
            case ENGLISH:
                msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
                break;
            case SPANISH:
                msg_file = new File(plugin.getDataFolder(), "messages_es.yml");
                break;
            case SIMPLIFIED_CHINESE:
                msg_file = new File(plugin.getDataFolder(), "messages_zh.yml");
                break;
            case ITALIAN:
                msg_file = new File(plugin.getDataFolder(), "messages_it.yml");
                break;
            case POLISH:
                msg_file = new File(plugin.getDataFolder(), "messages_pl.yml");
                break;
            case FRENCH:
                msg_file = new File(plugin.getDataFolder(), "messages_fr.yml");
                break;
            case CZECH:
                msg_file = new File(plugin.getDataFolder(), "messages_cz.yml");
                break;
            case UNKNOWN:
                Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
                break;
        }

        if (!msg_file.exists()) {
            FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName());

            if (creator.copy(msg_file)) {
                logger.scheduleLog(Level.INFO, "Created lang file " + msg_file.getName());
            }
        }

        if (cfg.FileSysValid()) {
            if (cfg.isMySQL()) {
                setupMySQL();
            }
        } else {
            if (configuration != null) {
                configuration.set("AccountSys", "File");
            }
        }

        try {
            File allowed_file = new File(plugin.getDataFolder(), "allowed.yml");
            FileCopy allowedCMDs = new FileCopy(plugin, "auto-generated/allowed.yml");
            allowedCMDs.copy(allowed_file);
            Configuration allowed = YamlConfiguration.getProvider(YamlConfiguration.class).load(allowed_file);

            AllowedCommands commands = new AllowedCommands();
            commands.addAll(allowed.getStringList("AllowedCommands"));
        } catch (Throwable ignored) {
        }

        try {
            YamlConfiguration.getProvider(YamlConfiguration.class).save(configuration, config_file);
            config.copy(config_file);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while saving config file");
        }
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
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CheckPlayerCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new LookUpCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new ApplyUpdateCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new SetPinCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new ResetPinCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CheckUpdateCommand());
    }

    /**
     * Setup mysql connection if the config
     * defines to use mysql
     */
    private void setupMySQL() {
        File sql_file = new File(plugin.getDataFolder(), "mysql.yml");
        FileCopy mysql = new FileCopy(plugin, "auto-generated/mysql.yml");

        mysql.copy(sql_file);

        MySQLData SQLData = new MySQLData();

        Bucket bucket = new Bucket(
                SQLData.getHost(),
                SQLData.getDatabase(),
                SQLData.getTable(),
                SQLData.getUser(),
                SQLData.getPassword(),
                SQLData.getPort(),
                SQLData.useSSL(),
                SQLData.ignoreCertificates());

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
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            if (CheckerBungee.isOutdated()) {
                Console.send("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + LockLoginVersion.version + " &f)");
                if (new ConfigGetter().UpdateSelf()) {
                    String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                    File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                    File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

                    InterfaceUtils utils = new InterfaceUtils();
                    if (!updatedLockLogin.exists() || !utils.isReadyToUpdate()) {
                        try {
                            DownloadLatest latest = new DownloadLatest(new ConfigGetter().isFatJar());
                            if (!latest.isDownloading()) {
                                latest.download(() -> {
                                    utils.setReadyToUpdate(true);
                                    Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                                });
                            }
                        } catch (Throwable e) {
                            logger.scheduleLog(Level.GRAVE, e);
                            logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
                        }
                    } else {
                        if (plugin.getProxy().getPlayers().isEmpty()) {
                            Main.updatePending = true;
                            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", Level.INFO);
                            new LockLoginBungeeManager().applyUpdate(null);
                            utils.setReadyToUpdate(false);
                        } else {
                            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", Level.INFO);
                        }
                    }
                } else {
                    if (!last_changelog.equals(LockLoginVersion.changeLog) || checks >= 3) {
                        CheckerBungee.sendChangeLog();
                        last_changelog = LockLoginVersion.changeLog;
                        checks = 0;
                    } else {
                        checks++;
                    }
                    Console.send("&3You can download latest version from &dhttps://www.spigotmc.org/resources/gsa-locklogin.75156/");
                }
            }
        });
    }

    /**
     * Start the version checker for bungee
     */
    private void startVersionChecker() {
        plugin.getProxy().getScheduler().schedule(plugin, this::doVersionCheck, new ConfigGetter().UpdateCheck(), TimeUnit.MINUTES);
    }

    /**
     * Start the alert checker for bungeecord
     */
    private void startAlertChecker() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (LockLoginAlerts.AlertAvailable()) {
                LockLoginAlerts.sendAlert();
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
            user.setLogStatus(false);

            ConfigGetter config = new ConfigGetter();
            MessageGetter messages = new MessageGetter();

            TempModule temp_module = new TempModule();
            ModuleLoader bungee_module_loader = new ModuleLoader(temp_module);
            try {
                if (!ModuleLoader.manager.isLoaded(temp_module)) {
                    bungee_module_loader.inject();
                }
                if (config.AccountsPerIp() != 0) {
                    IpData data = new IpData(temp_module, player.getAddress().getAddress());
                    data.fetch(Platform.BUNGEE);

                    if (data.getConnections() + 1 > config.AccountsPerIp()) {
                        user.Kick("&eLockLogin\n\n" + messages.MaxIp());
                    }
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while trying to inject LockLogin temp accessor API module");
                Console.send(plugin, "An error occurred while trying to load LockLogin temp accessor API module, check logs for more info", Level.GRAVE);
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

            BungeeSender dataSender = new BungeeSender();

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

            TempModule temp_module = new TempModule();
            ModuleLoader bungee_module_loader = new ModuleLoader(temp_module);
            try {
                if (!ModuleLoader.manager.isLoaded(temp_module)) {
                    bungee_module_loader.inject();
                }
                IpData data = new IpData(temp_module, player.getAddress().getAddress());
                data.delIP();
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while trying to inject LockLogin temp accessor API module");
                Console.send(plugin, "An error occurred while trying to load LockLogin temp accessor API module, check logs for more info", Level.GRAVE);
            }

            User user = new User(player);
            user.setLogStatus(false);

            MessageGetter messages = new MessageGetter();

            user.Message(messages.Prefix() + "&cPlugin update, your account have been un-auth to avoid errors, wait to 5 seconds before trying to logging again...");
            BungeeSender dataSender = new BungeeSender();
            dataSender.sendAccountStatus(player);
        }
    }
}

class TempModule extends Module {

    @Override
    public @NotNull Plugin owner() {
        return LockLoginBungee.plugin;
    }

    @Override
    public @NotNull String name() {
        return "LockLogin temp accessor module";
    }

    @Override
    public @NotNull String version() {
        return "1.0.0";
    }

    @Override
    public @NotNull String author() {
        return "KarmaDev";
    }

    @Override
    public @NotNull String description() {
        return "This module is used to access an API feature";
    }

    @Override
    public @NotNull String author_url() {
        return "https://karmaconfigs.ml/";
    }
}
