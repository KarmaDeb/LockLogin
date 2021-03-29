package ml.karmaconfigs.lockloginsystem.bungeecord.utils.servers;

import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter;
import net.md_5.bungee.api.config.ServerInfo;

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
public final class LobbyChecker implements LockLoginBungee {

    private final static ConfigGetter config = new ConfigGetter();

    private static boolean mainWorking = false;
    private static boolean authWorking = false;
    private final String main = config.getMainLobby();
    private final String auth = config.getAuthLobby();
    private final String fbMain = config.getFallBackMain();
    private final String fbAuth = config.getFallBackAuth();
    private String mainName = "";
    private String authName = "";

    /**
     * Check the servers
     */
    public final void checkServers() {
        if (plugin.getProxy().getServerInfo(main) != null) {
            mainName = main;
        } else {
            mainName = fbMain;
        }
        if (plugin.getProxy().getServerInfo(auth) != null) {
            authName = auth;
        } else {
            authName = fbAuth;
        }

        if (generateServerInfo(authName) != null)
            plugin.getProxy().getServers().get(authName).ping((result, error) -> authWorking = error == null);

        if (generateServerInfo(mainName) != null)
            plugin.getProxy().getServers().get(mainName).ping((result, error) -> mainWorking = error == null);
    }

    /**
     * Get the main server name
     *
     * @return the main server name
     */
    public final String getMain() {
        checkServers();
        if (!mainName.equals(authName)) {
            return mainName;
        } else {
            return "Lobby";
        }
    }

    /**
     * Get the auth server name
     *
     * @return the auth server name
     */
    public final String getAuth() {
        checkServers();
        if (!mainName.equals(authName)) {
            return authName;
        } else {
            return "AuthLobby";
        }
    }

    /**
     * Check if the main lobby
     * is even defined in BungeeCord's
     * config
     *
     * @return if the main server is valid
     */
    public final boolean mainOk() {
        checkServers();
        return plugin.getProxy().getServerInfo(getMain()) != null;
    }

    /**
     * Check if the auth lobby
     * is even defined in BungeeCord's
     * config
     *
     * @return if the auth server is valid
     */
    public final boolean authOk() {
        checkServers();
        return plugin.getProxy().getServerInfo(getAuth()) != null;
    }

    /**
     * Check if the main server is working
     *
     * @return if the main server is working
     */
    public final boolean mainWorking() {
        checkServers();
        if (mainOk()) {
            if (getMain() != null) {
                return mainWorking;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the auth server is working
     *
     * @return if the auth server is working
     */
    public final boolean authWorking() {
        checkServers();
        if (authOk()) {
            if (getAuth() != null) {
                return authWorking;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Generate a server info from name
     *
     * @param name the name
     * @return the server info of the specified server name
     */
    public final ServerInfo generateServerInfo(String name) {
        return plugin.getProxy().getServerInfo(name);
    }
}
