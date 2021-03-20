package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.OfflineUser;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.apache.commons.io.IOUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

public final class CheckPlayerCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private final Permission checkPlayerInfo = new Permission("locklogin.playerinfo", PermissionDefault.FALSE);

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull final Command cmd, @NotNull final String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (player.hasPermission(checkPlayerInfo)) {
                if (args.length == 1) {
                    String tar = args[0];

                    sendData(player, tar);
                } else {
                    user.send(messages.prefix() + messages.playerInfoUsage());
                }
            } else {
                user.send(messages.prefix() + messages.permission(checkPlayerInfo.getName()));
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                sendData(tar);
            } else {
                Console.send(messages.prefix() + messages.playerInfoUsage());
            }
        }
        return false;
    }

    /**
     * Fill the player data
     *
     * @param tName the player
     */
    private void sendData(Player issuer, String tName) {
        User user = new User(issuer);

        user.send("&6&l&m------&r &eLockLogin &6&l&m------");
        user.send(" ");
        user.send("&ePlayer&7: &f" + tName);
        user.send("&eUUID&7: &f" + getUUId(tName).toString());
        user.send("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            Player target = plugin.getServer().getPlayer(tName);
            User targetUser = new User(target);

            user.send("&eIP&7: &f" + target.getAddress().getAddress().getHostName());
            if (!targetUser.isLogged()) {
                if (targetUser.isRegistered()) {
                    user.send("&eStatus&7: &cNot logged");
                } else {
                    user.send("&eStatus&7: &cNot registered");
                }
            } else {
                if (targetUser.has2FA()) {
                    if (!targetUser.isTempLog()) {
                        user.send("&eStatus&7: &aVerified with 2FA");
                    } else {
                        user.send("&eStatus&7: &cNeeds 2FA");
                    }
                } else {
                    user.send("&eStatus&7: &aVerified");
                }
            }
            user.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                    .replace("false", "&cNo"));
            if (targetUser.has2FA()) {
                if (targetUser.getToken(false).isEmpty()) {
                    user.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                } else {
                    user.send("&eToken&7: &aTarget google token found");
                }
            }
            user.send("&eFly&7: &f" + targetUser.hasFly());
            String mode = target.getGameMode().name().substring(0, 1).toUpperCase() + target.getGameMode().name().substring(1).toLowerCase();
            user.send("&eGamemode&7: &f" + mode);
            user.send("&eServer&7: &f" + target.getServer().getName());
        } else {
            OfflineUser targetUser = new OfflineUser("", tName, true);

            if (targetUser.exists()) {
                user.send("&eIP&7: &cDISCONNECTED");
                user.send("&eStatus&7: &cNot logged");
                user.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                        .replace("false", "&cNo"));
                if (targetUser.has2FA()) {
                    if (targetUser.getToken().isEmpty()) {
                        user.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                    } else {
                        user.send("&eToken&7: &aTarget google token found");
                    }
                }
                user.send("&eFly&7: &f" + targetUser.hasFly());
                user.send("&eGamemode&7: &cDISCONNECTED");
                user.send("&eServer&7: &cDISCONNECTED");
            } else {
                user.send(messages.prefix() + messages.unknownPlayer(tName));
            }
        }
    }

    /**
     * Fill the player data
     *
     * @param tName the player
     */
    private void sendData(String tName) {
        Console.send("&6&l&m------&r &eLockLogin &6&l&m------");
        Console.send(" ");
        Console.send("&ePlayer&7: &f" + tName);
        Console.send("&eUUID&7: &f" + getUUId(tName).toString());
        Console.send("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            Player target = plugin.getServer().getPlayer(tName);
            User targetUser = new User(target);

            Console.send("&eIP&7: &f" + target.getAddress().getAddress().getHostName());
            if (!targetUser.isLogged()) {
                if (targetUser.isRegistered()) {
                    Console.send("&eStatus&7: &cNot logged");
                } else {
                    Console.send("&eStatus&7: &cNot registered");
                }
            } else {
                if (targetUser.has2FA()) {
                    if (!targetUser.isTempLog()) {
                        Console.send("&eStatus&7: &aVerified with 2FA");
                    } else {
                        Console.send("&eStatus&7: &cNeeds 2FA");
                    }
                } else {
                    Console.send("&eStatus&7: &aVerified");
                }
            }
            Console.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                    .replace("false", "&cNo"));
            if (targetUser.has2FA()) {
                if (targetUser.getToken(false).isEmpty()) {
                    Console.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                } else {
                    Console.send("&eToken&7: &aTarget google token found");
                }
            }
            Console.send("&eFly&7: &7: &f" + targetUser.hasFly());
            String mode = target.getGameMode().name().substring(0, 1).toUpperCase() + target.getGameMode().name().substring(1).toLowerCase();
            Console.send("&eGamemode&7: &f" + mode);
            Console.send("&eServer&7: &f" + target.getServer().getName());
        } else {
            OfflineUser targetUser = new OfflineUser("", tName, true);

            if (targetUser.exists()) {
                Console.send("&eIP&7: &cDISCONNECTED");
                Console.send("&eStatus&7: &cNot logged");
                Console.send("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                        .replace("false", "&cNo"));
                if (targetUser.has2FA()) {
                    if (targetUser.getToken().isEmpty()) {
                        Console.send("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                    } else {
                        Console.send("&eToken&7: &aTarget google token found");
                    }
                }
                Console.send("&eFly&7: &f" + targetUser.hasFly());
                Console.send("&eGamemode&7: &cDISCONNECTED");
                Console.send("&eServer&7: &cDISCONNECTED");
            } else {
                Console.send(messages.prefix() + messages.unknownPlayer(tName));
            }
        }
    }

    /**
     * Check if the target is online
     *
     * @param name the player name
     * @return a boolean
     */
    private boolean isOnline(String name) {
        return plugin.getServer().getPlayer(name) != null && plugin.getServer().getPlayer(name).isOnline();
    }

    /**
     * Get an uuid from the name
     *
     * @param name the name
     * @return an UUID
     */
    private UUID getUUId(String name) {
        if (plugin.getServer().getPlayer(name) != null) {
            return plugin.getServer().getPlayer(name).getUniqueId();
        } else {
            if (!plugin.getServer().getOnlineMode()) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            } else {
                return mojangUUID(name);
            }
        }
    }

    /**
     * Get the mojang player uuid
     *
     * @param name the player name
     * @return an uuid
     */
    private UUID mojangUUID(String name) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

            String UUIDJson = IOUtils.toString(new URL(url));

            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);

            return UUID.fromString(UUIDObject.get("id").toString());
        } catch (Throwable e) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
    }
}
