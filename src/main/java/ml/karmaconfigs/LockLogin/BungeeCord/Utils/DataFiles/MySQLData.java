package ml.karmaconfigs.LockLogin.BungeeCord.Utils.DataFiles;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.WarningLevel;

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

public final class MySQLData implements LockLoginBungee {

    private final FileManager manager = new FileManager("mysql.yml");

    /**
     * Get the mysql host
     *
     * @return a String
     */
    public final String getHost() {
        return manager.getString("MySQL.host");
    }

    /**
     * Get the mysql database
     *
     * @return a String
     */
    public final String getDatabase() {
        return manager.getString("MySQL.database");
    }

    /**
     * Get the mysql port
     *
     * @return an integer
     */
    public final int getPort() {
        return manager.getInt("MySQL.port");
    }

    /**
     * Get the mysql table
     *
     * @return a String
     */
    public final String getTable() {
        return manager.getString("MySQL.table");
    }

    /**
     * Get the mysql user
     *
     * @return a String
     */
    public final String getUser() {
        return manager.getString("MySQL.user");
    }

    /**
     * Get the mysql password
     *
     * @return a String
     */
    public final String getPassword() {
        return manager.getString("MySQL.password");
    }

    /**
     * Get if the mysql connection should
     * be with ssl
     *
     * @return a boolean
     */
    public final boolean useSSL() {
        return manager.getBoolean("MySQL.SSL");
    }

    /**
     * Get the minimum connections
     * that will connect to MySQL
     *
     * @return an Integer
     */
    public final int getMinConnections() {
        if (manager.getInt("Connection.Min") > 0) {
            return manager.getInt("Connection.Min");
        } else {
            out.Alert("MySQL min connections were " + manager.getInt("Connection.Min") + " which could cause errors and have been set back to 3", WarningLevel.WARNING);
            manager.set("Connection.Min", 3);
            return 3;
        }
    }

    /**
     * Get the maximum connections
     * that will connect to MySQL
     *
     * @return an Integer
     */
    public final int getMaxConnections() {
        if (manager.getInt("Connection.Max") > getMinConnections()) {
            return manager.getInt("Connection.Max");
        } else {
            out.Alert("MySQL max connections were " + manager.getInt("Connection.Max") + " which is lower than min connections (" + getMinConnections() + ") and have been set to " + getMinConnections() + 2 + " ( minConnections + 2 )", WarningLevel.WARNING);
            manager.set("Connection.Max", getMinConnections() + 2);
            return getMinConnections() + 2;
        }
    }

    /**
     * Get the max time to wait a
     * response from MySQL
     *
     * @return an Integer
     */
    public final int getTimeOut() {
        if (manager.getInt("Connection.TimeOut") >= 30) {
            return manager.getInt("Connection.TimeOut");
        } else {
            out.Alert("MySQL connection timeout was " + manager.getInt("Connection.TimeOut") + " which is lower than the required by the plugin and have been reset to 40", WarningLevel.WARNING);
            manager.set("Connection.TimeOut", 40);
            return 40;
        }
    }

    /**
     * Get connections max lifetime
     *
     * @return an Integer
     */
    public final int getLifeTime() {
        if (manager.getInt("Connection.LifeTime") >= 60) {
            return manager.getInt("Connection.LifeTime");
        } else {
            out.Alert("MySQL connections life time was " + manager.getInt("Connection.LifeTime") + " which is lower than 1 minute so have been set back to 300 ( 5 minutes )", WarningLevel.WARNING);
            manager.set("Connection.LifeTime", 300);
            return 300;
        }
    }
}
