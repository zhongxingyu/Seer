 package de.dustplanet.superwheat;
 
 import java.util.Random;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * SuperWheat for CraftBukkit/Bukkit
  * Handles block activities!
  * Refer to the forum thread:
  * http://bit.ly/superwheatthread
  * Refer to the dev.bukkit.org page: http://bit.ly/superwheatpage
  * 
  * @author  xGhOsTkiLLeRx
  * @thanks  to thescreem for the original SuperWheat plugin!
  */
 
 public class SuperWheatBlockListener implements Listener {
 	private SuperWheat plugin;
 	private Random random = new Random();
 	
 	public SuperWheatBlockListener(SuperWheat instance){
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event)  {
 		// Physical means jump on it
 		if (event.getAction() == Action.PHYSICAL) {
 			Block block = event.getClickedBlock();
 			// Is the world on the list?
 			if (block == null) return;
 			if (!plugin.enabledWorlds.contains(block.getWorld().getName())) return;
 			// If the block is farmland (soil) & matches any of the no tramplign blocks
 			if (block.getType() == Material.SOIL && ((!plugin.wheatTrampling && block.getRelative(BlockFace.UP).getType() == Material.CROPS)
 					|| (!plugin.carrotTrampling && block.getRelative(BlockFace.UP).getType() == Material.CARROT)
 					|| (!plugin.potatoTrampling && block.getRelative(BlockFace.UP).getType() == Material.POTATO))){
 				// Deny event and set the block
 				event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
 				event.setCancelled(true);
 				block.setTypeIdAndData(block.getType().getId(), block.getData(), true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockFromTo(BlockFromToEvent event) {
 		// If water flows "over" it
 		if (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.STATIONARY_WATER) {
 			final Block block = event.getToBlock();
 			// Is the world on the list?
 			if (!plugin.enabledWorlds.contains(block.getWorld().getName())) return;
 			byte data = block.getData();
 			// If the block is a wheat
 			if (event.getToBlock().getType() == Material.CROPS && plugin.wheatEnabled) {
 				// Fully grown
 				if (data == 7) {
 					// Should we cancel this?
 					if (plugin.wheatPreventWaterGrown) event.setCancelled(true);
 					else {
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						if (plugin.wheatWaterDropSeeds) dropSeeds(block);
 						if (plugin.wheatWaterDropWheat) dropWheat(block);
 						blockSchedulder(block, Material.CROPS.getId(), (byte) 0, plugin.wheatDelayWater, true);
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.wheatPreventWater) event.setCancelled(true);
 			}
 			// If the block is a nether warts block
 			else if (event.getToBlock().getType() == Material.NETHER_WARTS && plugin.netherWartEnabled) {
 				// Fully grown
 				if (data == 3) {
 					// Should we cancel this?
 					if (plugin.netherWartPreventWaterGrown) event.setCancelled(true);
 					else {
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						if (plugin.netherWartWaterDropNetherWart) dropNetherWart(block);
 						blockSchedulder(block, Material.NETHER_WARTS.getId(), (byte) 0, plugin.netherWartDelayWater, false);
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.netherWartPreventWater) event.setCancelled(true);
 			}
 			// If the block is cocoa block
 			else if (event.getToBlock().getType() == Material.COCOA && plugin.cocoaPlantEnabled) {
 				// Fully grown
 				if (data >= 8) {
 					// Should we cancel this?
 					if (plugin.cocoaPlantPreventWaterGrown) event.setCancelled(true);
 					else {
 						final byte dataNew = (byte) (block.getData() - 8);
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						if (plugin.cocoaPlantWaterDropCocoaPlant) dropCocoaBeans(block);
 						blockSchedulder(block, Material.COCOA.getId(), dataNew, plugin.cocoaPlantDelayWater, false);
 	
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.cocoaPlantPreventWater) event.setCancelled(true);
 			}
 			// If the block is carrot block
 			else if (event.getToBlock().getType() == Material.CARROT && plugin.carrotEnabled) {
 				// Fully grown
 				if (data == 7) {
 					// Should we cancel this?
 					if (plugin.carrotPreventWaterGrown) event.setCancelled(true);
 					else {
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						if (plugin.carrotWaterDropCarrot) dropCarrot(block);
 						blockSchedulder(block, Material.CARROT.getId(), (byte) 0, plugin.carrotDelayWater, true);
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.carrotPreventWater) event.setCancelled(true);
 			}
 			// If the block is potato block
 			else if (event.getToBlock().getType() == Material.POTATO && plugin.potatoEnabled) {
 				// Fully grown
 				if (data == 7) {
 					// Should we cancel this?
 					if (plugin.potatoPreventWaterGrown) event.setCancelled(true);
 					else {
 						// Set to air and drop. Then wait the delay and make it a premature block again
 						if (plugin.potatoWaterDropPotato) dropPotato(block);
 						blockSchedulder(block, Material.POTATO.getId(), (byte) 0, plugin.potatoDelayWater, true);
 					}
 				}
 				// MUST be a premature block, cancel it or not?
 				else if (plugin.potatoPreventWater) event.setCancelled(true);
 			}
 			else if (event.getToBlock().getType() == Material.SUGAR_CANE_BLOCK && plugin.sugarCaneEnabled) {
 				if (plugin.sugarCanePreventWater) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					if (plugin.sugarCaneWaterDropSugarCane) dropSugarCane(block);
 					blockSchedulder(block, Material.SUGAR_CANE_BLOCK.getId(), (byte) 0, plugin.sugarCaneDelayWater, true);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockPistonExtend (BlockPistonExtendEvent event) {
 		// Get the block the piston pushed
 		final Block block = event.getBlock().getRelative(event.getDirection());
 		byte data = block.getData();
 		// If the block is a wheat block
 		if (block.getType() == Material.CROPS && plugin.wheatEnabled) {
 			// Is the world on the list?
 			if (!plugin.enabledWorlds.contains(block.getWorld().getName())) return;
 			// Mature
 			if (data == 7) {
 				// Should we cancel this?
 				if (plugin.wheatPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					if (plugin.wheatPistonDropSeeds) dropSeeds(block);
 					if (plugin.wheatPistonDropWheat) dropWheat(block);
 					blockSchedulder(block, Material.CROPS.getId(), (byte) 0, plugin.wheatDelayPiston, true);
 				}
 			}
 			// MUST be a premature block, cancel it or not?
 			else if (plugin.wheatPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.NETHER_WARTS && plugin.netherWartEnabled) {
 			// Mature
 			if (data == 3) {
 				// Should we cancel this?
 				if (plugin.netherWartPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					if (plugin.netherWartPistonDropNetherWart) dropNetherWart(block);
 					blockSchedulder(block, Material.NETHER_WARTS.getId(), (byte) 0, plugin.netherWartDelayPiston, false);
 				}
 			}
 			// MUST be a premature block, cancel it or not?
 			else if (plugin.netherWartPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.COCOA && plugin.cocoaPlantEnabled) {
 			// Mature
 			if (data >= 8) {
 				// Should we cancel this?
 				if (plugin.cocoaPlantPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Minus 8 to keep the relative
 					final byte dataNew = (byte) (block.getData() - 8);
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					if (plugin.cocoaPlantPistonDropCocoaPlant) dropCocoaBeans(block);
 					blockSchedulder(block, Material.COCOA.getId(), dataNew, plugin.cocoaPlantDelayPiston, false);
 				}
 			}
 			// MUST be a premature block, cancel it or not?
 			else if (plugin.cocoaPlantPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.CARROT && plugin.carrotEnabled) {
 			// Mature
 			if (data == 7) {
 				// Should we cancel this?
 				if (plugin.carrotPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					if (plugin.carrotPistonDropCarrot) dropCarrot(block);
 					blockSchedulder(block, Material.CARROT.getId(), (byte) 0, plugin.carrotDelayPiston, true);
 				}
 			}
 			// MUST be a premature block, cancel it or not?
			else if (plugin.wheatPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.POTATO && plugin.potatoEnabled) {
 			// Mature
 			if (data == 7) {
 				// Should we cancel this?
 				if (plugin.potatoPreventPistonGrown) event.setCancelled(true);
 				else {
 					// Set to air and drop. Then wait the delay and make it a premature block again
 					if (plugin.potatoPistonDropPotato) dropPotato(block);
 					blockSchedulder(block, Material.POTATO.getId(), (byte) 0, plugin.potatoDelayPiston, true);
 				}
 			}
 			// MUST be a premature block, cancel it or not?
			else if (plugin.wheatPreventPiston) event.setCancelled(true);
 		}
 		else if (block.getType() == Material.SUGAR_CANE_BLOCK && plugin.sugarCaneEnabled) {
 			if (plugin.sugarCanePreventPiston) event.setCancelled(true);
 			else {
 				// Set to air and drop. Then wait the delay and make it a premature block again
 				if (plugin.sugarCanePistonDropSugarCane) dropSugarCane(block);
 				blockSchedulder(block, Material.SUGAR_CANE_BLOCK.getId(), (byte) 0, plugin.sugarCaneDelayPiston, true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		final Block block = event.getBlock();
 		Player player = event.getPlayer();
 		byte data = block.getData();
 		// Is the world on the list?
 		if (!plugin.enabledWorlds.contains(block.getWorld().getName())) return;
 		// If that block is a crop (Block ID #59)...
 		if (block.getType() == Material.CROPS && plugin.wheatEnabled) {
 			// If the data for the crop isn't 7 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if (data != 7) {
 				if (!player.hasPermission("SuperWheat.wheat.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					if (plugin.messageEnabled) player.sendMessage(plugin.message);
 				}
 			}
 			// Else, if the data for the block IS 7 (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if (data == 7 && player.hasPermission("SuperWheat.wheat.regrowing")) {
 				if (!player.hasPermission("SuperWheat.wheat.noseeds")) {
 					if (player.getInventory().contains(Material.SEEDS)) {
 						removeInventoryItems(player.getInventory(), Material.SEEDS, false, 1);
 					}
 					else return;
 				}
 				event.setCancelled(true);
 				// Set the block to a data value of 0, which is what the crop looks
 				// like right when you just plant it. With a light delay...
 				blockSchedulder(block, Material.CROPS.getId(), (byte) 0, plugin.wheatDelayHit, true);
 				// Drop wheat from the crop. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropWheat(block);
 				// Drop seeds, too (0-3)
 				if (player.hasPermission("SuperWheat.wheat.seed")) {
 					if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropSeeds(block);
 				}
 			}
 		}
 		else if (block.getType() == Material.NETHER_WARTS && plugin.netherWartEnabled) {
 			// If the data for the nether wart isn't 3 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if (data != 3) {
 				if (!player.hasPermission("SuperWheat.netherwart.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					if (plugin.messageEnabled) player.sendMessage(plugin.message);
 				}
 			}
 			// Else, if the data for the block IS 3 (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if (data == 3 && player.hasPermission("SuperWheat.netherwart.regrowing")) {
 				if (!player.hasPermission("SuperWheat.wheat.noseeds")) {
 					if (player.getInventory().contains(Material.NETHER_STALK)) {
 						removeInventoryItems(player.getInventory(), Material.NETHER_STALK, false, 1);
 					}
 					else return;
 				}
 				event.setCancelled(true);
 				// Set the block to a data value of 0, which is what the wart looks
 				// like right when you just plant it. With a light delay...
 				blockSchedulder(block, Material.NETHER_WARTS.getId(), (byte) 0, plugin.netherWartDelayHit, false);
 				// Drop some nether warts. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropNetherWart(block);
 			}
 		}
 		else if (block.getType() == Material.COCOA && plugin.cocoaPlantEnabled) {
 			// If the data for the cocoa plant isn't at least 8 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if (data < 8) {
 				if (!player.hasPermission("SuperWheat.cocoaplant.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					if (plugin.messageEnabled) player.sendMessage(plugin.message);
 				}
 			}
 			// Else, if the data for the block IS 8 or higher (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if (data >= 8 && player.hasPermission("SuperWheat.cocoaplant.regrowing")) {
 				if (!player.hasPermission("SuperWheat.cocoaplant.noseeds")) {
 					ItemStack i = new ItemStack(Material.INK_SACK);
 					i.setDurability((short) 3);
 					if (containsWithDurability(player.getInventory(), Material.INK_SACK, (short) 3)) {
 						removeInventoryItems(player.getInventory(), Material.INK_SACK, true, 1);
 					}
 					else return;
 				}
 				event.setCancelled(true);
 				// Minues 8 to keep the relative
 				final byte dataNew = (byte) (block.getData() - 8);
 				// Set the block to a data value of 0, which is what the plant looks
 				// like right when you just place it. With a light delay...
 				blockSchedulder(block, Material.COCOA.getId(), dataNew, plugin.cocoaPlantDelayHit, false);
 				// Drop some cocoa. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropCocoaBeans(block);
 			}
 		}
 		else if (block.getType() == Material.CARROT && plugin.carrotEnabled) {
 			// If the data for the carrot isn't 7 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if (data != 7) {
 				if (!player.hasPermission("SuperWheat.carrot.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					if (plugin.messageEnabled) player.sendMessage(plugin.message);
 				}
 			}
 			// Else, if the data for the block IS 7 (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if (data == 7 && player.hasPermission("SuperWheat.carrot.regrowing")) {
 				if (!player.hasPermission("SuperWheat.carrot.noseeds")) {
 					if (player.getInventory().contains(Material.CARROT_ITEM)) {
 						removeInventoryItems(player.getInventory(), Material.CARROT_ITEM, false, 1);
 					}
 					else return;
 				}
 				event.setCancelled(true);
 				// Set the block to a data value of 0, which is what the crop looks
 				// like right when you just plant it. With a light delay...
 				blockSchedulder(block, Material.CARROT.getId(), (byte) 0, plugin.carrotDelayHit, true);
 				// Drop some carrots. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropCarrot(block);
 			}
 		}
 		else if (block.getType() == Material.POTATO && plugin.potatoEnabled) {
 			// If the data for the potato isn't 7 (Isn't fully grown)
 			// And the player doesn't have the bypass permission...
 			// Also check if creative guys should be able to destroy
 			if (data != 7) {
 				if (!player.hasPermission("SuperWheat.potato.destroying") || plugin.blockCreativeDestroying) {
 					event.setCancelled(true);
 					if (plugin.messageEnabled) player.sendMessage(plugin.message);
 				}
 			}
 			// Else, if the data for the block IS 7 (The crop is fully grown) and the player
 			// has the permission node so the crop automatically re-grows after being harvested...
 			else if (data == 7 && player.hasPermission("SuperWheat.potato.regrowing")) {
 				if (!player.hasPermission("SuperWheat.potato.noseeds")) {
 					if (player.getInventory().contains(Material.POTATO_ITEM)) {
 						removeInventoryItems(player.getInventory(), Material.POTATO_ITEM, false, 1);
 					}
 					else return;
 				}
 				event.setCancelled(true);
 				// Set the block to a data value of 0, which is what the crop looks
 				// like right when you just plant it. With a light delay...
 				blockSchedulder(block, Material.POTATO.getId(), (byte) 0, plugin.potatoDelayHit, true);
 				// Drop some potatoes. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropPotato(block);
 			}
 		}
 		else if (block.getType() == Material.SUGAR_CANE_BLOCK && plugin.sugarCaneEnabled) {
 			// If the player has the permission node so the crop automatically re-grows after being harvested...
 			if (player.hasPermission("SuperWheat.sugarcane.regrowing")) {
 				if (!player.hasPermission("SuperWheat.sugarcane.noseeds")) {
 					if (player.getInventory().contains(Material.SUGAR_CANE)) {
 						removeInventoryItems(player.getInventory(), Material.SUGAR_CANE, false, 1);
 					}
 					else return;
 				}
 				event.setCancelled(true);
 				// Set the block to a data value of 0, which is what the crop looks
 				// like right when you just plant it. With a light delay...
 				blockSchedulder(block, Material.SUGAR_CANE_BLOCK.getId(), (byte) 0, plugin.sugarCaneDelayHit, false);
 				// Drop some potatoes. The amount of wheat is determined from the random number.
 				// Check for creative guys!
 				if (plugin.dropsCreative || player.getGameMode() != GameMode.CREATIVE) dropSugarCane(block);
 			}
 		}
 	}
 
 	// Drops wheat
 	private void dropWheat(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT, random.nextInt(3) + 1));
 	}
 
 	// Drops seeds
 	private void dropSeeds(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SEEDS, random.nextInt(4)));
 	}
 
 	// Drops netherWart
 	private void dropNetherWart(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHER_STALK, random.nextInt(4) + 2));
 	}
 
 	// Drops cocoa beans
 	private void dropCocoaBeans(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.INK_SACK, 3, (short) 3));
 	}
 	
 	// Drops carrots
 	private void dropCarrot(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.CARROT_ITEM, random.nextInt(4) + 1));
 	}
 	
 	// Drops potatoes
 	private void dropPotato(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POTATO_ITEM, random.nextInt(4) + 1));
 	}
 	
 	// Drops sugar canes
 	private void dropSugarCane(Block block) {
 		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SUGAR_CANE, 1));
 	}
 
 	// Removes item
 	private void removeInventoryItems(Inventory inv, Material type, boolean cocoa, int amount) {
 		for (ItemStack is : inv.getContents()) {
 			if (is != null && is.getType() == type) {
 				if (!cocoa || (cocoa && is.getDurability() == (short) 3)) {
 					int newamount = is.getAmount() - amount;
 					if (newamount > 0) {
 						is.setAmount(newamount);
 						break;
 					} else {
 						inv.remove(is);
 						amount -= newamount;
 						if (amount == 0) break;
 					}
 				}
 			}
 		}
 	}
 
 	// Checks with the durability, too
 	private boolean containsWithDurability(Inventory inventory, Material inkSack, short s) {
 		for (ItemStack is : inventory) {
 			if (is != null && is.getType() == inkSack && is.getDurability() == s) return true;
 		}
 		return false;
 	}
 	
 	private void blockSchedulder(final Block block, final int newID, final byte data, int delay, final boolean farmland) {
 		// Set the block to a data value of 0, which is what the crop looks
 		// like right when you just plant it. With a light delay...
 		block.setTypeId(0);
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				// First -> Farmland (soil)
 				if (farmland) {
 					// Use old farmland value of hydration
 					byte dataFarmaland = block.getRelative(BlockFace.DOWN).getData();
 					block.getRelative(BlockFace.DOWN).setType(Material.SOIL);
 					block.getRelative(BlockFace.DOWN).setData(dataFarmaland);
 				}
 				block.setTypeIdAndData(newID, data, true);
 			}
 		}, (20L * delay));
 	}
 }
