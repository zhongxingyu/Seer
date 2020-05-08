 package nl.lolmewn;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin{
 	
 	public HashSet<Material> checked = new HashSet<Material>();
 	public HashMap<Location, Integer> counter = new HashMap<Location, Integer>();
 	public HashSet<Location> timed = new HashSet<Location>();
 	public HashSet<Integer> counters = new HashSet<Integer>();
 	public FileConfiguration config;
 	
 	private int needed;
 
 	public void onDisable() {
 		
 	}
 
 	public void onEnable() {
 		doConfig();
 		needed = config.getInt("bonemeals", 10);
 		checked.add(Material.PUMPKIN);
 		checked.add(Material.SEEDS);
 		checked.add(Material.SUGAR_CANE);
 		checked.add(Material.MELON);
 		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, new FBlock(this), Priority.Normal, this);
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, new FPlay(this), Priority.Normal, this);
 	}
 	
 	private void doConfig() {
 		config = getConfig();
 		config.addDefault("bonemeals", 10);
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 
 	public class FBlock extends BlockListener{
 		public Main plugin;
 		public FBlock(Main main) {
 			plugin = main;
 		}
 		
 		@Override
 		public void onBlockBreak(BlockBreakEvent event){
 			Player p = event.getPlayer();
 			Block b = event.getBlock();
 			if(checked.contains(b.getType())){
 				if(p.hasPermission("ferment.override")){
 					return;
 				}
 				if(counter.containsKey(b.getLocation())){
 					int already = counter.get(b.getLocation());
 					if(!(already >= needed)){
 						p.sendMessage("You can't break it yet, you have to use " + (needed - already) + " more bonemeal on it!");
 						event.setCancelled(true);
 					}else{
 						//Break it
 						counter.remove(b.getLocation());
 					}
 				}
 			}
 		}
 		
 	}
 	
 	public class FPlay extends PlayerListener{
 		public Main plugin;
 		public FPlay(Main main) {
 			plugin = main;
 		}
 		
 		@Override
 		public void onPlayerInteract(final PlayerInteractEvent event){
 			if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 				return;
 			}
 			Player p = event.getPlayer();
 			//If player has bonemeal in hand and touches one of the items, do stuff
 			if(p.getItemInHand().getTypeId() == 351 && p.getItemInHand().getData().getData() == (byte)0x15){
 				p.sendMessage("Yep");
 				if(checked.contains(event.getClickedBlock().getType())){
 					p.sendMessage("Yerp");
 					if(counter.containsKey(event.getClickedBlock().getLocation())){
 						if(timed.contains(event.getClickedBlock().getLocation())){
 							p.sendMessage("Bonemeal has been added recently, wait some time!");
 							event.setCancelled(true);
 							return;
 						}
 						counter.put(event.getClickedBlock().getLocation(),counter.get(event.getClickedBlock().getLocation()) +1);
 						p.sendMessage("Wait 1 minute before adding more bonemeal!");
 						counters.add(plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
 							public void run() {
 								timed.remove(event.getClickedBlock().getLocation());
 							}
 						}, 1200L));
 					}else{
 						counter.put(event.getClickedBlock().getLocation(), 1);
 					}
 				}
 			}
 		}
 		
 	}
 
 }
