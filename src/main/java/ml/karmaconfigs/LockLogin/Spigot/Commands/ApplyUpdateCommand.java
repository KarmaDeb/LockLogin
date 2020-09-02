package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.LockLoginSpigotManager;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class ApplyUpdateCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    Permission applyUpdatePermission = new Permission("locklogin.update", PermissionDefault.FALSE);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (player.hasPermission(applyUpdatePermission)) {
                new LockLoginSpigotManager().applyUpdate();
                user.Message(messages.Prefix() + "&aLockLogin have been reloaded and its updates have been applied");
            } else {
                user.Message(messages.Prefix() + messages.PermissionError(applyUpdatePermission.getName()));
            }
        } else {
            new LockLoginSpigotManager().applyUpdate();
        }
        return false;
    }
}
