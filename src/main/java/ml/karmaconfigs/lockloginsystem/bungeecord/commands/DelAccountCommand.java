package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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

public final class DelAccountCommand extends Command implements LockLoginBungee, BungeeFiles {

    public DelAccountCommand() {
        super("delaccount", "", "delacc", "delcc");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final String forceDel = "locklogin.forcedel";
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
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

                        if (plugin.getProxy().getPlayer(tar) != null) {
                            ProxiedPlayer target = plugin.getProxy().getPlayer(tar);

                            if (!target.equals(player)) {
                                User targetUser = new User(target);

                                if (config.enableAuthLobby()) {
                                    if (lobbyCheck.AuthIsWorking()) {
                                        targetUser.sendTo(lobbyCheck.getAuth());
                                    }
                                }

                                targetUser.remove();
                                targetUser.setLogStatus(false);
                                targetUser.Message(messages.Prefix() + messages.ForcedDelAccount(player));
                                user.Message(messages.Prefix() + messages.ForcedDelAccountAdmin(target));
                                new StartCheck(target, CheckType.REGISTER);
                                dataSender.sendAccountStatus(target);
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
                        user.Message(messages.Prefix() + messages.PermissionError(forceDel));
                    }
                } else {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                            if (utils.checkPW()) {
                                user.remove();
                                user.setLogStatus(false);
                                user.Message(messages.Prefix() + messages.AccountDeleted());
                                new StartCheck(player, CheckType.REGISTER);
                                dataSender.sendAccountStatus(player);
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

                if (plugin.getProxy().getPlayer(tar) != null) {
                    ProxiedPlayer target = plugin.getProxy().getPlayer(tar);
                    User targetUser = new User(target);

                    if (config.enableAuthLobby()) {
                        if (lobbyCheck.AuthIsWorking()) {
                            targetUser.sendTo(lobbyCheck.getAuth());
                        }
                    }

                    targetUser.remove();
                    targetUser.setLogStatus(false);
                    targetUser.Message(messages.Prefix() + messages.ForcedDelAccount("SERVER"));
                    Console.send(messages.Prefix() + messages.ForcedDelAccountAdmin(target));
                    new StartCheck(target, CheckType.REGISTER);
                    dataSender.sendAccountStatus(target);
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
    }
}
