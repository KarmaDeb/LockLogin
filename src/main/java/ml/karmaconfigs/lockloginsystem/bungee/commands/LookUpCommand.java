package ml.karmaconfigs.lockloginsystem.bungee.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.lockloginmodules.bungee.PluginModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungee.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungee.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.bungee.utils.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Set;

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
public final class LookUpCommand extends Command implements LockLoginBungee, BungeeFiles {

    /**
     * Initialize lookup command
     */
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
                    user.send(messages.prefix() + messages.lookupUsage());
                } else {
                    if (args[0] != null) {
                        if (args.length == 1) {
                            String target = args[0];

                            TempPluginModule temp_module = new TempPluginModule();
                            PluginModuleLoader bungee_module_loader = new PluginModuleLoader(temp_module);
                            try {
                                if (!PluginModuleLoader.manager.isLoaded(temp_module)) {
                                    bungee_module_loader.inject();
                                }
                            } catch (Throwable ignored) {
                            }

                            OfflineUser off_user = new OfflineUser("", target, true);
                            if (off_user.exists()) {
                                Set<OfflineUser> detected = IPStorager.manager.getAlts(temp_module, off_user.getUUID().getId());

                                dataSender.openLookupGUI(player, detected);
                            } else {
                                user.send(messages.prefix() + messages.unknownPlayer(target));
                            }
                        } else {
                            user.send(messages.prefix() + messages.lookupUsage());
                        }
                    } else {
                        user.send(messages.prefix() + messages.lookupUsage());
                    }
                }
            } else {
                user.send(messages.prefix() + messages.permission(checkPlayerInfo));
            }
        } else {
            if (args.length == 0) {
                Console.send(messages.prefix() + messages.lookupUsage());
            } else {
                if (args[0] != null) {
                    if (args.length == 1) {
                        String target = args[0];

                        TempPluginModule temp_module = new TempPluginModule();
                        PluginModuleLoader bungee_module_loader = new PluginModuleLoader(temp_module);
                        try {
                            if (!PluginModuleLoader.manager.isLoaded(temp_module)) {
                                bungee_module_loader.inject();
                            }
                        } catch (Throwable ignored) {
                        }

                        OfflineUser off_user = new OfflineUser("", target, true);
                        if (off_user.exists()) {
                            Set<OfflineUser> detected = IPStorager.manager.getAlts(temp_module, off_user.getUUID().getId());

                            Console.send("&7------------ &eLockLogin alt accounts finder for " + target + " &7------------");
                            System.out.println("\n");
                            for (OfflineUser player : detected) {
                                Console.send("&e" + player.getName());
                                Console.send("  &7UUID: &e" + player.getUUID());
                            }
                        } else {
                            Console.send(messages.prefix() + messages.unknownPlayer(target));
                        }
                    } else {
                        Console.send(messages.prefix() + messages.lookupUsage());
                    }
                } else {
                    Console.send(messages.prefix() + messages.lookupUsage());
                }
            }
        }
    }
}
