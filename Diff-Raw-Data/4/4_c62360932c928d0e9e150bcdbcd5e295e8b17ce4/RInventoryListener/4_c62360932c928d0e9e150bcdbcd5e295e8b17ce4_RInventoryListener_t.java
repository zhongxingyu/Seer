 package net.croxis.plugins.research;
 
import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import java.util.HashSet;
 
 @SuppressWarnings("unused")
 public class RInventoryListener implements Listener{
	@EventHandler
 	public void onInventoryCraft(InventoryCraftEvent event){
 		if (event.getPlayer().hasPermission("research")){
 			event.getResult().getTypeId();
 			if(TechManager.players.get(event.getPlayer()).cantCraft.contains(event.getResult().getTypeId()) && event.getPlayer().hasPermission("research"))
 				event.setCancelled(true);
 		}
 	}
 
 }
