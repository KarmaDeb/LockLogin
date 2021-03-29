package ml.karmaconfigs.lockloginsystem.bungeecord.events;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.common.StringUtils;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.StartCheck;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import ml.karmaconfigs.lockloginsystem.shared.CaptchaType;
import ml.karmaconfigs.lockloginsystem.shared.CheckType;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.ipstorage.BFSystem;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Checker;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
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
    public final void onPreJoin(LoginEvent e) {
        PendingConnection connection = e.getConnection();
        InetAddress ip = User.external.getIp(connection.getSocketAddress());

        BFSystem bf_prevention = new BFSystem(ip);
        if (bf_prevention.isBlocked() && config.bfMaxTries() > 0) {
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.ipBlocked(bf_prevention.getBlockLeft()))));
        } else {
            if (config.checkNames()) {
                if (Checker.notValid(connection.getName())) {
                    e.setCancelled(true);
                    e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.illegalName(Checker.getIllegalChars(connection.getName())))));
                }
            }

            if (config.alreadyPlaying()) {
                if (plugin.getProxy().getPlayer(connection.getUniqueId()) != null) {
                    e.setCancelled(true);
                    e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.alreadyPlaying())));
                }
            }

            if (!e.isCancelled()) {
                if (config.accountsPerIP() != 0) {
                    TempModule temp_module = new TempModule();
                    try {
                        ModuleLoader loader = new ModuleLoader(temp_module);
                        if (!ModuleLoader.manager.isLoaded(temp_module)) {
                            loader.inject();
                        }
                    } catch (Throwable ignored) {
                    }

                    IpData data = new IpData(temp_module, ip);
                    data.fetch(Platform.BUNGEE);

                    if (data.getConnections() > config.accountsPerIP()) {
                        e.setCancelled(true);
                        e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.maxIP())));
                    } else {
                        plugin.getProxy().getScheduler().schedule(plugin, () -> {
                            if (plugin.getProxy().getPlayer(connection.getUniqueId()) != null) {
                                if (plugin.getProxy().getPlayer(connection.getUniqueId()).isConnected()) {
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
                        IPStorager storager = new IPStorager(temp_module, ip);

                        if (config.maxRegister() > 0) {
                            try {
                                if (storager.canJoin(connection.getUniqueId(), ip, config.maxRegister())) {
                                    storager.save(connection.getUniqueId(), connection.getName());

                                    if (storager.hasAltAccounts(connection.getUniqueId(), ip)) {
                                        for (ProxiedPlayer online : plugin.getProxy().getPlayers()) {
                                            User user = new User(online);

                                            if (online.hasPermission("locklogin.playerinfo") && !online.getUniqueId().equals(connection.getUniqueId()))
                                                user.send(messages.prefix() + messages.altsFound(connection.getName(), storager.getAltsAmount(connection.getUniqueId(), ip)));
                                        }
                                    }
                                } else {
                                    e.setCancelled(true);
                                    e.setCancelReason(TextComponent.fromLegacyText(StringUtils.toColor("&eLockLogin\n\n" + messages.maxRegisters())));
                                }
                            } catch (Throwable ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            storager.save(connection.getUniqueId(), connection.getName());
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onJoin(ServerConnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        User user = new User(player);

        if (config.isMySQL()) {
            Utils sql = new Utils(player);

            if (!sql.userExists()) {
                if (config.registerRestricted()) {
                    user.kick(messages.onlyAzuriom());

                    return;
                }
            }

            String UUID = player.getUniqueId().toString().replace("-", "");
            FileManager manager = new FileManager(UUID + ".yml", "playerdata");
            manager.setInternal("auto-generated/userTemplate.yml");

            if (manager.getManaged().exists()) {
                if (!sql.userExists())
                    sql.createUser();

                if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                    if (manager.isSet("Password")) {
                        if (!manager.isEmpty("Password")) {
                            AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.BUNGEE);
                            migrate.start();

                            Console.send(plugin, messages.migrating(player.getUniqueId().toString()), Level.INFO);
                            manager.delete();
                        }
                    }
                }

                if (sql.getName() == null || sql.getName().isEmpty())
                    sql.setName(player.getName());
            }
        } else {
            user.setupFile();
        }

        if (user.isLogged()) {
            if (e.getTarget().getName().equals(lobbyCheck.getAuth())) {
                if (lobbyCheck.mainOk() && lobbyCheck.mainWorking()) {
                    switch (e.getReason()) {
                        case UNKNOWN:
                        case PLUGIN:
                        case PLUGIN_MESSAGE:
                        case COMMAND:
                            e.setCancelled(true);
                            user.send(messages.prefix() + messages.alreadyLogged());
                            break;
                    }
                }
            }
        } else {
            if (!e.getTarget().getName().equals(lobbyCheck.getAuth())) {
                if (lobbyCheck.authOk() && lobbyCheck.authWorking()) {
                    switch (e.getReason()) {
                        case UNKNOWN:
                        case PLUGIN:
                        case PLUGIN_MESSAGE:
                        case COMMAND:
                            e.setCancelled(true);
                            if (user.isRegistered())
                                if (user.isTempLog())
                                    if (user.has2FA())
                                        user.send(messages.prefix() + messages.gAuthenticate());
                                    else
                                        user.send(messages.prefix() + messages.login(user.getCaptcha()));
                                else
                                    user.send(messages.prefix() + messages.register(user.getCaptcha()));

                            break;
                    }
                }
            }
        }

        dataSender.sendUUID(e.getPlayer().getUniqueId(), e.getPlayer().getServer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onPostJoin(PostLoginEvent e) {
        if (e.getPlayer() != null) {
            ProxiedPlayer player = e.getPlayer();
            User user = new User(player);

            if (!user.isLogged()) {
                user.genCaptcha();

                if (config.clearChat()) {
                    for (int i = 0; i < 150; i++) {
                        user.send(" ");
                    }
                }

                if (config.getCaptchaType().equals(CaptchaType.COMPLEX) && user.hasCaptcha()) {
                    if (config.getCaptchaTimeOut() > 0) {
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            int back = config.getCaptchaTimeOut();

                            @Override
                            public void run() {
                                if (back == 0 || !player.isConnected()) {
                                    cancel();

                                    if (player.isConnected())
                                        user.kick("&eLockLogin\n\n" + messages.captchaTimeOut());

                                    if (!user.isRegistered()) {
                                        FileManager manager = new FileManager(player.getUniqueId().toString().replace("-", "") + ".yml", "playerdata");
                                        manager.delete();

                                        user.remove();
                                    }
                                }

                                if (!user.hasCaptcha())
                                    cancel();

                                back--;
                            }
                        }, 0, 1000);
                    }
                } else {
                    if (user.isRegistered())
                        new StartCheck(player, CheckType.LOGIN);
                    else
                        new StartCheck(player, CheckType.REGISTER);
                }
            }

            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                dataSender.sendAccountStatus(player);
                dataSender.sendUUID(player.getUniqueId(), player.getServer());
                dataSender.sendBungeeCordMessages(player);

                user.checkServer();
            }, 1, TimeUnit.SECONDS);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onServerSwitch(ServerSwitchEvent e) {
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            ProxiedPlayer player = e.getPlayer();

            dataSender.sendAccountStatus(player);
            dataSender.sendUUID(e.getPlayer().getUniqueId(), e.getPlayer().getServer());
            dataSender.sendBungeeCordMessages(player);
        }, 1, TimeUnit.SECONDS);
    }
}
