package ml.karmaconfigs.lockloginsystem.shared.alerts;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public interface LockLoginAlerts {

    /**
     * Check if there's any alert available
     *
     * @return if there's a new alert
     */
    static boolean AlertAvailable() {
        return new IMSGUtils().AlertAvailable();
    }

    /**
     * Send the alert to the server console
     *
     */
    static void sendAlert() {
        new IMSGUtils().SendAlert();
    }
}
