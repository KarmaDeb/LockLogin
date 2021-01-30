package ml.karmaconfigs.lockloginmodules.shared;

import java.io.File;

public final class NoJarException extends Exception {

    public NoJarException(final File jar) {
        super("The specified file ( " + jar.getAbsolutePath().replaceAll("\\\\", "/") + " ) is not a valid .jar file");
    }
}
