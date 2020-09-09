package ml.karmaconfigs.LockLogin.Spigot.API.Events;

import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
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

public class PlayerPinEvent extends Event implements SpigotFiles {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final boolean isCorrect;

    /**
     * Initialize the player verify event
     *
     * @param player the player
     * @param status the pin status
     */
    public PlayerPinEvent(Player player, boolean status) {
        this.player = player;
        this.isCorrect = status;
    }

    /**
     * Get the event player
     *
     * @return a player
     */
    public final Player getPlayer() {
        return player;
    }

    /**
     * Get the pin status
     *
     * @return a boolean
     */
    public final boolean pinIsOk() {
        return isCorrect;
    }

    @Override
    public final HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
