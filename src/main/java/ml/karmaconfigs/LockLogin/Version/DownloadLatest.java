package ml.karmaconfigs.LockLogin.Version;

import ml.karmaconfigs.LockLogin.BungeeCord.LockLoginBungee;
import ml.karmaconfigs.LockLogin.Logs.Logger;
import ml.karmaconfigs.LockLogin.Platform;
import ml.karmaconfigs.LockLogin.PlatformUtils;
import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.WarningLevel;

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
    public DownloadLatest() throws Throwable {
        downloadURL = new URL("https://karmaconfigs.github.io/updates/LockLogin/LockLogin_latest.jar");

        try {
            update = new File(LockLoginSpigot.plugin.getServer().getWorldContainer() + "/plugins/update");
            destJar = new File(update + "/", LockLoginSpigot.getJarName());
        } catch (Throwable e) {
            String dir = LockLoginBungee.plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
            File pluginsFolder = new File(dir.replace("/LockLogin", ""));

            update = new File(pluginsFolder + "/update");
            destJar = new File(update + "/", LockLoginBungee.getJarName());
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
                    PlatformUtils.Alert("Created update folder for LockLogin new update", WarningLevel.WARNING);
                } else {
                    PlatformUtils.Alert("An unknown error occurred while creating update folder", WarningLevel.WARNING);
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

            PlatformUtils.Alert("Downloaded latest LockLogin version, restart your server to apply changes", WarningLevel.WARNING);
        } catch (Throwable e) {
            Logger.log(Platform.ANY, "ERROR WHILE DOWNLOADING LATEST LOCKLOGIN VERSION" + ": " + e.fillInStackTrace(), e);
        }
    }
}
