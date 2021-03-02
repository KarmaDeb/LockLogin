package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

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

public final class UnlogCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private final Permission forceUnLog = new Permission("locklogin.forceunlog", PermissionDefault.FALSE);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (config.TakeBack()) {
                    LastLocation lastLocation = new LastLocation(player);
                    lastLocation.saveLocation();
                }

                if (config.enableSpawn()) {
                    Spawn spawn = new Spawn();

                    user.teleport(spawn.getSpawn());
                }

                user.setLogStatus(false);
                new StartCheck(player, CheckType.LOGIN);
                user.send(messages.Prefix() + messages.UnLogged());
            } else {
                if (args.length == 1) {
                    Player target = plugin.getServer().getPlayer(args[0]);

                    if (target != null) {
                        if (!target.equals(player)) {
                            if (player.hasPermission(forceUnLog)) {
                                User targetUser = new User(target);

                                if (targetUser.isLogged() && !targetUser.isTempLog()) {
                                    if (config.TakeBack()) {
                                        LastLocation lastLocation = new LastLocation(target);
                                        lastLocation.saveLocation();
                                    }

                                    if (config.enableSpawn()) {
                                        Spawn spawn = new Spawn();

                                        targetUser.teleport(spawn.getSpawn());
                                    }

                                    targetUser.setLogStatus(false);
                                    new StartCheck(target, CheckType.LOGIN);
                                    targetUser.send(messages.Prefix() + messages.ForcedUnLog(player));
                                    user.send(messages.Prefix() + messages.ForcedUnLogAdmin(target));
                                } else {
                                    user.send(messages.Prefix() + messages.TargetAccessError(target));
                                }
                            } else {
                                user.send(messages.Prefix() + messages.PermissionError(forceUnLog.getName()));
                            }
                        } else {
                            user.send(messages.Prefix() + messages.UnLog());
                        }
                    } else {
                        if (player.hasPermission(forceUnLog)) {
                            user.send(messages.Prefix() + messages.ConnectionError(args[0]));
                        } else {
                            user.send(messages.Prefix() + messages.PermissionError(forceUnLog.getName()));
                        }
                    }
                } else {
                    user.send(messages.Prefix() + messages.UnLog());
                }
            }
        } else {
            if (args.length == 1) {
                Player target = plugin.getServer().getPlayer(args[0]);

                if (target != null) {
                    User targetUser = new User(target);

                    if (targetUser.isLogged() && !targetUser.isTempLog()) {
                        if (config.TakeBack()) {
                            LastLocation lastLocation = new LastLocation(target);
                            lastLocation.saveLocation();
                        }

                        if (config.enableSpawn()) {
                            Spawn spawn = new Spawn();

                            targetUser.teleport(spawn.getSpawn());
                        }

                        targetUser.setLogStatus(false);
                        new StartCheck(target, CheckType.LOGIN);
                        targetUser.send(messages.Prefix() + messages.ForcedUnLog("SERVER"));
                        Console.send(messages.Prefix() + messages.ForcedUnLog(target));
                    } else {
                        Console.send(messages.Prefix() + messages.TargetAccessError(target));
                    }
                } else {
                    Console.send(messages.Prefix() + messages.ConnectionError(args[0]));
                }
            } else {
                Console.send(plugin, "Correct usage: unlog <player>", Level.WARNING);
            }
        }
        return false;
    }
}
