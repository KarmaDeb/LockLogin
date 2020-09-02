package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory.PinInventory;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (config.EnablePins()) {
                if (!user.hasPin()) {
                    if (args.length == 1) {
                        try {
                            if (args[0].length() == 4) {
                                int pin = Integer.parseInt(args[0]);
                                user.setPin(pin);

                                user.Message(messages.Prefix() + messages.PinSet(pin));
                                user.setTempLog(true);

                                PinInventory inventory = new PinInventory(player);
                                inventory.open();
                            } else {
                                user.Message(messages.Prefix() + messages.PinLength());
                            }
                        } catch (NumberFormatException e) {
                            user.Message(messages.Prefix() + messages.PinUsage());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.PinUsage());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.AlreadyPin());
                }
            } else {
                user.Message(messages.Prefix() + messages.PinDisabled());
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
