 package us.aaronweiss.weisscraft;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 /**
  * WeissCraft Listener to prevent non-OP block editing.
  * @author Aaron Weiss
  * @version 1.0
  */
 public class BlockEventListener implements Listener {
 	@EventHandler
 	public void stopBlockPlace(BlockPlaceEvent e) {
		if (e.getPlayer() != null && !e.getPlayer().isOp()) {
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "You cannot ignite blocks.");
 		}
 	}
 	
 	@EventHandler
 	public void stopBlockBreak(BlockBreakEvent e) {
		if (e.getPlayer() != null && !e.getPlayer().isOp()) {
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "You cannot ignite blocks.");
 		}
 	}
 	
 	@EventHandler
 	public void stopBlockIgnite(BlockIgniteEvent e) {
 		if (e.getPlayer() != null && !e.getPlayer().isOp()) {
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "You cannot ignite blocks.");
 		}
 	}
 }
