package ml.karmaconfigs.LockLogin.BungeeCord.API.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

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
