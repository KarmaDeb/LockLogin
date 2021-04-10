package ml.karmaconfigs.lockloginsystem.bukkit.utils.files;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.karmayaml.FileCopy;
import ml.karmaconfigs.api.bukkit.karmayaml.YamlReloader;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
            case RUSSIAN:
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_ru.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
            case UNKNOWN:
                Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                messages = YamlConfiguration.loadConfiguration(msg_file);
                break;
        }

        if (!msg_file.exists()) {
            FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName()).withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));

            try {
                creator.copy(msg_file);
                YamlReloader reloader = new YamlReloader(plugin, msg_file, "messages/" + msg_file.getName());

                if (reloader.reloadAndCopy())
                    messages.loadFromString(reloader.getYamlString());
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reloading messages file ( " + msg_file.getName() + " )");
            }
        } else {
            manager.reload();
        }

        if (!loaded)
            loaded = manager.reload();
    }

    public final String prefix() {
        return messages.getString("Prefix");
    }

    public final String bungeeProxy() {
        return messages.getString("BungeeProxy", "&cPlease, connect through bungee proxy!");
    }

    public final String notVerified(Player target) {
        return Objects.requireNonNull(messages.getString("PlayerNotVerified")).replace("{player}", target.getName());
    }

    public final String alreadyPlaying() {
        return messages.getString("AlreadyPlaying");
    }

    public final String captcha(String code) {
        String msg = messages.getString("Captcha");
        assert msg != null;

        ConfigGetter cfg = new ConfigGetter();

        if (cfg.strikethrough()) {
            if (cfg.randomStrikethrough()) {
                String last_color = StringUtils.getLastColor(msg);

                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < code.length(); i++) {
                    int random = new Random().nextInt(100);

                    if (random > 50) {
                        builder.append(last_color).append("&m").append(code.charAt(i)).append("&r");
                    } else {
                        builder.append(last_color).append(code.charAt(i)).append("&r");
                    }
                }

                code = builder.toString();
            } else {
                code = "&m" + code;
            }
        }

        return msg.replace("{captcha}", code);
    }

    public final String typeCaptcha() {
        return Objects.requireNonNull(messages.getString("TypeCaptcha")).replace("{captcha}", "<captcha>");
    }

    public final String captchaTimeOut() {
        return messages.getString("CaptchaTimeOut");
    }

    public final String specifyCaptcha() {
        return messages.getString("SpecifyCaptcha");
    }

    public final String captchaValidated() {
        return messages.getString("CaptchaValidated");
    }

    public final String invalidCaptcha() {
        return messages.getString("InvalidCaptcha");
    }

    public final String invalidCaptcha(final String arguments) {
        return Objects.requireNonNull(messages.getString("InvalidCaptchaArguments")).replace("{arguments}", arguments);
    }

    public final String alreadyCaptcha() {
        return messages.getString("AlreadyCaptcha");
    }

    public final String login(final String captcha) {
        if (captcha != null && !captcha.isEmpty())
            return Objects.requireNonNull(messages.getString("Login")).replace("{captcha}", "<captcha>");
        else
            return Objects.requireNonNull(messages.getString("Login")).replace("<captcha>", "").replace("{captcha}", "");
    }

    public final String logged(Player player) {
        return Objects.requireNonNull(messages.getString("Logged")).replace("{player}", player.getName());
    }

    public final String alreadyLogged() {
        return messages.getString("AlreadyLogged");
    }

    public final String logError() {
        return messages.getString("LogError");
    }

    public final String register(final String captcha) {
        if (captcha != null && !captcha.isEmpty())
            return Objects.requireNonNull(messages.getString("Register")).replace("{captcha}", "<captcha>");
        else
            return Objects.requireNonNull(messages.getString("Register")).replace("<captcha>", "").replace("{captcha}", "");
    }

    public final String registered() {
        return messages.getString("Registered");
    }

    public final String alreadyRegistered() {
        return messages.getString("AlreadyRegistered");
    }

    public final String registerError() {
        return messages.getString("RegisterError");
    }

    public final String passwordInsecure() {
        return messages.getString("PasswordInsecure");
    }

    public final String passwordMinChar() {
        return messages.getString("PasswordMinChar");
    }

    public final String changePass() {
        return messages.getString("ChangePass");
    }

    public final String changeError() {
        return messages.getString("ChangeError");
    }

    public final String changeSame() {
        return messages.getString("ChangeSame");
    }

    public final String changeDone() {
        return messages.getString("ChangeDone");
    }

    public final String reset2FA() {
        return messages.getString("Reset2Fa");
    }

    public final String reseted2FA() {
        return messages.getString("ReseatedFA");
    }

    public final String enable2FA() {
        return messages.getString("Enable2FA");
    }

    public final String toggle2FAError() {
        return messages.getString("ToggleFAError");
    }

    public final String disabled2FA() {
        return messages.getString("Disabled2FA");
    }

    public final String gAuthDisabled() {
        return messages.getString("2FADisabled");
    }

    public final String loginTimeOut() {
        return messages.getString("LoginOut");
    }

    public final String registerTimeOut() {
        return messages.getString("RegisterOut");
    }

    public final String maxIp() {
        return messages.getString("MaxIp");
    }

    public final String loginTitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("LoginTitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String loginSubtitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("LoginSubtitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String registerTitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("RegisterTitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String registerSubtitle(int TimeLeft) {
        return Objects.requireNonNull(messages.getString("RegisterSubtitle")).replace("{time}", String.valueOf(TimeLeft));
    }

    public final String unLogin() {
        return messages.getString("UnLog");
    }

    public final String unLogged() {
        return messages.getString("UnLogged");
    }

    public final String deleteAccount() {
        return messages.getString("DelAccount");
    }

    public final String deleteAccError() {
        return messages.getString("DelAccountError");
    }

    public final String deleteAccMatch() {
        return messages.getString("DelAccountMatch");
    }

    public final String accountDeleted() {
        return Objects.requireNonNull(messages.getString("AccountDeleted"));
    }

    public final String forcedUnLog(Player admin) {
        return Objects.requireNonNull(messages.getString("ForcedUnLog")).replace("{player}", admin.getDisplayName());
    }

    public final String forcedUnLog(String admin) {
        return Objects.requireNonNull(messages.getString("ForcedUnLog")).replace("{player}", admin);
    }

    public final String forcedUnLogAdmin(Player target) {
        return Objects.requireNonNull(messages.getString("ForcedUnLogAdmin")).replace("{player}", target.getDisplayName());
    }

    public final String forceDelAccount(Player admin) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccount")).replace("{player}", admin.getDisplayName());
    }

    public final String forceDelAccount(String admin) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccount")).replace("{player}", admin);
    }

    public final String forceDelAccountAdmin(Player target) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccountAdmin")).replace("{player}", target.getDisplayName());
    }

    public final String forceDelAccountAdmin(String target) {
        return Objects.requireNonNull(messages.getString("ForcedDelAccountAdmin")).replace("{player}", target);
    }

    public final String gAuthLink() {
        return messages.getString("2FaLink");
    }

    public final String gAuthInstructions() {
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

    public final String already2FA() {
        return messages.getString("2FaAlready");
    }

    public final String permission(String permission) {
        return Objects.requireNonNull(messages.getString("PermissionError")).replace("{permission}", permission);
    }

    public final String connectionError(String player) {
        return Objects.requireNonNull(messages.getString("ConnectionError")).replace("{player}", player);
    }

    public final String unknownPlayer(String player) {
        return Objects.requireNonNull(messages.getString("NeverPlayed")).replace("{player}", player);
    }

    public final String targetAccessError(Player player) {
        return Objects.requireNonNull(messages.getString("TargetAccessError")).replace("{player}", player.getName());
    }

    public final String spawnSet() {
        return messages.getString("SpawnSet");
    }

    public final String spawnDisabled() {
        return messages.getString("SpawnDisabled");
    }

    public final String migratingAccount(String UUID) {
        return Objects.requireNonNull(messages.getString("Migrating")).replace("{uuid}", UUID);
    }

    public final String migratingAll() {
        return messages.getString("MigratingAll");
    }

    public final String migrated() {
        return messages.getString("Migrated");
    }

    public final String migrationError() {
        return messages.getString("MigrationConnectionError");
    }

    public final String locationsReseted() {
        return messages.getString("LocationsReset");
    }

    public final String locationReseted(Player player) {
        return Objects.requireNonNull(messages.getString("LocationReset")).replace("{player}", player.getName());
    }

    public final String locationReseted(String player) {
        return Objects.requireNonNull(messages.getString("LocationReset")).replace("{player}", player);
    }

    public final String noLastLocation(Player player) {
        return Objects.requireNonNull(messages.getString("NoLastLocation")).replace("{player}", player.getName());
    }

    public final String noLastLocation(String player) {
        return Objects.requireNonNull(messages.getString("NoLastLocation")).replace("{player}", player);
    }

    public final String resetLastLocUsage() {
        return messages.getString("RestLastLocUsage");
    }

    public final String playerInfoUsage() {
        return messages.getString("PlayerInfoUsage");
    }

    public final String altFound(final String name, final int amount) {
        return Objects.requireNonNull(messages.getString("AltFound")).replace("{player}", name).replace("{alts}", String.valueOf(amount));
    }

    public final String maxRegisters() {
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

    public final String lookupUsage() {
        return messages.getString("LookUpUsage");
    }

    public final String illegalName(String characters) {
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

    public final String pinSet(Object pin) {
        return Objects.requireNonNull(messages.getString("PinSet")).replace("{pin}", pin.toString());
    }

    public final String alreadyPin() {
        return messages.getString("AlreadyPin");
    }

    public final String noPin() {
        return messages.getString("NoPin");
    }

    public final String pinUsage() {
        return messages.getString("SetPin");
    }

    public final String resetPin() {
        return messages.getString("ResetPin");
    }

    public final String pinDisabled() {
        return messages.getString("PinDisabled");
    }

    public final String pinLength() {
        return messages.getString("PinLength");
    }

    public final String incorrectPin() {
        return messages.getString("IncorrectPin");
    }

    public final String antiBot() {
        ConfigGetter cfg = new ConfigGetter();

        List<String> replaced = messages.getStringList("AntiBot");
        for (int i = 0; i < replaced.size(); i++) {
            replaced.set(i, replaced.get(i)
                    .replace("{config:ServerName}", cfg.serverName())
                    .replace("{ServerName}", cfg.serverName())
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
        long seconds = TimeUnit.SECONDS.toSeconds(time);
        long minutes = TimeUnit.SECONDS.toMinutes(time);
        long hours = TimeUnit.SECONDS.toHours(time);

        String format;
        long final_time;
        if (seconds <= 59) {
            format = "sec(s)";
            final_time = seconds;
        } else {
            if (minutes <= 59) {
                format = "min(s) and " + Math.abs((minutes * 60) - seconds) + " sec(s)";
                final_time = minutes;
            } else {
                format = "hour(s) " + Math.abs((hours * 60) - minutes) + " min(s)";
                final_time = hours;
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

    public final String onlyAzuriom() {
        ConfigGetter cfg = new ConfigGetter();

        List<String> replaced = messages.getStringList("OnlyAzuriom");
        for (int i = 0; i < replaced.size(); i++) {
            replaced.set(i, replaced.get(i)
                    .replace("{config:ServerName}", cfg.serverName())
                    .replace("{ServerName}", cfg.serverName())
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

    /**
     * Get the messages manager
     */
    public interface manager {

        /**
         * Reload the messages file
         *
         * @return if the file could be reloaded
         */
        static boolean reload() {
            ConfigGetter cfg = new ConfigGetter();

            if (!cfg.isBungeeCord()) {
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
                    case RUSSIAN:
                        msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_ru.yml");
                        messages = YamlConfiguration.loadConfiguration(msg_file);
                        break;
                    case UNKNOWN:
                        Console.send(plugin, "&cERROR UNKNOWN LANG, valid languages are: &een_EN&b[English]&7, &ees_ES&b[Spanish]&7, &ezh_CN&b[Simplified_Chinese]&7, &eit_IT&b[Italian]&7, &epl_PL&b[Polish]&7, &efr_FR&b[French]&7, &ecz_CS&b[Czech]", Level.WARNING);
                        msg_file = new File(plugin.getDataFolder() + File.separator + "lang", "messages_en.yml");
                        messages = YamlConfiguration.loadConfiguration(msg_file);
                        break;
                }

                if (!msg_file.exists()) {
                    FileCopy creator = new FileCopy(plugin, "messages/" + msg_file.getName()).withDebug(FileInfo.apiDebug(LockLoginSpigot.getJar()));

                    try {
                        creator.copy(msg_file);
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
            }

            return false;
        }

        /**
         * Load BungeeCord messages
         *
         * @param yaml the BungeeCord yaml string
         */
        static void loadBungee(final String yaml) {
            ConfigGetter cfg = new ConfigGetter();

            if (cfg.isBungeeCord()) {
                try {
                    messages.loadFromString(yaml);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
