package ml.karmaconfigs.lockloginmodules.shared.channel.messaging;

import ml.karmaconfigs.lockloginmodules.shared.channel.channeling.Channel;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * LockLogin message
 */
public final class Message {

    private final Channel sender;
    private final MessageData data;

    /**
     * Initialize the message
     *
     * @param channel the channel who sents the message
     * @param message the message to send
     */
    public Message(final Channel channel, final MessageData message) {
        sender = channel;
        data = message;
    }

    /**
     * Get the channel sender
     *
     * @return the channel sender
     */
    public final Channel getSender() {
        return sender;
    }

    /**
     * Get a data
     *
     * @param path the data path
     * @return the data
     */
    public final Object getData(final String path) {
        return data.getData(path);
    }

    /**
     * Get all the message paths
     *
     * @return the message paths
     */
    public final Set<String> getPaths() {
        try {
            Field values = data.getClass().getDeclaredField("values");
            values.setAccessible(true);

            Object obj = values.get(null);

            if (obj instanceof Map) {
                Map<?, ?> mapSet = (Map<?, ?>) obj;

                Set<String> valueSet = new LinkedHashSet<>();
                for (Object key : mapSet.keySet())
                    valueSet.add(key.toString());

                return valueSet;
            }

            return Collections.emptySet();
        } catch (Throwable ex) {
            return Collections.emptySet();
        }
    }
}
