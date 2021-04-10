package ml.karmaconfigs.lockloginsystem.bukkit.utils.datafiles;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class LastLocation {

    private final LocationDatabase data;

    /**
     * Initialize the last location for
     * the player
     *
     * @param player the player
     */
    public LastLocation(Player player) {
        this.data = new LocationDatabase(player);
    }

    public LastLocation(String player) {
        this.data = new LocationDatabase(player);
    }

    /**
     * Check if the player has last location
     *
     * @return if the player has last location
     */
    public final boolean hasLastLocation() {
        return data.hasLastLoc();
    }

    /**
     * Get the player last location
     * world
     *
     * @return the player last location world
     */
    public final World getWorld() {
        return data.getWorld();
    }

    /**
     * Get the player last location X
     *
     * @return the player last location X value
     */
    public final double getX() {
        return data.getX();
    }

    /**
     * Get the player last location Y
     *
     * @return the player last location Y value
     */
    public final double getY() {
        return data.getY();
    }

    /**
     * Get the player last location Z
     *
     * @return the player last location Z value
     */
    public final double getZ() {
        return data.getZ();
    }

    /**
     * Get the player last location Pitch
     *
     * @return the player last location pitch
     */
    public final float getPitch() {
        return data.getPitch();
    }

    /**
     * Get the player last location Yaw
     *
     * @return the player last location yaw
     */
    public final float getYaw() {
        return data.getYaw();
    }

    /**
     * Get the player last location location
     *
     * @return the player last location
     */
    public final Location getLastLocation() {
        return data.getLastLoc();
    }

    /**
     * Set the player last location location
     */
    public final void saveLocation() {
        data.saveLocation();
    }

    /**
     * Remove the player last location location
     */
    public final void removeLocation() {
        data.removeLocation();
    }
}
