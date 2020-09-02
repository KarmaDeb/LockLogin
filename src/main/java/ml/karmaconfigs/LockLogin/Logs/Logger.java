package ml.karmaconfigs.LockLogin.Logs;

import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.PlatformUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class Logger {

    private static boolean pluginFolderCreated = false;
    private static boolean folderCreated = false;
    private static boolean fileCreated = false;

    /**
     * Logs the info into a log file
     *
     * @param prefix the info log prefix
     * @param info the info
     */
    public static void log(Platform platform, String prefix, String info) {
        Date date = new Date();
        DateFormat dayFormat = new SimpleDateFormat("dd-MM");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String day = dayFormat.format(date).replace("/", "-");
        String time = timeFormat.format(date);

        File file = null;
        File pluginFolder = null;
        File folder = null;

        prefix = "[" + time + "] " + prefix + ": ";
        String format = prefix + info;
        switch (platform) {
            case SPIGOT:
                pluginFolder = ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot.plugin.getDataFolder();
                folder = new File(pluginFolder + "/logs");
                file = new File(folder, day + ".log");
                break;
            case BUNGEE:
                pluginFolder = ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee.plugin.getDataFolder();
                folder = new File(pluginFolder + "/logs");
                file = new File(folder, day + ".log");
                break;
            case ANY:
                try {
                    pluginFolder = ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot.plugin.getDataFolder();
                    folder = new File(pluginFolder + "/logs");
                    file = new File(folder, day + ".log");
                    break;
                } catch (Throwable e) {
                    pluginFolder = ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee.plugin.getDataFolder();
                    folder = new File(pluginFolder + "/logs");
                    file = new File(folder, day + ".log");
                    break;
                }
        }

        pluginFolderCreated = !pluginFolder.exists();
        folderCreated = !folder.exists();
        fileCreated = !file.exists();

        if (pluginFolder.mkdir()) {
            pluginFolderCreated = true;
        }

        if (folder.mkdir()) {
            folderCreated = true;
        }

        try {
            if (file.createNewFile()) {
                fileCreated = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            PlatformUtils.Alert("An error occurred while creating file " + file.getName(), WarningLevel.ERROR);
        }

        File finalFile = file;
        String finalPrefix = prefix;
        try {
            InputStream fl = new FileInputStream(finalFile);
            InputStreamReader flReader = new InputStreamReader(fl, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(flReader);

            List<String> sets = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                sets.add(line);
            }

            if (pluginFolderCreated) {
                sets.add(finalPrefix + "Plugin folder created");
            }
            if (folderCreated) {
                sets.add(finalPrefix + "Logs folder created");
            }
            if (fileCreated) {
                sets.add(finalPrefix + "Log file created");
            }

            sets.add(format);

            FileWriter writer = new FileWriter(finalFile);
            for (String set : sets) {
                writer.write(set + "\n");
            }

            writer.flush();
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
            PlatformUtils.Alert("An error occurred while writing log", WarningLevel.ERROR);
        }
    }

    /**
     * Logs the info into a log file
     *
     * @param prefix the info log prefix
     * @param info the info
     */
    public static void log(Platform platform, String prefix, Throwable info) {
        Date date = new Date();
        DateFormat dayFormat = new SimpleDateFormat("dd-MM");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String day = dayFormat.format(date).replace("/", "-");
        String time = timeFormat.format(date);

        File file = null;
        File pluginFolder = null;
        File folder = null;
        prefix = "[" + time + "] " + prefix + "";
        switch (platform) {
            case SPIGOT:
                pluginFolder = ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot.plugin.getDataFolder();
                folder = new File(pluginFolder + "/logs");
                file = new File(folder, day + ".log");
                break;
            case BUNGEE:
                pluginFolder = ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee.plugin.getDataFolder();
                folder = new File(pluginFolder + "/logs");
                file = new File(folder, day + ".log");
                break;
            case ANY:
                try {
                    pluginFolder = ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot.plugin.getDataFolder();
                    folder = new File(pluginFolder + "/logs");
                    file = new File(folder, day + ".log");
                    break;
                } catch (Throwable e) {
                    pluginFolder = ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee.plugin.getDataFolder();
                    folder = new File(pluginFolder + "/logs");
                    file = new File(folder, day + ".log");
                    break;
                }
        }

        pluginFolderCreated = !pluginFolder.exists();
        folderCreated = !folder.exists();
        fileCreated = !file.exists();

        if (pluginFolder.mkdir()) {
            pluginFolderCreated = true;
        }

        if (folder.mkdir()) {
            folderCreated = true;
        }

        try {
            if (file.createNewFile()) {
                fileCreated = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            PlatformUtils.Alert("An error occurred while creating file " + file.getName(), WarningLevel.ERROR);
        }

        String finalPrefix = prefix;
        File finalFile = file;
        try {
            InputStream fl = new FileInputStream(finalFile);
            InputStreamReader flReader = new InputStreamReader(fl, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(flReader);

            List<String> sets = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                sets.add(line);
            }

            if (pluginFolderCreated) {
                sets.add(finalPrefix + "Plugin folder created");
            }
            if (folderCreated) {
                sets.add(finalPrefix + "Logs folder created");
            }
            if (fileCreated) {
                sets.add(finalPrefix + "Log file created");
            }

            sets.add(prefix);
            for (StackTraceElement element : info.getStackTrace()) {
                sets.add("                       "  + element);
            }

            FileWriter writer = new FileWriter(finalFile);
            for (String set : sets) {
                writer.write(set + "\n");
            }

            writer.flush();
            writer.close();
        } catch (Throwable e) {
            e.printStackTrace();
            PlatformUtils.Alert("An error occurred while writing log", WarningLevel.ERROR);
        }
    }
}
