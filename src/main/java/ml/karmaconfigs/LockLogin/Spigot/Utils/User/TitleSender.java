package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Objects;

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

public final class TitleSender implements LockLoginSpigot {

    private final Player player;

    /**
     * Initialize the titles class
     *
     * @param player the player
     */
    public TitleSender(Player player) {
        this.player = player;
    }

    /**
     * Gets the server version
     *
     * @return the version
     */
    private String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    /**
     * Gets a NMS class
     *
     * @param name the class name
     * @return the NMS class
     */
    private Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch (Throwable e) {
            Logger.log(Platform.SPIGOT, "ERROR", e);
            return null;
        }
    }

    /**
     * Send the actionbar
     *
     * @param title the title
     * @param subtitle the subtitle
     */
    public void sendTitle(TitleFormat title, String subtitle) {
        if (title == null) title = new TitleFormat("");
        if (subtitle == null) subtitle = "";

        if (getVersion().contains("1_8") || getVersion().contains("1_9") || getVersion().contains("1_10") || getVersion().contains("1_11")) {
            try {
                Object chatTitle = Objects.requireNonNull(getNMSClass("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class)
                        .invoke(null, "{\"text\": \"" + StringUtils.toColor(title.getText()) + "\"}");
                Constructor<?> titleConstructor = Objects.requireNonNull(getNMSClass("PacketPlayOutTitle")).getConstructor(
                        Objects.requireNonNull(getNMSClass("PacketPlayOutTitle")).getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                        int.class, int.class, int.class);
                Object packet = titleConstructor.newInstance(
                        Objects.requireNonNull(getNMSClass("PacketPlayOutTitle")).getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
                        title.getDisplay(), title.getKeep(), title.getHide());

                Object chatsTitle = Objects.requireNonNull(getNMSClass("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class)
                        .invoke(null, "{\"text\": \"" + StringUtils.toColor(subtitle) + "\"}");
                Constructor<?> timingTitleConstructor = Objects.requireNonNull(getNMSClass("PacketPlayOutTitle")).getConstructor(
                        Objects.requireNonNull(getNMSClass("PacketPlayOutTitle")).getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                        int.class, int.class, int.class);
                Object timingPacket = timingTitleConstructor.newInstance(
                        Objects.requireNonNull(getNMSClass("PacketPlayOutTitle")).getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle,
                        title.getDisplay(), title.getKeep(), title.getHide());

                Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

                playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
                playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, timingPacket);
            } catch (Exception e) {
                Logger.log(Platform.SPIGOT, "ERROR", e);
            }
        } else {
            player.sendTitle(title.getText(), subtitle, title.getDisplay(), title.getKeep(), title.getHide());
        }
    }
}
