package ml.karmaconfigs.lockloginsystem.bungeecord.api.events;

import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
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

public class PlayerAuthEvent extends Event {

    private final AuthType auth_type;
    private final ProxiedPlayer player;
    private EventAuthResult auth_result;
    private String auth_message;

    public PlayerAuthEvent(final AuthType _auth_type, final EventAuthResult _auth_result, final ProxiedPlayer _player, final String _auth_message) {
        auth_type = _auth_type;
        auth_result = _auth_result;
        player = _player;
        auth_message = _auth_message;
    }

    public final ProxiedPlayer getPlayer() {
        return player;
    }

    public final AuthType getAuthType() {
        return auth_type;
    }

    public final EventAuthResult getAuthResult() {
        return auth_result;
    }

    public final void setAuthResult(final EventAuthResult result) {
        auth_result = result;
    }

    public final String getAuthMessage() {
        return auth_message;
    }

    public final void setAuthResult(final EventAuthResult result, final String message) {
        auth_result = result;
        auth_message = message;
    }
}
