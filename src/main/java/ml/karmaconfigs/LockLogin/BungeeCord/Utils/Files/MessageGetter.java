package ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

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

public final class MessageGetter implements LockLoginBungee {

    private FileManager manager;

    public MessageGetter() {
        FileCreator creator = new FileCreator("messages_en.yml", true);
        ConfigGetter cfg = new ConfigGetter();
        if (cfg.langValid()) {
            if (cfg.isEnglish()) {
                creator.createFile();
                creator.setDefaults();
                creator.saveFile();
                manager = new FileManager("messages_en.yml");
            } else {
                if (cfg.isSpanish()) {
                    creator = new FileCreator("messages_es.yml", true);
                    creator.createFile();
                    creator.setDefaults();
                    creator.saveFile();
                    manager = new FileManager("messages_es.yml");
                } else {
                    if (cfg.isSimplifiedChinese()) {
                        creator = new FileCreator("messages_zh.yml", true);
                        creator.createFile();
                        creator.setDefaults();
                        creator.saveFile();
                        manager = new FileManager("messages_zh.yml");
                    } else {
                        if (cfg.isItalian()) {
                            creator = new FileCreator("messages_it.yml", true);
                            creator.createFile();
                            creator.setDefaults();
                            creator.saveFile();
                            manager = new FileManager("messages_it.yml");
                        } else {
                            if (cfg.isPolish()) {
                                creator = new FileCreator("messages_pl.yml", true);
                                creator.createFile();
                                creator.setDefaults();
                                creator.saveFile();
                                manager = new FileManager("messages_pl.yml");
                            } else {
                                if (cfg.isFrench()) {
                                    creator = new FileCreator("messages_fr.yml", true);
                                    creator.createFile();
                                    creator.setDefaults();
                                    creator.saveFile();
                                    manager = new FileManager("messages_fr.yml");
                                }
                            }
                        }
                    }
                }
            }
        } else {
            out.Alert("Invalid lang &f( " + cfg.MSGLang() + " &f) &c.Valid langs are: &ben_EN&7, &ees_ES&7, &ezh_CN&7, &eit_IT&7, &epl_PL&7, &efr_FR", WarningLevel.ERROR);
            FileManager configManager = new FileManager("config.yml");
            configManager.set("Lang", "en_EN");
            creator = new FileCreator("messages_en.yml", true);
            creator.createFile();
            creator.setDefaults();
            creator.saveFile();
        }
    }

    public final String Prefix() {
        return manager.getString("Prefix");
    }

    public final String AlreadyPlaying() {
        return manager.getString("AlreadyPlaying");
    }

    public final String Login() {
        return manager.getString("Login");
    }

    public final String Logged(ProxiedPlayer player) {
        return manager.getString("Logged").replace("{player}", player.getName());
    }

    public final String AlreadyLogged() {
        return manager.getString("AlreadyLogged");
    }

    public final String LogError() {
        return manager.getString("LogError");
    }

    public final String Register() {
        return manager.getString("Register");
    }

    public final String Registered() {
        return manager.getString("Registered");
    }

    public final String AlreadyRegistered() {
        return manager.getString("AlreadyRegistered");
    }

    public final String RegisterError() {
        return manager.getString("RegisterError");
    }

    public final String PasswordInsecure() {
        return manager.getString("PasswordInsecure");
    }

    public final String PasswordMinChar() {
        return manager.getString("PasswordMinChar");
    }

    public final String ChangePass() {
        return manager.getString("ChangePass");
    }

    public final String ChangeError() {
        return manager.getString("ChangeError");
    }

    public final String ChangeSame() {
        return manager.getString("ChangeSame");
    }

    public final String ChangeDone() {
        return manager.getString("ChangeDone");
    }

    public final String Reset2Fa() {
        return manager.getString("Reset2Fa");
    }

    public final String ReseatedFA() {
        return manager.getString("ReseatedFA");
    }

    public final String Enable2FA() {
        return manager.getString("Enable2FA");
    }

    public final String ToggleFAError() {
        return manager.getString("ToggleFAError");
    }

    public final String Disabled2FA() {
        return manager.getString("Disabled2FA");
    }

    public final String GAuthDisabled() {
        return manager.getString("2FADisabled");
    }

    public final String LoginOut() {
        return manager.getString("LoginOut");
    }

    public final String RegisterOut() {
        return manager.getString("RegisterOut");
    }

    public final String MaxIp() {
        return manager.getString("MaxIp");
    }

