package ml.karmaconfigs.LockLogin.Spigot.Utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.LastLocation;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory.PinInventory;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.BungeeVerifier;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

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
                String SubChannel = in.readUTF();

                if (SubChannel.equals("LoginData")) {
                    String[] data = in.readUTF().split(" ");
                    String id = data[0];
                    boolean value = Boolean.parseBoolean(data[1]);

                    UUID uuid = UUID.fromString(id);

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
                } else {
                    if (SubChannel.equals("VerifyUUID")) {
                        String id = in.readUTF();
                        UUID uuid = UUID.fromString(id);
                        BungeeVerifier verifier = new BungeeVerifier(uuid);

                        verifier.setVerified(true);
                    } else {
                        if (SubChannel.equals("OpenPin")) {
                            String id = in.readUTF();
                            UUID uuid = UUID.fromString(id);

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
                        } else {
                            if (SubChannel.equals("ClosePin")) {
                                UUID uuid = UUID.fromString(in.readUTF());

                                if (plugin.getServer().getPlayer(uuid) != null) {
                                    PinInventory inventory = new PinInventory(player);
                                    inventory.setVerified(true);
                                    inventory.close();

                                    inventoryAccess.remove(player);
                                }
                            } else {
                                if (SubChannel.equals("EffectManager")) {
                                    String[] data = in.readUTF().split("_");
                                    UUID uuid = UUID.fromString(data[0]);
                                    boolean apply = Boolean.parseBoolean(data[1]);

                                    if (plugin.getServer().getPlayer(uuid) != null) {
                                        player = plugin.getServer().getPlayer(uuid);
                                        User user = new User(player);

                                        if (apply) {
                                            user.saveCurrentEffects();
                                            user.applyBlindEffect();
                                        } else {
                                            user.removeBlindEffect();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                Logger.log(Platform.SPIGOT, "ERROR WHILE READING PLUGIN MESSAGE FROM BUNGEECORD", e);
            }
        }
    }
}
