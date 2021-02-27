package ml.karmaconfigs.lockloginsystem.shared.llsecurity;

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

import ml.karmaconfigs.api.shared.Level;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto.Codification;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme.AuthMeAuth;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme.libs.BCrypt;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.loginsecurity.LoginSecurityAuth;
import org.apache.commons.codec.binary.Base64;

public final class PasswordUtils {

    private final String password;
    private String token;
    private boolean useAzuriom = false;
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
     * Specify if password utils should use Azuriom's bcrypt
     *
     * @param support support Azuriom's BCrypt
     * @return the same instance with azuriom configuration
     */
    public PasswordUtils withAzuriomSupport(final boolean support) {
        useAzuriom = support;
        return this;
    }

    /**
     * Get the encrypted password
     *
     * @return the encrypted password
     */
    public final String encrypt() {
        if (useAzuriom) {
            String salt = BCrypt.gensalt();
            return BCrypt.hashpw(password, salt).replaceFirst("2a", "2y");
        } else {
            return new Codification().hash(password);
        }
    }

    /**
     * Get a hashed string of
     * password
     *
     * @return a hashed password
     */
    public final String hashEncrypted() {
        encoded = Base64.encodeBase64(encrypt().getBytes());
        return new String(encoded);
    }

    /**
     * Get a hashed string of
     * password string
     *
     * @return the hash value of password
     */
    public final String hashPassword() {
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
     * Check if the password is equals
     * to the decoded password
     *
     * @return if the password is correct
     */
    public final boolean checkPW() {
        String decode_str = token;
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
        }
    }
}
