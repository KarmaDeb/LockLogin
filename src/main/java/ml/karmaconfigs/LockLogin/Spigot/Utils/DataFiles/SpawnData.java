package ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.World;

public final class SpawnData implements LockLoginSpigot {

    private final FileManager manager;

    /**
     * Initialize spawn file
     */
    public SpawnData() {
        FileCreator creator = new FileCreator("spawn.yml", "", true);

        creator.createFile();
        creator.setDefaults();

        manager = new FileManager("spawn.yml");
    }

    /**
     * Check if the player world is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasWorld() {
        return !StringUtils.isNull(manager.get("Spawn.World"));
    }

    /**
     * Check if the player X is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasX() {
        return !StringUtils.isNull(manager.get("Spawn.X"));
    }

    /**
     * Check if the player Y is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasY() {
        return !StringUtils.isNull(manager.get("Spawn.Y"));
    }

    /**
     * Check if the player Z is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasZ() {
        return !StringUtils.isNull(manager.get("Spawn.Z"));
    }

    /**
     * Check if the player Pitch is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasPitch() {
        return !StringUtils.isNull(manager.get("Spawn.Pitch"));
    }

    /**
     * Check if the player Yaw is
     * usable
     *
     * @return a boolean
     */
    public final boolean hasYaw() {
        return !StringUtils.isNull(manager.get("Spawn.Yaw"));
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
     * @return a double
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
     * @return a double
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
     * @return a double
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
     * @return a float
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
     * @return a float
     */
    public final float getYaw() {
        if (hasYaw()) {
            return manager.getFloat("Spawn.Yaw");
        } else {
            return plugin.getServer().getWorlds().get(0).getSpawnLocation().getYaw();
        }
    }

    public final Location getSpawn() {
        Location spawn = new Location(getWorld(), getX(), getY(), getZ());
        spawn.setPitch(getPitch());
        spawn.setYaw(getYaw());

        return spawn;
    }

    public final void setSpawn(Location location) {
        manager.set("Spawn.World", location.getWorld().getName());
        manager.set("Spawn.X", location.getX());
        manager.set("Spawn.Y", location.getY());
        manager.set("Spawn.Z", location.getZ());
        manager.set("Spawn.Pitch", location.getPitch());
        manager.set("Spawn.Yaw", location.getYaw());
    }
}
