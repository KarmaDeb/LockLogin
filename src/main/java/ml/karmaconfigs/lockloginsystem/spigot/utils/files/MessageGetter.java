package ml.karmaconfigs.lockloginsystem.spigot.utils.files;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.karmayaml.FileCopy;
import ml.karmaconfigs.api.spigot.karmayaml.YamlReloader;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

public final class MessageGetter implements LockLoginSpigot {

    private static File msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
    private static YamlConfiguration messages;

    private static boolean loaded = false;

    public MessageGetter() {
        ConfigGetter cfg = new ConfigGetter();
        switch (cfg.getLang()) {
            case ENGLISH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case SPANISH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_es.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case SIMPLIFIED_CHINESE:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_zh.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case ITALIAN:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_it.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case POLISH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_pl.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case FRENCH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_fr.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case CZECH:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_cz.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case UNKNOWN:
                Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
        }

        if (!msg_file.exists()) {
            System.out.println(msg_file.getName());
            FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName());

            if (creator.copy(msg_file)) {
                try {
                    YamlReloader reloader = new YamlReloader(plugin, msg_file, "messages/" + msg_file.getName());

                    if (reloader.reloadAndCopy())
                        messages.loadFromString(reloader.getYamlString());
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while reloading messages file ( " + msg_file.getName() + " )");
                }
            }
        } else {
            manager.reload();
        }

