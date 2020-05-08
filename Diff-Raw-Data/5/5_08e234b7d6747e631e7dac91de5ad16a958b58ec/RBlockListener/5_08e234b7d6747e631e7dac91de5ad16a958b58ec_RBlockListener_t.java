 package net.croxis.plugins.research;
 
 import org.bukkit.GameMode;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import java.util.HashSet;
 
 @SuppressWarnings("unused")
 public class RBlockListener implements Listener{
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		if (event.getPlayer() == null)
 			return;
		if (event.getPlayer().getGameMode() == null)
			return;
 		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
 			return;
 		if (event.getPlayer().hasPermission("research")){
 			if(TechManager.players.get(event.getPlayer()).cantBreak.contains(event.getBlock().getTypeId())){
 				Research.logDebug("Canceling Break: " + event.getPlayer().getName() + "|" + event.getBlock().getType().toString());
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event){
 		if (event.getPlayer() == null)
 			return;
		if (event.getPlayer().getGameMode() == null)
			return;
 		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
 			return;
 		if (event.getPlayer().hasPermission("research")){
 			Research.logDebug("Place Event: " + event.getPlayer().getName() + "|" + Integer.toString(event.getBlock().getTypeId()));
 			if(TechManager.players.get(event.getPlayer()).cantPlace.contains(event.getBlock().getTypeId())){
 				Research.logDebug("Canceling Place: " + event.getPlayer().getName() + "|" + event.getBlock().getType().toString());
 				event.setCancelled(true);
 			}
 		}
 	}
 }