    public final String LoginTitle(int TimeLeft) {
        return manager.getString("LoginTitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String LoginSubtitle(int TimeLeft) {
        return manager.getString("LoginSubtitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String RegisterTitle(int TimeLeft) {
        return manager.getString("RegisterTitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String RegisterSubtitle(int TimeLeft) {
        return manager.getString("RegisterSubtitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String UnLog() {
        return manager.getString("UnLog");
    }

    public final String UnLogged() {
        return manager.getString("UnLogged");
    }

    public final String DelAccount() {
        return manager.getString("DelAccount");
    }

    public final String DelAccountError() {
        return manager.getString("DelAccountError");
    }

    public final String DelAccountMatch() {
        return manager.getString("DelAccountMatch");
    }

    public final String AccountDeleted() {
        return manager.getString("AccountDeleted").replace("{newline}", "\n");
    }

    public final String ForcedUnLog(ProxiedPlayer admin) {
        return manager.getString("ForcedUnLog").replace("{player}", admin.getDisplayName());
    }

    public final String ForcedUnLog(String admin) {
        return manager.getString("ForcedUnLog").replace("{player}", admin);
    }

    public final String ForcedUnLogAdmin(ProxiedPlayer target) {
        return manager.getString("ForcedUnLogAdmin").replace("{player}", target.getDisplayName());
    }

    public final String ForcedDelAccount(ProxiedPlayer admin) {
        return manager.getString("ForcedDelAccount").replace("{newline}", "\n").replace("{player}", admin.getDisplayName());
    }

    public final String ForcedDelAccount(String admin) {
        return manager.getString("ForcedDelAccount").replace("{newline}", "\n").replace("{player}", admin);
    }

    public final String ForcedDelAccountAdmin(ProxiedPlayer target) {
        return manager.getString("ForcedDelAccountAdmin").replace("{player}", target.getDisplayName());
    }

    public final String ForcedDelAccountAdmin(String target) {
        return manager.getString("ForcedDelAccountAdmin").replace("{player}", target);
    }

    public final String GAuthLink() {
        return manager.getString("2FaLink");
    }

    public final String GAuthInstructions() {
        List<String> msg = manager.getList("2FaInstructions");
        List<String> replace = new ArrayList<>();

        for (String str : msg) {
            if (!replace.contains(str)) {
                replace.add(str
                        .replace(",", "{replace-comma}")
                        .replace("[", "{replace-one}")
                        .replace("]", "{replace-two}") + "&r");
            }
        }

        return replace.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "&r\n&r")
                .replace("{replace-comma}", ",")
                .replace("{replace-one}", "[")
                .replace("{replace-two}", "]");
    }

    public final String gAuthAuthenticate() {
        return manager.getString("2FaAuthenticate");
    }

    public final String gAuthIncorrect() {
        return manager.getString("2FaIncorrect");
    }

    public final String gAuthCorrect() {
        return manager.getString("2FaCorrect");
    }

    public final String AlreadyFA() {
        return manager.getString("2FaAlready");
    }

    public final String PermissionError(String permission) {
        return manager.getString("PermissionError").replace("{permission}", permission);
    }

    public final String ConnectionError(String player) {
        return manager.getString("ConnectionError").replace("{player}", player);
    }

    public final String NeverPlayed(String player) {
        return manager.getString("NeverPlayed").replace("{player}", player);
    }

    public final String TargetAccessError(ProxiedPlayer player) {
        return manager.getString("TargetAccessError").replace("{player}", player.getName());
    }

    public final String Migrating(String UUID) {
        return manager.getString("Migrating").replace("{uuid}", UUID);
    }

    public final String MigratingYaml(String UUID) {
        return manager.getString("MigratingYaml").replace("{uuid}", UUID);
    }

    public final String MigratingAll() {
        return manager.getString("MigratingAll");
    }

    public final String Migrated() {
        return manager.getString("Migrated");
    }

    public final String MigrationUsage() {
        return manager.getString("MigrationUsage");
    }

    public final String MigrationConnectionError() {
        return manager.getString("MigrationConnectionError");
    }

    public final String PlayerInfoUsage() {
        return manager.getString("PlayerInfoUsage");
    }

    public final String MaxRegisters() {
        List<String> replaced = manager.getList("MaxRegisters");
        for (int i = 0; i < replaced.size(); i++) {
            replaced.set(i, replaced.get(i)
                    .replace("[", "{replace_open}")
                    .replace("]", "{replace_close}")
                    .replace(",", "{replace_comma}") + "&r");
        }
        
        return replaced.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "&r\n&r")
                .replace("{replace_open}", "[")
                .replace("{replace_close}", "]")
                .replace("{replace_comma}", ",");
    }

    public final String LookUpUsage() {
        return manager.getString("LookUpUsage");
    }

    public final String IllegalName(String characters) {
        List<String> msg = manager.getList("IllegalName");
        List<String> replace = new ArrayList<>();

        for (String str : msg) {
            if (!replace.contains(str)) {
                replace.add(str
                        .replace(",", "{replace-comma}")
                        .replace("[", "{replace-one}")
                        .replace("]", "{replace-two}")
                        .replace("{chars}", characters) + "&r");
            }
        }

        return replace.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "&r\n&r")
                .replace("{replace-comma}", ",")
                .replace("{replace-one}", "[")
                .replace("{replace-two}", "]")
                .replace("{replace_comma_gray}", "&7,");
    }

    public final String PinSet(Object pin) {
        return manager.getString("PinSet").replace("{pin}", pin.toString());
    }

    public final String AlreadyPin() {
        return manager.getString("AlreadyPin");
    }

    public final String NoPin() {
        return manager.getString("NoPin");
    }

    public final String PinUsage() {
        return manager.getString("SetPin");
    }

    public final String ResetPin() {
        return manager.getString("ResetPin");
    }

    public final String PinDisabled() {
        return manager.getString("PinDisabled");
    }

    public final String PinLength() {
        return manager.getString("PinLength");
    }

    public final String IncorrectPin() {
        return manager.getString("IncorrectPin");
    }
}
