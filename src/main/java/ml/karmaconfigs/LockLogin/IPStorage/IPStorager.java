package ml.karmaconfigs.LockLogin.IPStorage;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.PlatformUtils;
import ml.karmaconfigs.LockLogin.Security.Codifications.Codification2;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.WarningLevel;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class IPStorager {

    private final File file;
    private final String ip;

    /**
     * Initialize the IP storage
     * for that IP
     *
     * @param IP the IP
     */
    public IPStorager(InetAddress IP) {
        File file;
        File folder;
        try {
            folder = new File(LockLoginSpigot.plugin.getDataFolder() + "/data/");
        } catch (NoClassDefFoundError e) {
            folder = new File(LockLoginBungee.plugin.getDataFolder() + "/data/");
        }

        if (!folder.exists()) {
            if (folder.mkdir()) {
                PlatformUtils.Alert("Created LockLogin data folder", WarningLevel.WARNING);
            } else {
                PlatformUtils.Alert("An unknown error occurred while creating LockLogin data folder", WarningLevel.ERROR);
            }
        }

        try {
            file = new File(LockLoginSpigot.plugin.getDataFolder() + "/data/", "ips_v2.lldb");
        } catch (Throwable e) {
            file = new File(LockLoginBungee.plugin.getDataFolder() + "/data/", "ips_v2.lldb");
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    PlatformUtils.Alert("Created LockLogin IP data file", WarningLevel.WARNING);
                } else {
                    PlatformUtils.Alert("An unknown error occurred while creating IP data file", WarningLevel.ERROR);
                }
            } catch (Throwable e) {
                Logger.log(Platform.ANY, "ERROR WHILE CREATING LockLogin IP DATA FILE", e);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < IP.getHostAddress().length(); i++) {
            String letter = String.valueOf(IP.getHostAddress().charAt(i));
            if (letter.matches(".*[0-9].*") || letter.equals(".")) {
                builder.append(letter);
            }
        }

        if (!builder.toString().isEmpty()) {
            ip = new Codification2(builder.toString(), false).hash();
        } else {
            ip = new Codification2("127.0.0.1", false).hash();
        }

        this.file = file;
    }

    /**
     * Get the storage args
     *
     * @param arg      the arg
     * @param ipMethod IP/Player name
     * @return a List of String
     */
    public static List<String> getStorage(String arg, boolean ipMethod) {
         File file;
        try {
            file = new File(LockLoginSpigot.plugin.getDataFolder() + "/data/", "ips_v2.lldb");
        } catch (NoClassDefFoundError e) {
            file = new File(LockLoginBungee.plugin.getDataFolder() + "/data/", "ips_v2.lldb");
        }

        if (file.exists()) {
            try {
                List<String> players = new ArrayList<>();

                String toString = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

                String[] finalData = toString.split(";");
                HashMap<String, ArrayList<String>> ipData = new HashMap<>();

                if (!ipMethod) {
                    for (String str : finalData) {
                        String ip = str.split(":")[0];
                        String player = str.replace(ip + ":", "").replace(";", "");

                        ArrayList<String> ipPlayers;
                        if (!ipData.containsKey(ip)) {
                            ipPlayers = new ArrayList<>();
                            ipPlayers.add(player);
                        } else {
                            ipPlayers = ipData.get(ip);
                            if (!ipPlayers.contains(player)) {
                                ipPlayers.add(player);
                            }

                        }
                        ipData.put(ip, ipPlayers);
                    }

                    for (String ip : ipData.keySet()) {
                        if (ipData.getOrDefault(ip, new ArrayList<>()).contains(arg)) {
                            players = ipData.get(ip);
                            break;
                        }
                    }
                } else {
                    for (String str : finalData) {
                        String ip = str.split(":")[0];
                        String player = str.replace(ip + ":", "").replace(";", "");

                        ArrayList<String> ipPlayers;
                        if (!ipData.containsKey(ip)) {
                            ipPlayers = new ArrayList<>();
                            ipPlayers.add(player);
                        } else {
                            ipPlayers = ipData.get(ip);
                            if (!ipPlayers.contains(player)) {
                                ipPlayers.add(player);
                            }

                        }
                        ipData.put(ip, ipPlayers);
                    }

                    String ip = new Codification2(arg, false).hash();

                    players = ipData.getOrDefault(ip, new ArrayList<>());
                }

                return players;
            } catch (Throwable e) {
                Logger.log(Platform.ANY, "ERROR WHILE RETRIEVING LockLogin DATA FOR IP/USERNAME " + arg, e);
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Save the IP to the specified player
     *
     * @param name the player name
     */
    public final void saveStorage(String name) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    PlatformUtils.Alert("Created LockLogin IP data file", WarningLevel.WARNING);
                } else {
                    PlatformUtils.Alert("An unknown error occurred while creating LockLogin IP data file", WarningLevel.ERROR);
                }
            } catch (Throwable e) {
                Logger.log(Platform.ANY, "ERROR WHILE CREATING LockLogin IP DATA FILE", e);
            }
        }

        try {
            InputStreamReader inReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inReader);

            List<String> lines = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!lines.contains(line)) {
                    lines.add(line);
                }
            }

            if (!lines.contains(ip + ":" + name))
                lines.add(ip + ":" + name + ";");

            FileWriter writer = new FileWriter(file);

            for (int i = 0; i < lines.size(); i++) {
                if (i != lines.size() - 1) {
                    writer.write(lines.get(i) + "\n");
                } else {
                    writer.write(lines.get(i));
                }
            }

            inReader.close();
            reader.close();
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE WRITING IN LockLogin IP DATA", e);
        }
    }

    /**
     * Get the assigned players into that IP
     *
     * @return a list of string
     */
    public final HashSet<String> getStorage() {
        HashSet<String> ipPlayers = new HashSet<>();
        if (file.exists()) {
            try {
                String toString = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                String[] finalData = toString.split(";");

                for (String data : finalData) {
                    data = data.replaceAll("\\s","");
                    String dataIP = data.split(":")[0];
                    String dataName = data.split(":")[1].replace(";", "");

                    if (dataIP.equals(ip)) {
                        ipPlayers.add(dataName);
                    }
                }
            } catch (Throwable e) {
                Logger.log(Platform.ANY, "ERROR WHILE RETRIEVING LockLogin IP DATA", e);
            }
        }
        return ipPlayers;
    }

    /**
     * Check if that player and is already set
     *
     * @return a boolean
     */
    public final boolean notSet(String name) {
        return !getStorage().contains(name);
    }
}
