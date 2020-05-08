 package com.CC.Arenas;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.CC.General.onStartup;
 import com.CC.WorldGeneration.WorldGeneration;
 
 public class GameManager
 {
 
     private HashMap<String, String> players;
     private HashMap<String, Game> games;
     private onStartup plugin;
     private WorldGeneration worldgen;
 
     public GameManager(onStartup instance)
     {
     	plugin = instance;
     	worldgen = plugin.getWorldGenerator();
         this.games = new HashMap<String, Game>();
         this.players =  new HashMap<String, String>();
     }
     
     public Game getGame(String name)
     {
         return this.games.get(name);
     }
     public boolean isGame(String name)
     {
         return this.games.containsKey(name);
     }
     
     public boolean createGame(String name)
     {
         if(this.games.containsKey(name))
         {
             return false;
         }
         Game g = new Game(name, plugin);
         this.games.put(name, g);
         worldgen.newMap(name);
         return true;
     }
     
     public boolean removeGame(String name)
     {
         if(!this.games.containsKey(name))
         {
             return false;
         }
         this.games.remove(name);
         return true;
     }
     
     public Game getGameByPlayer(Player p)
     {
         return getGameByPlayer(p.getName());
     }
     
     /*
     *   Can return null!
     **/
     public Game getGameByPlayer(String pname)
     {
         if(players.containsKey(pname))
         {
             return games.get(players.get(pname));
         }
         return null;
     }
     
     public boolean isInGame(Player peter){
     	if(players.containsKey(peter.getName())) { 
     		return true;
     	}else{
     		return false;
     	}
         
     }
     
     public boolean isInGame(String string){
     	if(players.containsKey(string)){ 
     		return true;
     	}else{
     		return false;
     	}
     	
         
     }
     
     private boolean endGame(Game game, Team team){
     	for(String p : game.getPlayers()){
     		Player player = Bukkit.getPlayer(p);
     		//Put in method to teleport to lobby world 
     	}
     	if(team.equals(Team.BLUE)){
     		for(Player p : game.getBlueTeamPlayers()){
     			p.sendMessage("Congratulations ! Your team has won ");
     		}
     		for(Player p : game.getRedTeamPlayers()){
     			p.sendMessage("Sorry you lost");
     		}
     		return true;
     	}else if(team.equals(Team.RED)){
     		for(Player p : game.getBlueTeamPlayers()){
     			p.sendMessage("Sorry you lost");
     		}
     		for(Player p : game.getRedTeamPlayers()){
     			p.sendMessage("Congratulations ! Your team has won");
     		}
     		return true;
     		
     	}else{
     		for(String s : game.getPlayers()){
     			Bukkit.getPlayer(s).sendMessage(" The game has ended in a draw");
     		}
     		return true;
     	}
     }
     
     private boolean endGame(Game game, String reason){
     	for(String p : game.getPlayers()){
     		Player player = Bukkit.getPlayer(p);
     		//Put in method to teleport to lobby world 
     	}
     	
     		for(Player p : game.getBlueTeamPlayers()){
     			p.sendMessage("Game has been ended by an administrator for the following reason" + reason);
     		}
     		for(Player p : game.getRedTeamPlayers()){
     			p.sendMessage("Game has been ended by an administrator for the following reason" + reason);
     		}
     		return true;
     		
     	}
     
     
     
     
     //Also regenerates arena :D 
     public boolean endGame(String gameName, Team winningTeam){
     	if(!games.containsKey(gameName)) return false;
     		if(endGame(getGame(gameName), winningTeam)){
     			Bukkit.getServer().broadcastMessage(ChatColor.GRAY+ gameName + ChatColor.GREEN + " is being regenerated. You may experience lag");
     			worldgen.newMap(gameName);
     			return true;
     		}else{
     			return false;
     	}
     }
     
     public boolean endGame(String gameName, String reason){
     	if(!games.containsKey(gameName)) return false;
     		if(endGame(getGame(gameName), reason)){
     			Bukkit.getServer().broadcastMessage(ChatColor.GRAY+ gameName + ChatColor.GREEN + " is being regenerated. You may experience lag");
     			worldgen.newMap(gameName);
     			return true;
     		}else{
     			return false;
     	}
     }
     
     public ArrayList<Game> getOpenGames(){
     	ArrayList<Game> opengames = new ArrayList<Game>();
     	for(Game g : games.values()){
     		if(g.getPlayers().size() == 0){
     			if(g.regenerated){
     				opengames.add(g);
     				g.setRegenerated(false);
     			}
     		}
     	}
     	return opengames;
     }
     
     public HashMap<String, Game> getGames(){
     	return games;
     }
     
     public void playerJoinGame(String string){
     	players.put(string, string);
     }
     
     public void removePlayerFromGame(String string){
     	players.remove(string);
     }
     
     public void startGameCount(Game game){ 
    	
     }
     
 }
