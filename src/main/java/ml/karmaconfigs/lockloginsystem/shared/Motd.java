package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.api.shared.GlobalKarmaFile;

import java.io.File;
import java.util.List;

public final class Motd {

    private final GlobalKarmaFile file;

    /**
     * Initialize the motd
     *
     * @param motd_file the motd file to read from
     */
    public Motd(final File motd_file) {
        file = new GlobalKarmaFile(motd_file);
    }

    /**
     * Setup the motd file
     */
    public final void setup() {
        if (!file.exists()) {
            file.setBroadcastOptions(true, true);

            file.create();
            file.exportFromFile(getClass().getResourceAsStream("/auto-generated/motd.locklogin"));
        }

        file.applyKarmaAttribute();
    }

    /**
     * Check if the motd is enabled
     *
     * @return if the motd is enabled
     */
    public final boolean isEnabled() {
        setup();

        return file.getBoolean("ENABLED", false);
    }

    /**
     * Get the motd delay
     *
     * @return the motd send delay
     */
    public final int getDelay() {
        setup();

        return file.getInt("DElAY", 5);
    }

    /**
     * Get the on login message
     *
     * @param player the player name
     * @param serverName the server name
     *
     * @return the on login motd
     */
    public final List<String> onLogin(final String player, final String serverName) {
        setup();

        String path = file.getString("OnLogin", "MESSAGES");

        List<String> messages = file.getStringList(path, "&7Welcome&e {player}&7 to&b {ServerName}", " ", "&7Hope you got a nice day!");
        for (int i = 0; i < messages.size(); i++)
            messages.set(i, messages.get(i).replace("{player}", player).replace("{ServerName}", serverName));

        return messages;
    }

    /**
     * Get the on register message
     *
     * @param player the player name
     * @param serverName the server name
     *
     * @return the on login motd
     */
    public final List<String> onRegister(final String player, final String serverName) {
        setup();

        String path = file.getString("OnRegister", "MESSAGES");

        List<String> messages = file.getStringList(path, "&7Welcome&e {player}&7 to&b {ServerName}", " ", "&7Hope you got a nice day!");
        for (int i = 0; i < messages.size(); i++)
            messages.set(i, messages.get(i).replace("{player}", player).replace("{ServerName}", serverName));

        return messages;
    }
}
