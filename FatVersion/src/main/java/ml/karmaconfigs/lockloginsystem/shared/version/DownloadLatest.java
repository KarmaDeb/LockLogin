package ml.karmaconfigs.lockloginsystem.shared.version;

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class DownloadLatest {

    private File update;
    private File destJar;

    private final URL downloadURL;

    /**
     * Initialize the downloader
     *
     * @throws Throwable any kind of exception or error
     */
    public DownloadLatest(final boolean isFat) throws Throwable {
        downloadURL = new URL("https://karmaconfigs.github.io/updates/LockLogin/" + (isFat ? "LockLogin_fat.jar" : "LockLogin_flat.jar"));

        try {
            update = new File(LockLoginSpigot.plugin.getServer().getWorldContainer() + "/plugins/update");
            destJar = new File(update + "/", LockLoginSpigot.jar);
        } catch (Throwable e) {
            String dir = LockLoginBungee.plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
            File pluginsFolder = new File(dir.replace("/LockLogin", ""));

            update = new File(pluginsFolder + "/update");
            destJar = new File(update + "/", LockLoginBungee.jar);
        }
    }

    /**
     *  Download the latest LockLogin jar version
     */
    public final void download() {
        try {
            int count;
            URLConnection connection = downloadURL.openConnection();
            connection.connect();

            if (!update.exists()) {
                if (update.mkdir()) {
                    PlatformUtils.Alert("Created update folder for LockLogin new update", Level.INFO);
                } else {
                    PlatformUtils.Alert("An unknown error occurred while creating update folder", Level.GRAVE);
                }
            }

            InputStream input = new BufferedInputStream(downloadURL.openStream(), 1024);
            OutputStream output = new FileOutputStream(destJar);

            byte[] data = new byte[1024];

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();

            output.close();
            input.close();

            PlatformUtils.Alert("Downloaded latest LockLogin version, restart your server to apply changes", Level.INFO);
        } catch (Throwable e) {
            PlatformUtils.log(e, Level.GRAVE);
            PlatformUtils.log("Error while downloading latest LockLogin instance", Level.INFO);
        }
    }
}
