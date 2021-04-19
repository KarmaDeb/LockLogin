package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bukkit.SpigotExecutorService;
import ml.karmaconfigs.lockloginsystem.bungee.BungeeExecutorService;
import ml.karmaconfigs.lockloginsystem.shared.account.AccountManager;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;

import java.lang.reflect.Constructor;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public class PlatformUtils {

    private static Class<? extends AccountManager> manager = null;

    public static void setAccountManager(final Class<? extends AccountManager> manager) {
        PlatformUtils.manager = manager;
    }

    public static void send(String message) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
                new SpigotExecutorService().message(message);
                break;
            case BUNGEE:
                new BungeeExecutorService().message(message);
                break;
            default:
                break;
        }
    }

    public static void send(String message, Level level) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
                new SpigotExecutorService().message(message, level);
                break;
            case BUNGEE:
                new BungeeExecutorService().message(message, level);
                break;
            default:
                break;
        }
    }

    public static void log(String info, Level level) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
                new SpigotExecutorService().log(level, info);
                break;
            case BUNGEE:
                new BungeeExecutorService().log(level, info);
                break;
            default:
                break;
        }
    }

    public static void log(Throwable error, Level level) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
                new SpigotExecutorService().log(level, error);
                break;
            case BUNGEE:
                new BungeeExecutorService().log(level, error);
                break;
            default:
                break;
        }
    }

    public static boolean isPremium() {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
                return new SpigotExecutorService().isPremium();
            case BUNGEE:
                return new BungeeExecutorService().isPremium();
            default:
                return false;
        }
    }

    public static boolean accountManagerValid() {
        return manager != null;
    }

    public static boolean isNativeManager() {
        if (accountManagerValid())
            return manager.getName().contains("ml.karmaconfigs.lockloginsystem");

        return false;
    }

    public static CryptType getEncryption(final CryptTarget target) {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
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

    public static AccountManager getManager(final Class<?>[] paramTypes, final Object... parameters) {
        try {
            if (parameters.length > 0) {
                Constructor<? extends AccountManager> constructor = manager.getConstructor(paramTypes);
                return constructor.newInstance(parameters);
            } else {
                return manager.getDeclaredConstructor().newInstance();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String modulePrefix() {
        CurrentPlatform current = new CurrentPlatform();
        switch (current.getRunning()) {
            case BUKKIT:
                SpigotExecutorService spigot = new SpigotExecutorService();
                return String.valueOf(spigot.getCommandPrefix().charAt(0));
            case BUNGEE:
                BungeeExecutorService bungee = new BungeeExecutorService();
                return String.valueOf(bungee.getCommandPrefix().charAt(0));
            default:
                return "$";
        }
    }

    /**
     * All platform available
     * encryption target
     */
    public enum CryptTarget {
        /**
         * LockLogin password encryption
         */
        PASSWORD,
        /**
         * LockLogin pin encryption
         */
        PIN
    }
}
