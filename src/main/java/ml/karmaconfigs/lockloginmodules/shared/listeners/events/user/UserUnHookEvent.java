package ml.karmaconfigs.lockloginmodules.shared.listeners.events.user;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when the plugin
 * unhooks a player, this event can happen
 * when the server is stopped, or when the plugin
 * is about to be updated.
 */
public final class UserUnHookEvent extends Event {

    private boolean handled = false;

    private final Object player;
    private final Object eventObj;

    /**
     * Initialize event
     *
     * @param playerObject the player
     * @param event the event in where this event is fired
     */
    public UserUnHookEvent(final Object playerObject, final Object event) {
        player = playerObject;
        eventObj = event;
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

    /**
     * Get the event instance
     *
     * @return the event instance
     */
    @Override
    public @Nullable Object getEvent() {
        return eventObj;
    }
}
