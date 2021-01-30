package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.AuthType;
import ml.karmaconfigs.lockloginsystem.shared.ComponentMaker;
import ml.karmaconfigs.lockloginsystem.shared.EventAuthResult;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.PasswordUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.api.events.PlayerAuthEvent;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Mailer;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.Spawn;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (config.Enable2FA()) {
                if (args.length == 0) {
                    if (user.isRegistered()) {
                        if (user.isLogged()) {
                            if (user.has2FA()) {
                                if (user.isTempLog()) {
                                    user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                } else {
                                    user.Message(messages.Prefix() + messages.AlreadyFA());
                                }
                            } else {
                                if (!user.isTempLog()) {
                                    user.Message(messages.Prefix() + messages.Enable2FA());
                                } else {
                                    user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                }
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.Login());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.Prefix());
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
                                        event.setAuthResult(EventAuthResult.SUCCESS, messages.Prefix() + messages.gAuthCorrect());
                                    } else {
                                        event.setAuthResult(EventAuthResult.FAILED, messages.Prefix() + messages.gAuthIncorrect());
                                    }
                                } catch (NumberFormatException e) {
                                    event.setAuthResult(EventAuthResult.FAILED, messages.Prefix() + messages.gAuthIncorrect());
                                }

                                plugin.getServer().getPluginManager().callEvent(event);

                                switch (event.getAuthResult()) {
                                    case SUCCESS:
                                    case SUCCESS_TEMP:
                                        if (valid_code) {
                                            Mailer mailer = new Mailer();
                                            if (isValidEmailAddress(mailer.getEmail()) && mailer.sendLoginEmail()) {
                                                try {
                                                    TempModule module = new TempModule();

                                                    if (!ModuleLoader.manager.isLoaded(module)) {
                                                        ModuleLoader loader = new ModuleLoader(module);
                                                        loader.inject();
                                                    }

                                                    InetSocketAddress ip = player.getAddress();

                                                    if (ip != null) {
                                                        IPStorager storager = new IPStorager(module, ip.getAddress());

                                                        if (storager.differentIP(player.getUniqueId()) && isValidEmailAddress(user.getEmail()) && mailer.sendLoginEmail()) {
                                                            user.sendLoginEmail();
                                                        } else {
                                                            storager.saveLastIP(player.getUniqueId());
                                                        }
                                                    }
                                                } catch (Throwable ignored) {
                                                }
                                            }

                                            InetSocketAddress ip = player.getAddress();

                                            if (ip != null) {
                                                BFSystem bf_prevention = new BFSystem(ip.getAddress());
                                                bf_prevention.success();
                                            }

                                            user.sendTitle("", "", 1, 2, 1);
                                            user.setTempLog(false);

                                            user.Message(event.getAuthMessage());

                                            if (config.TakeBack()) {
                                                LastLocation lastLoc = new LastLocation(player);
                                                user.Teleport(lastLoc.getLastLocation());
                                            }

                                            if (config.LoginBlind())
                                                user.removeBlindEffect(config.LoginNausea());

                                            player.setAllowFlight(user.hasFly());
                                        } else {
                                            logger.scheduleLog(Level.WARNING, "Someone tried to force log (2FA) " + player.getName() + " using event API");
                                            user.Message(event.getAuthMessage());
                                        }
                                        break;
                                    case FAILED:
                                    case ERROR:
                                    case WAITING:
                                        user.Message(event.getAuthMessage());
                                        break;
                                }
                            } else {
                                if (!user.isLogged()) {
                                    if (user.isRegistered()) {
                                        user.Message(messages.Prefix() + messages.Login());
                                    } else {
                                        user.Message(messages.Prefix() + messages.Register());
                                    }
                                } else {
                                    if (!user.isTempLog()) {
                                        user.Message(messages.Prefix() + messages.AlreadyFA());
                                    }
                                }
                            }
                        } else {
                            if (user.isLogged() && !user.isTempLog()) {
                                String password = args[0];
                                String token = user.genToken();

                                PasswordUtils utils = new PasswordUtils(password, user.getPassword());

                                if (utils.PasswordIsOk()) {
                                    if (config.TakeBack()) {
                                        LastLocation lastLocation = new LastLocation(player);
                                        lastLocation.saveLocation();
                                    }

                                    if (config.HandleSpawn()) {
                                        Spawn spawn = new Spawn();

                                        user.Teleport(spawn.getSpawn());
                                    }

                                    user.setToken(token);
                                    user.setTempLog(true);
                                    user.set2FA(true);
                                    user.Message(messages.Prefix() + messages.GAuthInstructions());
                                    ComponentMaker json = new ComponentMaker(messages.GAuthLink());
                                    String url = json.getURL(player, token);
                                    json.setHoverText("&bQR Code &c( USE THE LINK BELOW IF YOU CAN'T CLICK THIS )");
                                    json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                                    user.Message(json.getComponent());
                                    user.Message("&b" + url);
                                } else {
                                    user.Message(messages.Prefix() + messages.ToggleFAError());
                                }
                            } else {
                                if (!user.isLogged()) {
                                    if (user.isRegistered()) {
                                        user.Message(messages.Prefix() + messages.Login());
                                    } else {
                                        user.Message(messages.Prefix() + messages.Register());
                                    }
                                } else {
                                    if (user.isTempLog()) {
                                        user.Message(messages.Prefix() + messages.gAuthAuthenticate());
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

                                        if (utils.PasswordIsOk()) {
                                            if (user.validateCode(code)) {
                                                user.set2FA(false);
                                                user.Message(messages.Prefix() + messages.Disabled2FA());
                                            } else {
                                                user.Message(messages.Prefix() + messages.gAuthIncorrect());
                                            }
                                        } else {
                                            user.Message(messages.Prefix() + messages.ToggleFAError());
                                        }
                                    } catch (NumberFormatException e) {
                                        user.Message(messages.Prefix() + messages.gAuthIncorrect());
                                        return false;
                                    }
                                } else {
                                    if (!user.isLogged()) {
                                        if (user.isRegistered()) {
                                            user.Message(messages.Prefix() + messages.Login());
                                        } else {
                                            user.Message(messages.Prefix() + messages.Register());
                                        }
                                    } else {
                                        if (user.isTempLog()) {
                                            user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                        }
                                    }
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.Enable2FA());
                            }
                        } else {
                            if (user.isRegistered()) {
                                if (user.isLogged()) {
                                    if (user.has2FA()) {
                                        if (user.isTempLog()) {
                                            user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                        } else {
                                            user.Message(messages.Prefix() + messages.AlreadyFA());
                                        }
                                    } else {
                                        if (!user.isTempLog()) {
                                            user.Message(messages.Prefix() + messages.Enable2FA());
                                        } else {
                                            user.Message(messages.Prefix() + messages.gAuthAuthenticate());
                                        }
                                    }
                                } else {
                                    user.Message(messages.Prefix() + messages.Login());
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.Prefix());
                            }
                        }
                    }
                }
            } else {
                user.Message(messages.Prefix() + messages.GAuthDisabled());
            }
        } else {
            Console.send(plugin, "This command is for players only", Level.WARNING);
        }
        return false;
    }

    private boolean isValidEmailAddress(final String email) {
        boolean result = true;
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (Throwable ex) {
            result = false;
        }
        return result;
    }
}
