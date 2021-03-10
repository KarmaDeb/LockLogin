package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import ml.karmaconfigs.api.bungee.Console;
import ml.karmaconfigs.lockloginsystem.bungeecord.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.bungeecord.utils.files.BungeeFiles;
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

public final class ModuleListCommand extends Command implements BungeeFiles, LockLoginBungee {

    public ModuleListCommand() {
        super("modules");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            User user = new User(player);

            if (player.hasPermission("locklogin.modules")) {
                dataSender.openModulesGUI(player);
            } else {
                user.send(messages.prefix() + messages.permission("locklogin.modules"));
            }
        } else {
            Console.send(messages.prefix() + "&cNot done yet!");
        }
    }
}
