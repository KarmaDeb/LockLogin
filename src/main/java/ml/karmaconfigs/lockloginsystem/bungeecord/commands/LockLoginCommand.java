package ml.karmaconfigs.lockloginsystem.bungeecord.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LockLoginCommand extends Command {

    public LockLoginCommand() {
        super("locklogin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {

        }
    }
}
