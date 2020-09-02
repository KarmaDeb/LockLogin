package ml.karmaconfigs.LockLogin.Security;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.util.List;

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
