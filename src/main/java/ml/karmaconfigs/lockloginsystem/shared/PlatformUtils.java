package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.BungeeExecutorService;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
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

    static void send(String message) {
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

    static void send(String message, Level level) {
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

    static CryptType getEncryption(final CryptTarget target) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case SPIGOT:
                SpigotExecutorService spigot = new SpigotExecutorService();

                switch (target) {
                    case PASSWORD:
                        return spigot.getPasswordType();
                    case PIN:
                        return spigot.getPinType();
                    default:
                        return CryptType.UNKNOWN;
                }
            case BUNGEE:
                BungeeExecutorService bungee = new BungeeExecutorService();

                switch (target) {
                    case PASSWORD:
                        return bungee.getPasswordType();
                    case PIN:
                        return bungee.getPinType();
                    default:
                        return CryptType.UNKNOWN;
                }
            default:
                return CryptType.UNKNOWN;
        }
    }

    enum CryptTarget {
        PASSWORD,PIN
    }
}
