package ml.karmaconfigs.lockloginsystem.shared.llsecurity.Plugins.AuthMe;

import ml.karmaconfigs.lockloginsystem.shared.llsecurity.Plugins.AuthMe.libs.BCrypt;

public final class AuthMeBcrypt {

    public boolean check(String password, String token) {
        return BCrypt.checkpw(password, token);
    }
}
