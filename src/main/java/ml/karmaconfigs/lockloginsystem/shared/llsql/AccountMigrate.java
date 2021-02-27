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

    private final Utils sql_instance;
    private final Migrate migration_type;
    private final Platform migration_platform;

    public AccountMigrate(Utils sqlAccount, Migrate migration, Platform platform) {
        sql_instance = sqlAccount;
        migration_type = migration;
        migration_platform = platform;
    }

    public final void start() {
        if (!sql_instance.userExists()) {
            ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter bungeeConfig = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter();
            ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter spigotConfig = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter();

            switch (migration_platform) {
                case BUNGEE:
                    if (bungeeConfig.registerRestricted())
                        return;
                    break;
                case SPIGOT:
                    if (spigotConfig.registerRestricted())
                        return;
                    break;
                case ANY:
                    try {
                        if (bungeeConfig.registerRestricted())
                            return;
                    } catch (Throwable ex) {
                        if (spigotConfig.registerRestricted())
                            return;
                    }
                    break;
            }

            sql_instance.createUser();
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
        switch (migration_type) {
            case MySQL:
                switch (migration_platform) {
                    case BUNGEE:
                        UUID = Objects.requireNonNull(sql_instance.getUUID()).toString().replace("-", "");
                        bungeeManager = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager(UUID + ".yml", "playerdata");
                        player = bungeeManager.getString("Player");
                        password = bungeeManager.getString("Password");
                        pin = bungeeManager.getString("Pin");
                        token = bungeeManager.getString("GAuth");
                        has2FA = bungeeManager.getBoolean("2FA");
                        hasFly = bungeeManager.getBoolean("Fly");
                        break;
                    case SPIGOT:
                        UUID = Objects.requireNonNull(sql_instance.getUUID()).toString().replace("-", "");
                        spigotManager = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager(UUID + ".yml", "playerdata");
                        player = spigotManager.getString("Player");
                        password = spigotManager.getString("Password");
                        pin = spigotManager.getString("Pin");
                        token = spigotManager.getString("GAuth");
                        has2FA = spigotManager.getBoolean("2FA");
                        hasFly = spigotManager.getBoolean("Fly");
                        break;
                }

                sql_instance.setName(player);
                sql_instance.setPassword(password, true);
                sql_instance.setPin(pin, true);
                sql_instance.gAuthStatus(has2FA);
                sql_instance.setGAuth(token, false);
                sql_instance.setFly(hasFly);
                break;
            case YAML:
                String UUIDForFile = Objects.requireNonNull(sql_instance.getUUID()).toString().replace("-", "");
                UUID = sql_instance.getUUID().toString();
                player = sql_instance.getName();
                password = sql_instance.getPassword();
                pin = sql_instance.getPin();
                has2FA = sql_instance.has2fa();
                token = sql_instance.getToken();
                hasFly = sql_instance.hasFly();
                switch (migration_platform) {
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
