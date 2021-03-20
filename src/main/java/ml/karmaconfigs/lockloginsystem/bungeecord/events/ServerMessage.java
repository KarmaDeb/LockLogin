package ml.karmaconfigs.lockloginsystem.bungeecord.events;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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

public final class ServerMessage implements Listener, LockLoginBungee, BungeeFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equalsIgnoreCase("ll:info")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String[] data = in.readUTF().split("_");
                String channel = data[0];
                if (channel.equals("PinInput")) {
                    String input = data[2];
                    UUID uuid = UUID.fromString(data[1]);

                    if (plugin.getProxy().getPlayer(uuid) != null) {
                        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
                        User user = new User(player);

                        PasswordUtils utils = new PasswordUtils(input, user.getPin());

                        PlayerAuthEvent event = new PlayerAuthEvent(AuthType.PIN, EventAuthResult.WAITING, player, "");

                        boolean valid_code = false;
                        if (utils.validate()) {
                            valid_code = true;
                            if (user.has2FA()) {
                                event.setAuthResult(EventAuthResult.SUCCESS_TEMP, messages.gAuthInstructions());
                            } else {
                                event.setAuthResult(EventAuthResult.SUCCESS, messages.prefix() + messages.logged(player));
                            }
                        } else {
                            event.setAuthResult(EventAuthResult.FAILED);
                        }

                        plugin.getProxy().getPluginManager().callEvent(event);

                        switch (event.getAuthResult()) {
                            case SUCCESS:
                                user.send(event.getAuthMessage());
                                if (valid_code) {
                                    user.setLogged(true);
                                    user.checkServer();

                                    dataSender.sendAccountStatus(player);
                                    dataSender.blindEffect(player, false, config.nauseaLogin());

                                    if (utils.needsRehash(config.pinEncryption())) {
                                         user.setPin(input);
                                    }
                                } else {
                                    logger.scheduleLog(Level.WARNING, "Someone tried to force log (PIN AUTH) " + player.getName() + " using event API");
                                    dataSender.openPinGUI(player);
                                }
                                break;
                            case SUCCESS_TEMP:
                                user.send(event.getAuthMessage());
                                if (valid_code) {
                                    user.setTempLog(true);
                                    dataSender.closePinGUI(player);

                                    if (utils.needsRehash(config.pinEncryption())) {
                                        user.setPin(input);
                                    }
                                } else {
                                    logger.scheduleLog(Level.WARNING, "Someone tried to force temp log (PIN AUTH) " + player.getName() + " using event API");
                                    dataSender.openPinGUI(player);
                                }
                                break;
                            case FAILED:
                                break;
                            case ERROR:
                            case WAITING:
                                user.send(event.getAuthMessage());
                                break;
                        }
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
