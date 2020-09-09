package ml.karmaconfigs.LockLogin.BungeeCord.Events;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Main;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileCreator;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.FileManager;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.StartCheck;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import ml.karmaconfigs.LockLogin.CheckType;
import ml.karmaconfigs.LockLogin.IPStorage.IPStorager;
import ml.karmaconfigs.LockLogin.IpData;
import ml.karmaconfigs.LockLogin.MySQL.AccountMigrate;
import ml.karmaconfigs.LockLogin.MySQL.Migrate;
import ml.karmaconfigs.LockLogin.MySQL.Utils;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Security.Checker;
import ml.karmaconfigs.LockLogin.WarningLevel;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

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

public final class JoinRelated implements Listener, LockLoginBungee, BungeeFiles {

    /**
     * This event will be executed before the player
     * joins the server, so he can detect if the
     * player is already playing
     *
     * @param e the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPreJoin(PreLoginEvent e) {
        if (!Main.updatePending) {
            if (config.CheckNames()) {
                if (!Checker.isValid(e.getConnection().getName())) {
                    e.setCancelled(true);
                    e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.IllegalName(Checker.getIllegalChars(e.getConnection().getName())))));
                }
            }

            if (plugin.getProxy().getPlayer(e.getConnection().getUniqueId()) != null) {
                e.setCancelled(true);
                e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.AlreadyPlaying())));
            }

            if (!e.isCancelled()) {
                if (config.AccountsPerIp() != 0) {
                    IpData data = new IpData(e.getConnection().getAddress().getAddress());
                    data.fetch(Platform.BUNGEE);

                    if (data.getConnections() + 1 > config.AccountsPerIp()) {
                        e.setCancelled(true);
                        e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.MaxIp())));
                    } else {
                        plugin.getProxy().getScheduler().schedule(plugin, () -> {
                            if (plugin.getProxy().getPlayer(e.getConnection().getUniqueId()) != null) {
                                if (plugin.getProxy().getPlayer(e.getConnection().getUniqueId()).isConnected()) {
                                    data.addIP();
                                }
                            }
                        }, 1, TimeUnit.SECONDS);
                    }
                }

                if (!e.isCancelled()) {
                    IPStorager storager = new IPStorager(e.getConnection().getAddress().getAddress());

                    if (config.MaxRegisters() != 0) {
                        try {
                            if (storager.getStorage().size() >= config.MaxRegisters()) {
                                if (storager.notSet(e.getConnection().getName())) {
                                    e.setCancelled(true);
                                    e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.MaxRegisters())));
                                }
                            } else {
                                if (storager.notSet(e.getConnection().getName())) {
                                    storager.saveStorage(e.getConnection().getName());
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } else {
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + "&cPlugin update in queue, please wait...")));
        }
    }

    /**
     * This event will be executed when a player
     * connects the server, so the plugin will be
     * able to redirect that connection to the
     * auth server
     *
     * @param e the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onJoin(ServerConnectEvent e) {
        ProxiedPlayer player = e.getPlayer();

        User user = new User(player);

        if (config.isYaml()) {
            user.setupFile();
        } else {
            String UUID = player.getUniqueId().toString().replace("-", "");
            FileManager manager = new FileManager(UUID + ".yml", "playerdata");
            FileCreator pf = new FileCreator(UUID + ".yml", "playerdata", false);

            Utils sql = new Utils(player.getUniqueId());

            sql.createUser();

            if (pf.exists()) {
                if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                    if (manager.isSet("Password")) {
                        if (!manager.isEmpty("Password")) {
                            new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                            out.Alert(messages.Migrating(player.getUniqueId().toString()), WarningLevel.WARNING);
                        }
                    }
                }
            }
        }

        if (!user.isLogged()) {
            user.checkServer();
        } else {
            if (config.EnableMain()) {
                if (lobbyCheck.MainOk() && lobbyCheck.MainIsWorking()) {
                    if (e.getReason().equals(ServerConnectEvent.Reason.COMMAND)) {
                        if (e.getTarget().getName().equals(lobbyCheck.getAuth())) {
                            e.setTarget(lobbyCheck.generateServerInfo(lobbyCheck.getMain()));
                            e.setCancelled(true);
                            user.Message(messages.Prefix() + messages.AlreadyLogged());
                        }
                    }
                }
            }
        }
    }

    /**
     * This event will be executed when a player
     * joins the server, so the plugin will be
     * able to execute actions over the player
     *
     * @param e the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPostJoin(PostLoginEvent e) {
        if (e.getPlayer() != null) {
            ProxiedPlayer player = e.getPlayer();
            User user = new User(player);

            if (!user.isLogged()) {
                plugin.getProxy().getScheduler().schedule(plugin, () -> {
                    if (config.ClearChat()) {
                        for (int i = 0; i < 150; i++) {
                            user.Message(" ");
                        }
                    }
                    if (!user.isRegistered()) {
                        new StartCheck(player, CheckType.REGISTER);
                    } else {
                        new StartCheck(player, CheckType.LOGIN);
                    }
                }, 1, TimeUnit.SECONDS);
            }

            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                dataSender.sendAccountStatus(player);
                dataSender.sendUUID(player.getUniqueId(), player.getServer());
            }, 1, TimeUnit.SECONDS);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onServerSwitch(ServerSwitchEvent e) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            ProxiedPlayer player = e.getPlayer();

            dataSender.sendAccountStatus(player);
            dataSender.sendUUID(e.getPlayer().getUniqueId(), e.getPlayer().getServer());
        }, 1, TimeUnit.SECONDS);
    }
}
