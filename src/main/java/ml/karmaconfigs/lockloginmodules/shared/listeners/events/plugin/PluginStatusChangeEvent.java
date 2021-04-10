package ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;

/**
 * This event is fired when the plugin
 * status changes, from {@link Status#LOAD} to {@link Status#UNLOAD},
 * {@link Status#RELOAD_START} to {@link Status#RELOAD_END}, or
 * {@link Status#UPDATE_START} to {@link Status#RELOAD_END}
 */
public final class PluginStatusChangeEvent extends Event {

    private boolean handled = false;
    private final Status status;

    /**
     * Initialize the event
     *
     * @param _status the plugin status
     */
    public PluginStatusChangeEvent(final Status _status) {
        status = _status;
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
