package ml.karmaconfigs.lockloginsystem.bukkit.utils;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.karmayaml.FileCopy;
import ml.karmaconfigs.api.bukkit.karmayaml.YamlReloader;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.bukkit.ModuleUtil;
import ml.karmaconfigs.lockloginmodules.bukkit.PluginModule;
import ml.karmaconfigs.lockloginmodules.bukkit.PluginModuleLoader;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin.PluginStatusChangeEvent;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserHookEvent;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserUnHookEvent;
import ml.karmaconfigs.lockloginsystem.shared.*;
import ml.karmaconfigs.lockloginsystem.shared.alerts.LockLoginAlerts;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.passwords.InsecurePasswords;
import ml.karmaconfigs.lockloginsystem.shared.metrics.SpigotMetrics;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.GetLatestVersion;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.commands.*;
import ml.karmaconfigs.lockloginsystem.bukkit.events.*;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.AllowedCommands;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.PlayerFile;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

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
public final class PluginManagerBukkit implements LockLoginSpigot {

    private static String last_changelog = "";
    private static int checks = 0;
    private static boolean ready_to_update = true;

    /**
     * The onEnable actions the
     * plugin will perform
     */
    public final void enable() {
        Console.send("--------------------");
        Console.send(" ");
        Console.send("&bEnabling {0}&b version {1}", name, version);
        Console.send("&aInitializing files...");
        setupFiles();
        if (!PlatformUtils.accountManagerValid() || PlatformUtils.isNativeManager()) {
            PlatformUtils.setAccountManager(PlayerFile.class);
            Console.send(plugin, "Set native LockLogin database manager as player account manager", Level.INFO);
        } else {
            Console.send(plugin, "Loaded custom player account manager", Level.INFO);
        }

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
            registerMetrics();
            setupPlayers();
        } else {
            boolean is_bungeecord = true;
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

        Logger coreLogger = (Logger) LogManager.getRootLogger();
        coreLogger.addFilter(new ConsoleFilter());

        PluginStatusChangeEvent event = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.LOAD, null);
        LockLoginListener.callEvent(event);
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
        if (plugin.getServer().getMessenger().isIncomingChannelRegistered(plugin, "ll:info")) {
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "ll:info");
        }
        PinInventory.clearVerifiedList();

        PluginStatusChangeEvent event = new PluginStatusChangeEvent(PluginStatusChangeEvent.Status.UNLOAD, null);
        LockLoginListener.callEvent(event);
    }

    /**
     * Setup the plugin files
     */
    public final void setupFiles() {
        File config_file = new File(plugin.getDataFolder(), "config.yml");
        FileCopy config = new FileCopy(plugin, "configs/config_spigot.yml").withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));
        try {
            config.copy(config_file);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
        Motd motd = new Motd(motd_file);
        motd.setup();

        FileManager cfgManager = new FileManager("config.yml");
        cfgManager.setInternal("configs/config_spigot.yml");

        File passwords_yml = new File(plugin.getDataFolder(), "passwords.yml");
        FileCopy passwords = new FileCopy(plugin, "auto-generated/passwords.yml").withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));
        try {
            passwords.copy(passwords_yml);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

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

        FileCopy msg = new FileCopy(plugin, "messages/" + msg_file.getName()).withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));
        try {
            msg.copy(msg_file);
            logger.scheduleLog(Level.INFO, "Checked lang file " + msg_file.getName());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (cfg.isMySQL()) {
            File module = new File(plugin.getDataFolder() + File.separator + "modules", "LockLoginSQL.jar");
            if (!module.exists()) {
                try {
                    if (!module.getParentFile().exists())
                        Files.createDirectories(module.getParentFile().toPath());

                    Console.send(plugin, "Detected mysql configuration, downloading MySQL module, please wait...", Level.INFO);

                    try {
                        URL download_url = new URL("https://karmaconfigs.github.io/updates/LockLogin/modules/sql/LockLoginSQL.jar");

                        URLConnection connection = download_url.openConnection();
                        connection.connect();

                        InputStream input = new BufferedInputStream(download_url.openStream(), 1024);
                        OutputStream output = new FileOutputStream(module);

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
                        Console.send(plugin, "Downloaded sql module, importing configuration...", Level.INFO);

                        File current_sql = new File(plugin.getDataFolder(), "mysql.yml");
                        File dest_sql = new File(plugin.getDataFolder() + File.separator + "modules" + File.separator + "LockLoginSQL", "config.yml");

                        try {
                            if (!dest_sql.getParentFile().exists())
                                Files.createDirectories(dest_sql.getParentFile().toPath());

                            Files.move(current_sql.toPath(), dest_sql.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Throwable ignored) {}

                        LockLoginBukkitManager manager = new LockLoginBukkitManager();
                        manager.applyUpdate(null);
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }

        File spawn_file = new File(plugin.getDataFolder(), "spawn.yml");
        FileCopy spawn = new FileCopy(plugin, "auto-generated/spawn.yml").withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));
        try {
            spawn.copy(spawn_file);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        File allowed_file = new File(plugin.getDataFolder(), "allowed.yml");
        FileCopy allowedCMDs = new FileCopy(plugin, "auto-generated/allowed.yml").withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));
        try {
            allowedCMDs.copy(allowed_file);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
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
     * Start the version checker for bukkit
     */
    private void startVersionChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::doVersionCheck, 0, 20L * new ConfigGetter().checkInterval());
    }

    /**
     * Start the alert checker for bukkit
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
        MessageGetter messages = new MessageGetter();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UserHookEvent event = new UserHookEvent(player, null);
            LockLoginListener.callEvent(event);

            if (!event.isHandled()) {
                User user = new User(player);

                if (!player.hasMetadata("LockLoginUser")) {
                    player.setMetadata("LockLoginUser", new FixedMetadataValue(plugin, player.getUniqueId()));
                }

                if (config.takeBack()) {
                    LastLocation last = new LastLocation(player);
                    last.saveLocation();
                }

                if (config.enableSpawn()) {
                    Spawn spawn = new Spawn();
                    user.teleport(spawn.getSpawn());
                }

                user.genCaptcha();

                if (config.getCaptchaType().equals(CaptchaType.COMPLEX) && user.hasCaptcha()) {
                    if (config.getCaptchaTimeOut() > 0)
                        new BukkitRunnable() {
                            int back = config.getCaptchaTimeOut();

                            @Override
                            public void run() {
                                if (back == 0 || !player.isOnline()) {
                                    cancel();

                                    if (player.isOnline())
                                        user.kick("&eLockLogin\n\n" + messages.captchaTimeOut());

                                    if (!user.isRegistered())
                                        user.remove();
                                }

                                if (!user.hasCaptcha())
                                    cancel();

                                back--;
                            }
                        }.runTaskTimer(plugin, 0, 20);
                } else {
                    user.checkStatus();
                }

                TempModule temp_module = new TempModule();
                PluginModuleLoader spigot_module_loader = new PluginModuleLoader(temp_module);
                try {
                    if (!ModuleUtil.isLoaded(temp_module)) {
                        spigot_module_loader.inject();
                    }
                    InetSocketAddress ip = player.getAddress();
                    if (ip != null && ip.getAddress() != null) {
                        IpData data = new IpData(temp_module, ip.getAddress());

                        if (new ConfigGetter().accountsPerIp() != 0) {
                            if (data.getConnections() + 1 > new ConfigGetter().accountsPerIp()) {
                                user.kick(new MessageGetter().maxIp());
                            } else {
                                data.addIP();
                            }
                        }
                    }
                } catch (Throwable ex) {
                    logger.scheduleLog(Level.GRAVE, ex);
                    logger.scheduleLog(Level.INFO, "Error while trying to inject LockLogin temp accessor API module");
                    Console.send(plugin, "An error occurred while trying to load LockLogin temp accessor API module, check logs for more info", Level.GRAVE);
                }
            }
        }
    }

    /**
     * Restore player profile stats
     */
    private void unsetPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UserUnHookEvent event = new UserUnHookEvent(player, null);
            LockLoginListener.callEvent(event);

            if (!event.isHandled()) {
                User user = new User(player);

                if (player.hasMetadata("LockLoginUser")) {
                    player.removeMetadata("LockLoginUser", plugin);
                }

                ConfigGetter cfg = new ConfigGetter();
                if (cfg.takeBack()) {
                    LastLocation last = new LastLocation(player);
                    last.saveLocation();
                }

                user.removeBlindEffect();
            }
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
        metrics.addCustomChart(new SpigotMetrics.SimplePie("file_system", () -> new ConfigGetter().accountSys().toLowerCase()
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
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJar().getName());

        if (updatedLockLogin.exists()) {
            VersionChannel dest_channel = FileInfo.getChannel(updatedLockLogin);
            VersionChannel current_channel = FileInfo.getChannel(LockLoginSpigot.getJar());

            String dest_version = FileInfo.getJarVersion(updatedLockLogin);
            String curr_version = FileInfo.getJarVersion(LockLoginSpigot.getJar());

            if (!dest_version.equals(curr_version)) {
                try {
                    Files.delete(updatedLockLogin.toPath());
                } catch (Throwable ignored) {
                }
            } else {
                if (!dest_channel.equals(current_channel)) {
                    try {
                        Files.delete(updatedLockLogin.toPath());
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        if (!updatedLockLogin.exists() || manager.notReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(file -> {
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

        Console.send("&3To use this new version, you must go to /plugins/update and copy {0} to /plugins folder, replacing current {1}", LockLoginSpigot.getJar().getName(), LockLoginSpigot.getJar().getName());
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
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJar().getName());

        if (updatedLockLogin.exists()) {
            VersionChannel dest_channel = FileInfo.getChannel(updatedLockLogin);
            VersionChannel current_channel = FileInfo.getChannel(LockLoginSpigot.getJar());

            String dest_version = FileInfo.getJarVersion(updatedLockLogin);
            String curr_version = FileInfo.getJarVersion(LockLoginSpigot.getJar());

            if (!dest_version.equals(curr_version)) {
                try {
                    Files.delete(updatedLockLogin.toPath());
                } catch (Throwable ignored) {
                }
            } else {
                if (!dest_channel.equals(current_channel)) {
                    try {
                        Files.delete(updatedLockLogin.toPath());
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        if (!updatedLockLogin.exists() || manager.notReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(file -> {
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

        Console.send("&3To use this new version, you must go to /plugins/update and copy {0} to /plugins folder, replacing current {1}", LockLoginSpigot.getJar().getName(), LockLoginSpigot.getJar().getName());

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
        File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.getJar().getName());

        if (updatedLockLogin.exists()) {
            VersionChannel dest_channel = FileInfo.getChannel(updatedLockLogin);
            VersionChannel current_channel = FileInfo.getChannel(LockLoginSpigot.getJar());

            String dest_version = FileInfo.getJarVersion(updatedLockLogin);
            String curr_version = FileInfo.getJarVersion(LockLoginSpigot.getJar());

            if (!dest_version.equals(curr_version)) {
                try {
                    Files.delete(updatedLockLogin.toPath());
                } catch (Throwable ignored) {
                }
            } else {
                if (!dest_channel.equals(current_channel)) {
                    try {
                        Files.delete(updatedLockLogin.toPath());
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        if (!updatedLockLogin.exists() || manager.notReadyToUpdate()) {
            try {
                DownloadLatest downloader = new DownloadLatest();
                if (!downloader.isDownloading()) {
                    downloader.download(file -> {
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

    /**
     * Plugin manager bukkit manager
     * utilities
     */
    public interface manager {

        /**
         * Check if the plugin is not ready to
         * update
         *
         * @return if the plugin is ready to update
         */
        static boolean notReadyToUpdate() {
            return !ready_to_update;
        }

        /**
         * Set if the plugin is ready to update
         *
         * @param status if is ready to update
         */
        static void setReadyToUpdate(final boolean status) {
            ready_to_update = status;
        }
    }
}

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
class TempModule extends PluginModule {

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
    public @NotNull String update_url() {
        return "https://karmaconfigs.ml/";
    }
}
