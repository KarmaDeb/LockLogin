package ml.karmaconfigs.LockLogin.Alerts;

import ml.karmaconfigs.LockLogin.Platform;

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

    static boolean AlertAvailable() {
        return new IMSGUtils().AlertAvailable();
    }

    static void sendAlert(Platform platform) {
        new IMSGUtils().SendAlert(platform);
    }
}
