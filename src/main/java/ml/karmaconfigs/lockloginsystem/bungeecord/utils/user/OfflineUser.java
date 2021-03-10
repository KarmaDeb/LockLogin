package ml.karmaconfigs.lockloginsystem.bungeecord.utils.user;

import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;

import java.io.File;
import java.util.List;
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

public final class OfflineUser implements LockLoginBungee, BungeeFiles {

    private final String uuid;
    private final String name;
    private FileManager manager = null;
    private Utils managerSQL = null;

    /**
     * Initialize the offline player management
     *
     * @param uuid the player
     * @param name the player uuid
     */
    public OfflineUser(String uuid, String name, final boolean byName) {
        this.uuid = uuid;
        this.name = name;
        checkFiles(byName);
    }

    /**
     * Check the files to search for specified player
     * file
     */
    private void checkFiles(boolean byName) {
        if (config.isYaml()) {
            File folder = new File(plugin.getDataFolder() + "/playerdata");

            if (folder.exists()) {
                if (folder.listFiles() != null) {
                    File[] files = folder.listFiles();
                    assert files != null;
                    for (File file : files) {
                        if (manager != null)
                            break;

                        String file_name = file.getName();

                        if (!byName) {
                            String supposed_file = uuid.replace("-", "") + ".yml";

                            if (file_name.equals(supposed_file))
                                manager = new FileManager(file_name, "playerdata");
                        } else {
                            FileManager temManager = new FileManager(file_name, "playerdata");

                            if (temManager.getString("Player").equals(name))
                                manager = temManager;
                        }
                    }
                }
            }
        } else {
            Utils utils = new Utils();
            List<String> uuids = utils.getUUIDs();

            for (String id : uuids) {
                if (managerSQL != null)
                    break;

                Utils idUtils = new Utils(id, name);
                if (idUtils.getName() != null && idUtils.getName().equals(name))
                    managerSQL = idUtils;
                else
                    if (id.equals(uuid))
                        managerSQL = idUtils;
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
        return manager != null || managerSQL != null;
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    public final String getName() {
        if (managerSQL == null)
            return manager.getString("Player");
        else
            return managerSQL.getName();
    }

    /**
     * Get the offline player UUID
     *
     * @return the player UUID
     */
    public final UUID getUUID() {
        if (managerSQL == null)
            return UUID.fromString(manager.getString("UUID"));
        else
            return Utils.fixUUID(managerSQL.getUUID());
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2fa
     */
    public final boolean has2FA() {
        if (managerSQL == null)
            return manager.getBoolean("2FA");
        else
            return managerSQL.has2fa();
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        if (managerSQL == null)
            return manager.isSet("Password") && !manager.isEmpty("Password");
        else
            return managerSQL.getPassword() != null && !managerSQL.getPassword().isEmpty();
    }

    /**
     * Get the user token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        if (managerSQL == null)
            return manager.getString("GAuth");
        else
            return managerSQL.getToken();
    }

    /**
     * Delete the offline player
     * data file
     */
    public final void delete() {
        if (managerSQL == null)
            manager.delete();
        else
            managerSQL.removeUser();
    }
}
