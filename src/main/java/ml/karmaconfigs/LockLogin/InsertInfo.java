package ml.karmaconfigs.LockLogin;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class InsertInfo {

    private final String player;
    private final String uuid;
    private String password;
    private String gauth;
    private String pin;
    private boolean faon, fly;

    /**
     * Initialize the insert info for player
     *
     * @param player the player
     */
    public InsertInfo(String player) {
        this.player = player;
        this.uuid = getUUID(player).toString();
    }

    /**
     * Set the password
     *
     * @param password the password
     */
    public final void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the player gAuth status
     *
     * @param status the status
     */
    public final void setGAuthStatus(boolean status) {
        this.faon = status;
    }

    /**
     * Set the player fly status
     *
     * @param status the status
     */
    public final void setFly(boolean status) {
        this.fly = status;
    }

    /**
     * Set the google auth token
     *
     * @param token the token
     */
    public final void setGauthToken(String token) {
        this.gauth = token;
    }

    /**
     * Set the pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        this.pin = pin.toString();
    }

    /**
     * Get all the data
     *
     * @return a data object
     */
    public final String[] getData() {
        String name = "Name: " + player;
        String id = "UUID: " + uuid;
        String auth = "Password: " + password;
        String token = "Token: " + gauth;
        String pinAuth = "Pin: " + pin;
        String gAuthStatus = "gAuth: " + faon;
        String flyStatus = "Fly: " + fly;

        return new String[]{name, id, auth, token, pinAuth, gAuthStatus, flyStatus};
    }

    /**
     * Get an offline player uuid
     *
     * @param name the name
     * @return an UUID
     */
    private UUID getUUID(String name) {
        UUID uuid;

        if (PlatformUtils.isPremium()) {
            uuid = mojangUUID(name);
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }

        return uuid;
    }

    /**
     * Get the mojang player uuid
     *
     * @param name the player name
     * @return an uuid
     */
    private UUID mojangUUID(String name) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

            String UUIDJson = IOUtils.toString(new URL(url));

            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);

            return UUID.fromString(UUIDObject.get("id").toString());
        } catch (Throwable e) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
    }
}
