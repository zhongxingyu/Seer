 package com.isitbroken.oddarrow;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 
 public class PbEntityListener extends PlayerListener {
 		
 	private ItemStack stack = new ItemStack((Material) Material.ARROW, 64);
 	private OddArrow plugin;
 	
 	public PbEntityListener(OddArrow instantince) {
 		plugin =  instantince;
 	}
 	
 	public void onPlayerInteract (PlayerInteractEvent event)
 	{
 		if (plugin.isPlayer(event.getPlayer())){
 			if (event.getItem().getType() == Material.BOW){
 				if (event.getPlayer().getInventory().contains(Material.ARROW)){
					Arrow arrow = event.getPlayer().shootArrow();
					ArrayList<Arrow> temarrowlist = plugin.getArrowList(event.getPlayer());
 					temarrowlist.add(arrow);
 					plugin.oddArrowListHash.put(event.getPlayer(), (ArrayList<Arrow>) temarrowlist);
 					plugin.lookforarrows(event.getPlayer());
 					event.setCancelled(true);
 				}else{
 					event.getPlayer().sendMessage("You Have No Arrows!!");
 					event.getPlayer().getInventory().addItem(stack);
 					event.getPlayer().sendMessage("Have 64, go have some fun!!");
 				}
 			}
 			
 		}
 	}
 }
