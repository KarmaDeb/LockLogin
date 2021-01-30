package ml.karmaconfigs.lockloginsystem.shared;

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

/**
 * Success = Login / Register action have been completed successfully<br>
 * Success_Temp = Login action have been completed but 2fa is needed<br>
 * <br>
 * Cancelled = Login / Register event have been cancelled or the player
 * is no longer online<br>
 * <br>
 * Offline = The player seems offline<br>
 * <br>
 * Idle = Well, nothing happened
 */
public enum AuthResult {
    SUCCESS, SUCCESS_TEMP, CANCELLED, OFFLINE, IDLE
}
