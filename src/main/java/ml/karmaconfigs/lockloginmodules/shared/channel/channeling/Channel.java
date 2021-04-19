package ml.karmaconfigs.lockloginmodules.shared.channel.channeling;

import ml.karmaconfigs.lockloginmodules.shared.channel.LockLoginChannels;
import ml.karmaconfigs.lockloginmodules.shared.channel.messaging.Message;
import ml.karmaconfigs.lockloginmodules.shared.channel.messaging.MessageData;

/**
 * LockLogin message channel
 */
public abstract class Channel {

    private final static ChannelKey key = null;

    /**
     * Listen for when a message is received
     *
     * @param message the message
     */
    public abstract void onMessageReceive(final Message message);

    /**
     * Send a message
     *
     * @param message the message to send
     */
    public final boolean sendMessage(final MessageData message) {
        return LockLoginChannels.sendMessage(this, message);
    }
}
