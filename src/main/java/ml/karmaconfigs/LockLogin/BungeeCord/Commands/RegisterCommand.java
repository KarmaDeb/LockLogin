package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerRegisterEvent;
import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.Passwords;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class RegisterCommand extends Command implements LockLoginBungee, BungeeFiles {

    public RegisterCommand() {
        super("register", "", "reg");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (user.isRegistered()) {
                user.Message(messages.Prefix() + messages.AlreadyRegistered());
            } else {
                if (!user.isLogged()) {
                    if (args.length == 2) {
                        String password = args[0];
                        String confirmation = args[1];

                        if (password.equals(confirmation)) {
                            if (Passwords.isSecure(password, player)) {
                                if (password.length() >= 4) {
                                    user.setPassword(password);
                                    user.setLogStatus(true);
                                    user.Message(messages.Prefix() + messages.Registered());

                                    if (config.EnableAuth()) {
                                        if (lobbyCheck.MainIsWorking()) {
                                            user.sendTo(lobbyCheck.getMain());
                                        }
                                    }

                                    dataSender.sendAccountStatus(player);

                                    PlayerRegisterEvent registerEvent = new PlayerRegisterEvent(player);

                                    plugin.getProxy().getPluginManager().callEvent(registerEvent);
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
                            user.Message(messages.Prefix() + messages.RegisterError());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.Register());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.AlreadyRegistered());
                }
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
    }
}
