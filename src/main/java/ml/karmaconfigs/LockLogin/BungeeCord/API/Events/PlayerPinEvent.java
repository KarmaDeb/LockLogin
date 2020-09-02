package ml.karmaconfigs.LockLogin.BungeeCord.API.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class PlayerPinEvent extends Event implements BungeeFiles {

    private final ProxiedPlayer player;
    private final boolean isCorrect;

    /**
     * Initialize the player verify event
     *
     * @param player the player
     * @param status the pin status
     */
    public PlayerPinEvent(ProxiedPlayer player, boolean status) {
        this.player = player;
        this.isCorrect = status;
    }

    /**
     * Get the event player
     *
     * @return a Proxied player
     */
    public final ProxiedPlayer getPlayer() {
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
}
