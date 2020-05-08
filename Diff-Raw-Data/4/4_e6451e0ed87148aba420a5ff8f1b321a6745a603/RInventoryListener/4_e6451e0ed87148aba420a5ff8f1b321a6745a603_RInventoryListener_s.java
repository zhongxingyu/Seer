 package net.croxis.plugins.research;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.CraftItemEvent;
 import java.util.HashSet;
 
 @SuppressWarnings("unused")
 public class RInventoryListener implements Listener{
 	@EventHandler
 	public void onInventoryCraft(CraftItemEvent event){
 		if (event.getWhoClicked().hasPermission("research")){
 			//if(TechManager.players.get(event.getWhoClicked()).cantCraft.contains(event.getResult().getTypeId()) && event.getPlayer().hasPermission("research"))
 			if(TechManager.players.get(event.getWhoClicked()).cantCraft.contains(event.getRecipe().getResult().getTypeId())){
				Research.logDebug("Canceling Craft: " + event.getWhoClicked().getName() + "|" + event.getRecipe().toString());
 				event.setCancelled(true);
 			}
 		}
 	}
 
 }
