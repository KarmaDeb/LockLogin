package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GoogleAuthResetCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (config.Enable2FA()) {
                if (args.length == 2) {
                    String password = args[0];

                    PasswordUtils passwordUtils = new PasswordUtils(password, user.getPassword());

                    if (passwordUtils.PasswordIsOk()) {
                        try {
                            int code = Integer.parseInt(args[1]);

                            if (user.validateCode(code)) {
                                String newToken = user.genNewToken();

                                if (config.TakeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.HandleSpawn()) {
                                    Spawn spawn = new Spawn();

                                    user.Teleport(spawn.getSpawn());
                                }

                                user.Message(messages.Prefix() + messages.ReseatedFA());
                                user.setToken(newToken);
                                user.setTempLog(true);
                                user.set2FA(true);
                                user.Message(messages.Prefix() + messages.GAuthInstructions());
                                ComponentMaker json = new ComponentMaker(messages.GAuthLink());
                                String url = json.getURL(player, newToken);
                                json.setHoverText("&bQR Code &c( USE THE LINK BELOW IF YOU CAN'T CLICK THIS )");
                                json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                                user.Message(json.getComponent());
                                user.Message("&b" + url);
                            } else {
                                user.Message(messages.Prefix() + messages.ToggleFAError());
                            }
                        } catch (NumberFormatException ex) {
                            user.Message(messages.Prefix() + messages.Reset2Fa());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.ToggleFAError());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.Reset2Fa());
                }
            } else {
                user.Message(messages.Prefix() + messages.GAuthDisabled());
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
