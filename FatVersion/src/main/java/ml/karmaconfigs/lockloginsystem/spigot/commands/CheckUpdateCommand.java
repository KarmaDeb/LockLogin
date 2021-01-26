package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.StringUtils;
import ml.karmaconfigs.api.spigot.reflections.BarMessage;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.GetLatestVersion;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.PluginManagerSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class CheckUpdateCommand implements CommandExecutor, SpigotFiles {

    private static boolean downloading = false;

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        GetLatestVersion latest = new GetLatestVersion();

        int last_version_id = latest.GetLatest();
        int curr_version_id = LockLoginSpigot.versionID;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "--version":
                        if (player.hasPermission("locklogin.readversion")) {
                            user.Message("&cLockLogin current version: &e" + StringUtils.stripColor(LockLoginSpigot.version));
                            user.Message("&cLatest LockLogin version: &e" + StringUtils.stripColor(latest.getVersionString()));
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError("locklogin.readversion"));
                        }
                        break;
                    case "--update":
                        if (player.hasPermission("locklogin.checkupdate")) {
                            if (last_version_id > curr_version_id) {
                                user.Message("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginSpigot.version) + "&c to &e" + StringUtils.stripColor(latest.getVersionString()));
                                user.Message("\n");
                                user.Message("&7To update, run /updateChecker --forceUpdate");
                                user.Message("&7otherwise, there are other two ways to update it:");
                                user.Message("\n");
                                user.Message("&e1 - &dUsing LockLogin auto-update system ( run /applyUpdates )");
                                user.Message("&e1 - &dRemoving current LockLogin plugin file and download");
                                user.Message("      &dand install again using LockLogin IM ( LockLogin installation media )");
                            } else {
                                user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError("locklogin.checkupdate"));
                        }
                        break;
                    case "--forceupdate":
                        if (player.hasPermission("locklogin.forceupdate")) {
                            if (last_version_id > curr_version_id) {
                                if (!downloading) {
                                    user.Message("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
                                    user.Message("&aWe will notice you when it's downloaded");

                                    if (!PluginManagerSpigot.manager.isReadyToUpdate()) {
                                        LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                                            try {
                                                DownloadLatest downloader = new DownloadLatest(config.isFatJar());

                                                Timer timer = new Timer();
                                                BarMessage bar = new BarMessage(player, "&fDownloading LockLogin update: &e" + downloader.getPercentage() + "&f%");
                                                bar.send(true);
                                                timer.schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        if (player.isOnline() && downloader.getPercentage() <= 97) {
                                                            bar.setMessage("&fDownloading LockLogin update: &e" + downloader.getPercentage() + "&f%");
                                                        } else {
                                                            bar.stop();
                                                            cancel();
                                                        }
                                                    }
                                                }, 0, TimeUnit.SECONDS.toMillis(1));

                                                if (downloader.isDownloading()) {
                                                    user.Message("&cLockLogin is already downloading an update");
                                                } else {
                                                    downloader.download(() -> {
                                                        if (player.isOnline()) {
                                                            user.Message("&aLatest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates");
                                                            downloading = false;
                                                            PluginManagerSpigot.manager.setReadyToUpdate(true);
                                                        }
                                                    });
                                                }
                                            } catch (Throwable ex) {
                                                LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
                                                user.Message("&cError while downloading latest LockLogin version, see console for more info");
                                            }
                                        });
                                    } else {
                                        user.Message("&aLatest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates");
                                    }
                                } else {
                                    user.Message("&cLockLogin is already downloading an update");
                                }
                            } else {
                                user.Message("&cWoah! Are you sure is LockLogin outdated?");
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError("locklogin.forceupdate"));
                        }
                        break;
                    case "--switchflat":
                        if (player.hasPermission("locklogin.forceupdate")) {
                            if (!downloading) {
                                user.Message("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
                                user.Message("&aWe will notice you when it's downloaded");

                                if (!PluginManagerSpigot.manager.isReadyToUpdate()) {
                                    LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                                        try {
                                            DownloadLatest downloader = new DownloadLatest(false);

                                            Timer timer = new Timer();
                                            BarMessage bar = new BarMessage(player, "&fDownloading LockLogin flat: &e" + downloader.getPercentage() + "&f%");
                                            bar.send(true);
                                            timer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    if (player.isOnline() && downloader.getPercentage() <= 97) {
                                                        bar.setMessage("&fDownloading LockLogin flat: &e" + downloader.getPercentage() + "&f%");
                                                    } else {
                                                        bar.stop();
                                                        cancel();
                                                    }
                                                }
                                            }, 0, TimeUnit.SECONDS.toMillis(1));

                                            if (downloader.isDownloading()) {
                                                user.Message("&cLockLogin is already downloading an update");
                                            } else {
                                                downloader.download(() -> {
                                                    if (player.isOnline()) {
                                                        user.Message("&aLockLogin flat version has been downloaded, to use it simply type /applyUpdates");
                                                        downloading = false;
                                                        PluginManagerSpigot.manager.setReadyToUpdate(true);
                                                    }
                                                });
                                            }
                                        } catch (Throwable ex) {
                                            LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
                                            user.Message("&cError while downloading latest LockLogin flat version, see logs for more info");
                                        }
                                    });
                                } else {
                                    user.Message("&cLockLogin has detected there's already an update LockLogin file, run &7/applyUpdates&c and try again");
                                }
                            } else {
                                user.Message("&cLockLogin is already downloading an update");
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError("locklogin.forceupdate"));
                        }
                        break;
                    default:
                        user.Message(messages.Prefix() + "&cPlease specify a command arg &7( &e--version&f, &e--update &7)");
                }
            } else {
                user.Message(messages.Prefix() + "&cPlease specify a command arg &7( &e--version&f, &e--update &7)");
            }
        } else {
            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "--version":
                        Console.send("&cLockLogin current version: &e{0}", StringUtils.stripColor(LockLoginSpigot.version));
                        Console.send("&cLatest LockLogin version: &e{0}", StringUtils.stripColor(latest.getVersionString()));
                        break;
                    case "--version&changelog":
                        Console.send("&cLockLogin current version: &e{0}", StringUtils.stripColor(LockLoginSpigot.version));
                        Console.send("&cLatest LockLogin version: &e{0}", StringUtils.stripColor(latest.getVersionString()));
                        System.out.println("\n");

                        Console.send(latest.getChangeLog());
                        break;
                    case "--update":
                        if (last_version_id > curr_version_id) {
                            Console.send("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginSpigot.version) + "&c to &e" + StringUtils.stripColor(latest.getVersionString()));
                            System.out.println("\n");
                            Console.send("&7If you want to download the new update run the command /updateChecker --forceUpdate");
                            Console.send("&7otherwise, there are other two ways to update it:");
                            System.out.println("\n");
                            Console.send("&e1 - &dUsing LockLogin auto-update system ( run /applyUpdates )");
                            Console.send("&e1 - &dRemoving current LockLogin plugin file and download and install again using LockLogin IM ( LockLogin installation media )");
                        } else {
                            Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                        }
                        break;
                    case "--forceupdate":
                        if (last_version_id > curr_version_id) {
                            if (!downloading) {
                                Console.send("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
                                Console.send("&aWe will notice you when it's downloaded");

                                if (!PluginManagerSpigot.manager.isReadyToUpdate()) {
                                    LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                                        try {
                                            DownloadLatest downloader = new DownloadLatest(config.isFatJar());
                                            if (downloader.isDownloading()) {
                                                Console.send(LockLoginSpigot.plugin, "LockLogin is already downloading an update", Level.GRAVE);
                                            } else {
                                                downloader.download(() -> {
                                                    Console.send(LockLoginSpigot.plugin, "Latest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates", Level.INFO);
                                                    downloading = false;
                                                    PluginManagerSpigot.manager.setReadyToUpdate(true);
                                                });
                                            }
                                        } catch (Throwable ex) {
                                            LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
                                            Console.send(LockLoginSpigot.plugin, "Error while downloading latest LockLogin version, see console for more info", Level.GRAVE);
                                        }
                                    });
                                } else {
                                    Console.send(LockLoginSpigot.plugin, "Latest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates", Level.INFO);
                                }
                            } else {
                                Console.send(LockLoginSpigot.plugin, "LockLogin is already downloading an update", Level.GRAVE);
                            }
                        } else {
                            Console.send("&cWoah! Are you sure is LockLogin outdated?");
                        }
                        break;
                    case "--switchFlat":
                        if (!downloading) {
                            Console.send("&aDownloading latest LockLogin flat version &c( this process is async but may lag the server a bit )");
                            Console.send("&aWe will notice you when it's downloaded");

                            if (!PluginManagerSpigot.manager.isReadyToUpdate()) {
                                LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                                    try {
                                        DownloadLatest downloader = new DownloadLatest(false);

                                        if (downloader.isDownloading()) {
                                            Console.send("&cLockLogin is already downloading an update");
                                        } else {
                                            downloader.download(() -> {
                                                Console.send("&aLockLogin flat version has been downloaded, to use it simply type /applyUpdates");
                                                downloading = false;
                                                PluginManagerSpigot.manager.setReadyToUpdate(true);
                                            });
                                        }
                                    } catch (Throwable ex) {
                                        LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
                                        Console.send("&cError while downloading latest LockLogin flat version, see logs for more info");
                                    }
                                });
                            } else {
                                Console.send("&cLockLogin has detected there's already an update LockLogin file, run &7/applyUpdates&c and try again");
                            }
                        } else {
                            Console.send("&cLockLogin is already downloading an update");
                        }
                        break;
                    default:
                        Console.send(messages.Prefix() + "&cPlease specify a command arg &7( &e--version&f, &e--update &7)");
                        break;
                }
            } else {
                Console.send(messages.Prefix() + "&cPlease specify a command arg &7( &e--version&f, &e--update &7)");
            }
        }

        return false;
    }
}
