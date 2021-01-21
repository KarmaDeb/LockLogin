package ml.karmaconfigs.lockloginsystem.bungeecord.api;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerVerifyEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.servers.LobbyChecker;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.AuthResult;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
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

public class PlayerAPI implements LockLoginBungee, BungeeFiles {

    private final Module module;
    private final ProxiedPlayer player;
    private final BungeeSender dataSender = new BungeeSender();
    private AuthResult result = AuthResult.IDLE;

    /**
     * Initialize LockLogin bungee's API
     *
     * @param loader the module that is calling
     *               the API method
     * @param player the player
     */
    public PlayerAPI(final Module loader, ProxiedPlayer player) {
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
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                if (Value) {
                    if (!utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " logged in " + player.getName());

                        if (utils.has2FA()) {
                            utils.setLogStatus(true);
                            utils.setTempLog(true);
                            utils.Message(Message);
                            utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                dataSender.sendAccountStatus(player);
                            }, (long) 1.5, TimeUnit.SECONDS);
                        } else {
                            utils.setLogStatus(true);
                            utils.setTempLog(false);
                            utils.Message(Message);
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.MainOk() && checker.MainIsWorking()) {
                                utils.sendTo(checker.getMain());
                            }
                            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                dataSender.sendAccountStatus(player);
                            }, (long) 1.5, TimeUnit.SECONDS);
                        }
                    }
                } else {
                    if (utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " un-logged in " + player.getName());

                        utils.setLogStatus(false);
                        utils.setTempLog(false);
                        utils.Message(Message);
                        LobbyChecker checker = new LobbyChecker();
                        if (checker.AuthOk() && checker.AuthIsWorking()) {
                            utils.sendTo(checker.getAuth());
                        }
                        plugin.getProxy().getScheduler().schedule(plugin, () -> {
                            dataSender.sendAccountStatus(player);
                        }, (long) 1.5, TimeUnit.SECONDS);
                    }
                }
            }, (long) 1.5, TimeUnit.SECONDS);
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
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                if (player.isConnected()) {
                    User utils = new User(player);
                    if (value) {
                        if (!utils.isLogged()) {
                            PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                            plugin.getProxy().getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                if (utils.has2FA()) {
                                    utils.setLogStatus(true);
                                    utils.setTempLog(true);
                                    utils.Message(message);
                                    utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                    dataSender.sendAccountStatus(player);
                                    result = AuthResult.SUCCESS_TEMP;
                                } else {
                                    utils.setLogStatus(true);
                                    utils.setTempLog(false);
                                    utils.Message(message);
                                    LobbyChecker checker = new LobbyChecker();
                                    if (checker.MainOk() && checker.MainIsWorking()) {
                                        utils.sendTo(checker.getMain());
                                    }
                                    plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                        dataSender.sendAccountStatus(player);
                                    }, (long) 1.5, TimeUnit.SECONDS);
                                    result = AuthResult.SUCCESS;
                                }
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
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.AuthOk() && checker.AuthIsWorking()) {
                                utils.sendTo(checker.getAuth());
                            }
                            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                                dataSender.sendAccountStatus(player);
                            }, (long) 1.5, TimeUnit.SECONDS);
                        }
                        result = AuthResult.SUCCESS;
                    }
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + (value ? " tried to login " : " tried to un-login ") + player.getName() + " with result " + result.name());
            }, (long) 1.5, TimeUnit.SECONDS);
        }

        return result;
    }

    /**
     * Try to log the player
     *
     * @param value the value
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                if (player.isConnected()) {
                    User utils = new User(player);
                    if (value) {
                        if (!utils.isLogged()) {
                            PlayerVerifyEvent event = new PlayerVerifyEvent(player);
                            plugin.getProxy().getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                if (utils.has2FA()) {
                                    utils.setLogStatus(true);
                                    utils.setTempLog(true);
                                    utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                    dataSender.sendAccountStatus(player);
                                    result = AuthResult.SUCCESS_TEMP;
                                } else {
                                    utils.setLogStatus(true);
                                    utils.setTempLog(false);
                                    LobbyChecker checker = new LobbyChecker();
                                    if (checker.MainOk() && checker.MainIsWorking()) {
                                        utils.sendTo(checker.getMain());
                                    }

                                    dataSender.sendAccountStatus(player);
                                    result = AuthResult.SUCCESS;
                                }
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
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.AuthOk() && checker.AuthIsWorking()) {
                                utils.sendTo(checker.getAuth());
                            }
                            dataSender.sendAccountStatus(player);
                            result = AuthResult.SUCCESS;
                        }
                    }
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + (value ? " tried to login " : " tried to un-login ") + player.getName() + " with result " + result.name());
            }, (long) 1.5, TimeUnit.SECONDS);
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
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);

                if (!utils.isRegistered()) {
                    logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " registered to " + player.getName());

                    utils.setLogStatus(true);
                    utils.setTempLog(utils.has2FA());
                    utils.setPassword(password);
                    utils.Message(messages.Prefix() + messages.Registered());
                    utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
                    plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
                }
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Tries to register the player
     *
     * @param password the password
     * @return the auth result of the request
     */
    public final AuthResult tryRegister(String password) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                if (player.isConnected()) {
                    User utils = new User(player);

                    if (!utils.isRegistered()) {
                        utils.setLogStatus(true);
                        utils.setTempLog(utils.has2FA());
                        utils.setPassword(password);
                        utils.Message(messages.Prefix() + messages.Registered());
                        utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
                        plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);

                        PlayerRegisterEvent event = new PlayerRegisterEvent(player);
                        plugin.getProxy().getPluginManager().callEvent(event);
                    }
                    result = AuthResult.SUCCESS;
                } else {
                    result = AuthResult.OFFLINE;
                }

                logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " tried to register " + player.getName() + " with result " + result.name());
            }, (long) 1.5, TimeUnit.SECONDS);
        }

        return result;
    }

    /**
     * Mark the player as registered/not registered
     */
    public final void unRegister() {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                if (utils.isRegistered()) {
                    logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " unregistered to " + player.getName());

                    utils.Kick("&eLockLogin" + "\n\n" + messages.AccountDeleted());
                    utils.setLogStatus(false);
                    utils.setTempLog(utils.has2FA());
                    utils.setPassword("");
                    LobbyChecker checker = new LobbyChecker();
                    if (checker.AuthOk() && checker.AuthIsWorking()) {
                        utils.sendTo(checker.getAuth());
                    }
                }
            }, (long) 1.5, TimeUnit.SECONDS);
        }
    }

    /**
     * Rest a trie left for the player
     */
    public final void restTrie() {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                utils.restTries();
            }, (long) 1.5, TimeUnit.SECONDS);
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

        return false;
    }

    /**
     * Mark the player as logged/un-logged
     *
     * @param Value true = log the player; false = unlog him
     */
    public final void setLogged(boolean Value) {
        if (player != null) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                User utils = new User(player);
                if (Value) {
                    if (!utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " logged in " + player.getName());

                        if (utils.has2FA()) {
                            utils.setLogStatus(true);
                            utils.setTempLog(true);
                            utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                            dataSender.sendAccountStatus(player);
                        } else {
                            utils.setLogStatus(true);
                            utils.setLogStatus(false);
                            LobbyChecker checker = new LobbyChecker();
                            if (checker.MainOk() && checker.MainIsWorking()) {
                                utils.sendTo(checker.getMain());
                            }
                            dataSender.sendAccountStatus(player);
                        }
                    }
                } else {
                    if (utils.isLogged()) {
                        logger.scheduleLog(Level.INFO, "Module " + module.name() + " by " + module.author() + " un-logged in " + player.getName());

                        utils.setLogStatus(false);
                        utils.setTempLog(false);
                        LobbyChecker checker = new LobbyChecker();
                        if (checker.AuthOk() && checker.AuthIsWorking()) {
                            utils.sendTo(checker.getAuth());
                        }
                        dataSender.sendAccountStatus(player);
                    }
                }
            }, (long) 1.5, TimeUnit.SECONDS);
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

        return false;
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
     * Gets the player country name
     *
     * @return player country
     * <code>This won't return any
     * util information</code>
     */
    @Deprecated
    public final String[] getCountry() {
        return new String[]{"REMOVED", "VERSION 3.0.2"};
    }

    /**
     * Get the player registered
     * accounts
     *
     * @return a list of names that can be associated to that player
     */
    public final HashSet<OfflineUser> getAccounts() {
        if (player != null) {
            return IPStorager.manager.getAlts(module, player.getUniqueId());
        }

        return new HashSet<>();
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
