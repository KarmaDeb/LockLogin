package ml.karmaconfigs.lockloginsystem.shared.llsecurity.Plugins.LoginSecurity;

public class LoginSecurityAuth {

    /**
     * Check the password with the specified token
     *
     * @param password the password
     * @param token the token
     *
     * @return if the password is correct
     */
    public boolean check(String password, String token) {
        return BCrypt.checkpw(password, token);
    }
}
