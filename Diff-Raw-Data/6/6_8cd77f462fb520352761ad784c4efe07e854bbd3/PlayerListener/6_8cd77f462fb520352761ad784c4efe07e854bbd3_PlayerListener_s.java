 package xpd.charsheets.listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import xpd.charsheets.CharSheets;
 import xpd.charsheets.character.sheet.Sheet;
 
 public class PlayerListener implements Listener {
 	
 	public final CharSheets plugin;
 	
 	public PlayerListener(CharSheets a){
 		this.plugin = a;
 	}
 
 	@EventHandler(priority=EventPriority.LOWEST)
	public void playerEvents(PlayerEvent event){
 		
 		Player player = event.getPlayer();
 		
 		if (event instanceof PlayerJoinEvent){
 			
 				Sheet.buildSheet(player);
 		}
 	}
 }
