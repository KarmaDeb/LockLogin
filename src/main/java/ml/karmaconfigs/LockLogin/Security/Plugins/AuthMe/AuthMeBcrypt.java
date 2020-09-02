package ml.karmaconfigs.LockLogin.Security.Plugins.AuthMe;

import ml.karmaconfigs.LockLogin.Security.Plugins.AuthMe.libs.BCrypt;

public final class AuthMeBcrypt {

    public boolean check(String password, String token) {
        return BCrypt.checkpw(password, token);
    }
}
