package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.api.spigot.KarmaFile;
import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications.Codification2;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import org.bukkit.OfflinePlayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public final class IPStorager implements LockLoginSpigot {

    private final static KarmaFile ip_data = new KarmaFile(plugin, "ips_v3.lldb", "data");

    private final InetAddress ip;

    private final static HashSet<String> hashed_ips = new HashSet<>();

    /**
     * Initialize the ip storager system
     *
     * @param address the ip address
     */
    public IPStorager(final Module module, final InetAddress address) throws UnknownHostException {
        if (isValid(address) && ModuleLoader.manager.isLoaded(module)) {
            if (hashed_ips.isEmpty())
                loadIPs();

            ip = address;

            if (!ip_data.exists())
                ip_data.create();

            String hashed_ip = new Codification2(ip.getHostName(), false).hash();
            hashed_ips.add(hashed_ip);

            ip_data.set("IPs", hashed_ips);
        } else {
            throw new UnknownHostException();
        }
    }

    /**
     * Load the stored ips in file
     */
    public final void loadIPs() {
        List<String> storage = ip_data.getStringList("IPs");
        if (storage == null)
            storage = new ArrayList<>();

        hashed_ips.addAll(storage);
    }

    /**
     * Add user name to the list of players
     * assigned to the ip
     *
     * @param uuid the the player uuid
     */
    public final void save(final UUID uuid) {
        String hashed_ip = new Codification2(ip.getHostName(), false).hash();

        List<String> assigned = ip_data.getStringList(hashed_ip);
        if (assigned == null)
            assigned = new ArrayList<>();

        if (!assigned.contains(uuid.toString())) {
            assigned.add(uuid.toString());

            ip_data.set(hashed_ip, assigned);
        }
    }

    /**
     * Check if the user can join the server
     *
     * @param uuid the uuid of the player
     * @param max the maximum amount of accounts
     *            allowed per ip
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
                available = true;
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
     * Get the alt accounts of the specified UUID
     *
     * @param uuid the uuid of the player
     * @return all the matching IP accounts
     * of the UUID
     */
    public final HashSet<OfflinePlayer> getAlts(final UUID uuid) {
        String hashed_ip = new Codification2(ip.getHostName(), false).hash();
        HashSet<String> alts = new HashSet<>();
        for (String ip : hashed_ips) {
            List<String> assigned = ip_data.getStringList(ip);
            if (assigned == null)
                assigned = new ArrayList<>();

            if (ip.equals(hashed_ip)) {
                alts.addAll(assigned);
            } else {
                if (assigned.contains(uuid.toString())) {
                    alts.addAll(assigned);
                }
            }
        }

        HashSet<OfflinePlayer> offline = new HashSet<>();
        for (String id : alts) {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(id));
            offline.add(player);
        }

        return offline;
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

        static HashSet<OfflinePlayer> getAlts(final Module module, final UUID target) {
            if (ModuleLoader.manager.isLoaded(module)) {
                HashSet<String> alts = new HashSet<>();
                for (String ip : hashed_ips) {
                    List<String> assigned = ip_data.getStringList(ip);
                    if (assigned == null)
                        assigned = new ArrayList<>();

                    if (assigned.contains(target.toString())) {
                        alts.addAll(assigned);
                    }
                }

                HashSet<OfflinePlayer> offline = new HashSet<>();
                for (String id : alts) {
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(id));
                    offline.add(player);
                }

                return offline;
            }
            return new HashSet<>();
        }
    }
}
