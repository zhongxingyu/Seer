 package net.mysticrealms.fireworks.scavengerhunt;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ScavengerInventory implements Runnable {
 
     private ScavengerHunt plugin;
 
     public ScavengerInventory(ScavengerHunt scavenger) {
 	plugin = scavenger;
     }
 
     @Override
     public void run() {
	    
	if (!plugin.isRunning) {
	    return;
	}
 	
 	if (plugin.end != 0 && plugin.end < System.currentTimeMillis()){
 	    plugin.stopScavengerEvent();
 	    return;
 	}
 	
 	for (Player p : plugin.getServer().getOnlinePlayers()) {
 	    if (!p.hasPermission("scavengerhunt.participate"))
 		continue;
 		
 	    Inventory i = p.getInventory();
 	    boolean hasItems = true;
 	    for (ItemStack item : plugin.items) {
 		if (!i.contains(item.getType(), item.getAmount()))
 		    hasItems = false;
 	    }
 	    if (hasItems) {
 		plugin.isRunning = false;
 		plugin.getServer().broadcastMessage(
 			ChatColor.DARK_RED + "Congratulations to "
 				+ ChatColor.GOLD + p.getDisplayName()
 				+ ChatColor.DARK_RED + "!");
 		plugin.getServer().broadcastMessage(
 			ChatColor.DARK_RED + "Prize was: ");
 		for (ItemStack reward : plugin.rewards) {
 		    plugin.getServer().broadcastMessage(
 			    ChatColor.GOLD + plugin.configToString(reward));
 		    i.addItem(reward);
 		}
 		return;
 	    }
 	}
     }
 
 }
