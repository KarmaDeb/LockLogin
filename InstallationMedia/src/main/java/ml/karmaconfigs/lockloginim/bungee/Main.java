package ml.karmaconfigs.lockloginim.bungee;

import ml.karmaconfigs.lockloginim.shared.BungeeManager;
import ml.karmaconfigs.lockloginim.shared.LockLoginType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.logging.Level;

/*
LockLogin Installation Media

This tool will install LockLogin
reading from updates repository
hosted on github
 */
public final class Main extends Plugin {

    private static LockLoginType type = LockLoginType.FAT;
    private static boolean install = true;
    private static boolean downloading = false;

    @Override
    public synchronized final void onEnable() {
        print("&aInitializing LockLogin installation media...");
        if (getProxy().getPluginManager().getPlugin("LockLogin") != null) {
            print("&cLockLogin already found in the server, disabling and removing LockLogin installation media...");
            install = false;
            getProxy().getScheduler().runAsync(this, () -> {
                try {
                    BungeeManager manager = new BungeeManager();
                    manager.unload(this);
                    File running_jar = new java.io.File(Main.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .getPath().replaceAll("%20", " "));
                    Files.delete(running_jar.toPath());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
        } else {
            print("&aPlease write in the LockLogin version type you want to use");
            print("&aValid inputs are: &7B = Fat &b| &7L = Flat\n\n");

            print("&7Fat version contains dependencies built in the plugin file, which makes it");
            print("&7a bug-proof plugin, but it's a big plugin size\n");
            print("&fFlat version doesn't contains any dependency, because it will download and inject them");
            print("&fwhich makes it lightweight, LockLogin installation media will be replaced soon with");
            print("&fLockLogin flat\n\n");

            readForInput();
        }
    }

    @Override
    public final void onDisable() {
        if (install && !downloading) {
            File dest = new File(getDataFolder().getParentFile(), "LockLogin.jar");
            File running_jar = new java.io.File(Main.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());

            String url = null;
            switch (type) {
                case FAT:
                    getProxy().getLogger().log(Level.INFO, "LockLogin fat installation selected, installing LockLogin");
                    url = "https://karmaconfigs.github.io/updates/LockLogin/LockLogin_fat.jar";
                    break;
                case FLAT:
                    getProxy().getLogger().log(Level.INFO, "LockLogin flat installation selected, installing LockLogin");
                    url = "https://karmaconfigs.github.io/updates/LockLogin/LockLogin_flat.jar";
                    break;
                default:
                    getProxy().getLogger().log(Level.SEVERE, "Invalid LockLogin installation specified...");
                    System.exit(1);
                    break;
            }

            try {
                downloading = true;
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

                BungeeManager manager = new BungeeManager();
                manager.unload(this);
                Files.delete(running_jar.toPath());
                manager.load(dest);
            } catch (Throwable ex) {
                getProxy().getLogger().log(Level.SEVERE, "ERROR WHILE DOWNLOADING LOCKLOGIN", ex);
                System.exit(1);
            }
        }
    }

    private void print(final String message) {
        ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    private void readForInput() {
        Scanner scanner = new Scanner(System.in);
        String result = scanner.nextLine().toLowerCase();

        if (result.equals("b") || result.equals("l")) {
            switch (result) {
                case "l":
                    type = LockLoginType.FLAT;
                    break;
                case "b":
                    type = LockLoginType.FAT;
                    break;
            }
            BungeeManager manager = new BungeeManager();
            manager.unload(this);
        } else {
            print("&cInvalid input &7( &f" + result + " &7)");
            print("&aValid inputs are: &7B = Fat &b| &7L = Flat\n\n");
            readForInput();
        }
    }
}
