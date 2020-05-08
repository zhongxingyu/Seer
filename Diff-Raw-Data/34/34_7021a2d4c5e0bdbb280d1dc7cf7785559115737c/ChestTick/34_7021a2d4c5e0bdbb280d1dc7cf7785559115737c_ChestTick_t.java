 package plugin;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class ChestTick{
 	
 	HashMap<Chest,Integer> chests = new HashMap<Chest,Integer>();
	Server server = null;
	Plugin plugin = null;
 	
 	public ChestTick(Server s, Plugin p){
		server = s;
		plugin = p;
 		
 		server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
 			public void run(){
				server.broadcastMessage("Tick!");
 				for(Chest c:chests.keySet()){
					server.broadcastMessage("Enter Chest ->");
 					if(Utility.containsFood(c.getInventory())){
						server.broadcastMessage("Contains Food!");
 						int value = chests.get(c);
						server.broadcastMessage("Start Value: "+value);
 						value = value - Utility.getRotValue(c.getLocation().getBlock().getBiome());
						server.broadcastMessage("Value after subtract: "+value);
 						while(((value + 100) <= 3600) && (c.getInventory().contains(Material.SNOW_BALL))){
 							value = value + 100;
							ItemStack is = c.getInventory().getItem(c.getInventory().first(Material.SNOW_BALL));
							is.setAmount(is.getAmount()-1);
							server.broadcastMessage("Snow Ball!");
 						}
 						while(((value + 1000) <= 3600) && (c.getInventory().contains(Material.SNOW_BLOCK))){
 							value = value + 1000;
							ItemStack is = c.getInventory().getItem(c.getInventory().first(Material.SNOW_BLOCK));
							is.setAmount(is.getAmount()-1);
							server.broadcastMessage("Snow Block!");
 						}
 						while(((value + 2400) <= 3600) && (c.getInventory().contains(Material.ICE))){
 							value = value + 2400;
							ItemStack is = c.getInventory().getItem(c.getInventory().first(Material.ICE));
							is.setAmount(is.getAmount()-1);
							server.broadcastMessage("Ice!");
 						}
 						chests.put(c, value);
						server.broadcastMessage("New Value: "+value);
 						if(value <= 0){
 							Utility.rotFood(c.getInventory(), c.getLocation().getBlock().getBiome());
 						}
 					}
 				}
 			}
 		}, 200, 200); // 72000 = 60 Minutes
 	}
 	
 }
