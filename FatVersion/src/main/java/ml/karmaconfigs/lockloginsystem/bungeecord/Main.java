package ml.karmaconfigs.lockloginsystem.bungeecord;

import ml.karmaconfigs.api.KarmaPlugin;
import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.api.bungee.Logger;
import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.PluginManagerBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.pluginmanager.LockLoginBungeeManager;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.Platform;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.concurrent.TimeUnit;

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

@KarmaPlugin(plugin_name = "LockLogin", plugin_version = "1.0.2.8", plugin_update_url = "https://karmaconfigs.github.io/updates/LockLogin/latest.txt")
public final class Main extends Plugin {

    public static boolean updatePending;

    @Override
    public final void onEnable() {
        new InterfaceUtils().setMain(this);

        CurrentPlatform current = new CurrentPlatform();
        current.setRunning(Platform.BUNGEE);

        Console.setInfoPrefix(this, "&8[ &eLockLogin &8] &7INFO &f>> &b");
        Console.setWarningPrefix(this, "&8[ &eLockLogin &8] &6WARNING &f>> &e");
        Console.setGravePrefix(this, "&8[ &eLockLogin &8] &4GRAVE &f>> &c");

        if (new ConfigGetter().UpdateSelf()) {
            String dir = getDataFolder().getPath().replaceAll("\\\\", "/");

            File pluginsFolder = new File(dir.replace("/LockLogin", ""));
            File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.jar);

            updatePending = updatedLockLogin.exists();
        }
        if (updatePending) {
            getProxy().getScheduler().schedule(this, () ->
                    new LockLoginBungeeManager().applyUpdate(), 10, TimeUnit.SECONDS);
        } else {
            new PluginManagerBungee().enable();
        }

        Logger logger = new Logger(this);
        logger.scheduleLog(Level.GRAVE, "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        new PluginManagerBungee().disable();
        LockLoginBungee.logger.scheduleLog(Level.INFO, "LockLogin disabled");
    }
}
