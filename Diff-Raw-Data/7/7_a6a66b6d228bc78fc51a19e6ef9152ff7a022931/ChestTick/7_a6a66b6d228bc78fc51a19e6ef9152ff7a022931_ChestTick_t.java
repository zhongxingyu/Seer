 package plugin;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class ChestTick{
 	
 	HashMap<Chest,Integer> chests = new HashMap<Chest,Integer>();
 	
 	public ChestTick(Server s, Plugin p){
 		Server server = s;
 		Plugin plugin = p;
 		
 		server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
 			public void run(){
 				for(Chest c:chests.keySet()){
 					if(Utility.containsFood(c.getInventory())){
 						int value = chests.get(c);
 						value = value - Utility.getRotValue(c.getLocation().getBlock().getBiome());
 						while(((value + 100) <= 3600) && (c.getInventory().contains(Material.SNOW_BALL))){
 							value = value + 100;
							c.getInventory().remove(new ItemStack(Material.SNOW_BALL,1));
 						}
 						while(((value + 1000) <= 3600) && (c.getInventory().contains(Material.SNOW_BLOCK))){
 							value = value + 1000;
							c.getInventory().remove(new ItemStack(Material.SNOW_BLOCK,1));
 						}
 						while(((value + 2400) <= 3600) && (c.getInventory().contains(Material.ICE))){
 							value = value + 2400;
							c.getInventory().remove(new ItemStack(Material.ICE,1));
 						}
 						chests.put(c, value);
 						if(value <= 0){
 							Utility.rotFood(c.getInventory(), c.getLocation().getBlock().getBiome());
 						}
 					}
 				}
 			}
 		}, 200, 200); // 72000 = 60 Minutes
 	}
 	
 }
