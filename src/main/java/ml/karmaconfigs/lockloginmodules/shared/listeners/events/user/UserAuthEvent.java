package ml.karmaconfigs.lockloginmodules.shared.listeners.events.user;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;
import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;

/**
 * This event is fired when an user auths.
 *
 * This event is fired when {@link ml.karmaconfigs.lockloginapi.bukkit.events.PlayerAuthEvent}
 * or {@link ml.karmaconfigs.lockloginapi.bungee.events.PlayerAuthEvent} is fired
 */
public final class UserAuthEvent extends Event {

    private boolean handled = false;

    private final AuthType auth_type;
    private final Object player;
    private final EventAuthResult auth_result;
    private final String auth_message;

    private final Object eventObj;

    /**
     * Initialize the player auth event
     *
     * @param _auth_type the auth type
     * @param _auth_result the auth result
     * @param _player the player
     * @param _auth_message the auth message
     * @param event the event in where this event is fired
     */
    public UserAuthEvent(final AuthType _auth_type, final EventAuthResult _auth_result, final Object _player, final String _auth_message, final Object event) {
        auth_type = _auth_type;
        auth_result = _auth_result;
        player = _player;
        auth_message = _auth_message;

        eventObj = event;
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
     * Get the auth type
     *
     * @return the auth type
     */
    public final AuthType getAuthType() {
        return auth_type;
    }

    /**
     * Get the auth result
     *
     * @return the auth result
     */
    public final EventAuthResult getAuthResult() {
        return auth_result;
    }

    /**
     * Get the auth message
     *
     * @return the auth message
     */
    public final String getAuthMessage() {
        return auth_message;
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

    /**
     * Get the event instance
     *
     * @return the event instance
     */
    @Override
    public Object getEvent() {
        return eventObj;
    }
}
