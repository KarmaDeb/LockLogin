package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public final class PlayerFile implements LockLoginSpigot, SpigotFiles {

    private final Player player;
    private final String uuid;

    private FileManager manager;

    /**
     * Start the player file manager
     *
     * @param player the player
     */
    public PlayerFile(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId().toString().replace("-", "");

        if (config.isYaml()) {
            FileCreator creator = new FileCreator(uuid + ".yml", "playerdata", "userTemplate.yml");
            if (!creator.exists()) {
                creator.createFile();
                creator.setDefaults();
            }
            manager = new FileManager(uuid + ".yml", "playerdata");

            if (manager.isSet("Security.Password")) {
                manager.set("Password", manager.getString("Security.Password"));
                manager.unset("Security.Password");
            }
            if (manager.isSet("Security.GAuth")) {
                manager.set("GAuth", manager.getString("Security.GAuth"));
                manager.unset("Security.GAuth");
            }
            if (manager.isSet("Security.2FA")) {
                manager.set("2FA", manager.getBoolean("Security.2FA"));
                manager.unset("Security.2FA");
            }
        }
    }

    /**
     * Check if the player file is old
     *
     * @return a boolean
     */
    public final boolean isOld() {
        if (config.isYaml()) {
            File file = new File(plugin.getDataFolder() + "/Users", uuid + ".yml");

            return file.exists();
        } else {
            return false;
        }
    }

    /**
     * Convert the old player file
     * to the new format
     */
    public final void startConversion() {
        if (config.isYaml()) {
            FileManager old = new FileManager(uuid + ".yml", "Users");

            String name = old.getString("Player");
            String password = old.getString("Auth.Password");
            String gAuth = old.getString("2FA.gAuth");
            boolean GoogleAuth = old.getBoolean("2FA.enabled");
            boolean fly = old.getBoolean("Fly");

            manager.set("Player", name);
            manager.set("UUID", player.getUniqueId().toString());
            manager.set("Password", password);
            manager.set("Pin", "");
            manager.set("GAuth", gAuth);
            manager.set("2FA", GoogleAuth);
            manager.set("Fly", fly);

            old.delete();
            File file = new File(plugin.getDataFolder() + "/Users");
            File[] files = file.listFiles();
            if (files == null) {
                out.Alert("Old player data folder have been removed (ALL OLD PLAYER DATA HAVE BEEN CONVERTED)", WarningLevel.WARNING);
            } else {
                if (Arrays.toString(files).isEmpty()) {
                    out.Alert("Old player data folder have been removed (ALL OLD PLAYER DATA HAVE BEEN CONVERTED)", WarningLevel.WARNING);
                } else {
                    for (File f : files) {
                        if (f.length() <= 4) {
                            if (f.delete()) {
                                out.Alert("Deleting file " + file.getName() + " due doesn't contains important player data", WarningLevel.WARNING);
                            } else {
                                out.Alert("An error occurred while trying to remove file " + file.getName(), WarningLevel.WARNING);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Setup the player file
     */
    public final void setupFile() {
        if (config.isYaml()) {
            if (manager.isEmpty("Player")) {
                manager.set("Player", player.getName());
            }
            if (manager.isEmpty("UUID")) {
                manager.set("UUID", player.getUniqueId().toString());
            }
        }
    }

    /**
     * Set the player fly status
     *
     * @param value true/false
     */
    public final void setFly(boolean value) {
        manager.set("Fly", value);
    }

    /**
     * Get the player UUID
     */
    public final UUID getUUID() {
        return UUID.fromString(manager.getString("UUID"));
    }

    /**
     * Get the player password
     *
     * @return a String
     */
    public final String getPassword() {
        return manager.getString("Password");
    }

    /**
     * Get the player pin
     *
     * @return a String
     */
    public final String getPin() {
        return manager.getString("Pin");
    }

    /**
     * Set the player's password
     *
     * @param newPassword the new player password
     */
    public final void setPassword(String newPassword) {
        manager.set("Password", new PasswordUtils(newPassword).Hash());
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        manager.set("Pin", new PasswordUtils(String.valueOf(pin)).Hash());
    }

    /**
     * Remove the player pin
     */
    public final void delPin() {
        manager.set("Pin", "");
    }

    /**
     * Get player Google Authenticator token
     *
     * @return a String
     */
    public final String getToken() {
        return manager.getString("GAuth");
    }

    /**
     * Set the player Google Authenticator token
     *
     * @param token the token
     */
    public final void setToken(String token) {
        manager.set("GAuth", new PasswordUtils(token).HashString());
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
     * Check if the player has fly
     *
     * @return a boolean
     */
    public final boolean hasFly() {
        return manager.getBoolean("Fly").equals(true);
    }

    /**
     * Set player 2fa status
     *
     * @param status the status
     */
    public final void set2FA(boolean status) {
        manager.set("2FA", status);
    }

    /**
     * Remove the player file
     */
    public final void removeFile() {
        manager.delete();
    }
}
