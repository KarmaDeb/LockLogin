package ml.karmaconfigs.lockloginsystem.spigot.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import ml.karmaconfigs.lockloginsystem.spigot.utils.datafiles.LastLocation;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.AltsAccountInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.ModuleListInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import ml.karmaconfigs.lockloginsystem.spigot.utils.reader.BungeeModuleReader;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.BungeeVerifier;
import ml.karmaconfigs.lockloginsystem.spigot.utils.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
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

public final class BungeeListener implements PluginMessageListener, LockLoginSpigot, SpigotFiles {

    public static ArrayList<Player> inventoryAccess = new ArrayList<>();

    /**
     * When a plugin message is received
     *
     * @param channel the message channel
     * @param player  the player
     * @param message the message (in bytes)
     */
    @Override
    @SuppressWarnings("all")
    public final void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("ll:info")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            try {
                String subchannel = in.readUTF();

                String[] data;
                String id;
                UUID uuid;
                switch (subchannel.toLowerCase()) {
                    case "logindata":
                        data = in.readUTF().split(" ");
                        id = data[0];
                        boolean value = Boolean.parseBoolean(data[1]);

                        uuid = UUID.fromString(id);

                        if (plugin.getServer().getPlayer(uuid) != null) {
                            player = plugin.getServer().getPlayer(uuid);
                            User user = new User(player);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (user.getLogStatus() != value) {
                                        user.setLogStatus(value);
                                    } else {
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 20);

                            if (value) {
                                if (config.TakeBack()) {
                                    LastLocation lastLoc = new LastLocation(player);
                                    user.Teleport(lastLoc.getLastLocation());
                                }
                            }
                        }
                        break;
                    case "verifyuuid":
                        id = in.readUTF();
                        uuid = UUID.fromString(id);
                        BungeeVerifier verifier = new BungeeVerifier(uuid);

                        verifier.setVerified(true);
                        break;
                    case "openpin":
                        id = in.readUTF();
                        uuid = UUID.fromString(id);

                        if (plugin.getServer().getPlayer(uuid) != null) {
                            player = plugin.getServer().getPlayer(uuid);
                            PinInventory inventory = new PinInventory(player);

                            if (!inventoryAccess.contains(player)) {
                                inventoryAccess.add(player);
                                inventory.open();
                            } else {
                                inventory.updateInput();
                            }
                        }
                        break;
                    case "closepin":
                        uuid = UUID.fromString(in.readUTF());

                        if (plugin.getServer().getPlayer(uuid) != null) {
                            PinInventory inventory = new PinInventory(player);
                            inventory.setVerified(true);
                            inventory.close();

                            inventoryAccess.remove(player);
                        }
                        break;
                    case "effectmanager":
                        data = in.readUTF().split("_");
                        uuid = UUID.fromString(data[0]);
                        boolean apply = Boolean.parseBoolean(data[1]);
                        boolean nausea = Boolean.parseBoolean(data[2]);

                        if (plugin.getServer().getPlayer(uuid) != null) {
                            player = plugin.getServer().getPlayer(uuid);
                            User user = new User(player);

                            if (apply) {
                                user.saveCurrentEffects();
                                user.applyBlindEffect(nausea);
                            } else {
                                user.removeBlindEffect(nausea);
                            }
                        }
                        break;
                    case "lookupgui":
                        data = in.readUTF().split(";");
                        uuid = UUID.fromString(data[0]);

                        HashSet<UUID> uuids = new HashSet<>();
                        for (int i = 0; i < data.length; i++) {
                            try {
                                uuids.add(UUID.fromString(data[i]));
                            } catch (Throwable ignored) {}
                        }

                        AltsAccountInventory alts = new AltsAccountInventory(uuid, uuids);
                        alts.openPage(0);
                        break;
                    case "modulesinfodata":
                        String msg = in.readUTF();
                        data = msg.split("\\{");
                        uuid = UUID.fromString(data[0]);

                        String modules_data = msg.replace(data[0] + "{", "").replace("}", "");
                        BungeeModuleReader reader = new BungeeModuleReader(modules_data);
                        reader.parse();

                        if (plugin.getServer().getPlayer(uuid) != null) {
                            player = plugin.getServer().getPlayer(uuid);

                            ModuleListInventory listInventory = new ModuleListInventory(player);
                            listInventory.openPage(0);
                        }
                        break;
                    default:
                        //UNKNOWN PLUGIN MESSAGE...
                        break;
                }
            } catch (Throwable e) {
                logger.scheduleLog(Level.GRAVE, e);
                logger.scheduleLog(Level.INFO, "Error while reading plugin message from BungeeCord");
            }
        }
    }
}