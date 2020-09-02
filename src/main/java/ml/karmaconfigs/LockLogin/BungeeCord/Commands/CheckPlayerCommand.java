package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.OfflineUser;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class CheckPlayerCommand extends Command implements LockLoginBungee, BungeeFiles {

    public CheckPlayerCommand() {
        super("playerinf", "", "playerinfo, playerinformation");
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final String checkPlayerInfo = "locklogin.playerinfo";
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (player.hasPermission(checkPlayerInfo)) {
                if (args.length == 1) {
                    String tar = args[0];

                    sendData(player, tar);
                } else {
                    user.Message(messages.Prefix() + messages.PlayerInfoUsage());
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError(checkPlayerInfo));
            }
        } else {
            if (args.length == 1) {
                String tar = args[0];

                sendData(tar);
            } else {
                out.Message(messages.Prefix() + messages.PlayerInfoUsage());
            }
        }
    }

    /**
     * Fill the player data
     *
     * @param tName the player
     */
    private void sendData(ProxiedPlayer issuer, String tName) {
        User user = new User(issuer);

        user.Message("&6&l&m------&r &eLockLogin &6&l&m------");
        user.Message(" ");
        user.Message("&ePlayer&7: &f" + tName);
        user.Message("&eUUID&7: &f" + getUUId(tName).toString());
        user.Message("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            ProxiedPlayer target = plugin.getProxy().getPlayer(tName);
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
            user.Message("&eFly&7: &f:BUNGEECORD");
            user.Message("&eGamemode&7: &fBUNGEECORD");
            user.Message("&eServer&7: &f" + target.getServer().getInfo().getName());
        } else {
            if (config.isYaml()) {
                ml.karmaconfigs.LockLogin.Spigot.Utils.User.OfflineUser targetUser = new ml.karmaconfigs.LockLogin.Spigot.Utils.User.OfflineUser(tName);

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
        out.Message("&6&l&m------&r &eLockLogin &6&l&m------");
        out.Message(" ");
        out.Message("&ePlayer&7: &f" + tName);
        out.Message("&eUUID&7: &f" + getUUId(tName).toString());
        out.Message("&eTrimmed&7: &f" + getUUId(tName).toString().replace("-", ""));
        if (isOnline(tName)) {
            ProxiedPlayer target = plugin.getProxy().getPlayer(tName);
            User targetUser = new User(target);

            out.Message("&eIP&7: &f" + target.getAddress().getAddress().getHostName());
            if (!targetUser.isLogged()) {
                if (targetUser.isRegistered()) {
                    out.Message("&eStatus&7: &cNot logged");
                } else {
                    out.Message("&eStatus&7: &cNot registered");
                }
            } else {
                if (targetUser.has2FA()) {
                    if (!targetUser.isTempLog()) {
                        out.Message("&eStatus&7: &aVerified with 2FA");
                    } else {
                        out.Message("&eStatus&7: &cNeeds 2FA");
                    }
                } else {
                    out.Message("&eStatus&7: &aVerified");
                }
            }
            out.Message("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                    .replace("false", "&cNo"));
            if (targetUser.has2FA()) {
                if (targetUser.getToken(false).isEmpty()) {
                    out.Message("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                } else {
                    out.Message("&eToken&7: &aTarget google token found");
                }
            }
            out.Message("&eFly&7: &7: &fBUNGEECORD");
            out.Message("&eGamemode&7: &fBUNGEECORD");
            out.Message("&eServer&7: &f" + target.getServer().getInfo().getName());
        } else {
            if (config.isYaml()) {
                OfflineUser targetUser = new OfflineUser(tName);

                if (targetUser.exists()) {
                    out.Message("&eIP&7: &cDISCONNECTED");
                    out.Message("&eStatus&7: &cNot logged");
                    out.Message("&e2FA&7: " + String.valueOf(targetUser.has2FA()).replace("true", "&aYes")
                            .replace("false", "&cNo"));
                    if (targetUser.has2FA()) {
                        if (targetUser.getToken().isEmpty()) {
                            out.Message("&eToken&7: &cNO GOOGLE TOKEN FOUND");
                        } else {
                            out.Message("&eToken&7: &aTarget google token found");
                        }
                    }
                    out.Message("&eFly&7: &fBUNGEECORD");
                    out.Message("&eGamemode&7: &fBUNGEECORD");
                    out.Message("&eServer&7: &cDISCONNECTED");
                } else {
                    out.Message(messages.Prefix() + messages.NeverPlayed(tName));
                }
            } else {
                Utils utils = new Utils(getUUId(tName));

                if (utils.userExists()) {
                    out.Message("&eIP&7: &cDISCONNECTED");
                    out.Message("&eStatus&7: &cNot logged");
                    out.Message("&e2FA&7: " + String.valueOf(utils.has2fa()).replace("true", "&aYes")
                            .replace("false", "&cNo"));
                    if (utils.has2fa()) {
                        if (utils.getToken() != null && !utils.getToken().isEmpty()) {
                            out.Message("&eToken&7: &aTarget token found");
                        } else {
                            out.Message("&eToken&7: &cNO TOKEN FOUND");
                        }
                    }
                    out.Message("&eFly&7: &fBUNGEECORD");
                    out.Message("&eGamemode&7: &fBUNGEECORD");
                    out.Message("&eServer&7: &cDISCONNECTED");
                } else {
                    out.Message(messages.Prefix() + messages.NeverPlayed(tName));
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
        return plugin.getProxy().getPlayer(name) != null && plugin.getProxy().getPlayer(name).isConnected();
    }

    /**
     * Get an uuid from the name
     *
     * @param name the name
     * @return an UUID
     */
    private UUID getUUId(String name) {
        if (plugin.getProxy().getPlayer(name) != null) {
            return plugin.getProxy().getPlayer(name).getUniqueId();
        } else {
            if (!plugin.getProxy().getConfig().isOnlineMode()) {
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
