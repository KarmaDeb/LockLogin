package ml.karmaconfigs.lockloginsystem.bukkit.commands;

import ml.karmaconfigs.lockloginmodules.bukkit.PluginModule;
import ml.karmaconfigs.lockloginsystem.bukkit.LockLoginSpigot;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

class TempModule extends PluginModule {

    @Override
    public @NotNull JavaPlugin owner() {
        return LockLoginSpigot.plugin;
    }

    @Override
    public @NotNull String name() {
        return "Command temp module";
    }

    @Override
    public @NotNull String version() {
        return "1.0.0";
    }

    @Override
    public @NotNull String author() {
        return "KarmaDev";
    }

    @Override
    public @NotNull String description() {
        return "This module is used to access an API feature when using /lookup command";
    }

    @Override
    @Deprecated
    public @NotNull String update_url() {
        return "https://karmaconfigs.ml/";
    }
}
