package ml.karmaconfigs.LockLogin.Spigot.Commands;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.Spigot.Utils.DataFiles.Spawn;
import ml.karmaconfigs.LockLogin.Spigot.Utils.Files.SpigotFiles;
import ml.karmaconfigs.LockLogin.Spigot.Utils.User.User;
import ml.karmaconfigs.LockLogin.WarningLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class SetSpawnCommand implements CommandExecutor, LockLoginSpigot, SpigotFiles {

    private final Permission setspawn = new Permission("locklogin.setspawn", PermissionDefault.FALSE);

    /**
     * The set spawn command
     *
     * @param sender who executes the command
     * @param cmd    the command
     * @param arg    the command arg
     * @param args   the command args
     * @return a boolean
     */
    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);

            if (player.hasPermission(setspawn)) {
                if (config.HandleSpawn()) {
                    Spawn spawn = new Spawn();

                    spawn.setSpawn(player.getLocation());
                    user.Message(messages.Prefix() + messages.SpawnSet());
                } else {
                    user.Message(messages.Prefix() + messages.SpawnDisabled());
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError(setspawn.getName()));
            }
        } else {
            out.Alert("This command is for players only", WarningLevel.ERROR);
        }
        return false;
    }
}
