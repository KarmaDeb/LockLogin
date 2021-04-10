package ml.karmaconfigs.lockloginsystem.bukkit.utils.user;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.KarmaFile;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountID;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountManager;
import ml.karmaconfigs.lockloginsystem.shared.account.AzuriomId;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
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
public final class PlayerFile extends AccountManager implements LockLoginSpigot, SpigotFiles {

    private final KarmaFile manager;

    private final OfflinePlayer player;

    public PlayerFile() {
        manager = null;
        player = null;
    }

    public PlayerFile(final File managed) {
        player = null;

        manager = new KarmaFile(managed);
    }

    public PlayerFile(final OfflinePlayer managed) {
        player = managed;

        AzuriomId id = new AzuriomId(AccountID.fromUUID(managed.getUniqueId()));
        File file = id.getAccountFile();
        if (file.exists())
            manager = new KarmaFile(file);
        else
            manager = new KarmaFile(plugin, player.getUniqueId().toString().replace("-", "") + ".lldb", "data", "accounts");
    }

    /**
     * Migrate from LockLogin v1 player database
     */
    public static void migrateV1() {
        File v1DataFolder = new File(plugin.getDataFolder() + File.separator + "Users");
        File[] files = v1DataFolder.listFiles();

        if (files != null) {
            Console.send(plugin, "Initializing LockLogin v1 player database migration", Level.INFO);

            for (File file : files) {
                if (file.getName().endsWith(".yml")) {
                    Console.send(plugin, "Migrating account #" + file.getName().replace(".yml", ""), Level.INFO);
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
                    } else {
                        user.set("PLAYER", (name != null ? name : ""));
                        user.set("UUID", "");
                        user.set("PASSWORD", (password != null ? password : ""));
                        user.set("TOKEN", (token != null ? token : ""));
                        user.set("PIN", "");
                    }
                    user.set("2FA", fa);
                }

                try {
                    Files.delete(file.toPath());
                } catch (Throwable ignored) {
                }
            }

            try {
                Files.delete(v1DataFolder.toPath());
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Migrate from LockLogin v2 player database
     */
    public static void migrateV2() {
        File v1DataFolder = new File(plugin.getDataFolder() + File.separator + "playerdata");
        File[] files = v1DataFolder.listFiles();

        if (files != null) {
            Console.send(plugin, "Initializing LockLogin v2 player database migration", Level.INFO);

            for (File file : files) {
                if (file.getName().endsWith(".yml")) {
                    Console.send(plugin, "Migrating account #" + file.getName().replace(".yml", ""), Level.INFO);
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
                    } else {
                        user.set("PLAYER", (name != null ? name : ""));
                        user.set("UUID", (uuid != null ? uuid : ""));
                        user.set("PASSWORD", (password != null ? password : ""));
                        user.set("TOKEN", (token != null ? token : ""));
                        user.set("PIN", (pin != null ? pin : ""));
                    }
                    user.set("2FA", fa);
                }

                try {
                    Files.delete(file.toPath());
                } catch (Throwable ignored) {
                }
            }

            try {
                Files.delete(v1DataFolder.toPath());
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public boolean exists() {
        return manager.exists();
    }

    @Override
    public boolean create() {
        if (manager.exists())
            try {
                if (player.getName() != null) {
                    if (manager.getString("PLAYER", "").replaceAll("\\s", "").isEmpty())
                        manager.set("PLAYER", player.getName());
                }

                if (manager.getString("UUID", "").replaceAll("\\s", "").isEmpty())
                    manager.set("UUID", player.getUniqueId().toString());
            } catch (Throwable ignored) {
            }

        if (!manager.exists()) {
            manager.exportFromFile("auto-generated/userTemplate.lldb");

            manager.applyKarmaAttribute();
            return true;
        }

        return false;
    }

    @Override
    public boolean remove() {
        try {
            return Files.deleteIfExists(manager.getFile().toPath());
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public void saveUUID(AccountID id) {
        manager.set("UUID", id.getId());
    }

    /**
     * Save the player name
     *
     * @param name the new player name
     */
    @Override
    public final void setName(final String name) {
        manager.set("PLAYER", name);
    }

    /**
     * Set the player's password
     *
     * @param newPassword the new player password
     */
    @Override
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
    @Override
    public final void setGAuth(String token) {
        manager.set("TOKEN", new PasswordUtils(token).hash());
    }

    /**
     * Set the player pin
     *
     * @param pin the pin
     */
    @Override
    public final void setPin(String pin) {
        if (pin != null) {
            manager.set("PIN", new PasswordUtils(pin).hashToken(config.pinEncryption()));
        } else {
            manager.set("PIN", "");
        }
    }

    /**
     * Set the account 2FA status
     *
     * @param status the account 2FA status
     */
    @Override
    public final void set2FA(final boolean status) {
        manager.set("2FA", status);
    }

    /**
     * Get the player name
     *
     * @return the player name
     */
    @Override
    public final String getName() {
        return manager.getString("PLAYER", "").replace("PLAYER:", "");
    }

    /**
     * Get the player UUID
     *
     * @return the player UUID
     */
    @Override
    public final AccountID getUUID() {
        return AccountID.fromUUID(UUID.fromString(manager.getString("UUID", UUID.randomUUID().toString()).replace("UUID:", "")));
    }

    /**
     * Get the player password
     *
     * @return the player password
     */
    @Override
    public final String getPassword() {
        return manager.getString("PASSWORD", "").replace("PASSWORD:", "");
    }

    /**
     * Get player Google Authenticator token
     *
     * @return the player google auth token
     */
    @Override
    public final String getGAuth() {
        return manager.getString("TOKEN", "").replace("TOKEN:", "");
    }

    /**
     * Get the player pin
     *
     * @return the player pin
     */
    @Override
    public final String getPin() {
        return manager.getString("PIN", "").replace("PIN:", "");
    }

    /**
     * Check if the player has 2fa
     *
     * @return if the player has 2Fa in his account
     */
    @Override
    public final boolean has2FA() {
        return manager.getBoolean("2FA", false);
    }

    @Override
    public Set<AccountManager> getAccounts() {
        Set<AccountManager> managers = new LinkedHashSet<>();

        File[] files = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "accounts").listFiles();
        if (files != null) {
            for (File file : files) {
                KarmaFile manager = new KarmaFile(file);
                String uuid = manager.getString("UUID", "");
                if (!uuid.replaceAll("\\s", "").isEmpty()) {
                    try {
                        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
                        AccountManager acManager = new PlayerFile(offline);

                        managers.add(acManager);
                    } catch (Throwable ignored) {}
                }
            }
        }

        return managers;
    }
}
