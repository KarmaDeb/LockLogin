package ml.karmaconfigs.LockLogin.Security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

public final class InsecurePasswords {

    private final static HashSet<String> insecurePasswords = new HashSet<>();

    /**
     * Initialize the insecure passwords
     */
    public InsecurePasswords() {
        List<String> passwords = new ArrayList<>();
        passwords.add("123456");
        passwords.add("password");
        passwords.add("12345678");
        passwords.add("qwerty");
        passwords.add("123456789");
        passwords.add("12345");
        passwords.add("1234");
        passwords.add("111111");
        passwords.add("1234567");
        passwords.add("dragon");
        passwords.add("123123");
        passwords.add("baseball");
        passwords.add("abc123");
        passwords.add("football");
        passwords.add("monkey");
        passwords.add("letmein");
        passwords.add("696969");
        passwords.add("shadow");
        passwords.add("master");
        passwords.add("master");
        passwords.add("666666");
        passwords.add("qwertyuiop");
        passwords.add("123321");
        passwords.add("mustang");
        passwords.add("1234567890");
        passwords.add("michael");
        passwords.add("654321");
        passwords.add("pussy");
        passwords.add("superman");
        passwords.add("1qaz2wsx");
        passwords.add("7777777");
        passwords.add("fuckyou");
        passwords.add("121212");
        passwords.add("000000");
        passwords.add("qazwsx");
        passwords.add("123qwe");
        passwords.add("killer");
        passwords.add("trustno1");
        passwords.add("jordan");
        passwords.add("jennifer");
        passwords.add("zxcvbnm");
        passwords.add("asdfgh");
        passwords.add("hunter");
        passwords.add("buster");
        passwords.add("soccer");
        passwords.add("harley");
        passwords.add("batman");
        passwords.add("andrew");
        passwords.add("tigger");
        passwords.add("sunshine");
        passwords.add("iloveyou");
        passwords.add("fuckme");
        passwords.add("2000");
        passwords.add("charlie");
        passwords.add("robert");
        passwords.add("thomas");
        passwords.add("hockey");
        passwords.add("ranger");
        passwords.add("daniel");
        passwords.add("starwars");
        passwords.add("klaster");
        passwords.add("112233");
        passwords.add("george");
        passwords.add("asshole");
        passwords.add("computer");
        passwords.add("michelle");
        passwords.add("jessica");
        passwords.add("pepper");
        passwords.add("1111");
        passwords.add("zxcvbn");
        passwords.add("555555");
        passwords.add("11111111");
        passwords.add("131313");
        passwords.add("freedom");
        passwords.add("777777");
        passwords.add("pass");
        passwords.add("fuck");
        passwords.add("maggie");
        passwords.add("159753");
        passwords.add("aaaaaa");
        passwords.add("ginger");
        passwords.add("princess");
        passwords.add("joshua");
        passwords.add("cheese");
        passwords.add("amanda");
        passwords.add("summer");
        passwords.add("love");
        passwords.add("ashley");
        passwords.add("6969");
        passwords.add("nicole");
        passwords.add("chelsea");
        passwords.add("biteme");
        passwords.add("matthew");
        passwords.add("access");
        passwords.add("yankees");
        passwords.add("987654321");
        passwords.add("dallas");
        passwords.add("austin");
        passwords.add("thunder");
        passwords.add("taylor");
        passwords.add("matrix");
        insecurePasswords.addAll(passwords);
    }

    /**
     * Add extra insecure passwords
     *
     * @param passwords a list of passwords
     */
    public final void addExtraPass(List<String> passwords) {
        insecurePasswords.addAll(passwords);
    }

    /**
     * Check if the password is secure
     *
     * @param password the password to check
     * @return a boolean
     */
    public final boolean isSecure(String password) {
        return !insecurePasswords.contains(password);
    }
}
