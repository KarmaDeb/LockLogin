package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
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

public final class UnlogCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private final Permission forceUnLog = new Permission("locklogin.forceunlog", PermissionDefault.FALSE);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (config.TakeBack()) {
                    LastLocation lastLocation = new LastLocation(player);
                    lastLocation.saveLocation();
                }

                if (config.HandleSpawn()) {
                    Spawn spawn = new Spawn();

                    user.Teleport(spawn.getSpawn());
                }

                user.setLogStatus(false);
                new StartCheck(player, CheckType.LOGIN);
                user.Message(messages.Prefix() + messages.UnLogged());
            } else {
                if (args.length == 1) {
                    if (plugin.getServer().getPlayer(args[0]) != null) {
                        Player target = plugin.getServer().getPlayer(args[0]);

                        if (!target.equals(player)) {
                            if (player.hasPermission(forceUnLog)) {
                                User targetUser = new User(target);

                                if (targetUser.isLogged() && !targetUser.isTempLog()) {
                                    if (config.TakeBack()) {
                                        LastLocation lastLocation = new LastLocation(target);
                                        lastLocation.saveLocation();
                                    }

                                    if (config.HandleSpawn()) {
                                        Spawn spawn = new Spawn();

                                        targetUser.Teleport(spawn.getSpawn());
                                    }

                                    targetUser.setLogStatus(false);
                                    new StartCheck(target, CheckType.LOGIN);
                                    targetUser.Message(messages.Prefix() + messages.ForcedUnLog(player));
                                    user.Message(messages.Prefix() + messages.ForcedUnLogAdmin(target));
                                } else {
                                    user.Message(messages.Prefix() + messages.TargetAccessError(target));
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.PermissionError(forceUnLog.getName()));
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.UnLog());
                        }
                    } else {
                        if (player.hasPermission(forceUnLog)) {
                            user.Message(messages.Prefix() + messages.ConnectionError(args[0]));
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError(forceUnLog.getName()));
                        }
                    }
                } else {
                    user.Message(messages.Prefix() + messages.UnLog());
                }
            }
        } else {
            if (args.length == 1) {
                if (plugin.getServer().getPlayer(args[0]) != null) {
                    Player target = plugin.getServer().getPlayer(args[0]);
                    User targetUser = new User(target);

                    if (targetUser.isLogged() && !targetUser.isTempLog()) {
                        if (config.TakeBack()) {
                            LastLocation lastLocation = new LastLocation(target);
                            lastLocation.saveLocation();
                        }

                        if (config.HandleSpawn()) {
                            Spawn spawn = new Spawn();

                            targetUser.Teleport(spawn.getSpawn());
                        }

                        targetUser.setLogStatus(false);
                        new StartCheck(target, CheckType.LOGIN);
                        targetUser.Message(messages.Prefix() + messages.ForcedUnLog("SERVER"));
                        out.Message(messages.Prefix() + messages.ForcedUnLog(target));
                    } else {
                        out.Message(messages.Prefix() + messages.TargetAccessError(target));
                    }
                } else {
                    out.Message(messages.Prefix() + messages.ConnectionError(args[0]));
                }
            } else {
                out.Alert("Correct usage is unlog <player>", WarningLevel.ERROR);
            }
        }
        return false;
    }
}
