package ml.karmaconfigs.LockLogin.MySQL;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.PlatformUtils;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.io.IOUtils;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Utils {

    private final String table = Bucket.getTable();
    private String uuid;

    /**
     * Starts the MySQL management
     * without any UUID
     * <code>This should be used only to
     * get a list of the registered UUIDs</code>
     */
    public Utils() {
    }

    /**
     * Starts the MySQL management
     *
     * @param uuid the player UUID
     */
    public Utils(UUID uuid) {
        this.uuid = uuid.toString();
        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {}
    }

    /**
     * Starts the MySQL management
     *
     * @param uuid the player UUID as string
     */
    public Utils(String uuid) {
        this.uuid = uuid;
        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {}
    }

    /**
     * Starts the MySQL management
     *
     * @param player the offline player
     */
    public Utils(OfflinePlayer player) {
        this.uuid = player.getUniqueId().toString();
        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {}
    }

    /**
     * Starts the MySQL management
     *
     * @param player the player
     */
    public Utils(ProxiedPlayer player) {
        this.uuid = player.getUniqueId().toString();
        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {}
    }

    /**
     * Checks if the MySQL user exists
     *
     * @return a boolean
     */
    @SuppressWarnings("all")
    public final boolean userExists() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
            statement.setString(1, uuid);

            ResultSet results = statement.executeQuery();
            return results.next();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CHECKING USER " + uuid, e);
            return false;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Creates user on MySQL tables
     */
    public final void createUser() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);

            ResultSet results = statement.executeQuery();
            results.next();
            if (!userExists()) {
                PreparedStatement add = connection.prepareStatement("INSERT INTO " + table + "(PLAYER,UUID,PASSWORD,PIN,FAON,GAUTH,FLY) VALUE (?,?,?,?,?,?,?)");

                add.setString(1, "");
                add.setString(2, uuid);
                add.setString(3, "");
                add.setString(4, "");
                add.setBoolean(5, false);
                add.setString(6, "");
                add.setBoolean(7, false);
                add.executeUpdate();
            }
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CREATING USER " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Removes the user from the MySQL tables
     */
    public final void removeUser() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("DELETE * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);

            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE DELETING USER [Switching to 2nd method]" + uuid, e);
            try {
                connection = Bucket.getBucket().getConnection();
                statement = connection.prepareStatement("DELETE FROM " + table + " WHERE UUID=?");

                statement.setString(1, uuid);

                statement.executeUpdate();
            } catch (Throwable ex) {
                Logger.log(Platform.ANY, "ERROR WHILE DELETING USER " + uuid, ex);
            }
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the player's password
     *
     * @param password player's password
     * @param literal  if the password is already hashed or not
     */
    public final void setPassword(String password, boolean literal) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET PASSWORD=? WHERE UUID=?");

            if (!literal) {
                statement.setString(1, new PasswordUtils(password).Hash());
            } else {
                statement.setString(1, password);
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE SETTING PASSWORD FOR USER " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the user account pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin, boolean literal) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET PIN=? WHERE UUID=?");

            if (!literal) {
                statement.setString(1, new PasswordUtils(pin.toString()).Hash());
            } else {
                statement.setString(1, pin.toString());
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE SETTING PING FOR USER " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Remove the player pin
     */
    public final void delPin() {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET PIN=? WHERE UUID=?");

            statement.setString(1, "");
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE DELETING USER " + uuid + " PIN", e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the account's 2fa status
     *
     * @param Value true/false
     */
    public final void gAuthStatus(boolean Value) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET FAON=? WHERE UUID=?");

            statement.setBoolean(1, Value);
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING USER " + uuid + " GOOGLE AUTH STATUS", e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the account's 2fa auth token
     *
     * @param Token the token
     */
    public final void setGAuth(String Token, boolean hashed) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET GAUTH=? WHERE UUID=?");

            if (hashed) {
                statement.setString(1, new PasswordUtils(Token).HashString());
            } else {
                statement.setString(1, Token);
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE SETTING USER " + uuid + " GOOGLE AUTH STATUS", e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the fly status for the player
     *
     * @param Value true/false
     */
    public final void setFly(boolean Value) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET FLY=? WHERE UUID=?");

            statement.setBoolean(1, Value);
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE SETTING USER " + uuid + " FLY STATUS", e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get the player name
     *
     * @return a String
     */
    public final String getName() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            return results.getString("PLAYER");
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING USERNAME FROM UUID " + uuid, e);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    public final void setName(String name) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET PLAYER=? WHERE UUID=?");

            statement.setString(1, name);
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE SETTING USERNAME FOR " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /*
    Removed in 3.4.7, this is no longer used, now
    checkTables and checkUUIDTables does all the work
     */
    @Deprecated
    public final void saveUUID(String name) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET UUID=? WHERE PLAYER=?");

            statement.setString(1, uuid);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE SAVING UUID FOR " + name, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Gets the player UUID
     *
     * @return a UUID
     */
    public final UUID getUUID() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            return UUID.fromString(results.getString("UUID"));
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING STORED UUID FOR " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
        return null;
    }

    /**
     * Gets the player encrypted password
     *
     * @return a String
     */
    public final String getPassword() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            return results.getString("PASSWORD");
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING USER PASSWORD" + uuid, e);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get the user pin
     *
     * @return a String
     */
    public final String getPin() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            return results.getString("PIN");
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING USER PIN " + uuid, e);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Check if the player has 2fa enabled in
     * his account
     *
     * @return a boolean
     */
    public final boolean has2fa() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            return results.getInt("FAON") == 1;
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CHECKING USER " + uuid + " 2FA STATUS", e);
            return false;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get the player gAuth token
     *
     * @return a String
     */
    public final String getToken() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            return results.getString("GAUTH");
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING USER " + uuid + " GOOGLE AUTH TOKEN", e);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Check if the player has fly
     *
     * @return a bollean
     */
    public final boolean hasFly() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            results.next();
            if (!String.valueOf(results.getInt("FLY")).isEmpty()) {
                return results.getInt("FLY") == 1;
            } else {
                return false;
            }
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CHECKING USER " + uuid + " FLY STATUS", e);
            return false;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get all the UUIDs listed on the MySQL
     *
     * @return a list of Strings
     */
    public final List<String> getUUIDs() {
        checkTables();
        Connection connection = null;
        PreparedStatement statement = null;
        List<String> UUIDs = new ArrayList<>();

        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table);

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                UUIDs.add(results.getString("UUID"));
            }
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE GETTING ALL STORED UUIDS", e);
        } finally {
            Bucket.close(connection, statement);
        }
        return UUIDs;
    }

    /**
     * Check all table values and fix them
     */
    public void checkTables() {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table);

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String name = results.getString("PLAYER");
                String id = results.getString("UUID");
                if (id == null || id.isEmpty()) {
                    UUID uuid = getUUID(name);
                    if (uuid != null && !uuid.toString().isEmpty()) {
                        statement = connection.prepareStatement("UPDATE " + table + " SET UUID=? WHERE PLAYER=?");

                        statement.setString(1, uuid.toString());
                        statement.setString(2, name);

                        statement.executeUpdate();

                        Logger.log(Platform.ANY, "INFO", "FIXED MYSQL USER {player} TABLE".replace("{player}", name));
                    } else {
                        Logger.log(Platform.ANY, "ERROR", "AN ERROR OCCURRED WHILE GETTING USER {user} UUID".replace("{user}", name));
                    }
                }
            }
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CHECKING MYSQL TABLES", e);
        } finally {
            Bucket.close(connection, statement);
        }
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