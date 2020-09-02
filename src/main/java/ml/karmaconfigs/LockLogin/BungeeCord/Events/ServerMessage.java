package ml.karmaconfigs.LockLogin.BungeeCord.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerPinEvent;
import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ServerMessage implements Listener, LockLoginBungee, BungeeFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equalsIgnoreCase("ll:info")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
            try {
                String[] data = in.readUTF().split("_");
                String channel = data[0];
                if(channel.equals("PinInput")) {
                    int input = Integer.parseInt(data[2]);
                    UUID uuid = UUID.fromString(data[1]);

                    if (plugin.getProxy().getPlayer(uuid) != null) {
                        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
                        User user = new User(player);

                        PasswordUtils utils = new PasswordUtils(String.valueOf(input), user.getPin());

                        PlayerPinEvent event = new PlayerPinEvent(player, utils.PasswordIsOk());
                        plugin.getProxy().getPluginManager().callEvent(event);

                        if (utils.PasswordIsOk()) {
                            if (!user.has2FA()) {
                                user.setTempLog(false);
                                user.Message(messages.Prefix() + messages.Logged(player));

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (config.EnableMain()) {
                                            if (lobbyCheck.MainIsWorking()) {
                                                user.sendTo(lobbyCheck.getMain());
                                            }
                                        }
                                    }
                                }, TimeUnit.SECONDS.toMillis(1));

                                dataSender.sendAccountStatus(player);
                            } else {
                                user.setTempLog(true);
                                user.Message(messages.GAuthInstructions());
                            }
                            dataSender.closePinGUI(player);
                            dataSender.blindEffect(player, false);
                        } else {
                            dataSender.blindEffect(player, true);
                            dataSender.openPinGUI(player);
                        }
                    }
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
