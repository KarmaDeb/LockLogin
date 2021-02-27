package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.AltsAccountInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

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

public final class LookUpCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {


    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            final String checkPlayerInfo = "locklogin.playerinfo";

            if (player.hasPermission(checkPlayerInfo)) {
                if (args.length == 0) {
                    user.Message(messages.Prefix() + messages.LookUpUsage());
                } else {
                    if (args[0] != null) {
                        if (args.length == 1) {
                            String target = args[0];

                            TempModule temp_module = new TempModule();
                            ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
                            try {
                                if (!ModuleLoader.manager.isLoaded(temp_module)) {
                                    spigot_module_loader.inject();
                                }
                            } catch (Throwable ignored) {
                            }

                            OfflineUser off_user = new OfflineUser(target);
                            if (off_user.exists()) {
                                HashSet<OfflineUser> detected = IPStorager.manager.getAlts(temp_module, player, off_user.getUUID());

                                if (detected != null) {
                                    new AltsAccountInventory(player, detected);
                                }
                            } else {
                                user.Message(messages.Prefix() + messages.NeverPlayed(target));
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
                Console.send(messages.Prefix() + messages.LookUpUsage());
            } else {
                if (args[0] != null) {
                    if (args.length == 1) {
                        String target = args[0];

                        TempModule temp_module = new TempModule();
                        ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
                        try {
                            if (!ModuleLoader.manager.isLoaded(temp_module)) {
                                spigot_module_loader.inject();
                            }
                        } catch (Throwable ignored) {
                        }

                        OfflineUser off_user = new OfflineUser(target);
                        if (off_user.exists()) {
                            HashSet<OfflineUser> detected = IPStorager.manager.getAlts(temp_module, null, off_user.getUUID());

                            if (detected != null) {
                                Console.send("&7------------ &eLockLogin alt accounts finder for " + target + " &7------------");
                                System.out.println("\n");
                                for (OfflineUser player : detected) {
                                    Console.send("&e" + player.getName());
                                    Console.send("  &7UUID: &e" + player.getUUID());
                                }
                            }
                        } else {
                            Console.send(messages.Prefix() + messages.NeverPlayed(target));
                        }
                    } else {
                        Console.send(messages.Prefix() + messages.LookUpUsage());
                    }
                } else {
                    Console.send(messages.Prefix() + messages.LookUpUsage());
                }
            }
        }
        return false;
    }
}
