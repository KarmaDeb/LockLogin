package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
public final class LoginCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    /**
     * The login command
     *
     * @param sender who executes the command
     * @param cmd    the command
     * @param arg    the command arg
     * @param args   the command args
     * @return a boolean
     */
    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (user.isRegistered() && !user.isLogged()) {
                if (args.length == 1) {
                    if (!user.hasCaptcha()) {
                        String password = args[0];

                        user.authPlayer(password);
                    } else {
                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                    }
                } else {
                    if (args.length == 2) {
                        if (config.getCaptchaType().equals(CaptchaType.SIMPLE) && user.hasCaptcha()) {
                            String captcha = args[1];

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
                switch (String.valueOf(user.isRegistered())) {
                    case "true":
                        user.send(messages.prefix() + messages.alreadyLogged());
                        break;
                    case "false":
                        user.send(messages.prefix() + messages.register(user.getCaptcha()));
                        break;
                }
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }

        return false;
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
