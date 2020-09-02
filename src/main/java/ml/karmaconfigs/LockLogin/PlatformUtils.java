package ml.karmaconfigs.LockLogin;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public interface PlatformUtils {

    static void Message(String message) {
        try {
            LockLoginSpigot.out.Message(message);
        } catch (NoClassDefFoundError ex) {
            LockLoginBungee.out.Message(message);
        }
    }

    static void Alert(String message, WarningLevel level) {
        try {
            LockLoginSpigot.out.Alert(message, level);
        } catch (NoClassDefFoundError ex) {
            LockLoginBungee.out.Alert(message, level);
        }
    }

    static boolean isPremium() {
       try {
           return LockLoginSpigot.plugin.getServer().getOnlineMode();
       } catch (NoClassDefFoundError ex) {
           return LockLoginBungee.plugin.getProxy().getConfig().isOnlineMode();
       }
    }
}
