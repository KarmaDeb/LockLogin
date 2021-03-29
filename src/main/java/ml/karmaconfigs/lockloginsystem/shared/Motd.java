package ml.karmaconfigs.lockloginsystem.shared;

import ml.karmaconfigs.api.common.GlobalKarmaFile;

import java.io.File;
import java.util.List;

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

        return file.getInt("DELAY", 5);
    }

    /**
     * Get the on login message
     *
     * @param player     the player name
     * @param serverName the server name
     * @return the on login motd
     */
    public final String onLogin(final String player, final String serverName) {
        setup();

        String path = file.getString("OnLogin", "MESSAGES");

        System.out.println("Path: " + path);

        List<String> messages = file.getStringList(path, "&7Welcome&e {player}&7 to&b {ServerName}", " ", "&7Hope you got a nice day!");
        System.out.println("Found messages: " + messages);

        for (int i = 0; i < messages.size(); i++)
            messages.set(i, messages.get(i)
                    .replace("{player}", player)
                    .replace("{ServerName}", serverName)
                    .replace("[", "{open}")
                    .replace("]", "{close}")
                    .replace(",", "{comma}"));

        return messages.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "{newline}")
                .replace("{open}", "[")
                .replace("{close}", "]")
                .replace("{comma}", ",");
    }

    /**
     * Get the on register message
     *
     * @param player     the player name
     * @param serverName the server name
     * @return the on login motd
     */
    public final String onRegister(final String player, final String serverName) {
        setup();

        String path = file.getString("OnRegister", "MESSAGES");

        List<String> messages = file.getStringList(path, "&7Welcome&e {player}&7 to&b {ServerName}", " ", "&7Hope you got a nice day!");
        for (int i = 0; i < messages.size(); i++)
            messages.set(i, messages.get(i)
                    .replace("{player}", player)
                    .replace("{ServerName}", serverName)
                    .replace("[", "{open}")
                    .replace("]", "{close}")
                    .replace(",", "{comma}"));

        return messages.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "{newline}")
                .replace("{open}", "[")
                .replace("{close}", "]")
                .replace("{comma}", ",");
    }
}
