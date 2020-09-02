package ml.karmaconfigs.LockLogin;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class IpData {

    private static HashMap<InetAddress, Integer> IPs = new HashMap<>();

    private final InetAddress ip;

    /**
     * Initialize the IP database
     *
     * @param Ip the IP
     */
    public IpData(InetAddress Ip) {
        this.ip = Ip;
    }

    /**
     * Add the IP to the hashmap, or
     * give it one more if it already
     * has
     */
    public final void addIP() {
        if (!IPs.containsKey(ip)) {
            IPs.put(ip, 1);
        } else {
            IPs.put(ip, getConnections() + 1);
        }
    }

    /**
     * Remove the IP from the hashmap
     * or remove the ip if the connections
     * - 1 will be 0
     */
    public final void delIP() {
        if (IPs.containsKey(ip)) {
            if (getConnections() - 1 != 0) {
                IPs.put(ip, getConnections() - 1);
            } else {
                IPs.remove(ip);
            }
        }
    }

    /**
     * Get the connections of that
     * ip
     *
     * @return an integer
     */
    public final int getConnections() {
        return IPs.getOrDefault(ip, 1);
    }

    /**
     * Fetch the ip data
     */
    public final void fetch(Platform platform) {
        HashMap<InetAddress, Integer> ips = new HashMap<>();
        switch (platform) {
            case ANY:
                try {
                    for(Player player : ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot.plugin.getServer().getOnlinePlayers()) {
                        ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                    }
                } catch (Throwable e) {
                    for(ProxiedPlayer player : ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee.plugin.getProxy().getPlayers()) {
                        ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                    }
                }
                break;
            case SPIGOT:
                for(Player player : ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot.plugin.getServer().getOnlinePlayers()) {
                    ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                }
                break;
            case BUNGEE:
                for(ProxiedPlayer player : ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee.plugin.getProxy().getPlayers()) {
                    ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                }
                break;
        }

        IPs = ips;
    }
}
