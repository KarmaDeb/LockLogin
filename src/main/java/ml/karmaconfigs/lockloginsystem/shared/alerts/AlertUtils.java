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
public final class AlertUtils {

    private static String lastMsg;

    private final AlertSystem wMSG = new AlertSystem();

    public final boolean available() {
        if (lastMsg != null) {
            return !lastMsg.equalsIgnoreCase(wMSG.getMessage());
        }
        return true;
    }

    /**
     * Send the alert
     */
    public final void sendAlert() {
        PlatformUtils.send("( LLAS ) " + wMSG.getLevel() + wMSG.getMessage());
        lastMsg = wMSG.getMessage();
    }
}
