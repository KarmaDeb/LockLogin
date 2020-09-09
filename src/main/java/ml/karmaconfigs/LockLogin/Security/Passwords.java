package ml.karmaconfigs.LockLogin.Security;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

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

public interface Passwords {

    static void addInsecure(List<String> passwords) {
        new InsecurePasswords().addExtraPass(passwords);
    }

    static boolean isSecure(String password) {
        return new InsecurePasswords().isSecure(password);
    }

    static boolean isSecure(String password, Player player) {
        return !password.contains(player.getName()) && isSecure(password);
    }

    static boolean isSecure(String password, ProxiedPlayer player) {
        return !password.contains(player.getName()) && isSecure(password);
    }
}
