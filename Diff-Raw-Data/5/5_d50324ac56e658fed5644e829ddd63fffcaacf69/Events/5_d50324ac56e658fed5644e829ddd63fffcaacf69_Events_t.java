 package com.ProjectTeam.SideKick;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import com.ProjectTeam.API.Back;
 import com.ProjectTeam.API.Quick;
 
 public class Events implements Listener {
 	
 	public static boolean Mystery = false;
 	public static boolean SignC = false;
 	public static boolean grief = false;
 	public static boolean SignCh = false;
 	public static boolean CPT = false;
 	public static boolean portal = false;
 	public static boolean burn = false;
 	public static boolean log = false;
 	
 	public static boolean usePorkDrops = false;
 	public static boolean cap = false;
 	public static boolean creeper = true;
 	
 	public static boolean Tools = false;
     public static boolean game = false;
     public static boolean Quit = false;
     public static boolean Join = false;
     public static boolean TheKit = false;
     public static boolean Sneak = false;
     public static boolean fly = false;
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(BlockBreakEvent event){
 		int A[]= new int[64];
 		A[0]=0;
 		A[1]=1;
 		Player p = event.getPlayer();
 		Block b = event.getBlock();
 		Location loc = p.getLocation();
 		Material iron = Material.IRON_BLOCK;
 		EntityType creeper = EntityType.CREEPER;
 		ItemStack ii = new ItemStack(Material.GOLDEN_APPLE,1);
 		if(b.getType()== iron){
 			if(Mystery){
 			if ((int) (Math.random() * 4) == 0) {
 				 ItemStack i = new ItemStack(Material.IRON_SWORD, A[1]);
 			b.getWorld().dropItem(loc, i);
 				     }
 				else if ((int) (Math.random() * 15) == 0) {
 					 b.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
 					 }
 				else if ((int) (Math.random() * 3) == 0) {
 							 ItemStack i = new ItemStack(Material.STONE_SWORD, A[1]);
 							b.getWorld().dropItemNaturally(loc,i);
 						 }
 				else if ((int) (Math.random() * 5) == 0) {
 								 ItemStack i = new ItemStack(Material.DIAMOND_SWORD, A[1]);
 								b.getWorld().dropItemNaturally(loc,i);
 
 					}
 				else if ((int) (Math.random() * 20) == 0) {
 								ItemStack i = new ItemStack(Material.DIAMOND, A[1]);
 								b.getWorld().dropItemNaturally(loc, i);
 				}
 				else if ((int) (Math.random() *70) ==0){
 					b.getWorld().spawnEntity(loc,creeper);			
 				}
 				else if ((int) (Math.random() * 30) ==0){
 					b.getWorld().spawnEntity(loc, EntityType.ENDERMAN);
 				}
 				else if ((int) (Math.random() *500) ==1){
 					b.getWorld().spawnEntity(loc, EntityType.GHAST);
 				}
 				else if ((int)(Math.random() *200) ==0){
 					ItemStack di = new ItemStack(Material.DIAMOND,1);
 					b.getWorld().dropItem(b.getLocation(), di);
 				}
 			}
 			if(b.getType()==Material.LEAVES){
 				if((int) (Math.random() *600) == 0){
 					b.getWorld().dropItemNaturally(b.getLocation(), ii);
 				}
 				if((int) (Math.random() * 100) == 0){
 					ItemStack iii = new ItemStack(Material.LEAVES);
 					b.getWorld().dropItem(b.getLocation(), iii);
 				}
 				if(grief){
 					if(!p.isOp() || !p.hasPermission("Sidekick.override")){
 					event.setCancelled(true);
 				}
 			}
 		}
 		}
 		if(log){
 			System.out.println(p.getName() + " has broken Block " + event.getBlock().getType());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockPlace(BlockPlaceEvent event){
 		if(grief){
 			Player p = event.getPlayer();
 			if(!p.isOp() || !p.hasPermission("Sidekick.override")){
 		event.setCancelled(true);
 		}
 	}
 		if(log){
 			Player p = event.getPlayer();
 			System.out.println(event.getBlockPlaced() + " Was placed by " + p.getName());
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBurn(BlockBurnEvent event){
 		if(burn){
 			event.setCancelled(true);
 		}
 	}
 	
 	//End BlockListener ==========================
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDeath(EntityDeathEvent event){
 		Entity e = event.getEntity();	   
 		World w = e.getWorld();	   
 		Location loc6 = e.getLocation();
 		ItemStack stack = new ItemStack(Material.PORK,1);
 		//Player
 		if(e instanceof org.bukkit.entity.Player){
 			//Drop the item
 			if(usePorkDrops){
 				w.dropItemNaturally(loc6, stack);
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDamage(EntityDamageEvent event) {
 		Entity e = event.getEntity();
 		if (e instanceof org.bukkit.entity.Player){
 			Player p = (Player) e;
 			if (PlayerInfo.get(p).godmode) {
 				p.setHealth(20);
 				event.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityExplode(EntityExplodeEvent event){
 		if (!creeper) {
 			event.setCancelled(true);
 		}
 	}
 	
 	//End EntityListener
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerInteract(PlayerInteractEvent event){
		try{
 		Player p = event.getPlayer(); 
         int x = event.getClickedBlock().getLocation().getBlockX();
         int y = event.getClickedBlock().getLocation().getBlockY();
         int z = event.getClickedBlock().getLocation().getBlockZ();
         if(Quick.isInMarkerMode(p.getName())){
         	if(p.hasPermission("Sidekick.marker")){
         		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
         			Quick.setWarpLocation(p.getName(), event.getClickedBlock().getLocation().add(new Vector(0,1,0)));
         			p.sendMessage(ChatColor.GOLD + "Quick Warp set at " + "x = " + ChatColor.LIGHT_PURPLE + x + " , y = " + y + " , z = " + z);
         			Quick.disableMarkerMode(p.getName());
         		}
         	}
         }
 	}catch (Exception i){}
 }
 
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerQuit(PlayerQuitEvent event){
 		if(Quit){
 		Player p = event.getPlayer();
 		Server s = p.getServer();
 		String playername = p.getName();
 		s.broadcastMessage(ChatColor.AQUA + playername + " Has Left The Server");
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event){
 		if(Join){
 			Player p = event.getPlayer();
 			Server s = p.getServer();
 			String playername = p.getName();
 			s.broadcastMessage(ChatColor.LIGHT_PURPLE + playername + " Has Joined The Server!");
 		} 
 		if(TheKit){
 		    Player p = event.getPlayer();
 			ItemStack i = new ItemStack(Material.GRASS, 10);
 			ItemStack i2 = new ItemStack(Material.WOOD_PICKAXE, 1);
 			ItemStack i3 = new ItemStack(Material.WOOD, 3);
 			p.getInventory().addItem(i);
 			p.getInventory().addItem(i2);
 			p.getInventory().addItem(i3);
 			Economy.economy.put(event.getPlayer().getName(), (double) 1);
 		}
 		if (!Economy.economy.containsKey(event.getPlayer().getName())) {
 		    Economy.economy.put(event.getPlayer().getName(), (double) 30);
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){
 		if(Sneak){
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerDeath(PlayerDeathEvent event){
 		Back.setBack(event.getEntity().getName(), event.getEntity().getLocation());
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		Back.setBack(event.getPlayer().getName(), event.getPlayer().getLocation());
 	}
 
 }
