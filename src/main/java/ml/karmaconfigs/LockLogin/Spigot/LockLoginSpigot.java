package ml.karmaconfigs.LockLogin.Spigot;

import ml.karmaconfigs.LockLogin.Spigot.Utils.Console;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;

public interface LockLoginSpigot {

    Main plugin = (Main) JavaPlugin.getProvidingPlugin(Main.class);
    String name = StringUtils.toColor("&c[ &4GSA &c] &eLockLogin");
    String version = StringUtils.toColor("&c" + plugin.getDescription().getVersion());
    Integer versionID = Integer.parseInt(StringUtils.stripColor(version)
            .replaceAll("[aA-zZ]", "")
            .replace(".", "")
            .replace(" ", ""));
    Console out = new Console();

    static String getJarName() {
        return new java.io.File(Main.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }
}
