package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Security.Passwords;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class ChangePassword extends Command implements LockLoginBungee, BungeeFiles {

    public ChangePassword() {
        super("change", "", "cpass");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (args.length == 2) {
                String oldPass = args[0];
                String newPass = args[1];

                PasswordUtils utils = new PasswordUtils(oldPass, user.getPassword());

                if (utils.PasswordIsOk()) {
                    if (!oldPass.equals(newPass)) {
                        if (Passwords.isSecure(newPass, player)) {
                            if (newPass.length() >= 4) {
                                user.removeServerInfo();
                                user.setPassword(newPass);
                                user.setLogStatus(false);
                                user.Message(messages.Prefix() + messages.ChangeDone());
                                new StartCheck(player, CheckType.LOGIN);

                                if (config.EnableAuth()) {
                                    if (lobbyCheck.AuthIsWorking()) {
                                        user.sendTo(lobbyCheck.getAuth());
                                    }
                                }
                                dataSender.sendAccountStatus(player);
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
    }
}
