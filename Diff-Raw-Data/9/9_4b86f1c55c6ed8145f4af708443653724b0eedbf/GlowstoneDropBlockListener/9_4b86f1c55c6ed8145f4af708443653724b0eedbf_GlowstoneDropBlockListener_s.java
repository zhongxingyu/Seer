 package de.xghostkillerx.glowstonedrop;
 
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 
 /**
  * GlowstoneDrop for CraftBukkit/Bukkit
  * Handles the block stuff!
  * 
  * Refer to the forum thread:
  * http://bit.ly/oW6iR1
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/rcN2QB
  *
  * @author xGhOsTkiLLeRx
  * @thanks to XxFuNxX for the original GlowstoneDrop plugin!
  * 
  */
 
 public class GlowstoneDropBlockListener extends BlockListener {
 	
 	public static GlowstoneDrop plugin;
 	public GlowstoneDropBlockListener(GlowstoneDrop instance) {
 		plugin = instance;
 	}
 
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		// Check for the pickaxes
 		if (((player.getItemInHand().getTypeId() == 270)
 			|| (player.getItemInHand().getTypeId() == 274)
 			|| (player.getItemInHand().getTypeId() == 257)
 			|| (player.getItemInHand().getTypeId() == 285)
 			|| (player.getItemInHand().getTypeId() == 278)
 			|| (plugin.items.contains(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
 			|| (plugin.itemsInt.contains(event.getPlayer().getItemInHand().getTypeId())))
 			&& (event.getBlock().getTypeId() == 89)
 			&& (player.getItemInHand().getType() != Material.AIR)) {
			// Check for the config value permisions
 			if (plugin.config.getBoolean("configuration.permissions") == true) {
 				// Normal
 				if (event.getBlock().getWorld().getEnvironment().equals(Environment.NORMAL)) {
 					// Block
 					if (plugin.config.getString("worlds.normal").equalsIgnoreCase("block")) {
 						if (player.hasPermission("glowstonedrop.use.normal")) {
 							dropBlock(event);
 						} else {
 							message(player);
 						}
 					}
 				}
 				// Nether
 				if (event.getBlock().getWorld().getEnvironment().equals(Environment.NETHER)) {
 					// Block
 					if (plugin.config.getString("worlds.nether").equalsIgnoreCase("block")) {
 						if (player.hasPermission("glowstonedrop.use.nether")) {
 							dropBlock(event);
 						} else {
 							message(player);
 						}
 					}
 				}
 				// The End
 				if (event.getBlock().getWorld().getEnvironment().equals(Environment.THE_END)) {
 					// Block
 					if (plugin.config.getString("worlds.end").equalsIgnoreCase("block")) {
 						if (player.hasPermission("glowstonedrop.use.end")) {
 							dropBlock(event);
 						} else {
 							message(player);
 						}
 					}
 				}
 			}
 			// Without permissions -> no messages
 			else if (plugin.config.getBoolean("configuration.permissions") == false) {
 				// Normal
 				if (event.getBlock().getWorld().getEnvironment().equals(Environment.NORMAL)) {
 					// Block
 					if (plugin.config.getString("worlds.normal").equalsIgnoreCase("block")) {
 						event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(89, 1));
 						event.getBlock().setType(Material.AIR);
 					}
 				}
 				// Nether
 				if (event.getBlock().getWorld().getEnvironment().equals(Environment.NETHER)) {
 					// Block
 					if (plugin.config.getString("worlds.nether").equalsIgnoreCase("block")){
 						dropBlock(event);
 					}
 				}
 				// The End
 				if (event.getBlock().getWorld().getEnvironment().equals(Environment.THE_END)) {
 					// Block
 					if (plugin.config.getString("worlds.end").equalsIgnoreCase("block")) {
 						dropBlock(event);
 					}
 				}
 			}
 		}
 	}
 	
 	// Drop the block
 	public void dropBlock(BlockBreakEvent event) {
 		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(89, 1));
 		event.getBlock().setType(Material.AIR);
 	}
 	
 	// Sends a message
 	public void message(Player player) {
 		if (plugin.config.getBoolean("configuration.messages") == true) {
 			player.sendMessage(ChatColor.DARK_RED + "You don't have the permission to use GlowstoneDrop! Dropping dust instead!");
 		}
 	}
 }
