package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
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

public final class RegisterCommand extends Command implements LockLoginBungee, BungeeFiles {

    public RegisterCommand() {
        super("register", "", "reg");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (user.isRegistered()) {
                user.Message(messages.Prefix() + messages.AlreadyRegistered());
            } else {
                if (!user.isLogged()) {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            if (Passwords.isSecure(password, player)) {
                                if (password.length() >= 4) {
                                    user.setPassword(password);
                                    user.setLogStatus(true);
                                    user.Message(messages.Prefix() + messages.Registered());

                                    if (config.EnableAuth()) {
                                        if (lobbyCheck.MainIsWorking()) {
                                            user.sendTo(lobbyCheck.getMain());
                                        }
                                    }

                                    dataSender.sendAccountStatus(player);

                                    PlayerRegisterEvent registerEvent = new PlayerRegisterEvent(player);

                                    plugin.getProxy().getPluginManager().callEvent(registerEvent);
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
                            user.Message(messages.Prefix() + messages.RegisterError());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.AlreadyRegistered());
                }
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
