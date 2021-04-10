package ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.argon;

import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;

import java.nio.charset.StandardCharsets;

import static com.kosprov.jargon2.api.Jargon2.*;

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
 *
 *  @deprecated This has been deprecated as it was not working
 *  at all, moved to {@link Argon2Util}
 */
@Deprecated
public final class Argon2UtilBackend {

    private final String password;

    /**
     * Argon 2 utils class
     *
     * @param key the password/token to use
     */
    public Argon2UtilBackend(final String key) {
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
                return jargon2Hasher()
                        .type(Type.ARGON2i)
                        .memoryCost(1024)
                        .timeCost(22)
                        .parallelism(2)
                        .password(password.getBytes(StandardCharsets.UTF_8)).encodedHash();
            case ARGON2ID:
                return jargon2Hasher()
                        .type(Type.ARGON2id)
                        .memoryCost(1024)
                        .timeCost(22)
                        .parallelism(2)
                        .password(password.getBytes(StandardCharsets.UTF_8)).encodedHash();
            default:
                return password;
        }
    }

    /**
     * Check if the argon password matches with the specified token
     *
     * @param token the player hashed password
     * @param type  the encryption type
     * @return if the password matches
     */
    public final boolean checkPassword(final String token, final CryptType type) {
        switch (type) {
            case ARGON2I:
                return jargon2Verifier()
                        .type(Type.ARGON2i).hash(token)
                        .password(password.getBytes(StandardCharsets.UTF_8)).verifyEncoded();
            case ARGON2ID:
                return jargon2Verifier()
                        .type(Type.ARGON2id).hash(token)
                        .password(password.getBytes(StandardCharsets.UTF_8)).verifyEncoded();
            default:
                return false;
        }
    }
}
