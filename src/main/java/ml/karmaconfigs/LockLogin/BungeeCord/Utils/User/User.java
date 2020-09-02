package ml.karmaconfigs.LockLogin.BungeeCord.Utils.User;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public final class User implements LockLoginBungee, BungeeFiles {

    private final static HashMap<ProxiedPlayer, Boolean> logStatus = new HashMap<>();
    private final static HashMap<ProxiedPlayer, Integer> playerTries = new HashMap<>();
    private final static HashMap<ProxiedPlayer, Boolean> correctServer = new HashMap<>();
    private final static HashSet<ProxiedPlayer> tempLog = new HashSet<>();

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
                out.Alert("Detected old player &f( &b" + player.getName() + " &f)&e data file, converting it...", WarningLevel.WARNING);
                playerFile.startConversion();
            } else {
                playerFile.setupFile();
            }
        } else {
            Utils sql = new Utils(player);
            sql.createUser();
        }
    }

    /**
     * Send a message to the player
     *
     * @param text the message
     */
    public final void Message(String text) {
        player.sendMessage(TextComponent.fromLegacyText(StringUtils.toColor(text)));
    }

    /**
     * Send a list messages to the player
     *
     * @param texts the messages
     */
    public final void Message(List<String> texts) {
        for (String str : texts) {
            Message(str);
        }
    }

    /**
     * Send a json message to the player
     *
     * @param JSonMessage the json message
     */
    public final void Message(TextComponent JSonMessage) {
        player.sendMessage(JSonMessage);
    }

    /**
     * Send a list of messages to the player
     *
     * @param messages the messages
     */
    public final void Message(HashSet<String> messages) {
        for (String str : messages) {
            Message(str);
        }
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

    /**
     * Send the player to the specified
     * server name
     *
     * @param Server the server name
     */
    public final void sendTo(String Server) {
        player.connect(plugin.getProxy().getServerInfo(Server));
    }

    /**
     * Kick the player
     *
     * @param reason the kick reason
     */
    public final void Kick(String reason) {
        player.disconnect(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + reason)));
    }

    /**
     * Set the player session status
     *
     * @param value true/false
     */
    public final void setLogStatus(boolean value) {
        logStatus.put(player, value);
    }

    /**
     * Rest a trie for the player
     */
    public final void restTries() {
        playerTries.put(player, getTriesLeft() - 1);
    }

    /**
     * Remove the player from tries
     * left
     */
    public final void delTries() {
        playerTries.remove(player);
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
        if (!isInCorrectServer()) {
            if (config.EnableAuth()) {
                if (lobbyCheck.AuthOk() && lobbyCheck.AuthIsWorking()) {
                    if (player.getServer() != null) {
                        if (player.getServer().getInfo() != null) {
                            if (player.getServer().getInfo().getName() != null && !player.getServer().getInfo().getName().isEmpty()) {
                                if (!player.getServer().getInfo().equals(lobbyCheck.generateServerInfo(lobbyCheck.getAuth()))) {
                                    correctServer.put(player, false);
                                    sendTo(lobbyCheck.getAuth());
                                } else {
                                    correctServer.put(player, true);
                                }
                            }
                        }
                    }
                }
            } else {
                if (config.EnableMain()) {
                    if (lobbyCheck.MainOk() && lobbyCheck.MainIsWorking()) {
                        if (player.getServer() != null) {
                            if (player.getServer().getInfo() != null) {
                                if (player.getServer().getInfo().getName() != null && !player.getServer().getInfo().getName().isEmpty()) {
                                    if (!player.getServer().getInfo().equals(lobbyCheck.generateServerInfo(lobbyCheck.getMain()))) {
                                        correctServer.put(player, false);
                                        sendTo(lobbyCheck.getMain());
                                    } else {
                                        correctServer.put(player, true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove the player from correct
     * server list
     */
    public final void removeServerInfo() {
        correctServer.remove(player);
    }

    /**
     * Get the player UUID
     * <code>No longer used</code>
     *
     * @return a UUID
     */
    @Deprecated
    public final UUID getUUID() {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            return playerFile.getUUID();
        } else {
            Utils sql = new Utils(player);

            return sql.getUUID();
        }
    }

    /**
     * Check if the player is registered
     *
     * @return a boolean
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
     * @return a boolean
     */
    public final boolean hasPin() {
        if (config.EnablePins()) {
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
     * @return a boolean
     */
    public final boolean has2FA() {
        if (config.Enable2FA()) {
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
     * Check if the player is in the server
     * where he should be
     *
     * @return a boolean
     */
    public final boolean isInCorrectServer() {
        return correctServer.getOrDefault(player, false).equals(true);
    }

    /**
     * Get the player password
     *
     * @return a String
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
     * Get the player pin
     *
     * @return a String
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
     * @return a String
     */
    public final String getToken(boolean unHashed) {
        if (config.isYaml()) {
            PlayerFile playerFile = new PlayerFile(player);

            if (!unHashed) {
                return playerFile.getToken();
            } else {
                return new PasswordUtils(playerFile.getToken()).UnHash();
            }
        } else {
            Utils sql = new Utils(player);

            if (!unHashed) {
                return sql.getToken();
            } else {
                return new PasswordUtils(sql.getToken()).UnHash();
            }
        }
    }

    /**
     * Generate a google authenticator token
     * for the player
     *
     * @return a String
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
     * @return a String
     */
    public final String genNewToken() {
        GoogleAuthenticator gauth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gauth.createCredentials();

        return key.getKey();
    }

    /**
     * Check if the player is logged
     *
     * @return a boolean
     */
    public final boolean isLogged() {
        return logStatus.getOrDefault(player, false).equals(true);
    }

    /**
     * Check if the player has tries left
     *
     * @return a boolean
     */
    public final boolean hasTries() {
        if (playerTries.containsKey(player)) {
            return playerTries.get(player) != 0;
        } else {
            playerTries.put(player, config.GetMaxTries());
            return true;
        }
    }

    /**
     * Check if the player is in temp
     * login status
     *
     * @return a boolean
     */
    public final boolean isTempLog() {
        return tempLog.contains(player);
    }

    /**
     * Set if the player is in temp-login
     * status
     *
     * @param value true/false
     */
    public final void setTempLog(boolean value) {
        if (value) {
            tempLog.add(player);
        } else {
            tempLog.remove(player);
        }
    }

    /**
     * Check if the code is ok
     *
     * @param code the code
     * @return a boolean
     */
    public final boolean validateCode(int code) {
        GoogleAuthenticator gauth = new GoogleAuthenticator();

        return gauth.authorize(getToken(true), code);
    }

    /**
     * Get the player tries left
     *
     * @return an integer
     */
    public final int getTriesLeft() {
        return playerTries.getOrDefault(player, config.GetMaxTries());
    }
}
