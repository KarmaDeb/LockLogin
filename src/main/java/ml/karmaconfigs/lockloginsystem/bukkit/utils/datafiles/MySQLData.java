package ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.FileManager;

import java.util.List;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class MySQLData implements LockLoginSpigot {

    private final FileManager manager = new FileManager("mysql.yml");

    /**
     * Get the mysql host
     *
     * @return the mysql.yml host address
     */
    public final String getHost() {
        return manager.getString("MySQL.host");
    }

    /**
     * Get the mysql database
     *
     * @return the mysql.yml database name
     */
    public final String getDatabase() {
        return manager.getString("MySQL.database");
    }

    /**
     * Get the mysql port
     *
     * @return the mysql.yml connection port
     */
    public final int getPort() {
        return manager.getInt("MySQL.port");
    }

    /**
     * Get the mysql table
     *
     * @return the mysql.yml database table name
     */
    public final String getTable() {
        return manager.getString("MySQL.table");
    }

    /**
     * Get the mysql user
     *
     * @return the mysql.yml connection user name
     */
    public final String getUser() {
        return manager.getString("MySQL.user");
    }

    /**
     * Get the mysql password
     *
     * @return the mysql.yml connection user password
     */
    public final String getPassword() {
        return manager.getString("MySQL.password");
    }

    /**
     * Get if the mysql connection should
     * be with ssl
     *
     * @return the mysql.yml connection SLL option
     */
    public final boolean useSSL() {
        return manager.getBoolean("MySQL.SSL");
    }

    /**
     * Check if the mysql connection should ignore
     * CA certificates
     *
     * @return if the MySQL connection should ignore CA
     * certificates
     */
    public final boolean ignoreCertificates() {
        return manager.getBoolean("MySQL.IgnoreCertificates");
    }

    /**
     * Get a list of string with the columns that should
     * be ignored
     *
     * @return a list of the columns that will be ignored
     * on column-deletion
     */
    public final List<String> ignoredColumns() {
        return manager.getList("IgnoredColumns");
    }

    /**
     * Get the minimum connections
     * that will connect to MySQL
     *
     * @return the minimum amount of connections to keep alive
     */
    public final int getMinConnections() {
        if (manager.getInt("Connection.Min") > 0) {
            return manager.getInt("Connection.Min");
        } else {
            Console.send(plugin, "MySQL min connections were " + manager.getInt("Connection.Min") + " which could cause errors and have been set back to 3", Level.INFO);
            manager.set("Connection.Min", 3);
            return 3;
        }
    }

    /**
     * Get the maximum connections
     * that will connect to MySQL
     *
     * @return the maximum amount of connections to keep alive
     */
    public final int getMaxConnections() {
        if (manager.getInt("Connection.Max") > getMinConnections()) {
            return manager.getInt("Connection.Max");
        } else {
            Console.send(plugin, "MySQL max connections were " + manager.getInt("Connection.Max") + " which is lower than min connections (" + getMinConnections() + ") and have been set to " + getMinConnections() + 2 + " ( minConnections + 2 )", Level.INFO);
            manager.set("Connection.Max", getMinConnections() + 2);
            return getMinConnections() + 2;
        }
    }

    /**
     * Get the max time to wait a
     * response from MySQL
     *
     * @return the time (in milis) that the connection will be switched if no response
     */
    public final int getTimeOut() {
        if (manager.getInt("Connection.TimeOut") >= 30) {
            return manager.getInt("Connection.TimeOut");
        } else {
            Console.send(plugin, "MySQL connection timeout was " + manager.getInt("Connection.TimeOut") + " which is lower than the required by the plugin and have been reset to 40", Level.INFO);
            manager.set("Connection.TimeOut", 40);
            return 40;
        }
    }

    /**
     * Get connections max lifetime
     *
     * @return the connections max life time
     */
    public final int getLifeTime() {
        if (manager.getInt("Connection.LifeTime") >= 60) {
            return manager.getInt("Connection.LifeTime");
        } else {
            Console.send(plugin, "MySQL connections life time was " + manager.getInt("Connection.LifeTime") + " which is lower than 1 minute so have been set back to 300 ( 5 minutes )", Level.INFO);
            manager.set("Connection.LifeTime", 300);
            return 300;
        }
    }
}
