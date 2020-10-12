package ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.Lang;

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

public final class ConfigGetter implements LockLoginBungee {

    private final FileManager manager = new FileManager("config.yml");

    public ConfigGetter() {
        FileCreator creator = new FileCreator("config.yml", true);

        creator.createFile();
        creator.setDefaults();
        creator.saveFile();
    }

    public final String ServerName() {
        return manager.getString("ServerName");
    }

    public final boolean isEnglish() {
        return getLang().equals(Lang.ENGLISH);
    }

    public final boolean isSpanish() {
        return getLang().equals(Lang.SPANISH);
    }

    public final boolean isSimplifiedChinese() {
        return getLang().equals(Lang.SIMPLIFIED_CHINESE);
    }

    public final boolean isItalian() {
        return getLang().equals(Lang.ITALIAN);
    }

    public final boolean isPolish() {
        return getLang().equals(Lang.POLISH);
    }

    public final boolean isFrench() {
        return getLang().equals(Lang.FRENCH);
    }

    public final boolean isCzech() {
        return getLang().equals(Lang.CZECH);
    }

    public final boolean langValid() {
        return isEnglish() || isSpanish() || isSimplifiedChinese() || isItalian() || isPolish() || isFrench() || isCzech();
    }

    public final Lang getLang() {
        String val = manager.getString("Lang");

        switch (val) {
            case "en_EN":
            case "english":
            case "English":
                return Lang.ENGLISH;
            case "es_ES":
            case "spanish":
            case "Spanish":
                return Lang.SPANISH;
            case "zh_CN":
            case "simplified_chinese":
            case "Simplified_chinese":
            case "Simplified_Chinese":
            case "simplified chinese":
            case "Simplified chinese":
            case "Simplified Chinese":
                return Lang.SIMPLIFIED_CHINESE;
            case "it_IT":
            case "italian":
            case "Italian":
                return Lang.ITALIAN;
            case "pl_PL":
            case "polish":
            case "Polish":
                return Lang.POLISH;
            case "fr_FR":
            case "french":
            case "French":
                return Lang.FRENCH;
            case "cz_CS":
            case "czech":
            case "Czech":
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
        return manager.getBoolean("Register.Blind");
    }

    public final boolean RegisterNausea() {
        return manager.getBoolean("Register.Nausea");
    }

    public final int MaxRegister() {
        return manager.getInt("Register.TimeOut");
    }

    public final int MaxRegisters() {
        return manager.getInt("Register.Max");
    }

    public final boolean LoginBlind() {
        return manager.getBoolean("Login.Blind");
    }

    public final boolean LoginNausea() {
        return manager.getBoolean("Login.Nausea");
    }

    public final int MaxLogin() {
        return manager.getInt("Login.TimeOut");
    }

    public final int GetMaxTries() {
        return manager.getInt("Login.MaxTries");
    }

    public final boolean EnablePins() {
        return manager.getBoolean("Pin");
    }

    public final boolean CheckForUpdates() {
        return manager.getBoolean("Updater.Check");
    }

    public final int UpdateCheck() {
        if (manager.getInt("Updater.CheckTime") >= 5 && manager.getInt("Updater.CheckTime") <= 61) {
            return (int) TimeUnit.MINUTES.toSeconds(manager.getInt("Updater.CheckTime"));
        } else {
            manager.set("Updater.CheckTime", 5);
            return (int) TimeUnit.MINUTES.toSeconds(5);
        }
    }

    public final boolean UpdateSelf() {
        return manager.getBoolean("Updater.AutoUpdate");
    }

    public final boolean ChangeLogs() {
        return manager.getBoolean("Updater.ChangeLog");
    }

    public final boolean Enable2FA() {
        return manager.getBoolean("2FA");
    }

    public final boolean ClearChat() {
        return manager.getBoolean("ClearChat");
    }

    public final int AccountsPerIp() {
        if (!manager.getInt("AccountsPerIp").equals(0)) {
            return manager.getInt("AccountsPerIp");
        } else {
            return 100000;
        }
    }

    public final boolean CheckNames() {
        return manager.getBoolean("CheckNames");
    }

    public final boolean EnableAuth() {
        return manager.getString("Servers.AuthLobby").isEmpty();
    }

    public final boolean EnableMain() {
        return manager.getString("Servers.MainLobby").isEmpty();
    }

    public final String AuthLobby() {
        return manager.getString("Servers.AuthLobby");
    }

    public final String MainLobby() {
        return manager.getString("Servers.MainLobby");
    }

    public final String FallBackAuth() {
        return manager.getString("FallBack.AuthLobby");
    }

    public final String FallBackMain() {
        return manager.getString("FallBack.MainLobby");
    }

    public final String MSGLang() {
        return manager.getString("Lang");
    }

    public final String FileSys() {
        return manager.getString("AccountSys");
    }
}
