package ml.karmaconfigs.lockloginsystem.spigot.utils;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.karmayaml.FileCopy;
import ml.karmaconfigs.api.spigot.karmayaml.YamlReloader;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.ConsoleFilter;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.alerts.LockLoginAlerts;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.passwords.InsecurePasswords;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.shared.metrics.SpigotMetrics;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.GetLatestVersion;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.commands.*;
import ml.karmaconfigs.lockloginsystem.spigot.events.*;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.AllowedCommands;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.MySQLData;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

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

    private static String last_changelog = "";
    private static int checks = 0;
    private static boolean ready_to_update = true;
    private static boolean is_bungeecord = false;

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
        if (!new ConfigGetter().isBungeeCord()) {
            if (new ConfigGetter().checkUpdates()) {
                startVersionChecker();
                Console.send(plugin, "LockLogin will search for updates and latest version will be downloaded automatically", Level.INFO);
            } else {
                doVersionCheck();
                Console.send(plugin, "YOU DISABLED LOCKLOGIN UPDATE CHECKER, WE HIGHLY RECOMMEND YOU TO ENABLE THIS. LOCKLOGIN WON'T UPDATE HIMSELF, JUST NOTIFY YOU ABOUT NEW UPDATES", Level.WARNING);
            }
            startAlertChecker();
            setupPlayers();
            registerMetrics();
        } else {
            is_bungeecord = true;
            startVersionChecker();

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
        unsetPlayers();
        if (!is_bungeecord) {
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
    public final void setupFiles() {
        File config_file = new File(plugin.getDataFolder(), "config.yml");
        FileCopy config = new FileCopy(plugin, "configs/config_spigot.yml");
        config.copy(config_file);

        FileManager cfgManager = new FileManager("config.yml");
        cfgManager.setInternal("configs/config_spigot.yml");

        File passwords_yml = new File(plugin.getDataFolder(), "passwords.yml");
        FileCopy passwords = new FileCopy(plugin, "auto-generated/passwords.yml");
        passwords.copy(passwords_yml);

        FileManager passwordsManager = new FileManager("passwords.yml");
        List<String> customPasswords = passwordsManager.getList("Insecure");
        InsecurePasswords insecure = new InsecurePasswords();
        insecure.addExtraPass(customPasswords);

        ConfigGetter cfg = new ConfigGetter();
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
            case RUSSIAN:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_ru.yml");
                //That's not even needed, but for just to make it stetically correct with other languages, i'll leave it like that
                old_msg = new File(plugin.getDataFolder(), "messages_ru.yml");
                break;
            case UNKNOWN:
                Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]&7, &eru_RU&b[Russian]", Level.WARNING);
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

        FileCopy msg = new FileCopy(plugin, "messages/" + msg_file.getName());
        if (msg.copy(msg_file)) {
            logger.scheduleLog(Level.INFO, "Checked lang file " + msg_file.getName());
        }

        File sql_file = new File(plugin.getDataFolder(), "mysql.yml");
        FileCopy mysql = new FileCopy(plugin, "auto-generated/mysql.yml");

        mysql.copy(sql_file);

        if (cfg.accountSysValid()) {
            if (cfg.isMySQL()) {
                setupMySQL();
            }
        }

        File spawn_file = new File(plugin.getDataFolder(), "spawn.yml");
        FileCopy spawn = new FileCopy(plugin, "auto-generated/spawn.yml");
        spawn.copy(spawn_file);

        File allowed_file = new File(plugin.getDataFolder(), "allowed.yml");
        FileCopy allowedCMDs = new FileCopy(plugin, "auto-generated/allowed.yml");
        allowedCMDs.copy(allowed_file);
        YamlConfiguration allowed = YamlConfiguration.loadConfiguration(allowed_file);

        AllowedCommands commands = new AllowedCommands();
        commands.addAll(allowed.getStringList("AllowedCommands"));
        commands.add("lockloginmailer:recovery");
        commands.add("recovery");

        if (!new ConfigGetter().isBungeeCord()) {
            try {
                File mail = new File(plugin.getDataFolder(), "mail.yml");
                if (mail.exists()) {
                    //Prepare plugin to migrate from LockLogin email system to LockLoginMailer module...
                    YamlConfiguration mailer = YamlConfiguration.loadConfiguration(mail);

                    String email = mailer.getString("Email", "");
                    assert email != null;

                    if (!email.replaceAll("\\s", "").isEmpty()) {
                        Console.send(plugin, "Detected valid email configuration, migrating from LockLogin email system to LockLogin mailer module", Level.INFO);

                        String password = mailer.getString("Password", "");
                        boolean login_email = mailer.getBoolean("LoginEmail", true);

                        String smtp_host = mailer.getString("SMTP.Host", "smtp.gmail.com");
                        int smtp_port = mailer.getInt("SMTP.Port", 587);
                        boolean use_tls = mailer.getBoolean("SMTP.TLS", true);

                        String recovery_subject = Objects.requireNonNull(mailer.getString("Subjects.PasswordRecovery", "[{server}] Recover your account {player}")).replace("{server}", Objects.requireNonNull(cfgManager.getString("ServerName")));
                        String confirm_subject = Objects.requireNonNull(mailer.getString("Subjects.LoginLog", "[{server}] New login in your account: {player}")).replace("{server}", Objects.requireNonNull(cfgManager.getString("ServerName")));

                        File new_config = new File(plugin.getDataFolder().getParentFile() + File.separator + "LockLoginMailer", "config.yml");
                        if (!new_config.exists()) {
                            if (!new_config.getParentFile().exists())
                                Files.createDirectories(new_config.getParentFile().toPath());

                            Files.createFile(new_config.toPath());
                        }

                        FileCopy copy = new FileCopy(plugin, "auto-generated/mail.yml");
                        copy.copy(new_config);

                        YamlConfiguration new_cfg = YamlConfiguration.loadConfiguration(new_config);
                        new_cfg.set("Email", email);
                        new_cfg.set("Password", password);
                        new_cfg.set("ConfirmEmails", true);
                        new_cfg.set("VerifyIpChanges", login_email);
                        new_cfg.set("SMTP.Host", smtp_host);
                        new_cfg.set("SMTP.Port", smtp_port);
                        new_cfg.set("SMTP.TLS", use_tls);
                        new_cfg.set("Subjects.PasswordRecovery", recovery_subject);
                        new_cfg.set("Subjects.LoginLog", confirm_subject);

                        new_cfg.save(new_config);

                        YamlReloader reloader = new YamlReloader(plugin, new_config, "auto-generated/mail.yml");
                        reloader.reloadAndCopy();
                        new_cfg.loadFromString(reloader.getYamlString());

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
                            plugin.getServer().getPluginManager().loadPlugin(destJar);
                        }
                    }

                    Files.delete(mail.toPath());
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Setup mysql connection if the config
     * defines to use mysql
     */
    private void setupMySQL() {
        if (!new ConfigGetter().isBungeeCord()) {
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

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Utils utils = new Utils();
                utils.checkTables();
            });
        }
    }

    /**
     * Register the plugin commands
     */
    private void registerCommands() {
        if (!new ConfigGetter().isBungeeCord()) {
            plugin.getCommand("captcha").setExecutor(new CaptchaCommand());
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
            Console.send(plugin, "BungeeCord mode detected, essential commands have been registered only", Level.INFO);
        }
        plugin.getCommand("resetlastloc").setExecutor(new ResetLastLoc());
        plugin.getCommand("setloginspawn").setExecutor(new SetSpawnCommand());
        plugin.getCommand("locklogin").setExecutor(new LockLoginCommand());
        plugin.getCommand("updateChecker").setExecutor(new CheckUpdateCommand());
    }

    /**
     * Register the plugin events
     */
    private void registerEvents() {
        ConfigGetter cfg = new ConfigGetter();
        if (!cfg.isBungeeCord()) {
            plugin.getServer().getPluginManager().registerEvents(new JoinRelated(), plugin);
            plugin.getServer().getPluginManager().registerEvents(new PreJoinRelated(), plugin);
            plugin.getServer().getPluginManager().registerEvents(new PlayerKick(), plugin);
        } else {
            plugin.getServer().getPluginManager().registerEvents(new BungeeJoinEventHandler(), plugin);
            Console.send(plugin, "BungeeCord mode detected, the plugin will register only the blocked events for non-logged players", Level.INFO);
        }
        plugin.getServer().getPluginManager().registerEvents(new PlayerLeave(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockedEvents(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryEventHandler(), plugin);
    }

    /**
     * Do the plugin version check
     */
    private void doVersionCheck() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            GetLatestVersion latest = new GetLatestVersion();

            int last_version_id = latest.getId();
            int curr_version_id = LockLoginSpigot.versionID;

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
     * Start the version checker for spigot
     */
    private void startVersionChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::doVersionCheck, 0, 20 * new ConfigGetter().checkInterval());
    }

    /**
     * Start the alert checker for spigot
     */
    private void startAlertChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (LockLoginAlerts.available()) {
                LockLoginAlerts.sendAlert();
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
                if (config.blindLogin()) {
                    user.saveCurrentEffects();
                    user.applyBlindEffect(config.nauseaLogin());
                }
            } else {
                new StartCheck(player, CheckType.REGISTER);
                if (config.blindRegister()) {
                    user.saveCurrentEffects();
                    user.applyBlindEffect(config.nauseaRegister());
                }
            }

            TempModule temp_module = new TempModule();
            ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
            try {
                if (!ModuleLoader.manager.isLoaded(temp_module)) {
                    spigot_module_loader.inject();
                }
                IpData data = new IpData(temp_module, player.getAddress().getAddress());

                if (new ConfigGetter().accountsPerIp() != 0) {
                    if (data.getConnections() + 1 > new ConfigGetter().accountsPerIp()) {
                        user.kick(new MessageGetter().maxIp());
                    } else {
                        data.addIP();
                    }
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while trying to inject LockLogin temp accessor API module");
                Console.send(plugin, "An error occurred while trying to load LockLogin temp accessor API module, check logs for more info", Level.GRAVE);
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

            user.removeBlindEffect();
        }
    }

    /**
     * Register the metrics
     */
    private void registerMetrics() {
        SpigotMetrics metrics = new SpigotMetrics(plugin, 6513);

        metrics.addCustomChart(new SpigotMetrics.SimplePie("used_locale", () -> new ConfigGetter().getLang().friendlyName()));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("clear_chat", () -> String.valueOf(new ConfigGetter().clearChat())
                .replace("true", "Clear chat")
                .replace("false", "Don't clear chat")));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("file_system", () -> new ConfigGetter().accountSys()
                .replace("file", "File")
                .replace("mysql", "MySQL")));
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
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);

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

        if (!updatedLockLogin.exists() || !manager.isReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(() -> {
                        manager.setReadyToUpdate(true);
                        Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                    });
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
            }
        } else {
            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", Level.INFO);
        }

        Console.send("&3To use this new version, you must go to /plugins/update and copy {0} to /plugins folder, replacing current {1}", LockLoginSpigot.jar, LockLoginSpigot.jar);
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
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);

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

        if (!updatedLockLogin.exists() || !manager.isReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(() -> {
                        manager.setReadyToUpdate(true);
                        Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                    });
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
            }
        } else {
            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", Level.INFO);
        }

        Console.send("&3To use this new version, you must go to /plugins/update and copy {0} to /plugins folder, replacing current {1}", LockLoginSpigot.jar, LockLoginSpigot.jar);

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
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);

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

        if (!updatedLockLogin.exists() || !manager.isReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(() -> {
                        manager.setReadyToUpdate(true);
                        Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                    });
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
            }
        } else {
            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", Level.INFO);
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

    public interface manager {

        static boolean isReadyToUpdate() {
            return ready_to_update;
        }

        static void setReadyToUpdate(final boolean status) {
            ready_to_update = status;
        }
    }
}

class TempModule extends Module {

    @Override
    public @NotNull JavaPlugin owner() {
        return LockLoginSpigot.plugin;
    }

    @Override
    public @NotNull String name() {
        return "Initialization temp module";
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
