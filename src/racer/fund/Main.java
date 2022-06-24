package racer.fund;

import java.util.ArrayList;

import java.util.Collection;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_8_R1.ChatComponentText;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;


public class Main extends JavaPlugin{
	static BukkitRunnable countDownTask;
	static ArrayList<Player> gamePlayers=new ArrayList<Player>();
	static ArrayList<Player> outPlayers=new ArrayList<Player>();
	static Player blinderPlayer;
	public static boolean gameStarted=false;
	public static boolean musicFinished=true;
	public static boolean strikeUsed=true;
	public static FileConfiguration config;
	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	config=this.getConfig();
    	config.addDefault("lobbyPosition", new int[]{0,0,0});
    	config.options().copyDefaults(true);
    	this.saveConfig();
    	    	
    	this.getServer().getPluginManager().registerEvents(new eventListener(), this);
    	
    	this.getCommand("gfregister").setExecutor(new gfRegisterCommand());
    	this.getCommand("gfstart").setExecutor(new gfStartCommand());
    	this.getCommand("gfsetlobby").setExecutor(new gfSetLobbyPosition());

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
    	    public void run() {
    	    	if(gameStarted) {
    	    		sendActionBar(blinderPlayer, getActionBarCountDown());
    	    	}
    	    }
    	}, 20, 20);
    	
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
	    		countDown=239;
	        	ActionBarBuilder=new StringBuilder("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
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
						Bukkit.broadcastMessage("Loudest claps to following "+players.length+" performancers!");
						for(int i=0;i<players.length;i++) {
							Bukkit.broadcastMessage(players[i].getName());
							players[i].setGameMode(GameMode.SPECTATOR);
							players[i].removePotionEffect(PotionEffectType.SLOW);
							Main.sendTrickersWins(players[i]);
						}
	        			Main.blinderPlayer.setGameMode(GameMode.SPECTATOR);
						Main.blinderPlayer.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
						Main.blinderPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
						Main.sendTrickersWins(Main.blinderPlayer);
						initiateGame();
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
    
    void sendActionBar(Player player, String message) {
	    PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte)2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
    
    static StringBuilder ActionBarBuilder;
    static int countDown;
    String getActionBarCountDown() {
    		ActionBarBuilder.deleteCharAt(countDown);
    		countDown--;
    		return ActionBarBuilder.toString()+"|";
    }
    
    public static void initiateGame() {
		// TODO Auto-generated method stub
    	new BukkitRunnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Player[] players=Main.gamePlayers.toArray(new Player[Main.gamePlayers.size()]);
		    	for(int i=0;i<players.length;i++) {
					players[i].teleport(new Location(players[i].getWorld(),Main.config.getIntegerList("lobbyPosition").get(0),Main.config.getIntegerList("lobbyPosition").get(1),Main.config.getIntegerList("lobbyPosition").get(2)));
//					players[i].removePotionEffect(PotionEffectType.SLOW);
					players[i].setGameMode(GameMode.ADVENTURE);
				}
				Main.blinderPlayer.teleport(new Location(Main.blinderPlayer.getWorld(),Main.config.getIntegerList("lobbyPosition").get(0),Main.config.getIntegerList("lobbyPosition").get(1),Main.config.getIntegerList("lobbyPosition").get(2)));
				Main.blinderPlayer.setGameMode(GameMode.ADVENTURE);
				
		    	Main.gamePlayers.clear();
				Main.outPlayers.clear();
				Main.blinderPlayer=null;
				Main.gameStarted=false;
			}
		}.runTaskLater(Main.getPlugin(Main.class), 20L*5L);
		
	}
}







class eventListener implements Listener {
	@EventHandler
    public void onPlayerDead (PlayerDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            //CHECK IF GAME STARTED
        	if(Main.gameStarted) {
        		boolean playerInGame=false;
        		Player[] players=Main.gamePlayers.toArray(new Player[Main.gamePlayers.size()]);
        		for(int j=0;j<players.length;j++) {
        			if(players[j].getName().equals(e.getEntity().getName())||Main.blinderPlayer.getName().equals(e.getEntity().getName())) {
        				playerInGame=true;
        				break;
        			}
        		}
        		//SEND DEADPLAYER TO SPECTATOR
        		if(playerInGame) {
        			e.getEntity().setGameMode(GameMode.SPECTATOR);
            		Main.outPlayers.add(e.getEntity());
        		}
        		//BLINDER DEAD
        		if(Main.blinderPlayer.getName().equals(e.getEntity().getName())) {
        			Bukkit.broadcastMessage("Loudest claps to following "+players.length+" performancers!");
					for(int i=0;i<players.length;i++) {
						Bukkit.broadcastMessage(players[i].getName());
						players[i].setGameMode(GameMode.SPECTATOR);
						players[i].removePotionEffect(PotionEffectType.SLOW);
						Main.sendTrickersWins(players[i]);
					}
					Main.sendTrickersWins(Main.blinderPlayer);
					Main.countDownTask.cancel();
					Main.initiateGame();

        		}
        		
        		//ALL TRICKER DEAD
        		else if(Main.outPlayers.size()==Main.gamePlayers.size()) {
        			for(int i=0;i<players.length;i++) {
        				players[i].setGameMode(GameMode.SPECTATOR);
        				Main.sendBlinderWins(players[i]);
        			}
        			Main.blinderPlayer.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
					Main.blinderPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
        			Main.blinderPlayer.setGameMode(GameMode.SPECTATOR);
        			Main.sendBlinderWins(Main.blinderPlayer);
        			Main.countDownTask.cancel(); 
    				Main.initiateGame();
        		}
        	}
        }
    }
	
}
