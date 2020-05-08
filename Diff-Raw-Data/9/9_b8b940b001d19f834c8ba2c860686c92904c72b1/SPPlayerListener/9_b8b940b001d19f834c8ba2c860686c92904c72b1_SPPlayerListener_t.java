 package aor.SimplePlugin;
 
import java.util.HashSet;
 
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;
 /* Example Template
  * By Adamki11s
  * HUGE Plugin Tutorial
  */
 
 public class SPPlayerListener extends PlayerListener {
 	public static SimplePlugin plugin;
 	
 	public SPPlayerListener(SimplePlugin instance) {
 		plugin = instance;
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			/// Blah blah blah
 			Player player = event.getPlayer();
 			player.sendMessage("You just right clicked something! Congratz!");
			Block block = player.getTargetBlock(null, 100);
			block.setType(Material.BEDROCK);
 			
 		}
 	}
 	
 /*	public void onPlayerMove(PlayerMoveEvent event){
 		
 		Player player = event.getPlayer();
 		Location playerLoc = player.getLocation();
 		
 		player.sendMessage("Your X Coordinates : " + playerLoc.getX());
 		player.sendMessage("Your Y Coordinates : " + playerLoc.getY());
 		player.sendMessage("Your Z Coordinates : " + playerLoc.getZ());
 	}
 	*/
 
 }
