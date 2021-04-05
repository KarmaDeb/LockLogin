package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.api.bukkit.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class SpawnData implements LockLoginSpigot {

    private final FileManager manager;

    /**
     * Initialize spawn file
     */
    public SpawnData() {
        File spawn = new File(plugin.getDataFolder(), "spawn.yml");
        FileCopy spawn_copy = new FileCopy(plugin, "auto-generated/spawn.yml").withDebug(FileInfo.apiDebug(new File(jar)));

        try {
            spawn_copy.copy(spawn);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        manager = new FileManager("spawn.yml");
        manager.setInternal("auto-generated/spawn.yml");
    }

    /**
     * Check if the player world is
     * usable
     *
     * @return if the plugin spawn has a world
     */
    public final boolean hasWorld() {
        return !StringUtils.isNullOrEmpty(manager.get("Spawn.World"));
    }

    /**
     * Check if the player X is
     * usable
     *
     * @return if the plugin spawn has X value
     */
    public final boolean hasX() {
        return !StringUtils.isNullOrEmpty(manager.get("Spawn.X"));
    }

    /**
     * Check if the player Y is
     * usable
     *
     * @return if the plugin spawn has Y value
     */
    public final boolean hasY() {
        return !StringUtils.isNullOrEmpty(manager.get("Spawn.Y"));
    }

    /**
     * Check if the player Z is
     * usable
     *
     * @return if the plugin spawn has Z value
     */
    public final boolean hasZ() {
        return !StringUtils.isNullOrEmpty(manager.get("Spawn.Z"));
    }

    /**
     * Check if the player Pitch is
     * usable
     *
     * @return if the plugin spawn has pitch
     */
    public final boolean hasPitch() {
        return !StringUtils.isNullOrEmpty(manager.get("Spawn.Pitch"));
    }

    /**
     * Check if the player Yaw is
     * usable
     *
     * @return if the plugin spawn has yaw
     */
    public final boolean hasYaw() {
        return !StringUtils.isNullOrEmpty(manager.get("Spawn.Yaw"));
    }

    /**
     * Get the player last world if
     * available, if not, current
     * will be used
     *
     * @return the plugin spawn world name
     */
    public final String getWorldName() {
        if (hasWorld()) {
            return manager.getString("Spawn.World");
        } else {
            return plugin.getServer().getWorlds().get(0).getName();
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
     * @return the plugin spawn X value
     */
    public final double getX() {
        if (hasX()) {
            return manager.getDouble("Spawn.X");
        } else {
            return plugin.getServer().getWorlds().get(0).getSpawnLocation().getX();
        }
    }

    /**
     * Get the player last Y if
     * available, if not, current
     * will be used
     *
     * @return the plugin spawn Y value
     */
    public final double getY() {
        if (hasY()) {
            return manager.getDouble("Spawn.Y");
        } else {
            return plugin.getServer().getWorlds().get(0).getSpawnLocation().getY();
        }
    }

    /**
     * Get the player last Z if
     * available, if not, current
     * will be used
     *
     * @return the plugin spawn Z value
     */
    public final double getZ() {
        if (hasZ()) {
            return manager.getDouble("Spawn.Z");
        } else {
            return plugin.getServer().getWorlds().get(0).getSpawnLocation().getZ();
        }
    }

    /**
     * Get the player last Pitch if
     * available, if not, current
     * will be used
     *
     * @return the plugin spawn pitch
     */
    public final float getPitch() {
        if (hasPitch()) {
            return manager.getFloat("Spawn.Pitch");
        } else {
            return plugin.getServer().getWorlds().get(0).getSpawnLocation().getPitch();
        }
    }

    /**
     * Get the player last Yaw if
     * available, if not, current
     * will be used
     *
     * @return the plugin spawn yaw
     */
    public final float getYaw() {
        if (hasYaw()) {
            return manager.getFloat("Spawn.Yaw");
        } else {
            return plugin.getServer().getWorlds().get(0).getSpawnLocation().getYaw();
        }
    }

    /**
     * Get the plugin spawn
     *
     * @return the plugin spawn
     */
    public final Location getSpawn() {
        Location spawn = new Location(getWorld(), getX(), getY(), getZ());
        spawn.setPitch(getPitch());
        spawn.setYaw(getYaw());

        return spawn;
    }

    /**
     * Set the plugin spawn location
     *
     * @param location the location
     */
    public final void setSpawn(Location location) {
        manager.set("Spawn.World", location.getWorld().getName());
        manager.set("Spawn.X", location.getX());
        manager.set("Spawn.Y", location.getY());
        manager.set("Spawn.Z", location.getZ());
        manager.set("Spawn.Pitch", location.getPitch());
        manager.set("Spawn.Yaw", location.getYaw());
    }
}
