package ml.karmaconfigs.LockLogin.BungeeCord.Utils.User;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileManager;

import java.io.File;
import java.util.UUID;

public final class OfflineUser implements LockLoginBungee {

    private static FileManager manager;
    private final String Name;

    /**
     * Initialize the offline player
     * management
     *
     * @param name the player name
     */
    public OfflineUser(String name) {
        this.Name = name;
        checkFiles();
    }

    private void checkFiles() {
        File folder = new File(plugin.getDataFolder() + "/playerdata");

        if (folder.exists()) {
            if (folder.listFiles() != null) {
                File[] files = folder.listFiles();
                assert files != null;
                for (File file : files) {
                    FileManager fileManager = new FileManager(file.getName(), "playerdata");

                    if (fileManager.getString("Player").equals(Name)) {
                        manager = new FileManager(file.getName(), "playerdata");
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
     * @return a boolean
     */
    public final boolean exists() {
        return manager != null;
    }

    /**
     * Get the offline player UUID
     *
     * @return an UUID
     */
    public final UUID getUUID() {
        return UUID.fromString(manager.getString("UUID"));
    }

    /**
     * Check if the player has 2fa
     *
     * @return a boolean
     */
    public final boolean has2FA() {
        return manager.getBoolean("2FA");
    }

    /**
     * Get the user token
     *
     * @return a String
     */
    public final String getToken() {
        return manager.getString("GAuth");
    }

    /**
     * Delete the offline player
     * data file
     */
    public final void delete() {
        manager.delete();
    }
}
