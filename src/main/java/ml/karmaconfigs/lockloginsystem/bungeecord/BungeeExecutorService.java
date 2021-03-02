package ml.karmaconfigs.lockloginsystem.bungeecord;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;

public final class BungeeExecutorService {

    public final void message(final String message) {
        Console.send(message);
    }

    public final void message(final String message, final Level level) {
        Console.send(LockLoginBungee.plugin, message, level);
    }

    public final void log(final Level level, final String info) {
        LockLoginBungee.logger.scheduleLog(level, info);
    }

    public final void log(final Level level, final Throwable error) {
        LockLoginBungee.logger.scheduleLog(level, error);
    }

    public final boolean isPremium() {
        return LockLoginBungee.plugin.getProxy().getConfig().isOnlineMode();
    }

    public final CryptType getPasswordType() {
        return BungeeFiles.config.passwordEncryption();
    }

    public final CryptType getPinType() {
        return BungeeFiles.config.pinEncryption();
    }
}
