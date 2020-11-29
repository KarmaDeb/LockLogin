package ml.karmaconfigs.lockloginsystem.bungeecord.utils.files;

import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.bungee.karmayaml.FileCopy;
import ml.karmaconfigs.api.bungee.karmayaml.YamlReloader;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.shared.Lang;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.concurrent.TimeUnit;

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

public final class ConfigGetter {

    private final static Plugin plugin = new InterfaceUtils().getPlugin();
    private final static Logger logger = new Logger(plugin);
    private final static File config = new File(plugin.getDataFolder(), "config.yml");

    private static FileManager configuration = new FileManager("config.yml");

    private static boolean loaded = false;

    public ConfigGetter() {
        if (!config.exists()) {
            FileCopy creator = new FileCopy(plugin, "configs/config.yml");

            if (creator.copy(config)) {
                try {
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

    public interface manager {

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

    public final String ServerName() {
        return configuration.getString("ServerName");
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
            default:
                return Lang.UNKNOWN;
        }
    }

    public final boolean isYaml() {
        return FileSys().equalsIgnoreCase("File") || FileSys().equalsIgnoreCase("file");
    }

    public final boolean isMySQL() {
        return FileSys().equalsIgnoreCase("MySQL") || FileSys().equalsIgnoreCase("mysql");
    }

    public final boolean FileSysValid() {
        return isYaml() || isMySQL();
    }

    public final boolean RegisterBlind() {
        return configuration.getBoolean("Register.Blind");
    }

    public final boolean RegisterNausea() {
        return configuration.getBoolean("Register.Nausea");
    }

    public final int MaxRegister() {
        return configuration.getInt("Register.TimeOut");
    }

    public final int MaxRegisters() {
        return configuration.getInt("Register.Max");
    }

    public final boolean LoginBlind() {
        return configuration.getBoolean("Login.Blind");
    }

    public final boolean LoginNausea() {
        return configuration.getBoolean("Login.Nausea");
    }

    public final int MaxLogin() {
        return configuration.getInt("Login.TimeOut");
    }

    public final int GetMaxTries() {
        return configuration.getInt("Login.MaxTries");
    }

    public final int BFMaxTries() {
        return configuration.getInt("BruteForce.Tries");
    }

    public final int BFBlockTime() {
        int val = configuration.getInt("BruteForce.BlockTime");

        if (val <= 0)
            val = 30;

        return (int) TimeUnit.MINUTES.toSeconds(val);
    }

    public final boolean EnablePins() {
        return configuration.getBoolean("Pin");
    }

    public final boolean CheckForUpdates() {
        return configuration.getBoolean("Updater.Check");
    }

    public final int UpdateCheck() {
        if (configuration.getInt("Updater.CheckTime") >= 5 && configuration.getInt("Updater.CheckTime") <= 61) {
            return (int) TimeUnit.MINUTES.toSeconds(configuration.getInt("Updater.CheckTime"));
        } else {
            configuration.set("Updater.CheckTime", 5);
            return (int) TimeUnit.MINUTES.toSeconds(5);
        }
    }

    public final boolean UpdateSelf() {
        return configuration.getBoolean("Updater.AutoUpdate");
    }

    public final boolean isFatJar() {
        String value = configuration.getString("Updater.FileType");
        if (value == null || value.isEmpty())
            value = "Flat";
        value = value.toLowerCase();

        if (!value.equals("flat") && !value.equals("fat")) {
            value = "flat";
            configuration.set("Updater.FileType", "Flat");
        }

        return value.equals("fat");
    }

    public final boolean Enable2FA() {
        return configuration.getBoolean("2FA");
    }

    public final boolean ClearChat() {
        return configuration.getBoolean("ClearChat");
    }

    public final int AccountsPerIp() {
        if (configuration.getInt("AccountsPerIp") != 0) {
            return configuration.getInt("AccountsPerIp");
        } else {
            return 100000;
        }
    }

    public final boolean CheckNames() {
        return configuration.getBoolean("CheckNames");
    }

    public final boolean EnableAuth() {
        return configuration.getString("Servers.AuthLobby").isEmpty();
    }

    public final boolean EnableMain() {
        return configuration.getString("Servers.MainLobby").isEmpty();
    }

    public final String AuthLobby() {
        return configuration.getString("Servers.AuthLobby");
    }

    public final String MainLobby() {
        return configuration.getString("Servers.MainLobby");
    }

    public final String FallBackAuth() {
        return configuration.getString("FallBack.AuthLobby");
    }

    public final String FallBackMain() {
        return configuration.getString("FallBack.MainLobby");
    }

    public final String MSGLang() {
        return configuration.getString("Lang");
    }

    public final String FileSys() {
        return configuration.getString("AccountSys");
    }
}
