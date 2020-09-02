package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class SetPinCommand extends Command implements LockLoginBungee, BungeeFiles {

    public SetPinCommand() {
        super("pin", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
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

                                dataSender.openPinGUI(player);
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
    }
}
