package ml.karmaconfigs.LockLogin.BungeeCord.Utils;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashSet;
import java.util.List;

public final class Console implements LockLoginBungee {

    /**
     * Sends a message to the console
     *
     * @param message the message
     */
    public final void Message(String message) {
        plugin.getProxy().getConsole().sendMessage(
                TextComponent.fromLegacyText(StringUtils.toColor(message.replace("{0}", name).replace("{1}", version))));
    }

    /**
     * Sends a list message to the console
     *
     * @param messages the messages
     */
    public final void Message(List<String> messages) {
        for (String str : messages) {
            Message(str);
        }
    }

    /**
     * Sends a hashset of messages to the console
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
