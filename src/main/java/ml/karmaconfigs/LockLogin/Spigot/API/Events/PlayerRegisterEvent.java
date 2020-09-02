package ml.karmaconfigs.LockLogin.Spigot.API.Events;

import ml.karmaconfigs.LockLogin.Spigot.API.PlayerAPI;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRegisterEvent extends Event implements SpigotFiles {

    private static final HandlerList HANDLERS = new HandlerList();

    private static String registerMessage;
    private final Player player;

    /**
     * Initialize the player verify event
     *
     * @param player   the player
     */
    public PlayerRegisterEvent(Player player) {
        this.player = player;
        registerMessage = messages.Registered();
    }

    /**
     * Get the event player
     *
     * @return a Proxied player
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

    /**
     * Get the player API
     *
     * @return a PlayerAPI
     */
    public final PlayerAPI getAPI() {
        return new PlayerAPI(player);
    }

    @Override
    public final HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
