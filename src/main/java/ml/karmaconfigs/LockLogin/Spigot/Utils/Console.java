package ml.karmaconfigs.LockLogin.Spigot.Utils;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.WarningLevel;

import java.util.HashSet;
import java.util.List;

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

public final class Console implements LockLoginSpigot {

    /**
     * Sends a message to the console
     *
     * @param message the message
     */
    public final void Message(String message) {
        plugin.getServer().getConsoleSender().sendMessage(StringUtils.toColor(message)
                .replace("{0}", name)
                .replace("{1}", version));
    }

    /**
     * Sends a list of messages to the console
     *
     * @param message the messages
     */
    public final void Message(List<String> message) {
        for (String str : message) {
            Message(str);
        }
    }

    /**
     * Sends a list of messages to the console
     *
     * @param messages the messages
     */
    public final void Message(HashSet<String> messages) {
        for (String str : messages) {
            Message(str);
        }
    }

    /**
     * Send an alert to the console
     *
     * @param message the alert message
     * @param level   the alert level
     */
    public final void Alert(String message, WarningLevel level) {
        switch (level) {
            case NONE:
                Message("&c[ &eLockLogin &c] &f( &aGOOD &f) &d" + message);
                break;
            case WARNING:
                Message("&c[ &eLockLogin &c] &f( &6WARNING &f) &e" + message);
                break;
            case ERROR:
                Message("&c[ &eLockLogin &c] &f( &4ERROR &f) &c" + message);
                break;
        }
    }
}
