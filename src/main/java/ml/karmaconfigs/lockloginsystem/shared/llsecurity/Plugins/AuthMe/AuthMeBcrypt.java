package ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme;

import ml.karmaconfigs.lockloginsystem.shared.llsecurity.plugins.authme.libs.BCrypt;

public final class AuthMeBcrypt {

    public boolean check(String password, String token) {
        return BCrypt.checkpw(password, token);
    }
}
