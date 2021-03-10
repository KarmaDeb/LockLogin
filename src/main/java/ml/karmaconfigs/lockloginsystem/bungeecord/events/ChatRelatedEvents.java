package ml.karmaconfigs.lockloginsystem.bungeecord.events;

import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.AllowedCommands;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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

public final class ChatRelatedEvents implements Listener, LockLoginBungee, BungeeFiles {

    private final BungeeSender dataSender = new BungeeSender();

    /**
     * Get the main command from the cmd
     * even if it has :
     *
     * @param cmd the cmd
     * @return a command ignoring ":" prefix
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
            } catch (Throwable ignored) {
            }
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
     * @return a command including ":" prefix
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

        String cmd = getCommand(e.getMessage());

        if (e.getMessage().startsWith("/")) {
            if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                if (!user.isLogged()) {
                    if (!user.isRegistered()) {
                        if (!cmd.equals("register") && !cmd.equals("reg")) {
                            e.setCancelled(true);
                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                        }
                    } else {
                        if (!AllowedCommands.external.isAllowed(getCompleteCommand(e.getMessage()))) {
                            if (!cmd.equals("login") && !cmd.equals("l")) {
                                e.setCancelled(true);
                                user.send(messages.prefix() + messages.login(user.getCaptcha()));
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
                                user.send(messages.prefix() + messages.gAuthenticate());
                            }
                        }
                    }
                }
            } else {
                if (!cmd.equals("captcha"))
                    user.send(messages.prefix() + messages.typeCaptcha(user.getCaptcha()));
            }
        } else {
            if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                if (!user.isLogged()) {
                    e.setCancelled(true);
                    if (!user.isRegistered()) {
                        user.send(messages.prefix() + messages.register(user.getCaptcha()));
                    } else {
                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                    }
                } else {
                    if (user.isTempLog()) {
                        e.setCancelled(true);
                        if (user.has2FA()) {
                            user.send(messages.prefix() + messages.gAuthenticate());
                        }
                    }
                }
            } else {
                user.send(messages.prefix() + messages.typeCaptcha(user.getCaptcha()));
            }
        }
    }
}
