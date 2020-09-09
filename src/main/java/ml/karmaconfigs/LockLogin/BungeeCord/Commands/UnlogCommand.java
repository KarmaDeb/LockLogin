package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.WarningLevel;
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
                user.removeServerInfo();
                user.setLogStatus(false);
                new StartCheck(player, CheckType.LOGIN);
                user.Message(messages.Prefix() + messages.UnLogged());

                dataSender.sendAccountStatus(player);
            } else {
                if (args.length == 1) {
                    if (plugin.getProxy().getPlayer(args[0]) != null) {
                        ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);

                        if (!target.equals(player)) {
                            if (player.hasPermission(forceUnLog)) {
                                User targetUser = new User(target);

                                if (targetUser.isLogged() && !targetUser.isTempLog()) {
                                    targetUser.removeServerInfo();
                                    targetUser.setLogStatus(false);
                                    new StartCheck(target, CheckType.LOGIN);
                                    targetUser.Message(messages.Prefix() + messages.ForcedUnLog(player));
                                    user.Message(messages.Prefix() + messages.ForcedUnLogAdmin(target));

                                    if (config.EnableAuth()) {
                                        if (lobbyCheck.AuthIsWorking()) {
                                            targetUser.sendTo(lobbyCheck.getAuth());
                                        }
                                    }

                                    dataSender.sendAccountStatus(target);
                                } else {
                                    user.Message(messages.Prefix() + messages.TargetAccessError(target));
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.PermissionError(forceUnLog));
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.UnLog());
                        }
                    } else {
                        if (player.hasPermission(forceUnLog)) {
                            user.Message(messages.Prefix() + messages.ConnectionError(args[0]));
                        } else {
                            user.Message(messages.Prefix() + messages.PermissionError(forceUnLog));
                        }
                    }
                } else {
                    user.Message(messages.Prefix() + messages.UnLog());
                }
            }
        } else {
            if (args.length == 1) {
                if (plugin.getProxy().getPlayer(args[0]) != null) {
                    ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
                    User targetUser = new User(target);

                    if (targetUser.isLogged() && !targetUser.isTempLog()) {
                        targetUser.removeServerInfo();
                        targetUser.setLogStatus(false);
                        new StartCheck(target, CheckType.LOGIN);
                        targetUser.Message(messages.Prefix() + messages.ForcedUnLog(config.ServerName()));
                        out.Message(messages.Prefix() + messages.ForcedUnLog(target));

                        if (config.EnableAuth()) {
                            if (lobbyCheck.AuthIsWorking()) {
                                targetUser.sendTo(lobbyCheck.getAuth());
                            }
                        }

                        dataSender.sendAccountStatus(target);
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
    }
}
