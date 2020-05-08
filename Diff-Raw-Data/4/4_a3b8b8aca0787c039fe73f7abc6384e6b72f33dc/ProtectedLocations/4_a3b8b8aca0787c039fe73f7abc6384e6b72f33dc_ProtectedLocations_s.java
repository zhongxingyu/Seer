 /*******************************************************************************
  * Copyright (c) 2012 GaryMthrfkinOak (Jesse Caple).
  * 
  * This is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or any later version.
  * 
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.ignoreourgirth.gary.oakcorelib;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
 import com.google.common.collect.HashMultiset;
 
 public class ProtectedLocations implements Listener {
 
 	private static HashMultiset<Location> locations;
 	private static HashMultiset<Location> cancelExplosionSet;
 	
 	static {
		locations = HashMultiset.create();
		cancelExplosionSet = HashMultiset.create();
 	}
 	
 	protected ProtectedLocations() {}
 	
 	public static void add(Location location) {
 		locations.add(location);
 	}
 	
 	public static void add(Location location, boolean cancelExplosions) {
 		locations.add(location);
 		if (cancelExplosions) {
 			cancelExplosionSet.add(location);
 		}
 	}
 	
 	public static void remove(Location location) {
 		if (locations.contains(location)) {
 			locations.remove(location);
 		}
 		if (cancelExplosionSet.contains(location)) {
 			cancelExplosionSet.remove(location);
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (locations.contains(event.getBlock().getLocation())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (locations.contains(event.getBlock().getLocation())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPistonExtend(BlockPistonExtendEvent event) {
 		Iterator<Block> blocks = event.getBlocks().iterator();
 		while (blocks.hasNext()) {
 			if (locations.contains(blocks.next().getLocation())) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPistonRetract(BlockPistonRetractEvent event) {
 		if (locations.contains(event.getRetractLocation())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPistonChangeBlock(EntityChangeBlockEvent event) {
 		if (locations.contains(event.getBlock().getLocation())) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler (priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEntityExplode(EntityExplodeEvent event) {
 		ArrayList<Block> removalList = new ArrayList<Block>();
 		List<Block> blockList = event.blockList();
 		Iterator<Block> blocks = blockList.iterator();
 		while (blocks.hasNext()) {
 			Block nextBlock = blocks.next();
 			Location location = nextBlock.getLocation();
 			if (locations.contains(location)) {
 				if (cancelExplosionSet.contains(location)) {
 					event.setCancelled(true);
 				} else {
 					removalList.add(nextBlock);
 				}
 			}
 		}
 		Iterator<Block> removal = removalList.iterator();
 		while (removal.hasNext()) {
 			blockList.remove(removal.next());
 		}
 	}
 	
 }
