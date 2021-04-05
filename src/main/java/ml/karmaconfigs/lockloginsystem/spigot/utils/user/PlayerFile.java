package ml.karmaconfigs.lockloginsystem.spigot.utils.user;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.KarmaFile;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
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
public final class PlayerFile implements LockLoginSpigot, SpigotFiles {

    private KarmaFile manager;

    /**
     * Start the player file manager
     *
     * @param player the player
     */
    public PlayerFile(Player player) {
        String uuid = player.getUniqueId().toString().replace("-", "");

        if (config.isYaml()) {
            manager = new KarmaFile(plugin, uuid + ".lldb", "data", "accounts");
            manager.exportFromFile("auto-generated/userTemplate.lldb");

            manager.applyKarmaAttribute();
        }

        try {
            OfflinePlayer offline = plugin.getServer().getOfflinePlayer(player.getUniqueId());

            if (offline.getName() != null) {
                if (manager.getString("PLAYER", "").replaceAll("\\s", "").isEmpty())
                    manager.set("PLAYER", offline.getName());
            }

            if (manager.getString("UUID", "").replaceAll("\\s", "").isEmpty())
                manager.set("UUID", player.getUniqueId().toString());
        } catch (Throwable ignored) {
        }
    }

    /**
     * Migrate from LockLogin v1 player database
     */
    public static void migrateV1() {
        try {
            File v1DataFolder = new File(plugin.getDataFolder() + File.separator + "Users");
            File[] files = v1DataFolder.listFiles();

            if (files != null) {
                Console.send(plugin, "Initializing LockLogin v1 player database migration", Level.INFO);

                for (File file : files) {
                    if (file.getName().endsWith(".yml")) {
                        FileManager oldManager = new FileManager(file.getName(), "Users");

                        File newFile = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "accounts", file.getName().replace(".yml", ".lldb"));
                        KarmaFile user = new KarmaFile(newFile);

                        String name = oldManager.getString("Player");
                        String password = oldManager.getString("Auth.Password");
                        String token = oldManager.getString("2FA.gAuth");
                        boolean fa = oldManager.getBoolean("2FA.enabled");

                        if (!user.exists()) {
                            user.create();

                            user.set("/// LockLogin user data file. -->");
                            user.set("/// Please do not modify this file -->");
                            user.set("/// until you know what you are doing! -->");

                            user.set("\n");

                            user.set("/// The first recorded player name -->");
                            user.set("PLAYER", (name != null ? name : ""));

                            user.set("\n");

                            //UUID record wasn't a feature in that time...
                            user.set("/// The user UUID, used for offline API -->");
                            user.set("UUID", "");

                            user.set("\n");

                            user.set("/// The user password -->");
                            user.set("PASSWORD", (password != null ? password : ""));

                            user.set("\n");

                            user.set("/// The user google auth token -->");
                            user.set("TOKEN", (token != null ? token : ""));

                            user.set("\n");

                            //Pin didn't exist at that time, so let's just set it empty
                            user.set("/// The user pin -->");
                            user.set("PIN", "");

                            user.set("\n");

                            user.set("/// The user Google Auth status -->");
                            user.set("2FA", fa);

                        /*
                        Fly does not longer exist in this plugin,
                        so it won't be written...
                         */
                        }
                    }

                    try {
                        Files.delete(file.toPath());
                    } catch (Throwable ignored) {}
                }

                try {
                    Files.delete(v1DataFolder.toPath());
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Migrate from LockLogin v2 player database
     */
    public static void migrateV2() {
        try {
            File v1DataFolder = new File(plugin.getDataFolder() + File.separator + "playerdata");
            File[] files = v1DataFolder.listFiles();

            if (files != null) {
                Console.send(plugin, "Initializing LockLogin v2 player database migration", Level.INFO);

                for (File file : files) {
                    if (file.getName().endsWith(".yml")) {
                        FileManager oldManager = new FileManager(file.getName(), "playerdata");

                        File newFile = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "accounts", file.getName().replace(".yml", ".lldb"));
                        KarmaFile user = new KarmaFile(newFile);

                        String name = oldManager.getString("Player");
                        String uuid = oldManager.getString("UUID");
                        String password = oldManager.getString("Password");
                        String token = oldManager.getString("GAuth");
                        String pin = oldManager.getString("Pin");
                        boolean fa = oldManager.getBoolean("2FA");

                        if (!user.exists()) {
                            user.create();

                            user.set("/// LockLogin user data file. -->");
                            user.set("/// Please do not modify this file -->");
                            user.set("/// until you know what you are doing! -->");

                            user.set("\n");

                            user.set("/// The first recorded player name -->");
                            user.set("PLAYER", (name != null ? name : ""));

                            user.set("\n");

                            //UUID record wasn't a feature in that time...
                            user.set("/// The user UUID, used for offline API -->");
                            user.set("UUID", (uuid != null ? uuid : ""));

                            user.set("\n");

                            user.set("/// The user password -->");
                            user.set("PASSWORD", (password != null ? password : ""));

                            user.set("\n");

                            user.set("/// The user google auth token -->");
                            user.set("TOKEN", (token != null ? token : ""));

                            user.set("\n");

                            //Pin didn't exist at that time, so let's just set it empty
                            user.set("/// The user pin -->");
                            user.set("PIN", (pin != null ? pin : ""));

                            user.set("\n");

                            user.set("/// The user Google Auth status -->");
                            user.set("2FA", fa);

                        /*
                        Fly does not longer exist in this plugin,
                        so it won't be written...
                         */
                        }
                    }

                    try {
                        Files.delete(file.toPath());
                    } catch (Throwable ignored) {}
                }

                try {
                    Files.delete(v1DataFolder.toPath());
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Save the player name
     *
     * @param name the new player name
     */
    public final void setName(final String name) {
        manager.set("PLAYER", name);
    }

    /**
     * Set the player's password
     *
     * @param newPassword the new player password
     */
    public final void setPassword(String newPassword) {
        if (newPassword != null) {
            manager.set("PASSWORD", new PasswordUtils(newPassword).hashToken(config.passwordEncryption()));
        } else {
            manager.set("PASSWORD", "");
        }
    }

    /**
     * Set the player Google Authenticator token
     *
     * @param token the token
     */
    public final void setToken(String token) {
        manager.set("TOKEN", token);
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    public final void setPin(Object pin) {
        if (pin != null) {
            manager.set("PIN", new PasswordUtils(String.valueOf(pin)).hashToken(config.pinEncryption()));
        } else {
            manager.set("PIN", "");
        }
    }

    public final void set2FA(final boolean status) {
        manager.set("2FA", status);
    }

    /**
     * Remove the player pin
     */
    public final void delPin() {
        manager.set("PIN", "");
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    public final String getName() {
        return manager.getString("PLAYER", "");
    }

    /**
     * Get the player UUID
     *
     * @return the player UUID
     */
    public final UUID getUUID() {
        return UUID.fromString(manager.getString("UUID", UUID.randomUUID().toString()));
    }

    /**
     * Get the player password
     *
     * @return the player password
     */
    public final String getPassword() {
        return manager.getString("PASSWORD", "");
    }

    /**
     * Get player Google Authenticator token
     *
     * @return the player google auth token
     */
    public final String getToken() {
        return manager.getString("TOKEN", "");
    }

    /**
     * Get the player pin
     *
     * @return the player pin
     */
    public final String getPin() {
        return manager.getString("PIN", "");
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2Fa in his account
     */
    public final boolean has2FA() {
        return manager.getBoolean("2FA", false);
    }

    /**
     * Remove the player file
     */
    public final void removeFile() {
        try {
            Files.delete(manager.getFile().toPath());
        } catch (Throwable ignored) {}
    }
}
