 /*  BeefyBlocks - A Bukkit plugin to beef-up blocks, making them last longer
  *  Copyright (C) 2011 Letat
  *  Copyright (C) 2011 Robert Sargant
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */    
 
 package org.chryson.bukkit.beefyblocks;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.ContainerBlock;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class BeefyBlocksBlockListener extends BlockListener {
 	private class BlockLife {
 		public byte startingLives;
 		public byte remainingLives;
 		
 		public BlockLife(byte lives) {
 			startingLives = lives;
 			remainingLives = lives;
 		}
 		
 		public boolean isGone() {
 			return (remainingLives <= 0);
 		}
 		
 		public String toString() {
 			return String.format("%d/%d destroyed", startingLives-remainingLives, startingLives);
 		}
 	}
 	
     private BeefyBlocks parent;
 
     public BeefyBlocksBlockListener(BeefyBlocks instance) {
         parent = instance;
     }
 
     private byte checkTopAttached(Block block) {
     	Material mat = block.getFace(BlockFace.UP, 1).getType();
     	switch (mat) {
     	case SAPLING:
     	case POWERED_RAIL:
     	case DETECTOR_RAIL:
     	case LONG_GRASS:
     	case DEAD_BUSH:
     	case YELLOW_FLOWER:
     	case RED_ROSE:
     	case BROWN_MUSHROOM:
     	case RED_MUSHROOM:
     	case TORCH:
     	case REDSTONE_WIRE:
     	case CROPS:
     	case SIGN_POST:
     	case RAILS:
     	case LEVER:
     	case STONE_PLATE:
     	case WOOD_PLATE:
     	case REDSTONE_TORCH_OFF:
     	case REDSTONE_TORCH_ON:
     	case SNOW:
     	case CAKE_BLOCK:
     	case DIODE_BLOCK_OFF:
     	case DIODE_BLOCK_ON:
     		return 1;
     	case WOODEN_DOOR:
     	case IRON_DOOR_BLOCK:
     	case CACTUS:
     	case SUGAR_CANE_BLOCK:
     	case PORTAL:
     		byte num = 1;
     		while (block.getFace(BlockFace.UP, 1+num).getType() == mat)
     			num++;
     		return num;
     	default: break;
     	}
     	return 0;
     }
     
     private byte checkSideAttached(Block block, BlockFace dir) {
     	Material mat = block.getFace(dir, 1).getType();
     	switch (mat) {
     	case TORCH:
     	case LADDER:
     	case WALL_SIGN:
     	case LEVER:
     	case REDSTONE_TORCH_OFF:
     	case REDSTONE_TORCH_ON:
     	case STONE_BUTTON:
     	case PORTAL:
     	case TRAP_DOOR:
     		return 1;
     	default: break;
     	}
     	return 0;
     }
     
     public void respawnBlock(Block block) {
         Material oldType = block.getType();
         int oldId = block.getTypeId();
         byte oldData = block.getData();
         String[] oldLines = null;
 		if (oldId == 63)
 			oldLines = ((Sign)block.getState()).getLines();
         block.setType(Material.AIR);
         block.setType(oldType);
         block.setData(oldData);
         if (oldLines != null)
         	for (byte i = 0; i < oldLines.length; i++)
         		((Sign)block.getState()).setLine(i, oldLines[i]);
         if (isContainer(block))
         	restoreInventory(block);
     }
     
     public boolean isPlaced(Block block) {
         return (parent.getPlacedBlockAt(block.getLocation(), true) != null);
     }
     
     public boolean isAttached(Block block) {
     	return (parent.attachedBlocks.containsKey(block.getLocation().toString()));
     }
     
     public void trackLife(Block block) {
     	int lives = 1;
     	if (isPlaced(block))
     		lives = parent.placedLives[block.getTypeId()];
     	else
     		lives = parent.baseLives[block.getTypeId()];
     	parent.blockLives.put(block.getLocation().toString(), new BlockLife((byte) lives));
     }
     
     public boolean isLifeTracked(Block block) {
     	return parent.blockLives.containsKey(block.getLocation().toString());
     }
     
     public BlockLife loseLife(Block block) {
     	String locStr = block.getLocation().toString();
     	BlockLife life = (BlockLife)parent.blockLives.get(locStr);
     	life.remainingLives--;
         parent.blockLives.put(locStr, life);
     	return life;
     }
     
     public boolean isTool(ItemStack item) {
     	switch (item.getType()) {
     	case IRON_SPADE:
     	case IRON_PICKAXE:
     	case IRON_AXE:
     	case IRON_SWORD:
     	case WOOD_SWORD:
     	case WOOD_SPADE:
     	case WOOD_PICKAXE:
     	case WOOD_AXE:
     	case STONE_SWORD:
     	case STONE_SPADE:
     	case STONE_PICKAXE:
     	case STONE_AXE:
     	case DIAMOND_SWORD:
     	case DIAMOND_SPADE:
     	case DIAMOND_PICKAXE:
     	case DIAMOND_AXE:
     	case GOLD_SWORD:
     	case GOLD_SPADE:
     	case GOLD_PICKAXE:
     	case GOLD_AXE:
     	case WOOD_HOE:
     	case STONE_HOE:
     	case IRON_HOE:
     	case DIAMOND_HOE:
     	case GOLD_HOE:
     		return true;
     	}
     	return false;
     }
     
     public void damageTool(Player p, ItemStack item) {
     	if (isTool(item)) {
 	    	if (item.getDurability() >= item.getType().getMaxDurability())
 	    		if (item.getAmount() > 1)
 	    			item.setAmount(item.getAmount()-1);
 	    		else
 	    			p.setItemInHand(null);
 	    	else
 	    		item.setDurability((short)(item.getDurability()+1));
     	}
     }
     
     public void untrackLife(Block block) {
         parent.blockLives.remove(block.getLocation().toString());
     }
     
     public void removeBlock(Block block) {
     	if (isPlaced(block)) {
     		PlacedBlock pBlock = parent.getPlacedBlockAt(block.getLocation(), false);
     		if (pBlock != null)
     			parent.getDatabase().delete(pBlock);
     	}
     }
     
     public void untrackAttachedLives(Block block) {
         parent.attachedBlocks.remove(block.getLocation().toString());
     }
     
     public void trackAttachedLives(Block block) {
     	// We need to find all attached blocks and store them in a map along with
     	// the main block's material type so that when they break in the physics 
     	// event following the main block's break event, they can decide whether 
     	// or not to cancel the secondary "breakage" event and remove themselves from the map
     	int id = block.getTypeId();
     	
         byte[] blocks = new byte[5];
     	blocks[0] = checkTopAttached(block);
         blocks[1] = checkSideAttached(block, BlockFace.EAST);
         blocks[2] = checkSideAttached(block, BlockFace.WEST);
         blocks[3] = checkSideAttached(block, BlockFace.NORTH);
         blocks[4] = checkSideAttached(block, BlockFace.SOUTH);
         
         for (byte i = 0; i < blocks[0]; i++)
         	parent.attachedBlocks.put(block.getFace(BlockFace.UP, i+1)
         							  .getLocation().toString(), id);
         for (byte i = 0; i < blocks[1]; i++)
         	parent.attachedBlocks.put(block.getFace(BlockFace.EAST, i+1)
 					  				  .getLocation().toString(), id);
         for (byte i = 0; i < blocks[2]; i++)
         	parent.attachedBlocks.put(block.getFace(BlockFace.WEST, i+1)
 					  				  .getLocation().toString(), id);
         for (byte i = 0; i < blocks[3]; i++)
         	parent.attachedBlocks.put(block.getFace(BlockFace.NORTH, i+1)
 					  				  .getLocation().toString(), id);
         for (byte i = 0; i < blocks[4]; i++)
         	parent.attachedBlocks.put(block.getFace(BlockFace.SOUTH, i+1)
 					  				  .getLocation().toString(), id);
     }
     
     public void sendProgressMessage(Player p, BlockLife life) {
     	if (parent.getDisplayPref(p) == DisplayPreference.ALL)
     		p.sendMessage(life.toString());
     }
     
     public void resizeMaps() {
         // To avoid consuming too much memory, reset these
         if (parent.blockLives.size() > 1000000) {
         	parent.blockLives.clear();
         }
     }
     
     public boolean isCoolingDown(Block block) {
     	PlacedBlock pBlock = parent.getPlacedBlockAt(block.getLocation(), true);
     	if (pBlock == null)
     		return false;
     	Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
     	long periodEnd =  pBlock.getTimestamp().getTime() + (parent.cooldown*60000);
     	return now.before(new Timestamp(periodEnd));
     }
     
     public boolean isContainer(Block block) {
     	return (block.getType() == Material.CHEST ||
     			block.getType() == Material.DISPENSER ||
     			block.getType() == Material.FURNACE);
     }
     
     public void removeInventory(Block block) {
 		Inventory inventory = null;
 		if (block.getType() == Material.CHEST)
 			inventory = ((Chest)block.getState()).getInventory();
 		else if (block.getType() == Material.DISPENSER)
 			inventory = ((Dispenser)block.getState()).getInventory();
 		else if (block.getType() == Material.FURNACE)
 			inventory = ((Furnace)block.getState()).getInventory();
 		ItemStackSerializable[] sInventory = ItemStackSerializable.toItemStackSerializableArr(inventory.getContents());
 		parent.inventories.put(block.getLocation().toString(), sInventory);
 		inventory.clear();
     }
     
     public void restoreInventory(Block block) {
 		ItemStack[] inventory = ItemStackSerializable.toItemStackArr(
 				((ItemStackSerializable[]) parent.inventories.get(block.getLocation().toString())));
 		if (block.getType() == Material.CHEST) {
 			((Chest)block.getState()).getInventory().setContents(inventory);
 		} else if (block.getType() == Material.DISPENSER) {
 			((Dispenser)block.getState()).getInventory().setContents(inventory);
 		} else if (block.getType() == Material.FURNACE) {
 			((Furnace)block.getState()).getInventory().setContents(inventory);
 		}
 		parent.inventories.remove(block.getLocation().toString());
     }
     
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled())
             return;
         
     	if (parent.getPlacedBlockAt(event.getBlock().getLocation(), true) == null) {
     		PlacedBlock pBlock = new PlacedBlock(event.getPlayer(), event.getBlock());
     		parent.getDatabase().save(pBlock);
     	} else
     		event.setCancelled(true);
     }
 
     @Override
     public void onBlockBurn(BlockBurnEvent event) {
         if (event.isCancelled())
             return;
         
         Block block = event.getBlock();
         if (!isLifeTracked(block))
         	trackLife(block);
         BlockLife life = loseLife(block);
         if (life.isGone()) {
         	untrackLife(block);
         	// remove a placed block from database (if it was one)
         	removeBlock(block);
         } else {
         	trackAttachedLives(block);
         	event.setCancelled(true);
         }
         resizeMaps();
     }
     
     @Override
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled())
             return;
         
         Player player = event.getPlayer();
         Block block = event.getBlock();
         if (BeefyBlocks.hasPermission(player, "beefyblocks.admin")) {
         	if (isLifeTracked(block))
         		untrackLife(block);
         	// remove a placed block from database (if it was one)
         	removeBlock(block);
         } else if (isCoolingDown(block)) {
         	if (isLifeTracked(block))
         		untrackLife(block);
         	// remove placed block from database
         	removeBlock(block);
         } else {
 	        if (!isLifeTracked(block))
 	        	trackLife(block);
 	        BlockLife life = loseLife(block);
 	        if (life.isGone()) {
 	        	untrackLife(block);
 	        	// remove a placed block from database (if it was one)
 	        	removeBlock(block);
 	        } else {
 	        	if (isContainer(block))
 	        		removeInventory(block);
 	        	trackAttachedLives(block);
 	        	event.setCancelled(true);
 		        damageTool(player, player.getItemInHand());
 		        // necessary to make it evident to the client instantly
 	        	respawnBlock(block);
 	        	sendProgressMessage(player, life);
 	        }
 	        // performance optimization
 	        resizeMaps();
         }
     }
     
     // TODO: Fix bug where torch on top of a block hops onto side of an adjacent block that is one
     //       level higher
     @Override
     public void onBlockPhysics(BlockPhysicsEvent event) {
         if (event.isCancelled())
             return;
         
         Block block = event.getBlock();
         if (isAttached(block)) {
             int attachedToId = (Integer)parent.attachedBlocks.get(block.getLocation().toString());
             if (attachedToId == event.getChangedTypeId()) {
             	untrackAttachedLives(block);
             	event.setCancelled(true);
             }
         }
     }
 }
 
