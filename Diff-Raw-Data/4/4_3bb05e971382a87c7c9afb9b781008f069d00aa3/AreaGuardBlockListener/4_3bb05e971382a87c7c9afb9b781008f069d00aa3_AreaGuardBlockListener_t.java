 package com.zand.bukkit.areaguard.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.*;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.block.*;
 
 import com.zand.areaguard.Config;
 import com.zand.areaguard.area.Cuboid;
 import com.zand.bukkit.areaguard.AreaGuard;
 import com.zand.bukkit.areaguard.Session;
 
 public class AreaGuardBlockListener extends BlockListener {
 	
 	public final AreaGuard plugin;
 
     public AreaGuardBlockListener(AreaGuard instance) {
     	plugin = instance;
     }
     
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
     	if (event.isCancelled()) return;
     	
     	Player player = (Player)event.getPlayer();
     	Block block = event.getBlock();
     	
     	checkCanBuild(player, block, event);
     }
     
     @Override
     public void onBlockDamage(BlockDamageEvent event) {
     	Player player = event.getPlayer();
     	Block block = event.getBlock();
     	
     	// Set Point
     	if (	event.getDamageLevel() == BlockDamageLevel.STARTED &&
     			player.getItemInHand().getTypeId() == Config.createTool) {
     		Session ps = plugin.getSession(player);
     		ps.select(Config.storage.getWorld(player.getWorld().getName()));
     		ps.selectLeft(block.getX(), block.getY(), block.getZ());
     		player.sendMessage("Left Point Selected (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ") set");
     	}
     	
     	// Don't process cancelled events beyond this point
     	if (event.isCancelled()) return;
     	
    	if (!checkCanBuild(player, block, event)) return;
     }
     
     @Override
     public void onBlockRightClick(BlockRightClickEvent event) {
     	Block block = event.getBlock();
     	Player player = event.getPlayer();
     	
     	// Set Point
     	if (event.getItemInHand().getTypeId() == Config.createTool) {
     		Session ps = plugin.getSession(player);
     		ps.select(Config.storage.getWorld(player.getWorld().getName()));
     		ps.selectRight(block.getX(), block.getY(), block.getZ());
     		player.sendMessage("Right Point Selected (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ") set");
     	}
     	
     	// Check Area
     	else if (event.getItemInHand().getTypeId() == Config.checkTool) {
     		Cuboid cuboid = Config.storage.getWorld(player.getWorld().getName())
         		.getCuboid(block.getX(), block.getY(), block.getZ());
     		if (cuboid != null && cuboid.exsists())
         		plugin.showCuboidInfo(event.getPlayer(), cuboid);
     		else player.sendMessage(ChatColor.YELLOW + "not an Area");
     	}
     }
 	
     @Override
 	public void onBlockInteract(BlockInteractEvent event) {
 		if (event.isCancelled()) return;
 		if (!event.isPlayer()) return;
 		
 		Player player = (Player)event.getEntity();
 		Block block = event.getBlock();
 		
 		checkCanUse(player, block, event);
 	}
 	
 	/**
 	 * Checks if a player is allowed to create or destroy a block.
 	 * @param player	The player to check for
 	 * @param block		The block to check
 	 * @param event		The event to cancel if he can't
 	 * @return			If the player can create or destroy
 	 */
 	public boolean checkCanBuild(Player player, Block block, Cancellable event) {
 		// Check build
 		if (!plugin.checkEvent(event, player, 
 				new String[] {"owners", "allow", "build"}, 
 				block.getX(), block.getY(), block.getZ()))
 			return false;
 		
 		// Check can use
 		if (!checkCanUse(player, block, event))
 			return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Checks if a player is allowed to use a block.
 	 * @param player	The player to check for
 	 * @param block		The block to check
 	 * @param event		The event to cancel if he can't
 	 * @return			If the player can use the block
 	 */
 	public boolean checkCanUse(Player player, Block block, Cancellable event) {
 		Material mat = block.getType();
 		String list = mat.name().toLowerCase().replaceAll("\\W", "").replaceAll("_", "-");
 		
 		// If it can be opened
 		if (block.getState() instanceof ContainerBlock) {
 			if (!plugin.checkEvent(event, player, 
 				new String[] {"owners", "allow", "open", list}, 
 				block.getX(), block.getY(), block.getZ())) return false;
 		}
 		
 		return plugin.checkEvent(event, player, 
 				new String[] {"owners", "allow", "use", list}, 
 				block.getX(), block.getY(), block.getZ());
 	}
 }
