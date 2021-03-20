package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
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

public final class UnlogCommand extends Command implements LockLoginBungee, BungeeFiles {

    public UnlogCommand() {
        super("unlog", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final String forceUnLog = "locklogin.forceunlog";
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 0) {
                user.setLogged(false);
                new StartCheck(player, CheckType.LOGIN);
                user.send(messages.prefix() + messages.unLogged());

                dataSender.sendAccountStatus(player);
            } else {
                if (args.length == 1) {
                    if (plugin.getProxy().getPlayer(args[0]) != null) {
                        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);

                        if (!target.equals(player)) {
                            if (player.hasPermission(forceUnLog)) {
                                User targetUser = new User(target);

                                if (targetUser.isLogged() && !targetUser.isTempLog()) {
                                    targetUser.setLogged(false);
                                    new StartCheck(target, CheckType.LOGIN);
                                    targetUser.send(messages.prefix() + messages.forcedUnLogin(player));
                                    user.send(messages.prefix() + messages.forcedUnLogAdmin(target));

                                    if (config.enableAuthLobby()) {
                                        if (lobbyCheck.authWorking()) {
                                            targetUser.sendTo(lobbyCheck.getAuth());
                                        }
                                    }

                                    dataSender.sendAccountStatus(target);
                                } else {
                                    user.send(messages.prefix() + messages.targetAccessError(target));
                                }
                            } else {
                                user.send(messages.prefix() + messages.permission(forceUnLog));
                            }
                        } else {
                            user.send(messages.prefix() + messages.unLog());
                        }
                    } else {
                        if (player.hasPermission(forceUnLog)) {
                            user.send(messages.prefix() + messages.connectionError(args[0]));
                        } else {
                            user.send(messages.prefix() + messages.permission(forceUnLog));
                        }
                    }
                } else {
                    user.send(messages.prefix() + messages.unLog());
                }
            }
        } else {
            if (args.length == 1) {
                if (plugin.getProxy().getPlayer(args[0]) != null) {
                    ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
                    User targetUser = new User(target);

                    if (targetUser.isLogged() && !targetUser.isTempLog()) {
                        targetUser.setLogged(false);
                        new StartCheck(target, CheckType.LOGIN);
                        targetUser.send(messages.prefix() + messages.forcedUnLogin(config.serverName()));
                        Console.send(messages.prefix() + messages.forcedUnLogin(target));

                        if (config.enableAuthLobby()) {
                            if (lobbyCheck.authWorking()) {
                                targetUser.sendTo(lobbyCheck.getAuth());
                            }
                        }

                        dataSender.sendAccountStatus(target);
                    } else {
                        Console.send(messages.prefix() + messages.targetAccessError(target));
                    }
                } else {
                    Console.send(messages.prefix() + messages.connectionError(args[0]));
                }
            } else {
                Console.send(plugin, "Correct usage: unlog <player>", Level.WARNING);
            }
        }
    }
}
