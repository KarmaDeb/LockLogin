package ml.karmaconfigs.LockLogin.BungeeCord;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.BungeeSender;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Console;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Servers.LobbyChecker;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.StringUtils;

public interface LockLoginBungee {

    Main plugin = new InterfaceUtils().getPlugin();
    String name = new InterfaceUtils().getName();
    String version = new InterfaceUtils().getVersion();
    Console out = new Console();
    Integer versionID = Integer.parseInt(StringUtils.stripColor(version)
            .replaceAll("[aA-zZ]", "")
            .replace(".", "")
            .replace(" ", ""));
    LobbyChecker lobbyCheck = new LobbyChecker();
    BungeeSender dataSender = new BungeeSender();

    static String getJarName() {
        return new java.io.File(Main.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }
}
