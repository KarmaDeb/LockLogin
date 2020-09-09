package ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.OfflineUser;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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

public final class LocationDatabase implements LockLoginSpigot {

    private String UUID;
    private Player player;

    private FileManager manager;

    /**
     * Start the location database for
     * the player
     *
     * @param player the player
     */
    public LocationDatabase(Player player) {
        FileCreator creator = new FileCreator("locations.yml", "userdata", false);
        creator.createFile();
        manager = new FileManager("locations.yml", "userdata");
        this.player = player;
        this.UUID = player.getUniqueId().toString().replace("-", "");
    }

    /**
     * Start the location database for
     * the player name
     *
     * @param player the player
     */
    public LocationDatabase(String player) {
        OfflineUser user = new OfflineUser(player);

        if (user.exists()) {
            this.player = null;
            this.UUID = user.getUUID().toString().replace("-", "");
        }
    }

    /**
     * Gets the player location
     *
     * @return a Location
     */
    private Location getLoc() {
        return player.getLocation();
    }

    /**
     * Saves the player last location
     */
    public final void saveLocation() {
        manager.set("LastLoc." + UUID + ".World", getLoc().getWorld().getName());
        manager.set("LastLoc." + UUID + ".X", getLoc().getX());
        manager.set("LastLoc." + UUID + ".Y", getLoc().getY());
        manager.set("LastLoc." + UUID + ".Z", getLoc().getZ());
        manager.set("LastLoc." + UUID + ".Pitch", getLoc().getPitch());
        manager.set("LastLoc." + UUID + ".Yaw", getLoc().getYaw());
    }

    /**
     * Removes the player last location
     */
    public final void removeLocation() {
        manager.unset("LastLoc." + UUID);
    }

    /**
     * Check if the player has last
     * location
     *
     * @return a boolean
     */
    public final boolean hasLastLoc() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID));
    }

    /**
     * Check if the player world is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasWorld() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID + ".World"));
    }

    /**
     * Check if the player X is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasX() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID + ".X"));
    }

    /**
     * Check if the player Y is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasY() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID + ".Y"));
    }

    /**
     * Check if the player Z is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasZ() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID + ".Z"));
    }

    /**
     * Check if the player Pitch is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasPitch() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID + ".Pitch"));
    }

    /**
     * Check if the player Yaw is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasYaw() {
        return !StringUtils.isNull(manager.get("LastLoc." + UUID + ".Yaw"));
    }

    /**
     * Get the player last world if
     * available, if not, current
     * will be used
     *
     * @return a double
     */
    public final String getWorldName() {
        if (hasWorld()) {
            return manager.getString("LastLoc." + UUID + ".World");
        } else {
            return getLoc().getWorld().getName();
        }
    }

    public final World getWorld() {
        return plugin.getServer().getWorld(getWorldName());
    }

    /**
     * Get the player last X if
     * available, if not, current
     * will be used
     *
     * @return a double
     */
    public final double getX() {
        if (hasX()) {
            return manager.getDouble("LastLoc." + UUID + ".X");
        } else {
            return getLoc().getX();
        }
    }

    /**
     * Get the player last Y if
     * available, if not, current
     * will be used
     *
     * @return a double
     */
    public final double getY() {
        if (hasY()) {
            return manager.getDouble("LastLoc." + UUID + ".Y");
        } else {
            return getLoc().getY();
        }
    }

    /**
     * Get the player last Z if
     * available, if not, current
     * will be used
     *
     * @return a double
     */
    public final double getZ() {
        if (hasZ()) {
            return manager.getDouble("LastLoc." + UUID + ".Z");
        } else {
            return getLoc().getZ();
        }
    }

    /**
     * Get the player last Pitch if
     * available, if not, current
     * will be used
     *
     * @return a float
     */
    public final float getPitch() {
        if (hasPitch()) {
            return manager.getFloat("LastLoc." + UUID + ".Pitch");
        } else {
            return getLoc().getPitch();
        }
    }

    /**
     * Get the player last Yaw if
     * available, if not, current
     * will be used
     *
     * @return a float
     */
    public final float getYaw() {
        if (hasYaw()) {
            return manager.getFloat("LastLoc." + UUID + ".Yaw");
        } else {
            return getLoc().getYaw();
        }
    }

    /**
     * Get the latest player location
     *
     * @return a Location (with pitch and yaw)
     */
    public final Location getLastLoc() {
        Location lastLoc = new Location(getWorld(), getX(), getY(), getZ());
        lastLoc.setPitch(getPitch());
        lastLoc.setYaw(getYaw());

        return lastLoc;
    }
}
