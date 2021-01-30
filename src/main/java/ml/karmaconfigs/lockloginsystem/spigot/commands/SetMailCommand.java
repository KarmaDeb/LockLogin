package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Mailer;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

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

public final class SetMailCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

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

            Mailer mailer = new Mailer();
            if (isValidEmailAddress(mailer.getEmail())) {
                if (args.length == 1) {
                    String email = args[0];

                    if (isValidEmailAddress(email)) {
                        user.setEmail(email, false);
                        user.Message(messages.Prefix() + messages.emailSet(email));
                    } else {
                        user.Message(messages.Prefix() + messages.invalidEmail());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.invalidEmail());
                }
            } else {
                user.Message(messages.Prefix() + messages.emailDisabled());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }

        return false;
    }

    private boolean isValidEmailAddress(final String email) {
        boolean result = true;
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (Throwable ex) {
            result = false;
        }
        return result;
    }
}
