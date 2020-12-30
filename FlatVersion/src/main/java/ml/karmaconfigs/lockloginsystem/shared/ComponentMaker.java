package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.spigot.utils.files.SpigotFiles;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class ComponentMaker {

    private static TextComponent component;

    /**
     * Initialize the component maker
     *
     * @param text the text
     */
    public ComponentMaker(String text) {
        component = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
    }

    /**
     * Set the component hover text
     *
     * @param text the text
     */
    public final void setHoverText(String text) {
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text))));
    }

    /**
     * Set the component click event
     *
     * @param event the click event
     */
    public final void setClickEvent(ClickEvent event) {
        component.setClickEvent(event);
    }

    /**
     * Return the google auth qr code web url with already
     * replaced strings
     *
     * @param player the player
     * @param token  google auth token
     * @return The google auth qr url
     */
    public final String getURL(Player player, String token) {
        return "https://karmaconfigs.ml/qr/?" + player.getName() + "%20" + "(" + SpigotFiles.config.ServerName() + ")?" + token;
    }

    /**
     * Return the google auth qr code web url with already
     * replaced strings
     *
     * @param player the player
     * @param token  google auth token
     * @return The google auth qr url
     */
    public final String getURL(ProxiedPlayer player, String token) {
        return "https://karmaconfigs.ml/qr/?" + player.getName() + "%20" + "(" + BungeeFiles.config.ServerName() + ")?" + token;
    }

    /**
     * Get the component
     *
     * @return the message component
     */
    public final TextComponent getComponent() {
        return component;
    }
}
