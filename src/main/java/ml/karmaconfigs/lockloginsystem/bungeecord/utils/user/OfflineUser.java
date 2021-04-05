package ml.karmaconfigs.lockloginsystem.bungeecord.utils.user;

import ml.karmaconfigs.api.bungee.KarmaFile;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

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
    private String name;

    private KarmaFile manager = null;
    private Utils managerSQL = null;

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
            return manager.getString("PLAYER", "");
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
            return UUID.fromString(manager.getString("UUID", String.valueOf(UUID.randomUUID())));
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
            return manager.getBoolean("2FA", false);
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
            return manager.isSet("PASSWORD") && !manager.getString("PASSWORD", "").isEmpty();
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
            return manager.getString("TOKEN", "");
        else
            return managerSQL.getToken();
    }

    /**
     * Delete the offline player
     * data file
     */
    public final void delete() {
        if (managerSQL == null)
            try {
                Files.delete(manager.getFile().toPath());
            } catch (Throwable ignored) {}
        else
            managerSQL.removeUser();
    }
}
