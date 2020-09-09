package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
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
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (user.isRegistered() && !user.isLogged()) {
                if (args.length == 1) {
                    String password = args[0];

                    user.authPlayer(password);
                } else {
                    user.Message(messages.Prefix() + messages.Login());
                }
            } else {
                switch (String.valueOf(user.isRegistered())) {
                    case "true":
                        user.Message(messages.Prefix() + messages.AlreadyLogged());
                        break;
                    case "false":
                        user.Message(messages.Prefix() + messages.Register());
                        break;
                }
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }

        return false;
    }
}
