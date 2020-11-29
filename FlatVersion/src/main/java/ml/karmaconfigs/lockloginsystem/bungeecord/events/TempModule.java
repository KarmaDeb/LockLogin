package ml.karmaconfigs.lockloginsystem.bungeecord.events;

import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class TempModule extends Module {

    @Override
    public @NotNull Plugin owner() {
        return LockLoginBungee.plugin;
    }

    @Override
    public @NotNull String name() {
        return "Event temp module";
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
        return "This module is used to access an API feature";
    }

    @Override
    public @NotNull String author_url() {
        return "https://karmaconfigs.ml/";
    }
}
