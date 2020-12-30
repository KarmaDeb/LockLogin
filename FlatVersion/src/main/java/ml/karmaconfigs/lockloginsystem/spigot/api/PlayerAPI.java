package ml.karmaconfigs.lockloginsystem.spigot.api;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.AuthResult;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.IPStorager;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerVerifyEvent;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

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

@SuppressWarnings("unused")
public final class PlayerAPI implements LockLoginSpigot, SpigotFiles {

    private final Player player;
    private final Module module;
    private AuthResult result = AuthResult.IDLE;

    /**
     * Initialize LockLogin bungee's API
     *
     * @param loader the module that is calling
     *               the API method
     * @param player the player
     */
    public PlayerAPI(final Module loader, final Player player) {
        module = loader;
        if (ModuleLoader.manager.isLoaded(loader)) {
            logger.scheduleLog(Level.INFO, "Module " + loader.name() + " by " + loader.author() + " initialized PlayerAPI for " + player.getName());
            this.player = player;
        } else {
            this.player = null;
        }
    }

    /**
     * Mark the player as logged/un-logged with a message
     *
     * @param Value   true/false
     * @param Message the message
     */
    public final void setLogged(boolean Value, String Message) {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                User utils = new User(player);
                if (Value) {
                    if (!utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " logged in " + player.getName());

                        if (config.TakeBack()) {
                            LastLocation lastLoc = new LastLocation(player);
                            utils.Teleport(lastLoc.getLastLocation());
                        }
                        if (config.LoginBlind()) {
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> utils.removeBlindEffect(config.LoginNausea()));
                        }
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(utils.hasFly()));

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

