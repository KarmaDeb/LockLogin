package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

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

public final class ResetLastLoc implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private final Permission resetLastLoc = new Permission("locklogin.resetlocations", PermissionDefault.FALSE);

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (player.hasPermission(resetLastLoc)) {
                if (args.length == 0) {
                    user.Message(messages.Prefix() + messages.RestLastLocUsage());
                } else {
                    if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("@me")) {
                        if (args[0].equalsIgnoreCase("all")) {
                            FileManager manager = new FileManager("locations.yml", "userdata");
                            manager.delete();

                            user.Message(messages.Prefix() + messages.LocationsReset());
                        }
                        if (args[0].equalsIgnoreCase("@me")) {
                            LastLocation lastLocation = new LastLocation(player);

                            lastLocation.removeLocation();

                            user.Message(messages.Prefix() + messages.LocationReset(player));
                        } else {
                            String target = args[0];

                            LastLocation lastLocation = new LastLocation(target);

                            if (lastLocation.hasLastLocation()) {

                                lastLocation.removeLocation();

                                if (plugin.getServer().getPlayer(target) != null) {
                                    Player tar = plugin.getServer().getPlayer(target);

                                    user.Message(messages.Prefix() + messages.LocationReset(tar));
                                } else {
                                    user.Message(messages.Prefix() + messages.LocationReset(target));
                                }
                            } else {
                                if (plugin.getServer().getPlayer(target) != null) {
                                    Player tar = plugin.getServer().getPlayer(target);

                                    user.Message(messages.Prefix() + messages.NoLastLocation(tar));
                                } else {
                                    user.Message(messages.Prefix() + messages.NoLastLocation(target));
                                }
                            }
                        }
                    }
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError(resetLastLoc.getName()));
            }
        } else {
            if (args.length == 0) {
                out.Message(messages.Prefix() + messages.RestLastLocUsage());
            } else {
                if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("@me")) {
                    if (args[0].equalsIgnoreCase("all")) {
                        FileManager manager = new FileManager("locations.yml", "userdata");
                        manager.delete();

                        out.Message(messages.Prefix() + messages.LocationsReset());
                    }
                    if (args[0].equalsIgnoreCase("@me")) {
                        out.Alert("Console server doesn't have last location!", WarningLevel.ERROR);
                    } else {
                        String target = args[0];

                        LastLocation lastLocation = new LastLocation(target);

                        if (lastLocation.hasLastLocation()) {

                            lastLocation.removeLocation();

                            if (plugin.getServer().getPlayer(target) != null) {
                                Player tar = plugin.getServer().getPlayer(target);

                                out.Message(messages.Prefix() + messages.LocationReset(tar));
                            } else {
                                out.Message(messages.Prefix() + messages.LocationReset(target));
                            }
                        } else {
                            if (plugin.getServer().getPlayer(target) != null) {
                                Player tar = plugin.getServer().getPlayer(target);

                                out.Message(messages.Prefix() + messages.NoLastLocation(tar));
                            } else {
                                out.Message(messages.Prefix() + messages.NoLastLocation(target));
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
