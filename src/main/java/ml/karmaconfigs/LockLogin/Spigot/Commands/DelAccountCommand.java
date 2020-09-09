package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.OfflineUser;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

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
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
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

                                if (config.HandleSpawn()) {
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

                            if (utils.PasswordIsOk()) {
                                if (config.TakeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.HandleSpawn()) {
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

                    if (config.HandleSpawn()) {
                        Spawn spawn = new Spawn();

                        targetUser.Teleport(spawn.getSpawn());
                    }

                    targetUser.remove();
                    targetUser.setLogStatus(false);
                    targetUser.Message(messages.Prefix() + messages.ForcedDelAccount("SERVER"));
                    out.Message(messages.Prefix() + messages.ForcedDelAccountAdmin(target));
                    new StartCheck(target, CheckType.REGISTER);
                } else {
                    OfflineUser targetUser = new OfflineUser(tar);

                    if (targetUser.exists()) {
                        targetUser.delete();
                        out.Message(messages.Prefix() + messages.ForcedDelAccountAdmin(tar));
                    } else {
                        out.Message(messages.Prefix() + messages.NeverPlayed(tar));
                    }
                }
            } else {
                out.Alert("Correct usage is delacc <player>", WarningLevel.ERROR);
            }
        }
        return false;
    }
}
