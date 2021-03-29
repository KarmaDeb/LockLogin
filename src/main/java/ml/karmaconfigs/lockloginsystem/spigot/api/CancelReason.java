package ml.karmaconfigs.lockloginsystem.spigot.api;

/**
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
public enum CancelReason {
    /** Player block move event cancel reason */ OWN_HANDLE,
    /** Player block move event cancel reason */ ALLOW_MOVEMENT,
    /** Player block move event cancel reason */ UNKNOWN
}
