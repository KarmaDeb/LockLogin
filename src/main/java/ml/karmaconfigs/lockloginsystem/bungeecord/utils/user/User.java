package ml.karmaconfigs.lockloginsystem.bungeecord.utils.user;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Passwords;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
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
public final class User implements LockLoginBungee, BungeeFiles {

    private final static HashMap<UUID, Boolean> logStatus = new HashMap<>();
    private final static HashMap<UUID, Integer> playerTries = new HashMap<>();
    private final static HashMap<UUID, Integer> playerCaptcha = new HashMap<>();
    private final static HashSet<UUID> tempLog = new HashSet<>();

    private final ProxiedPlayer player;

    /**
     * Setup the user
     *
     * @param player the player
     */
    public User(ProxiedPlayer player) {
        this.player = player;
    }

    /**
     * Setup the player file
     */
    public final void setupFile() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);
            if (playerFile.isOld()) {
                Console.send(plugin, "Detected old player &f( &b" + player.getName() + " &f)&e data file, converting it...", Level.INFO);
                playerFile.startConversion();
            } else {
                playerFile.setupFile();
            }

            if (!playerFile.getName().equals(player.getName()))
                playerFile.setName(player.getName());
        } else {
            if (config.registerRestricted() && !isRegistered())
                return;

            Utils sql = new Utils(player);
            sql.createUser();

            String name = sql.getName();
            if (name != null && !name.equals(player.getName()))
                sql.setName(player.getName());
        }
    }

    /**
     * Generate a captcha for the player
     */
    public final void genCaptcha() {
        int captcha = StringUtils.randomNumber(config.getCaptchaLength());
        playerCaptcha.put(player.getUniqueId(), captcha);

        send(messages.prefix() + messages.captcha(captcha));
    }

    /**
     * Send a message to the player
     *
     * @param text the message
     */
    public final void send(String text) {
        if (!text.replace(messages.prefix(), "").replaceAll("\\s", "").isEmpty())
            if (!text.contains("\n"))
                player.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(text)));
            else
                send(Arrays.asList(text.split("\n")));
    }

    /**
     * Send a list messages to the player
     *
     * @param texts the messages
     */
    public final void send(List<String> texts) {
        if (!texts.isEmpty())
            for (String str : texts)
                send(str);
    }

    /**
     * Send a json message to the player
     *
     * @param JSonMessage the json message
     */
    public final void send(TextComponent JSonMessage) {
        if (!JSonMessage.getText().replace(messages.prefix(), "").replaceAll("\\s", "").isEmpty())
            player.sendMessage(JSonMessage);
    }

    /**
     * Send a list of messages to the player
     *
     * @param messages the messages
     */
    public final void send(HashSet<String> messages) {
        if (!messages.isEmpty())
            for (String str : messages)
                send(str);
    }

    /**
     * Send a title to the player
     *
     * @param Title    the title
     * @param Subtitle the subtitle
     */
    public final void sendTitle(String Title, String Subtitle) {
        Title title = plugin.getProxy().createTitle();
        title.fadeIn(20);
        title.stay(20 * 5);
        title.fadeOut(20);
        title.title(TextComponent.fromLegacyText(StringUtils.toColor(Title)));
        title.subTitle(TextComponent.fromLegacyText(StringUtils.toColor(Subtitle)));

        title.send(player);
    }

    public final void authPlayer(final String password) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            PasswordUtils utils = new PasswordUtils(password, getPassword());

            BFSystem bf_prevention = new BFSystem(player.getPendingConnection().getVirtualHost().getAddress());

            PlayerAuthEvent event = new PlayerAuthEvent(AuthType.PASSWORD, EventAuthResult.WAITING, player, "");

            boolean valid_password = false;
            if (utils.validate()) {
                valid_password = true;
                if (hasPin()) {
                    event.setAuthResult(EventAuthResult.SUCCESS_TEMP);
                } else {
                    if (has2FA()) {
                        event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.gAuthInstructions());
                    } else {
                        event.setAuthResult(EventAuthResult.SUCCESS, messages.prefix() + messages.logged(player));
                    }
                }
            } else {
                event.setAuthResult(EventAuthResult.FAILED, messages.prefix() + messages.logError());
            }

            plugin.getProxy().getPluginManager().callEvent(event);

            switch (event.getAuthResult()) {
                case SUCCESS:
                    send(event.getAuthMessage());
                    if (valid_password) {
                        bf_prevention.success();
                        setLogStatus(true);
                        checkServer();

                        dataSender.sendAccountStatus(player);
                    } else {
                        logger.scheduleLog(Level.WARNING, "Someone tried to force log " + player.getName() + " using event API");
                    }

                    if (Passwords.isLegacySalt(getPassword())) {
                        setPassword(password);
                        send(messages.prefix() + "&cYour account password was using legacy encryption and has been updated");
                    } else {
                        if (utils.needsRehash(config.passwordEncryption())) {
                            setPassword(password);
                        }
                    }
                    break;
                case SUCCESS_TEMP:
                    if (valid_password) {
                        bf_prevention.success();
                        setLogStatus(true);

                        if (Passwords.isLegacySalt(getPassword())) {
                            setPassword(password);
                            send(messages.prefix() + "&cYour account password was using legacy encryption and has been updated");
                        } else {
                            if (utils.needsRehash(config.passwordEncryption())) {
                                setPassword(password);
                            }
                        }

                        setTempLog(true);
                        if (!hasPin()) {
                            send(event.getAuthMessage());
                        } else {
                            dataSender.openPinGUI(player);
                        }
                    } else {
                        logger.scheduleLog(Level.WARNING, "Someone tried to force temp log " + player.getName() + " using event API");
                        send(event.getAuthMessage());
                    }
                    break;
                case FAILED:
                    if (bf_prevention.getTries() >= config.bfMaxTries() && config.bfMaxTries() > 0) {
                        bf_prevention.block();
                        bf_prevention.updateTime(config.bfBlockTime());

                        Timer unban = new Timer();
                        unban.schedule(new TimerTask() {
                            final BFSystem saved_system = bf_prevention;
                            int back = config.bfBlockTime();

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

                        kick("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft()));
                    }
                    if (hasTries()) {
                        restTries();
                        send(event.getAuthMessage());
                    } else {
                        bf_prevention.fail();
                        delTries();
                        kick("&eLockLogin\n\n" + messages.logError());
                    }
                    break;
                case ERROR:
                case WAITING:
                    send(event.getAuthMessage());
                    break;
            }
        });
    }

    /**
     * Send the player to the specified
     * server name
     *
     * @param server the server name
     */
    public final void sendTo(String server) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> player.connect(lobbyCheck.generateServerInfo(server)), 0, TimeUnit.SECONDS);
    }

    /**
     * Kick the player
     *
     * @param reason the kick reason
     */
    public final void kick(String reason) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> player.disconnect(TextComponent.fromLegacyText(StringUtils.toColor(reason))), 0, TimeUnit.SECONDS);
    }

    /**
     * Set the player session status
     *
     * @param value true/false
     */
    public final void setLogStatus(boolean value) {
        logStatus.put(player.getUniqueId(), value);
    }

    /**
     * Rest a trie for the player
     */
    public final void restTries() {
        playerTries.put(player.getUniqueId(), getTriesLeft() - 1);
    }

    /**
     * Remove the player from tries
     * left
     */
    public final void delTries() {
        playerTries.remove(player.getUniqueId());
    }

    /**
     * Set the player 2fa status
     *
     * @param value true/false
     */
    public final void set2FA(boolean value) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.set2FA(value);
        } else {
            Utils sql = new Utils(player);

            sql.gAuthStatus(value);
        }
    }

    /**
     * Set the player 2fa token
     *
     * @param token the token
     */
    public final void setToken(String token) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.setToken(token);
        } else {
            Utils sql = new Utils(player);

            sql.setGAuth(token, true);
        }
    }

    /**
     * Remove the player file
     * or if using mysql, player info
     */
    public final void remove() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.removeFile();
        } else {
            Utils sql = new Utils(player);

            sql.removeUser();
        }
    }

    /**
     * Check the user server
     */
    public final void checkServer() {
        Server current_server = player.getServer();

        if (current_server != null) {
            if (current_server.isConnected() && player.isConnected()) {
                ServerInfo current_info = current_server.getInfo();
                boolean sent = false;
                if (!isLogged() || isTempLog())
                    if (config.enableAuthLobby())
                        if (lobbyCheck.authOk() && lobbyCheck.authWorking())
                            if (!current_info.getName().equals(lobbyCheck.getAuth())) {
                                sendTo(lobbyCheck.getAuth());
                                sent = true;
                            } else
                                sent = current_info.getName().equals(lobbyCheck.getMain());

                if (!sent)
                    if (config.enableMainLobby())
                        if (lobbyCheck.mainOk() && lobbyCheck.mainWorking())
                            if (!current_info.getName().equals(lobbyCheck.getMain()))
                                sendTo(lobbyCheck.getMain());
            }
        }
    }

    /**
     * Tries to get the player IP
     * using socket connection
     *
     * @return the player InetAddress
     */
    @Nullable
    public final InetAddress getIp() {
        try {
            InetSocketAddress address = (InetSocketAddress) player.getSocketAddress();
            return address.getAddress();
        } catch (Throwable ex) {
            logger.scheduleLog(Level.GRAVE, ex);
            logger.scheduleLog(Level.INFO, "Failed to retrieve player IP");
            return null;
        }
    }

    /**
     * Get the player UUID
     * <code>No longer used</code>
     *
     * @return the player UUID
     */
    @Deprecated
    public final UUID getUUID() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            return playerFile.getUUID();
        } else {
            Utils sql = new Utils(player);

            return Utils.fixUUID(sql.getUUID());
        }
    }

    public final boolean checkCaptcha(final int code) {
        if (playerCaptcha.containsKey(player.getUniqueId()))
            return code == playerCaptcha.remove(player.getUniqueId());

        return false;
    }

    public final boolean hasCaptcha() {
        return playerCaptcha.getOrDefault(player.getUniqueId(), -1) > 0;
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            return !playerFile.getPassword().isEmpty();
        } else {
            Utils sql = new Utils(player);

            if (sql.getPassword() != null) {
                return !sql.getPassword().isEmpty();
            }
            return false;
        }
    }

    /**
     * Check if the player has a pin set
     *
     * @return if the player has pin
     */
    public final boolean hasPin() {
        if (config.pinEnabled()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.getPin() != null && !playerFile.getPin().isEmpty();
            } else {
                Utils sql = new Utils(player);

                return sql.getPin() != null && !sql.getPin().isEmpty();
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the player has 2fa enabled
     *
     * @return if the player has 2FA in his account
     */
    public final boolean has2FA() {
        if (config.enable2FA()) {
            if (config.isYaml()) {
                PlayerFile playerFile = new PlayerFile(player);

                return playerFile.has2FA();
            } else {
                Utils sql = new Utils(player);

                return sql.has2fa();
            }
        } else {
            return false;
        }
    }

    /**
     * Get the player password
     *
     * @return the player password
     */
    public final String getPassword() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            return playerFile.getPassword();
        } else {
            Utils sql = new Utils(player);

            return sql.getPassword();
        }
    }

    /**
     * Set the player password
     *
     * @param password the password
     */
    public final void setPassword(String password) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.setPassword(password);
        } else {
            Utils sql = new Utils(player);
            sql.setPassword(password, false);
        }
    }

    /**
     * Get the player pin
     *
     * @return the player pin
     */
    public final String getPin() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            return playerFile.getPin();
        } else {
            Utils sql = new Utils(player);

            return sql.getPin();
        }
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.setPin(pin);
        } else {
            Utils sql = new Utils(player);

            sql.setPin(pin, false);
        }
    }

    /**
     * Remove the user pin
     */
    public final void removePin() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            playerFile.delPin();
        } else {
            Utils sql = new Utils(player);

            sql.delPin();
        }
    }

    /**
     * Get the player token
     *
     * @param unHashed hash or not the token
     * @return the player google auth token
     */
    public final String getToken(boolean unHashed) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            if (!unHashed) {
                return playerFile.getToken();
            } else {
                return new PasswordUtils(playerFile.getToken()).unHash();
            }
        } else {
            Utils sql = new Utils(player);

            if (!unHashed) {
                return sql.getToken();
            } else {
                return new PasswordUtils(sql.getToken()).unHash();
            }
        }
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return player google auth token if set, if not, a new google auth token
     */
    public final String genToken() {
        if (getToken(true).isEmpty()) {
            GoogleAuthenticator gauth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gauth.createCredentials();

            return key.getKey();
        } else {
            return getToken(true);
        }
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return a new google auth token
     */
    public final String genNewToken() {
        GoogleAuthenticator gauth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gauth.createCredentials();

        return key.getKey();
    }

    /**
     * Check if the player is logged
     *
     * @return if the player is logged
     */
    public final boolean isLogged() {
        return logStatus.getOrDefault(player.getUniqueId(), false).equals(true);
    }

    /**
     * Check if the player has tries left
     *
     * @return if the player has login tries left
     */
    public final boolean hasTries() {
        if (playerTries.containsKey(player.getUniqueId())) {
            return playerTries.get(player.getUniqueId()) != 0;
        } else {
            playerTries.put(player.getUniqueId(), config.loginMaxTries());
            return true;
        }
    }

    /**
     * Check if the player is in temp
     * login status
     *
     * @return if the player is in temp-log status
     * For example, if he has 2Fa or pin
     */
    public final boolean isTempLog() {
        return tempLog.contains(player.getUniqueId());
    }

    /**
     * Set if the player is in temp-login
     * status
     *
     * @param value true/false
     */
    public final void setTempLog(boolean value) {
        if (value) {
            tempLog.add(player.getUniqueId());
        } else {
            tempLog.remove(player.getUniqueId());
        }
    }

    /**
     * Check if the code is ok
     *
     * @param code the code
     * @return if the specified code is valid
     */
    public final boolean validateCode(int code) {
        GoogleAuthenticator gauth = new GoogleAuthenticator();

        return gauth.authorize(getToken(true), code);
    }

    /**
     * Get the player tries left
     *
     * @return the amount of login tries left
     * of the player
     */
    public final int getTriesLeft() {
        return playerTries.getOrDefault(player.getUniqueId(), config.loginMaxTries());
    }

    public final int getCaptcha() {
        return playerCaptcha.getOrDefault(player.getUniqueId(), -1);
    }

    public interface external {

        @Nullable
        static InetAddress getIp(final SocketAddress connection) {
            try {
                InetSocketAddress address = (InetSocketAddress) connection;
                return address.getAddress();
            } catch (Throwable ex) {
                return null;
            }
        }
    }
}
