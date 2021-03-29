package ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

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
public final class Codification2 {

    private final Object password;
    private final boolean isHashed;

    /**
     * Initialize the codification
     *
     * @param value  the value to codify
     * @param hashed is the value hashed?
     */
    public Codification2(Object value, boolean hashed) {
        this.password = value;
        this.isHashed = hashed;
    }

    /**
     * Check if the specified password
     * is correct
     *
     * @param password the password
     * @param value    the value to check
     * @return a boolean
     */
    public static boolean check(Object password, Object value) {
        String pass = new Codification2(password, true).hash();
        String check = new Codification2(value, false).hash();

        return pass.equals(check);
    }

    /**
     * hashEncrypted and encrypt the value
     *
     * @return a hashed value
     */
    @SuppressWarnings("all")
    public final String hash() {
        if (!isHashed)
            return Hashing.sha256().hashString(password.toString(), StandardCharsets.UTF_8).toString();
        ;
        return password.toString();
    }
}