package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

public final class GoogleAuthResetCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (config.enable2FA()) {
                if (args.length == 2) {
                    String password = args[0];

                    PasswordUtils passwordUtils = new PasswordUtils(password, user.getPassword());

                    if (passwordUtils.validate()) {
                        try {
                            int code = Integer.parseInt(args[1]);

                            if (user.validateCode(code)) {
                                String newToken = user.genNewToken();

                                if (config.takeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.enableSpawn()) {
                                    Spawn spawn = new Spawn();

                                    user.teleport(spawn.getSpawn());
                                }

                                user.send(messages.prefix() + messages.reseted2FA());
                                user.setToken(newToken);
                                user.setTempLog(true);
                                user.set2FA(true);
                                user.send(messages.prefix() + messages.gAuthInstructions());
                                ComponentMaker json = new ComponentMaker(messages.gAuthLink());
                                String url = json.getURL(player, newToken);
                                json.setHoverText("&bQR Code &c( USE THE LINK BELOW IF YOU CAN'T CLICK THIS )");
                                json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                                user.send(json.getComponent());
                                user.send("&b" + url);
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
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
        return false;
    }
}
