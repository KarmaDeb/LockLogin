package ml.karmaconfigs.lockloginsystem.bungee;

import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.common.utils.FileUtilities;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungee.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungee.utils.servers.LobbyChecker;

import java.io.File;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public interface LockLoginBungee {

    /**
     * LockLogin main instance
     */
    Main plugin = new InterfaceUtils().getPlugin();

    /**
     * Plugin name
     */
    String name = new InterfaceUtils().getName();

    /**
     * Plugin version
     */
    String version = new InterfaceUtils().getVersion();

    /**
     * Get the plugin jar file
     *
     * @return the plugin jar file
     */
    static File getJar() {
        String jar = new java.io.File(Main.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();

        File pluginsFolder = FileUtilities.getPluginsFolder();
        if (!jar.contains(pluginsFolder.getAbsolutePath()) || !jar.contains(pluginsFolder.getAbsolutePath().replaceAll("\\\\", "/")))
            return new File(FileUtilities.getPluginsFolder(), jar);
        else
            return new File(jar);
    }

    /**
     * Get the version ID
     */
    Integer versionID = Integer.parseInt(StringUtils.stripColor(version)
            .replaceAll("[aA-zZ]", "")
            .replace(".", "")
            .replace(" ", ""));

    /**
     * Get the plugin custom logger
     */
    Logger logger = new Logger(plugin);

    /**
     * Plugin bungeecord message sender
     */
    BungeeSender dataSender = new BungeeSender();

    /**
     * Plugin server status checker
     */
    LobbyChecker lobbyCheck = new LobbyChecker();
}
