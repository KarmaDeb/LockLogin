package ml.karmaconfigs.LockLogin.MySQL.SQLite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;

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

public final class SQLiteReader {

    private final File sql;
    private final String table;
    private final String realName;
    private final String passwordRow;

    /**
     * Initialize the sqlite reader
     *
     * @param database the database
     * @param table the table
     * @param realnameColumn the real name column
     * @param passwordColumn the password column
     */
    public SQLiteReader(File database, String table, String realnameColumn, String passwordColumn) {
        this.sql = database;
        this.table = table;
        this.realName = realnameColumn;
        this.passwordRow = passwordColumn;
    }

    /**
     * Try the sqlite connection
     */
    public final boolean tryConnection() {
        try {
            String path = sql.getPath().replaceAll("\\\\", "/");
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection("jdbc:sqlite:" + path);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a connection
     *
     * @return a connection
     */
    public final Connection getConnection() throws Throwable {
        String path = sql.getPath().replaceAll("\\\\", "/");
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    /**
     * Get a value from the sqlite
     *
     * @param player the player name
     * @return a value
     */
    public final String getPassword(String player) {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE " + realName + "=?");

            statement.setString(1, player);
            ResultSet results = statement.executeQuery();
            return results.getString(passwordRow);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Get a list of all the players name
     *
     * @return a hashset
     */
    public final HashSet<String> getPlayers() {
        HashSet<String> names = new HashSet<>();

        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table);

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                names.add(results.getString(realName));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return names;
    }
}
