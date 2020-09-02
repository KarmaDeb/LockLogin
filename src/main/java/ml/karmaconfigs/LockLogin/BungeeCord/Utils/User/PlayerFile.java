package ml.karmaconfigs.LockLogin.BungeeCord.Utils.User;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public final class PlayerFile implements LockLoginBungee, BungeeFiles {

    private final ProxiedPlayer player;
    private final String uuid;

    private FileManager manager;
    private Utils sql;

    /**
     * Start the player file manager
     *
     * @param player the player
     */
    public PlayerFile(ProxiedPlayer player) {
        this.player = player;
        this.uuid = player.getUniqueId().toString().replace("-", "");

        if (config.isYaml()) {
            FileCreator creator = new FileCreator(uuid + ".yml", "playerdata", "userTemplate.yml");
            if (!creator.exists()) {
                creator.createFile();
                creator.setDefaults();
                creator.saveFile();
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

        } else {
            this.sql = new Utils(player.getUniqueId());
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
            Object password = old.get("Auth.Password");
            String gAuth = old.getString("2FA.gAuth");
            boolean GoogleAuth = old.getBoolean("2FA.enabled");
            boolean fly = old.getBoolean("Fly");

            manager.set("Player", name);
            manager.set("UUID", player.getUniqueId().toString());
            manager.set("Password", password.toString());
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
            if (manager.isSet("Player")) {
                if (manager.isEmpty("Player")) {
                    manager.set("Player", player.getName());
                }
            } else {
                manager.set("Player", player.getName());
            }
            if (manager.isSet("UUID")) {
                if (manager.isEmpty("UUID")) {
                    manager.set("UUID", player.getUniqueId().toString());
                }
            } else {
                manager.set("UUID", player.getUniqueId().toString());
            }
        } else {
            sql.createUser();
            sql.setName(player.getName());
        }
    }

    /**
     * Get the player UUID
     */
    public final UUID getUUID() {
        if (config.isYaml()) {
            return UUID.fromString(manager.getString("UUID"));
        } else {
            return sql.getUUID();
        }
    }

    /**
     * Get the player password
     *
     * @return a String
     */
    public final String getPassword() {
        if (config.isYaml()) {
            return manager.getString("Password");
        } else {
            return sql.getPassword();
        }
    }

    /**
     * Get the player pin
     *
     * @return a String
     */
    public final String getPin() {
        if (config.isYaml()) {
            return manager.getString("Pin");
        } else {
            return sql.getPin();
        }
    }

    /**
     * Set the player's password
     *
     * @param newPassword the new player password
     */
    public final void setPassword(String newPassword) {
        if (config.isYaml()) {
            manager.set("Password", new PasswordUtils(newPassword).Hash());
        } else {
            sql.setPassword(newPassword, false);
        }
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        if (config.isYaml()) {
            manager.set("Pin", new PasswordUtils(String.valueOf(pin)).Hash());
        } else {
            sql.setPin(pin, false);
        }
    }

    /**
     * Remove the player pin
     */
    public final void delPin() {
        if (config.isYaml()) {
            manager.set("Pin", "");
        } else {
            sql.delPin();
        }
    }

    /**
     * Get player Google Authenticator token
     *
     * @return a String
     */
    public final String getToken() {
        if (config.isYaml()) {
            return manager.getString("GAuth");
        } else {
            return sql.getToken();
        }
    }

    /**
     * Set the player Google Authenticator token
     *
     * @param token the token
     */
    public final void setToken(String token) {
        if (config.isYaml()) {
            manager.set("GAuth", new PasswordUtils(token).HashString());
        } else {
            sql.setGAuth(token, true);
        }
    }

    /**
     * Check if the player has 2fa
     *
     * @return a boolean
     */
    public final boolean has2FA() {
        if (config.isYaml()) {
            return manager.getBoolean("2FA");
        } else {
            return sql.has2fa();
        }
    }

    /**
     * Set player 2fa status
     *
     * @param status the status
     */
    public final void set2FA(boolean status) {
        if (config.isYaml()) {
            manager.set("2FA", status);
        } else {
            sql.gAuthStatus(status);
        }
    }

    /**
     * Remove the player file
     */
    public final void removeFile() {
        if (config.isYaml()) {
            manager.delete();
        } else {
            sql.removeUser();
        }
    }
}
