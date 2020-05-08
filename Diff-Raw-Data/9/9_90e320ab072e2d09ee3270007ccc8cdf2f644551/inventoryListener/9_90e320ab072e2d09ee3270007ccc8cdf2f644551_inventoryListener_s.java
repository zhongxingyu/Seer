 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.List;
 
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 import com.zolli.rodolffoutilsreloaded.utils.expUtils;
 
 public class inventoryListener implements Listener {
 
 	private rodolffoUtilsReloaded plugin;
 	private expUtils exp = new expUtils();
 	public inventoryListener(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void itemCraft(CraftItemEvent e) {
 		
 		List<HumanEntity> viewers = e.getViewers();
 		
 		for(HumanEntity p : viewers) {
 			
 			Player pl = plugin.getServer().getPlayer(p.getName());
 			ItemStack craftingResult = e.getCurrentItem();
 			
 			if(craftingResult.getTypeId() == 384) {
 				
 				if(!(e.isShiftClick())) {
 				
 					if(pl.getTotalExperience() >= plugin.config.getInt("xpbottlecrafting")) {
 						
 						exp.awardExperience(pl, -plugin.config.getInt("xpbottlecrafting"));
 						pl.sendMessage(plugin.messages.getString("xpbank.successstored").replace("(XP)", Integer.toString(plugin.config.getInt("xpbottlecrafting"))));
 						
 					} else {
 							
 						e.setCancelled(true);
 						pl.sendMessage(plugin.messages.getString("xpbank.notenoughtexp"));
 							
 					}
 				
 				} else {
 					
					pl.sendMessage("4Krlek egyesvel vedd ki az itemeket. ;)");
 					e.setCancelled(true);
 					
 				}
 					
 			} 
 			
 		}	
 		
 	}
 	
 }
