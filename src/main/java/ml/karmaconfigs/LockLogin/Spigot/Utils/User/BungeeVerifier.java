package ml.karmaconfigs.LockLogin.Spigot.Utils.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

public final class BungeeVerifier {

    private final static List<UUID> verified = new ArrayList<>();

    private final UUID uuid;

    /**
     * Initialize the bungee verifier
     *
     * @param uuid the uuid
     */
    public BungeeVerifier(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Check if the uuid is verified
     *
     * @return a boolean
     */
    public final boolean isVerified() {
        return verified.contains(uuid);
    }

    /**
     * Set the uuid verification status
     *
     * @param value true/false
     */
    public final void setVerified(boolean value) {
        if (value) {
            if (!verified.contains(uuid)) {
                verified.add(uuid);
            }
        } else {
            verified.remove(uuid);
        }
    }
}
