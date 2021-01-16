package ml.karmaconfigs.lockloginsystem.bungeecord.events;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.Main;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Checker;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPreJoin(PreLoginEvent e) {
        BFSystem bf_prevention = new BFSystem(e.getConnection().getVirtualHost().getAddress());
        if (bf_prevention.isBlocked() && config.BFMaxTries() > 0) {
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft()))));
        } else {
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
                        TempModule temp_module = new TempModule();
                        try {
                            ModuleLoader loader = new ModuleLoader(temp_module);
                            if (!ModuleLoader.manager.isLoaded(temp_module)) {
                                loader.inject();
                            }
                        } catch (Throwable ignored) {
                        }

                        IpData data = new IpData(temp_module, e.getConnection().getAddress().getAddress());
                        data.fetch(Platform.BUNGEE);

                        if (data.getConnections() > config.AccountsPerIp()) {
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
                        TempModule temp_module = new TempModule();
                        try {
                            ModuleLoader loader = new ModuleLoader(temp_module);
                            if (!ModuleLoader.manager.isLoaded(temp_module)) {
                                loader.inject();
                            }
                        } catch (Throwable ignored) {
                        }

                        try {
                            IPStorager storager = new IPStorager(temp_module, e.getConnection().getAddress().getAddress());

                            if (config.MaxRegisters() > 0) {
                                try {
                                    if (storager.canJoin(e.getConnection().getUniqueId(), config.MaxRegisters())) {
                                        storager.save(e.getConnection().getUniqueId());
                                    } else {
                                        e.setCancelled(true);
                                        e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.MaxRegisters())));
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            } else {
                e.setCancelled(true);
                e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + "&cPlugin update in queue, please wait...")));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onJoin(ServerConnectEvent e) {
        ProxiedPlayer player = e.getPlayer();

        User user = new User(player);

        if (config.isYaml()) {
            user.setupFile();
        } else {
            String UUID = player.getUniqueId().toString().replace("-", "");
            FileManager manager = new FileManager(UUID + ".yml", "playerdata");
            manager.setInternal("auto-generated/userTemplate.yml");

            Utils sql = new Utils(player.getUniqueId());

            sql.createUser();

            if (manager.getManaged().exists()) {
                if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                    if (manager.isSet("Password")) {
                        if (!manager.isEmpty("Password")) {
                            new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                            Console.send(plugin, messages.Migrating(player.getUniqueId().toString()), Level.INFO);
                        }
                    }
                }
            }

            if (sql.getName() == null || sql.getName().isEmpty())
                sql.setName(player.getName());
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPostJoin(PostLoginEvent e) {
        if (e.getPlayer() != null) {
            ProxiedPlayer player = e.getPlayer();
            User user = new User(player);

            if (!user.isLogged()) {
                plugin.getProxy().getScheduler().schedule(plugin, () -> {
                    user.checkServer();
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