                        utils.sendTitle("", "", 1, 2, 1);
                    }
                } else {
                    if (utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " un-logged in " + player.getName());

                        utils.setLogStatus(false);
                        utils.setTempLog(false);
                        utils.Message(Message);
                    }
                }
            }, (long) (20 * 1.5));
        }
    }

    /**
     * Try to log the player
     *
     * @param value   the value
     * @param message the login message
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value, String message) {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    User utils = new User(player);
                    if (value) {
                        if (!utils.isLogged()) {
                            PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                            plugin.getServer().getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                if (config.TakeBack()) {
                                    LastLocation lastLoc = new LastLocation(player);
                                    utils.Teleport(lastLoc.getLastLocation());
                                }
                                if (config.LoginBlind()) {
                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> utils.removeBlindEffect(config.LoginNausea()));
                                }
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(utils.hasFly()));

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

                                utils.sendTitle("", "", 1, 2, 1);
                            } else {
                                result = AuthResult.CANCELLED;
                            }
                        } else {
                            result = AuthResult.SUCCESS;
                        }
                    } else {
                        if (utils.isLogged()) {
                            utils.setLogStatus(false);
                            utils.setTempLog(false);
                            utils.Message(message);
                        }
                        result = AuthResult.SUCCESS;
                    }
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + (value ? " tried to login " : " tried to un-login ") + player.getName() + " with result " + result.name());
            }, (long) (20 * 1.5));
        }

        return result;
    }

    /**
     * Try to login the player
     *
     * @param value the value
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value) {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    User utils = new User(player);
                    if (value) {
                        if (!utils.isLogged()) {
                            PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                            plugin.getServer().getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                if (config.TakeBack()) {
                                    LastLocation lastLoc = new LastLocation(player);
                                    utils.Teleport(lastLoc.getLastLocation());
                                }
                                if (config.LoginBlind()) {
                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> utils.removeBlindEffect(config.LoginNausea()));
                                }
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(utils.hasFly()));

                                if (utils.has2FA()) {
                                    utils.setLogStatus(true);
                                    utils.setTempLog(true);
                                    utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                    result = AuthResult.SUCCESS_TEMP;
                                } else {
                                    utils.setLogStatus(true);
                                    utils.setTempLog(false);
                                    result = AuthResult.SUCCESS;
                                }

                                utils.sendTitle("", "", 1, 2, 1);
                            } else {
                                result = AuthResult.CANCELLED;
                            }
                        } else {
                            result = AuthResult.SUCCESS;
                        }
                    } else {
                        if (utils.isLogged()) {
                            utils.setLogStatus(false);
                            utils.setTempLog(false);
                        }
                        result = AuthResult.SUCCESS;
                    }
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + (value ? " tried to login " : " tried to un-login ") + player.getName() + " with result " + result.name());
            }, (long) (20 * 1.5));
        }

        return result;
    }

    /**
     * Registers a player with the specified password
     *
     * @param password the password
     */
    public final void register(String password) {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                User utils = new User(player);

                if (!utils.isRegistered()) {
                    logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " registered to " + player.getName());

                    if (config.TakeBack()) {
                        LastLocation lastLoc = new LastLocation(player);
                        utils.Teleport(lastLoc.getLastLocation());
                    }
                    if (config.RegisterBlind()) {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> utils.removeBlindEffect(config.RegisterNausea()));
                    }
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(utils.hasFly()));

                    utils.setLogStatus(true);
                    utils.setTempLog(utils.has2FA());
                    utils.setPassword(password);
                    utils.Message(messages.Prefix() + messages.Registered());
                    utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");

                    utils.sendTitle("", "", 1, 2, 1);
                }
            }, (long) (20 * 1.5));
        }
    }

    /**
     * Try yo register the player
     *
     * @param password the password
     * @return the auth result of the request
     */
    public final AuthResult tryRegister(String password) {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    User utils = new User(player);

                    if (!utils.isRegistered()) {
                        if (config.TakeBack()) {
                            LastLocation lastLoc = new LastLocation(player);
                            utils.Teleport(lastLoc.getLastLocation());
                        }
                        if (config.RegisterBlind()) {
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> utils.removeBlindEffect(config.RegisterNausea()));
                        }
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(utils.hasFly()));

                        utils.setLogStatus(true);
                        utils.setTempLog(utils.has2FA());
                        utils.setPassword(password);
                        utils.Message(messages.Prefix() + messages.Registered());
                        utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");

                        PlayerRegisterEvent event = new PlayerRegisterEvent(player);
                        plugin.getServer().getPluginManager().callEvent(event);
                        result = AuthResult.SUCCESS;

                        utils.sendTitle("", "", 1, 2, 1);
                    } else {
                        result = AuthResult.SUCCESS;
                    }
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " tried to register " + player.getName() + " with result " + result.name());
            }, (long) (20 * 1.5));
        }

        return result;
    }

    /**
     * Mark the player as registered/not registered
     */
    public final void unRegister() {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                User utils = new User(player);

                if (utils.isRegistered()) {
                    logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " unregistered to " + player.getName());

                    if (config.TakeBack()) {
                        LastLocation lastLocation = new LastLocation(player);
                        lastLocation.saveLocation();
                    }
                    if (config.HandleSpawn()) {
                        Spawn spawn = new Spawn();
                        utils.Teleport(spawn.getSpawn());
                    }

                    utils.Kick("&eLockLogin" + "\n\n" + messages.AccountDeleted());
                    utils.setLogStatus(false);
                    utils.setTempLog(utils.has2FA());
                    utils.setPassword("");
                    new StartCheck(player, CheckType.REGISTER);
                }
            }, (long) (20 * 1.5));
        }
    }

    /**
     * Rest a trie left for the player
     */
    public final void restTrie() {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                User utils = new User(player);
                utils.restTries();
            }, (long) (20 * 1.5));
        }
    }

    /**
     * Checks if the player is logged or not
     *
     * @return if the player is logged
     */
    public final boolean isLogged() {
        if (player != null) {
            User utils = new User(player);
            if (utils.has2FA()) {
                return utils.isLogged() && !utils.isTempLog();
            }
            return utils.isLogged();
        }

        return true;
    }

    /**
     * Mark the player as logged/un-logged
     *
     * @param Value true/false
     */
    public final void setLogged(boolean Value) {
        if (player != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                User utils = new User(player);
                if (Value) {
                    if (!utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " logged in " + player.getName());

                        if (config.TakeBack()) {
                            LastLocation lastLoc = new LastLocation(player);
                            utils.Teleport(lastLoc.getLastLocation());
                        }
                        if (config.LoginBlind()) {
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> utils.removeBlindEffect(config.LoginNausea()));
                        }
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setAllowFlight(utils.hasFly()));

                        if (utils.has2FA()) {
                            utils.setLogStatus(true);
                            utils.setTempLog(true);
                            utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                        } else {
                            utils.setLogStatus(true);
                            utils.setLogStatus(false);
                        }

                        utils.sendTitle("", "", 1, 2, 1);
                    }
                } else {
                    if (utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " un-logged in " + player.getName());

                        utils.setLogStatus(false);
                        utils.setTempLog(false);
                    }
                }
            }, (long) (20 * 1.5));
        }
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (player != null) {
            User utils = new User(player);
            return utils.isRegistered();
        }

        return true;
    }

    /**
     * Check if the player has tries left
     *
     * @return if the player has login tries left
     */
    public final boolean hasTries() {
        if (player != null) {
            User utils = new User(player);
            return utils.hasTries();
        }

        return true;
    }

    /**
     * Check if the user has a pin
     *
     * @return if the player has a pin
     */
    public final boolean hasPin() {
        if (player != null) {
            User user = new User(player);
            return user.hasPin();
        }

        return true;
    }

    /**
     * Check if the user is verified with
     * his pin
     *
     * @return if the player is verified via pin
     */
    public final boolean isPinVerified() {
        if (player != null) {
            if (hasPin()) {
                PinInventory inventory = new PinInventory(player);

                return inventory.isVerified();
            } else {
                return true;
            }
        }

        return false;
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

    /**
     * Get the player registered
     * accounts
     *
     * @return a list of names that can be associated to that player
     */
    public final List<String> getAccounts() {
        if (player != null) {
            return IPStorager.getStorage(player.getName(), false);
        }

        return Collections.emptyList();
    }

    /**
     * Get the player connections
     *
     * @return the amount of connections from the player IP
     */
    public final int getConnections() {
        if (player != null) {
            IpData data = new IpData(module, player.getAddress().getAddress());
            data.fetch(Platform.BUNGEE);

            return data.getConnections();
        }

        return 0;
    }
}
