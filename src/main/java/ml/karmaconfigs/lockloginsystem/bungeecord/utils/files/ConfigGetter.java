package ml.karmaconfigs.lockloginsystem.bungeecord.utils.files;

import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.bungee.karmayaml.FileCopy;
import ml.karmaconfigs.api.bungee.karmayaml.YamlReloader;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.Lang;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
import ml.karmaconfigs.lockloginsystem.shared.version.VersionChannel;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
public final class ConfigGetter implements LockLoginBungee {

    private final static Plugin plugin = new InterfaceUtils().getPlugin();
    private final static Logger logger = new Logger(plugin);
    private final static File config = new File(plugin.getDataFolder(), "config.yml");

    private static FileManager configuration = new FileManager("config.yml");

    private static boolean loaded = false;

    public ConfigGetter() {
        if (!config.exists()) {
            FileCopy creator = new FileCopy(plugin, "configs/config.yml").withDebug(FileInfo.apiDebug(new File(jar)));

            try {
                creator.copy(config);
                YamlReloader reloader = new YamlReloader(plugin, config, "configs/config.yml");
                if (reloader.reloadAndCopy()) {
                    logger.scheduleLog(Level.INFO, "Created and reloaded config file");
                    configuration = new FileManager("config.yml");
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reloading config file");
            }
        }

        if (configuration == null) {
            try {
                configuration = new FileManager("config.yml");
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while loading config file");
            }
        }

        if (!loaded) {
            manager.reload();
            loaded = true;
        }
    }

    public final String serverName() {
        String name = configuration.getString("ServerName");
        if (name == null)
            name = "";

        if (name.isEmpty()) {
            name = StringUtils.randomString(8, StringUtils.StringGen.ONLY_LETTERS, StringUtils.StringType.ALL_LOWER);
            configuration.set("ServerName", name);
            configuration.save();

            ConfigGetter.manager.reload();
        }

        return name;
    }

    public final Lang getLang() {
        String val = configuration.getString("Lang").toLowerCase();

        switch (val) {
            case "en_en":
            case "english":
                return Lang.ENGLISH;
            case "es_es":
            case "spanish":
                return Lang.SPANISH;
            case "zh_cn":
            case "simplified_chinese":
                return Lang.SIMPLIFIED_CHINESE;
            case "it_it":
            case "italian":
                return Lang.ITALIAN;
            case "pl_pl":
            case "polish":
                return Lang.POLISH;
            case "fr_fr":
            case "french":
                return Lang.FRENCH;
            case "cz_cs":
            case "czech":
                return Lang.CZECH;
            case "ru_ru":
            case "russian":
                return Lang.RUSSIAN;
            default:
                return Lang.UNKNOWN;
        }
    }

    public final boolean isYaml() {
        return accountSystem().equalsIgnoreCase("File") || accountSystem().equalsIgnoreCase("file");
    }

    public final boolean isMySQL() {
        return accountSystem().equalsIgnoreCase("MySQL") || accountSystem().equalsIgnoreCase("mysql");
    }

    public final boolean accountSysValid() {
        return isYaml() || isMySQL();
    }

    public final boolean registerRestricted() {
        return configuration.getBoolean("Azuriom.Restrict");
    }

    public final boolean semiPremium() {
        return configuration.getBoolean("Azuriom.SemiPremium");
    }

    public final boolean blindRegister() {
        return configuration.getBoolean("Register.Blind");
    }

    public final boolean nauseaRegister() {
        return configuration.getBoolean("Register.Nausea");
    }

    public final int registerTimeOut() {
        return configuration.getInt("Register.TimeOut");
    }

    public final int maxRegister() {
        return configuration.getInt("Register.Max");
    }

    public final CryptType passwordEncryption() {
        String value = configuration.getString("Encryption.Passwords");

        switch (value.toLowerCase()) {
            case "256":
            case "sha256":
                return CryptType.SHA256;
            case "bcrypt":
                return CryptType.BCrypt;
            case "argon2i":
                return CryptType.ARGON2I;
            case "argon2id":
                return CryptType.ARGON2ID;
            case "512":
            case "sha512":
            default:
                return CryptType.SHA512;
        }
    }

    public final CryptType pinEncryption() {
        String value = configuration.getString("Encryption.Pins");

        switch (value.toLowerCase()) {
            case "256":
            case "sha256":
                return CryptType.SHA256;
            case "bcrypt":
                return CryptType.BCrypt;
            case "argon2i":
                return CryptType.ARGON2I;
            case "argon2id":
                return CryptType.ARGON2ID;
            case "512":
            case "sha512":
            default:
                return CryptType.SHA512;
        }
    }

    public final boolean blindLogin() {
        return configuration.getBoolean("Login.Blind");
    }

    public final boolean nauseaLogin() {
        return configuration.getBoolean("Login.Nausea");
    }

    public final int loginTimeOut() {
        return configuration.getInt("Login.TimeOut");
    }

    public final int loginMaxTries() {
        return configuration.getInt("Login.MaxTries");
    }

    public final int registerInterval() {
        int value = configuration.getInt("MessagesInterval.Registration");

        if (value < 5 || value > registerTimeOut()) {
            value = 5;
            configuration.set("MessagesInterval.Register", value);
            configuration.save();

            ConfigGetter.manager.reload();
        }

        return value;
    }

    public final int loginInterval() {
        int value = configuration.getInt("MessagesInterval.Logging");

        if (value < 5 || value > loginTimeOut()) {
            value = 5;
            configuration.set("MessagesInterval.Login", value);
            configuration.save();

            ConfigGetter.manager.reload();
        }

        return value;
    }

    public final CaptchaType getCaptchaType() {
        String val = configuration.getString("Captcha.Mode");
        assert val != null;

        switch (val.toLowerCase()) {
            case "simple":
                return CaptchaType.SIMPLE;
            case "disabled":
                return CaptchaType.DISABLED;
            case "complex":
            default:
                return CaptchaType.COMPLEX;
        }
    }

    public final int getCaptchaTimeOut() {
        return configuration.getInt("Captcha.TimeOut");
    }

    public final int getCaptchaLength() {
        int val = configuration.getInt("Captcha.Difficulty.Length");

        if (val >= 4 && val <= 8)
            return val;
        else
            return 6;
    }

    public final boolean letters() {
        return configuration.getBoolean("Captcha.Difficulty.Letters");
    }

    public final boolean strikethrough() {
        return configuration.getBoolean("Captcha.Strikethrough.Enabled");
    }

    public final boolean randomStrikethrough() {
        return configuration.getBoolean("Captcha.Strikethrough.Random");
    }

    public final int bfMaxTries() {
        return configuration.getInt("BruteForce.Tries");
    }

    public final int bfBlockTime() {
        int val = configuration.getInt("BruteForce.BlockTime");

        if (val <= 0)
            val = 30;

        return (int) TimeUnit.MINUTES.toSeconds(val);
    }

    public final boolean alreadyPlaying() {
        return configuration.getBoolean("AlreadyPlaying");
    }

    public final boolean pinEnabled() {
        return configuration.getBoolean("Pin");
    }

    public final VersionChannel getUpdateChannel() {
        String value = configuration.getString("Updater.Channel");

        switch (value.toLowerCase()) {
            case "rc":
            case "releasecandidate":
            case "release_candidate":
                return VersionChannel.RC;
            case "snapshot":
                return VersionChannel.SNAPSHOT;
            case "release":
            default:
                return VersionChannel.RELEASE;
        }
    }

    public final boolean checkUpdates() {
        return configuration.getBoolean("Updater.Check");
    }

    public final int checkInterval() {
        if (configuration.getInt("Updater.CheckTime") >= 5 && configuration.getInt("Updater.CheckTime") <= 86400) {
            return (int) TimeUnit.MINUTES.toSeconds(configuration.getInt("Updater.CheckTime"));
        } else {
            configuration.set("Updater.CheckTime", 5);
            return (int) TimeUnit.MINUTES.toSeconds(5);
        }
    }

    public final boolean enable2FA() {
        return configuration.getBoolean("2FA");
    }

    public final boolean clearChat() {
        return configuration.getBoolean("ClearChat");
    }

    public final int accountsPerIP() {
        if (configuration.getInt("AccountsPerIp") != 0) {
            return configuration.getInt("AccountsPerIp");
        } else {
            return 100000;
        }
    }

    public final boolean checkNames() {
        return configuration.getBoolean("CheckName");
    }

    public final boolean enableAuthLobby() {
        return !configuration.getString("Servers.AuthLobby").isEmpty();
    }

    public final boolean enableMainLobby() {
        return !configuration.getString("Servers.MainLobby").isEmpty();
    }

    public final String getAuthLobby() {
        return configuration.getString("Servers.AuthLobby");
    }

    public final String getMainLobby() {
        return configuration.getString("Servers.MainLobby");
    }

    public final String getFallBackAuth() {
        return configuration.getString("FallBack.AuthLobby");
    }

    public final String getFallBackMain() {
        return configuration.getString("FallBack.MainLobby");
    }

    public final String accountSystem() {
        return configuration.getString("AccountSys");
    }

    /**
     * Configuration manager
     * utilities
     */
    public interface manager {

        /**
         * Reload configuration file
         *
         * @return if the file could be reloaded
         */
        static boolean reload() {
            try {
                YamlReloader reloader = new YamlReloader(plugin, config, "configs/config.yml");
                if (reloader.reloadAndCopy()) {
                    logger.scheduleLog(Level.INFO, "Reloaded config file");
                    return true;
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reloading config file");
            }

            return false;
        }
    }
}
