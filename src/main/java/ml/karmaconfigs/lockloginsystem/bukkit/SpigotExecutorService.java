package ml.karmaconfigs.lockloginsystem.bukkit;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
import ml.karmaconfigs.lockloginsystem.bukkit.utils.files.SpigotFiles;

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
public final class SpigotExecutorService {

    /**
     * Send a console message
     *
     * @param message the message to send
     */
    public final void message(final String message) {
        Console.send(message);
    }

    /**
     * Send a console message with message
     * level
     *
     * @param message the message to send
     * @param level the message level
     */
    public final void message(final String message, final Level level) {
        Console.send(LockLoginSpigot.plugin, message, level);
    }

    /**
     * Log something into the plugin's logger
     *
     * @param level the log level
     * @param info the info to log
     */
    public final void log(final Level level, final String info) {
        LockLoginSpigot.logger.scheduleLog(level, info);
    }

    /**
     * Log an exception into the plugin's logger
     *
     * @param level the log level
     * @param error the exception to log
     */
    public final void log(final Level level, final Throwable error) {
        LockLoginSpigot.logger.scheduleLog(level, error);
    }

    /**
     * Check if the server is online mode
     *
     * @return if the server is in online mode
     */
    public final boolean isPremium() {
        return LockLoginSpigot.plugin.getServer().getOnlineMode();
    }

    /**
     * Get the server password encryption type
     *
     * @return the server password encryption
     */
    public final CryptType getPasswordType() {
        return SpigotFiles.config.passwordEncryption();
    }

    /**
     * Get the server pin encryption type
     *
     * @return the server pin encryption
     */
    public final CryptType getPinType() {
        return SpigotFiles.config.pinEncryption();
    }
}
