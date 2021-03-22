package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.*;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetSocketAddress;

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

public final class GoogleAuthCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (config.enable2FA()) {
                if (args.length == 0) {
                    if (user.isRegistered()) {
                        if (user.isLogged()) {
                            if (user.has2FA()) {
                                if (user.isTempLog()) {
                                    user.send(messages.prefix() + messages.gAuthAuthenticate());
                                } else {
                                    user.send(messages.prefix() + messages.already2FA());
                                }
                            } else {
                                if (!user.isTempLog()) {
                                    user.send(messages.prefix() + messages.enable2FA());
                                } else {
                                    user.send(messages.prefix() + messages.gAuthAuthenticate());
                                }
                            }
                        } else {
                            if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                            } else {
                                user.send(messages.prefix() + messages.typeCaptcha());
                            }
                        }
                    } else {
                        user.send(messages.prefix() + messages.prefix());
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
                                } catch (NumberFormatException e) {
                                    event.setAuthResult(EventAuthResult.FAILED, messages.prefix() + messages.gAuthIncorrect());
                                }

                                plugin.getServer().getPluginManager().callEvent(event);

                                switch (event.getAuthResult()) {
                                    case SUCCESS:
                                    case SUCCESS_TEMP:
                                        if (valid_code) {
                                            InetSocketAddress ip = player.getAddress();

                                            if (ip != null) {
                                                BFSystem bf_prevention = new BFSystem(ip.getAddress());
                                                bf_prevention.success();
                                            }

                                            user.sendTitle("", "", 1, 2, 1);
                                            user.setTempLog(false);

                                            user.send(event.getAuthMessage());

                                            if (config.takeBack()) {
                                                LastLocation lastLoc = new LastLocation(player);
                                                if (lastLoc.hasLastLocation())
                                                    user.teleport(lastLoc.getLastLocation());
                                            }

                                            user.removeBlindEffect();

                                            player.setAllowFlight(user.hasFly());

                                            File motd_file = new File(plugin.getDataFolder(), "motd.locklogin");
                                            Motd motd = new Motd(motd_file);

                                            if (motd.isEnabled())
                                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> user.send(motd.onLogin(player.getName(), config.serverName())), 20L * motd.getDelay());
                                        } else {
                                            logger.scheduleLog(Level.WARNING, "Someone tried to force log (2FA) " + player.getName() + " using event API");
                                            user.send(event.getAuthMessage());
                                        }
                                        break;
                                    case FAILED:
                                    case ERROR:
                                    case WAITING:
                                        user.send(event.getAuthMessage());
                                        break;
                                }
                            } else {
                                if (!user.isLogged()) {
                                    if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                        if (user.isRegistered()) {
                                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                        } else {
                                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.typeCaptcha());
                                    }
                                } else {
                                    if (!user.isTempLog()) {
                                        user.send(messages.prefix() + messages.already2FA());
                                    }
                                }
                            }
                        } else {
                            if (user.isLogged() && !user.isTempLog()) {
                                String password = args[0];
                                String token = user.genToken();

                                PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                                if (utils.validate()) {
                                    if (config.takeBack()) {
                                        LastLocation lastLocation = new LastLocation(player);
                                        lastLocation.saveLocation();
                                    }

                                    if (config.enableSpawn()) {
                                        Spawn spawn = new Spawn();

                                        user.teleport(spawn.getSpawn());
                                    }

                                    user.setToken(token);
                                    user.setTempLog(true);
                                    user.set2FA(true);
                                    user.send(messages.prefix() + messages.gAuthInstructions());
                                    ComponentMaker json = new ComponentMaker(messages.gAuthLink());
                                    String url = json.getURL(player, token);
                                    json.setHoverText("&bQR Code &c( USE THE LINK BELOW IF YOU CAN'T CLICK THIS )");
                                    json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                                    user.send(json.getComponent());
                                    user.send("&b" + url);
                                } else {
                                    user.send(messages.prefix() + messages.toggle2FAError());
                                }
                            } else {
                                if (!user.isLogged()) {
                                    if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                        if (user.isRegistered()) {
                                            user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                        } else {
                                            user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.typeCaptcha());
                                    }
                                } else {
                                    if (user.isTempLog()) {
                                        user.send(messages.prefix() + messages.gAuthAuthenticate());
                                    }
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
                                        return false;
                                    }
                                } else {
                                    if (!user.isLogged()) {
                                        if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                            if (user.isRegistered()) {
                                                user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                            } else {
                                                user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                            }
                                        } else {
                                            user.send(messages.prefix() + messages.typeCaptcha());
                                        }
                                    } else {
                                        if (user.isTempLog()) {
                                            user.send(messages.prefix() + messages.gAuthAuthenticate());
                                        }
                                    }
                                }
                            } else {
                                user.send(messages.prefix() + messages.enable2FA());
                            }
                        } else {
                            if (!user.hasCaptcha() || config.getCaptchaType().equals(CaptchaType.SIMPLE)) {
                                if (user.isRegistered()) {
                                    if (user.isLogged()) {
                                        if (user.has2FA()) {
                                            if (user.isTempLog()) {
                                                user.send(messages.prefix() + messages.gAuthAuthenticate());
                                            } else {
                                                user.send(messages.prefix() + messages.already2FA());
                                            }
                                        } else {
                                            if (!user.isTempLog()) {
                                                user.send(messages.prefix() + messages.enable2FA());
                                            } else {
                                                user.send(messages.prefix() + messages.gAuthAuthenticate());
                                            }
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                    }
                                } else {
                                    user.send(messages.prefix() + messages.register(user.getCaptcha()));
                                }
                            } else {
                                user.send(messages.prefix() + messages.typeCaptcha());
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
        return false;
    }
}
