package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginapi.bungee.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.Motd;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
public final class RegisterCommand extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize register command
     */
    public RegisterCommand() {
        super("register", "", "reg");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (user.isRegistered()) {
                user.send(messages.prefix() + messages.alreadyRegister());
            } else {
                if (!user.isLogged()) {
                    if (args.length == 2) {
                        if (!user.hasCaptcha()) {
                            String password = args[0];
                            String confirmation = args[1];

                            if (password.equals(confirmation)) {
                                if (Passwords.isSecure(password, player)) {
                                    if (password.length() >= 4) {
                                        user.setPassword(password);
                                        user.setLogged(true);
                                        user.send(messages.prefix() + messages.registered());
                                        user.checkServer();

                                        dataSender.sendAccountStatus(player);

                                        PlayerRegisterEvent registerEvent = new PlayerRegisterEvent(player);

                                        plugin.getProxy().getPluginManager().callEvent(registerEvent);

                                        File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
                                        Motd motd = new Motd(motd_file);

                                        if (motd.isEnabled())
                                            plugin.getProxy().getScheduler().schedule(plugin, () -> user.send(motd.onRegister(player.getName(), config.serverName())), motd.getDelay(), TimeUnit.SECONDS);
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
                                user.send(messages.prefix() + messages.registerError());
                            }
                        } else {
                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                        }
                    } else {
                        if (args.length == 3) {
                            if (config.getCaptchaType().equals(CaptchaType.SIMPLE) && user.hasCaptcha()) {
                                String captcha = args[2];

                                if (StringUtils.containsLetters(captcha) && !config.letters()) {
                                    user.send(messages.prefix() + messages.invalidCaptcha(getInvalidChars(args[2])));
                                } else {
                                    if (user.checkCaptcha(captcha)) {
                                        user.send(messages.prefix() + messages.captchaValidated());
                                        player.chat("/register " + args[0] + " " + args[1]);
                                    } else {
                                        user.send(messages.prefix() + messages.invalidCaptcha());
                                    }
                                }
                            } else {
                                user.send(messages.prefix() + messages.register(user.getCaptcha()));
                            }
                        } else {
                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                        }
                    }
                } else {
                    user.send(messages.prefix() + messages.alreadyRegister());
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
