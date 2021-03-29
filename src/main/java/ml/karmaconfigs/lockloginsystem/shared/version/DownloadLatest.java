package ml.karmaconfigs.lockloginsystem.shared.version;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.shared.FileInfo;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.spigot.LockLoginSpigot;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Private GSA code
 * <p>
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="https://karmaconfigs.ml/license/"> here </a>
 */
public final class DownloadLatest {

    private static boolean downloading = false;
    private static double percentage = 0.0;
    private final URL downloadURL;
    private File main;
    private File update;
    private File destJar;

    /**
     * Initialize the downloader
     *
     * @throws Throwable any kind of exception or error
     */
    public DownloadLatest() throws Throwable {
        downloadURL = new URL("https://karmaconfigs.github.io/updates/LockLogin/LockLogin.jar");

        try {
            main = new File(LockLoginSpigot.jar);
            update = new File(LockLoginSpigot.plugin.getServer().getWorldContainer() + "/plugins/update");
            destJar = new File(update + "/", LockLoginSpigot.jar);
        } catch (Throwable e) {
            String dir = LockLoginBungee.plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
            File pluginsFolder = new File(dir.replace("/LockLogin", ""));

            main = new File(LockLoginBungee.jar);
            update = new File(pluginsFolder + "/update");
            destJar = new File(update + "/", LockLoginBungee.jar);
        }
    }

    /**
     * Download the latest LockLogin jar version
     *
     * @param onEnd do something on end...
     */
    public final void download(Runnable onEnd) {
        if (destJar.exists()) {
            String dest_version = FileInfo.getJarVersion(destJar);
            String curr_version = FileInfo.getJarVersion(main);

            if (dest_version.equals(curr_version)) {
                percentage = 100;
                downloading = false;
                return;
            }
        }

        //Prevent the plugin from downloading LockLogin
        //more than once at the same time, to avoid errors
        if (!downloading) {
            try {
                URLConnection connection = downloadURL.openConnection();
                int size = connection.getContentLength();
                connection.connect();

                if (!update.exists()) {
                    if (update.mkdir()) {
                        PlatformUtils.send("Created update folder for LockLogin new update", Level.INFO);
                    } else {
                        PlatformUtils.send("An unknown error occurred while creating update folder", Level.GRAVE);
                    }
                }

                InputStream input = new BufferedInputStream(downloadURL.openStream(), 1024);
                OutputStream output = new FileOutputStream(destJar);

                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                double sumCount = 0.0;
                while ((bytesRead = input.read(dataBuffer, 0, 1024)) != -1) {
                    output.write(dataBuffer, 0, bytesRead);

                    sumCount += bytesRead;
                    percentage = (sumCount / size * 100.0);

                    downloading = true;
                }

                output.flush();

                output.close();
                input.close();

                PlatformUtils.send("Downloaded latest LockLogin version, restart your server to apply changes", Level.INFO);
            } catch (Throwable e) {
                PlatformUtils.log(e, Level.GRAVE);
                PlatformUtils.log("Error while downloading latest LockLogin instance", Level.INFO);
            } finally {
                onEnd.run();
                downloading = false;
            }
        }
    }

    /**
     * Check if the plugin is downloading a plugin
     * update
     *
     * @return if the plugin is being downloaded
     */
    public final boolean isDownloading() {
        return downloading;
    }

    /**
     * Get the download percentage of the file
     *
     * @return the download percentage of the file
     */
    public final int getPercentage() {
        String per_str = String.valueOf(percentage);
        try {
            return Integer.parseInt(per_str.split("\\.")[0]);
        } catch (Throwable ex) {
            return 0;
        }
    }
}
