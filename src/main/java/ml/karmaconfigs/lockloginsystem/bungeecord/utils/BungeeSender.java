package ml.karmaconfigs.lockloginsystem.bungeecord.utils;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashSet;
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
        boolean nausea;

        User user = new User(player);

        if (!user.isRegistered()) {
            blind = config.RegisterBlind();
            nausea = config.RegisterNausea();
        } else {
            blind = config.LoginBlind();
            nausea = config.LoginNausea();
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
                    blindEffect(player, blind, nausea);

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                    }
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
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
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
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
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                    }
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
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
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                    }
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                }
            }
        }
    }

    /**
     * Open the player lookup GUI
     *
     * @param player the player that is trying to open the GUI
     * @param users  the users to show in the GUI
     */
    public final void openLookupGUI(final ProxiedPlayer player, final HashSet<OfflineUser> users) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                StringBuilder uuids_builder = new StringBuilder();
                for (OfflineUser user : users)
                    uuids_builder.append(";").append(user.getUUID());

                try {
                    message.writeUTF("LookupGUI");
                    message.writeUTF(player.getUniqueId() + uuids_builder.toString());

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                    }
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                }
            }
        }
    }

    /**
     * Open the modules viewer GUI
     *
     * @param player the player that is trying to open the GUI
     */
    public final void openModulesGUI(final ProxiedPlayer player) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                ModuleSerializer serializer = new ModuleSerializer(player);
                String serialized = serializer.serialize();

                try {
                    message.writeUTF("ModulesInfoData");
                    message.writeUTF(serialized);

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                    }
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                }
            }
        }
    }

    /**
     * Send a request to apply blind effects to
     * the player
     *
     * @param player the player
     * @param apply  apply, or remove the effects
     * @param nausea is nausea enabled in config?
     */
    public final void blindEffect(ProxiedPlayer player, boolean apply, boolean nausea) {
        if (plugin.getProxy().getPlayers().isEmpty())
            return;

        if (player != null) {
            if (player.getServer() != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream message = new DataOutputStream(b);

                try {
                    message.writeUTF("EffectManager");
                    message.writeUTF(player.getUniqueId().toString() + "_" + apply + "_" + nausea);

                    try {
                        player.getServer().getInfo().sendData("ll:info", b.toByteArray());
                    } catch (Throwable e) {
                        logger.scheduleLog(Level.GRAVE, e);
                        logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                    }
                } catch (Throwable e) {
                    logger.scheduleLog(Level.GRAVE, e);
                    logger.scheduleLog(Level.INFO, "Error while sending a plugin message from BungeeCord");
                }
            }
        }
    }
}
