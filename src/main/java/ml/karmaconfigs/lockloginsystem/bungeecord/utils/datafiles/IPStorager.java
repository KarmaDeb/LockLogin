package ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.KarmaFile;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications.Codification2;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class IPStorager implements LockLoginBungee {

    private final static HashMap<ProxiedPlayer, Integer> scan_passed = new HashMap<>();
    private final KarmaFile separated_ip_data;
    private final Module module;

    /**
     * Initialize the ip storager system
     *
     * @param module  the module that is calling the API
     * @param address the ip address
     */
    public IPStorager(final Module module, final InetAddress address) throws UnknownHostException {
        if (isValid(address) && ModuleLoader.manager.isLoaded(module)) {
            migrateFromV2();
            migrateFromV3();

            this.module = module;

            String hashed_ip = new Codification2(address.getHostName(), false).hash();

            separated_ip_data = new KarmaFile(plugin, hashed_ip, "data", "ips_v4");
            if (!separated_ip_data.exists())
                separated_ip_data.create();
        } else {
            throw new UnknownHostException();
        }
    }

    /**
     * Migrate from LockLogin v2 database
     */
    private void migrateFromV2() {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            KarmaFile old_data = new KarmaFile(plugin, "ips_v2.lldb", "data");

            if (old_data.exists()) {
                Console.send(plugin, "Trying to migrate from old ip v2 data...", Level.INFO);

                List<String> lines = old_data.readFullFile();
                for (String str : lines) {
                    String data = str.replace(";", "");

                    try {
                        String ip = data.split(":")[0];
                        String name = data.replace(ip + ":", "");

                        KarmaFile new_data = new KarmaFile(plugin, ip, "data", "ips_v4");
                        if (!new_data.exists())
                            new_data.create();

                        List<String> uuids = new_data.readFullFile();

                        OfflineUser user = new OfflineUser(name);
                        if (user.exists()) {
                            UUID uuid = user.getUUID();

                            if (!uuids.contains(uuid.toString())) {
                                uuids.add(uuid.toString());
                                new_data.write(uuids);
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                try {
                    Files.delete(old_data.getFile().toPath());
                } catch (Throwable ignored) {}
            }
        });
    }

    /**
     * Migrate from LockLogin v3 database
     */
    private void migrateFromV3() {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            KarmaFile old_data = new KarmaFile(plugin, "ips_v3.lldb", "data");

            if (old_data.exists()) {
                Console.send(plugin, "Trying to migrate from old ip v3 data...", Level.INFO);

                List<String> ips = old_data.getStringList("IPs");

                for (String ip : ips) {
                    KarmaFile new_data = new KarmaFile(plugin, ip, "data", "ips_v4");
                    if (!new_data.exists())
                        new_data.create();

                    List<String> stored_uuids = new_data.readFullFile();
                    for (String uuid : old_data.getStringList(ip)) {
                        if (!stored_uuids.contains(uuid)) {
                            stored_uuids.add(uuid);
                        }
                    }

                    new_data.write(stored_uuids);
                }

                try {
                    Files.delete(old_data.getFile().toPath());
                } catch (Throwable ignored) {}
            }
        });
    }

    /**
     * Add user name to the list of players
     * assigned to the ip
     *
     * @param uuid the the player uuid
     */
    public final void save(final UUID uuid) {
        List<String> assigned = separated_ip_data.readFullFile();

        if (!assigned.contains(uuid.toString())) {
            assigned.add(uuid.toString());
            separated_ip_data.write(assigned);
        }
    }

    /**
     * Check if the user can join the server
     *
     * @param uuid the uuid of the player
     * @param max  the maximum amount of accounts
     *             allowed per ip
     * @return if the amount of users is over the max or
     * the user is already saved
     */
    public final boolean canJoin(final UUID uuid, final int max) {
        HashSet<OfflineUser> alts = manager.getAlts(module, null, uuid);
        boolean available = false;
        if (alts != null) {
            for (OfflineUser user : alts) {
                if (user.exists()) {
                    if (user.getUUID().toString().equals(uuid.toString())) {
                        available = true;
                        break;
                    }
                }
            }

            return available || alts.size() < max;
        }

        return true;
    }

    /**
     * Check if the user has alts accounts by checking all alt
     * matching results and checking the amount of names it has
     *
     * @param target the uuid of player target
     * @return if the player has alt account
     */
    public final boolean hasAltAccounts(final UUID target) {
        return getAltsAmount(target) > 0;
    }

    /**
     * Get the amount of alts the player has
     *
     * @param target the uuid of player target
     * @return the amount of player alt accounts
     */
    public final int getAltsAmount(final UUID target) {
        HashSet<OfflineUser> alts = manager.getAlts(module, null, target);
        if (alts != null)
            return alts.size() - 1;

        return 0;
    }

    /**
     * Check if the specified ip is valid
     *
     * @param address the ip to check
     * @return if the specified ip is valid
     */
    private boolean isValid(final InetAddress address) {
        return address.getAddress() != null && address.getHostName() != null && !address.getHostName().isEmpty();
    }

    public interface manager {

        static HashSet<OfflineUser> getAlts(final Module module, final ProxiedPlayer issuer, final UUID target) {
            if (ModuleLoader.manager.isLoaded(module)) {
                boolean showBar = false;
                if (issuer != null) {
                    showBar = true;

                    if (scan_passed.containsKey(issuer)) {
                        issuer.sendMessage(TextComponent.fromLegacyText("&cAlready searching alts..."));
                        return null;
                    }
                }

                File main_folder = new File(plugin.getDataFolder() + File.separator + "data", "ips_v4");
                File[] files = main_folder.listFiles();

                if (files != null) {
                    int max = files.length;
                    scan_passed.put(issuer, 0);

                    if (showBar) {
                        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (!scan_passed.containsKey(issuer)) {
                                        timer.cancel();
                                        issuer.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
                                    } else {
                                        int updated_passed = scan_passed.getOrDefault(issuer, 0);

                                        double division = (double) updated_passed / max;
                                        long iPart = (long) division;
                                        double fPart = division - iPart;

                                        double percentage = fPart * 100.0;

                                        issuer.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                                                StringUtils.toColor("&eScanning ip files: &7" + percentage + "&c%")
                                        ));
                                    }
                                }
                            }, 0, TimeUnit.SECONDS.toMillis(1));
                        });
                    }

                    HashSet<KarmaFile> matching_files = new HashSet<>();
                    for (File file : files) {
                        if (file.isFile()) {
                            KarmaFile ip_data = new KarmaFile(plugin, file.getName(), "data", "ips_v4");
                            List<String> assigned = ip_data.readFullFile();

                            if (assigned.contains(target.toString()))
                                matching_files.add(ip_data);
                        }

                        scan_passed.put(issuer, scan_passed.getOrDefault(issuer, 0) + 1);
                    }

                    HashSet<OfflineUser> users = new HashSet<>();
                    HashSet<String> added_uuids = new HashSet<>();
                    OfflineUser user;
                    for (KarmaFile matching : matching_files) {
                        List<String> uuids = matching.readFullFile();

                        for (String id : uuids) {
                            if (!added_uuids.contains(id)) {
                                user = new OfflineUser(UUID.fromString(id));

                                users.add(user);
                                added_uuids.add(id);
                            }
                        }
                    }

                    scan_passed.remove(issuer);

                    return users;
                }
            }

            return new HashSet<>();
        }
    }
}