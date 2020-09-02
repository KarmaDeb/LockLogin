package ml.karmaconfigs.LockLogin.Spigot.Utils;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public final class BungeeSender implements LockLoginSpigot {

    private final Player player;

    /**
     * Initialize the bungee sender class
     *
     * @param player the player
     */
    public BungeeSender(Player player) {
        this.player = player;
    }

    /**
     * Send the pin input to BungeeCord
     *
     * @param input the pin input
     */
    public final void sendPinInput(int input) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("PinInput_" + player.getUniqueId().toString() + "_" + input);
        } catch (Throwable e) {
            Logger.log(Platform.SPIGOT, "ERROR WHILE SENDING PIN INPUT TO BUNGEECORD", e);
        }
        player.sendPluginMessage(plugin, "ll:info", b.toByteArray());
    }
}
