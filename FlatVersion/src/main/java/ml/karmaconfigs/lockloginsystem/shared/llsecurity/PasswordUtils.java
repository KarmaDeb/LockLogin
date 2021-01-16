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
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications.Codification;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Plugins.AuthMe.AuthMeAuth;
import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Plugins.LoginSecurity.LoginSecurityAuth;
import org.apache.commons.codec.binary.Base64;

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
    public final String Encrypted() {
        return new Codification().hash(password);
    }

    /**
     * Get a hashed string of
     * password
     *
     * @return a hashed password
     */
    public final String Hash() {
        encoded = Base64.encodeBase64(Encrypted().getBytes());
        return new String(encoded);
    }

    /**
     * Get a hashed string of
     * password string
     *
     * @return the hash value of password
     */
    public final String HashString() {
        encoded = Base64.encodeBase64(password.getBytes());
        return new String(encoded);
    }

    /**
     * Unhash the password
     *
     * @return the unhashed password
     */
    public final String UnHash() {
        byte[] decoded = Base64.decodeBase64(password);
        return new String(decoded);
    }

    /**
     * Check if the password is equals
     * to the decoded password
     *
     * @return if the password is correct
     */
    public final boolean PasswordIsOk() {
        byte[] decode = Base64.decodeBase64(token);

        String decode_str = new String(decode);

        Codification codification = new Codification();
        AuthMeAuth authme = new AuthMeAuth();
        LoginSecurityAuth ls_auth = new LoginSecurityAuth();

        try {
            return authme.check(password, token) || ls_auth.check(password, token) || codification.auth(password, decode_str);
        } catch (Throwable ex) {
            PlatformUtils.log(ex, Level.GRAVE);
            PlatformUtils.log("An error occurred while trying to auth a player ( 1/2 )", Level.INFO);
            try {
                return codification.auth(password, decode_str);
            } catch (Throwable ex_2) {
                PlatformUtils.log(ex_2, Level.GRAVE);
                PlatformUtils.log("An error occurred while trying to auth a player ( 2/2 )", Level.INFO);
                PlatformUtils.log("False positive invalid password - Has been returned to the player, couldn't login", Level.WARNING);
                //False negative
                return false;
            }
        }

        /*
        try {
            if (new Codification().auth(password, new String(decode))) {
                return true;
            } else {
                if (new AuthMeAuth().check(password, token)) {
                    return true;
                } else {
                    return new LoginSecurityAuth().check(password, token);
                }
            }
        } catch (Throwable ex) {

            try {
                if (new AuthMeAuth().check(password, token)) {
                    return true;
                } else {
                    return new LoginSecurityAuth().check(password, token);
                }
            } catch (Throwable exc) {
                PlatformUtils.log(ex, Level.GRAVE);
                PlatformUtils.log("An error occurred while trying to auth a player ( 2/2 )", Level.INFO);
                PlatformUtils.log("False negative has been returned to the player, couldn't login", Level.WARNING);
                //False negative
                return false;
            }
        }*/
    }
}
