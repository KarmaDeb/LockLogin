package ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.argon;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;

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
public final class Argon2Util {

    private final String password;

    /**
     * Argon 2 utils class
     *
     * @param key the password/token to use
     */
    public Argon2Util(final String key) {
        password = key;
    }

    /**
     * Hash to argon password
     *
     * @param type the argon password type
     * @return the hashed password if it's not argon
     */
    public final String hashPassword(final CryptType type) {
        switch (type) {
            case ARGON2I:
                Argon2 argon2i = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2i);
                return argon2i.hash(22, 1024, 2, password.toCharArray(), StandardCharsets.UTF_8);
            case ARGON2ID:
                Argon2 argon2id = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
                return argon2id.hash(22, 1024, 2, password.toCharArray(), StandardCharsets.UTF_8);
            default:
                return password;
        }
    }

    /**
     * Check if the argon password matches with the specified token
     *
     * @param token the player hashed password
     * @param type the encryption type
     * @return if the password matches
     */
    public final boolean checkPassword(final String token, final CryptType type) {
        switch (type) {
            case ARGON2I:
                Argon2 argon2i = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2i);
                return argon2i.verify(token, password.toCharArray(), StandardCharsets.UTF_8);
            case ARGON2ID:
                Argon2 argon2id = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
                return argon2id.verify(token, password.toCharArray(), StandardCharsets.UTF_8);
            default:
                return false;
        }
    }
}
