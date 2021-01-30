package ml.karmaconfigs.lockloginmodules.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class Module {

    @NotNull
    public abstract JavaPlugin owner();

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