        if (!loaded)
            loaded = manager.reload();
    }

    public final String Prefix() {
        return messages.getString("Prefix");
    }

    public final String NotVerified(Player target) {
        return Objects.requireNonNull(messages.getString("PlayerNotVerified")).replace("{player}", target.getName());
    }

    public final String AlreadyPlaying() {
        return messages.getString("AlreadyPlaying");
    }

    public final String Login() {
        return messages.getString("Login");
    }

    public final String Logged(Player player) {
        return Objects.requireNonNull(messages.getString("Logged")).replace("{player}", player.getName());
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
        return Objects.requireNonNull(messages.getString("LoginTitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String LoginSubtitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("LoginSubtitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String RegisterTitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("RegisterTitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String RegisterSubtitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("RegisterSubtitle")).replace("{time}", String.valueOf(TimeLeft));
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
        return Objects.requireNonNull(messages.getString("AccountDeleted")).replace("{newline}", "\n");
    }

    public final String ForcedUnLog(Player admin) {
        return Objects.requireNonNull(messages.getString("ForcedUnLog")).replace("{player}", admin.getDisplayName());
    }

    public final String ForcedUnLog(String admin) {
        return Objects.requireNonNull(messages.getString("ForcedUnLog")).replace("{player}", admin);
    }

    public final String ForcedUnLogAdmin(Player target) {
        return Objects.requireNonNull(messages.getString("ForcedUnLogAdmin")).replace("{player}", target.getDisplayName());
    }

    public final String ForcedDelAccount(Player admin) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccount")).replace("{newline}", "\n").replace("{player}", admin.getDisplayName());
    }

    public final String ForcedDelAccount(String admin) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccount")).replace("{newline}", "\n").replace("{player}", admin);
    }

    public final String ForcedDelAccountAdmin(Player target) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccountAdmin")).replace("{player}", target.getDisplayName());
    }

    public final String ForcedDelAccountAdmin(String target) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccountAdmin")).replace("{player}", target);
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
                .replace(",", "\n")
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
        return Objects.requireNonNull(messages.getString("PermissionError")).replace("{permission}", permission);
    }

    public final String ConnectionError(String player) {
        return Objects.requireNonNull(messages.getString("ConnectionError")).replace("{player}", player);
    }

    public final String NeverPlayed(String player) {
        return Objects.requireNonNull(messages.getString("NeverPlayed")).replace("{player}", player);
    }

    public final String TargetAccessError(Player player) {
        return Objects.requireNonNull(messages.getString("TargetAccessError")).replace("{player}", player.getName());
    }

    public final String SpawnSet() {
        return messages.getString("SpawnSet");
    }

    public final String SpawnDisabled() {
        return messages.getString("SpawnDisabled");
    }

    public final String Migrating(String UUID) {
        return Objects.requireNonNull(messages.getString("Migrating")).replace("{uuid}", UUID);
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

    public final String LocationsReset() {
        return messages.getString("LocationsReset");
    }

    public final String LocationReset(Player player) {
        return Objects.requireNonNull(messages.getString("LocationReset")).replace("{player}", player.getName());
    }

    public final String LocationReset(String player) {
        return Objects.requireNonNull(messages.getString("LocationReset")).replace("{player}", player);
    }

    public final String NoLastLocation(Player player) {
        return Objects.requireNonNull(messages.getString("NoLastLocation")).replace("{player}", player.getName());
    }

    public final String NoLastLocation(String player) {
        return Objects.requireNonNull(messages.getString("NoLastLocation")).replace("{player}", player);
    }

    public final String RestLastLocUsage() {
        return messages.getString("RestLastLocUsage");
    }

    public final String PlayerInfoUsage() {
        return messages.getString("PlayerInfoUsage");
    }

    public final String altFound(final String name, final int amount) {
        return Objects.requireNonNull(messages.getString("AltFound")).replace("{player}", name).replace("{alts}", String.valueOf(amount));
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
                .replace(",", "\n")
                .replace("{replace-comma}", ",")
                .replace("{replace-one}", "[")
                .replace("{replace-two}", "]");
    }

    public final String PinSet(Object pin) {
        return Objects.requireNonNull(messages.getString("PinSet")).replace("{pin}", pin.toString());
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

    public final String AntiBot() {
        ConfigGetter cfg = new ConfigGetter();

        List<String> replaced = messages.getStringList("AntiBot");
        for (int i = 0; i < replaced.size(); i++) {
            replaced.set(i, replaced.get(i)
                    .replace("{config:ServerName}", cfg.ServerName())
                    .replace("{ServerName}", cfg.ServerName())
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

    public final String ipBlocked(final long time) {
        String format = "sec(s)";
        int final_time = (int) time;
        if (TimeUnit.SECONDS.toMinutes(time) >= 1 && TimeUnit.SECONDS.toMinutes(time) <= 60) {
            format = "min(s)";
            final_time = (int) TimeUnit.SECONDS.toMinutes(time);
        } else {
            if (TimeUnit.SECONDS.toHours(time) >= 1) {
                format = "hour(s)";
                final_time = (int) TimeUnit.SECONDS.toHours(time);
            }
        }

        List<String> msg = messages.getStringList("IpBlocked");
        List<String> replace = new ArrayList<>();

        for (String str : msg) {
            if (!replace.contains(str)) {
                replace.add(str
                        .replace(",", "{replace-comma}")
                        .replace("[", "{replace-one}")
                        .replace("]", "{replace-two}")
                        .replace("{time}", String.valueOf(final_time))
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
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case SPANISH:
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_es.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case SIMPLIFIED_CHINESE:
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_zh.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case ITALIAN:
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_it.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case POLISH:
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_pl.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case FRENCH:
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_fr.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case CZECH:
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_cz.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
                case UNKNOWN:
                    Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                    msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                    messages = YamlConfiguration.loadConfiguration(msg_file);
                    break;
            }

            if (!msg_file.exists()) {
                FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName());

                if (creator.copy(msg_file)) {
                    try {
                        InputStream stream = plugin.getResource("messages/" + msg_file.getName());
                        if (stream != null) {
                            YamlReloader reloader = new YamlReloader(plugin, msg_file, "messages/" + msg_file.getName());

                            if (reloader.reloadAndCopy())
                                messages.loadFromString(reloader.getYamlString());
                        }
                    } catch (Throwable e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while reloading messages file ( " + msg_file.getName() + " )");
                    }
                }
            }

            try {
                messages.save(msg_file);
                InputStream stream = plugin.getResource("messages/" + msg_file.getName());
                if (stream != null) {
                    YamlReloader reloader = new YamlReloader(plugin, msg_file, "messages/" + msg_file.getName());
                    if (reloader.reloadAndCopy()) {
                        messages.loadFromString(reloader.getYamlString());
                        return true;
                    }
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reloading config file");
            }

            return false;
        }
    }
}
