package racer.fund;

import java.util.ArrayList;
import java.util.Collection;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEnderSignal;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EntityEnderSignal;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;


public class Main extends JavaPlugin{
	static BukkitRunnable countDownTask;
	static ArrayList<Player> gamePlayers=new ArrayList<Player>();
	static ArrayList<Player> outPlayers=new ArrayList<Player>();
	static Player blinderPlayer;
	public static boolean gameStarted=false;
	public static boolean musicFinished=true;
	public static boolean strikeUsed=true;
	
	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	this.getServer().getPluginManager().registerEvents(new eventListener(), this);
    	this.getCommand("gfregister").setExecutor(new gfRegisterCommand());
    	this.getCommand("gfstart").setExecutor(new gfStartCommand());
    	this.getCommand("gfstrike").setExecutor(new gfStrikePlayer());
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {
    	
    }
	
    public static boolean registerPlayerToGame(String arg) {
    	Collection<? extends Player> collectionPlayers = Bukkit.getOnlinePlayers();
    	Player[] players=collectionPlayers.toArray(new Player[collectionPlayers.size()]);
    	Player[] addedPlayers=gamePlayers.toArray(new Player[gamePlayers.size()]);
    	
    	for(int i=0;i<players.length;i++) {
    		if(players[i].getName().equals(arg)) {
    			boolean playerAdded=false;
    			for(int i2=0;i2<addedPlayers.length;i2++) {
    				if(arg.equals(addedPlayers[i2].getName())) {
    					playerAdded=true;
    				}
    			}
    			if(!playerAdded) {
    				gamePlayers.add(players[i]);
    			}
    			return true;
    		}
    	}
    	return false;
    }
    
    @SuppressWarnings("deprecation")
	public static boolean startGame() {
    	if(!gameStarted) {
	    	if(gamePlayers.size()>=2) {
	    		int randomPlayer=(int) (Math.random()*gamePlayers.size());
	    		Player[] players=gamePlayers.toArray(new Player[gamePlayers.size()]);
	    		blinderPlayer=players[randomPlayer];
	    		
	    		gamePlayers.remove(randomPlayer);
	    		players=gamePlayers.toArray(new Player[gamePlayers.size()]);
	    		
	    		blinderPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE,0));
	    		blinderPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE,255));
	    		sendBlinderStart(blinderPlayer);
	    		if (musicFinished) {
	    		blinderPlayer.playSound(blinderPlayer.getLocation(), "records.stal", 500	, 1);
	    		}
	    		for(int i=0;i<players.length;i++) {
	    			players[i].addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE,0));
	    			if (musicFinished) {
	    			players[i].playSound(players[i].getLocation(), "records.stal", 500	, 1);
	    			}
	    			sendTrickersStart(players[i]);
	    		}
	    		musicFinished=false;
	    		new BukkitRunnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						musicFinished=true;
					}
				}.runTaskLater(getPlugin(Main.class), 20L*150L);
	    		gameStarted=true;
	    		countDownTask=new BukkitRunnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Player[] players=Main.gamePlayers.toArray(new Player[Main.gamePlayers.size()]);
						Bukkit.broadcastMessage(players.length+"length");
						for(int i=0;i<players.length;i++) {
							Bukkit.broadcastMessage(players[i].getName());
							players[i].setGameMode(GameMode.ADVENTURE);
							players[i].removePotionEffect(PotionEffectType.SLOW);
							Main.sendTrickersWins(players[i]);
						}
						Main.blinderPlayer.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
						Main.blinderPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
						Main.sendTrickersWins(Main.blinderPlayer);
						Main.gamePlayers.clear();
						Main.outPlayers.clear();
						Main.blinderPlayer=null;
						Main.gameStarted=false;
					}
				};
				countDownTask.runTaskLater(getPlugin(Main.class), 20L*240L);
	    		return true;
	    	}
    	}
    	return false;
    }
    
    static void sendBlinderWins(Player player) {
    	IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\":\"" + ChatColor.RED + "BLINDER" + ChatColor.DARK_RED + " WIN..." + "\"}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(5, 50, 5);

    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
    }
    
    static void sendTrickersWins(Player player) {
    	IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\":\"" + ChatColor.BLUE + "TRCIKERS" + ChatColor.DARK_BLUE + " WIN..." + "\"}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(5, 50, 5);

    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
    }
    
    static void sendBlinderStart(Player player) {
    	IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\":\"" + ChatColor.RED + "You're.." + ChatColor.DARK_RED + " BLINDER" + "\"}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(5, 50, 5);

    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
    }
    
    static void sendTrickersStart(Player player) {
    	IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\":\"" + ChatColor.BLUE + "You're.." + ChatColor.DARK_BLUE + " TRICKER" + "\"}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(5, 50, 5);

    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
    }
}






