package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.Mailer;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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

public final class SetMailCommand extends Command implements LockLoginBungee, BungeeFiles {

    public SetMailCommand() {
        super("setmail");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
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
