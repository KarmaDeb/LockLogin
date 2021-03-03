package ml.karmaconfigs.lockloginsystem.shared.llsql;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
public final class Utils {

    private final static HashMap<UUID, String> offline_conversion = new HashMap<>();
    private final String table = Bucket.getTable();
    private String uuid;

    /**
     * Starts the MySQL management
     * without any UUID
     * <code>This should be used only to
     * get a list of the registered UUIDs</code>
     */
    public Utils() {}

    /**
     * Starts the MySQL management
     *
     * @param uuid the player UUID
     */
    public Utils(UUID uuid) {
        this.uuid = uuid.toString();

        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {
        }
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
        } catch (NoClassDefFoundError ignored) {
        }
    }

    /**
     * Starts the MySQL management
     *
     * @param player the offline player
     */
    public Utils(OfflinePlayer player) {
        if (!offline_conversion.containsKey(player.getUniqueId()) || offline_conversion.get(player.getUniqueId()).equals(player.getUniqueId().toString())) {
            String stored_uuid = fetchUUID(player.getName());

            if (stored_uuid == null) {
                ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter spigotConfig = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter();

                if (!PlatformUtils.isPremium()) {
                    if (spigotConfig.registerRestricted() && spigotConfig.semiPremium()) {
                        this.uuid = mojangUUID(player.getName()).toString().replace("-", "");
                    } else {
                        this.uuid = player.getUniqueId().toString();
                    }
                } else {
                    if (spigotConfig.semiPremium())
                        this.uuid = player.getUniqueId().toString().replace("-", "");
                    else {
                        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(StandardCharsets.UTF_8)).toString();
                    }
                }
            } else {
                this.uuid = stored_uuid;
            }

            offline_conversion.put(player.getUniqueId(), this.uuid);
        } else {
            this.uuid = offline_conversion.getOrDefault(player.getUniqueId(), player.getUniqueId().toString());
        }

        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {
        }
    }

    /**
     * Starts the MySQL management
     *
     * @param player the player
     */
    public Utils(ProxiedPlayer player) {
        if (!offline_conversion.containsKey(player.getUniqueId()) || offline_conversion.get(player.getUniqueId()).equals(player.getUniqueId().toString())) {
            String stored_uuid = fetchUUID(player.getName());

            if (stored_uuid == null) {
                ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter bungeeConfig = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter();

                if (!PlatformUtils.isPremium()) {
                    if (bungeeConfig.registerRestricted() && bungeeConfig.semiPremium()) {
                        this.uuid = mojangUUID(player.getName()).toString().replace("-", "");
                    } else {
                        this.uuid = player.getUniqueId().toString();
                    }
                } else {
                    if (bungeeConfig.semiPremium()) {
                        this.uuid = player.getUniqueId().toString().replace("-", "");
                    } else {
                        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(StandardCharsets.UTF_8)).toString();
                    }
                }
            } else {
                this.uuid = stored_uuid;
            }

            offline_conversion.put(player.getUniqueId(), this.uuid);
        } else {
            this.uuid = offline_conversion.getOrDefault(player.getUniqueId(), player.getUniqueId().toString());
        }

        try {
            checkTables();
        } catch (NoClassDefFoundError ignored) {
        }
    }

    /**
     * Checks if the MySQL user exists
     *
     * @return if the user exists in the mysql
     */
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
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while checking MySQL user existence for " + uuid, Level.INFO);
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
            if (!results.next())
                if (!userExists()) {
                    PreparedStatement add = connection.prepareStatement("INSERT INTO " + table + "(PLAYER,EMAIL,UUID,PASSWORD,PIN,FAON,GAUTH,FLY) VALUE (?,?,?,?,?,?,?,?)");

                    add.setString(1, "");
                    add.setString(2, "");
                    add.setString(3, uuid);
                    add.setString(4, "");
                    add.setString(5, "");
                    add.setBoolean(6, false);
                    add.setString(7, "");
                    add.setBoolean(8, false);
                    add.executeUpdate();
                }
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while creating MySQL user " + uuid, Level.INFO);
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
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while creating MySQL user " + uuid + ", switching to 2nd method", Level.INFO);
            try {
                connection = Bucket.getBucket().getConnection();
                statement = connection.prepareStatement("DELETE FROM " + table + " WHERE UUID=?");

                statement.setString(1, uuid);

                statement.executeUpdate();
            } catch (Throwable ex) {
                PlatformUtils.log(e, Level.GRAVE);
                PlatformUtils.log("Error while creating MySQL user " + uuid, Level.INFO);
            }
        } finally {
            Bucket.close(connection, statement);
        }
    }

    private void registerAzuriom() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET NAME=? WHERE UUID=?");

            statement.setString(1, getName());
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while registering " + uuid + " as azuriom user", Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    public final String getEmail() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("EMAIL");
            else
                return "";
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user from " + uuid, Level.INFO);
            return "";
        } finally {
            Bucket.close(connection, statement);
        }
    }

    public final void setEmail(String email) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET EMAIL=? WHERE UUID=?");

            if (email != null) {
                statement.setString(1, email);
            } else {
                statement.setString(1, "");
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while setting MySQL user password of " + uuid, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }

        registerAzuriom();
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

            if (password != null) {
                if (!literal) {
                    if (Bucket.isAzuriom()) {
                        statement.setString(1, new PasswordUtils(password).encrypt(PlatformUtils.getEncryption(PlatformUtils.CryptTarget.PASSWORD)));
                    } else {
                        statement.setString(1, new PasswordUtils(password).hashToken(PlatformUtils.getEncryption(PlatformUtils.CryptTarget.PASSWORD)));
                    }
                } else {
                    if (Bucket.isAzuriom()) {
                        PasswordUtils utils = new PasswordUtils(password);

                        statement.setString(1, utils.unHash());
                    } else {
                        statement.setString(1, password);
                    }
                }
            } else {
                statement.setString(1, "");
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while setting MySQL user password of " + uuid, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the user account pin
     *
     * @param pin     the pin
     * @param literal is the pin encrypted?
     */
    public final void setPin(Object pin, boolean literal) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET PIN=? WHERE UUID=?");

            if (!literal) {
                statement.setString(1, new PasswordUtils(pin.toString()).hashToken(PlatformUtils.getEncryption(PlatformUtils.CryptTarget.PIN)));
            } else {
                statement.setString(1, pin.toString());
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while setting MySQL user pin of " + uuid, Level.INFO);
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
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while deleting MySQL user pin of " + uuid, Level.INFO);
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
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user google auth status of " + uuid, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the account's 2fa auth token
     *
     * @param Token  the token
     * @param hashed is the token hashed?
     */
    public final void setGAuth(String Token, boolean hashed) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET GAUTH=? WHERE UUID=?");

            if (hashed) {
                statement.setString(1, new PasswordUtils(Token).hash());
            } else {
                statement.setString(1, Token);
            }
            statement.setString(2, uuid);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while setting MySQL user google auth status of " + uuid, Level.INFO);
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
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while setting MySQL user fly status of " + uuid, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get the player name
     *
     * @return the client name
     */
    public final String getName() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("PLAYER");
            else
                return "";
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user from " + uuid, Level.INFO);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Set the player name
     *
     * @param name the new name
     */
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
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while setting MySQL user name of " + uuid, Level.INFO);
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
        /*Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET UUID=? WHERE PLAYER=?");

            statement.setString(1, uuid);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while saving MySQL user uuid of " + uuid, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }*/
    }

    /**
     * Migrate from AuthMe tables
     *
     * @param realname_column the real name player column
     * @param password_column the password player column
     */
    public final boolean migrateAuthMe(final String realname_column, final String password_column) {
        if (Bucket.columnSet(realname_column) && Bucket.columnSet(password_column)) {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                connection = Bucket.getBucket().getConnection();
                statement = connection.prepareStatement("UPDATE " + table + " SET PLAYER = " + realname_column + ", PASSWORD = " + password_column);

                statement.executeUpdate();

                if (!realname_column.equalsIgnoreCase("PLAYER"))
                    Bucket.removeColumn(realname_column);
                if (!password_column.equalsIgnoreCase("PASSWORD"))
                    Bucket.removeColumn(password_column);

                checkTables();
                return true;
            } catch (Throwable ex) {
                PlatformUtils.log(ex, Level.GRAVE);
                PlatformUtils.log("Error while migrating MySQL authme accounts", Level.INFO);
            } finally {
                Bucket.close(connection, statement);
            }
        }

        return false;
    }

    /**
     * Migrate from LoginSecurity tables
     */
    public final boolean migrateLoginSecurity() {
        if (Bucket.columnSet("last_name")) {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                connection = Bucket.getBucket().getConnection();
                statement = connection.prepareStatement("UPDATE " + table + " SET PLAYER = last_name");

                statement.executeUpdate();

                Bucket.removeColumn("last_name");
                checkTables();
                return true;
            } catch (Throwable ex) {
                PlatformUtils.log(ex, Level.GRAVE);
                PlatformUtils.log("Error while migrating MySQL authme accounts", Level.INFO);
            } finally {
                Bucket.close(connection, statement);
            }
        }

        return false;
    }

    /**
     * Gets the player UUID
     *
     * @return the player UUID
     */
    public final String getUUID() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("UUID");
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL stored UUID of " + uuid, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
        return null;
    }

    /**
     * Gets the player UUID
     *
     * @param player the player
     * @return the player UUID
     */
    public final String fetchUUID(final String player) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE PLAYER=?");

            statement.setString(1, player);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("UUID");
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL stored UUID of " + player, Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
        return null;
    }

    /**
     * Gets the player encrypted password
     *
     * @return the player password
     */
    public final String getPassword() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("PASSWORD");
            else
                return "";
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user password of " + uuid, Level.INFO);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get the user pin
     *
     * @return the player pin
     */
    public final String getPin() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("PIN");
            else
                return "";
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user pin of " + uuid, Level.INFO);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Check if the player has 2fa enabled in
     * his account
     *
     * @return if the player has 2Fa in his account
     */
    public final boolean has2fa() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getInt("FAON") == 1;
            else
                return false;
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user google auth status of " + uuid, Level.INFO);
            return false;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get the player gAuth token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getString("GAUTH");
            else
                return "";
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user google auth token of " + uuid, Level.INFO);
            return null;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Check if the player has fly
     *
     * @return if the player has fly
     */
    public final boolean hasFly() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next())
                if (!String.valueOf(results.getInt("FLY")).isEmpty()) {
                    return results.getInt("FLY") == 1;
                } else {
                    return false;
                }
            else
                return false;
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL user fly status of " + uuid, Level.INFO);
            return false;
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get all the UUIDs listed on the MySQL
     *
     * @return a list off all mysql stored UUIDs
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
            while (results.next())
                UUIDs.add(results.getString("UUID"));
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while getting MySQL stored UUIDs", Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }

        return UUIDs;
    }

    public final boolean usesAzuriom() {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Bucket.getBucket().getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE AZURIOM=?");

            statement.setInt(1, 1);

            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return true;
            }
        } catch (Throwable ex) {
            PlatformUtils.log(ex, Level.GRAVE);
            PlatformUtils.log("Error while asking MySQL azuriom status", Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }

        return false;
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

                        Utils utils = new Utils(uuid);
                        utils.setPassword(utils.getPassword(), true);

                        PlatformUtils.log("Fixed MySQL user table of " + uuid, Level.INFO);
                    } else {
                        PlatformUtils.log("An error occurred while getting user " + name + " UUID", Level.GRAVE);
                    }
                }
            }
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while checking MySQL tables", Level.INFO);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Get an offline player uuid
     *
     * @param name the name
     * @return the uuid from the specified player name
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
     * @return the mojang uuid from the specified player name
     */
    private UUID mojangUUID(String name) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

            String UUIDJson = IOUtils.toString(new URL(url));

            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);

            try {
                return UUID.fromString(UUIDObject.get("id").toString());
            } catch (Throwable ex) {
                return fixUUID(UUIDObject.get("id").toString());
            }
        } catch (Throwable e) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Fix the trimmed UUID
     *
     * @param id the trimmed UUID
     * @return the full UUID
     * @throws IllegalArgumentException if the UUID is invalid ( not an UUID )
     */
    public static UUID fixUUID(String id) throws IllegalArgumentException {
        if (id == null) throw new IllegalArgumentException();
        if (!id.contains("-")) {
            StringBuilder builder = new StringBuilder(id.trim());
            try {
                builder.insert(20, "-");
                builder.insert(16, "-");
                builder.insert(12, "-");
                builder.insert(8, "-");
            } catch (StringIndexOutOfBoundsException e) {
                throw new IllegalArgumentException();
            }

            return UUID.fromString(builder.toString());
        }

        return UUID.fromString(id);
    }
}