package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import org.bukkit.Location;
import org.bukkit.World;

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
