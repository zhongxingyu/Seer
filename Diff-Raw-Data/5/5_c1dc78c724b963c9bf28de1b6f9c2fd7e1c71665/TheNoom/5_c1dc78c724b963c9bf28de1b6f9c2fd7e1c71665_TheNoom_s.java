 package me.supermaxman.TheNoom;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.EnderCrystal;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import java.util.logging.Logger;
 import java.util.HashMap;
 import java.util.Random;
 
 public class TheNoom extends JavaPlugin implements Listener
 {
 	public static TheNoom plugin;
     private Logger log = Logger.getLogger("Minecraft");
     PluginDescriptionFile pluginDescriptionFile;
 	static String world = "thenoom";
 	static HashMap<Player, Integer> BrokenCrystals = new HashMap<Player, Integer>();
     
 	@Override
     public void onEnable(){
         getServer().getPluginManager().registerEvents(new TheNoom(), this);
         pluginDescriptionFile = getDescription();
         log.info("[TheNoom] v" + pluginDescriptionFile.getVersion() + " enabled");
         TheNoom.plugin = this;
     }
     
     @Override
     public void onDisable(){
         log.info("[TheNoom] v" + pluginDescriptionFile.getVersion() +" disabled");
     }
     
     @Override
     public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
     {
         return new TheNoomChunkGenerator(id);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerMove(PlayerMoveEvent event){
 		Player player = event.getPlayer();
 		if (player.getWorld().getName().equalsIgnoreCase(world)){
 			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 5));
 			if ((player.getInventory().getHelmet()==null)||(player.getInventory().getHelmet().getType()!=Material.GOLD_HELMET)){
 				player.damage(1);
 			
 			}
 		}
 	}
 	
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamage(EntityDamageEvent event){
 		Entity e = event.getEntity();
 			if (event.getCause()==DamageCause.FALL){
 			if (e.getWorld().getName().equalsIgnoreCase(world)){
 				event.setCancelled(true);
 		}
 	}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onCreatureSpawn(CreatureSpawnEvent event){
 		Entity e = event.getEntity();
 			if (e.getWorld().getName().equalsIgnoreCase(world)){
 				if (e instanceof PigZombie){
 					if ((new Random().nextInt(3) < 2)) {
 					event.setCancelled(true);
 					}
 				}
 				
 			}
 	}
 	
 	@EventHandler
 	public void onEntityDeath(final EntityDeathEvent event) {
 		if(event.getEntity() instanceof EnderDragon){
 				Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){ 
 				public void run() {	
 					Location loc = event.getEntity().getLocation();
 					event.getEntity().remove();
 					event.setDroppedExp(50);
 					loc.getBlock().setType(Material.DRAGON_EGG);
 				}
 			}, 60);
 		}
 	}
 
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(EntityExplodeEvent event){
 		Entity e = event.getEntity();
 			if (e instanceof EnderCrystal){
 			if (e.getWorld().getName().equalsIgnoreCase(world)){
 				
 				for(Entity ent : e.getNearbyEntities(10, 10, 10)){
 					if (ent instanceof Player){
 						Player p = (Player) ent;
 						if(BrokenCrystals.get(p) == null){
 							BrokenCrystals.put(p, 0);
 						}
 						BrokenCrystals.put(p, BrokenCrystals.get(p)+1);
 						if (BrokenCrystals.get(p) == 1){
 							p.sendMessage(ChatColor.DARK_RED+""+BrokenCrystals.get(p)+ChatColor.DARK_AQUA+" Ender Crystal Has Been Destroyed!");
 						}else{
 							p.sendMessage(ChatColor.DARK_RED+""+BrokenCrystals.get(p)+ChatColor.DARK_AQUA+" Ender Crystals Have Been Destroyed!");
 						}
 						if (BrokenCrystals.get(p) == 5){
 							p.sendMessage(ChatColor.DARK_RED+"The Noom Feels Restless. . .");
 						}else if (BrokenCrystals.get(p) == 9){
 							p.sendMessage(ChatColor.DARK_RED+"The Noom Begins To Tremble. . .");
 						}else if(BrokenCrystals.get(p) == 10){
 							BrokenCrystals.put(p, 0);
 							p.sendMessage(ChatColor.DARK_RED+"The Noom Has Awakened!");
 							p.teleport(ent.getLocation().add(0,12,0));
 							p.getServer().getWorld(world).spawn(p.getLocation().add(0, 30, 0), EnderDragon.class);
 						}
 					}
 				}
 
 		}
 	}
 	}
     
     @EventHandler
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player p = event.getPlayer();
 		if ((p.getWorld().getName().equalsIgnoreCase(world))&&(event.isCancelled()==false)){
 			for (Entity e : p.getNearbyEntities(10, 20, 10)){
 				if (e instanceof PigZombie){
					((PigZombie) e).damage(0, p);
 				}
 			}
 		}
 	}
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event){
 		Player p = event.getPlayer();
 		if ((p.getWorld().getName().equalsIgnoreCase(world))&&(event.isCancelled()==false)){
 			for (Entity e : p.getNearbyEntities(10, 20, 10)){
 				if (e instanceof PigZombie){
					((PigZombie) e).damage(0, p);
 				}
 			}
 		}
 	}
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event){
     	final Player p = event.getPlayer();
     	if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
     	if (event.getClickedBlock().getType()==Material.STONE_BUTTON){
     		final Block b = event.getClickedBlock();
         		if ((b.getRelative(BlockFace.DOWN).getType()==Material.DIAMOND_BLOCK)&&
             			(b.getRelative(BlockFace.DOWN).getRelative(1, 0, 1).getType()==Material.IRON_BLOCK)&&
             			(b.getRelative(BlockFace.DOWN).getRelative(-1, 0, -1).getType()==Material.IRON_BLOCK)&&
             			(b.getRelative(BlockFace.DOWN).getRelative(-1, 0, 1).getType()==Material.IRON_BLOCK)&&
             			(b.getRelative(BlockFace.DOWN).getRelative(1, 0, -1).getType()==Material.IRON_BLOCK)&&
             			(b.getRelative(BlockFace.DOWN).getRelative(0, 3, 0).getType()==Material.GOLD_BLOCK)){
             			if((b.getRelative(BlockFace.NORTH).getType()==Material.IRON_BLOCK)||
             					(b.getRelative(BlockFace.SOUTH).getType()==Material.IRON_BLOCK)||
             					(b.getRelative(BlockFace.EAST).getType()==Material.IRON_BLOCK)||
             					(b.getRelative(BlockFace.WEST).getType()==Material.IRON_BLOCK)){
             	   			if((b.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).getType()==Material.IRON_BLOCK)||
                 					(b.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).getType()==Material.IRON_BLOCK)||
                 					(b.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getType()==Material.IRON_BLOCK)||
                 					(b.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getType()==Material.IRON_BLOCK)){
             	   				int c1 = 0;
             	   				if ((c1==0)&&(b.getRelative(BlockFace.NORTH).getTypeId()==0)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).getTypeId()==0)&&(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP, 2).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP, 3).getType()==Material.GLASS)){b.getRelative(BlockFace.NORTH).setType(Material.IRON_BLOCK);b.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).setType(Material.GLASS);b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP, 2).setTypeId(0);b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP, 3).setTypeId(0); c1 = 1;}
             	   				if ((c1==0)&&(b.getRelative(BlockFace.SOUTH).getTypeId()==0)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).getTypeId()==0)&&(b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP, 2).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP, 3).getType()==Material.GLASS)){b.getRelative(BlockFace.SOUTH).setType(Material.IRON_BLOCK);b.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).setType(Material.GLASS);b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP, 2).setTypeId(0);b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP, 3).setTypeId(0); c1 = 2;}
             	   				if ((c1==0)&&(b.getRelative(BlockFace.EAST).getTypeId()==0)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getTypeId()==0)&&(b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP, 2).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP, 3).getType()==Material.GLASS)){b.getRelative(BlockFace.EAST).setType(Material.IRON_BLOCK);b.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).setType(Material.GLASS);b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP, 2).setTypeId(0);b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP, 3).setTypeId(0); c1 = 3;}
             	   				if ((c1==0)&&(b.getRelative(BlockFace.WEST).getTypeId()==0)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getTypeId()==0)&&(b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP, 2).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP, 3).getType()==Material.GLASS)){b.getRelative(BlockFace.WEST).setType(Material.IRON_BLOCK);b.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).setType(Material.GLASS);b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP, 2).setTypeId(0);b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP, 3).setTypeId(0); c1 = 4;}
             	   				boolean isRocket = false;
             	   				if ((c1 == 1)&&(b.getRelative(BlockFace.SOUTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.EAST).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.WEST).getType()==Material.IRON_BLOCK)){isRocket = true;}
             	   				if((c1 == 2)&&(b.getRelative(BlockFace.NORTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.EAST).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.WEST).getType()==Material.IRON_BLOCK)){isRocket = true;}
             	   				if((c1 == 3)&&(b.getRelative(BlockFace.NORTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.SOUTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.WEST).getType()==Material.IRON_BLOCK)){isRocket = true;}
             	   				if((c1 == 4)&&(b.getRelative(BlockFace.NORTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.EAST).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.SOUTH).getType()==Material.IRON_BLOCK)){isRocket = true;}
             					final int c = c1;
             					
             					
             					
             	   				if ((c!=0)&&(isRocket)){
                 	   				Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){ 
                 						public void run() {	
                 							if ((p.getLocation().getBlockX()==b.getLocation().getBlockX())&&(p.getLocation().getBlockY()==b.getLocation().getBlockY())&&(p.getLocation().getBlockZ()==b.getLocation().getBlockZ())){
                         					
                 								
                 								
                 								p.teleport(p.getServer().getWorld(world).getBlockAt(p.getLocation().getBlockX()^4, heighestBlockAtIgnoreRoof(p.getServer().getWorld(world),p.getLocation().getBlockX()^4,p.getLocation().getBlockZ()^4 ), p.getLocation().getBlockZ()^4).getLocation());
                 							}
                 						}
                 					}, 80);
             	   				Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){ 
             						public void run() {	
             							
             									if (c==1){
             										if ((b.getRelative(BlockFace.NORTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).getType()==Material.GLASS)){
                 										b.getRelative(BlockFace.NORTH).setType(Material.AIR);
                 										b.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).setType(Material.AIR);
                 										b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP, 2).setType(Material.IRON_BLOCK);
                 										b.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP, 3).setType(Material.GLASS);
             										}
             									}else if(c == 2){
             										if ((b.getRelative(BlockFace.SOUTH).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).getType()==Material.GLASS)){
 
             										b.getRelative(BlockFace.SOUTH).setType(Material.AIR);
             										b.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).setType(Material.AIR);
             										b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP, 2).setType(Material.IRON_BLOCK);
             										b.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP, 3).setType(Material.GLASS);
             										}
             									}else if (c == 3){
             										if ((b.getRelative(BlockFace.EAST).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getType()==Material.GLASS)){
 
             										b.getRelative(BlockFace.EAST).setType(Material.AIR);
             										b.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).setType(Material.AIR);
             										b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP, 2).setType(Material.IRON_BLOCK);
             										b.getRelative(BlockFace.EAST).getRelative(BlockFace.UP, 3).setType(Material.GLASS);
             										}
             									}else if(c == 4){
             										if ((b.getRelative(BlockFace.WEST).getType()==Material.IRON_BLOCK)&&(b.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getType()==Material.GLASS)){
 
             										b.getRelative(BlockFace.WEST).setType(Material.AIR);
             										b.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).setType(Material.AIR);
             										b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP, 2).setType(Material.IRON_BLOCK);
             										b.getRelative(BlockFace.WEST).getRelative(BlockFace.UP, 3).setType(Material.GLASS);
             										}
             									}
             									
             									
             						}
             					}, 100);
             	   				
             	   				}
             	   				
             	   				
             	   				
             	   				
             	   				
             	   				
                 			}
             	   			
             			}
             				
             				
             				
             				
             			}
     			
             		
     	}else if(event.getClickedBlock().getType()==Material.DRAGON_EGG){
     		event.setCancelled(true);
     	}
     	}else if (event.getAction() == Action.LEFT_CLICK_BLOCK){
     		if ((event.getClickedBlock().getType()==Material.DRAGON_EGG)&&(event.getClickedBlock().getLocation().getWorld().getName().equalsIgnoreCase(world))){
     			if((p.getInventory().firstEmpty()<=36)&&(p.getInventory().firstEmpty()>=0)){
         		event.setCancelled(true);
     			p.getInventory().setItem(p.getInventory().firstEmpty(), new ItemStack(Material.DRAGON_EGG,1));
     			event.getClickedBlock().setTypeId(0);
     			}
     		}
     	}
     		
     	
     }
     
     public int heighestBlockAtIgnoreRoof(World w, int x, int z) {
     	w.getChunkAt(z, z);
     	int i = w.getSeaLevel()+2;
     	while(i<250){
     		if(w.getBlockAt(x, i, z).getTypeId()==0){
     			break;
     		}
     		i++;
     	}
     	
     	
     	return i;
 	}
     
     
     
 }
