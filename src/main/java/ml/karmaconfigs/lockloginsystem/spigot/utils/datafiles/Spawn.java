package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class Spawn {

    private final SpawnData data = new SpawnData();

    /**
     * Get the spawn world
     *
     * @return the plugin spawn world
     */
    public final World getWorld() {
        return data.getWorld();
    }

    /**
     * Get the spawn X
     *
     * @return the plugin spawn X value
     */
    public final double getX() {
        return data.getX();
    }

    /**
     * Get the spawn Y
     *
     * @return the plugin spawn Y value
     */
    public final double getY() {
        return data.getY();
    }

    /**
     * Get the spawn Z
     *
     * @return the plugin spawn Z value
     */
    public final double getZ() {
        return data.getZ();
    }

    /**
     * Get the spawn Pitch
     *
     * @return the plugin spawn pitch
     */
    public final double getPitch() {
        return data.getPitch();
    }

    /**
     * Get the spawn Yaw
     *
     * @return the plugin spawn yaw
     */
    public final double getYaw() {
        return data.getYaw();
    }

    /**
     * Get the spawn location
     *
     * @return the plugin spawn
     */
    public final Location getSpawn() {
        return data.getSpawn();
    }

    /**
     * Set the spawn location
     *
     * @param location the location
     */
    public final void setSpawn(Location location) {
        data.setSpawn(location);
    }
}
