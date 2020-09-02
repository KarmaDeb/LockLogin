package ml.karmaconfigs.LockLogin.Alerts;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.Platform;
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
public final class IMSGUtils {

    private static String lastMsg;

    private final GetWarningMSG wMSG = new GetWarningMSG();

    public final boolean AlertAvailable() {
        if (lastMsg != null) {
            return !lastMsg.equalsIgnoreCase(wMSG.GetMessage());
        }
        return true;
    }

    /**
     * Send the alert to the specified
     * platform
     *
     * @param platform The platform (Bungee | Spigot)
     */
    public final void SendAlert(Platform platform) {
        switch (platform) {
            case BUNGEE:
                LockLoginBungee.out.Message(wMSG.GetLevel() + wMSG.GetMessage());
                break;
            case SPIGOT:
                LockLoginSpigot.out.Message(wMSG.GetLevel() + wMSG.GetMessage());
                break;
        }
        lastMsg = wMSG.GetMessage();
    }
}
