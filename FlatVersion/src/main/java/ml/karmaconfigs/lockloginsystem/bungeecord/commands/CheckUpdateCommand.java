package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.StringUtils;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.CheckerBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.version.DownloadLatest;
import ml.karmaconfigs.lockloginsystem.shared.version.LockLoginVersion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class CheckUpdateCommand extends Command implements BungeeFiles, LockLoginVersion {

    private static boolean downloading = false;

    public CheckUpdateCommand() {
        super("updateChecker", "", "checkUpdates");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "--version":
                        if (player.hasPermission("locklogin.readversion")) {
                            user.Message("&cLockLogin current version: &e" + StringUtils.stripColor(LockLoginBungee.version));
                            user.Message("&cLatest LockLogin version: &e" + StringUtils.stripColor(LockLoginVersion.version));
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError("locklogin.readversion"));
                        }
                        break;
                    case "--update":
                        if (player.hasPermission("locklogin.checkupdate")) {
                            if (CheckerBungee.isOutdated()) {
                                user.Message("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginBungee.version) + "&c to &e" + StringUtils.stripColor(LockLoginVersion.version));
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
                            if (CheckerBungee.isOutdated()) {
                                if (!downloading) {
                                    user.Message("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
                                    user.Message("&aWe will notice you when it's downloaded");

                                    InterfaceUtils utils = new InterfaceUtils();
                                    if (!utils.isReadyToUpdate()) {
                                        LockLoginBungee.plugin.getProxy().getScheduler().runAsync(LockLoginBungee.plugin, () -> {
                                            try {
                                                DownloadLatest latest = new DownloadLatest(config.isFatJar());

                                                Timer timer = new Timer();
                                                timer.schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        if (player.isConnected() && latest.getPercentage() <= 97) {
                                                            player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor("&fDownloading LockLogin update: &e" + latest.getPercentage() + "&f%")));
                                                        } else {
                                                            cancel();
                                                        }
                                                    }
                                                }, 0, TimeUnit.SECONDS.toMillis(1));

                                                if (latest.isDownloading()) {
                                                    user.Message("&cLockLogin is already downloading an update");
                                                } else {
                                                    latest.download(() -> {
                                                        if (player.isConnected()) {
                                                            user.Message("&aLatest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates");
                                                            downloading = false;
                                                            utils.setReadyToUpdate(true);
                                                        }
                                                    });
                                                }
                                            } catch (Throwable ex) {
                                                LockLoginBungee.logger.scheduleLog(Level.GRAVE, ex);
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
                        Console.send("&cLockLogin current version: &e{0}", StringUtils.stripColor(LockLoginBungee.version));
                        Console.send("&cLatest LockLogin version: &e{0}", StringUtils.stripColor(LockLoginVersion.version));
                        break;
                    case "--version&changelog":
                        Console.send("&cLockLogin current version: &e{0}", StringUtils.stripColor(LockLoginBungee.version));
                        Console.send("&cLatest LockLogin version: &e{0}", StringUtils.stripColor(LockLoginVersion.version));
                        System.out.println("\n");
                        CheckerBungee.sendChangeLog();
                        break;
                    case "--update":
                        if (CheckerBungee.isOutdated()) {
                            Console.send("&cLockLogin needs to update from &e" + StringUtils.stripColor(LockLoginBungee.version) + "&c to &e" + StringUtils.stripColor(LockLoginVersion.version));
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
                        if (CheckerBungee.isOutdated()) {
                            if (!downloading) {
                                Console.send("&aDownloading latest LockLogin version &c( this process is async but may lag the server a bit )");
                                Console.send("&aWe will notice you when it's downloaded");

                                InterfaceUtils utils = new InterfaceUtils();
                                if (!utils.isReadyToUpdate()) {
                                    LockLoginBungee.plugin.getProxy().getScheduler().runAsync(LockLoginBungee.plugin, () -> {
                                        try {
                                            DownloadLatest latest = new DownloadLatest(config.isFatJar());
                                            if (latest.isDownloading()) {
                                                Console.send(LockLoginBungee.plugin, "LockLogin is already downloading an update", Level.GRAVE);
                                            } else {
                                                latest.download(() -> {
                                                    Console.send(LockLoginBungee.plugin, "Latest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates", Level.INFO);
                                                    downloading = false;
                                                    utils.setReadyToUpdate(true);
                                                });
                                            }
                                        } catch (Throwable ex) {
                                            LockLoginBungee.logger.scheduleLog(Level.GRAVE, ex);
                                            Console.send(LockLoginBungee.plugin, "Error while downloading latest LockLogin version, see console for more info", Level.GRAVE);
                                        }
                                    });
                                } else {
                                    Console.send(LockLoginBungee.plugin, "Latest LockLogin version jar file has been downloaded, to apply updates simply run /applyUpdates", Level.INFO);
                                }
                            } else {
                                Console.send(LockLoginBungee.plugin, "LockLogin is already downloading an update", Level.GRAVE);
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
    }
}
