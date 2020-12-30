package ml.karmaconfigs.lockloginsystem.shared.ipstorage;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications.Codification2;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Private GSA code
 * <p>
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
    public IPStorager(Module loader, InetAddress IP) {
        if (ModuleLoader.manager.isLoaded(loader)) {
            File file;
            File folder;
            try {
                folder = new File(LockLoginSpigot.plugin.getDataFolder() + "/data/");
            } catch (NoClassDefFoundError e) {
                folder = new File(LockLoginBungee.plugin.getDataFolder() + "/data/");
            }

            if (!folder.exists()) {
                if (folder.mkdir()) {
                    PlatformUtils.Alert("Created LockLogin data folder", Level.INFO);
                } else {
                    PlatformUtils.Alert("An unknown error occurred while creating LockLogin data folder", Level.GRAVE);
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
                        PlatformUtils.Alert("Created LockLogin IP data file", Level.INFO);
                    } else {
                        PlatformUtils.Alert("An unknown error occurred while creating IP data file", Level.GRAVE);
                    }
                } catch (Throwable e) {
                    PlatformUtils.log(e, Level.GRAVE);
                    PlatformUtils.log("Error while creating IP storage data file", Level.INFO);
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
        } else {
            this.file = null;
            this.ip = null;
        }
    }

    /**
     * Initialize the IP storage
     * for that IP
     *
     * @param IP the IP
     */
    public IPStorager(ml.karmaconfigs.lockloginmodules.bungee.Module loader, InetAddress IP) {
        if (ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader.manager.isLoaded(loader)) {
            File file;
            File folder;
            try {
                folder = new File(LockLoginSpigot.plugin.getDataFolder() + "/data/");
            } catch (NoClassDefFoundError e) {
                folder = new File(LockLoginBungee.plugin.getDataFolder() + "/data/");
            }

            if (!folder.exists()) {
                if (folder.mkdir()) {
                    PlatformUtils.Alert("Created LockLogin data folder", Level.INFO);
                } else {
                    PlatformUtils.Alert("An unknown error occurred while creating LockLogin data folder", Level.GRAVE);
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
                        PlatformUtils.Alert("Created LockLogin IP data file", Level.INFO);
                    } else {
                        PlatformUtils.Alert("An unknown error occurred while creating IP data file", Level.GRAVE);
                    }
                } catch (Throwable e) {
                    PlatformUtils.log(e, Level.GRAVE);
                    PlatformUtils.log("Error while creating IP storage data file", Level.INFO);
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
        } else {
            this.file = null;
            this.ip = null;
        }
    }

    /**
     * Get the storage args
     *
     * @param arg      the arg
     * @param ipMethod IP/Player name
     * @return all the stored IPs / Players associated to the specified IP / Player
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
                PlatformUtils.log(e, Level.GRAVE);
                PlatformUtils.log("Error while retrieving " + (ipMethod ? "ip data" : "player data") + " of " + arg, Level.INFO);
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
        if (file != null && ip != null) {
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        PlatformUtils.Alert("Created LockLogin IP data file", Level.WARNING);
                    } else {
                        PlatformUtils.Alert("An unknown error occurred while creating LockLogin IP data file", Level.GRAVE);
                    }
                } catch (Throwable e) {
                    PlatformUtils.log(e, Level.GRAVE);
                    PlatformUtils.log("Error while creating IP storage data file", Level.INFO);
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
                PlatformUtils.log(e, Level.GRAVE);
                PlatformUtils.log("Error while writing into IP storage data", Level.INFO);
            }
        }
    }

    /**
     * Get the assigned players into that IP
     *
     * @return all the accounts attached to the IP
     */
    public final HashSet<String> getStorage() {
        if (file != null && ip != null) {
            HashSet<String> ipPlayers = new HashSet<>();
            if (file.exists()) {
                try {
                    String toString = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                    String[] finalData = toString.split(";");

                    for (String data : finalData) {
                        data = data.replaceAll("\\s", "");
                        String dataIP = data.split(":")[0];
                        String dataName = data.replace(dataIP + ":", "");

                        if (dataIP.equals(ip)) {
                            ipPlayers.add(dataName);
                        }
                    }
                } catch (Throwable e) {
                    PlatformUtils.log(e, Level.GRAVE);
                    PlatformUtils.log("Error while retrieving IP storage data", Level.INFO);
                }
            }
            return ipPlayers;
        }

        return new HashSet<>();
    }

    /**
     * Check if that player and is already set
     *
     * @param name the user name
     * @return if the player is set in the storage
     */
    public final boolean notSet(String name) {
        if (file != null && ip != null) {
            return !getStorage().contains(name);
        }

        return false;
    }
}
