package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Timer;
import java.util.TimerTask;

public final class StartCheck implements LockLoginSpigot, SpigotFiles {

    /**
     * Start the checker
     *
     * @param player    the player
     * @param type the check type
     */
    public StartCheck(Player player, CheckType type) {
        StartSendingMessage(player, type);

        Timer timer = new Timer();
        switch (type) {
            case REGISTER:
                timer.schedule(new TimerTask() {
                    int back = config.RegisterOut();
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            User user = new User(player);
                            if (back != 0) {
                                user.Title(messages.RegisterTitle(back), messages.RegisterSubtitle(back));
                                if (user.isRegistered()) {
                                    user.Title("", "");
                                    cancel();
                                }
                            } else {
                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.Kick(messages.RegisterOut()), 20);
                                cancel();
                            }
                            back--;
                        } else {
                            cancel();
                        }
                    }
                }, 0, 1000);
                break;
            case LOGIN:
                timer.schedule(new TimerTask() {
                    int back = config.LoginOut();
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            User user = new User(player);
                            if (back != 0) {
                                user.Title(messages.LoginTitle(back), messages.LoginSubtitle(back));
                                if (user.isLogged()) {
                                    user.Title("", "");
                                    cancel();
                                }
                            } else {
                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.Kick(messages.LoginOut()), 20);
                                cancel();
                            }
                            back--;
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
