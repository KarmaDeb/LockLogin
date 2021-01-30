package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
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

                        BFSystem bf_prevention = new BFSystem(player.getPendingConnection().getVirtualHost().getAddress());

                        PlayerAuthEvent event = new PlayerAuthEvent(AuthType.PASSWORD, EventAuthResult.WAITING, player, "");

                        boolean valid_password = false;
                        if (utils.PasswordIsOk()) {
                            valid_password = true;
                            if (user.hasPin()) {
                                event.setAuthResult(EventAuthResult.SUCCESS_TEMP);
                            } else {
                                if (user.has2FA()) {
                                    event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.GAuthInstructions());
                                } else {
                                    event.setAuthResult(EventAuthResult.SUCCESS, messages.Prefix() + messages.Logged(player));
                                }
                            }
                        } else {
                            event.setAuthResult(EventAuthResult.FAILED, messages.Prefix() + messages.LogError());
                        }

                        plugin.getProxy().getPluginManager().callEvent(event);

                        switch (event.getAuthResult()) {
                            case SUCCESS:
                                user.Message(event.getAuthMessage());
                                if (valid_password) {
                                    bf_prevention.success();
                                    user.setLogStatus(true);

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
                                    logger.scheduleLog(Level.WARNING, "Someone tried to force log " + player.getName() + " using event API");
                                }

                                if (Passwords.isLegacySalt(user.getPassword())) {
                                    user.setPassword(password);
                                    user.Message(messages.Prefix() + "&cYour account password was using legacy encryption and has been updated");
                                }
                                break;
                            case SUCCESS_TEMP:
                                if (valid_password) {
                                    bf_prevention.success();
                                    user.setLogStatus(true);

                                    if (Passwords.isLegacySalt(user.getPassword())) {
                                        user.setPassword(password);
                                        user.Message(messages.Prefix() + "&cYour account password was using legacy encryption and has been updated");
                                    }

                                    user.setTempLog(true);
                                    if (!user.hasPin()) {
                                        user.Message(event.getAuthMessage());
                                    } else {
                                        dataSender.openPinGUI(player);
                                    }
                                } else {
                                    logger.scheduleLog(Level.WARNING, "Someone tried to force temp log " + player.getName() + " using event API");
                                    user.Message(event.getAuthMessage());
                                }
                                break;
                            case FAILED:
                                if (bf_prevention.getTries() >= config.BFMaxTries() && config.BFMaxTries() > 0) {
                                    bf_prevention.block();
                                    bf_prevention.updateTime(config.BFBlockTime());

                                    Timer unban = new Timer();
                                    unban.schedule(new TimerTask() {
                                        final BFSystem saved_system = bf_prevention;
                                        int back = config.BFBlockTime();

                                        @Override
                                        public void run() {
                                            if (back == 0) {
                                                saved_system.unlock();
                                                cancel();
                                            }
                                            saved_system.updateTime(back);
                                            back--;
                                        }
                                    }, 0, TimeUnit.SECONDS.toMillis(1));

                                    user.Kick("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft()));
                                }
                                if (user.hasTries()) {
                                    user.restTries();
                                    user.Message(event.getAuthMessage());
                                } else {
                                    bf_prevention.fail();
                                    user.delTries();
                                    user.Kick(messages.LogError());
                                }
                                break;
                            case ERROR:
                            case WAITING:
                                user.Message(event.getAuthMessage());
                                break;
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.Login());
                    }
                } else {
                    user.Message(messages.Prefix() + messages.AlreadyLogged());
                }
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
