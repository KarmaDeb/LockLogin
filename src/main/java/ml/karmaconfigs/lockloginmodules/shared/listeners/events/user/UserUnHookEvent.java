package ml.karmaconfigs.lockloginmodules.shared.listeners.events.user;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;

/**
 * This event is fired when the plugin
 * unhooks a player, this event can happen
 * when the server is stopped, or when the plugin
 * is about to be updated.
 */
public final class UserUnHookEvent extends Event {

    private boolean handled = false;

    private final Object player;

    /**
     * Initialize event
     *
     * @param playerObject the player
     */
    public UserUnHookEvent(final Object playerObject) {
        player = playerObject;
    }

    /**
     * Set the event handle status
     *
     * @param status the handle status
     */
    @Override
    public void setHandled(boolean status) {
        handled = true;
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

    /**
     * Get the player
     *
     * @return the player
     */
    public final Object getPlayer() {
        return player;
    }
}
