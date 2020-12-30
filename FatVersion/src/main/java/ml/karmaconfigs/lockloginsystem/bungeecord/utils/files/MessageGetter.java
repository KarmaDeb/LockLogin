package ml.karmaconfigs.lockloginsystem.bungeecord.utils.files;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.karmayaml.FileCopy;
import ml.karmaconfigs.api.bungee.karmayaml.YamlReloader;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

public final class MessageGetter implements LockLoginBungee {

    private static File msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
    private static Configuration messages;

    private static boolean loaded = false;

    public MessageGetter() {
        ConfigGetter cfg = new ConfigGetter();
        switch (cfg.getLang()) {
            case ENGLISH:
                msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
                break;
            case SPANISH:
                msg_file = new File(plugin.getDataFolder(), "messages_es.yml");
                break;
            case SIMPLIFIED_CHINESE:
                msg_file = new File(plugin.getDataFolder(), "messages_zh.yml");
                break;
            case ITALIAN:
                msg_file = new File(plugin.getDataFolder(), "messages_it.yml");
                break;
            case POLISH:
                msg_file = new File(plugin.getDataFolder(), "messages_pl.yml");
                break;
            case FRENCH:
                msg_file = new File(plugin.getDataFolder(), "messages_fr.yml");
                break;
            case CZECH:
                msg_file = new File(plugin.getDataFolder(), "messages_cz.yml");
                break;
            case UNKNOWN:
                Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
                break;
        }

        try {
            messages = YamlConfiguration.getProvider(YamlConfiguration.class).load(msg_file);
        } catch (Throwable ex) {
            logger.scheduleLog(Level.GRAVE, ex);
            Console.send(plugin, "An error occurred while loading messages", Level.GRAVE);
        }

        if (!msg_file.exists() || messages == null) {
            FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName());

            if (creator.copy(msg_file)) {
                try {
                    messages = YamlConfiguration.getProvider(YamlConfiguration.class).load(msg_file);

                    YamlReloader reloader = new YamlReloader(plugin, msg_file, "messages/" + msg_file.getName());

                    if (reloader.reloadAndCopy())
                        logger.scheduleLog(Level.INFO, "Reloaded message file ( " + msg_file.getName() + " )");
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while reloading messages file ( " + msg_file.getName() + " )");
                }
            }
        }

