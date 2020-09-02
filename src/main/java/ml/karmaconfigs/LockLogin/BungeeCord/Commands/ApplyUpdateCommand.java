package ml.karmaconfigs.LockLogin.BungeeCord.Commands;

import ml.karmaconfigs.LockLogin.BungeeCord.Utils.Files.BungeeFiles;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.PluginManager.LockLoginBungeeManager;
import ml.karmaconfigs.LockLogin.BungeeCord.Utils.User.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class ApplyUpdateCommand extends Command implements BungeeFiles {

    public ApplyUpdateCommand() {
        super("applyUpdates");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (player.hasPermission("locklogin.applyupdates")) {
                new LockLoginBungeeManager().applyUpdate();
                user.Message(messages.Prefix() + "&aLockLogin have been reloaded and its updates have been applied");
            } else {
                user.Message(messages.Prefix() + messages.PermissionError("locklogin.applyupdates"));
            }
        } else {
            new LockLoginBungeeManager().applyUpdate();
        }
    }
}
