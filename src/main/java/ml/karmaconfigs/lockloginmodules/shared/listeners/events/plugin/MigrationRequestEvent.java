package ml.karmaconfigs.lockloginmodules.shared.listeners.events.plugin;

import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;

/**
 * This event is fired when a player
 * or the console types migrate command, with
 * an invalid argument, for example /locklogin migrate Hello.
 *
 * So if a module has a custom migration method, it can be
 * handled by it using this event.
 */
public final class MigrationRequestEvent extends Event {

    private boolean handled = false;

    private final String arg;
    private final String[] parameters;
    private final Object commandSender;

    /**
     * Initialize event
     *
     * @param argument the command argument
     * @param params the command parameters
     * @param sender the command sender
     */
    public MigrationRequestEvent(final String argument, final String[] params, final Object sender) {
        arg = argument;
        parameters = params;
        commandSender = sender;
    }

    /**
     * Get the command event argument
     *
     * @return the argument
     */
    public final String getArgument() {
        return arg;
    }

    /**
     * Get the command event parameters
     *
     * @return the command parameters
     */
    public final String[] getParameters() {
        return parameters;
    }

    /**
     * Get the command sender
     *
     * @return the command sender
     */
    public final Object getSender() {
        return commandSender;
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
