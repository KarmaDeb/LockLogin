package ml.karmaconfigs.lockloginsystem.bungeecord.utils.user;

import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

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

public final class StartCheck implements LockLoginBungee, BungeeFiles {

    private final BungeeSender dataSender = new BungeeSender();
    private ScheduledTask task;
    private ScheduledTask msTask;
    private int back;

    /**
     * Start the checker
     *
     * @param p    the player
     * @param type the check type
     */
    public StartCheck(ProxiedPlayer p, CheckType type) {
        StartSendingMessage(p, type);
        switch (type) {
            case REGISTER:
                back = config.MaxRegister();
                task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
                    if (p != null) {
                        if (p.isConnected()) {
                            User user = new User(p);
                            if (back != 0) {
                                dataSender.sendUUID(p.getUniqueId(), p.getServer());
                                if (!user.isRegistered()) {
                                    dataSender.sendAccountStatus(p);
                                    user.sendTitle(messages.RegisterTitle(back), messages.RegisterSubtitle(back));
                                    user.checkServer();
                                } else {
                                    user.sendTitle("", "");
                                    task.cancel();
                                }
                            } else {
                                user.Kick(messages.RegisterOut());
                                task.cancel();
                            }
                            back--;
                        } else {
                            task.cancel();
                        }
                    } else {
                        task.cancel();
                    }
                }, 0, 1, TimeUnit.SECONDS);
                break;
            case LOGIN:
                back = config.MaxLogin();
                task = plugin.getProxy().getScheduler().schedule(plugin, () -> {
                    if (p != null) {
                        if (p.isConnected()) {
                            User user = new User(p);
                            if (back != 0) {
                                dataSender.sendUUID(p.getUniqueId(), p.getServer());
                                if (!user.isLogged()) {
                                    dataSender.sendAccountStatus(p);
                                    user.sendTitle(messages.LoginTitle(back), messages.LoginSubtitle(back));
                                    user.checkServer();
                                } else {
                                    user.sendTitle("", "");
                                    task.cancel();
                                }
                            } else {
                                user.Kick(messages.LoginOut());
                                task.cancel();
                            }
                            back--;
                        } else {
                            task.cancel();
                        }
                    } else {
                        task.cancel();
                    }
                }, 0, 1, TimeUnit.SECONDS);
                break;
        }
    }

    /**
     * Send the auth message
     *
     * @param player the player
     * @param type   the auth message type
     */
    private void sendAuthMessage(ProxiedPlayer player, CheckType type) {
        User user = new User(player);
        switch (type) {
            case LOGIN:
                user.Message(messages.Prefix() + messages.Login());
                break;
            case REGISTER:
                user.Message(messages.Prefix() + messages.Register());
                break;
        }
    }

    /**
     * Start sending the login message
     *
     * @param player the player
     * @param type   the message type
     */
    private void StartSendingMessage(ProxiedPlayer player, CheckType type) {
        msTask = plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User user = new User(player);
            if (!user.isLogged()) {
                if (type.equals(CheckType.LOGIN)) {
                    sendAuthMessage(player, CheckType.LOGIN);
                } else {
                    sendAuthMessage(player, CheckType.REGISTER);
                }
            } else {
                msTask.cancel();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
