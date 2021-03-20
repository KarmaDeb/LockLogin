package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class CaptchaCommand extends Command implements LockLoginBungee, BungeeFiles {

    public CaptchaCommand() {
        super("captcha");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (config.getCaptchaType().equals(CaptchaType.COMPLEX)) {
                if (user.hasCaptcha()) {
                    if (args.length == 0) {
                        user.send(messages.prefix() + messages.specifyCaptcha());
                    } else {
                        String captcha = args[0];

                        if (StringUtils.containsLetters(captcha) && !config.letters()) {
                            user.send(messages.prefix() + messages.invalidCaptcha(getInvalidChars(args[0])));
                        } else {
                            if (user.checkCaptcha(captcha)) {
                                user.send(messages.prefix() + messages.captchaValidated());
                                if (config.isYaml())
                                    user.setupFile();

                                if (user.isRegistered())
                                    new StartCheck(player, CheckType.LOGIN);
                                else
                                    new StartCheck(player, CheckType.REGISTER);
                            } else
                                user.send(messages.prefix() + messages.invalidCaptcha());
                        }
                    }
                } else {
                    user.send(messages.prefix() + messages.alreadyCaptcha());
                }
            } else {
                if (user.isLogged())
                    user.send(messages.prefix() + messages.alreadyLogged());
                else if (!user.isTempLog())
                    if (user.isRegistered())
                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                    else
                        user.send(messages.prefix() + messages.register(user.getCaptcha()));
                else
                if (user.has2FA())
                    user.send(messages.prefix() + messages.gAuthenticate());
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
