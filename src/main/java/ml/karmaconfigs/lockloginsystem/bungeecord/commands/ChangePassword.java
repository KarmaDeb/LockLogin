package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
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

public final class ChangePassword extends Command implements LockLoginBungee, BungeeFiles {

    public ChangePassword() {
        super("change", "", "cpass");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 2) {
                String oldPass = args[0];
                String newPass = args[1];

                PasswordUtils utils = new PasswordUtils(oldPass, user.getPassword());

                if (utils.validate()) {
                    if (!oldPass.equals(newPass)) {
                        if (Passwords.isSecure(newPass, player)) {
                            if (newPass.length() >= 4) {
                                user.setPassword(newPass);
                                user.setLogStatus(false);
                                user.Message(messages.Prefix() + messages.ChangeDone());
                                new StartCheck(player, CheckType.LOGIN);

                                if (config.enableAuthLobby()) {
                                    if (lobbyCheck.AuthIsWorking()) {
                                        user.sendTo(lobbyCheck.getAuth());
                                    }
                                }
                                dataSender.sendAccountStatus(player);
                            } else {
                                user.Message(messages.Prefix() + messages.PasswordMinChar());
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.PasswordInsecure());

                            ComponentMaker json = new ComponentMaker(messages.Prefix() + " &bClick here to generate a secure password");
                            json.setHoverText("&7Opens an url to a password-gen page");
                            json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://karmaconfigs.ml/password/"));

                            user.Message(json.getComponent());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.ChangeSame());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.ChangeError());
                }
            } else {
                if (user.isLogged()) {
                    user.Message(messages.Prefix() + messages.ChangePass());
                } else {
                    if (user.isRegistered()) {
                        user.Message(messages.Prefix() + messages.Login());
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                }
            }
        } else {
            /*if (args.length == 2) {
                Coming soon...
            } else {
                Console.send(plugin, "Correct usage: change <player> <password>", Level.WARNING);
            }*/
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
