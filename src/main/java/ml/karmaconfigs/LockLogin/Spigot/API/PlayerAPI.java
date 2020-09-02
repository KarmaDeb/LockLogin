package ml.karmaconfigs.LockLogin.Spigot.API;

import ml.karmaconfigs.LockLogin.IPStorage.IPStorager;
import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory.PinInventory;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("unused")
public final class PlayerAPI implements LockLoginSpigot, SpigotFiles {

    private final Player player;

    /**
     * Initialize LockLogin bungee's API
     *
     * @param player the player
     */
    public PlayerAPI(Player player) {
        this.player = player;
    }

    /**
     * Mark the player as logged/un-logged with a message
     *
     * @param Value   true/false
     * @param Message the message
     */
    public final void setLogged(boolean Value, String Message) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            User utils = new User(player);
            if (Value) {
                if (utils.has2FA()) {
                    utils.setLogStatus(true);
                    utils.setTempLog(true);
                    utils.Message(Message);
                    utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                } else {
                    utils.setLogStatus(true);
                    utils.setTempLog(false);
                    utils.Message(Message);
                }
            } else {
                utils.setLogStatus(false);
                utils.setTempLog(false);
                utils.Message(Message);
            }
        }, (long) (20 * 1.5));
    }

    /**
     * Mark the player as registered/not registered
     */
    public final void unRegister() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            User utils = new User(player);
            utils.Kick("&eLockLogin" + "\n\n" + messages.AccountDeleted());
            utils.setLogStatus(false);
            utils.setTempLog(utils.has2FA());
            utils.setPassword("");
        }, (long) (20 * 1.5));
    }

    /**
     * Registers a player with the specified password
     *
     * @param password the password
     */
    public final void register(String password) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            User utils = new User(player);
            utils.setLogStatus(true);
            utils.setTempLog(utils.has2FA());
            utils.setPassword(password);
            utils.Message(messages.Prefix() + messages.Registered());
            utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
        }, (long) (20 * 1.5));
    }

    /**
     * Rest a trie left for the player
     */
    public final void restTrie() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            User utils = new User(player);
            utils.restTries();
        }, (long) (20 * 1.5));
    }

    /**
     * Mark the player as logged/un-logged
     *
     * @param Value true/false
     */
    public final void setLogged(boolean Value) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            User utils = new User(player);
            if (Value) {
                if (utils.has2FA()) {
                    utils.setLogStatus(true);
                    utils.setTempLog(true);
                    utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                } else {
                    utils.setLogStatus(true);
                    utils.setLogStatus(false);
                }
            } else {
                utils.setLogStatus(false);
                utils.setTempLog(false);
            }
        }, (long) (20 * 1.5));
    }

    /**
     * Checks if the player is logged or not
     *
     * @return a boolean
     */
    public final boolean isLogged() {
        User utils = new User(player);
        if (utils.has2FA()) {
            return utils.isLogged() && !utils.isTempLog();
        }
        return utils.isLogged();
    }

    /**
     * Check if the player is registered
     *
     * @return a boolean
     */
    public final boolean isRegistered() {
        User utils = new User(player);
        return utils.isRegistered();
    }

    /**
     * Check if the player has tries left
     *
     * @return a boolean
     */
    public final boolean hasTries() {
        User utils = new User(player);
        return utils.hasTries();
    }

    /**
     * Check if the user has a pin
     *
     * @return a boolean
     */
    public final boolean hasPin() {
        User user = new User(player);
        return user.hasPin();
    }

    /**
     * Check if the user is verified with
     * his pin
     *
     * @return a boolean
     */
    public final boolean isPinVerified() {
        if (hasPin()) {
            PinInventory inventory = new PinInventory(player);

            return inventory.isVerified();
        } else {
            return true;
        }
    }

    /**
     * Gets the player country name
     *
     * @return a string
     * <code>This won't return any
     * util information</code>
     */
    @Deprecated
    public final String[] getCountry() {
        return new String[]{"REMOVED", "IN 3.0.2"};
    }

    /**
     * Get the player registered
     * accounts
     *
     * @return a list of strings
     */
    public final List<String> getAccounts() {
        return IPStorager.getStorage(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName(), false);
    }

    /**
     * Get the player connections
     *
     * @return an integer
     */
    public final int getConnections() {
        IpData data = new IpData(player.getAddress().getAddress());

        return data.getConnections();
    }
}
