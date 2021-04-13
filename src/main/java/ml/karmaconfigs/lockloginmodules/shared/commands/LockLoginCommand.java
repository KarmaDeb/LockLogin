package ml.karmaconfigs.lockloginmodules.shared.commands;

import ml.karmaconfigs.lockloginmodules.Module;

import java.lang.reflect.Method;
import java.util.*;

/**
 * LockLogin command
 */
public final class LockLoginCommand {

    private final static Map<Module, Set<Command>> commands = new HashMap<>();

    /**
     * Register a command and link it to
     * the specified module
     *
     * @param owner the module owner
     * @param cmd the command class
     */
    public static void registerCommand(final Module owner, final Command cmd) {
        Set<Command> handlers = commands.getOrDefault(owner, new LinkedHashSet<>());
        handlers.add(cmd);

        commands.put(owner, handlers);
    }

    /**
     * Unregister all the commands of the specified
     * module
     *
     * @param module the module
     */
    public static void unregisterCommands(final Module module) {
        commands.put(module, new HashSet<>());
    }

    /**
     * Fire a command, so each module can handle it
     *
     * @param argument the command argument
     * @param sender the command sender
     * @param arguments the command sub arguments
     */
    public static void fireCommand(String argument, final Object sender, final String... arguments) {
        //LockLogin command identifier, is a $ symbol
        //and not a '/'
        if (argument.startsWith("$")) {
            argument = argument.replaceFirst("\\$", "");

            for (Module module : commands.keySet()) {
                Set<Command> handlers = commands.getOrDefault(module, new LinkedHashSet<>());

                for (Command handler : handlers) {
                    //Only call the event if the event class is instance of the
                    //listener class
                    List<String> valid_args = Arrays.asList(handler.validArguments());
                    if (valid_args.contains(argument)) {
                        try {
                            Method processCommandMethod = handler.getClass().getMethod("processCommand", String.class, Object.class, String[].class);
                            processCommandMethod.invoke(handler, argument, sender, arguments);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all the modules that have a command on it
     *
     * @return all the modules that have commands
     */
    public static Set<Module> getModules() {
        return commands.keySet();
    }

    /**
     * Get all the available commands
     *
     * @return all the available commands
     */
    public static Set<String> availableCommands() {
        Set<String> cmds = new LinkedHashSet<>();

        for (Module module : getModules()) {
            Set<Command> handlers = commands.getOrDefault(module, new LinkedHashSet<>());

            for (Command handler : handlers)
                cmds.addAll(Arrays.asList(handler.validArguments()));
        }

        return cmds;
    }

    /**
     * Get all the command data to enable
     * $help command
     *
     * @return all the command data
     */
    public static Set<CommandData> getData() {
        Set<CommandData> cmds = new LinkedHashSet<>();

        for (Module module : getModules()) {
            Set<Command> handlers = commands.getOrDefault(module, new LinkedHashSet<>());

            for (Command handler : handlers)
                cmds.add(new CommandData(handler));
        }

        return cmds;
    }

    /**
     * Check if the command is valid
     *
     * @param command the command
     * @return if the command is valid
     */
    public static boolean isValid(String command) {
        if (command.startsWith("$"))
            command = command.replaceFirst("\\$", "");

        return availableCommands().stream().anyMatch(command::matches);
    }
}

