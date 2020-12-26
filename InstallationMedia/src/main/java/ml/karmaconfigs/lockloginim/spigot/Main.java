package ml.karmaconfigs.lockloginim.spigot;

import ml.karmaconfigs.api.spigot.karmaserver.ServerVersion;
import ml.karmaconfigs.lockloginim.shared.BukkitManager;
import ml.karmaconfigs.lockloginim.shared.LockLoginType;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.logging.Level;

/*
LockLogin Installation Media

This tool will install LockLogin
reading from updates repository
hosted on github
 */
public final class Main extends JavaPlugin {

    private static LockLoginType type = LockLoginType.FAT;
    private static boolean install = true;

    @Override
    public synchronized final void onEnable() {
        try {
            print("&aInitializing LockLogin installation media...");
            if (getServer().getPluginManager().isPluginEnabled("LockLogin")) {
                print("&cLockLogin already found in the server, disabling and removing LockLogin installation media...");
                install = false;
                BukkitManager manager = new BukkitManager();
                manager.unload(this);
                File running_jar = new java.io.File(Main.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath().replaceAll("%20", " "));
                Files.delete(running_jar.toPath());
            } else {
                ServerVersion version = new ServerVersion(getServer());

                float v = version.getVersion();
                int v_int = Integer.parseInt(String.valueOf(v).replace(".", ""));

                if (v_int <= 112) {
                    print("&bLockLogin Installation Media initialized for Paper/Spigot legacy");
                } else {
                    print("&bLockLogin Installation Media initialized for Paper/Spigot 1.13+");
                }

                print("&aPlease write in the LockLogin version type you want to use");
                print("&aValid inputs are: &7B = Fat &b| &7L = Flat\n\n");

                print("&7Fat version contains dependencies built in the plugin file, which makes it");
                print("&7a bug-proof plugin, but it's a big plugin size\n");
                print("&fFlat version doesn't contains any dependency, but download them and inject them");
                print("&fwhich makes it lightweight, but the possibilities of bugs using this version is");
                print("&fbigger depending on the OS you are, your host, and the server.jar you are using\n\n");

                readForInput(v_int <= 112);
            }
        } catch (Throwable ex) {
            getServer().getLogger().log(Level.SEVERE, "ERROR WHILE ENABLING LOCKLOGIN INSTALLATION MEDIA", ex);
            System.exit(1);
        }
    }

    @Override
    public final void onDisable() {
        if (install) {
            File dest = new File(getDataFolder().getParentFile(), "LockLogin.jar");
            File running_jar = new java.io.File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());

            String url = null;
            switch (type) {
                case FAT:
                    getServer().getLogger().log(Level.INFO, "LockLogin fat installation selected, installing LockLogin");
                    url = "https://karmaconfigs.github.io/updates/LockLogin/LockLogin_fat.jar";
                    break;
                case FLAT:
                    getServer().getLogger().log(Level.INFO, "LockLogin flat installation selected, installing LockLogin");
                    url = "https://karmaconfigs.github.io/updates/LockLogin/LockLogin_flat.jar";
                    break;
                default:
                    getServer().getLogger().log(Level.SEVERE, "Invalid LockLogin installation specified...");
                    System.exit(1);
                    break;
            }

            try {
                URL dl_url = new URL(url);
                int count;
                URLConnection connection = dl_url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(dl_url.openStream(), 1024);
                OutputStream output = new FileOutputStream(dest);

                byte[] data = new byte[1024];

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                output.flush();

                output.close();
                input.close();

                BukkitManager manager = new BukkitManager();
                manager.unload(this);
                Files.delete(running_jar.toPath());
                manager.load(dest);
            } catch (Throwable ex) {
                getServer().getLogger().log(Level.SEVERE, "ERROR WHILE DOWNLOADING LOCKLOGIN", ex);
                System.exit(1);
            }
        }
    }

    private void print(final String message) {
        JavaPlugin.getProvidingPlugin(Main.class).getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void readForInput(boolean legacy) throws Throwable {
        if (legacy) {
            print("&cLEGACY MODE, YOU MUST TYPE THE INPUT TWICE THEN PRESS ENTER");
            String result = System.console().readLine().toLowerCase();

            if (result.equals("b") || result.equals("l")) {
                switch (result) {
                    case "l":
                        type = LockLoginType.FLAT;
                        break;
                    case "b":
                        type = LockLoginType.FAT;
                        break;
                }
                BukkitManager manager = new BukkitManager();
                manager.unload(this);
            } else {
                print("&cInvalid input &7( &f" + result + " &7)");
                print("&aValid inputs are: &7B = Fat &b| &7L = Flat\n\n");
                readForInput(true);
            }
        } else {
            int result = System.in.read();

            // l = 108
            // b = 98
            // L = 76
            // B = 66
            if (result == 108 || result == 76 || result == 98 || result == 66) {
                switch (result) {
                    case 108:
                    case 76:
                        type = LockLoginType.FLAT;
                        break;
                    case 98:
                    case 66:
                        type = LockLoginType.FAT;
                        break;
                }
                BukkitManager manager = new BukkitManager();
                manager.unload(this);
            } else {
                print("&cInvalid input &7( &f" + (char) result + " &7)");
                print("&aValid inputs are: &7B = Fat &b| &7L = Flat\n\n");
                readForInput(false);
            }
        }
    }
}
