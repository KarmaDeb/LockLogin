package ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme;

import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme.libs.BCrypt;

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
public final class AuthMeBcrypt {

    /**
     * Check bcrypt password
     *
     * @param password the password to check
     * @param token the encrypted password
     * @return if the password matches the token
     */
    public boolean check(String password, String token) {
        return BCrypt.checkpw(password, token);
    }
}
