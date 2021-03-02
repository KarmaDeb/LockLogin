package ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public final class Codification2 {

    private final Object password;
    private final boolean isHashed;

    /**
     * Initialize the codification
     *
     * @param value  the value to codify
     * @param hashed is the value hashed?
     */
    public Codification2(Object value, boolean hashed) {
        this.password = value;
        this.isHashed = hashed;
    }

    /**
     * Check if the specified password
     * is correct
     *
     * @param password the password
     * @param value    the value to check
     * @return a boolean
     */
    public static boolean check(Object password, Object value) {
        String pass = new Codification2(password, true).hash();
        String check = new Codification2(value, false).hash();

        return pass.equals(check);
    }

    /**
     * hashEncrypted and encrypt the value
     *
     * @return a hashed value
     */
    @SuppressWarnings("all")
    public final String hash() {
        if (!isHashed)
            return Hashing.sha256().hashString(password.toString(), StandardCharsets.UTF_8).toString();
        ;
        return password.toString();
    }
}