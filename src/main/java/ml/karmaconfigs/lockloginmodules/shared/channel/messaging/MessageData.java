package ml.karmaconfigs.lockloginmodules.shared.channel.messaging;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class MessageData {

    private final Map<String, Object> values = new HashMap<>();

    /**
     * Add or replace existing data
     *
     * @param path the data path
     * @param value the data
     */
    public final void addData(final String path, final Object value) {
        values.put(path, value);
    }

    /**
     * Get the data
     *
     * @param path the data path
     * @return the data
     */
    @Nullable
    public final Object getData(final String path) {
        return values.getOrDefault(path, null);
    }
}
