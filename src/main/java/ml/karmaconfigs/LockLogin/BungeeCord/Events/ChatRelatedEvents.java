package ml.karmaconfigs.LockLogin.BungeeCord.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.DataFiles.AllowedCommands;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class ChatRelatedEvents implements Listener, LockLoginBungee,  BungeeFiles {

    /**
     * Get the main command from the cmd
     * even if it has :
     *
     * @param cmd the cmd
     * @return a String
     */
    private String getCommand(String cmd) {
        if (cmd.contains(":")) {
            try {
                String[] cmdData = cmd.split(":");

                if (cmdData[0] != null && !cmdData[0].isEmpty()) {
                    if (cmdData[1] != null && !cmdData[1].isEmpty()) {
                        return cmdData[1];
                    }
                }
            } catch (Throwable ignored) {}
            return cmd.split(" ")[0].replace("/", "");
        } else {
            if (cmd.contains(" ")) {
                return cmd.split(" ")[0].replace("/", "");
            } else {
                return cmd.replace("/", "");
            }
        }
    }

    /**
     * Get the complete main command
     * including ':'
     *
     * @param cmd the cmd
     * @return a String
     */
    private String getCompleteCommand(String cmd) {
        if (cmd.contains(" ")) {
            return cmd.split(" ")[0].replace("/", "");
        } else {
            return cmd.replace("/", "");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onChat(ChatEvent e) {
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        User user = new User(player);

        AllowedCommands allowed = new AllowedCommands();

        String cmd = getCommand(e.getMessage());

        if (e.getMessage().startsWith("/")) {
            if (!user.isLogged()) {
                if (!user.isRegistered()) {
                    if (!cmd.equals("register") && !cmd.equals("reg")) {
                        e.setCancelled(true);
                        user.Message(messages.Prefix() + messages.Register());
                    }
                } else {
                    if (!allowed.isAllowed(getCompleteCommand(e.getMessage()))) {
                        if (!cmd.equals("login") && !cmd.equals("l")) {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.Login());
                        }
                    }
                }
            } else {
                if (user.isTempLog()) {
                    if (user.hasPin()) {
                        dataSender.openPinGUI(player);
                    }
                    if (user.has2FA()) {
                        if (!cmd.equals("2fa")) {
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                        }
                    }
                }
            }
        } else {
            if (!user.isLogged()) {
                e.setCancelled(true);
                if (!user.isRegistered()) {
                    user.Message(messages.Prefix() + messages.Register());
                } else {
                    user.Message(messages.Prefix() + messages.Login());
                }
            } else {
                if (user.isTempLog()) {
                    e.setCancelled(true);
                    if (user.has2FA()) {
                        user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                    }
                }
            }
        }
    }
}
