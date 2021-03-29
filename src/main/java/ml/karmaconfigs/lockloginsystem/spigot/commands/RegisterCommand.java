package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.StringUtils;
import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.Motd;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
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
public final class RegisterCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    /**
     * The register command
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

            if (user.isRegistered()) {
                user.send(messages.prefix() + messages.alreadyRegistered());
            } else {
                if (!user.isLogged()) {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            if (Passwords.isSecure(password, player)) {
                                if (password.length() >= 4) {
                                    PlayerRegisterEvent event = new PlayerRegisterEvent(player);

                                    user.sendTitle("", "", 1, 2, 1);

                                    user.setPassword(password);
                                    user.setLogged(true);
                                    user.send(messages.prefix() + messages.registered());

                                    if (config.takeBack()) {
                                        LastLocation lastLoc = new LastLocation(player);
                                        user.teleport(lastLoc.getLastLocation());
                                    }

                                    player.setAllowFlight(user.hasFly());

                                    plugin.getServer().getPluginManager().callEvent(event);
                                    user.removeBlindEffect();

                                    File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
                                    Motd motd = new Motd(motd_file);

                                    if (motd.isEnabled())
                                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.send(motd.onRegister(player.getName(), config.serverName())), 20L * motd.getDelay());
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
                        if (args.length == 3) {
                            if (user.hasCaptcha()) {
                                String code = args[2];

                                if (StringUtils.containsLetters(code) && !config.letters()) {
                                    user.send(messages.prefix() + messages.invalidCaptcha(getInvalidChars(args[2])));
                                } else {
                                    if (user.checkCaptcha(code)) {
                                        user.send(messages.prefix() + messages.captchaValidated());
                                        player.performCommand("register " + args[0] + " " + args[1]);
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
                    user.send(messages.prefix() + messages.alreadyRegistered());
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
