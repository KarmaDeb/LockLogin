package ml.karmaconfigs.lockloginmodules.shared.listeners.events.user;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;

/**
 * This event is fired when a player joins
 * the server at the eyes of the plugin. In
 * bukkit, this event is fired with {@link org.bukkit.event.player.PlayerLoginEvent},
 * and with {@link net.md_5.bungee.api.event.ServerConnectEvent} in BungeeCord
 */
public final class UserJoinEvent extends Event {

    private final Object player;

    private boolean handled = false;

    /**
     * Initialize event
     *
     * @param player the player
     */
    public UserJoinEvent(final Object player) {
        this.player = player;
    }

    /**
     * Get the event player
     *
     * @return the event player
     */
    public final Object getPlayer() {
        return player;
    }

    /**
     * Set the event handle status
     *
     * @param status the handle status
     */
    @Override
    public void setHandled(boolean status) {
        handled = status;
    }

    /**
     * Check if the event has been handled
     *
     * @return if the event has been handled
     */
    @Override
    public boolean isHandled() {
        return handled;
    }
}
