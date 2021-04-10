package ml.karmaconfigs.lockloginsystem.shared.llsecurity;

import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.Codification;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.Codification2;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.CryptType;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.argon.Argon2Util;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme.AuthMeAuth;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme.libs.BCrypt;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.loginsecurity.LoginSecurityAuth;
import org.apache.commons.codec.binary.Base64;

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
public final class PasswordUtils {

    private final String password;
    private String token;
    private byte[] encoded;

    /**
     * Start the password utils
     *
     * @param password the password
     */
    public PasswordUtils(String password) {
        this.password = password;
    }

    /**
     * Start the password utils
     *
     * @param password the password
     * @param token    the token
     */
    public PasswordUtils(String password, String token) {
        this.password = password;
        this.token = token;
    }

    /**
     * Get the encrypted password
     *
     * @return the encrypted password
     */
    public final String encrypt(final CryptType type) {
        switch (type) {
            case SHA256:
                return new Codification2(password, false).hash();
            case BCrypt:
            case BCryptPHP:
                return BCrypt.hashpw(password, BCrypt.gensalt()).replaceFirst("2a", "2y");
            case ARGON2I:
                return new Argon2Util(password).hashPassword(CryptType.ARGON2I);
            case ARGON2ID:
                return new Argon2Util(password).hashPassword(CryptType.ARGON2ID);
            case SHA512:
            case UNKNOWN:
            case NONE:
            default:
                return new Codification().hash(password);
        }
    }

    /**
     * Get a hashed string of
     * password
     *
     * @return a hashed password
     */
    public final String hashToken(final CryptType type) {
        encoded = Base64.encodeBase64(encrypt(type).getBytes());
        return new String(encoded);
    }

    /**
     * Get a hashed string of
     * password string
     *
     * @return the hash value of password
     */
    public final String hash() {
        encoded = Base64.encodeBase64(password.getBytes());
        return new String(encoded);
    }

    /**
     * Unhash the password
     *
     * @return the unhashed password
     */
    public final String unHash() {
        byte[] decoded = Base64.decodeBase64(password);
        return new String(decoded);
    }

    /**
     * Get the crypto type of the token
     *
     * @return the token encryption type
     */
    public final CryptType getCrypto() {
        if (token != null) {
            try {
                String[] token_data;
                if (Base64.isBase64(token))
                    token_data = new String(Base64.decodeBase64(token)).split("\\$");
                else
                    token_data = token.split("\\$");

                String type_backend = token_data[2];
                String type = token_data[1];

                CryptType crypto;

                switch (type.toLowerCase()) {
                    case "sha512":
                    case "512":
                        crypto = CryptType.SHA512;
                        break;
                    case "sha256":
                    case "256":
                        crypto = CryptType.SHA256;
                        break;
                    case "2y":
                        crypto = CryptType.BCryptPHP;
                        break;
                    case "2a":
                        crypto = CryptType.BCrypt;
                        break;
                    case "argon2i":
                        crypto = CryptType.ARGON2I;
                        break;
                    case "argon2id":
                        crypto = CryptType.ARGON2ID;
                        break;
                    default:
                        crypto = CryptType.UNKNOWN;
                        break;
                }

                if (crypto == CryptType.UNKNOWN)
                    switch (type_backend.toLowerCase()) {
                        case "sha512":
                        case "512":
                            crypto = CryptType.SHA512;
                            break;
                        case "sha256":
                        case "256":
                            crypto = CryptType.SHA256;
                            break;
                        case "2y":
                            crypto = CryptType.BCryptPHP;
                            break;
                        case "2a":
                            crypto = CryptType.BCrypt;
                            break;
                        case "argon2i":
                            crypto = CryptType.ARGON2I;
                            break;
                        case "argon2id":
                            crypto = CryptType.ARGON2ID;
                            break;
                        default:
                            crypto = CryptType.UNKNOWN;
                            break;
                    }

                return crypto;
            } catch (Throwable ex) {
                return CryptType.UNKNOWN;
            }
        } else {
            return CryptType.NONE;
        }
    }

    /**
     * Check if the current token needs a re-hash
     *
     * @return if the token needs a re-hash
     */
    public final boolean needsRehash(final CryptType current_crypto) {
        CryptType token_crypto = getCrypto();

        if (!token_crypto.equals(CryptType.NONE) && !token_crypto.equals(CryptType.UNKNOWN))
            return !current_crypto.equals(token_crypto);
        else
            return true;
    }

    /**
     * Check if the password is equals
     * to the decoded password
     *
     * @return if the password is correct
     */
    public final boolean validate() {
        /*String decode_str = token;
        if (Base64.isBase64(token)) {
            byte[] decode = Base64.decodeBase64(token);
            decode_str = new String(decode);
        }

        Codification codification = new Codification();
        AuthMeAuth authme = new AuthMeAuth();
        LoginSecurityAuth ls_auth = new LoginSecurityAuth();

        try {
            return authme.check(password, token) || ls_auth.check(password, token) || codification.auth(password, decode_str) || BCrypt.checkpw(password, decode_str.replaceFirst("2y", "2a"));
        } catch (Throwable ex) {
            PlatformUtils.log(ex, Level.GRAVE);
            PlatformUtils.log("An error occurred while trying to auth a player ( 1/2 )", Level.INFO);
            try {
                return codification.auth(password, decode_str);
            } catch (Throwable ex_2) {
                PlatformUtils.log(ex_2, Level.GRAVE);
                PlatformUtils.log("An error occurred while trying to auth a player ( 2/2 )", Level.INFO);
                PlatformUtils.log("False negative invalid password - Has been returned to the player, couldn't login", Level.WARNING);
                //False negative
                return false;
            }
        }*/

        CryptType current_type = getCrypto();

        String key = token;
        if (Base64.isBase64(key))
            key = new String(Base64.decodeBase64(key));

        Codification sha512 = new Codification();
        Argon2Util argon2id = new Argon2Util(password);

        switch (current_type) {
            case SHA512:
                return sha512.auth(password, key);
            case SHA256:
                return Codification2.check(password, key);
            case BCrypt:
            case BCryptPHP:
                return BCrypt.checkpw(password, key.replaceFirst("2y", "2a"));
            case ARGON2I:
                return argon2id.checkPassword(key, CryptType.ARGON2I);
            case ARGON2ID:
                return argon2id.checkPassword(key, CryptType.ARGON2ID);
            case UNKNOWN:
                AuthMeAuth authme = new AuthMeAuth();
                LoginSecurityAuth lsAuth = new LoginSecurityAuth();

                return authme.check(password, key) || lsAuth.check(password, key);
            case NONE:
            default:
                PlatformUtils.log("Error while getting current token hash type: " + current_type.name(), Level.GRAVE);
                return false;
        }
    }
}
