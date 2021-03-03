package ml.karmaconfigs.lockloginsystem.bungeecord.utils;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.karmayaml.FileCopy;
import ml.karmaconfigs.api.bungee.karmayaml.YamlReloader;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
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
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.alerts.LockLoginAlerts;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.shared.metrics.BungeeMetrics;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.GetLatestVersion;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
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
        if (new ConfigGetter().checkUpdates()) {
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
            if (configuration.getString("serverName", "").isEmpty()) {
                configuration.set("serverName", random);
            }
        }

        ConfigGetter cfg = new ConfigGetter();
        if (cfg.getMainLobby().equals(cfg.getAuthLobby()) || cfg.getMainLobby().equals(cfg.getFallBackAuth())) {
            Console.send(plugin, "Your lobby and auth lobby are the same, if you don't have auth lobby," +
                    " LockLogin will detect it automatically and use the player server as auth server " +
                    "( REMEMBER TO INSTALL LOCKLOGIN IN EACH SERVER SO THIS WILL HAPPEN )", Level.WARNING);
        }

        File msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
        File old_msg = new File(plugin.getDataFolder(), "messages_en.yml");
        switch (cfg.getLang()) {
            case ENGLISH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_en.yml");
                break;
            case SPANISH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_es.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_es.yml");
                break;
            case SIMPLIFIED_CHINESE:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_zh.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_zh.yml");
                break;
            case ITALIAN:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_it.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_it.yml");
                break;
            case POLISH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_pl.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_pl.yml");
                break;
            case FRENCH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_fr.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_fr.yml");
                break;
            case CZECH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_cz.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_cz.yml");
                break;
            case UNKNOWN:
                Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                old_msg = new File(plugin.getDataFolder(), "messages_en.yml");
                break;
        }

        if (old_msg.exists()) {
            try {
                Files.move(old_msg.toPath(), msg_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable ignored) {
            }
        }

        if (!msg_file.exists()) {
            FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName());

            if (creator.copy(msg_file)) {
                logger.scheduleLog(Level.INFO, "Created lang file " + msg_file.getName());
            }
        }

        if (cfg.accountSysValid()) {
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
            commands.add("recovery");
            commands.add("lockloginmailer:recovery");
        } catch (Throwable ignored) {
        }

        try {
            File mail = new File(plugin.getDataFolder(), "mail.yml");
            if (mail.exists()) {
                //Prepare plugin to migrate from LockLogin email system to LockLoginMailer module...
                Configuration mailer = YamlConfiguration.getProvider(YamlConfiguration.class).load(mail);

                String email = mailer.getString("Email", "");
                assert email != null;

                if (!email.replaceAll("\\s", "").isEmpty()) {
                    Console.send(plugin, "Detected valid email configuration, migrating from LockLogin email system to LockLogin mailer module", Level.INFO);

                    String password = mailer.getString("Password", "");
                    boolean login_email = mailer.getBoolean("LoginEmail", true);

                    String smtp_host = mailer.getString("SMTP.Host", "smtp.gmail.com");
                    int smtp_port = mailer.getInt("SMTP.Port", 587);
                    boolean use_tls = mailer.getBoolean("SMTP.TLS", true);

                    String recovery_subject = Objects.requireNonNull(mailer.getString("Subjects.PasswordRecovery", "[{server}] Recover your account {player}")).replace("{server}", Objects.requireNonNull(configuration.getString("ServerName", StringUtils.randomString(8))));
                    String confirm_subject = Objects.requireNonNull(mailer.getString("Subjects.LoginLog", "[{server}] New login in your account: {player}")).replace("{server}", Objects.requireNonNull(configuration.getString("ServerName", StringUtils.randomString(8))));

                    File new_config = new File(plugin.getDataFolder().getParentFile() + File.separator + "LockLoginMailer", "config.yml");
                    if (!new_config.exists()) {
                        if (!new_config.getParentFile().exists())
                            Files.createDirectories(new_config.getParentFile().toPath());

                        Files.createFile(new_config.toPath());
                    }

                    FileCopy copy = new FileCopy(plugin, "auto-generated/mail.yml");
                    copy.copy(new_config);

                    Configuration new_cfg = YamlConfiguration.getProvider(YamlConfiguration.class).load(new_config);
                    new_cfg.set("Email", email);
                    new_cfg.set("Password", password);
                    new_cfg.set("ConfirmEmails", true);
                    new_cfg.set("VerifyIpChanges", login_email);
                    new_cfg.set("SMTP.Host", smtp_host);
                    new_cfg.set("SMTP.Port", smtp_port);
                    new_cfg.set("SMTP.TLS", use_tls);
                    new_cfg.set("Subjects.PasswordRecovery", recovery_subject);
                    new_cfg.set("Subjects.LoginLog", confirm_subject);

                    YamlConfiguration.getProvider(YamlConfiguration.class).save(new_cfg, new_config);

                    YamlReloader reloader = new YamlReloader(plugin, new_config, "auto-generated/mail.yml");
                    reloader.reloadAndCopy();

                    Files.delete(mail.toPath());

                    Console.send(plugin, "Downloading LockLoginMailer...", Level.INFO);

                    File destJar = new File(plugin.getDataFolder().getParentFile(), "LockLoginMailer.jar");
                    try {
                        URL download_url = new URL("https://karmaconfigs.github.io/updates/LockLogin/modules/mailer/LockLoginMailer.jar");

                        URLConnection connection = download_url.openConnection();
                        connection.connect();

                        InputStream input = new BufferedInputStream(download_url.openStream(), 1024);
                        OutputStream output = new FileOutputStream(destJar);

                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(dataBuffer, 0, 1024)) != -1) {
                            output.write(dataBuffer, 0, bytesRead);
                        }

                        output.flush();
                        output.close();
                        input.close();
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    } finally {
                        LockLoginBungeeManager manager = new LockLoginBungeeManager();
                        manager.loadPlugin(destJar);
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
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
        plugin.getProxy().getPluginManager().registerCommand(plugin, new MigrateCommand());
        plugin.getProxy().getPluginManager().registerCommand(plugin, new ModuleListCommand());
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

        bucket.prepareTables(SQLData.ignoredColumns());

        Utils utils = new Utils();
        utils.checkTables();
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
            GetLatestVersion latest = new GetLatestVersion();

            int last_version_id = latest.getId();
            int curr_version_id = LockLoginBungee.versionID;

            if (last_version_id > curr_version_id) {
                ConfigGetter cfg = new ConfigGetter();

                switch (cfg.getUpdateChannel()) {
                    case SNAPSHOT:
                        switch (latest.getChannel()) {
                            case SNAPSHOT:
                                snapshot(latest);
                                break;
                            case RELEASE:
                                releaseUpdate(latest);
                                break;
                        }
                        break;
                    case RC:
                        switch (latest.getChannel()) {
                            case RC:
                                releaseCandidate(latest);
                                break;
                            case RELEASE:
                                releaseUpdate(latest);
                                break;
                        }
                    case RELEASE:
                        if (latest.getChannel().equals(VersionChannel.RELEASE))
                            releaseUpdate(latest);
                        break;
                }
            }
        });
    }

    /**
     * Start the version checker for bungee
     */
    private void startVersionChecker() {
        plugin.getProxy().getScheduler().schedule(plugin, this::doVersionCheck, new ConfigGetter().checkInterval(), TimeUnit.MINUTES);
    }

    /**
     * Start the alert checker for bungeecord
     */
    private void startAlertChecker() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (LockLoginAlerts.available()) {
                LockLoginAlerts.sendAlert();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * Register the metrics
     */
    private void registerMetrics() {
        BungeeMetrics metrics = new BungeeMetrics(plugin, 6512);

        metrics.addCustomChart(new BungeeMetrics.SimplePie("used_locale", () -> new ConfigGetter().getLang().friendlyName()));
        metrics.addCustomChart(new BungeeMetrics.SimplePie("country_protect", () -> "Removed in 3.0.2"));
        metrics.addCustomChart(new BungeeMetrics.SimplePie("clear_chat", () -> String.valueOf(new ConfigGetter().clearChat())
                .replace("true", "Clear chat")
                .replace("false", "Don't clear chat")));
        metrics.addCustomChart(new BungeeMetrics.SimplePie("file_system", () -> new ConfigGetter().accountSystem()
                .replace("file", "File")
                .replace("mysql", "MySQL")));
    }

    /**
     * Hook all the players again
     */
    private void reHookPlayers() {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

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
                if (config.accountsPerIP() != 0) {
                    IpData data = new IpData(temp_module, User.external.getIp(player.getSocketAddress()));
                    data.fetch(Platform.BUNGEE);

                    if (data.getConnections() + 1 > config.accountsPerIP()) {
                        user.Kick("&eLockLogin\n\n" + messages.MaxIp());
                    }
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while trying to inject LockLogin temp accessor API module");
                Console.send(plugin, "An error occurred while trying to load LockLogin temp accessor API module, check logs for more info", Level.GRAVE);
            }

            user.checkServer();
            if (config.clearChat()) {
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
                IpData data = new IpData(temp_module, User.external.getIp(player.getSocketAddress()));
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

    /**
     * Send snapshot alert
     *
     * @param latest the latest version instance
     */
    private void snapshot(final GetLatestVersion latest) {
        Console.send("&eLockLogin &7>> &aNew version snapshot available for LockLogin &f( &3" + latest.getVersion() + " &f)");
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

        if (updatedLockLogin.exists()) {
            VersionChannel dest_channel = FileInfo.getChannel(updatedLockLogin);
            VersionChannel current_channel = FileInfo.getChannel(new File(jar));

            String dest_version = FileInfo.getJarVersion(updatedLockLogin);
            String curr_version = FileInfo.getJarVersion(new File(jar));

            if (!dest_version.equals(curr_version)) {
                try {
                    Files.delete(updatedLockLogin.toPath());
                } catch (Throwable ignored) {
                }
            } else {
                if (!dest_channel.equals(current_channel)) {
                    try {
                        Files.delete(updatedLockLogin.toPath());
                    } catch (Throwable ignored) {}
                }
            }
        }

        InterfaceUtils utils = new InterfaceUtils();
        if (!updatedLockLogin.exists() || !utils.isReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(() -> {
                        utils.setReadyToUpdate(true);
                        Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                    });
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
            }
        } else {
            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", Level.INFO);
        }

        Console.send("&3To use this new version, you must go to /plugins/update and copy {0} to /plugins folder, replacing current {1}", LockLoginBungee.jar, LockLoginBungee.jar);
        Console.send(plugin, "PLEASE NOTE THIS IS A SNAPSHOT CONTAINING EXPERIMENTAL FEATURES THAT MAY BE REMOVED OR BREAK PLUGIN FUNCTIONALITY", Level.WARNING);

        if (!last_changelog.equals(latest.getChangeLog()) || checks >= 3) {
            last_changelog = latest.getChangeLog();
            Console.send(last_changelog);
            checks = 0;
        } else {
            checks++;
        }
    }

    /**
     * Send rc alert
     *
     * @param latest the latest version instance
     */
    private void releaseCandidate(final GetLatestVersion latest) {
        Console.send("&eLockLogin &7>> &aNew version candidate available for LockLogin &f( &3" + latest.getVersion() + " &f)");
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

        if (updatedLockLogin.exists()) {
            VersionChannel dest_channel = FileInfo.getChannel(updatedLockLogin);
            VersionChannel current_channel = FileInfo.getChannel(new File(jar));

            String dest_version = FileInfo.getJarVersion(updatedLockLogin);
            String curr_version = FileInfo.getJarVersion(new File(jar));

            if (!dest_version.equals(curr_version)) {
                try {
                    Files.delete(updatedLockLogin.toPath());
                } catch (Throwable ignored) {
                }
            } else {
                if (!dest_channel.equals(current_channel)) {
                    try {
                        Files.delete(updatedLockLogin.toPath());
                    } catch (Throwable ignored) {}
                }
            }
        }

        InterfaceUtils utils = new InterfaceUtils();
        if (!updatedLockLogin.exists() || !utils.isReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(() -> {
                        utils.setReadyToUpdate(true);
                        Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                    });
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
            }
        } else {
            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", Level.INFO);
        }

        Console.send("&3To use this new version, you must go to /plugins/update and copy {0} to /plugins folder, replacing current {1}", LockLoginBungee.jar, LockLoginBungee.jar);

        if (!last_changelog.equals(latest.getChangeLog()) || checks >= 3) {
            last_changelog = latest.getChangeLog();
            Console.send(last_changelog);
            checks = 0;
        } else {
            checks++;
        }
    }

    /**
     * Send release alert
     *
     * @param latest the latest version instance
     */
    private void releaseUpdate(final GetLatestVersion latest) {
        Console.send("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + latest.getVersion() + " &f)");
        String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

        File pluginsFolder = new File(dir.replace("/LockLogin", ""));
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

        if (updatedLockLogin.exists()) {
            VersionChannel dest_channel = FileInfo.getChannel(updatedLockLogin);
            VersionChannel current_channel = FileInfo.getChannel(new File(jar));

            String dest_version = FileInfo.getJarVersion(updatedLockLogin);
            String curr_version = FileInfo.getJarVersion(new File(jar));

            if (!dest_version.equals(curr_version)) {
                try {
                    Files.delete(updatedLockLogin.toPath());
                } catch (Throwable ignored) {
                }
            } else {
                if (!dest_channel.equals(current_channel)) {
                    try {
                        Files.delete(updatedLockLogin.toPath());
                    } catch (Throwable ignored) {}
                }
            }
        }

        InterfaceUtils utils = new InterfaceUtils();
        if (!updatedLockLogin.exists() || !utils.isReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(() -> {
                        utils.setReadyToUpdate(true);
                        Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                    });
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
            }
        } else {
            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /applyUpdates or restart your proxy (Recommended)", Level.INFO);
        }

        Console.send("&3Otherwise, you can download latest version from &dhttps://www.spigotmc.org/resources/gsa-locklogin.75156/");

        if (!last_changelog.equals(latest.getChangeLog()) || checks >= 3) {
            last_changelog = latest.getChangeLog();
            Console.send(last_changelog);
            checks = 0;
        } else {
            checks++;
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
        return "This module is used to access an API feature when the plugin starts";
    }

    @Override
    public @NotNull String author_url() {
        return "https://karmaconfigs.ml/";
    }
}
