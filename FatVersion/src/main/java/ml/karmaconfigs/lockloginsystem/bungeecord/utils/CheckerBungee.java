package ml.karmaconfigs.lockloginsystem.bungeecord.utils;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.shared.version.LockLoginVersion;

/*
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

public interface CheckerBungee extends LockLoginVersion, LockLoginBungee, BungeeFiles {

    static boolean isOutdated() {
        return LockLoginVersion.versionID > LockLoginBungee.versionID;
    }

    static void sendChangeLog() {
        Console.send(changeLog);
    }
}
