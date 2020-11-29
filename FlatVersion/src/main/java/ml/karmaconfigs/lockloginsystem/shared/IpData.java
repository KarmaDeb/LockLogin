package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
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
    public IpData(final Module loader, InetAddress Ip) {
        if (ModuleLoader.manager.isLoaded(loader)) {
            this.ip = Ip;
        } else {
            this.ip = null;
        }
    }

    /**
     * Initialize the IP database
     *
     * @param Ip the IP
     */
    public IpData(final ml.karmaconfigs.lockloginmodules.bungee.Module loader, InetAddress Ip) {
        if (ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader.manager.isLoaded(loader)) {
            this.ip = Ip;
        } else {
            this.ip = null;
        }
    }

    /**
     * Add the IP to the hashmap, or
     * give it one more if it already
     * has
     */
    public final void addIP() {
        if (ip != null) {
            if (!IPs.containsKey(ip)) {
                IPs.put(ip, 1);
            } else {
                IPs.put(ip, getConnections() + 1);
            }
        }
    }

    /**
     * Remove the IP from the hashmap
     * or remove the ip if the connections
     * - 1 will be 0
     */
    public final void delIP() {
        if (ip != null) {
            if (IPs.containsKey(ip)) {
                if (getConnections() - 1 != 0) {
                    IPs.put(ip, getConnections() - 1);
                } else {
                    IPs.remove(ip);
                }
            }
        }
    }

    /**
     * Get the connections of that
     * ip
     *
     * @return the amount of connections of that IP
     */
    public final int getConnections() {
        if (ip != null) {
            return IPs.getOrDefault(ip, 1);
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Fetch the ip data
     *
     * @param platform the platform ( BungeeCord / Spigot )
     */
    public final void fetch(Platform platform) {
        if (ip != null) {
            HashMap<InetAddress, Integer> ips = new HashMap<>();
            switch (platform) {
                case ANY:
                    try {
                        for (Player player : ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot.plugin.getServer().getOnlinePlayers()) {
                            ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                        }
                    } catch (Throwable e) {
                        for (ProxiedPlayer player : ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee.plugin.getProxy().getPlayers()) {
                            ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                        }
                    }
                    break;
                case SPIGOT:
                    for (Player player : ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot.plugin.getServer().getOnlinePlayers()) {
                        ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                    }
                    break;
                case BUNGEE:
                    for (ProxiedPlayer player : ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee.plugin.getProxy().getPlayers()) {
                        ips.put(player.getAddress().getAddress(), ips.getOrDefault(player.getAddress().getAddress(), 0) + 1);
                    }
                    break;
            }

            IPs = ips;
        }
    }
}
