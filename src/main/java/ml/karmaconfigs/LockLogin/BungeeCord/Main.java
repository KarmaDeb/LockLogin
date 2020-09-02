package ml.karmaconfigs.LockLogin.BungeeCord;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.ConfigGetter;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.PluginManager.LockLoginBungeeManager;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.PluginManagerBungee;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class Main extends Plugin {

    public static boolean updatePending;

    @Override
    public final void onEnable() {
        new InterfaceUtils().setMain(this);

        if (new ConfigGetter().UpdateSelf()) {
            String dir = getDataFolder().getPath().replaceAll("\\\\", "/");

            File pluginsFolder = new File(dir.replace("/LockLogin", ""));
            File updatedLockLogin = new File(pluginsFolder + "/update/", LockLoginBungee.getJarName());

            updatePending = updatedLockLogin.exists();
        }

        if (updatePending) {
            getProxy().getScheduler().schedule(this, () ->
                new LockLoginBungeeManager().applyUpdate(), 10, TimeUnit.SECONDS);
        } else {
            new PluginManagerBungee().enable();
        }

        Logger.log(Platform.BUNGEE, "INFO", "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        new PluginManagerBungee().disable();
        Logger.log(Platform.BUNGEE, "INFO", "LockLogin stopped");
    }
}
