package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public final class TitleSender implements LockLoginSpigot {

    private final Player player;
    private final String Title;
    private final String Subtitle;

    /**
     * Setup the title sender
     *
     * @param player   the player
     * @param Title    the title
     * @param Subtitle the subtitle
     */
    public TitleSender(Player player, String Title, String Subtitle) {
        this.player = player;
        this.Title = StringUtils.toColor(Title);
        this.Subtitle = StringUtils.toColor(Subtitle);
    }

    /**
     * Just, send the title
     */
    public final void send() {
        try {
            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + Title + "\"}");
            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class);
            Object packet = titleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
                    20 * 2, 20 * 5, 20 * 2);

            Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + Subtitle + "\"}");
            Constructor<?> stitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class);
            Object spacket = stitleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle,
                    20 * 2, 20 * 5, 20 * 2);

            sendPacket(packet);
            sendPacket(spacket);
        } catch (Exception ex) {
            out.Alert("An error occurred while sending a title for " + player.getName(), WarningLevel.ERROR);
            out.Message("&c" + ex.fillInStackTrace());
        }
    }

    /**
     * Send a title to player
     *
     * @param fadeInTime  The time the title takes to fade in
     * @param showTime    The time the title is displayed
     * @param fadeOutTime The time the title takes to fade out
     */
    @Deprecated
    public final void sendWithTiming(int fadeInTime, int showTime, int fadeOutTime) {
        try {
            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + Title + "\"}");
            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class);
            Object packet = titleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle,
                    20 * fadeInTime, 20 * showTime, 20 * fadeOutTime);

            Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + Subtitle + "\"}");
            Constructor<?> stitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                    int.class, int.class, int.class);
            Object spacket = stitleConstructor.newInstance(
                    getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle,
                    20 * fadeInTime, 20 * showTime, 20 * fadeOutTime);

            sendPacket(packet);
            sendPacket(spacket);
        } catch (Exception ex) {
            out.Alert("An error occurred while sending a title for " + player.getName(), WarningLevel.ERROR);
            out.Message("&c" + ex.fillInStackTrace());
        }
    }

    /**
     * Send the packet to the player
     *
     * @param packet the packet
     */
    private void sendPacket(Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception ex) {
            out.Alert("An error occurred while trying to send the packet " + packet.toString() + " to " + player.getName(), WarningLevel.ERROR);
            out.Message("&c" + ex.fillInStackTrace());
        }
    }

    /**
     * Get NMS class using reflection
     *
     * @param name Name of the class
     * @return Class
     */
    private Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server."
                    + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException ex) {
            out.Alert("An error occurred while trying to get NMS class " + name, WarningLevel.ERROR);
            out.Message("&c" + ex.fillInStackTrace());
            return null;
        }
    }
}
