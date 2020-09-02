package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class GoogleAuthResetCommand extends Command implements LockLoginBungee, BungeeFiles {

    public GoogleAuthResetCommand() {
        super("resetfa", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (user.isRegistered()) {
                if (user.isLogged()) {
                    if (!user.isTempLog()) {
                        if (config.Enable2FA()) {
                            if (args.length == 2) {
                                String password = args[0];

                                PasswordUtils passwordUtils = new PasswordUtils(password, user.getPassword());

                                if (passwordUtils.PasswordIsOk()) {
                                    try {
                                        int code = Integer.parseInt(args[1]);

                                        if (user.validateCode(code)) {
                                            if (config.EnableAuth()) {
                                                if (lobbyCheck.AuthIsWorking()) {
                                                    user.sendTo(lobbyCheck.getAuth());
                                                }
                                            }

                                            String newToken = user.genNewToken();

                                            user.Message(messages.Prefix() + messages.ReseatedFA());
                                            user.setToken(newToken);
                                            user.setTempLog(true);
                                            user.set2FA(true);
                                            user.Message(messages.Prefix() + messages.GAuthInstructions());
                                            ComponentMaker json = new ComponentMaker(messages.GAuthLink());
                                            json.setHoverText("&aQR Code");
                                            json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, json.getURL(player, newToken)));
                                            user.Message(json.getComponent());

                                            dataSender.sendAccountStatus(player);
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
                        user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.Login());
                }
            } else {
                user.Message(messages.Prefix() + messages.Register());
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
    }
}
