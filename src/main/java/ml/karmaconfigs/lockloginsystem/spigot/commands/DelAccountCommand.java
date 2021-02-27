package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
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

public final class DelAccountCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    Permission forceDel = new Permission("locklogin.forcedel", PermissionDefault.FALSE);

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (user.isLogged()) {
                    user.Message(messages.Prefix() + messages.DelAccount());
                } else {
                    if (user.isRegistered()) {
                        user.Message(messages.Prefix() + messages.Login());
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                }
            } else {
                if (args.length == 1) {
                    if (player.hasPermission(forceDel)) {

                        String tar = args[0];

                        if (plugin.getServer().getPlayer(tar) != null) {
                            Player target = plugin.getServer().getPlayer(tar);
                            if (!target.equals(player)) {
                                User targetUser = new User(target);

                                if (config.TakeBack()) {
                                    LastLocation lastLocation = new LastLocation(target);
                                    lastLocation.saveLocation();
                                }

                                if (config.enableSpawn()) {
                                    Spawn spawn = new Spawn();

                                    targetUser.Teleport(spawn.getSpawn());
                                }

                                targetUser.remove();
                                targetUser.setLogStatus(false);
                                targetUser.setTempLog(false);
                                targetUser.Message(messages.Prefix() + messages.ForcedDelAccount(player));
                                user.Message(messages.Prefix() + messages.ForcedDelAccountAdmin(target));
                                new StartCheck(target, CheckType.REGISTER);
                            } else {
                                user.Message(messages.Prefix() + messages.DelAccount());
                            }
                        } else {
                            OfflineUser targetUser = new OfflineUser(tar);

                            if (targetUser.exists()) {
                                targetUser.delete();
                                user.Message(messages.Prefix() + messages.ForcedDelAccountAdmin(tar));
                            } else {
                                user.Message(messages.Prefix() + messages.NeverPlayed(tar));
                            }
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.PermissionError(forceDel.getName()));
                    }
                } else {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                            if (utils.checkPW()) {
                                if (config.TakeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.enableSpawn()) {
                                    Spawn spawn = new Spawn();

                                    user.Teleport(spawn.getSpawn());
                                }

                                user.remove();
                                user.setLogStatus(false);
                                user.Message(messages.Prefix() + messages.AccountDeleted());
                                new StartCheck(player, CheckType.REGISTER);
                            } else {
                                user.Message(messages.Prefix() + messages.DelAccountError());
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.DelAccountMatch());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.DelAccount());
                    }
                }
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                if (plugin.getServer().getPlayer(tar) != null) {
                    Player target = plugin.getServer().getPlayer(tar);
                    User targetUser = new User(target);

                    if (config.TakeBack()) {
                        LastLocation lastLocation = new LastLocation(target);
                        lastLocation.saveLocation();
                    }

                    if (config.enableSpawn()) {
                        Spawn spawn = new Spawn();

                        targetUser.Teleport(spawn.getSpawn());
                    }

                    targetUser.remove();
                    targetUser.setLogStatus(false);
                    targetUser.Message(messages.Prefix() + messages.ForcedDelAccount("SERVER"));
                    Console.send(messages.Prefix() + messages.ForcedDelAccountAdmin(target));
                    new StartCheck(target, CheckType.REGISTER);
                } else {
                    OfflineUser targetUser = new OfflineUser(tar);

                    if (targetUser.exists()) {
                        targetUser.delete();
                        Console.send(messages.Prefix() + messages.ForcedDelAccountAdmin(tar));
                    } else {
                        Console.send(messages.Prefix() + messages.NeverPlayed(tar));
                    }
                }
            } else {
                Console.send(plugin, "Correct usage: delacc <player>", Level.WARNING);
            }
        }
        return false;
    }
}
