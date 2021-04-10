package ml.karmaconfigs.lockloginsystem.bungee.utils.user;

import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountID;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountManager;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class OfflineUser implements LockLoginBungee, BungeeFiles {

    private final String uuid;
    private final String name;

    private AccountManager manager;

    /**
     * Initialize the offline player
     * management
     *
     * @param uuid   the player uuid
     * @param name   the player name
     * @param byName fetch by name
     */
    public OfflineUser(final String uuid, String name, final boolean byName) {
        this.uuid = uuid;
        this.name = name;
        checkFiles(byName);
    }

    private void checkFiles(boolean byName) {
        /*
        if (config.isYaml()) {
            File folder = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "accounts");

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
                                manager = new KarmaFile(file);
                        } else {
                            KarmaFile temManager = new KarmaFile(file);

                            if (temManager.getString("PLAYER", "").equals(name))
                                manager = temManager;
                        }
                    }
                }
            }
        } else {
            Utils utils = new Utils();
            List<String> uuids = utils.getUUIDs();

            for (String id : uuids) {
                if (id != null) {
                    if (managerSQL != null)
                        break;

                    if (!id.replaceAll("\\s", "").isEmpty()) {
                        if (name.isEmpty())
                            name = new Utils().fetchName(id);

                        Utils idUtils = new Utils(id, name);
                        if (idUtils.getName() != null && idUtils.getName().equals(name))
                            managerSQL = idUtils;
                        else if (id.equals(uuid))
                            managerSQL = idUtils;
                    }
                }
            }
        }*/

        if (PlatformUtils.accountManagerValid()) {
            AccountManager currentManager = PlatformUtils.getManager(null);
            assert currentManager != null;

            for (AccountManager acManager : currentManager.getAccounts()) {
                if (byName) {
                    if (acManager.getName().equals(name)) {
                        manager = acManager;
                        break;
                    }
                } else {
                    if (acManager.getUUID().getId().equals(uuid)) {
                        manager = acManager;
                        break;
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
        return manager != null && manager.exists();
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    public final String getName() {
        return manager.getName();
    }

    /**
     * Get the offline player UUID
     *
     * @return the player UUID
     */
    public final AccountID getUUID() {
        return manager.getUUID();
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2fa
     */
    public final boolean has2FA() {
        return manager.has2FA();
    }

    /**
     * Check if the player is registered
     *
     * @return if the player is registered
     */
    public final boolean isRegistered() {
        return !manager.getPassword().replace("PASSWORD:", "").replaceAll("\\s", "").isEmpty();
    }

    /**
     * Get the user token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        return manager.getGAuth();
    }

    /**
     * Delete the offline player
     * data file
     */
    public final void delete() {
        manager.remove();
    }
}
