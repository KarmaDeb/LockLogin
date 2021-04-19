package ml.karmaconfigs.lockloginsystem.bungee;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class BungeeExecutorService {

    /**
     * Send a message to BungeeCord console
     *
     * @param message the message to send
     */
    public final void message(final String message) {
        Console.send(message);
    }

    /**
     * Send a message level to BungeeCord console
     *
     * @param message the message to send
     * @param level   the message level
     */
    public final void message(final String message, final Level level) {
        Console.send(LockLoginBungee.plugin, message, level);
    }

    /**
     * Log something into BungeeCord logger
     *
     * @param level the log level
     * @param info  the log info
     */
    public final void log(final Level level, final String info) {
        LockLoginBungee.logger.scheduleLog(level, info);
    }

    /**
     * Log an exception into BungeeCord logger
     *
     * @param level the log level
     * @param error the log error
     */
    public final void log(final Level level, final Throwable error) {
        LockLoginBungee.logger.scheduleLog(level, error);
    }

    /**
     * Check if BungeeCord is set to online mode
     *
     * @return if the server is online mode
     */
    public final boolean isPremium() {
        return LockLoginBungee.plugin.getProxy().getConfig().isOnlineMode();
    }

    /**
     * Get BungeeCord password encryption type
     *
     * @return the server password encryption method
     */
    public final CryptType getPasswordType() {
        return BungeeFiles.config.passwordEncryption();
    }

    /**
     * Get BungeeCord pin encryption type
     *
     * @return the server pin encryption type
     */
    public final CryptType getPinType() {
        return BungeeFiles.config.pinEncryption();
    }

    public final String getCommandPrefix() {
        return BungeeFiles.config.getCommandPrefix();
    }
}
