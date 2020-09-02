package ml.karmaconfigs.LockLogin.Spigot.API.Events;

import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
