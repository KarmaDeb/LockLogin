package ml.karmaconfigs.lockloginsystem.shared.llsql;

import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

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
            switch (migration_platform) {
                case BUNGEE:
                    ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter bungeeConfig = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter();
                    if (bungeeConfig.registerRestricted())
                        return;
                    break;
                case BUKKIT:
                    ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter spigotConfig = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter();
                    if (spigotConfig.registerRestricted())
                        return;
                    break;
                case ANY:
                    try {
                        bungeeConfig = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter();
                        if (bungeeConfig.registerRestricted())
                            return;
                    } catch (Throwable ex) {
                        spigotConfig = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.ConfigGetter();
                        if (spigotConfig.registerRestricted())
                            return;
                    }
                    break;
            }

            sql_instance.createUser();
        }

        String id;
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
                        id = Objects.requireNonNull(sql_instance.getUUID()).toString().replace("-", "");
                        ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager bungeeManager = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager(id + ".yml", "playerdata");
                        player = bungeeManager.getString("Player");
                        password = bungeeManager.getString("Password");
                        pin = bungeeManager.getString("Pin");
                        token = bungeeManager.getString("GAuth");
                        has2FA = bungeeManager.getBoolean("2FA");
                        hasFly = bungeeManager.getBoolean("Fly");
                        break;
                    case BUKKIT:
                        id = Objects.requireNonNull(sql_instance.getUUID()).toString().replace("-", "");
                        ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager spigotManager = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager(id + ".yml", "playerdata");
                        player = spigotManager.getString("Player");
                        password = spigotManager.getString("Password");
                        pin = spigotManager.getString("Pin");
                        token = spigotManager.getString("GAuth");
                        has2FA = spigotManager.getBoolean("2FA");
                        hasFly = spigotManager.getBoolean("Fly");
                        break;
                }

                PasswordUtils utils = new PasswordUtils(password);

                sql_instance.setName(player);
                sql_instance.setPassword((Bucket.isAzuriom() ? utils.unHash() : password), true);
                sql_instance.setPin(pin, true);
                sql_instance.gAuthStatus(has2FA);
                sql_instance.setGAuth(token, false);
                sql_instance.setFly(hasFly);
                break;
            case YAML:
                id = sql_instance.getUUID();

                if (id != null) {
                    if (!id.contains("-")) {
                        UUID offline_uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + sql_instance.getName()).getBytes(StandardCharsets.UTF_8));
                        id = offline_uuid.toString();
                    }

                    String UUIDForFile = id.replace("-", "");

                    player = sql_instance.getName();
                    password = sql_instance.getPassword();
                    pin = sql_instance.getPin();
                    has2FA = sql_instance.has2fa();
                    token = sql_instance.getToken();
                    hasFly = sql_instance.hasFly();

                    utils = new PasswordUtils(password);

                    switch (migration_platform) {
                        case BUNGEE:
                            ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager bungeeManager = new ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager(UUIDForFile + ".yml", "playerdata");
                            bungeeManager.set("Player", player);
                            bungeeManager.set("UUID", Utils.fixUUID(id).toString());
                            bungeeManager.set("Password", utils.hash());
                            bungeeManager.set("Pin", pin);
                            bungeeManager.set("GAuth", token);
                            bungeeManager.set("2FA", has2FA);
                            bungeeManager.set("Fly", hasFly);
                            bungeeManager.save();
                            break;
                        case BUKKIT:
                            ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager spigotManager = new ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager(UUIDForFile + ".yml", "playerdata");
                            spigotManager.set("Player", player);
                            spigotManager.set("UUID", Utils.fixUUID(id).toString());
                            spigotManager.set("Password", utils.hash());
                            spigotManager.set("Pin", pin);
                            spigotManager.set("GAuth", token);
                            spigotManager.set("2FA", has2FA);
                            spigotManager.set("Fly", hasFly);
                            spigotManager.save();
                            break;
                    }
                    break;
                }
        }
    }
}
