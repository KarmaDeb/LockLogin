package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Security.Passwords;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChangePassword implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (args.length == 2) {
                String oldPass = args[0];
                String newPass = args[1];

                PasswordUtils utils = new PasswordUtils(oldPass, user.getPassword());

                if (utils.PasswordIsOk()) {
                    if (!oldPass.equals(newPass)) {
                        if (Passwords.isSecure(newPass, player)) {
                            if (newPass.length() >= 4) {
                                user.setPassword(newPass);

                                if (config.TakeBack()) {
                                    LastLocation lastLocation = new LastLocation(player);
                                    lastLocation.saveLocation();
                                }

                                if (config.HandleSpawn()) {
                                    Spawn spawn = new Spawn();

                                    user.Teleport(spawn.getSpawn());
                                }

                                user.setLogStatus(false);
                                user.Message(messages.Prefix() + messages.ChangeDone());
                                new StartCheck(player, CheckType.LOGIN);
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
                        user.Message(messages.Prefix() + messages.ChangeSame());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.ChangeError());
                }
            } else {
                if (user.isLogged()) {
                    user.Message(messages.Prefix() + messages.ChangePass());
                } else {
                    if (user.isRegistered()) {
                        user.Message(messages.Prefix() + messages.Login());
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                }
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
