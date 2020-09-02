package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.OfflineUser;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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
                                targetUser.removeServerInfo();

                                if (config.EnableAuth()) {
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

                            if (utils.PasswordIsOk()) {
                                user.removeServerInfo();
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

                    targetUser.removeServerInfo();

                    if (config.EnableAuth()) {
                        if (lobbyCheck.AuthIsWorking()) {
                            targetUser.sendTo(lobbyCheck.getAuth());
                        }
                    }

                    targetUser.remove();
                    targetUser.setLogStatus(false);
                    targetUser.Message(messages.Prefix() + messages.ForcedDelAccount("SERVER"));
                    out.Message(messages.Prefix() + messages.ForcedDelAccountAdmin(target));
                    new StartCheck(target, CheckType.REGISTER);
                    dataSender.sendAccountStatus(target);
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
    }
}
