package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.lockloginsystem.bungeecord.InterfaceUtils;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.ConfigGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.MessageGetter;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.pluginmanager.LockLoginBungeeManager;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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

public final class ApplyUpdateCommand extends Command implements BungeeFiles {

    public ApplyUpdateCommand() {
        super("applyUpdates");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        InterfaceUtils utils = new InterfaceUtils();
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (player.hasPermission("locklogin.applyupdates")) {
                if (new LockLoginBungeeManager().applyUpdate()) {
                    user.Message(messages.Prefix() + "&aLockLogin have been reloaded and its updates have been applied");
                    utils.setReadyToUpdate(false);
                } else {
                    user.Message(messages.Prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                    if (ConfigGetter.manager.reload())
                        user.Message(messages.Prefix() + "&aConfig file reloaded!");
                    if (MessageGetter.manager.reload())
                        user.Message(messages.Prefix() + "&aMessages file reloaded!");
                }
            } else {
                user.Message(messages.Prefix() + messages.PermissionError("locklogin.applyupdates"));
            }
        } else {
            if (new LockLoginBungeeManager().applyUpdate()) {
                Console.send(messages.Prefix() + "&aLockLogin have been reloaded and its updates have been applied");
                utils.setReadyToUpdate(false);
            } else {
                Console.send(messages.Prefix() + "&aLockLogin couldn't be updated, but it will try to reload config and files");
                if (ConfigGetter.manager.reload())
                    Console.send(messages.Prefix() + "&aConfig file reloaded!");
                if (MessageGetter.manager.reload())
                    Console.send(messages.Prefix() + "&aMessages file reloaded!");
            }
        }
    }
}
