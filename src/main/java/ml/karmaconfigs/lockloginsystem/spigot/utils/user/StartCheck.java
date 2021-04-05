package ml.karmaconfigs.lockloginsystem.spigot.utils.user;

import ml.karmaconfigs.api.bukkit.timer.AdvancedPluginTimer;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class StartCheck implements LockLoginSpigot, SpigotFiles {

    /**
     * Start the checker
     *
     * @param player the player
     * @param type   the check type
     */
    public StartCheck(Player player, CheckType type) {
        startSendingMessage(player, type);

        AdvancedPluginTimer timer = new AdvancedPluginTimer(plugin, 1, true).setAsync(false);
        switch (type) {
            case REGISTER:
                timer.addAction(new Runnable() {
                    int back = config.registerTimeOut();

                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            User user = new User(player);

                            if (!user.isRegistered()) {
                                if (back != 0) {
                                    user.sendTitle(messages.registerTitle(back), messages.registerSubtitle(back), 0, 2, 0);
                                } else {
                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.kick(messages.registerTimeOut()), 20);
                                    timer.setCancelled();
                                }

                                back--;
                            } else {
                                timer.setCancelled();
                                user.sendTitle("&a", "&b", 0, 5, 0);
                                user.removeBlindEffect();
                            }
                        } else {
                            timer.setCancelled();
                        }
                    }
                }).start();
                break;
            case LOGIN:
                timer.addAction(new Runnable() {
                    int back = config.loginTimeOut();

                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            User user = new User(player);

                            if (user.isRegistered()) {
                                if (back != 0) {
                                    if (user.isLogged()) {
                                        user.sendTitle("&a", "&b", 0, 5, 0);
                                        user.removeBlindEffect();
                                        timer.setCancelled();
                                    } else {
                                        user.sendTitle(messages.loginTitle(back), messages.loginSubtitle(back), 0, 2, 0);
                                    }
                                } else {
                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.kick(messages.loginTimeOut()), 20);
                                    timer.setCancelled();
                                }
                                back--;
                            } else {
                                user.sendTitle("&a", "&b", 0, 5, 0);
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, user::removeBlindEffect);
                                timer.setCancelled();
                            }
                        } else {
                            timer.setCancelled();
                            System.out.println("Player not online");
                        }
                    }
                }).start();
                break;
            default:
                timer.setCancelled();
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
                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                break;
            case REGISTER:
                user.send(messages.prefix() + messages.register(user.getCaptcha()));
                break;
        }
    }

    /**
     * Start sending the login message
     *
     * @param player the player
     * @param type   the message type
     */
    private void startSendingMessage(Player player, CheckType type) {
        long interval = (new User(player).isRegistered() ? config.loginInterval() : config.registerInterval());

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
        }.runTaskTimerAsynchronously(plugin, 0, 20 * interval);
    }
}
