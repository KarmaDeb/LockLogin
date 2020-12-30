package ml.karmaconfigs.lockloginsystem.bungeecord.utils.user;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.util.Arrays;
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
            /*File playerdata = new File(plugin.getDataFolder(), "playerdata");
            File player_file = new File(playerdata, uuid + ".yml");

            FileCopy copy = new FileCopy(plugin, "auto-generated/userTemplate.yml");
            copy.copy(player_file);*/

            manager = new FileManager(uuid + ".yml", "playerdata");
            manager.setInternal("auto-generated/userTemplate.yml");

            if (manager.get("Security.Password") != null) {
                manager.set("Password", manager.getString("Security.Password"));
                manager.unset("Security.Password");
            }
            if (manager.get("Security.GAuth") != null) {
                manager.set("GAuth", manager.getString("Security.GAuth"));
                manager.unset("Security.GAuth");
            }
            if (manager.get("Security.2FA") != null) {
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
                Console.send(plugin, "Old player data folder have been removed (ALL OLD PLAYER DATA HAVE BEEN CONVERTED)", Level.INFO);
            } else {
                if (Arrays.toString(files).isEmpty()) {
                    Console.send("Old player data folder have been removed (ALL OLD PLAYER DATA HAVE BEEN CONVERTED)", Level.INFO);
                } else {
                    for (File f : files) {
                        if (f.length() <= 4) {
                            if (f.delete()) {
                                Console.send(plugin, "Deleting file " + file.getName() + " due doesn't contains important player data", Level.INFO);
                            } else {
                                Console.send(plugin, "An error occurred while trying to remove file " + file.getName(), Level.GRAVE);
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
            if (manager.get("Player") != null) {
                if (manager.get("Player").toString().isEmpty()) {
                    manager.set("Player", player.getName());
                }
            } else {
                manager.set("Player", player.getName());
            }
            if (manager.get("UUID") != null) {
                if (manager.get("UUID").toString().isEmpty()) {
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
     *
     * @return the player UUID
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
     * @return the player password
     */
    public final String getPassword() {
        if (config.isYaml()) {
            return manager.getString("Password");
        } else {
            return sql.getPassword();
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
     * Get the player pin
     *
     * @return the player pin
     */
    public final String getPin() {
        if (config.isYaml()) {
            return manager.getString("Pin");
        } else {
            return sql.getPin();
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
     * @return the player google auth token
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
     * @return if the player has 2FA
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
     * @param status true = 2FA enabled ; false = 2FA disabled
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
            File player_data = new File(plugin.getDataFolder(), "playerdata");
            File player_file = new File(player_data, uuid + ".yml");
            player_file.delete();
        } else {
            sql.removeUser();
        }
    }
}
