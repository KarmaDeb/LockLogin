package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.reflections.BarMessage;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.PluginManagerSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.GetLatestVersion;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class CheckUpdateCommand implements CommandExecutor, SpigotFiles {

    private static boolean downloading = false;

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        GetLatestVersion latest = new GetLatestVersion();

        int last_version_id = latest.getId();
        int curr_version_id = LockLoginSpigot.versionID;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "--version":
                        if (player.hasPermission("locklogin.readversion")) {
                            user.Message("&cLockLogin current version: &e" + StringUtils.stripColor(LockLoginSpigot.version));
                            user.Message("&cLatest LockLogin version: &e" + StringUtils.stripColor(latest.getVersion()));
                            user.Message("&cVersion channel: &e" + latest.getChannel().name().toUpperCase());
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError("locklogin.readversion"));
                        }
                        break;
                    case "--update":
                        if (player.hasPermission("locklogin.checkupdate")) {
                            if (last_version_id > curr_version_id) {
                                switch (latest.getChannel()) {
                                    case SNAPSHOT:
                                        if (config.getUpdateChannel().equals(VersionChannel.SNAPSHOT)) {
                                            noReleaseMessage(player, latest);
                                        } else {
                                            user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                        }
                                        break;
                                    case RC:
                                        switch (config.getUpdateChannel()) {
                                            case SNAPSHOT:
                                            case RC:
                                                noReleaseMessage(player, latest);
                                                break;
                                            default:
                                                user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                        }
                                        break;
                                    case RELEASE:
                                        switch (config.getUpdateChannel()) {
                                            case SNAPSHOT:
                                            case RC:
                                            case RELEASE:
                                            default:
                                                releaseMessage(player, latest);
                                                break;
                                        }
                                        break;
                                    default:
                                        user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                        break;
                                }
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
                                    switch (latest.getChannel()) {
                                        case SNAPSHOT:
                                            if (config.getUpdateChannel() == VersionChannel.SNAPSHOT) {
                                                performDownload(player);
                                            } else {
                                                user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                            }
                                            break;
                                        case RC:
                                            switch (config.getUpdateChannel()) {
                                                case SNAPSHOT:
                                                case RC:
                                                    performDownload(player);
                                                    break;
                                                default:
                                                    user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                                    break;
                                            }
                                            break;
                                        case RELEASE:
                                            switch (config.getUpdateChannel()) {
                                                case SNAPSHOT:
                                                case RC:
                                                case RELEASE:
                                                default:
                                                    performDownload(player);
                                                    break;
                                            }
                                            break;
                                        default:
                                            user.Message("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                            break;
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
                    default:
                        user.Message(messages.Prefix() + "&cPlease specify a command arg &7( &e--version&f, &e--update &7)");
                        break;
                }
            } else {
                user.Message(messages.Prefix() + "&cPlease specify a command arg &7( &e--version&f, &e--update &7)");
            }
        } else {
            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "--version":
                        Console.send("&cLockLogin current version: &e" + StringUtils.stripColor(LockLoginSpigot.version));
                        Console.send("&cLatest LockLogin version: &e" + StringUtils.stripColor(latest.getVersion()));
                        Console.send("&cVersion channel: &e" + latest.getChannel().name().toUpperCase());
                        break;
                    case "--version&changelog":
                        Console.send("&cLockLogin current version: &e" + StringUtils.stripColor(LockLoginSpigot.version));
                        Console.send("&cLatest LockLogin version: &e" + StringUtils.stripColor(latest.getVersion()));
                        Console.send("&cVersion channel: &e" + latest.getChannel().name().toUpperCase());
                        Console.send("\n");

                        Console.send(latest.getChangeLog());
                        break;
                    case "--update":
                        if (last_version_id > curr_version_id) {
                            switch (latest.getChannel()) {
                                case SNAPSHOT:
                                    if (config.getUpdateChannel().equals(VersionChannel.SNAPSHOT)) {
                                        noReleaseMessage(null, latest);
                                    } else {
                                        Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                    }
                                    break;
                                case RC:
                                    switch (config.getUpdateChannel()) {
                                        case SNAPSHOT:
                                        case RC:
                                            noReleaseMessage(null, latest);
                                            break;
                                        default:
                                            Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                    }
                                    break;
                                case RELEASE:
                                    switch (config.getUpdateChannel()) {
                                        case SNAPSHOT:
                                        case RC:
                                        case RELEASE:
                                        default:
                                            releaseMessage(null, latest);
                                            break;
                                    }
                                    break;
                                default:
                                    Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                    break;
                            }
                        } else {
                            Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                        }
                        break;
                    case "--forceupdate":
                        if (last_version_id > curr_version_id) {
                            if (!downloading) {
                                switch (latest.getChannel()) {
                                    case SNAPSHOT:
                                        if (config.getUpdateChannel() == VersionChannel.SNAPSHOT) {
                                            performDownload(null);
                                        } else {
                                            Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                        }
                                        break;
                                    case RC:
                                        switch (config.getUpdateChannel()) {
                                            case SNAPSHOT:
                                            case RC:
                                                performDownload(null);
                                                break;
                                            default:
                                                Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                                break;
                                        }
                                        break;
                                    case RELEASE:
                                        switch (config.getUpdateChannel()) {
                                            case SNAPSHOT:
                                            case RC:
                                            case RELEASE:
                                            default:
                                                performDownload(null);
                                                break;
                                        }
                                        break;
                                    default:
                                        Console.send("&aLockLogin is fully updated and you are enjoying the latest features and bug fixes");
                                        break;
                                }
                            } else {
                                Console.send("&cLockLogin is already downloading an update");
                            }
                        } else {
                            Console.send("&cWoah! Are you sure is LockLogin outdated?");
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

    private void noReleaseMessage(final Player player, final GetLatestVersion latest) {
        if (player != null) {
            User user = new User(player);

            user.Message("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginSpigot.version) + "&c to &e" + StringUtils.stripColor(latest.getVersion()) + "&7( &e" + latest.getChannel().name() + " &7)");
            user.Message("\n");
            user.Message("&7To update, run /updateChecker --forceUpdate");
            user.Message("&7then replace plugins/{0} with /plugins/update/{1}".replace("{0}", LockLoginSpigot.jar).replace("{1}", LockLoginSpigot.jar));
        } else {
            Console.send("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginSpigot.version) + "&c to &e" + StringUtils.stripColor(latest.getVersion()) + "&7( &e" + latest.getChannel().name() + " &7)");
            Console.send("\n");
            Console.send("&7To update, run /updateChecker --forceUpdate");
            Console.send("&7then replace plugins/{0} with /plugins/update/{1}".replace("{0}", LockLoginSpigot.jar).replace("{1}", LockLoginSpigot.jar));
        }
    }

    private void releaseMessage(final Player player, final GetLatestVersion latest) {
        if (player != null) {
            User user = new User(player);

            user.Message("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginSpigot.version) + "&c to &e" + StringUtils.stripColor(latest.getVersion()));
            user.Message("\n");
            user.Message("&7To update, run /updateChecker --forceUpdate");
            user.Message("&7otherwise, there are other two ways to update it:");
            user.Message("\n");
            user.Message("&e1 - &dUsing LockLogin auto-update system ( run /locklogin applyUpdates )");
            user.Message("&e2 - &dRemoving current LockLogin plugin file and replace it with");
            user.Message("      &dthe new version ( https://www.spigotmc.org/resources/gsa-locklogin.75156/ )");
        } else {
            Console.send("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginSpigot.version) + "&c to &e" + StringUtils.stripColor(latest.getVersion()));
            Console.send("\n");
            Console.send("&7To update, run /updateChecker --forceUpdate");
            Console.send("&7otherwise, there are other two ways to update it:");
            Console.send("\n");
            Console.send("&e1 - &dUsing LockLogin auto-update system ( run /locklogin applyUpdates )");
            Console.send("&e2 - &dRemoving current LockLogin plugin file and replace it with");
            Console.send("      &dthe new version ( https://www.spigotmc.org/resources/gsa-locklogin.75156/ )");
        }
    }

    private void performDownload(final Player player) {
        if (player != null) {
            User user = new User(player);

            user.Message("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
            user.Message("&aWe will notice you when it's downloaded");

            if (!PluginManagerSpigot.manager.isReadyToUpdate()) {
                LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                    try {
                        DownloadLatest downloader = new DownloadLatest();

                        BarMessage bmsg = new BarMessage(player, StringUtils.toColor("&fDownloading LockLogin update: &e" + downloader.getPercentage()));
                        bmsg.send(true);

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (player.isOnline() && downloader.getPercentage() <= 97) {
                                    bmsg.setMessage(StringUtils.toColor("&fDownloading LockLogin update: &e" + downloader.getPercentage() + "&f%"));
                                } else {
                                    bmsg.setMessage("");
                                    bmsg.stop();
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
            Console.send("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
            Console.send("&aWe will notice you when it's downloaded");

            if (!PluginManagerSpigot.manager.isReadyToUpdate()) {
                LockLoginSpigot.plugin.getServer().getScheduler().runTaskAsynchronously(LockLoginSpigot.plugin, () -> {
                    try {
                        DownloadLatest downloader = new DownloadLatest();

                        if (downloader.isDownloading()) {
                            Console.send("&cLockLogin is already downloading an update");
                        } else {
                            downloader.download(() -> {
                                Console.send("&aLatest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates");
                                downloading = false;
                                PluginManagerSpigot.manager.setReadyToUpdate(true);
                            });
                        }
                    } catch (Throwable ex) {
                        LockLoginSpigot.logger.scheduleLog(Level.GRAVE, ex);
                        Console.send("&cError while downloading latest LockLogin version, see console for more info");
                    }
                });
            } else {
                Console.send("&aLatest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates");
            }
        }
    }
}
