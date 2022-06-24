package racer.fund;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class gfStartCommand implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
			if(Main.startGame()) {
				Bukkit.broadcastMessage("[DBGF]: Game Started");
			}
			else {
				Bukkit.broadcastMessage("[DBGF]: Game can only start when above 2 players");
			}
			return true;
	}
}
