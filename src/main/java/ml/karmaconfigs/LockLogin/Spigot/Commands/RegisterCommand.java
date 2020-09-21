package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.Passwords;
import ml.karmaconfigs.LockLogin.Spigot.API.Events.PlayerRegisterEvent;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
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
                                    PlayerRegisterEvent event = new PlayerRegisterEvent(player);

                                    user.setPassword(password);
                                    user.setLogStatus(true);
                                    user.Message(messages.Prefix() + messages.Registered());

                                    if (config.TakeBack()) {
                                        LastLocation lastLoc = new LastLocation(player);
                                        user.Teleport(lastLoc.getLastLocation());
                                    }

                                    player.setAllowFlight(user.hasFly());

                                    plugin.getServer().getPluginManager().callEvent(event);
                                    if (config.RegisterBlind()) {
                                        user.removeBlindEffect(config.RegisterNausea());
                                    }
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
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
