package ml.karmaconfigs.lockloginsystem.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import javax.inject.Inject;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Velocity main
 */
@Plugin(
        id = "locklogin",
        name = "LockLogin",
        version = "1.0.9.6",
        description = "Adds an extra protection to your Bukkit-Spigot-Paper/BungeeCord/Velocity server with this plugins that adds 2FA to his security system",
        authors = {"KarmaConfigs", "KarmaDev"})
public final class Main {

    private final ProxyServer server;
    private final Path dataFolder;

    @Inject
    public Main(final ProxyServer proxySV, final Logger logger, final Path pluginDir) {
        server = proxySV;
        dataFolder = pluginDir;

        logger.info("Preparing LockLogin for velocity...");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            Files.createDirectories(dataFolder);
        } catch (Throwable ignored) {}

        server.getConsoleCommandSource().sendMessage(
                TextComponent.
                        ofChildren(Component
                                .text()
                                .content("LockLogin is not ready to run in velocity, coming soon")
                                .color(TextColor.color(Color.CYAN.getRed(), Color.CYAN.getGreen(), Color.CYAN.getBlue()))
                ));
    }
}
