package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.BungeeExecutorService;
import ml.karmaconfigs.lockloginsystem.spigot.SpigotExecutorService;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public interface PlatformUtils {

    static void Message(String message) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case SPIGOT:
                new SpigotExecutorService().message(message);
                break;
            case BUNGEE:
                new BungeeExecutorService().message(message);
                break;
            default:
                break;
        }
    }

    static void Alert(String message, Level level) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case SPIGOT:
                new SpigotExecutorService().message(message, level);
                break;
            case BUNGEE:
                new BungeeExecutorService().message(message, level);
                break;
            default:
                break;
        }
    }

    static void log(String info, Level level) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case SPIGOT:
                new SpigotExecutorService().log(level, info);
                break;
            case BUNGEE:
                new BungeeExecutorService().log(level, info);
                break;
            default:
                break;
        }
    }

    static void log(Throwable error, Level level) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case SPIGOT:
                new SpigotExecutorService().log(level, error);
                break;
            case BUNGEE:
                new BungeeExecutorService().log(level, error);
                break;
            default:
                break;
        }
    }

    static boolean isPremium() {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case SPIGOT:
                return new SpigotExecutorService().isPremium();
            case BUNGEE:
                return new BungeeExecutorService().isPremium();
            default:
                return false;
        }
    }
}
