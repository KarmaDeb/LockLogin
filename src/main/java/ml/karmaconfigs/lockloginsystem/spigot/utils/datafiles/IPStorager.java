package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.api.spigot.KarmaFile;
import ml.karmaconfigs.api.spigot.reflections.BarMessage;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.Codification2;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class IPStorager implements LockLoginSpigot {

    private final static Map<UUID, Integer> scan_passed = new HashMap<>();

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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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
                    } catch (Throwable ignored) {
                    }
                }

                try {
                    Files.delete(old_data.getFile().toPath());
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * Migrate from LockLogin v3 database
     */
    private void migrateFromV3() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
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
                } catch (Throwable ignored) {
                }
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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (ModuleLoader.manager.isLoaded(module)) {
                List<String> assigned = separated_ip_data.readFullFile();

                if (!assigned.contains(uuid.toString())) {
                    assigned.add(uuid.toString());
                    separated_ip_data.write(assigned);
                }
            }
        });
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
        if (ModuleLoader.manager.isLoaded(module)) {
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

        return false;
    }

    /**
     * Check if the user has alts accounts by checking all alt
     * matching results and checking the amount of names it has
     *
     * @param target the uuid of player target
     * @return if the player has alt account
     */
    public final boolean hasAltAccounts(final UUID target) {
        if (ModuleLoader.manager.isLoaded(module))
            return getAltsAmount(target) > 0;
        else
            return false;
    }

    /**
     * Get the amount of alts the player has
     *
     * @param target the uuid of player target
     * @return the amount of player alt accounts
     */
    public final int getAltsAmount(final UUID target) {
        if (ModuleLoader.manager.isLoaded(module)) {
            HashSet<OfflineUser> alts = manager.getAlts(module, null, target);
            if (alts != null)
                return alts.size() - 1;

            return 0;
        }

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

        static HashSet<OfflineUser> getAlts(final Module module, final Player issuer, final UUID target) {
            if (ModuleLoader.manager.isLoaded(module)) {
                boolean showBar = false;
                if (issuer != null) {
                    showBar = true;

                    if (scan_passed.containsKey(issuer.getUniqueId())) {
                        issuer.sendMessage(StringUtils.toColor("&cAlready searching alts..."));
                        return null;
                    }
                }

                File main_folder = new File(plugin.getDataFolder() + File.separator + "data", "ips_v4");
                File[] files = main_folder.listFiles();

                if (files != null) {
                    int max = files.length;
                    if (issuer != null)
                        scan_passed.put(issuer.getUniqueId(), 0);

                    if (showBar) {
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                            BarMessage bar = new BarMessage(issuer, "&eScanning ip files: &7STARTING");
                            bar.send(true);

                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (!scan_passed.containsKey(issuer.getUniqueId())) {
                                        timer.cancel();
                                        bar.setMessage("");
                                        bar.stop();
                                    } else {
                                        int updated_passed = scan_passed.getOrDefault(issuer.getUniqueId(), 0);

                                        double division = (double) updated_passed / max;
                                        long iPart = (long) division;
                                        double fPart = division - iPart;

                                        double percentage = fPart * 100.0;

                                        bar.setMessage("&eScanning ip files: &7" + percentage + "&c%");
                                    }
                                }
                            }, 0, TimeUnit.SECONDS.toMillis(2));
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

                        if (issuer != null)
                            scan_passed.put(issuer.getUniqueId(), scan_passed.getOrDefault(issuer.getUniqueId(), 0) + 1);
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

                    if (issuer != null)
                        scan_passed.remove(issuer.getUniqueId());

                    return users;
                }
            }

            return new HashSet<>();
        }
    }
}