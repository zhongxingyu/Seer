 package me.tehbeard.BeardAch.achievement;
 import java.util.*;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.ChunkCache;
 import me.tehbeard.BeardAch.dataSource.IDataSource;
 
 /**
  * Manages the link between achievements and players
  * @author James
  *
  */
 public class AchievementManager {
 
 	public static HashMap<String,HashSet<String>> playerHasCache = new  HashMap<String,HashSet<String>>();
 	public static HashMap<Achievement,HashSet<String>> playerCheckCache = new  HashMap<Achievement,HashSet<String>>();
 	public static IDataSource database = null;
 
 
 	private static List<Achievement> achievements = new LinkedList<Achievement>();
 
 	public static void loadAchievements(){
 		//clear cache
 		clearAchievements();
 		//reset Achievement Id's
 		Achievement.resetId();
 		//load achievements
 		database.loadAchievements();
 
 		//load each players 
 		for(Player p :Bukkit.getOnlinePlayers()){
 			AchievementManager.loadPlayersAchievements(p.getName());
 		}
 	}
 	/**
 	 * Clear the caches.
 	 */
 	public static void clearAchievements(){
 		ChunkCache.clearCache();
 		playerHasCache = new HashMap<String,HashSet<String>>();
 		achievements = new LinkedList<Achievement>();
 		playerCheckCache = new HashMap<Achievement,HashSet<String>>();	
 	}
 
 	/**
 	 * Return the list of loaded achievements
 	 * @return
 	 */
 	public static List<Achievement> getAchievementsList() {
 		return achievements;
 	}
 
 
 
 	public static Achievement getAchievement(String name){
 		for(Achievement a :achievements){
 			if(a.getName().equals(name)){
 				return a;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Add achievement to the manager
 	 * @param ach
 	 */
 	public static void addAchievement(Achievement ach){
 		achievements.add(ach);
 		ChunkCache.addAchievement(ach);
 	}
 
 	/**
 	 * Load the achievements for a player
 	 * @param player
 	 */
 	public static void loadPlayersAchievements(String player){
 		HashSet<String> got = database.getPlayersAchievements(player);
 		//put to cache
 		playerHasCache.put(player,got);
 		//cycle all loaded achievements
 		buildPlayerCheckCache(player);
 	}
 
 	private static void buildPlayerCheckCache(String player){
 
 		for(Achievement ach :achievements){
 
 			//if they don't have that achievement, add them to the check cache
 			if(!playerHasCache.get(player).contains(ach.getName())){
 				//push key if it doesn't exist
 				if(!playerCheckCache.containsKey(ach)){
 					playerCheckCache.put(ach, new HashSet<String>());
 				}
 				//add player to cache
 				playerCheckCache.get(ach).add(player);
 			}
 		}
 	}
 
 
 	/**
 	 * Check all players online
 	 */
 	public static void checkPlayers(){
 
 		//wipe players not online
 		//for each achievement
 		for( Entry<Achievement, HashSet<String>> entry : playerCheckCache.entrySet()){
 			BeardAch.printDebugCon("ach:"+entry.getKey().getName());
 			//loop all players, check them.
 			Iterator<String> it = entry.getValue().iterator();
 			String ply;
 			Player p;
 			while(it.hasNext()){
 				ply = it.next();
 
 				//get player object for offline detection
 				p =Bukkit.getPlayer(ply);
 				if(p instanceof Player){
 					BeardAch.printDebugCon("Player "+ply+" online");
 					//check for bleep bloop
 					if(entry.getKey().checkAchievement(p)){
 						BeardAch.printDebugCon("Achievement Get! " + ply + "=>" + entry.getKey().getName());
 						//push to cache
 						playerHasCache.get(ply).add(entry.getKey().getName());
 						//push to DB
 						database.setPlayersAchievements(ply, entry.getKey().getName());
 						it.remove();
 					}
 
 				}else{
 					it.remove();
 				}
 
 			}
 
 
 		}
 
 
 	}
 
 	public static List<Achievement> getAchievements(String player){
 		if(playerHasCache.containsKey(player)){
 			List<Achievement> l = new LinkedList<Achievement>();
 			for(String s:playerHasCache.get(player)){
 				Achievement a = getAchievement(s);
 				if(a!=null){
 					l.add(a);
 					Collections.sort(l,new Comparator<Achievement>() {
 
 						public int compare(Achievement o1, Achievement o2) {
 							int res = o1.getId() - o2.getId();
 							return res/Math.abs(res);
 						}
 					});
 				}
 			}
 			return l;
 		}
 		return null;
 
 	}
 
 	public static Achievement getAchievement(int i) {
		if(i>0 && i<achievements.size()){
 			return achievements.get(i-1);
 		}
 		return null;
 	}
 
 }