class gfRegisterCommand implements CommandExecutor{
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

class gfStartCommand implements CommandExecutor{
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

class gfStrikePlayer implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
//			if(Main.startGame()) {
				Bukkit.broadcastMessage("[DBGF]: Striked");
				Player player=(Player) sender;
			    player.getWorld().strikeLightning(player.getLocation());
//			}
			return true;
	}
}

class eventListener implements Listener {
	@EventHandler
    public void onPlayerDead (PlayerDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            //some code here
        	if(Main.gameStarted) {
        		boolean playerInGame=false;
        		Player[] players=Main.gamePlayers.toArray(new Player[Main.gamePlayers.size()]);
        		for(int j=0;j<players.length;j++) {
        			if(players[j].getName().equals(e.getEntity().getName())) {
        				playerInGame=true;
        			}
        		}
        		if(playerInGame) {
        			e.getEntity().setGameMode(GameMode.SPECTATOR);
            		Main.outPlayers.add(e.getEntity());
        		}
        		if(Main.outPlayers.size()==Main.gamePlayers.size()) {
        			for(int i=0;i<players.length;i++) {
        				players[i].setGameMode(GameMode.ADVENTURE);
        				players[i].removePotionEffect(PotionEffectType.SLOW);
        				Main.sendBlinderWins(players[i]);
        			}
        			Main.blinderPlayer.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        			Main.blinderPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
        			
        			Main.sendBlinderWins(Main.blinderPlayer);
        			Main.countDownTask.cancel();
        			Main.gamePlayers.clear();
        			Main.outPlayers.clear();
        			Main.blinderPlayer=null;
        			Main.gameStarted=false;
        		}
        	}
        }
    }
	
//	@EventHandler
//	public void onPlayerInteract(PlayerInteractEvent e)
//	{
//		Player player = e.getPlayer();
//		
//		if (e.getItem() == null ||  e.getItem().getType() != Material.EYE_OF_ENDER) return; // If the player isn't holding an item
//	
//		if (e.getItem().getType() == Material.EYE_OF_ENDER)
//		{
//			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
//			{
//				if (e.getClickedBlock() != null) if (e.getClickedBlock().getType() == Material.ENDER_PORTAL_FRAME) return; 
//
//				e.setCancelled(true); // If block clicked was End Portal Frame, the event is ignored and item is used normally.
//				e.setUseItemInHand(Result.DENY); // This makes sure we don't get duplicate ender eyes spawning (happens if this isn't set to 'deny')
//				
//				// Get nearest target location
//				Location target = new Location(player.getWorld(), 317, 69, 96);
//				if (target == null) return;
//								
//				// Get the location to spawn the ender signal at
//				Location signalLocation = player.getLocation(); // The ender signal spawns at the player's eye height
//				signalLocation.setY(signalLocation.getY() + player.getEyeHeight());
//				
//				// Spawn the ender signal + get EntityEnderSignal handle
//				EnderSignal eye = e.getPlayer().getWorld().spawn(signalLocation, EnderSignal.class);
//				
//				
//				// Make some noise :D
//				World world =  e.getPlayer().getWorld();
//				
//				// Play sound in world (player entity, x, y, z, sound effect, sound category, balance?, volume?)
////				world.playSound(signalLocation, Sound.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 100, 1);
//				
//				// This function sets the location that ender eyes float towards
//				eye.setTargetLocation(target);
//				
//			}
//		}
//	}
}



class strikeCDCounterThread extends Thread{
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			strikeCDCounterThread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}