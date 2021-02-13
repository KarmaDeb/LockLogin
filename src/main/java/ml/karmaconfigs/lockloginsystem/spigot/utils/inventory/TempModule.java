package ml.karmaconfigs.lockloginsystem.spigot.utils.inventory;

import ml.karmaconfigs.lockloginmodules.spigot.Module;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

class TempModule extends Module {

    @Override
    public @NotNull JavaPlugin owner() {
        return LockLoginSpigot.plugin;
    }

    @Override
    public @NotNull String name() {
        return "Pin inventory temp module";
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
        return "This module is used to access an API feature when a player verifies through PIN GUI";
    }

    @Override
    public @NotNull String author_url() {
        return "https://karmaconfigs.ml/";
    }
}
