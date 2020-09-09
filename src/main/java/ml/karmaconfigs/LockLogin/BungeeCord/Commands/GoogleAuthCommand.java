package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.ComponentMaker;
import ml.karmaconfigs.LockLogin.Security.PasswordUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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

public final class GoogleAuthCommand extends Command implements LockLoginBungee, BungeeFiles {

    public GoogleAuthCommand() {
        super("2fa", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
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
                                        user.setTempLog(false);
                                        user.setLogStatus(true);
                                        user.Message(messages.Prefix() + messages.gAuthCorrect());

                                        if (config.EnableMain()) {
                                            if (lobbyCheck.MainIsWorking()) {
                                                user.sendTo(lobbyCheck.getMain());
                                            }
                                        }

                                        dataSender.sendAccountStatus(player);
                                    } else {
                                        user.Message(messages.Prefix() + messages.gAuthIncorrect());
                                    }
                                } catch (NumberFormatException e) {
                                    user.Message(messages.Prefix() + messages.gAuthIncorrect());
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
                                    if (config.EnableAuth()) {
                                        if (lobbyCheck.AuthIsWorking()) {
                                            user.sendTo(lobbyCheck.getAuth());
                                        }
                                    }

                                    user.setToken(token);
                                    user.setTempLog(true);
                                    user.set2FA(true);
                                    user.Message(messages.Prefix() + messages.GAuthInstructions());
                                    ComponentMaker json = new ComponentMaker(messages.GAuthLink());
                                    json.setHoverText("&aQR Code");
                                    json.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, json.getURL(player, token)));
                                    user.Message(json.getComponent());

                                    dataSender.sendAccountStatus(player);
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
    }
}
