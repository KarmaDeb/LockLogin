package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                                try {
                                    int code = Integer.parseInt(args[0]);
                                    if (user.validateCode(code)) {
                                        LastLocation lastLoc = new LastLocation(player);

                                        user.setTempLog(false);
                                        user.Message(messages.Prefix() + messages.gAuthCorrect());
                                        if (config.TakeBack()) {
                                            user.Teleport(lastLoc.getLastLocation());
                                        }

                                        player.setAllowFlight(user.hasFly());
                                    } else {
                                        user.Message(messages.Prefix() + messages.gAuthIncorrect());
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
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
