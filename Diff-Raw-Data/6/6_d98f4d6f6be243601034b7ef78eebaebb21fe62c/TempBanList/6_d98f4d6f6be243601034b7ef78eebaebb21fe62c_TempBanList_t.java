 package coolawesomeme.basics_plugin;
 
import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class TempBanList {
 
	private static LinkedList<Player> bannedPlayers = new LinkedList<Player>();
 	
 	public static void addPlayer(Player victim){
 		bannedPlayers.add(victim);
 	}
 	
 	public static void removePlayer(Player victim){
 		bannedPlayers.remove(victim);
 	}
 	
 	public static List<Player> getList(){
 		return bannedPlayers;
 	}
 	
 	public static void unbanAll(){
 		for(Player victim : bannedPlayers){
 			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pardon " + victim.getName());
 			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "unban " + victim.getName());
 		}
 	}
 }
