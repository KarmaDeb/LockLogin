package ml.karmaconfigs.lockloginsystem.spigot.utils.user;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

/**
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
            manager = new FileManager(uuid + ".yml", "playerdata");
            manager.setInternal("auto-generated/userTemplate.yml");

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

        try {
            if (manager.getString("Player").replaceAll("\\s", "").isEmpty())
                manager.set("Player", plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());

            if (manager.getString("UUID").replaceAll("\\s", "").isEmpty())
                manager.set("UUID", player.getUniqueId().toString());

            manager.save();
        } catch (Throwable ignored) {}
    }

    /**
     * Check if the player file is old
     *
     * @return if the player file is old
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
                Console.send(plugin, "Old player data folder have been removed (ALL OLD PLAYER DATA HAVE BEEN CONVERTED)", Level.INFO);
            } else {
                if (Arrays.toString(files).isEmpty()) {
                    Console.send(plugin, "Old player data folder have been removed (ALL OLD PLAYER DATA HAVE BEEN CONVERTED)", Level.INFO);
                } else {
                    for (File f : files) {
                        if (f.length() <= 4) {
                            if (f.delete()) {
                                Console.send(plugin, "Deleting file " + file.getName() + " due doesn't contains important player data", Level.INFO);
                            } else {
                                Console.send(plugin, "An error occurred while trying to remove file " + file.getName(), Level.INFO);
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
     * Get the player name
     *
     * @return the player name
     */
    public final String getName() {
        return manager.getString("Player");
    }

    /**
     * Save the player name
     *
     * @param name the new player name
     */
    public final void setName(final String name) {
        manager.set("Player", name);
    }

    /**
     * Get the player UUID
     *
     * @return the player UUID
     */
    public final UUID getUUID() {
        return UUID.fromString(manager.getString("UUID"));
    }

    /**
     * Get the player password
     *
     * @return the player password
     */
    public final String getPassword() {
        return manager.getString("Password");
    }

    /**
     * Set the player's password
     *
     * @param newPassword the new player password
     */
    public final void setPassword(String newPassword) {
        if (newPassword != null) {
            manager.set("Password", new PasswordUtils(newPassword).hashToken(config.passwordEncryption()));
        } else {
            manager.set("Password", "");
        }
    }

    /**
     * Get the player pin
     *
     * @return the player pin
     */
    public final String getPin() {
        return manager.getString("Pin");
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        if (pin != null) {
            manager.set("Pin", new PasswordUtils(String.valueOf(pin)).hashToken(config.pinEncryption()));
        } else {
            manager.set("Pin", "");
        }
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
     * @return the player google auth token
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
        manager.set("GAuth", token);
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2Fa in his account
     */
    public final boolean has2FA() {
        return manager.getBoolean("2FA");
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
