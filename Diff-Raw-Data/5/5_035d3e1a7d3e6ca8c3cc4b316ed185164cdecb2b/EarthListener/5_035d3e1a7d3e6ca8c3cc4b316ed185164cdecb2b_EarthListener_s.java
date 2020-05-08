 package me.shock.avatarpvp;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class EarthListener implements Listener
 {
 
 	public HashMap<String, Long> fortify = new HashMap<String, Long>();
 	public HashMap<String, Long> golem = new HashMap<String, Long>();
 
 	
 	public Main plugin;
 	
 	public EarthListener(Main instance)
 	{
 		this.plugin = instance;
 	}
 	
 	String apvp = ChatColor.BLUE + "[" + ChatColor.WHITE + "AvatarPvP" + ChatColor.BLUE + "]" + ChatColor.WHITE + ": ";
 	
 	/**
 	 * Listen to earth abilities.
 	 * Fortify - 5 seconds sphere protection.
 	 * Golem - summon a rock golem to protect you.
 	 */
 	
 	@EventHandler
 	public void earthInteract(PlayerInteractEvent event)
 	{
 		/**
 		 * Stuff we need for everything.
 		 * Set up cooldowns in seconds.
 		 */
 		
 		long golemcool = 60;
 		long fortifycool = 60;
 		Player player = event.getPlayer();
 		Action action = event.getAction();
 		if(action == Action.LEFT_CLICK_BLOCK)
 		{
 			ItemStack itemStack = player.getItemInHand();
 			ItemMeta meta = itemStack.getItemMeta();
 			List<String> lore = meta.getLore();
 			
 			if(lore.isEmpty())
 			{
 				return;
 			}
 			
 			/**
 			 * Check for lore of earth abilities added via commands.
 			 */
 			else
 			{
 				if(lore.contains(ChatColor.GREEN + "Fortify"))
 				{
 					// TODO: stuff for fortify here.
 				}
 				
 				if(lore.contains(ChatColor.GREEN + "Golem"))
 				{
 					if(player.hasPermission("avatarpvp.earth.golem"))
 					{
 						// Check if the player has used the ability already.
 						if(golem.containsKey(player.getName()))
 						{
 							long diff = (System.currentTimeMillis() - golem.get(player.getName())) / 1000;
 							
 							// Used it too recently.
 							if(golemcool > diff)
 							{
 								player.sendMessage(apvp + "You must wait " + ChatColor.RED + (golemcool - diff) + ChatColor.WHITE + " before using this again.");
 							}
 							
 							// Can use it again.
 							else
 							{
 								Block clickedBlock = event.getClickedBlock();
 								Location loc = clickedBlock.getLocation();
 								loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
 								golem.remove(player.getName());
 								golem.put(player.getName(), System.currentTimeMillis());
 								player.sendMessage(apvp + "Spawned an iron golem to protect you.");
 							}
 						}
 						
 						// Player hasn't already used it.
 						else
 						{
 							Block clickedBlock = event.getClickedBlock();
 							Location loc = clickedBlock.getLocation();
 							loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
 							golem.put(player.getName(), System.currentTimeMillis());
 						}
 					}
 					else
 					{
 						player.sendMessage(apvp + "You don't have permission to use this ability.");
 					}
 				}
 			}
 		}
 		if(action == Action.LEFT_CLICK_AIR)
 		{
 			/**
 			 * Need to handle left click air so that way we get the location
 			 * of the player and not the clicked block so they don't spawn
 			 * the box on themselves.
 			 */
 			ItemStack itemStack = player.getItemInHand();
 			ItemMeta meta = itemStack.getItemMeta();
 			List<String> lore = meta.getLore();
 			
 			if(lore.isEmpty())
 			{
 				return;
 			}
 			
 			if(lore.contains(ChatColor.GREEN + "Fortify"))
 			{
 				if(player.hasPermission("avatarpvp.earth.fortify"))
 				{
 					if(fortify.containsKey(player.getName()))
 					{
 						long diff = (System.currentTimeMillis() - golem.get(player.getName())) / 1000;
 						
 						// Used it too recently.
 						if(fortifycool > diff)
 						{
 							player.sendMessage(apvp + "You must wait " + ChatColor.RED + (fortifycool - diff) + ChatColor.WHITE + " before using this again.");
 						}
 						// Can use the ability again.
 						else
 						{
 							Location loc = player.getLocation();
 							
 							Location loc1 = loc.add(2, -1, -2);
 							Location loc2 = loc.add(2, -1, -1);
 							Location loc3 = loc.add(2, -1, 0);
 							Location loc4 = loc.add(2, -1, 1);
 							Location loc5 = loc.add(2, -1, 1);
 							Location loc6 = loc.add(-1, -1, 2);
 							Location loc7 = loc.add(0, -1, 2);
 							Location loc8 = loc.add(1, -1, 2);
 							Location loc9 = loc.add(-2, -1, -2);
 							Location loc10 = loc.add(-2, -1, -1);
 							Location loc11 = loc.add(-2, -1, 0);
 							Location loc12 = loc.add(-2, -1, 1);
 							Location loc13 = loc.add(-2, -1, 2);
 							Location loc14 = loc.add(-1, -1, -2);
 							Location loc15 = loc.add(0, -1, -2);
 							Location loc16 = loc.add(1, -1, -2);
 
 							/**
 							 * Lets spawn some obsidian around the player.
 							 * Spawn it like this four blocks high:
 							 *   12345
 							 *   6   6
 							 *   5 P 7
 							 *   4   8
 							 *   32109
 							 */
 							
 							int count = 1;
 							
 							// Spawn the blocks 
 							for(count = 1; count < 4; count++)
 							{
 								
 								Location locx1 = loc1.add(0, count, 0);
 								Location locx2 = loc2.add(0, count, 0);
 								Location locx3 = loc3.add(0, count, 0);
 								Location locx4 = loc4.add(0, count, 0);
 								Location locx5 = loc5.add(0, count, 0);
 								Location locx6 = loc6.add(0, count, 0);
 								Location locx7 = loc7.add(0, count, 0);
 								Location locx8 = loc8.add(0, count, 0);
 								Location locx9 = loc9.add(0, count, 0);
 								Location locx10 = loc10.add(0, count, 0);
 								Location locx11 = loc11.add(0, count, 0);
 								Location locx12 = loc12.add(0, count, 0);
 								Location locx13 = loc13.add(0, count, 0);
 								Location locx14 = loc14.add(0, count, 0);
 								Location locx15 = loc15.add(0, count, 0);
 								Location locx16 = loc16.add(0, count, 0);
 								
 								loc.getWorld().spawnFallingBlock(locx1, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx2, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx3, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx4, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx5, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx6, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx7, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx8, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx9, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx10, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx11, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx12, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx13, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx14, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx15, Material.OBSIDIAN, (byte) 0);
 								loc.getWorld().spawnFallingBlock(locx16, Material.OBSIDIAN, (byte) 0);
 							}
 							player.sendMessage(apvp + "Obsidian fortification is protecting you.");
 						}
 					}
 					else
 					{
Location loc = player.getLocation();
 						
 						Location loc1 = loc.add(2, -1, -2);
 						Location loc2 = loc.add(2, -1, -1);
 						Location loc3 = loc.add(2, -1, 0);
 						Location loc4 = loc.add(2, -1, 1);
 						Location loc5 = loc.add(2, -1, 1);
 						Location loc6 = loc.add(-1, -1, 2);
 						Location loc7 = loc.add(0, -1, 2);
 						Location loc8 = loc.add(1, -1, 2);
 						Location loc9 = loc.add(-2, -1, -2);
 						Location loc10 = loc.add(-2, -1, -1);
 						Location loc11 = loc.add(-2, -1, 0);
 						Location loc12 = loc.add(-2, -1, 1);
 						Location loc13 = loc.add(-2, -1, 2);
 						Location loc14 = loc.add(-1, -1, -2);
 						Location loc15 = loc.add(0, -1, -2);
 						Location loc16 = loc.add(1, -1, -2);
 
 						/**
 						 * Lets spawn some obsidian around the player.
 						 * Spawn it like this four blocks high:
 						 *   12345
 						 *   6   6
 						 *   5 P 7
 						 *   4   8
 						 *   32109
 						 */
 						
 						int count = 1;
 						
 						// Spawn the blocks 
 						for(count = 1; count < 4; count++)
 						{
 							
 							Location locx1 = loc1.add(0, count, 0);
 							Location locx2 = loc2.add(0, count, 0);
 							Location locx3 = loc3.add(0, count, 0);
 							Location locx4 = loc4.add(0, count, 0);
 							Location locx5 = loc5.add(0, count, 0);
 							Location locx6 = loc6.add(0, count, 0);
 							Location locx7 = loc7.add(0, count, 0);
 							Location locx8 = loc8.add(0, count, 0);
 							Location locx9 = loc9.add(0, count, 0);
 							Location locx10 = loc10.add(0, count, 0);
 							Location locx11 = loc11.add(0, count, 0);
 							Location locx12 = loc12.add(0, count, 0);
 							Location locx13 = loc13.add(0, count, 0);
 							Location locx14 = loc14.add(0, count, 0);
 							Location locx15 = loc15.add(0, count, 0);
 							Location locx16 = loc16.add(0, count, 0);
 							
 							loc.getWorld().spawnFallingBlock(locx1, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx2, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx3, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx4, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx5, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx6, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx7, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx8, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx9, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx10, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx11, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx12, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx13, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx14, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx15, Material.OBSIDIAN, (byte) 0);
 							loc.getWorld().spawnFallingBlock(locx16, Material.OBSIDIAN, (byte) 0);
 						}
 						player.sendMessage(apvp + "Obsidian fortification is protecting you.");
 					}
 				}
 				else
 				{
 					player.sendMessage(apvp + "You don't have permission to use this ability.");
 				}
 			}
 		}
 		// Everything must be above this.
 	}
 	
 	/**
 	 * Get the iron golem spawned then 
 	 * make it so it doesn't attack anyone else.
 	 */
 	
 	@EventHandler
 	public void onGolemTarget(EntityTargetLivingEntityEvent event)
 	{
 		EntityType type = event.getEntityType();
 		if(type == EntityType.IRON_GOLEM)
 		{
 			LivingEntity entityTarget = event.getTarget();
 			if(entityTarget instanceof Player)
 			{
 				Player player = (Player) entityTarget;
 				if(player.hasPermission("avatarpvp.earth"))
 				{
 					event.setCancelled(true);
 				}
 			}
 			return;
 		}
 		return;
 	}
 }
