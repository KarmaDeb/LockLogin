package ml.karmaconfigs.LockLogin.Spigot;

import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.Spigot.Utils.PluginManagerSpigot;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public final void onEnable() {
        new PluginManagerSpigot().enable();
        Logger.log(Platform.SPIGOT, "INFO", "LockLogin initialized");
    }

    @Override
    public final void onDisable() {
        new PluginManagerSpigot().disable();
        Logger.log(Platform.SPIGOT, "INFO", "LockLogin stopped");
    }
}
