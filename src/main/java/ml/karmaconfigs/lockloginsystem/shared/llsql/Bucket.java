package ml.karmaconfigs.lockloginsystem.shared.llsql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
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
    private static boolean useSSL, ignoreCertificate;
    private static String host, database, table, username, password;
    private static int port;

    private static HikariDataSource dataSource;

    /**
     * Initialize the Bucket <code>
     * Connection pool
     * </code> connection
     *
     * @param host     the mysql host
     * @param database the mysql database name
     * @param table    the mysql database table name
     * @param user     the mysql user
     * @param password the mysql username password
     * @param port     the mysql port
     * @param useSSL   connect to mysql using SSL?
     */
    public Bucket(String host, String database, String table, String user, String password, int port, boolean useSSL, boolean ignoreCertificate) {
        Bucket.host = host;
        Bucket.database = database;
        Bucket.table = table;
        Bucket.username = user;
        Bucket.password = password;
        Bucket.port = port;
        Bucket.useSSL = useSSL;
        Bucket.ignoreCertificate = ignoreCertificate;
    }

    /**
     * Close the connection and return it to
     * the connection pool
     *
     * @param connection the mysql connection
     * @param statement  the mysql command
     */
    public static void close(Connection connection, PreparedStatement statement) {
        if (connection != null) try {
            connection.close();
        } catch (Throwable ignored) {
        }

        if (statement != null) try {
            statement.close();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Terminate the MySQL connection pool
     */
    public static void terminateMySQL() {
        /*
        try {
            if (dataSource != null) {
                dataSource.close();
            }
        } catch (Throwable ignored) {}*/
    }

    /**
     * Get the MySQL connection
     *
     * @return a connection
     */
    public static DataSource getBucket() {
        return dataSource;
    }

    /**
     * Get the MySQL table
     *
     * @return they used mysql database table
     */
    public static String getTable() {
        return table;
    }

    /**
     * Get the MySQL password
     *
     * @return the used mysql connection password
     */
    public static String getPassword() {
        return password;
    }

    /**
     * Get the MySQL port
     *
     * @return the used mysql connection port
     */
    public static int getPort() {
        return port;
    }

    /**
     * Check if the table is an azuriom table meaning
     * the server is using azuriom integration
     *
     * @return if the server is using azuriom integration
     */
    public static boolean isAzuriom() {
        return columnSet("email_verified_at");
    }

    /**
     * Insert a column in the MySQL table
     *
     * @param column the column name
     * @return if the column could be inserted
     */
    private static boolean insertColumn(String column, String type) {
        Connection connection = null;
        PreparedStatement statement = null;
        boolean status = false;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("ALTER TABLE " + table + " " + "ADD " + column + " " + type);

            try {
                statement.executeUpdate();
            } catch (Throwable ex) {
                statement.executeLargeUpdate();
            }

            status = true;
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while inserting column " + column, Level.INFO);
        } finally {
            close(connection, statement);
        }

        return status;
    }

    /**
     * Remove the specified column
     *
     * @param column the column name
     */
    static void removeColumn(final String column) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("ALTER TABLE " + table + " DROP COLUMN " + column);
            statement.executeUpdate();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while removing column " + column, Level.INFO);
        } finally {
            close(connection, statement);
        }
    }

    /**
     * Check if the specified column exists
     *
     * @param column the column
     * @return if the column exists
     */
    static boolean columnSet(String column) {
        Connection connection = null;
        boolean value = false;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, table, column);
            value = rs.next();
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while checking for column existence " + column, Level.INFO);
        } finally {
            close(connection, null);
        }

        return value;
    }

    /**
     * Set extra Bucket options
     *
     * @param max      the max amount of connections
     * @param min      the minimum amount of connections
     * @param timeout  the connections time outs
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
        config.setIdleTimeout(lifetime * 1000L);
        config.setConnectionTimeout(timeout * 1000L);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("useSSL", useSSL);
        config.addDataSourceProperty("verifyServerCertificate", ignoreCertificate);
        config.setLeakDetectionThreshold(60 * 1000);

        dataSource = new HikariDataSource(config);
    }

    /**
     * Initialize the MySQL tables
     */
    public final void prepareTables(final List<String> ignored) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (PLAYER text, EMAIL text, UUID text, PASSWORD text, FAON boolean, GAUTH text, FLY boolean, PIN text)");

            statement.executeUpdate();
            removeAndRenameTables(ignored);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            close(connection, statement);
        }
    }

    /**
     * Remove unused tables and rename the used ones
     * to match with LockLogin database format
     */
    private void removeAndRenameTables(final List<String> ignored) {
        boolean changes = false;
        Connection connection = null;
        PreparedStatement statement = null;

        if (!columnSet("PLAYER")) {
            changes = insertColumn("PLAYER", "text");
        }
        if (!columnSet("EMAIL")) {
            changes = insertColumn("EMAIL", "text");
        }
        if (!columnSet("UUID")) {
            changes = insertColumn("UUID", "text");
        }
        if (!columnSet("PASSWORD")) {
            changes = insertColumn("PASSWORD", "text");
        }
        if (!columnSet("FAON")) {
            changes = insertColumn("FAON", "boolean");
        }
        if (!columnSet("GAUTH")) {
            changes = insertColumn("GAUTH", "text");
        }
        if (!columnSet("FLY")) {
            changes = insertColumn("FLY", "boolean");
        }
        if (!columnSet("PIN")) {
            changes = insertColumn("PIN", "text");
        }

        boolean doTableCheck = true;
        List<String> lowerCaseIgnored = new ArrayList<>();
        for (String str : ignored) {
            if (str.equalsIgnoreCase("all")) {
                doTableCheck = false;
                lowerCaseIgnored.clear();
                break;
            } else {
                lowerCaseIgnored.add(str.toLowerCase());
            }
        }

        if (doTableCheck) {
            try {
                connection = dataSource.getConnection();
                statement = connection.prepareStatement("SELECT * FROM " + table);

                ResultSet rs = statement.executeQuery();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String name = rsMetaData.getColumnName(i);

                    if (lowerCaseIgnored.stream().noneMatch(name.toLowerCase()::contains)) {
                        switch (name.toLowerCase()) {
                            case "player":
                            case "email":
                            case "uuid":
                            case "password":
                            case "faon":
                            case "gauth":
                            case "fly":
                            case "pin":
                            case "id":
                            case "name":
                            case "email_verified_at":
                            case "role_id":
                            case "money":
                            case "game_id":
                            case "access_token":
                            case "google_2fa_secret":
                            case "last_login_at":
                            case "is_banned":
                            case "is_deleted":
                            case "remember_token":
                            case "created_at":
                            case "updated_at":
                            case "last_name":
                                break;
                            default:
                                removeColumn(name);
                                changes = true;
                        }
                    }
                }
            } catch (Throwable e) {
                PlatformUtils.log(e, Level.GRAVE);
                PlatformUtils.log("Error while setting up tables and columns", Level.INFO);
            } finally {
                close(connection, statement);
            }
        }

        //Check for LoginSecurity tables, to start auto-migration
        if (columnSet("last_name")) {
            try {
                connection = dataSource.getConnection();
                statement = connection.prepareStatement("UPDATE " + table + " SET PLAYER = last_name");

                statement.executeUpdate();
                removeColumn("last_name");
            } catch (Throwable ex) {
                PlatformUtils.log(ex, Level.GRAVE);
                PlatformUtils.log("Error while trying to migrate from LoginSecurity sql", Level.INFO);
            }
        }

        if (changes) {
            PlatformUtils.send("MySQL tables have been resolved", Level.INFO);
        }
    }
}
