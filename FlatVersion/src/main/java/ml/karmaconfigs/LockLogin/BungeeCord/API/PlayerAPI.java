package ml.karmaconfigs.LockLogin.BungeeCord.API;

import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerRegisterEvent;
import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerVerifyEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.servers.LobbyChecker;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.AuthResult;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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

/**
 * @deprecated Now in package:
 * ml.karmaconfigs.lockloginsystem.bungeecord.api
 *
 * It's also insecure for server owners since it has
 * no control over who calls it
 */
@Deprecated
public class PlayerAPI implements LockLoginBungee, BungeeFiles {

    private final ProxiedPlayer player;
    private AuthResult result = AuthResult.IDLE;
    private final BungeeSender dataSender = new BungeeSender();

    /**
     * Initialize LockLogin bungee's API
     *
     * @param player the player
     */
    public PlayerAPI(ProxiedPlayer player) {
        this.player = player;
    }

    /**
     * Mark the player as logged/un-logged with a message
     *
     * @param Value   true/false
     * @param Message the message
     */
    public final void setLogged(boolean Value, String Message) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User utils = new User(player);
            if (Value) {
                if (utils.has2FA()) {
                    utils.setLogStatus(true);
                    utils.setTempLog(true);
                    utils.Message(Message);
                    utils.Message(messages.Prefix() + messages.gAuthAuthenticate());
                    plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
                } else {
                    utils.setLogStatus(true);
                    utils.setTempLog(false);
                    utils.Message(Message);
                    LobbyChecker checker = new LobbyChecker();
                    if (checker.MainOk() && checker.MainIsWorking()) {
                        utils.sendTo(checker.getMain());
                    }
                    plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
                }
            } else {
                utils.setLogStatus(false);
                utils.setTempLog(false);
                utils.Message(Message);
                LobbyChecker checker = new LobbyChecker();
                if (checker.AuthOk() && checker.AuthIsWorking()) {
                    utils.sendTo(checker.getAuth());
                }
                plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
            }
        }, (long) 1.5, TimeUnit.SECONDS);
    }

    /**
     * Try to log the player
     *
     * @param value the value
     * @param message the login message
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value, String message) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (player != null && player.isConnected()) {
                User utils = new User(player);
                if (value) {
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
                            plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
                            result = AuthResult.SUCCESS;
                        }
                    } else {
                        result = AuthResult.CANCELLED;
                    }
                } else {
                    utils.setLogStatus(false);
                    utils.setTempLog(false);
                    utils.Message(message);
                    LobbyChecker checker = new LobbyChecker();
                    if (checker.AuthOk() && checker.AuthIsWorking()) {
                        utils.sendTo(checker.getAuth());
                    }
                    plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
                    result = AuthResult.SUCCESS;
                }
            } else {
                result = AuthResult.OFFLINE;
            }
        }, (long) 1.5, TimeUnit.SECONDS);

        return result;
    }

    /**
     * Mark the player as registered/not registered
     */
    public final void unRegister() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User utils = new User(player);
            utils.Kick("&eLockLogin" + "\n\n" + messages.AccountDeleted());
            utils.setLogStatus(false);
            utils.setTempLog(utils.has2FA());
            utils.setPassword("");
            LobbyChecker checker = new LobbyChecker();
            if (checker.AuthOk() && checker.AuthIsWorking()) {
                utils.sendTo(checker.getAuth());
            }
        }, (long) 1.5, TimeUnit.SECONDS);
    }

    /**
     * Registers a player with the specified password
     *
     * @param password the password
     */
    public final void register(String password) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User utils = new User(player);
            utils.setLogStatus(true);
            utils.setTempLog(utils.has2FA());
            utils.setPassword(password);
            utils.Message(messages.Prefix() + messages.Registered());
            utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
            plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);
        }, (long) 1.5, TimeUnit.SECONDS);
    }

    /**
     * Tries to register the player
     *
     * @param password the password
     * @return the auth result of the request
     */
    public final AuthResult tryRegister(String password) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (player != null && player.isConnected()) {
                User utils = new User(player);
                utils.setLogStatus(true);
                utils.setTempLog(utils.has2FA());
                utils.setPassword(password);
                utils.Message(messages.Prefix() + messages.Registered());
                utils.Message("&aSERVER &7>> &cYour password is &3" + password + " &cdon't share it with anyone");
                plugin.getProxy().getScheduler().schedule(plugin, () -> dataSender.sendAccountStatus(player), (long) 1.5, TimeUnit.SECONDS);

                PlayerRegisterEvent event = new PlayerRegisterEvent(player);
                plugin.getProxy().getPluginManager().callEvent(event);

                result = AuthResult.SUCCESS;
            } else {
                result = AuthResult.OFFLINE;
            }
        }, (long) 1.5, TimeUnit.SECONDS);

        return result;
    }

    /**
     * Rest a trie left for the player
     */
    public final void restTrie() {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User utils = new User(player);
            utils.restTries();
        }, (long) 1.5, TimeUnit.SECONDS);
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
     * Mark the player as logged/un-logged
     *
     * @param Value true = log the player; false = unlog him
     */
    public final void setLogged(boolean Value) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            User utils = new User(player);
            if (Value) {
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
            } else {
                utils.setLogStatus(false);
                utils.setTempLog(false);
                LobbyChecker checker = new LobbyChecker();
                if (checker.AuthOk() && checker.AuthIsWorking()) {
                    utils.sendTo(checker.getAuth());
                }
                dataSender.sendAccountStatus(player);
            }
        }, (long) 1.5, TimeUnit.SECONDS);
    }

    /**
     * Try to log the player
     *
     * @param value the value
     * @return the auth result of the request
     */
    public final AuthResult tryLogin(boolean value) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            if (player != null && player.isConnected()) {
                User utils = new User(player);
                if (value) {
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
                    utils.setLogStatus(false);
                    utils.setTempLog(false);
                    LobbyChecker checker = new LobbyChecker();
                    if (checker.AuthOk() && checker.AuthIsWorking()) {
                        utils.sendTo(checker.getAuth());
                    }
                    dataSender.sendAccountStatus(player);
                    result = AuthResult.SUCCESS;
                }
            } else {
                result = AuthResult.OFFLINE;
            }
        }, (long) 1.5, TimeUnit.SECONDS);

        return result;
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
}
