package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
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
public final class GoogleAuthResetCommand extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize google auth reset command
     */
    public GoogleAuthResetCommand() {
        super("resetfa", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                if (user.isRegistered()) {
                    if (user.isLogged()) {
                        if (!user.isTempLog()) {
                            if (config.enable2FA()) {
                                if (args.length == 2) {
                                    String password = args[0];

                                    PasswordUtils passwordUtils = new PasswordUtils(password, user.getPassword());

                                    if (passwordUtils.validate()) {
                                        try {
                                            int code = Integer.parseInt(args[1]);

                                            if (user.validateCode(code)) {
                                                if (config.enableAuthLobby()) {
                                                    if (lobbyCheck.authWorking()) {
                                                        user.sendTo(lobbyCheck.getAuth());
                                                    }
                                                }

                                                String newToken = user.genNewToken();

                                                user.send(messages.prefix() + messages.reseted2FA());
                                                user.setToken(newToken);
                                                user.setTempLog(true);
                                                user.set2FA(true);
                                                user.send(messages.prefix() + messages.gAuthInstructions());
                                                ComponentMaker json = new ComponentMaker(messages.gAuthLink());
                                                json.setHoverText("&aQR Code");
                                                json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, json.getURL(player, newToken)));
                                                user.send(json.getComponent());

                                                dataSender.sendAccountStatus(player);
                                            } else {
                                                user.send(messages.prefix() + messages.toggle2FAError());
                                            }
                                        } catch (NumberFormatException ex) {
                                            user.send(messages.prefix() + messages.reset2FA());
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.toggle2FAError());
                                    }
                                } else {
                                    user.send(messages.prefix() + messages.reset2FA());
                                }
                            } else {
                                user.send(messages.prefix() + messages.gAuthDisabled());
                            }
                        } else {
                            user.send(messages.prefix() + messages.gAuthenticate());
                        }
                    } else {
                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                    }
                } else {
                    user.send(messages.prefix() + messages.register(user.getCaptcha()));
                }
            } else {
                user.send(messages.prefix() + messages.typeCaptcha());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
