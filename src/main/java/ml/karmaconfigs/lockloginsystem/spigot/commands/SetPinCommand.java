package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
public final class SetPinCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    /**
     * The pin set command
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

            if (config.enablePin()) {
                if (!user.hasPin()) {
                    if (args.length == 1) {
                        try {
                            if (args[0].length() == 4) {
                                user.setPin(args[0]);

                                user.send(messages.prefix() + messages.pinSet(args[0]));
                                user.setTempLog(true);

                                PinInventory inventory = new PinInventory(player);
                                inventory.open();
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
        return false;
    }
}
