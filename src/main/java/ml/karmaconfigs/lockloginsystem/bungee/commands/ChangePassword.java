package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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
public final class ChangePassword extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize password change command
     */
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
                                user.setLogged(false);
                                user.send(messages.prefix() + messages.changeDone());
                                new StartCheck(player, CheckType.LOGIN);

                                if (config.enableAuthLobby()) {
                                    if (lobbyCheck.authWorking()) {
                                        user.sendTo(lobbyCheck.getAuth());
                                    }
                                }
                                dataSender.sendAccountStatus(player);
                            } else {
                                user.send(messages.prefix() + messages.passwordMinChar());
                            }
                        } else {
                            user.send(messages.prefix() + messages.passwordInsecure());

                            ComponentMaker json = new ComponentMaker(messages.prefix() + " &bClick here to generate a secure password");
                            json.setHoverText("&7Opens an url to a password-gen page");
                            json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://karmaconfigs.ml/password/"));

                            user.send(json.getComponent());
                        }
                    } else {
                        user.send(messages.prefix() + messages.changeSame());
                    }
                } else {
                    user.send(messages.prefix() + messages.changeError());
                }
            } else {
                if (user.isLogged()) {
                    user.send(messages.prefix() + messages.changePass());
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
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
