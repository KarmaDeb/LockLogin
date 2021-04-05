package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.bukkit.KarmaFile;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.Codification2;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.*;

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
public final class IPStorager implements LockLoginSpigot {

    private final KarmaFile separated_ip_data;
    private final Module module;

    /**
     * Initialize the ip storager system
     *
     * @param module  the module that is calling the API
     * @param address the ip address
     * @throws UnknownHostException with a null ip
     */
    public IPStorager(final Module module, final InetAddress address) throws UnknownHostException {
        if (isValid(address) && ModuleLoader.manager.isLoaded(module)) {
            migrateFromV2();
            migrateFromV3();
            migrateFromV4();

            this.module = module;

            String hashed_ip = new Codification2(address.getHostName(), false).hash();

            separated_ip_data = new KarmaFile(plugin, hashed_ip, "data", "ips_v5");
            if (!separated_ip_data.exists())
                separated_ip_data.create();
        } else {
            throw new UnknownHostException();
        }
    }

    /**
     * Migrate from LockLogin v2 database
     */
    public static void migrateFromV2() {
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

                        OfflineUser user = new OfflineUser("", name, true);
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
    public static void migrateFromV3() {
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
     * Migrate from LockLogin v4 database
     */
    public static void migrateFromV4() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File ips_v4 = new File(plugin.getDataFolder() + File.separator + "data", "ips_v4");
            File[] data = ips_v4.listFiles();

            if (data != null) {
                for (File file : data) {
                    KarmaFile v4File = new KarmaFile(file);
                    List<String> ids = v4File.readFullFile();

                    KarmaFile v5File = new KarmaFile(plugin, v4File.getFile().getName(), "data", "ips_v5");
                    if (!v5File.exists())
                        v5File.create();

                    for (String id : ids) {
                        KarmaFile library = new KarmaFile(plugin, id.replace("-", "").toLowerCase(), "data", "ips_library");
                        if (!library.exists())
                            library.create();

                        List<String> ip_files = library.getStringList("DATA");
                        if (!ip_files.contains(v5File.getFile().getName())) {
                            ip_files.add(v5File.getFile().getName());
                            library.set("DATA", ip_files);
                        }

                        List<String> assigned = v5File.getStringList("UUIDs");
                        if (!assigned.contains(id)) {
                            assigned.add(id);
                            v5File.set("UUIDs", assigned);
                        }

                        List<String> libraries_owner = v5File.getStringList("LIBRARIES");
                        if (!libraries_owner.contains(library.getFile().getName())) {
                            libraries_owner.add(library.getFile().getName());
                            v5File.set("LIBRARIES", libraries_owner);
                        }
                    }

                    try {
                        Files.delete(v4File.getFile().toPath());
                    } catch (Throwable ignored) {
                    }
                }

                try {
                    Files.delete(ips_v4.toPath());
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
    public final void save(final UUID uuid, final String name) {
        KarmaFile library = new KarmaFile(plugin, uuid.toString().replace("-", "").toLowerCase(), "data", "ips_library");
        if (!library.exists())
            library.create();

        List<String> libraries = library.getStringList("DATA");
        if (!libraries.contains(separated_ip_data.getFile().getName())) {
            libraries.add(separated_ip_data.getFile().getName());
            library.set("DATA", libraries);
        }

        List<String> assigned = separated_ip_data.getStringList("UUIDs");
        if (!assigned.contains(uuid.toString())) {
            assigned.add(uuid.toString());
            separated_ip_data.set("UUIDs", assigned);
        }

        List<String> libraries_owner = separated_ip_data.getStringList("LIBRARIES");
        if (!libraries_owner.contains(library.getFile().getName())) {
            libraries_owner.add(library.getFile().getName());
            separated_ip_data.set("LIBRARIES", libraries_owner);
        }

        if (!separated_ip_data.isSet(uuid.toString()))
            separated_ip_data.set(uuid.toString(), name);
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
    @Deprecated
    public final boolean canJoin(final UUID uuid, final int max) {
        Set<OfflineUser> alts = manager.getAlts(module, uuid);
        boolean available = false;
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

    /**
     * Check if the user has alts accounts by checking all alt
     * matching results and checking the amount of names it has
     *
     * @param target the uuid of player target
     * @return if the player has alt account
     */
    @Deprecated
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
        Set<OfflineUser> alts = manager.getAlts(module, target);
        return alts.size() - 1;
    }

    /**
     * Check if the user can join the server
     *
     * @param uuid the uuid of the player
     * @param ip   the target ip
     * @param max  the maximum amount of accounts
     *             allowed per ip
     * @return if the amount of users is over the max or
     * the user is already saved
     */
    public final boolean canJoin(final UUID uuid, final InetAddress ip, final int max) {
        Set<OfflineUser> alts = manager.getAlts(module, ip, uuid);
        boolean available = false;
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

    /**
     * Check if the user has alts accounts by checking all alt
     * matching results and checking the amount of names it has
     *
     * @param target the uuid of player target
     * @param ip     the target ip
     * @return if the player has alt account
     */
    public final boolean hasAltAccounts(final UUID target, final InetAddress ip) {
        return getAltsAmount(target, ip) > 0;
    }

    /**
     * Get the amount of alts the player has
     *
     * @param target the uuid of player target
     * @param ip     the target ip
     * @return the amount of player alt accounts
     */
    public final int getAltsAmount(final UUID target, final InetAddress ip) {
        Set<OfflineUser> alts = manager.getAlts(module, ip, target);
        return alts.size() - 1;
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

    /**
     * Get ip storager manager
     * utilities
     */
    public interface manager {

        /**
         * Get player alt accounts
         *
         * @param module the LockLogin module to access
         *               api method
         * @param target the request target
         * @return the target alt accounts
         */
        static Set<OfflineUser> getAlts(final Module module, final UUID target) {
            Set<OfflineUser> users = new LinkedHashSet<>();

            if (ModuleLoader.manager.isLoaded(module)) {
                KarmaFile library = new KarmaFile(plugin, target.toString().replace("-", "").toLowerCase(), "data", "ips_library");

                Set<String> added_ids = new HashSet<>();

                //Check if the library exists, if not, it means the
                //user has never played in the server...
                if (library.exists()) {
                    List<String> ip_files = library.getStringList("DATA");

                    for (String ip_file : ip_files) {
                        KarmaFile ipFile = new KarmaFile(plugin, ip_file, "data", "ips_v5");

                        List<String> uuids = ipFile.getStringList("UUIDs");
                        for (String id : uuids) {
                            OfflineUser user = new OfflineUser(id, ipFile.getString(id, ""), true);
                            if (!user.exists())
                                user = new OfflineUser(id, "", false);

                            if (user.exists())
                                if (!added_ids.contains(id)) {
                                    added_ids.add(id);
                                    users.add(user);
                                }
                        }
                    }
                }
            }

            return users;
        }

        /**
         * Get player alt accounts
         *
         * @param module the LockLogin module to access
         *               api method
         * @param ip     the target ip
         * @param target the request target
         * @return the target alt accounts
         */
        static Set<OfflineUser> getAlts(final Module module, final InetAddress ip, final UUID target) {
            Set<OfflineUser> users = new LinkedHashSet<>();

            if (ModuleLoader.manager.isLoaded(module)) {
                KarmaFile library = new KarmaFile(plugin, target.toString().replace("-", "").toLowerCase(), "data", "ips_library");

                Set<String> added_ids = new HashSet<>();

                //Check if the library exists, if not, it means the
                //user has never played in the server...
                if (library.exists()) {
                    List<String> ip_files = library.getStringList("DATA");

                    for (String ip_file : ip_files) {
                        KarmaFile ipFile = new KarmaFile(plugin, ip_file, "data", "ips_v5");

                        List<String> uuids = ipFile.getStringList("UUIDs");
                        for (String id : uuids) {
                            OfflineUser user = new OfflineUser(id, ipFile.getString(id, ""), true);
                            if (!user.exists())
                                user = new OfflineUser(id, "", false);

                            if (user.exists())
                                if (!added_ids.contains(id)) {
                                    added_ids.add(id);
                                    users.add(user);
                                }
                        }
                    }
                } else {
                    String hashed_ip = new Codification2(ip.getHostName(), false).hash();

                    //Initialize reverse search ( ip file > each library > ip files )
                    KarmaFile mainIpFile = new KarmaFile(plugin, hashed_ip, "data", "ips_v5");
                    if (mainIpFile.exists()) {
                        List<String> libraries = mainIpFile.getStringList("LIBRARIES");

                        for (String libFile : libraries) {
                            library = new KarmaFile(plugin, libFile, "data", "ips_library");

                            List<String> libs = library.getStringList("DATA");
                            for (String lib : libs) {
                                KarmaFile ipFile = new KarmaFile(plugin, lib, "data", "ips_v5");

                                List<String> uuids = ipFile.getStringList("UUIDs");
                                for (String id : uuids) {
                                    OfflineUser user = new OfflineUser(id, ipFile.getString(id, ""), true);
                                    if (!user.exists())
                                        user = new OfflineUser(id, "", false);

                                    if (user.exists())
                                        if (!added_ids.contains(id)) {
                                            added_ids.add(id);
                                            users.add(user);
                                        }
                                }
                            }
                        }
                    }
                }
            }

            return users;
        }
    }
}