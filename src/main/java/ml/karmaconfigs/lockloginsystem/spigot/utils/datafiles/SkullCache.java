package ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles;

import ml.karmaconfigs.api.spigot.KarmaFile;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class SkullCache implements LockLoginSpigot {

    private final KarmaFile sk_cache;

    public SkullCache(final String owner_name) {
        sk_cache = new KarmaFile(plugin, owner_name, "data", "skulls");

        if (!sk_cache.exists())
            sk_cache.create();
    }

    /**
     * Save the skull value
     *
     * @param value the skull value
     */
    public final void saveSkullValue(final String value) {
        if (!sk_cache.exists())
            sk_cache.create();

        Date today = new Date();
        SimpleDateFormat ll_format = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = ll_format.format(today);

        sk_cache.set("Value", value);
        sk_cache.set("Timestamp", timestamp);
    }

    /**
     * Get the cache skull value
     *
     * @return the cache skull value
     */
    @Nullable
    public final String getValue() {
        if (!sk_cache.exists())
            sk_cache.create();

        return sk_cache.getString("Value", null);
    }

    /**
     * Check if the skull needs to reload cache
     *
     * @return if the skull needs to reload cache
     */
    public final boolean needsCache() {
        if (!sk_cache.exists())
            sk_cache.create();

        if (sk_cache.isSet("Timestamp")) {
            Date today = new Date();
            SimpleDateFormat ll_format = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = ll_format.format(today);

            String stored_timestamp = sk_cache.getString("Timestamp", timestamp);
            try {
                Date way_back = ll_format.parse(stored_timestamp);

                return Math.round((today.getTime() - way_back.getTime()) / (double) 86400000) > 1;
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }
}
