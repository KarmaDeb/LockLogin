package ml.karmaconfigs.lockloginsystem.spigot.commands;

import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
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
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (player.hasPermission(checkPlayerInfo)) {
                if (args.length == 1) {
                    String tar = args[0];

                    sendData(player, tar);
                } else {
                    user.Message(messages.Prefix() + messages.PlayerInfoUsage());
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError(checkPlayerInfo.getName()));
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                sendData(tar);
            } else {
                Console.send(messages.Prefix() + messages.PlayerInfoUsage());
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

        user.Message("&6&l&m------&r &eLockLogin &6&l&m------");
        user.Message(" ");
        user.Message("&ePlayer&7: &f" + tName);
        user.Message("&eUUID&7: &f" + getUUId(tName).toString());
        user.Message("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            Player target = plugin.getServer().getPlayer(tName);
            User targetUser = new User(target);

            user.Message("&eIP&7: &f" + target.getAddress().getAddress().getHostName());
            if (!targetUser.isLogged()) {
                if (targetUser.isRegistered()) {
                    user.Message("&eStatus&7: &cNot logged");
                } else {
                    user.Message("&eStatus&7: &cNot registered");
                }
            } else {
                if (targetUser.has2FA()) {
                    if (!targetUser.isTempLog()) {
                        user.Message("&eStatus&7: &aVerified with 2FA");
                    } else {
                        user.Message("&eStatus&7: &cNeeds 2FA");
                    }
                } else {
                    user.Message("&eStatus&7: &aVerified");
                }
            }
            user.Message("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                    .replace("false", "&cNo"));
            if (targetUser.has2FA()) {
                if (targetUser.getToken(false).isEmpty()) {
                    user.Message("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                } else {
                    user.Message("&eToken&7: &aTarget google token found");
                }
            }
            user.Message("&eFly&7: &f" + targetUser.hasFly());
            String mode = target.getGameMode().name().substring(0, 1).toUpperCase() + target.getGameMode().name().substring(1).toLowerCase();
            user.Message("&eGamemode&7: &f" + mode);
            user.Message("&eServer&7: &f" + target.getServer().getName());
        } else {
            if (config.isYaml()) {
                OfflineUser targetUser = new OfflineUser(tName);

                if (targetUser.exists()) {
                    user.Message("&eIP&7: &cDISCONNECTED");
                    user.Message("&eStatus&7: &cNot logged");
                    user.Message("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                            .replace("false", "&cNo"));
                    if (targetUser.has2FA()) {
                        if (targetUser.getToken().isEmpty()) {
                            user.Message("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                        } else {
                            user.Message("&eToken&7: &aTarget google token found");
                        }
                    }
                    user.Message("&eFly&7: &f" + targetUser.hasFly());
                    user.Message("&eGamemode&7: &cDISCONNECTED");
                    user.Message("&eServer&7: &cDISCONNECTED");
                } else {
                    user.Message(messages.Prefix() + messages.NeverPlayed(tName));
                }
            } else {
                Utils utils = new Utils(getUUId(tName));

                if (utils.userExists()) {
                    user.Message("&eIP&7: &cDISCONNECTED");
                    user.Message("&eStatus&7: &cNot logged");
                    user.Message("&e2FA&7: " + String.valueOf(utils.has2fa()).replace("true", "&aYes")
                            .replace("false", "&cNo"));
                    if (utils.has2fa()) {
                        if (utils.getToken() != null && !utils.getToken().isEmpty()) {
                            user.Message("&eToken&7: &aTarget token found");
                        } else {
                            user.Message("&eToken&7: &cNO TOKEN FOUND");
                        }
                    }
                    user.Message("&eFly&7: &f" + utils.hasFly());
                    user.Message("&eGamemode&7: &cDISCONNECTED");
                    user.Message("&eServer&7: &cDISCONNECTED");
                } else {
                    user.Message(messages.Prefix() + messages.NeverPlayed(tName));
                }
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
            if (config.isYaml()) {
                OfflineUser targetUser = new OfflineUser(tName);

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
                    Console.send(messages.Prefix() + messages.NeverPlayed(tName));
                }
            } else {
                Utils utils = new Utils(getUUId(tName));

                if (utils.userExists()) {
                    Console.send("&eIP&7: &cDISCONNECTED");
                    Console.send("&eStatus&7: &cNot logged");
                    Console.send("&e2FA&7: " + String.valueOf(utils.has2fa()).replace("true", "&aYes")
                            .replace("false", "&cNo"));
                    if (utils.has2fa()) {
                        if (utils.getToken() != null && !utils.getToken().isEmpty()) {
                            Console.send("&eToken&7: &aTarget token found");
                        } else {
                            Console.send("&eToken&7: &cNO TOKEN FOUND");
                        }
                    }
                    Console.send("&eFly&7: &f" + utils.hasFly());
                    Console.send("&eGamemode&7: &cDISCONNECTED");
                    Console.send("&eServer&7: &cDISCONNECTED");
                } else {
                    Console.send(messages.Prefix() + messages.NeverPlayed(tName));
                }
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
