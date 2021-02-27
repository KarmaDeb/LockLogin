package ml.karmaconfigs.lockloginsystem.spigot.utils.user;

import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Timer;
import java.util.TimerTask;

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

public final class StartCheck implements LockLoginSpigot, SpigotFiles {

    /**
     * Start the checker
     *
     * @param player the player
     * @param type   the check type
     */
    public StartCheck(Player player, CheckType type) {
        StartSendingMessage(player, type);

        Timer timer = new Timer();
        switch (type) {
            case REGISTER:
                timer.schedule(new TimerTask() {
                    int back = config.registerTimeOut();

                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            User user = new User(player);
                            if (!user.isLogged()) {
                                if (back != 0) {
                                    if (user.isRegistered()) {
                                        user.sendTitle("", "", 0, 5, 0);
                                        cancel();
                                    } else {
                                        user.sendTitle(messages.RegisterTitle(back), messages.RegisterSubtitle(back), 0, 2, 0);
                                    }
                                } else {
                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.Kick(messages.RegisterOut()), 20);
                                    cancel();
                                }
                                back--;
                            } else {
                                cancel();
                            }
                        } else {
                            cancel();
                        }
                    }
                }, 0, 1000);
                break;
            case LOGIN:
                timer.schedule(new TimerTask() {
                    int back = config.loginTimeOut();

                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            User user = new User(player);
                            if (user.isRegistered()) {
                                if (back != 0) {
                                    if (user.isLogged()) {
                                        user.sendTitle("", "", 0, 5, 0);
                                        cancel();
                                    } else {
                                        user.sendTitle(messages.LoginTitle(back), messages.LoginSubtitle(back), 0, 2, 0);
                                    }
                                } else {
                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.Kick(messages.LoginOut()), 20);
                                    cancel();
                                }
                                back--;
                            } else {
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                    if (config.blindLogin())
                                        user.removeBlindEffect(config.nauseaLogin());
                                });

                                cancel();
                            }
                        } else {
                            cancel();
                        }
                    }
                }, 0, 1000);
                break;
        }
    }

    /**
     * Send the auth message
     *
     * @param player the player
     * @param type   the auth message type
     */
    private void sendAuthMessage(Player player, CheckType type) {
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
    private void StartSendingMessage(Player player, CheckType type) {
        new BukkitRunnable() {
            @Override
            public void run() {
                User user = new User(player);

                if (!user.isLogged()) {
                    sendAuthMessage(player, type);
                } else {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20 * 5);
    }
}
