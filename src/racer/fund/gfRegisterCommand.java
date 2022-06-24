package racer.fund;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class gfRegisterCommand implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
			if(arg3.length==0) {
				sender.sendMessage("[DBGF]: Invalid Player Name");
				return false;
			}
			else if(Main.registerPlayerToGame(arg3[0])){
				sender.sendMessage("[DBGF]: Success");
				return true;
			}
			else {
				sender.sendMessage("[DBGF]: Invalid Player Name");
				return false;
			}
	}
}
