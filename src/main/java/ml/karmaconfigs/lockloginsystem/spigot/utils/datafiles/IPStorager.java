package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.api.spigot.KarmaFile;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications.Codification2;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public final class IPStorager implements LockLoginSpigot {

    private final static KarmaFile ip_data = new KarmaFile(plugin, "ips_v3.lldb", "data");
    private final static KarmaFile user_ips = new KarmaFile(plugin, "ips_v3_users.lldb", "userdata");

    private final static HashSet<String> hashed_ips = new HashSet<>();

    private final Module module;

    private final InetAddress ip;

    /**
     * Initialize the ip storager system
     *
     * @param module  the module is calling the API
     * @param address the ip address
     */
    public IPStorager(final Module module, final InetAddress address) throws UnknownHostException {
        if (isValid(address) && ModuleLoader.manager.isLoaded(module)) {
            this.module = module;
            if (hashed_ips.isEmpty())
                loadIPs();

            ip = address;

            if (!ip_data.exists())
                ip_data.create();

            String hashed_ip = new Codification2(ip.getHostName(), false).hash();
            hashed_ips.add(hashed_ip);

            ip_data.set("IPs", new ArrayList<>(hashed_ips));
        } else {
            throw new UnknownHostException();
        }
    }

    /**
     * Load the stored ips in file
     */
    private static void loadIPs() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> storage = ip_data.getStringList("IPs");
            if (storage == null)
                storage = new ArrayList<>();

            hashed_ips.addAll(storage);
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
            String hashed_ip = new Codification2(ip.getHostName(), false).hash();

            List<String> assigned = ip_data.getStringList(hashed_ip);
            if (assigned == null)
                assigned = new ArrayList<>();

            if (!assigned.contains(uuid.toString())) {
                assigned.add(uuid.toString());

                ip_data.set(hashed_ip, assigned);
            }
        });
    }

    /**
     * Save the player last IP
     *
     * @param uuid the player uuid
     */
    public final void saveLastIP(final UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String hashed_ip = new Codification2(ip.getHostName(), false).hash();

            user_ips.set(uuid.toString(), hashed_ip);
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
        boolean available = false;
        String hashed_ip = new Codification2(ip.getHostName(), false).hash();
        HashSet<String> alts = new HashSet<>();
        for (String ip : hashed_ips) {
            List<String> assigned = ip_data.getStringList(ip);
            if (assigned == null)
                assigned = new ArrayList<>();

            if (ip.equals(hashed_ip)) {
                alts.addAll(assigned);
                available = assigned.contains(uuid.toString());
            } else {
                if (assigned.contains(uuid.toString())) {
                    available = true;
                    alts.addAll(assigned);
                }
            }
        }

        int count = alts.size();
        return available || count < max;
    }

    /**
     * Check if the user has alts accounts by checking all alt
     * matching results and checking the amount of names it has
     *
     * @param target the uuid of player target
     * @return if the player has alt account
     */
    public final boolean hasAltAccounts(final UUID target) {
        HashSet<OfflineUser> alts = manager.getAlts(module, target);

        HashSet<String> names = new HashSet<>();
        for (OfflineUser user : alts) {
            names.add(user.getName());
        }

        return names.size() > 0;
    }

    /**
     * Check if the player IP is the same as
     * his old IP
     *
     * @param uuid the player UUID
     * @return if the player IP is the same as last one
     */
    public final boolean differentIP(final UUID uuid) {
        String hashed_ip = new Codification2(ip.getHostName(), false).hash();
        String stored_ip = user_ips.getString(uuid.toString(), null);

        if (stored_ip != null)
            return !stored_ip.equals(hashed_ip);
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
        HashSet<OfflineUser> alts = manager.getAlts(module, target);

        return alts.size();
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

        static HashSet<OfflineUser> getAlts(final Module module, final UUID target) {
            if (ModuleLoader.manager.isLoaded(module)) {
                if (hashed_ips.isEmpty())
                    loadIPs();

                List<String> alts = new ArrayList<>();
                for (String ip : hashed_ips) {
                    List<String> assigned = ip_data.getStringList(ip);
                    if (assigned == null)
                        assigned = new ArrayList<>();

                    if (assigned.contains(target.toString())) {
                        for (String assign : assigned) {
                            if (!alts.contains(assign.toLowerCase()))
                                alts.add(assign);
                        }
                    }
                }

                HashSet<OfflineUser> offline = new HashSet<>();
                for (String id : alts) {
                    OfflineUser player = new OfflineUser(UUID.fromString(id));
                    offline.add(player);
                }

                return offline;
            }
            return new HashSet<>();
        }
    }
}
