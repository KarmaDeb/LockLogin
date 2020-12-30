package ml.karmaconfigs.lockloginmodules.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class Module {

    @NotNull
    public abstract Plugin owner();

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String version();

    @NotNull
    public abstract String author();

    @NotNull
    public abstract String description();

    @NotNull
    public abstract String author_url();
}
