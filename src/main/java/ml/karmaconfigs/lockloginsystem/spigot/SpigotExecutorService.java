package ml.karmaconfigs.lockloginsystem.spigot;

import ml.karmaconfigs.api.bukkit.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;

/**
 GNU LESSER GENERAL PUBLIC LICENSE
 Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

 [This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */
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
