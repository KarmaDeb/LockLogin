package ml.karmaconfigs.LockLogin.BungeeCord.Utils.Servers;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import net.md_5.bungee.api.config.ServerInfo;

public final class LobbyChecker implements LockLoginBungee, BungeeFiles {

    private static boolean MainWork = false;
    private static boolean AuthWork = false;
    private final String Main = config.MainLobby();
    private final String Auth = config.AuthLobby();
    private final String FBMain = config.FallBackMain();
    private final String FBAuth = config.FallBackAuth();
    private String MainName = "";
    private String AuthName = "";

    /**
     * Check the servers
     */
    public final void CheckServers() {
        if (plugin.getProxy().getServerInfo(Main) != null) {
            MainName = Main;
        } else {
            MainName = FBMain;
        }
        if (plugin.getProxy().getServerInfo(Auth) != null) {
            AuthName = Auth;
        } else {
            AuthName = FBAuth;
        }
    }

    /**
     * Get the main server name
     *
     * @return a String
     */
    public final String getMain() {
        CheckServers();
        if (!MainName.equals(AuthName)) {
            return MainName;
        } else {
            return "Lobby";
        }
    }

    /**
     * Get the auth server name
     *
     * @return a String
     */
    public final String getAuth() {
        CheckServers();
        if (!MainName.equals(AuthName)) {
            return AuthName;
        } else {
            return "AuthLobby";
        }
    }

    /**
     * Check if the main lobby
     * is even defined in BungeeCord's
     * config
     *
     * @return a boolean
     */
    public final boolean MainOk() {
        CheckServers();
        return plugin.getProxy().getServerInfo(getMain()) != null;
    }

    /**
     * Check if the auth lobby
     * is even defined in BungeeCord's
     * config
     *
     * @return a boolean
     */
    public final boolean AuthOk() {
        CheckServers();
        return plugin.getProxy().getServerInfo(getAuth()) != null;
    }

    /**
     * Check if the main server is working
     *
     * @return a boolean
     */
    public final boolean MainIsWorking() {
        CheckServers();
        if (MainOk()) {
            if (getMain() != null) {
                plugin.getProxy().getServers().get(getMain()).ping((result, error) -> {
                    MainWork = error == null;
                });
                return MainWork;
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
     * @return a boolean
     */
    public final boolean AuthIsWorking() {
        CheckServers();
        if (AuthOk()) {
            if (getAuth() != null) {
                plugin.getProxy().getServers().get(getAuth()).ping((result, error) -> {
                    AuthWork = error == null;
                });
                return AuthWork;
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
     * @return a ServerInfo
     */
    public final ServerInfo generateServerInfo(String name) {
        return plugin.getProxy().getServerInfo(name);
    }
}
