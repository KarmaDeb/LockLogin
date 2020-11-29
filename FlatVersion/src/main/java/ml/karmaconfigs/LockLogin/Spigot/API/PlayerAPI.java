package ml.karmaconfigs.LockLogin.Spigot.API;

import ml.karmaconfigs.LockLogin.Spigot.API.Events.PlayerRegisterEvent;
import ml.karmaconfigs.LockLogin.Spigot.API.Events.PlayerVerifyEvent;
import ml.karmaconfigs.lockloginsystem.shared.AuthResult;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;

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

/**
 * @deprecated Now in package:
 * ml.karmaconfigs.lockloginsystem.spigot.api
 *
 * It's also insecure for server owners since it has
 * no control over who calls it
 */
@Deprecated
public final class PlayerAPI implements LockLoginSpigot, SpigotFiles {

    private final Player player;
    private AuthResult result = AuthResult.IDLE;

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
     * Try to log the player
     *
     * @param value the value
     * @param message the login message
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value, String message) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player != null && !player.isOnline()) {
                User utils = new User(player);
                if (value) {
                    PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                    plugin.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        if (utils.has2FA()) {
                            utils.setLogStatus(true);
                            utils.setTempLog(true);
                            utils.Message(message);
                            utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                            result = AuthResult.SUCCESS_TEMP;
                        } else {
                            utils.setLogStatus(true);
                            utils.setTempLog(false);
                            utils.Message(message);
                            result = AuthResult.SUCCESS;
                        }
                    } else {
                        result = AuthResult.CANCELLED;
                    }
                } else {
                    utils.setLogStatus(false);
                    utils.setTempLog(false);
                    utils.Message(message);
                    result = AuthResult.SUCCESS;
                }
            } else {
                result = AuthResult.OFFLINE;
            }
        }, (long) (20 * 1.5));

        return result;
    }

    /**
     * Try yo register the player
     *
     * @param password the password
     * @return the auth result of the request
     */
    public final AuthResult tryRegister(String password) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player != null && player.isOnline()) {
                User utils = new User(player);
                utils.setLogStatus(true);
                utils.setTempLog(utils.has2FA());
                utils.setPassword(password);
                utils.Message(messages.Prefix() + messages.Registered());
                utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");

                PlayerRegisterEvent event = new PlayerRegisterEvent(player);
                plugin.getServer().getPluginManager().callEvent(event);
                result = AuthResult.SUCCESS;
            } else {
                result = AuthResult.OFFLINE;
            }
        }, (long) (20 * 1.5));

        return result;
    }

    /**
     * Try to login the player
     *
     * @param value the value
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player != null && player.isOnline()) {

                User utils = new User(player);
                if (value) {
                    PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                    plugin.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        if (utils.has2FA()) {
                            utils.setLogStatus(true);
                            utils.setTempLog(true);
                            utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                            result = AuthResult.SUCCESS_TEMP;
                        } else {
                            utils.setLogStatus(true);
                            utils.setLogStatus(false);
                            result = AuthResult.SUCCESS;
                        }
                    } else {
                        result = AuthResult.CANCELLED;
                    }
                } else {
                    utils.setLogStatus(false);
                    utils.setTempLog(false);
                    result = AuthResult.SUCCESS;
                }
            } else {
                result = AuthResult.OFFLINE;
            }
        }, (long) (20 * 1.5));

        return result;
    }

    /**
     * Checks if the player is logged or not
     *
     * @return if the player is logged
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
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        User utils = new User(player);
        return utils.isRegistered();
    }

    /**
     * Check if the player has tries left
     *
     * @return if the player has login tries left
     */
    public final boolean hasTries() {
        User utils = new User(player);
        return utils.hasTries();
    }

    /**
     * Check if the user has a pin
     *
     * @return if the player has a pin
     */
    public final boolean hasPin() {
        User user = new User(player);
        return user.hasPin();
    }

    /**
     * Check if the user is verified with
     * his pin
     *
     * @return if the player is verified via pin
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
     * @return the player country
     * <code>This won't return any
     * util information</code>
     */
    @Deprecated
    public final String[] getCountry() {
        return new String[]{"REMOVED", "IN 3.0.2"};
    }
}
