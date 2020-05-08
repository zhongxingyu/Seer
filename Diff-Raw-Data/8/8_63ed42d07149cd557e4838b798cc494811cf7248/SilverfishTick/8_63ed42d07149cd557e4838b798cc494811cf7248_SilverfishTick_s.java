 package plugin;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 public class SilverfishTick{
 	
 	List<Location> chests = new ArrayList<Location>();
 	Random r = new Random();
 	Server server = null;
 	Plugin plugin = null;
 	
 	public SilverfishTick(Server s, Plugin p){
 		server = s;
 		plugin = p;
 		
 		server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
 			public void run(){
				Location randomChest = chests.get(r.nextInt(chests.size()-1));
				randomChest.add(0,1,0);
				randomChest.getWorld().spawnEntity(randomChest, EntityType.SILVERFISH);
 				
 				Player p[] = server.getOnlinePlayers();
 				List<Location> player = new ArrayList<Location>();
 				for(int i=0;i<p.length;i++){
 					if(p[i].getInventory().contains(Material.ROTTEN_FLESH)){
 						player.add(p[i].getLocation());
 					}
 				}
 				if(player.size() > 0){
 					Location randomPlayer = player.get(r.nextInt(player.size()-1));
 					randomPlayer.add(0,1,0);
 					randomPlayer.getWorld().spawnEntity(randomPlayer, EntityType.SILVERFISH);
 				}
 			}
 		}, 200, 200); // 1200 = 1 Minute
 	}
 	
 }
