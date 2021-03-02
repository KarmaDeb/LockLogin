package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.StartCheck;
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

public final class ChangePassword implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
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

                                if (config.TakeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.enableSpawn()) {
                                    Spawn spawn = new Spawn();

                                    user.teleport(spawn.getSpawn());
                                }

                                user.setLogStatus(false);
                                user.send(messages.Prefix() + messages.ChangeDone());
                                new StartCheck(player, CheckType.LOGIN);
                            } else {
                                user.send(messages.Prefix() + messages.PasswordMinChar());
                            }
                        } else {
                            user.send(messages.Prefix() + messages.PasswordInsecure());

                            ComponentMaker json = new ComponentMaker(messages.Prefix() + " &bClick here to generate a secure password");
                            json.setHoverText("&7Opens an url to a password-gen page");
                            json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://karmaconfigs.ml/password/"));

                            user.send(json.getComponent());
                        }
                    } else {
                        user.send(messages.Prefix() + messages.ChangeSame());
                    }
                } else {
                    user.send(messages.Prefix() + messages.ChangeError());
                }
            } else {
                if (user.isLogged()) {
                    user.send(messages.Prefix() + messages.ChangePass());
                } else {
                    if (user.isRegistered()) {
                        user.send(messages.Prefix() + messages.Login());
                    } else {
                        user.send(messages.Prefix() + messages.Register());
                    }
                }
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
        return false;
    }
}
