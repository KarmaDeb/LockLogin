package ml.karmaconfigs.LockLogin.BungeeCord.API.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.API.PlayerAPI;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
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

public class PlayerVerifyEvent extends Event implements Cancellable, BungeeFiles {

    private static String loginMessage;
    private static String cancelMessage;
    private final ProxiedPlayer player;
    private boolean isCancelled = false;

    /**
     * Initialize the player verify event
     *
     * @param player   the player
     */
    public PlayerVerifyEvent(ProxiedPlayer player) {
        this.player = player;
        loginMessage = messages.Logged(player);
        cancelMessage = "&cSorry {player}, we couldn't process your login attempt".replace("{player}", player.getName());
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
     * Get the plugin login message
     *
     * @return a String
     */
    public final String getLoginMessage() {
        return loginMessage;
    }

    /**
     * Set the login message
     *
     * @param message the message
     */
    public final void setLoginMessage(String message) {
        if (!message.isEmpty()) {
            loginMessage = message.replace("{player}", player.getName());
        }
    }

    /**
     * Get the event cancel message
     *
     * @return a String
     */
    public final String getCancelMessage() {
        return cancelMessage.replace("{player}", player.getName());
    }

    /**
     * Set the canceled message
     *
     * @param message the message
     */
    public final void setCancelMessage(String message) {
        if (!message.isEmpty()) {
            cancelMessage = message.replace("{player}", player.getName());
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
    public final boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public final void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
