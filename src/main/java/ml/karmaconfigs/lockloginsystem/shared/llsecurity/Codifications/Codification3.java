package ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Codification3 {

    private final Cipher ecipher;
    private final Cipher dcipher;

    private final SecretKey key;

    private String email;

    public Codification3(final String _email, final boolean encrypted) throws Throwable {
        email = _email;
        if (!encrypted) {
            key = KeyGenerator.getInstance("DES").generateKey();
        } else {
            email = new String(Base64.getDecoder().decode(email));
            String[] email_data = email.split("\\$");
            String key_email = email_data[0];
            email = email.replace(key_email + "$", "");

            byte[] key_bytes = Base64.getDecoder().decode(key_email);

            key = new SecretKeySpec(key_bytes, 0, key_bytes.length, "DES");
        }

        ecipher = Cipher.getInstance("DES");
        dcipher = Cipher.getInstance("DES");

        ecipher.init(Cipher.ENCRYPT_MODE, key);
        dcipher.init(Cipher.DECRYPT_MODE, key);
    }

    public final String encrypt() {
        try {
            byte[] utf8 = email.getBytes(StandardCharsets.UTF_8);
            byte[] enc = ecipher.doFinal(utf8);

            enc = BASE64EncoderStream.encode(enc);

            String prefix = new String(Base64.getEncoder().encode(key.getEncoded()));
            String encoded = new String(enc);
            String full = prefix + "$" + encoded;

            return new String(Base64.getEncoder().encode(full.getBytes(StandardCharsets.UTF_8)));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public final String decrypt() {
        try {
            byte[] dec = BASE64DecoderStream.decode(email.getBytes());
            byte[] utf8 = dcipher.doFinal(dec);

            return new String(utf8, StandardCharsets.UTF_8);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
