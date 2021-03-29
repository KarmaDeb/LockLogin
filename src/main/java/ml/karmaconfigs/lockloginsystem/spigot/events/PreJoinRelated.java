package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.bukkit.Console;
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

                    try {
                        IPStorager storager = new IPStorager(temp_module, e.getAddress());

                        if (config.maxRegister() > 0) {
                            if (storager.canJoin(player.getUniqueId(), e.getAddress(), config.maxRegister())) {
                                storager.save(player.getUniqueId(), player.getName());

                                if (storager.hasAltAccounts(player.getUniqueId(), e.getAddress())) {
                                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                                        if (online.hasPermission("locklogin.playerinfo") && !online.getUniqueId().equals(player.getUniqueId()))
                                            user.send(messages.prefix() + messages.altFound(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName(), storager.getAltsAmount(player.getUniqueId(), e.getAddress())));
                                    }
                                }
                            } else {
                                user.kick("&eLockLogin\n\n" + messages.maxRegisters());
                            }
                        } else {
                            storager.save(player.getUniqueId(), player.getName());
                        }
                    } catch (Throwable ignored) {}

                    if (config.accountsPerIp() != 0) {
                        IpData data = new IpData(temp_module, e.getAddress());
                        data.fetch(Platform.BUKKIT);

                        if (data.getConnections() > config.accountsPerIp()) {
                            user.kick("&eLockLogin\n\n" + messages.maxIp());
                        } else {
                            if (!player.isBanned()) {
                                data.addIP();
                            }
                        }
                    }

                    if (config.isMySQL()) {
                        if (!user.isRegistered()) {
                            if (config.registerRestricted()) {
                                user.kick("&eLockLogin\n\n" + messages.onlyAzuriom());
                                return;
                            }
                        }

                        String UUID = player.getUniqueId().toString().replace("-", "");
                        FileManager manager = new FileManager(UUID + ".yml", "playerdata");
                        Utils sql = new Utils(player.getUniqueId(), plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());

                        manager.setInternal("auto-generated/userTemplate.yml");

                        if (manager.getManaged().exists()) {
                            if (!sql.userExists())
                                sql.createUser();

                            if (sql.getName() == null || sql.getName().isEmpty())
                                sql.setName(plugin.getServer().getOfflinePlayer(player.getUniqueId()).getName());

                            if (sql.getPassword() == null || sql.getPassword().isEmpty()) {
                                if (manager.isSet("Password")) {
                                    if (!manager.isEmpty("Password")) {
                                        AccountMigrate migrate = new AccountMigrate(sql, Migrate.MySQL, Platform.BUKKIT);
                                        migrate.start();

                                        Console.send(plugin, messages.migratingAccount(player.getUniqueId().toString()), Level.INFO);
                                        manager.delete();
                                    }
                                }
                            }
                        }
                    }
                    //user.setupFile()
                });
            }
        }
    }
}
