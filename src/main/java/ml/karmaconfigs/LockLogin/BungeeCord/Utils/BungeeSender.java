package ml.karmaconfigs.LockLogin.BungeeCord.Utils;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.UUID;

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

public final class BungeeSender implements LockLoginBungee, BungeeFiles {

    /**
     * Send the player login info
     *
     * @param player the player
     */
    public final void sendAccountStatus(ProxiedPlayer player) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        boolean status;
        boolean blind;

        User user = new User(player);

        if (!user.isRegistered()) {
            blind = config.RegisterBlind();
        } else {
            blind = config.LoginBlind();
        }

        status = user.isLogged();

        if (status) blind = false;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                try {
                    message.writeUTF("LoginData");
                    message.writeUTF(player.getUniqueId().toString() + " " + status);
                    blindEffect(player, blind);

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                    }
                } catch (Throwable e) {
                    Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                }
            }
        }
    }

    /**
     * Send the UUID to the server
     *
     * @param id     the uuid
     * @param server the server
     */
    public final void sendUUID(UUID id, Server server) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (id != null && server != null) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream message = new DataOutputStream(b);

            try {
                message.writeUTF("VerifyUUID");
                message.writeUTF(id.toString());

                try {
                    server.getInfo().sendData("ll:info", b.toByteArray());
                } catch (NullPointerException e) {
                    Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                }
            } catch (Throwable e) {
                Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
            }
        }
    }

    /**
     * Open the pin gui for the player
     *
     * @param player the player
     */
    public final void openPinGUI(ProxiedPlayer player) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                try {
                    message.writeUTF("OpenPin");
                    message.writeUTF(player.getUniqueId().toString());

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                    }
                } catch (Throwable e) {
                    Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                }
            }
        }
    }

    /**
     * Send the player pin status
     *
     * @param player the player
     */
    public final void closePinGUI(ProxiedPlayer player) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                try {
                    message.writeUTF("ClosePin");
                    message.writeUTF(player.getUniqueId().toString());

                    try {
                         player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                    }
                } catch (Throwable e) {
                    Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                }
            }
        }
    }

    /**
     * Send a request to apply blind effects to
     * the player
     *
     * @param player the player
     * @param apply apply, or remove the effects
     */
    public final void blindEffect(ProxiedPlayer player, boolean apply) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                try {
                    message.writeUTF("EffectManager");
                    message.writeUTF(player.getUniqueId().toString() + "_" + apply);

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                    }
                } catch (Throwable e) {
                    Logger.log(Platform.BUNGEE, "ERROR WHILE WRITING A PLUGIN MESSAGE FROM BUNGEECORD", e);
                }
            }
        }
    }
}
