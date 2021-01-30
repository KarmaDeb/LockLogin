package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
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

public final class GoogleAuthResetCommand extends Command implements LockLoginBungee, BungeeFiles {

    public GoogleAuthResetCommand() {
        super("resetfa", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (user.isRegistered()) {
                if (user.isLogged()) {
                    if (!user.isTempLog()) {
                        if (config.Enable2FA()) {
                            if (args.length == 2) {
                                String password = args[0];

                                PasswordUtils passwordUtils = new PasswordUtils(password, user.getPassword());

                                if (passwordUtils.PasswordIsOk()) {
                                    try {
                                        int code = Integer.parseInt(args[1]);

                                        if (user.validateCode(code)) {
                                            if (config.EnableAuth()) {
                                                if (lobbyCheck.AuthIsWorking()) {
                                                    user.sendTo(lobbyCheck.getAuth());
                                                }
                                            }

                                            String newToken = user.genNewToken();

                                            user.Message(messages.Prefix() + messages.ReseatedFA());
                                            user.setToken(newToken);
                                            user.setTempLog(true);
                                            user.set2FA(true);
                                            user.Message(messages.Prefix() + messages.GAuthInstructions());
                                            ComponentMaker json = new ComponentMaker(messages.GAuthLink());
                                            json.setHoverText("&aQR Code");
                                            json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, json.getURL(player, newToken)));
                                            user.Message(json.getComponent());

                                            dataSender.sendAccountStatus(player);
                                        } else {
                                            user.Message(messages.Prefix() + messages.ToggleFAError());
                                        }
                                    } catch (NumberFormatException ex) {
                                        user.Message(messages.Prefix() + messages.Reset2Fa());
                                    }
                                } else {
                                    user.Message(messages.Prefix() + messages.ToggleFAError());
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.Reset2Fa());
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.GAuthDisabled());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.Login());
                }
            } else {
                user.Message(messages.Prefix() + messages.Register());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
