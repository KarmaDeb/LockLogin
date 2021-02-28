package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.api.spigot.Console;
import ml.karmaconfigs.lockloginmodules.spigot.ModuleLoader;
import ml.karmaconfigs.lockloginsystem.shared.IpData;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import ml.karmaconfigs.lockloginsystem.shared.llsql.AccountMigrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Migrate;
import ml.karmaconfigs.lockloginsystem.shared.llsql.Utils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.IPStorager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.FileManager;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PreJoinRelated implements Listener, LockLoginSpigot, SpigotFiles {

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onLogin(PlayerLoginEvent e) {
        if (!config.isBungeeCord()) {
            if (e.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    Player player = e.getPlayer();
                    User user = new User(player);

                    TempModule temp_module = new TempModule();
                    ModuleLoader spigot_module_loader = new ModuleLoader(temp_module);
                    try {
                        if (!ModuleLoader.manager.isLoaded(temp_module)) {
                            spigot_module_loader.inject();
                        }
                    } catch (Throwable ignored) {
                    }

                    if (config.maxRegister() > 0) {
                        try {
                            IPStorager storager = new IPStorager(temp_module, e.getAddress());
                            if (storager.canJoin(player.getUniqueId(), config.maxRegister())) {
                                storager.save(player.getUniqueId());

                                if (storager.hasAltAccounts(player.getUniqueId())) {
                                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                                        if (online.hasPermission("locklogin.playerinfo") && !online.getUniqueId().equals(player.getUniqueId()))
                                            user.Message(messages.Prefix() + messages.altFound(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName(), storager.getAltsAmount(player.getUniqueId())));
                                    }
                                }
                            } else {
                                user.Kick("&eLockLogin\n\n" + messages.MaxRegisters());
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    if (config.AccountsPerIp() != 0) {
                        IpData data = new IpData(temp_module, e.getAddress());
                        data.fetch(Platform.SPIGOT);

                        if (data.getConnections() > config.AccountsPerIp()) {
                            user.Kick("&eLockLogin\n\n" + messages.MaxIp());
                        } else {
                            if (!player.isBanned()) {
                                data.addIP();
                            }
                        }
                    }

                    if (config.isMySQL()) {
                        if (!user.isRegistered()) {
                            if (config.registerRestricted()) {
                                user.Kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                                return;
                            }
                        }

                        String UUID = player.getUniqueId().toString().replace("-", "");

                        FileManager manager = new FileManager(UUID + ".yml", "playerdata");
                        manager.setInternal("auto-generated/userTemplate.yml");

                        Utils sql = new Utils(player.getUniqueId());
                        sql.createUser();

                        if (manager.getManaged().exists()) {
                            if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                                if (manager.isSet("Password")) {
                                    if (!manager.isEmpty("Password")) {
                                        new AccountMigrate(sql, Migrate.MySQL, Platform.SPIGOT);
                                        Console.send(plugin, messages.Migrating(player.getUniqueId().toString()), Level.INFO);
                                    }
                                }
                            }
                        }

                        if (sql.getName() == null || sql.getName().isEmpty())
                            sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());
                    }
                });
            }
        }
    }
}
