package ml.karmaconfigs.lockloginsystem.spigot.utils.user;

import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;

import java.io.File;
import java.util.UUID;

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

public final class OfflineUser implements LockLoginSpigot {

    private FileManager manager;
    private final Object finder;

    /**
     * Initialize the offline player
     * management
     *
     * @param name the player name
     */
    public OfflineUser(String name) {
        this.finder = name;
        checkFiles();
    }

    /**
     * Initialize the offline player
     * management
     *
     * @param id the player uuid
     */
    public OfflineUser(UUID id) {
        this.finder = id;
        checkFiles();
    }

    /**
     * Check the files to search for specified player
     * file
     */
    private void checkFiles() {
        File folder = new File(plugin.getDataFolder() + "/playerdata");

        if (folder.exists()) {
            if (folder.listFiles() != null) {
                File[] files = folder.listFiles();
                assert files != null;
                for (File file : files) {
                    if (manager != null)
                        break;

                    String file_name = file.getName();
                    if (finder instanceof UUID) {
                        UUID uuid = (UUID) finder;

                        String supposed_file = uuid.toString().replace("-", "") + ".yml";

                        if (file_name.equals(supposed_file))
                            manager = new FileManager(file_name, "playerdata");
                    } else {
                        String name = String.valueOf(finder);

                        FileManager current = new FileManager(file_name, "playerdata");
                        if (current.getString("Player").equals(name))
                            manager = current;
                    }
                }
            }
        }
    }

    /**
     * Check if the player has even
     * played on the server
     *
     * @return if the player data exists
     */
    public final boolean exists() {
        return manager != null;
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    public final String getName() {
        return manager.getString("Player");
    }

    /**
     * Get the offline player UUID
     *
     * @return the player UUID
     */
    public final UUID getUUID() {
        return UUID.fromString(manager.getString("UUID"));
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2fa
     */
    public final boolean has2FA() {
        return manager.getBoolean("2FA");
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        return manager.isSet("Password") && !manager.isEmpty("Password");
    }

    /**
     * Get the user token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        return manager.getString("GAuth");
    }

    /**
     * Check if the player has fly
     *
     * @return if the player has fly
     */
    public final boolean hasFly() {
        return manager.getBoolean("Fly").equals(true);
    }

    /**
     * Delete the offline player
     * data file
     */
    public final void delete() {
        manager.delete();
    }
}
