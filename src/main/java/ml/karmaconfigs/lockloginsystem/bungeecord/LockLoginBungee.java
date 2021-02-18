package ml.karmaconfigs.lockloginsystem.bungeecord;

import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.shared.StringUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.BungeeSender;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.servers.LobbyChecker;

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

public interface LockLoginBungee {

    Main plugin = new InterfaceUtils().getPlugin();

    String name = new InterfaceUtils().getName();
    String version = new InterfaceUtils().getVersion();
    String jar = new java.io.File(Main.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath())
            .getName();

    Integer versionID = Integer.parseInt(StringUtils.stripColor(version)
            .replaceAll("[aA-zZ]", "")
            .replace(".", "")
            .replace(" ", ""));

    Logger logger = new Logger(plugin);

    BungeeSender dataSender = new BungeeSender();
    LobbyChecker lobbyCheck = new LobbyChecker();
}
