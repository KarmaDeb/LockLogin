package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
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

public final class LoginCommand extends Command implements LockLoginBungee, BungeeFiles {

    public LoginCommand() {
        super("login", "", "l");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (!user.isRegistered()) {
                user.send(messages.prefix() + messages.register(user.getCaptcha()));
            } else {
                if (!user.isLogged()) {
                    if (args.length == 1) {
                        if (!user.hasCaptcha()) {
                            String password = args[0];

                            user.authPlayer(password);
                        } else {
                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                        }
                    } else {
                        if (args.length == 2) {
                            String captcha = args[1];

                            if (config.getCaptchaType().equals(CaptchaType.SIMPLE) && user.hasCaptcha()) {
                                if (StringUtils.containsLetters(captcha) && !config.letters()) {
                                    user.send(messages.prefix() + messages.invalidCaptcha(getInvalidChars(args[1])));
                                } else {
                                    if (user.checkCaptcha(captcha)) {
                                        user.send(messages.prefix() + messages.captchaValidated());
                                        user.authPlayer(args[0]);
                                    } else {
                                        user.send(messages.prefix() + messages.invalidCaptcha());
                                    }
                                }
                            } else {
                                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                            }
                        } else {
                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                        }
                    }
                } else {
                    user.send(messages.prefix() + messages.alreadyLogged());
                }
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }

    private String getInvalidChars(final String str) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char character = str.charAt(i);
            if (!Character.isDigit(character))
                builder.append("'").append(character).append("'").append((i != str.length() - 1 ? ", " : ""));
        }

        return builder.toString();
    }
}
