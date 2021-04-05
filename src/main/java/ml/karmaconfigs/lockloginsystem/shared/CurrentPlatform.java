package ml.karmaconfigs.lockloginsystem.shared;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public final class CurrentPlatform {

    private static Platform running;

    /**
     * Get the current running platform
     *
     * @return the current platform
     */
    public final Platform getRunning() {
        return running;
    }

    /**
     * Set the current running platform
     *
     * @param platform the current platform
     */
    public final void setRunning(final Platform platform) {
        running = platform;
    }
}
