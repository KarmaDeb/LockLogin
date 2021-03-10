package ml.karmaconfigs.lockloginsystem.spigot.api.events;

import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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

public class PlayerRegisterEvent extends Event implements SpigotFiles {

    private static final HandlerList HANDLERS = new HandlerList();

    private static String registerMessage;
    private final Player player;

    /**
     * Initialize the player verify event
     *
     * @param player the player
     */
    public PlayerRegisterEvent(Player player) {
        this.player = player;
        registerMessage = messages.registered();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the event player
     *
     * @return the player event
     */
    public final Player getPlayer() {
        return player;
    }

    /**
     * Get the plugin register message
     *
     * @return a String
     */
    public final String getRegisterMessage() {
        return registerMessage;
    }

    /**
     * Set the login message
     *
     * @param message the message
     */
    public final void setRegisterMessage(String message) {
        if (!message.isEmpty()) {
            registerMessage = message.replace("{player}", player.getName());
        }
    }

    @Override
    public final HandlerList getHandlers() {
        return HANDLERS;
    }
}
