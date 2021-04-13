package ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when the plugin
 * status changes, from {@link Status#LOAD} to {@link Status#UNLOAD},
 * {@link Status#RELOAD_START} to {@link Status#RELOAD_END}, or
 * {@link Status#UPDATE_START} to {@link Status#RELOAD_END}
 */
public final class PluginStatusChangeEvent extends Event {

    private boolean handled = false;
    private final Status status;

    private final Object eventObj;

    /**
     * Initialize the event
     *
     * @param _status the plugin status
     * @param event the event in where this event is fired
     */
    public PluginStatusChangeEvent(final Status _status, final Object event) {
        status = _status;
        eventObj = event;
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
    public @Nullable Object getEvent() {
        return null;
    }

    /**
     * Get the plugin status
     *
     * @return the plugin status
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * Available plugin status
     */
    public enum Status {
        /**
         * Plugin loading status
         */
        LOAD,

        /**
         * Plugin unloading status
         */
        UNLOAD,

        /**
         * Plugin starting reload status
         */
        RELOAD_START,

        /**
         * Plugin finishing reload status
         */
        RELOAD_END,

        /**
         * Plugin start update status
         */
        UPDATE_START,

        /**
         * Plugin finish update status
         */
        UPDATE_END
    }
}