        if (!loaded)
            loaded = manager.reload();
    }

    public final String Prefix() {
        return messages.getString("Prefix");
    }

    public final String AlreadyPlaying() {
        return messages.getString("AlreadyPlaying");
    }

    public final String Login() {
        return messages.getString("Login");
    }

    public final String Logged(ProxiedPlayer player) {
        return messages.getString("Logged").replace("{player}", player.getName());
    }

    public final String AlreadyLogged() {
        return messages.getString("AlreadyLogged");
    }

    public final String LogError() {
        return messages.getString("LogError");
    }

    public final String Register() {
        return messages.getString("Register");
    }

    public final String Registered() {
        return messages.getString("Registered");
    }

    public final String AlreadyRegistered() {
        return messages.getString("AlreadyRegistered");
    }

    public final String RegisterError() {
        return messages.getString("RegisterError");
    }

    public final String PasswordInsecure() {
        return messages.getString("PasswordInsecure");
    }

    public final String PasswordMinChar() {
        return messages.getString("PasswordMinChar");
    }

    public final String ChangePass() {
        return messages.getString("ChangePass");
    }

    public final String ChangeError() {
        return messages.getString("ChangeError");
    }

    public final String ChangeSame() {
        return messages.getString("ChangeSame");
    }

    public final String ChangeDone() {
        return messages.getString("ChangeDone");
    }

    public final String Reset2Fa() {
        return messages.getString("Reset2Fa");
    }

    public final String ReseatedFA() {
        return messages.getString("ReseatedFA");
    }

    public final String Enable2FA() {
        return messages.getString("Enable2FA");
    }

    public final String ToggleFAError() {
        return messages.getString("ToggleFAError");
    }

    public final String Disabled2FA() {
        return messages.getString("Disabled2FA");
    }

    public final String GAuthDisabled() {
        return messages.getString("2FADisabled");
    }

    public final String LoginOut() {
        return messages.getString("LoginOut");
    }

    public final String RegisterOut() {
        return messages.getString("RegisterOut");
    }

    public final String MaxIp() {
        return messages.getString("MaxIp");
    }

    public final String LoginTitle(int TimeLeft) {
        return messages.getString("LoginTitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String LoginSubtitle(int TimeLeft) {
        return messages.getString("LoginSubtitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String RegisterTitle(int TimeLeft) {
        return messages.getString("RegisterTitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String RegisterSubtitle(int TimeLeft) {
        return messages.getString("RegisterSubtitle").replace("{time}", String.valueOf(TimeLeft));
    }

    public final String UnLog() {
        return messages.getString("UnLog");
    }

    public final String UnLogged() {
        return messages.getString("UnLogged");
    }

    public final String DelAccount() {
        return messages.getString("DelAccount");
    }

    public final String DelAccountError() {
        return messages.getString("DelAccountError");
    }

    public final String DelAccountMatch() {
        return messages.getString("DelAccountMatch");
    }

    public final String AccountDeleted() {
        return messages.getString("AccountDeleted").replace("{newline}", "\n");
    }

    public final String ForcedUnLog(ProxiedPlayer admin) {
        return messages.getString("ForcedUnLog").replace("{player}", admin.getDisplayName());
    }

    public final String ForcedUnLog(String admin) {
        return messages.getString("ForcedUnLog").replace("{player}", admin);
    }

    public final String ForcedUnLogAdmin(ProxiedPlayer target) {
        return messages.getString("ForcedUnLogAdmin").replace("{player}", target.getDisplayName());
    }

    public final String ForcedDelAccount(ProxiedPlayer admin) {
        return messages.getString("ForcedDelAccount").replace("{newline}", "\n").replace("{player}", admin.getDisplayName());
    }

    public final String ForcedDelAccount(String admin) {
        return messages.getString("ForcedDelAccount").replace("{newline}", "\n").replace("{player}", admin);
    }

    public final String ForcedDelAccountAdmin(ProxiedPlayer target) {
        return messages.getString("ForcedDelAccountAdmin").replace("{player}", target.getDisplayName());
    }

    public final String ForcedDelAccountAdmin(String target) {
        return messages.getString("ForcedDelAccountAdmin").replace("{player}", target);
    }

    public final String GAuthLink() {
        return messages.getString("2FaLink");
    }

    public final String GAuthInstructions() {
        List<String> msg = messages.getStringList("2FaInstructions");
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
        return messages.getString("2FaAuthenticate");
    }

    public final String gAuthIncorrect() {
        return messages.getString("2FaIncorrect");
    }

    public final String gAuthCorrect() {
        return messages.getString("2FaCorrect");
    }

    public final String AlreadyFA() {
        return messages.getString("2FaAlready");
    }

    public final String PermissionError(String permission) {
        return messages.getString("PermissionError").replace("{permission}", permission);
    }

    public final String ConnectionError(String player) {
        return messages.getString("ConnectionError").replace("{player}", player);
    }

    public final String NeverPlayed(String player) {
        return messages.getString("NeverPlayed").replace("{player}", player);
    }

    public final String TargetAccessError(ProxiedPlayer player) {
        return messages.getString("TargetAccessError").replace("{player}", player.getName());
    }

    public final String Migrating(String UUID) {
        return messages.getString("Migrating").replace("{uuid}", UUID);
    }

    public final String MigratingYaml(String UUID) {
        return messages.getString("MigratingYaml").replace("{uuid}", UUID);
    }

    public final String MigratingAll() {
        return messages.getString("MigratingAll");
    }

    public final String Migrated() {
        return messages.getString("Migrated");
    }

    public final String MigrationConnectionError() {
        return messages.getString("MigrationConnectionError");
    }

    public final String PlayerInfoUsage() {
        return messages.getString("PlayerInfoUsage");
    }

    public final String MaxRegisters() {
        List<String> replaced = messages.getStringList("MaxRegisters");
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
        return messages.getString("LookUpUsage");
    }

    public final String IllegalName(String characters) {
        List<String> msg = messages.getStringList("IllegalName");
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
        return messages.getString("PinSet").replace("{pin}", pin.toString());
    }

    public final String AlreadyPin() {
        return messages.getString("AlreadyPin");
    }

    public final String NoPin() {
        return messages.getString("NoPin");
    }

    public final String PinUsage() {
        return messages.getString("SetPin");
    }

    public final String ResetPin() {
        return messages.getString("ResetPin");
    }

    public final String PinDisabled() {
        return messages.getString("PinDisabled");
    }

    public final String PinLength() {
        return messages.getString("PinLength");
    }

    public final String IncorrectPin() {
        return messages.getString("IncorrectPin");
    }

    public final String ipBlocked(final long time) {
        String format = "min(s)";
        if (TimeUnit.MINUTES.toSeconds(time) <= 60)
            format = "sec(s)";
        if (TimeUnit.MINUTES.toHours(time) >= 1)
            format = "hour(s)";

        List<String> msg = messages.getStringList("IpBlocked");
        List<String> replace = new ArrayList<>();

        for (String str : msg) {
            if (!replace.contains(str)) {
                replace.add(str
                        .replace(",", "{replace-comma}")
                        .replace("[", "{replace-one}")
                        .replace("]", "{replace-two}")
                        .replace("{time}", String.valueOf(Integer.parseInt(String.valueOf(time))))
                        .replace("{time_format}", format) + "&r");
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

    public interface manager {

        static boolean reload() {
            ConfigGetter cfg = new ConfigGetter();
            switch (cfg.getLang()) {
                case ENGLISH:
                    msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
                    break;
                case SPANISH:
                    msg_file = new File(plugin.getDataFolder(), "messages_es.yml");
                    break;
                case SIMPLIFIED_CHINESE:
                    msg_file = new File(plugin.getDataFolder(), "messages_zh.yml");
                    break;
                case ITALIAN:
                    msg_file = new File(plugin.getDataFolder(), "messages_it.yml");
                    break;
                case POLISH:
                    msg_file = new File(plugin.getDataFolder(), "messages_pl.yml");
                    break;
                case FRENCH:
                    msg_file = new File(plugin.getDataFolder(), "messages_fr.yml");
                    break;
                case CZECH:
                    msg_file = new File(plugin.getDataFolder(), "messages_cz.yml");
                    break;
                case UNKNOWN:
                    Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                    msg_file = new File(plugin.getDataFolder(), "messages_en.yml");
                    break;
            }

            try {
                messages = YamlConfiguration.getProvider(YamlConfiguration.class).load(msg_file);
            } catch (Throwable ex) {
                logger.scheduleLog(Level.GRAVE, ex);
                Console.send(plugin, "An error occurred while loading messages", Level.GRAVE);
            }

            try {
                YamlReloader reloader = new YamlReloader(plugin, msg_file, "messages/" + msg_file.getName());
                if (reloader.reloadAndCopy()) {
                    messages = YamlConfiguration.getProvider(YamlConfiguration.class).load(msg_file);
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
