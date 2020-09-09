package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerVerifyEvent;
import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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

public final class LoginCommand extends Command implements LockLoginBungee, BungeeFiles {

    public LoginCommand() {
        super("login", "", "l");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (!user.isRegistered()) {
                user.Message(messages.Prefix() + messages.Register());
            } else {
                if (!user.isLogged()) {
                    if (args.length == 1) {
                        String password = args[0];
                        PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                        if (utils.PasswordIsOk()) {
                            user.setLogStatus(true);
                            if (!user.hasPin()) {
                                PlayerVerifyEvent event = new PlayerVerifyEvent(player);

                                plugin.getProxy().getPluginManager().callEvent(event);

                                if (event.isCancelled()) {
                                    user.Message(messages.Prefix() + event.getCancelMessage());
                                } else {
                                    user.Message(messages.Prefix() + messages.Logged(player));

                                    if (!user.has2FA()) {
                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (config.EnableMain()) {
                                                    if (lobbyCheck.MainIsWorking()) {
                                                        user.sendTo(lobbyCheck.getMain());
                                                    }
                                                }
                                            }
                                        }, TimeUnit.SECONDS.toMillis(1));

                                        dataSender.sendAccountStatus(player);
                                    } else {
                                        user.setTempLog(true);
                                        user.Message(messages.GAuthInstructions());
                                    }
                                }
                            } else {
                                user.setTempLog(true);
                                dataSender.openPinGUI(player);
                            }
                            dataSender.blindEffect(player, false);
                        } else {
                            if (user.hasTries()) {
                                user.restTries();
                                user.Message(messages.Prefix() + messages.LogError());
                            } else {
                                user.delTries();
                                user.Kick(messages.LogError());
                            }
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.Login());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.AlreadyLogged());
                }
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
    }
}
