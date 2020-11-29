package ml.karmaconfigs.lockloginsystem.shared.ipstorage;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class BFSystem {

    private final InetAddress ip;

    private final static HashMap<InetAddress, Integer> data = new HashMap<>();
    private final static HashMap<InetAddress, Long> block_time = new HashMap<>();
    private final static HashSet<InetAddress> blocked = new HashSet<>();

    /**
     * Initialize the brute-force system
     *
     * @param _ip the ip that tried
     *            to login
     */
    public BFSystem(final InetAddress _ip) {
        ip = _ip;
    }

    /**
     * Add a fail to the ip tries
     */
    public final void fail() {
        data.put(ip, data.getOrDefault(ip, 0) + 1);
    }

    /**
     * Remove ip tries
     */
    public final void success() {
        data.remove(ip);
    }

    /**
     * Block the current ip
     */
    public final void block() {
        blocked.add(ip);
    }

    /**
     * Unblock the current IP
     */
    public final void unlock() {
        blocked.remove(ip);
    }

    /**
     * Update the IP block time left
     *
     * @param new_block_left the new amount of time
     *                       the IP has to wait to
     *                       be unblocked
     */
    public final void updateTime(final long new_block_left) {
        block_time.put(ip, new_block_left);
    }

    /**
     * Check if the ip is blocked
     *
     * @return if the ip is blocked
     */
    public final boolean isBlocked() {
        return blocked.contains(ip);
    }

    /**
     * Get the amount of time the IP has
     * to wait before being unblocked
     * again
     *
     * @return the amount of block time left
     * of the IP
     */
    public final long getBlockLeft() {
        return block_time.getOrDefault(ip, 0L);
    }

    /**
     * Get the tries the IP has
     *
     * @return the amount of login tries
     * that IP has
     */
    public final int getTries() {
        return data.getOrDefault(ip, 0);
    }
}
