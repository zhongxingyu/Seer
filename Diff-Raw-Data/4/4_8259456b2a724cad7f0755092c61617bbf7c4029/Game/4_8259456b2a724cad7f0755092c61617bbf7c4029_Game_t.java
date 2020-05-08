 package com.CC.Arenas;
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import static org.bukkit.ChatColor.*;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.CC.General.onStartup;
 
 public class Game
 {
 	
     String name;
     ArrayList<String> redTeam;
     ArrayList<String> blueTeam;
     boolean regenerated;
     public boolean started;
     private GameManager gm;
     int TimeofGame;
     int WarningTime;
     onStartup plugin;
     public int Countdown;
     public int GameTimer;
 	
     public Game(String name, onStartup instance)
     {
     	plugin = instance;
     	TimeofGame = plugin.getGameTime();
     	WarningTime = plugin.getWarningTime();
     	started = false;
     	gm = plugin.getGameManager();
         redTeam = new ArrayList<String>();
         blueTeam = new ArrayList<String>();
     }
     
     public String getName(){
     	return this.name;
     }
     
     public ArrayList<String> getPlayers()
     {
         ArrayList<String> ret = new ArrayList<String>();
         ret.addAll(redTeam);
         ret.addAll(blueTeam);
         return ret;
     }
     
     public Team getTeam(Player p)
     {
         return getTeam(p.getName());
     }
     
     public Team getTeam(String name)
     {
         if(redTeam.contains(name))
         {
             return Team.RED;
         }
         else if(blueTeam.contains(name))
         {
             return Team.BLUE;
         }
         return Team.NONE;
     }
     
     public ArrayList<String> getRedTeam(){
     	return redTeam;
     }
     
     public ArrayList<Player> getRedTeamPlayers(){
     	ArrayList<Player> player = new ArrayList<Player>();
     	for(String s : redTeam){
     		Player p = Bukkit.getPlayer(s);
     		player.add(p);
     	}
     	return player;
     }
     
     public ArrayList<String> getBlueTeam(){
     	return blueTeam;
     }
     
     public ArrayList<Player> getBlueTeamPlayers(){
     	ArrayList<Player> player = new ArrayList<Player>();
     	for(String s : blueTeam){
     		Player p = Bukkit.getPlayer(s);
     		player.add(p);
     	}
     	return player;
     }
     
     public boolean removePlayer(String string){
     	Team team = getTeam(string);
     	if(team.equals(Team.BLUE)){
     		blueTeam.remove(string);
     		gm.removePlayerFromGame(string);
     		return true;
     	}else if (team.equals(Team.RED)){
     		redTeam.remove(string);
     		gm.removePlayerFromGame(string);
     		return true;
     	}
     	return false;
     	
     }
     
     public boolean removePlayer(Player player){
     	Team team = getTeam(player.getName());
     	if(team.equals(Team.BLUE)){
     		blueTeam.remove(player.getName());
     		gm.removePlayerFromGame(player.getName());
     		return true;
     	}else if (team.equals(Team.RED)){
     		redTeam.remove(player.getName());
     		gm.removePlayerFromGame(player.getName());
     		return true;
     	}
     	return false;
     	
     	}
     
     public void setRegenerated(boolean trueorfalse){
     	regenerated = trueorfalse;
     }
     
     public void addRedPlayer(String playername){
     	Player player = Bukkit.getServer().getPlayer(playername);
     	redTeam.add(playername);
     	player.sendMessage(new StringBuilder(RED.toString()).append("You have succesfully join the red team!").toString());
     	gm.playerJoinGame(playername);
     }
     
     public void addBluePlayer(String playername){
     	Player player = Bukkit.getServer().getPlayer(playername);
     	blueTeam.add(playername);
     	player.sendMessage(new StringBuilder(BLUE.toString()).append("You have succesfully join the blue team!").toString());
     	gm.playerJoinGame(playername);
     }
     
     
     public Location getRedSpawn(String WorldName){
     	
     		return new Location(Bukkit.getServer().getWorld(WorldName), -866, 143, -762); 
     	}
     	 //Just for now until the actual spawn locations are found;
     	/**
     	 * When the new spawn locations are found it will be World == Arena Name and than the location of the spawn for the current team
     	 */
     
     public Location getBlueSpawn(String WorldName){
     	
     		return new Location(Bukkit.getServer().getWorld(WorldName), -936, 143, -762); 
     	}
     	 //Just for now until the actual spawn locations are found;
     	/**
     	 * When the new spawn locations are found it will be World == Arena Name and than the location of the spawn for the current team
     	 */
     
     
     
     public void startGameCountdown() {
     	Countdown = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
     	int count = WarningTime;
         public void run() {
             
            
             if(count == WarningTime){
                 sendMessageAll(ChatColor.GREEN + "" + WarningTime/60 + " minutes until the game starts!");
             }
            
             if(count == WarningTime/2){
             	sendMessageAll(ChatColor.GREEN + "" + WarningTime/60/2 + " minute until the game starts!");
             }
    
             if(count == 30 || count == 20 || (count < 11 && count > 1)){
             	sendMessageAll(ChatColor.GREEN + "" + count + " seconds until game starts!");
             }
             
             if(count == 1){
             	sendMessageAll(ChatColor.GREEN + "" + count + " second until game starts!");
             }
    
             if(count == 0){
             	sendMessageAll(ChatColor.GREEN + "The game has now started!");
             	started = true;
             	startGameTimer();
                 Bukkit.getScheduler().cancelTask(Countdown);
             }
             count--;
         }
     }, 0L, 20L);
 }
 
 public void startGameTimer() {
 	GameTimer = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 	int count = TimeofGame;
     public void run() {
         
        
         if(count == TimeofGame/2){
             sendMessageAll(ChatColor.GREEN + "Halftime! " + TimeofGame/60/2 + " minutes until the game ends!");
         }
        
        if(count/60 == 2){
        	sendMessageAll(ChatColor.GREEN + "2 minutes until the game ends!");
         }
 
         if(count == 30 || count == 20 || (count < 11 && count > 1)){
         	sendMessageAll(ChatColor.GREEN + "" + count + " seconds until game ends!");
         }
         
         if(count == 1){
         	sendMessageAll(ChatColor.GREEN + "" + count + " second until game ends!");
         }
 
         if(count == 0){
         	sendMessageAll("The game has ended");
         	started = false;
         	gm.endGame(name, getWinningTeam());
             Bukkit.getScheduler().cancelTask(GameTimer);
         }
         count--;
     }
 }, 0L, 20L);
 }
     
     	
 
 
     	 
     	
         
 	
 	public Team getWinningTeam(){
 		if(getBlueTeamPlayers().size() > getRedTeamPlayers().size()){
 			return Team.BLUE;
 		}else if(getBlueTeamPlayers().size() < getRedTeamPlayers().size()){
 			return Team.RED;
 		}else{
 			return Team.NONE;
 		}
 	}
 	
 	public void sendMessageAll(String string){
 		System.out.println(" sendMessage All");
 		
 		for(String s : getPlayers()){
 			if(Bukkit.getPlayer(s) == null){
 				System.out.println("Player " + s + " is null");
 				continue;
 			}
 			Bukkit.getPlayer(s).sendMessage(string);	
 		}		
 	}
 	
 	public void sendMessageBlue(String string){
 		for(Player p : getBlueTeamPlayers()){
 			p.sendMessage(string);
 		}
 	}
 	public void sendMessageRed(String string){
 		for(Player p : getRedTeamPlayers()){
 			p.sendMessage(string);
 		}
 	}
 	
     
 }   
     
     
     
