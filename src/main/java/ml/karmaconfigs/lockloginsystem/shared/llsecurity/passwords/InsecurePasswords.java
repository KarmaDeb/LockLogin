package ml.karmaconfigs.lockloginsystem.shared.llsecurity.passwords;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
public final class InsecurePasswords {

    private final static HashSet<String> insecurePasswords = new HashSet<>();

    /**
     * Initialize the insecure passwords
     */
    public InsecurePasswords() {
        if (insecurePasswords.isEmpty()) {
            List<String> passwords = new ArrayList<>();

            try {
                InputStream inPasswords = getClass().getResourceAsStream("/insecure.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inPasswords, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null)
                    passwords.add(line);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            insecurePasswords.addAll(passwords);
        }
    }

    /**
     * Add extra insecure passwords
     *
     * @param passwords a list of insecure passwords
     */
    public final void addExtraPass(List<String> passwords) {
        insecurePasswords.addAll(passwords);
    }

    /**
     * Check if the password is secure
     *
     * @param password the password to check
     * @return if the password is secure
     */
    public final boolean isSecure(String password) {
        return !insecurePasswords.contains(password);
    }
}
