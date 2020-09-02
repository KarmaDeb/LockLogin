package ml.karmaconfigs.LockLogin.Security.Plugins.LoginSecurity;

public class LoginSecurityAuth {

    /**
     * Check the password with the specified token
     *
     * @param password the password
     * @param token the token
     * @return a boolean
     */
    public boolean check(String password, String token) {
        return BCrypt.checkpw(password, token);
    }
}
