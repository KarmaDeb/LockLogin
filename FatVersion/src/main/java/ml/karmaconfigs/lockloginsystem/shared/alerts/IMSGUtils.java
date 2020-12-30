package ml.karmaconfigs.lockloginsystem.shared.alerts;

import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;

/**
 * Private GSA code
 * <p>
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
     * Send the alert
     */
    public final void SendAlert() {
        PlatformUtils.Message("( LLAS ) " + wMSG.GetLevel() + wMSG.GetMessage());
        lastMsg = wMSG.GetMessage();
    }
}
