package ml.karmaconfigs.LockLogin.MySQL.Insert;

import ml.karmaconfigs.LockLogin.InsertInfo;
import ml.karmaconfigs.LockLogin.InsertReader;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.MySQL.Bucket;
import ml.karmaconfigs.LockLogin.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class BucketInserter {

    private final String table = Bucket.getTable();
    private final String name, uuid, pass, token, pin;
    private final boolean faon, fly;

    /**
     * Initialize the Bucket ( MySQL ) inserter
     *
     * @param info the info to insert
     */
    public BucketInserter(InsertInfo info) {
        InsertReader reader = new InsertReader(info.getData());
        name = reader.get("Name").toString();
        uuid = reader.get("UUID").toString();
        pass = reader.get("Password").toString();
        token = reader.get("Token").toString();
        pin = reader.get("Pin").toString();
        faon = Boolean.parseBoolean(reader.get("gAuth").toString());
        fly = Boolean.parseBoolean(reader.get("Fly").toString());
    }

    /**
     * Creates user on MySQL tables
     */
    public final void insert() {
        if (userExists(uuid)) {
            removeUser(uuid);
        }

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = Objects.requireNonNull(connection).prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);

            ResultSet results = statement.executeQuery();
            results.next();
            PreparedStatement add = connection.prepareStatement("INSERT INTO " + table + "(PLAYER,UUID,PASSWORD,PIN,FAON,GAUTH,FLY) VALUE (?,?,?,?,?,?,?)");

            add.setString(1, name);
            add.setString(2, uuid);
            add.setString(3, pass);
            add.setString(4, pin);
            add.setBoolean(5, faon);
            add.setString(6, token);
            add.setBoolean(7, fly);
            add.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE INSERTING USER " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Removes the user from the MySQL tables
     */
    private void removeUser(String uuid) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = Bucket.getBucket().getConnection();
            statement = Objects.requireNonNull(connection).prepareStatement("DELETE * FROM " + table + " WHERE UUID=?");

            statement.setString(1, uuid);

            statement.executeUpdate();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE DELETING USER " + uuid, e);
        } finally {
            Bucket.close(connection, statement);
        }
    }

    /**
     * Checks if the MySQL user exists
     *
     * @return a boolean
     */
    private boolean userExists(String uuid) {
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
}
