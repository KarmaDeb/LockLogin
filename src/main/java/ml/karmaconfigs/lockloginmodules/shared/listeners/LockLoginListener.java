package ml.karmaconfigs.lockloginmodules.shared.listeners;

import ml.karmaconfigs.lockloginmodules.Module;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.AdvancedEventHandler;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.util.Event;

import java.lang.reflect.Method;
import java.util.*;

/**
 * LockLogin listener
 */
public final class LockLoginListener {

    private final static Map<Module, Set<AdvancedEventHandler>> listeners = new HashMap<>();
    private final static Map<Module, Set<Method>> unregistered = new HashMap<>();

    /**
     * Register a listener and link it to
     * the specified module
     *
     * @param owner the module owner
     * @param handler the event handler class
     */
    public static void registerListener(final Module owner, final AdvancedEventHandler handler) {
        Set<AdvancedEventHandler> handlers = listeners.getOrDefault(owner, new LinkedHashSet<>());
        handlers.add(handler);

        listeners.put(owner, handlers);
    }

    /**
     * Unregister a listener from the specified owner
     *
     * @param owner the module owner
     * @param event the event to ignore
     */
    public static void unregisterListener(final Module owner, final Event event) {
        Set<AdvancedEventHandler> handlers = listeners.getOrDefault(owner, new LinkedHashSet<>());
        Set<Method> disabled = unregistered.getOrDefault(owner, new LinkedHashSet<>());
        for (AdvancedEventHandler handler : handlers) {

            Method[] methods = handler.getClass().getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                    disabled.add(method);
                }
            }
        }

        unregistered.put(owner, disabled);
    }

    /**
     * Unregister all the listeners of the specified
     * module
     *
     * @param module the module
     */
    public static void unregisterListeners(final Module module) {
        listeners.put(module, new HashSet<>());
    }

    /**
     * Call an event, so each module can handle it
     *
     * @param event the event to call
     */
    public static void callEvent(final Event event) {
        for (Module module : listeners.keySet()) {
            Set<AdvancedEventHandler> handlers = listeners.getOrDefault(module, new LinkedHashSet<>());

            for (AdvancedEventHandler handler : handlers) {
                //Only call the event if the event class is instance of the
                //listener class
                Method[] methods = handler.getClass().getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(LockLoginEventHandler.class)) {
                        if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                            try {
                                method.invoke(handler, event);
                            } catch (Throwable ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all the modules that have a listener on it
     *
     * @return all the modules that have listeners
     */
    public static Set<Module> getModules() {
        return listeners.keySet();
    }
}
