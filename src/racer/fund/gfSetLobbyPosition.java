package racer.fund;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class gfSetLobbyPosition implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		if(arg3.length==3){
			int[] lobbyPosition=new int[3];
			try {
				lobbyPosition[0]=Integer.valueOf(arg3[0]);
				lobbyPosition[1]=Integer.valueOf(arg3[1]);
				lobbyPosition[2]=Integer.valueOf(arg3[2]);
			}
			catch(NumberFormatException e) {
				e.printStackTrace();
				sender.sendMessage("[DBGF]: Invalid Position");
				return false;
			}
			Main.config.set("lobbyPosition", lobbyPosition);
			Main.getPlugin(Main.class).saveConfig();
			sender.sendMessage("[DBGF]: Success"+lobbyPosition[0]+lobbyPosition[1]+lobbyPosition[2]);
			return true;
		}
		else {
			sender.sendMessage("[DBGF]: Invalid Position");
			return false;
		}
	}

}
