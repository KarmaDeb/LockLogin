package ml.karmaconfigs.LockLogin.MySQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.PlatformUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;

import java.sql.*;

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
public final class Bucket {

    private static int max = 3, min = 10, timeout = 40, lifetime = 300;
    private static String host, database, table, username, password;
    private static int port;

    private static HikariDataSource dataSource;

    /**
     * Initialize the Bucket <code>
     * Connection pool
     * </code> connection
     */
    public Bucket(String host, String database, String table, String user, String password, int port, boolean useSSL) {
        Bucket.host = host;
        Bucket.database = database + "?autoReconnect=true&useSSL=" + useSSL;
        if (!table.contains("_")) {
            Bucket.table = "ll_" + table;
        } else {
            Bucket.table = table;
        }
        Bucket.username = user;
        Bucket.password = password;
        Bucket.port = port;
    }

    /**
     * Close the connection and return it to
     * the connection pool
     */
    public static void close(Connection connection, PreparedStatement statement) {
        if (connection != null) try {
            connection.close();
        } catch (Throwable ignored) {}

        if (statement != null) try {
            statement.close();
        } catch (Throwable ignored) {}
    }

    /**
     * Terminate the MySQL connection pool
     */
    public static void terminateMySQL() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Get the MySQL connection
     *
     * @return a connection
     */
    public static HikariDataSource getBucket() {
        return dataSource;
    }

    /**
     * Get the MySQL table
     *
     * @return a String
     */
    public static String getTable() {
        return table;
    }

    /**
     * Get the MySQL password
     *
     * @return a String
     */
    public static String getPassword() {
        return password;
    }

    /**
     * Get the MySQL port
     *
     * @return an integer
     */
    public static int getPort() {
        return port;
    }

    /**
     * Set extra Bucket options
     *
     * @param max the max amount of connections
     * @param min the minimum amount of connections
     * @param timeout the connections time outs
     * @param lifetime the connection life time
     */
    public final void setOptions(int max, int min, int timeout, int lifetime) {
        Bucket.max = max;
        Bucket.min = min;
        Bucket.timeout = timeout;
        Bucket.lifetime = lifetime;

        setup();
    }

    /**
     * Setup the bucket connection
     * <code>Initialize the pool of connections</code>
     */
    private void setup() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(min);
        config.setMaximumPoolSize(max);
        config.setMaxLifetime(lifetime * 1000);
        config.setConnectionTimeout(timeout * 1000);
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Initialize the MySQL tables
     */
    public final void prepareTables() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (PLAYER text, UUID text, PASSWORD text, FAON boolean, GAUTH text, FLY boolean, PIN text)");

            statement.executeUpdate();

            if (columnSet("realname")) {
                renameColumn("realname", "PLAYER", "text");
            }

            removeAndRenameTables();
        } catch (Throwable e) {
            e.printStackTrace();
            //Logger.log(Platform.ANY, "ERROR WHILE CREATING MYSQL TABLES", e);
        } finally {
            close(connection, statement);
        }
    }

    /**
     * Remove unused tables and rename the used ones
     * to match with LockLogin database format
     */
    private void removeAndRenameTables() {
        boolean changes = false;
        Connection connection = null;
        PreparedStatement statement = null;

        if (columnNotSet("PLAYER")) {
            changes = insertColumn("PLAYER", "text");
        }
        if (columnNotSet("UUID")) {
            changes = insertColumn("UUID", "text");
        }
        if (columnNotSet("PASSWORD")) {
            changes = insertColumn("PASSWORD", "text");
        }
        if (columnNotSet("FAON")) {
            changes = insertColumn("FAON", "boolean");
        }
        if (columnNotSet("GAUTH")) {
            changes = insertColumn("GAUTH", "text");
        }
        if (columnNotSet("FLY")) {
            changes = insertColumn("FLY", "boolean");
        }
        if (columnNotSet("PIN")) {
            changes = insertColumn("PIN", "text");
        }

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table);

            ResultSet rs = statement.executeQuery();
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++ ) {
                String name = rsMetaData.getColumnName(i);

                if (!name.equalsIgnoreCase("player")
                        && !name.equalsIgnoreCase("uuid")
                        && !name.equalsIgnoreCase("password")
                        && !name.equalsIgnoreCase("faon")
                        && !name.equalsIgnoreCase("gauth")
                        && !name.equalsIgnoreCase("fly")
                        && !name.equalsIgnoreCase("pin")) {
                    changes = deleteColumn(name);
                }
            }
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE RESOLVING MYSQL TABLES", e);
        } finally {
            close(connection, statement);
        }

        if (changes) {
            PlatformUtils.Alert("MySQL tables have been resolved", WarningLevel.WARNING);
        }
    }

    /**
     * Rename a column
     *
     * @param column the column
     * @param newName the new column name
     */
    private void renameColumn(String column, String newName, String type) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("ALTER TABLE " + table + " CHANGE " + column + " " + newName + " " + type);
            statement.executeUpdate();

        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE RENAMING MYSQL COLUMN " + column, e);
        } finally {
            close(connection, statement);
        }
    }

    /**
     * Remove the column
     *
     * @param column the column name
     * @return a boolean
     */
    private boolean deleteColumn(String column) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("ALTER TABLE " + table + " " + "DROP " + column);
            statement.executeUpdate();

            PlatformUtils.Alert("Removed column " + column, WarningLevel.WARNING);

            return true;
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE REMOVING MYSQL COLUMN " + column, e);
        } finally {
            close(connection, statement);
        }
        return false;
    }

    /**
     * Insert a column in the MySQL table
     *
     * @param column the column name
     * @return a boolean
     */
    private boolean insertColumn(String column, String type) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("ALTER TABLE " + table + " " + "ADD "  + column +" " + type);
            statement.executeUpdate();

            return true;
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE REMOVING MYSQL COLUMN " + column, e);
        } finally {
            close(connection, statement);
        }
        return false;
    }

    /**
     * Check if the specified column exists
     *
     * @param column the column
     * @return a boolean
     */
    private boolean columnNotSet(String column) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, table, column);
            return !rs.next();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CHECKING MYSQL COLUMN EXISTENCE OF " + column, e);
            return true;
        } finally {
            close(connection, null);
        }
    }

    /**
     * Check if the specified column exists
     *
     * @param column the column
     * @return a boolean
     */
    private boolean columnSet(String column) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, table, column);
            return rs.next();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE CHECKING MYSQL COLUMN EXISTENCE OF " + column, e);
            return true;
        } finally {
            close(connection, null);
        }
    }
}
