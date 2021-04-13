package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginapi.bungee.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginmodules.shared.listeners.LockLoginListener;
import ml.karmaconfigs.lockloginmodules.shared.listeners.events.user.UserAuthEvent;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.*;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class GoogleAuthCommand extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize google auth command
     */
    public GoogleAuthCommand() {
        super("2fa", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (config.enable2FA()) {
                if (args.length == 0) {
                    if (user.isLogged()) {
                        if (user.has2FA()) {
                            if (user.isTempLog()) {
                                user.send(messages.prefix() + messages.gAuthenticate());
                            } else {
                                user.send(messages.prefix() + messages.already2FA());
                            }
                        } else {
                            if (!user.isTempLog()) {
                                user.send(messages.prefix() + messages.enable2FA());
                            } else {
                                user.send(messages.prefix() + messages.gAuthenticate());
                            }
                        }
                    } else {
                        if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                            if (user.isRegistered()) {
                                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                            } else {
                                user.send(messages.prefix() + messages.register(user.getCaptcha()));
                            }
                        } else {
                            user.send(messages.prefix() + messages.typeCaptcha());
                        }
                    }
                } else {
                    if (args.length == 1) {
                        if (user.has2FA()) {
                            if (user.isLogged() && user.isTempLog()) {
                                PlayerAuthEvent event = new PlayerAuthEvent(AuthType.FA_2, EventAuthResult.WAITING, player, "");

                                boolean valid_code = false;
                                try {
                                    int code = Integer.parseInt(args[0]);
                                    if (user.validateCode(code)) {
                                        valid_code = true;
                                        event.setAuthResult(EventAuthResult.SUCCESS, messages.prefix() + messages.gAuthCorrect());
                                    } else {
                                        event.setAuthResult(EventAuthResult.FAILED, messages.prefix() + messages.gAuthIncorrect());
                                    }
                                } catch (Throwable ex) {
                                    event.setAuthResult(EventAuthResult.FAILED, messages.prefix() + messages.gAuthIncorrect());
                                }

                                plugin.getProxy().getPluginManager().callEvent(event);
                                UserAuthEvent authEvent = new UserAuthEvent(event.getAuthType(), event.getAuthResult(), player, event.getAuthMessage(), event);

                                switch (event.getAuthResult()) {
                                    case SUCCESS:
                                    case SUCCESS_TEMP:
                                        user.send(event.getAuthMessage());
                                        if (valid_code) {
                                            user.setLogged(true);
                                            user.setTempLog(false);
                                            user.checkServer();

                                            dataSender.sendAccountStatus(player);
                                            dataSender.blindEffect(player, false, config.nauseaLogin());

                                            File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
                                            Motd motd = new Motd(motd_file);

                                            if (motd.isEnabled())
                                                plugin.getProxy().getScheduler().schedule(plugin, () -> user.send(motd.onLogin(player.getName(), config.serverName())), motd.getDelay(), TimeUnit.SECONDS);

                                            LockLoginListener.callEvent(authEvent);
                                        } else {
                                            logger.scheduleLog(Level.WARNING, "Someone tried to force log (2FA) " + player.getName() + " using event API");
                                        }
                                        break;
                                    case FAILED:
                                    case ERROR:
                                    case WAITING:
                                        user.send(event.getAuthMessage());
                                        LockLoginListener.callEvent(authEvent);
                                        break;
                                }
                            } else {
                                if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                    if (!user.isLogged()) {
                                        if (user.isRegistered()) {
                                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                        } else {
                                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                        }
                                    } else {
                                        if (!user.isTempLog()) {
                                            user.send(messages.prefix() + messages.already2FA());
                                        }
                                    }
                                } else {
                                    user.send(messages.prefix() + messages.typeCaptcha());
                                }
                            }
                        } else {
                            if (user.isLogged() && !user.isTempLog()) {
                                String password = args[0];
                                String token = user.genToken();

                                PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                                if (utils.validate()) {
                                    user.checkServer();

                                    user.setToken(token);
                                    user.setTempLog(true);
                                    user.set2FA(true);
                                    user.send(messages.prefix() + messages.gAuthInstructions());
                                    ComponentMaker json = new ComponentMaker(messages.gAuthLink());
                                    json.setHoverText("&aQR Code");
                                    json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, json.getURL(player, token)));
                                    user.send(json.getComponent());

                                    dataSender.sendAccountStatus(player);
                                } else {
                                    user.send(messages.prefix() + messages.toggle2FAError());
                                }
                            } else {
                                if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                    if (!user.isLogged()) {
                                        if (user.isRegistered()) {
                                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                        } else {
                                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                        }
                                    } else {
                                        if (user.isTempLog()) {
                                            user.send(messages.prefix() + messages.gAuthenticate());
                                        }
                                    }
                                } else {
                                    user.send(messages.prefix() + messages.typeCaptcha());
                                }
                            }
                        }
                    } else {
                        if (args.length == 2) {
                            if (user.has2FA()) {
                                if (user.isLogged() && !user.isTempLog()) {
                                    try {
                                        int code = Integer.parseInt(args[1]);
                                        String password = args[0];

                                        PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                                        if (utils.validate()) {
                                            if (user.validateCode(code)) {
                                                user.set2FA(false);
                                                user.send(messages.prefix() + messages.disabled2FA());
                                            } else {
                                                user.send(messages.prefix() + messages.gAuthIncorrect());
                                            }
                                        } else {
                                            user.send(messages.prefix() + messages.toggle2FAError());
                                        }
                                    } catch (NumberFormatException e) {
                                        user.send(messages.prefix() + messages.gAuthIncorrect());
                                    }
                                } else {
                                    if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                        if (!user.isLogged()) {
                                            if (user.isRegistered()) {
                                                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                            } else {
                                                user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                            }
                                        } else {
                                            if (user.isTempLog()) {
                                                user.send(messages.prefix() + messages.gAuthenticate());
                                            }
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.typeCaptcha());
                                    }
                                }
                            } else {
                                user.send(messages.prefix() + messages.enable2FA());
                            }
                        } else {
                            if (user.isLogged()) {
                                if (user.has2FA()) {
                                    if (user.isTempLog()) {
                                        user.send(messages.prefix() + messages.gAuthenticate());
                                    } else {
                                        user.send(messages.prefix() + messages.already2FA());
                                    }
                                } else {
                                    if (!user.isTempLog()) {
                                        user.send(messages.prefix() + messages.enable2FA());
                                    } else {
                                        user.send(messages.prefix() + messages.gAuthenticate());
                                    }
                                }
                            } else {
                                if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                    if (user.isRegistered()) {
                                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                    } else {
                                        user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                    }
                                } else {
                                    user.send(messages.prefix() + messages.typeCaptcha());
                                }
                            }
                        }
                    }
                }
            } else {
                user.send(messages.prefix() + messages.gAuthDisabled());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
    }
}
