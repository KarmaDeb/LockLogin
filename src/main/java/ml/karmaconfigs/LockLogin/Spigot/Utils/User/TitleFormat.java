package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

import ml.karmaconfigs.LockLogin.Spigot.Utils.StringUtils;

public final class TitleFormat {

    private final String text;

    private int display = 3,keep = 5,hide = 3;

    /**
     * Create a title type class
     * instance
     *
     * @param title the title text
     */
    public TitleFormat(String title) {
        text = title;
    }

    /**
     * Set the title display time
     *
     * @param displayTime the time
     */
    public final void setDisplayTime(int displayTime) {
        display = displayTime;
    }

    /**
     * Set the title keep-in-screen time
     *
     * @param keepTime the time
     */
    public final void setKeepTime(int keepTime) {
        keep = keepTime;
    }

    /**
     * Set the title hide time
     *
     * @param hideTime the time
     */
    public final void setHideTime(int hideTime) {
        hide = hideTime;
    }

    /**
     * Get the title text
     *
     * @return a String
     */
    public final String getText() {
        return StringUtils.toColor(text);
    }

    /**
     * Get the display time
     *
     * @return an integer
     */
    public final int getDisplay() {
        return 20 * display;
    }

    /**
     * Get the keep-in-screen time
     *
     * @return an integer
     */
    public final int getKeep() {
        return 20 * keep;
    }

    /**
     * Get the hide time
     *
     * @return an integer
     */
    public final int getHide() {
        return 20 * hide;
    }
}
