package ml.karmaconfigs.lockloginmodules.shared.channel.channeling;

public final class ChannelPair {

    private Channel first;
    private Channel second;

    public final void addChannel(final Channel channel) {
        if (first == null)
            first = channel;
        else
            if (second == null)
                second = channel;
    }

    public final Channel getFirst() {
        return first;
    }

    public final Channel getSecond() {
        return second;
    }
}
