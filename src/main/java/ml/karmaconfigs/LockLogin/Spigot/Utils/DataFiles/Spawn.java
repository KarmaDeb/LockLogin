package ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles;

import org.bukkit.Location;
import org.bukkit.World;

public final class Spawn {

    private final SpawnData data = new SpawnData();

    /**
     * Get the spawn world
     *
     * @return a World
     */
    public final World getWorld() {
        return data.getWorld();
    }

    /**
     * Get the spawn X
     *
     * @return a double
     */
    public final double getX() {
        return data.getX();
    }

    /**
     * Get the spawn Y
     *
     * @return a double
     */
    public final double getY() {
        return data.getY();
    }

    /**
     * Get the spawn Z
     *
     * @return a double
     */
    public final double getZ() {
        return data.getZ();
    }

    /**
     * Get the spawn Pitch
     *
     * @return a double
     */
    public final double getPitch() {
        return data.getPitch();
    }

    /**
     * Get the spawn Yaw
     *
     * @return a double
     */
    public final double getYaw() {
        return data.getYaw();
    }

    /**
     * Get the spawn location
     *
     * @return a location
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
