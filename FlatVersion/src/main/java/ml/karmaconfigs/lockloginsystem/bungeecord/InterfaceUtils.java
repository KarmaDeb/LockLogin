package ml.karmaconfigs.lockloginsystem.bungeecord;

import ml.karmaconfigs.lockloginsystem.bungeecord.utils.StringUtils;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class InterfaceUtils {

    private static Main plugin;

    /**
     * Start the interface utils
     *
     * @param instance a Main instance
     */
    public final void setMain(Main instance) {
        plugin = instance;
    }

    /**
     * Get a Main instance
     *
     * @return the plugin BungeeCord main instance
     */
    public final Main getPlugin() {
        return plugin;
    }

    /**
     * Get the plugin name
     *
     * @return the plugin name
     */
    public final String getName() {
        return StringUtils.toColor("&c[ &4GSA &c] &eLockLogin");
    }

    /**
     * Get the plugin version
     *
     * @return the plugin version
     */
    public final String getVersion() {
        return StringUtils.toColor("&c" + plugin.getDescription().getVersion());
    }

    /**
     * Get the plugin version as
     * integer
     *
     * @return the plugin version ID
     */
    @Deprecated
    public final int getVersionID() {
        return Integer.parseInt(plugin.getDescription().getVersion()
                .replaceAll("[aA-zZ]", "")
                .replace(".", "")
                .replace(" ", ""));
    }
}
