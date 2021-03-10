package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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

public final class SetPinCommand extends Command implements LockLoginBungee, BungeeFiles {

    public SetPinCommand() {
        super("pin", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (config.pinEnabled()) {
                if (!user.hasPin()) {
                    if (args.length == 1) {
                        try {
                            if (args[0].length() == 4) {
                                user.setPin(args[0]);

                                user.send(messages.prefix() + messages.pinSet(args[0]));
                                user.setTempLog(true);

                                dataSender.openPinGUI(player);
                            } else {
                                user.send(messages.prefix() + messages.pinLength());
                            }
                        } catch (NumberFormatException e) {
                            user.send(messages.prefix() + messages.pinUsage());
                        }
                    } else {
                        user.send(messages.prefix() + messages.pinUsage());
                    }
                } else {
                    user.send(messages.prefix() + messages.alreadyPin());
                }
            } else {
                user.send(messages.prefix() + messages.pinDisabled());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
