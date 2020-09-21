package ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;

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

    public final boolean isSpanish() {
        return MSGLang().equalsIgnoreCase("es_ES");
    }

    public final boolean isEnglish() {
        return MSGLang().equalsIgnoreCase("en_EN");
    }

    public final boolean isSimplifiedChinese() {
        return MSGLang().equalsIgnoreCase("zh_CN");
    }

    public final boolean isItalian() {
        return MSGLang().equalsIgnoreCase("it_IT");
    }

    public final boolean isPolish() {
        return MSGLang().equalsIgnoreCase("pl_PL");
    }

    public final boolean isFrench() {
        return MSGLang().equalsIgnoreCase("fr_FR");
    }

    public final boolean langValid() {
        return isEnglish() || isSpanish() || isSimplifiedChinese() || isItalian() || isPolish() || isFrench();
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
