package ml.karmaconfigs.lockloginmodules.shared;

import java.io.File;

public class NoPluginException extends Exception {

    public NoPluginException(final File jar, final String reason) {
        super("The specified jar ( " + jar.getAbsolutePath().replaceAll("\\\\", "/") + " ) is not a valid plugin ( " + reason + " )");
    }
}
