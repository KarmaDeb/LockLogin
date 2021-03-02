package ml.karmaconfigs.lockloginsystem.spigot;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;

public final class SpigotExecutorService {

    public final void message(final String message) {
        Console.send(message);
    }

    public final void message(final String message, final Level level) {
        Console.send(LockLoginSpigot.plugin, message, level);
    }

    public final void log(final Level level, final String info) {
        LockLoginSpigot.logger.scheduleLog(level, info);
    }

    public final void log(final Level level, final Throwable error) {
        LockLoginSpigot.logger.scheduleLog(level, error);
    }

    public final boolean isPremium() {
        return LockLoginSpigot.plugin.getServer().getOnlineMode();
    }

    public final CryptType getPasswordType() {
        return SpigotFiles.config.passwordEncryption();
    }

    public final CryptType getPinType() {
        return SpigotFiles.config.pinEncryption();
    }
}
