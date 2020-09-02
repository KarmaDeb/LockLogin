package ml.karmaconfigs.LockLogin.Spigot.Utils;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Version.LockLoginVersion;

public interface CheckerSpigot extends LockLoginVersion, LockLoginSpigot, SpigotFiles {

    static boolean isOutdated() {
        return LockLoginVersion.versionID > LockLoginSpigot.versionID;
    }

    static void sendChangeLog() {
        if (config.ChangeLogs()) {
            out.Message(changeLog);
        }
    }
}
