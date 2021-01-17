package ml.karmaconfigs.lockloginsystem.spigot.utils;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.karmayaml.FileCopy;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.Main;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.ConsoleFilter;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.alerts.LockLoginAlerts;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Bucket;
import ml.karmaconfigs.lockloginsystem.shared.metrics.SpigotMetrics;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.LockLoginVersion;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.commands.*;
import ml.karmaconfigs.lockloginsystem.spigot.events.*;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.AllowedCommands;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.MySQLData;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter;
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

    private static String last_changelog = "";
    private static int checks = 0;
    private static boolean ready_to_update = false;
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
            if (new ConfigGetter().CheckForUpdates()) {
                startVersionChecker();
            } else {
                doVersionCheck();
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

        Console.send(plugin, "LockLogin will search for updates and will be updated automatically when no players are in server", Level.INFO);
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
    private void setupFiles() {
        File config_file = new File(plugin.getDataFolder(), "config.yml");
        FileCopy config = new FileCopy(plugin, "configs/config_spigot.yml");
        config.copy(config_file);

        YamlConfiguration cfg_yml = YamlConfiguration.loadConfiguration(config_file);

        String random = StringUtils.randomString(5);

        if (cfg_yml.getString("ServerName", "").isEmpty()) {
            cfg_yml.set("ServerName", random);
        }

        ConfigGetter cfg = new ConfigGetter();
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
            cfg_yml.set("AccountSys", "File");
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

        try {
            cfg_yml.save(config_file);
            config.copy(config_file);
        } catch (Throwable e) {
            logger.scheduleLog(Level.GRAVE, e);
            logger.scheduleLog(Level.INFO, "Error while saving config file");
        }
    }

    /**
     * Setup mysql connection if the config
     * defines to use mysql
     */
    private void setupMySQL() {
        if (!new ConfigGetter().isBungeeCord()) {
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
            if (CheckerSpigot.isOutdated()) {
                Console.send("&eLockLogin &7>> &aNew version available for LockLogin &f( &3" + LockLoginVersion.version + " &f)");
                if (new ConfigGetter().UpdateSelf()) {
                    String dir = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");

                    File pluginsFolder = new File(dir.replace("/LockLogin", ""));
                    File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginSpigot.jar);

                    if (!updatedLockLogin.exists() || !ready_to_update) {
                        try {
                            DownloadLatest latest = new DownloadLatest(new ConfigGetter().isFatJar());
                            if (!latest.isDownloading()) {
                                latest.download(() -> {
                                    ready_to_update = true;
                                    Console.send(plugin, "[ LLAUS ] LockLogin downloaded latest version and is ready to update", Level.INFO);
                                });
                            }
                        } catch (Throwable e) {
                            logger.scheduleLog(Level.GRAVE, e);
                            logger.scheduleLog(Level.INFO, "[ LLAUS ] Error while downloading LockLogin latest version instance");
                        }
                    } else {
                        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                            Main.updatePending = true;
                            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, and LockLogin will apply updates automatically due no online players were found", Level.INFO);
                            new LockLoginSpigotManager().applyUpdate(null);
                            ready_to_update = false;
                        } else {
                            Console.send(plugin, "[ LLAUS ] LockLogin have been updated, you can run /locklogin applyUpdates or restart your proxy (Recommended)", Level.INFO);
                        }
                    }
                } else {
                    if (!last_changelog.equals(LockLoginVersion.changeLog) || checks >= 3) {
                        CheckerSpigot.sendChangeLog();
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
     * Start the version checker for spigot
     */
    private void startVersionChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::doVersionCheck, 0, 20 * new ConfigGetter().UpdateCheck());
    }

    /**
     * Start the alert checker for spigot
     */
    private void startAlertChecker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (LockLoginAlerts.AlertAvailable()) {
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

            TempModule temp_module = new TempModule();
            ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
            try {
                if (!ModuleLoader.manager.isLoaded(temp_module)) {
                    spigot_module_loader.inject();
                }
                IpData data = new IpData(temp_module, player.getAddress().getAddress());

                if (new ConfigGetter().AccountsPerIp() != 0) {
                    if (data.getConnections() + 1 > new ConfigGetter().AccountsPerIp()) {
                        user.Kick(new MessageGetter().MaxIp());
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
        SpigotMetrics metrics = new SpigotMetrics(plugin, 6513);

        metrics.addCustomChart(new SpigotMetrics.SimplePie("used_locale", () -> String.valueOf(new ConfigGetter().getLang())));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("country_protect", () -> "Removed in 3.0.2"));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("clear_chat", () -> String.valueOf(new ConfigGetter().ClearChat())
                .replace("true", "Clear chat")
                .replace("false", "Don't clear chat")));
        metrics.addCustomChart(new SpigotMetrics.SimplePie("file_system", () -> new ConfigGetter().FileSys()
                .replace("file", "File")
                .replace("mysql", "MySQL")));
    }

    public interface manager {

        static void setReadyToUpdate(final boolean status) {
            ready_to_update = status;
        }

        static boolean isReadyToUpdate() {
            return ready_to_update;
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
        return "This module is used to access an API feature";
    }

    @Override
    public @NotNull String author_url() {
        return "https://karmaconfigs.ml/";
    }
}
