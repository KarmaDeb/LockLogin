package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.IPStorage.IPStorager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

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

public final class LookUpCommand extends Command implements LockLoginBungee, BungeeFiles {

    public LookUpCommand() {
        super("lookup", "");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            final String checkPlayerInfo = "locklogin.playerinfo";

            if (player.hasPermission(checkPlayerInfo)) {
                if (args.length == 0) {
                    user.Message(messages.Prefix() + messages.LookUpUsage());
                } else {
                    if (args[0] != null) {
                        if (args.length == 1) {
                            if (args[0].length() > 3) {
                                if (args[0].startsWith("-p") || args[0].startsWith("-a")) {
                                    List<String> alsoKnownAs = new ArrayList<>();

                                    if (args[0].startsWith("-p")) {
                                        String target = args[0].replace(args[0].substring(0, 2), "");

                                        alsoKnownAs = IPStorager.getStorage(target, false);
                                    } else {
                                        if (args[0].startsWith("-a")) {
                                            String IP = args[0].replace(args[0].substring(0, 2), "");

                                            alsoKnownAs = IPStorager.getStorage(IP, true);
                                        }
                                    }

                                    if (!alsoKnownAs.isEmpty()) {

                                        List<String> message = new ArrayList<>();

                                        if (alsoKnownAs.size() <= 6) {
                                            message.add("&6&l&m------&r &eLockLogin &6&l&m------");
                                            message.add(" ");
                                            message.add("&7Player/IP &b" + args[0].replace(args[0].substring(0, 2), "") + " &7is also known as:");
                                            for (String str : alsoKnownAs) {
                                                ProxiedPlayer tar = plugin.getProxy().getPlayer(str);
                                                if (tar != null) {
                                                    if (tar != player) {
                                                        message.add("&8&l&m=&r &7" + str + " &f&l&o( &aOnline &f&l&o)");
                                                    } else {
                                                        message.add("&8&l&m=&r &7" + str + " &f&l&o( &bYou &f&l&o)");
                                                    }
                                                } else {
                                                    message.add("&8&l&m=&r &7" + str + " &f&l&o( &cOffline &f&l&o)");
                                                }
                                            }

                                            user.Message(message);
                                        } else {
                                            for (String str : alsoKnownAs) {
                                                ProxiedPlayer tar = plugin.getProxy().getPlayer(str);
                                                if (tar != null) {
                                                    if (tar != player) {
                                                        message.add("&a" + str);
                                                    } else {
                                                        message.add("&b" + str);
                                                    }
                                                } else {
                                                    message.add("&c" + str);
                                                }
                                            }

                                            user.Message(messages.Prefix() + "&7Player/IP &b" + args[0].replace(args[0].substring(0, 2), "") + " &7is also known as: &a" +
                                                    message.toString()
                                                            .replace("[", "")
                                                            .replace(",", StringUtils.toColor("&7,"))
                                                            .replace("]", ""));
                                        }
                                    } else {
                                        user.Message(messages.Prefix() + "&7Player/IP &b" + args[0].replace(args[0].substring(0, 2), "") + " &7is also known as: &cNo data found");
                                    }
                                } else {
                                    user.Message(messages.Prefix() + messages.LookUpUsage());
                                }
                            }
                        } else {
                            user.Message(messages.Prefix() + messages.LookUpUsage());
                        }
                    } else {
                        user.Message(messages.Prefix() + messages.LookUpUsage());
                    }
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError(checkPlayerInfo));
            }
        } else {
            if (args.length == 0) {
                out.Message(messages.Prefix() + messages.LookUpUsage());
            } else {
                if (args[0] != null) {
                    if (args.length == 1) {
                        if (args[0].length() > 3) {
                            if (args[0].startsWith("-p") || args[0].startsWith("-a")) {
                                List<String> alsoKnownAs = new ArrayList<>();

                                if (args[0].startsWith("-p")) {
                                    String target = args[0].replace(args[0].substring(0, 2), "");

                                    alsoKnownAs = IPStorager.getStorage(target, false);
                                } else {
                                    if (args[0].startsWith("-a")) {
                                        String IP = args[0].replace(args[0].substring(0, 2), "");

                                        alsoKnownAs = IPStorager.getStorage(IP, true);
                                    }
                                }

                                if (!alsoKnownAs.isEmpty()) {
                                    List<String> message = new ArrayList<>();

                                    if (alsoKnownAs.size() <= 6) {
                                        message.add("&6&l&m------&r &eLockLogin &6&l&m------");
                                        message.add(" ");
                                        message.add("&7Player/IP &b" + args[0].replace(args[0].substring(0, 2), "") + " &7is also known as:");
                                        for (String str : alsoKnownAs) {
                                            ProxiedPlayer tar = plugin.getProxy().getPlayer(str);
                                            if (tar != null) {
                                                message.add("&8&l&m=&r &7" + str + " &f&l&o( &aOnline &f&l&o)");
                                            } else {
                                                message.add("&8&l&m=&r &7" + str + " &f&l&o( &cOffline &f&l&o)");
                                            }
                                        }

                                        out.Message(message);
                                    } else {
                                        for (String str : alsoKnownAs) {
                                            ProxiedPlayer tar = plugin.getProxy().getPlayer(str);
                                            if (tar != null) {
                                                message.add("&a" + str);
                                            } else {
                                                message.add("&c" + str);
                                            }
                                        }

                                        out.Message(messages.Prefix() + "&7Player/IP &b" + args[0].replace(args[0].substring(0, 2), "") + " &7is also known as: &a" +
                                                message.toString()
                                                        .replace("[", "")
                                                        .replace(",", StringUtils.toColor("&7,"))
                                                        .replace("]", ""));
                                    }
                                } else {
                                    out.Message(messages.Prefix() + "&7Player/IP &b" + args[0].replace(args[0].substring(0, 2), "") + " &7is also known as: &cNo data found");
                                }
                            } else {
                                out.Message(messages.Prefix() + messages.LookUpUsage());
                            }
                        }
                    } else {
                        out.Message(messages.Prefix() + messages.LookUpUsage());
                    }
                } else {
                    out.Message(messages.Prefix() + messages.LookUpUsage());
                }
            }
        }
    }
}
