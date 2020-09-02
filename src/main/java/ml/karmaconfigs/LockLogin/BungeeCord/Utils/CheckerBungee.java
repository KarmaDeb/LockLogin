package ml.karmaconfigs.LockLogin.BungeeCord.Utils;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.Version.LockLoginVersion;

public interface CheckerBungee extends LockLoginVersion, LockLoginBungee, BungeeFiles {

    static boolean isOutdated() {
        return LockLoginVersion.versionID > LockLoginBungee.versionID;
    }

    static void sendChangeLog() {
        if (config.ChangeLogs()) {
            out.Message(changeLog);
        }
    }
}
