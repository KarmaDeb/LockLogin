package ml.karmaconfigs.lockloginsystem.shared.llsql;

import ml.karmaconfigs.lockloginsystem.shared.Platform;

import java.util.Objects;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class AccountMigrate {

    public AccountMigrate(Utils sqlAccount, Migrate migration, Platform platform) {
        if (!sqlAccount.userExists()) {
            sqlAccount.createUser();
        }

        ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager spigotManager;
        ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager bungeeManager;
        String UUID;
        String player = "";
        String password = "";
        String pin = "";
        boolean has2FA = false;
        String token = "";
        boolean hasFly = false;
        switch (migration) {
            case MySQL:
                switch (platform) {
                    case BUNGEE:
                        UUID = Objects.requireNonNull(sqlAccount.getUUID()).toString().replace("-", "");
                        bungeeManager = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager(UUID + ".yml", "playerdata");
                        player = bungeeManager.getString("Player");
                        password = bungeeManager.getString("Password");
                        pin = bungeeManager.getString("Pin");
                        token = bungeeManager.getString("GAuth");
                        has2FA = bungeeManager.getBoolean("2FA");
                        hasFly = bungeeManager.getBoolean("Fly");
                        break;
                    case SPIGOT:
                        UUID = Objects.requireNonNull(sqlAccount.getUUID()).toString().replace("-", "");
                        spigotManager = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager(UUID + ".yml", "playerdata");
                        player = spigotManager.getString("Player");
                        password = spigotManager.getString("Password");
                        pin = spigotManager.getString("Pin");
                        token = spigotManager.getString("GAuth");
                        has2FA = spigotManager.getBoolean("2FA");
                        hasFly = spigotManager.getBoolean("Fly");
                        break;
                }
                if (!sqlAccount.userExists()) {
                    sqlAccount.createUser();
                }

                sqlAccount.setName(player);
                sqlAccount.setPassword(password, true);
                sqlAccount.setPin(pin, true);
                sqlAccount.gAuthStatus(has2FA);
                sqlAccount.setGAuth(token, false);
                sqlAccount.setFly(hasFly);
                break;
            case YAML:
                String UUIDForFile = Objects.requireNonNull(sqlAccount.getUUID()).toString().replace("-", "");
                UUID = sqlAccount.getUUID().toString();
                player = sqlAccount.getName();
                password = sqlAccount.getPassword();
                pin = sqlAccount.getPin();
                has2FA = sqlAccount.has2fa();
                token = sqlAccount.getToken();
                hasFly = sqlAccount.hasFly();
                switch (platform) {
                    case BUNGEE:
                        bungeeManager = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager(UUIDForFile + ".yml", "playerdata");
                        bungeeManager.set("Player", player);
                        bungeeManager.set("UUID", UUID);
                        bungeeManager.set("Password", password);
                        bungeeManager.set("Pin", pin);
                        bungeeManager.set("GAuth", token);
                        bungeeManager.set("2FA", has2FA);
                        bungeeManager.set("Fly", hasFly);
                        break;
                    case SPIGOT:
                        spigotManager = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager(UUIDForFile + ".yml", "playerdata");
                        spigotManager.set("Player", player);
                        spigotManager.set("UUID", UUID);
                        spigotManager.set("Password", password);
                        spigotManager.set("Pin", pin);
                        spigotManager.set("GAuth", token);
                        spigotManager.set("2FA", has2FA);
                        spigotManager.set("Fly", hasFly);
                        break;
                }
                break;
        }
    }
}
