package ml.karmaconfigs.lockloginsystem.bukkit.commands;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

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
public final class DelAccountCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    Permission forceDel = new Permission("locklogin.forcedel", PermissionDefault.FALSE);

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 0) {
                if (user.isLogged()) {
                    user.send(messages.prefix() + messages.deleteAccount());
                } else {
                    if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                        if (user.isRegistered()) {
                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                        } else {
                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                        }
                    } else {
                        user.send(messages.prefix() + messages.typeCaptcha());
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

                                if (config.takeBack()) {
                                    LastLocation lastLocation = new LastLocation(target);
                                    lastLocation.saveLocation();
                                }

                                if (config.enableSpawn()) {
                                    Spawn spawn = new Spawn();

                                    targetUser.teleport(spawn.getSpawn());
                                }

                                targetUser.remove();
                                targetUser.setLogged(false);
                                targetUser.setTempLog(false);
                                targetUser.send(messages.prefix() + messages.forceDelAccount(player));
                                user.send(messages.prefix() + messages.forceDelAccountAdmin(target));
                                new StartCheck(target, CheckType.REGISTER);
                            } else {
                                user.send(messages.prefix() + messages.deleteAccount());
                            }
                        } else {
                            OfflineUser targetUser = new OfflineUser("", tar, true);

                            if (targetUser.exists()) {
                                targetUser.delete();
                                user.send(messages.prefix() + messages.forceDelAccountAdmin(tar));
                            } else {
                                user.send(messages.prefix() + messages.unknownPlayer(tar));
                            }
                        }
                    } else {
                        user.send(messages.prefix() + messages.permission(forceDel.getName()));
                    }
                } else {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                            if (utils.validate()) {
                                if (config.takeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.enableSpawn()) {
                                    Spawn spawn = new Spawn();

                                    user.teleport(spawn.getSpawn());
                                }

                                user.remove();
                                user.setLogged(false);
                                user.send(messages.prefix() + messages.accountDeleted());
                                new StartCheck(player, CheckType.REGISTER);
                            } else {
                                user.send(messages.prefix() + messages.deleteAccError());
                            }
                        } else {
                            user.send(messages.prefix() + messages.deleteAccMatch());
                        }
                    } else {
                        user.send(messages.prefix() + messages.deleteAccount());
                    }
                }
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                if (plugin.getServer().getPlayer(tar) != null) {
                    Player target = plugin.getServer().getPlayer(tar);
                    User targetUser = new User(target);

                    if (config.takeBack()) {
                        LastLocation lastLocation = new LastLocation(target);
                        lastLocation.saveLocation();
                    }

                    if (config.enableSpawn()) {
                        Spawn spawn = new Spawn();

                        targetUser.teleport(spawn.getSpawn());
                    }

                    targetUser.remove();
                    targetUser.setLogged(false);
                    targetUser.send(messages.prefix() + messages.forceDelAccount("SERVER"));
                    Console.send(messages.prefix() + messages.forceDelAccountAdmin(target));
                    new StartCheck(target, CheckType.REGISTER);
                } else {
                    OfflineUser targetUser = new OfflineUser("", tar, true);

                    if (targetUser.exists()) {
                        targetUser.delete();
                        Console.send(messages.prefix() + messages.forceDelAccountAdmin(tar));
                    } else {
                        Console.send(messages.prefix() + messages.unknownPlayer(tar));
                    }
                }
            } else {
                Console.send(plugin, "Correct usage: delacc <player>", Level.WARNING);
            }
        }
        return false;
    }
}
