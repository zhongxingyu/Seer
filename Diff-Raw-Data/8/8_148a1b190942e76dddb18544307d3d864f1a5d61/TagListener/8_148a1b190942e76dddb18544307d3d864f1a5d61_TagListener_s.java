 package co.mcme.perks.listeners;
 
 import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 
 import co.mcme.perks.MCMEPerks;
 
 /**
  * 
  * @author dags_ <dags@dags.me>
  */
 
 public class TagListener implements Listener{
 	
 	static ScoreboardManager manager;
 	static Scoreboard board;
 	static Objective objective;
 	static Team donator;
 	
 	public static void setBoard(){
 		manager = Bukkit.getScoreboardManager();
     	board = manager.getMainScoreboard();
     	clearBoard();
     	donator = board.registerNewTeam("Donators");
     	donator.setPrefix(MCMEPerks.donarTag.toString());
 	}
     
     @EventHandler (priority = EventPriority.LOWEST)
     public void playerJoin(PlayerJoinEvent event){
     	Player p = event.getPlayer();
     	if(p.hasPermission("perks.tag")){
     		donator.addPlayer(p);
     		if(!p.getDisplayName().startsWith("§")){
    			p.setPlayerListName(ChatColor.WHITE+p.getDisplayName());
     		}
     	}
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void playerQuit(PlayerQuitEvent event){
     	Player p = event.getPlayer();
     	if(donator.hasPlayer(p)){
     		donator.removePlayer(p);
     	}
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void playerKicked(PlayerKickEvent event){
     	Player p = event.getPlayer();
     	if(donator.hasPlayer(p)){
     		donator.removePlayer(p);
     	}
     }
     
     public static void clearBoard(){
     	if(!board.getTeams().isEmpty()){
     		for(Team t : board.getTeams()){
         		t.unregister();
         	}
     	}
     }
 }
