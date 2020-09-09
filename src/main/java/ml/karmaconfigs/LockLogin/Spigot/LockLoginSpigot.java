package ml.karmaconfigs.LockLogin.Spigot;

import ml.karmaconfigs.LockLogin.Spigot.Utils.Console;
import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;

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
