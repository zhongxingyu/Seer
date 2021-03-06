 /*
  * This file is part of Lift.
  *
  * Copyright (c) ${project.inceptionYear}-2013, croxis <https://github.com/croxis/>
  *
  * Lift is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Lift is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Lift. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.croxis.plugins.lift;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.block.Sign;
 
 
 public class BukkitLiftRedstoneListener implements Listener {
 	private final BukkitLift plugin;
 	BukkitElevator bukkitElevator = null;
 	
 	// Supporting annoying out of date servers
 	private boolean canDo = false;
 	private Block block = null;
 	
 	public BukkitLiftRedstoneListener(BukkitLift plugin){
 		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
 		this.plugin = plugin;
 	} 
 	
 	@EventHandler
 	public void onBlockRedstoneChange(BlockRedstoneEvent event){
 		block = event.getBlock();
 		canDo = false;
 		canDo = (event.getBlock().getType() == Material.STONE_BUTTON || event.getBlock().getType() == Material.WOOD_BUTTON) 
 				//&& (!event.getBlock().isBlockIndirectlyPowered())
 				&& event.getBlock().getRelative(BlockFace.UP).getType() == Material.WALL_SIGN;
 		
 		if (BukkitLift.redstone){
 			plugin.logDebug("Redstone scan of " + event.getBlock().toString());
 			Block[] blocks = new Block[4];
 			blocks[0] = event.getBlock().getRelative(BlockFace.EAST);
 			blocks[1] = event.getBlock().getRelative(BlockFace.WEST);
 			blocks[2] = event.getBlock().getRelative(BlockFace.NORTH);
 			blocks[3] = event.getBlock().getRelative(BlockFace.SOUTH);
 			
 			for (Block b : blocks){
 				Block[] blocks2 = new Block[4];
 				blocks2[0] = b.getRelative(BlockFace.EAST);
 				blocks2[1] = b.getRelative(BlockFace.WEST);
 				blocks2[2] = b.getRelative(BlockFace.NORTH);
 				blocks2[3] = b.getRelative(BlockFace.SOUTH);
 				for (Block b2 : blocks2){
 					//plugin.logDebug("Block Type " + b.toString());
 					if ((b2.getType() == Material.STONE_BUTTON || b2.getType() == Material.WOOD_BUTTON)
 							&& b2.getRelative(BlockFace.UP).getType() == Material.WALL_SIGN){
 						canDo = true;
 						block = b2;
 						break;
 					}
 				}
 			}
 			plugin.logDebug("Redstone scan no match");
 		}
 			
 		if (canDo){
 			long startTime = System.currentTimeMillis();
 			
 			bukkitElevator = BukkitElevatorManager.createLift(block);
 			if (bukkitElevator == null)
 				return;
 			
 			String line = ((Sign) block.getRelative(BlockFace.UP).getState()).getLine(2);
 			if (line.isEmpty())
 				return;
			String[] splits = line.split(": ");
 			if (splits.length != 2)
 				return;
			int destination = Integer.parseInt(splits[1]);	
 			
 			//See if lift is in use
 			for (BukkitElevator e : BukkitElevatorManager.bukkitElevators){
 				Iterator<Block> iterator = bukkitElevator.baseBlocks.iterator();
 				while (iterator.hasNext()){
 					if (e.baseBlocks.contains(iterator.next()))
 						return;
 				}
 			}
 			
 			if (bukkitElevator.getTotalFloors() < 2)
 				return;
 			
 			int y = block.getY();
 			Floor startFloor = bukkitElevator.floormap.get(y);
 			bukkitElevator.startFloor = startFloor;
 			//Get all players in elevator shaft (at floor of button pusher if possible)
 			//And set their gravity to 0
 			bukkitElevator.destFloor = bukkitElevator.getFloorFromN(destination);			
 			
 			if (startFloor == null || bukkitElevator.destFloor == null){
 				plugin.logInfo("Critical Error. Startfloor is null. Please set debug to true in config and report bug.");
 				plugin.logInfo("Floormap: " + bukkitElevator.floormap.toString());
 				plugin.logInfo("Floormap2: " + bukkitElevator.floormap2.toString());
 				plugin.logInfo("Floormap3: " + bukkitElevator.destFloor.toString());
 			}
 			
 			if (bukkitElevator.destFloor.getY() > startFloor.getY()){
 				bukkitElevator.goingUp = true;
 			}
 			
 			if (BukkitLift.debug){
 				System.out.println("Elevator start floor:" + startFloor.getFloor());
 				System.out.println("Elevator start floor y:" + startFloor.getY());
 				System.out.println("Elevator destination floor:" + destination);
 				System.out.println("Elevator destination y:" + bukkitElevator.destFloor.getY());
 			}
 			
 			Iterator<Block> baseBlocksIterator = bukkitElevator.baseBlocks.iterator();
 			for(Chunk chunk : bukkitElevator.chunks){
 				plugin.logDebug("Number of entities in this chunk: " + Integer.toString(chunk.getEntities().length));
 				for(Entity entity : chunk.getEntities()){
 					if (bukkitElevator.isInShaftAtFloor(entity, startFloor)){
 						if (BukkitElevatorManager.isPassenger(entity)){
 							if (entity instanceof Player)
 								((Player) entity).sendMessage("You are already in a lift. Relog in case this is an error.");
 							continue;
 						}
 						BukkitElevatorManager.addPassenger(bukkitElevator, entity);
 						if (baseBlocksIterator.hasNext() && plugin.autoPlace){
 							Location loc = baseBlocksIterator.next().getLocation();
 							entity.teleport(new Location(entity.getWorld(), loc.getX() + 0.5D, entity.getLocation().getY(), loc.getZ() + 0.5D, entity.getLocation().getYaw(), entity.getLocation().getPitch()));
 						}
 						if (entity instanceof Player){
 							Player player = (Player) entity;
 							plugin.logDebug("Flyers: " + BukkitElevatorManager.flyers.toString());
 							if (!player.hasPermission("lift")){
 								BukkitElevatorManager.addHolder(bukkitElevator, entity, entity.getLocation());
 							}
 						}
 					} else if (!bukkitElevator.isInShaftAtFloor(entity, startFloor) && bukkitElevator.isInShaft(entity)){
 						BukkitElevatorManager.addHolder(bukkitElevator, entity, entity.getLocation());
 					}
 				}
 			}
 			
 			//Disable all glass inbetween players and destination
 			ArrayList<Floor> glassfloors = new ArrayList<Floor>();
 			//Going up
 			if (bukkitElevator.goingUp){
 				for(int i = startFloor.getFloor() + 1; i<= bukkitElevator.destFloor.getFloor(); i++){
 					glassfloors.add(bukkitElevator.floormap2.get(i));
 				}
 			}
 			//Going down
 			else {
 				for(int i = bukkitElevator.destFloor.getFloor() + 1; i<= startFloor.getFloor(); i++){
 					glassfloors.add(bukkitElevator.floormap2.get(i));
 				}
 			}
 			for (Floor f : glassfloors){
 				for (Block b : bukkitElevator.baseBlocks){
 					Block gb = event.getBlock().getWorld().getBlockAt(b.getX(), f.getY()-2, b.getZ());
 					gb.setType(Material.AIR);
 					bukkitElevator.glassBlocks.add(gb);
 				}
 			}
 			
 			BukkitElevatorManager.bukkitElevators.add(bukkitElevator);
 
 			if (BukkitLift.debug){
 				System.out.println("Going Up: " + Boolean.toString(bukkitElevator.goingUp));
 				System.out.println("Number of passengers: " + Integer.toString(bukkitElevator.getSize()));
 				System.out.println("Elevator chunks: " + Integer.toString(bukkitElevator.chunks.size()));
 				System.out.println("Total generation time: " + Long.toString(System.currentTimeMillis() - startTime));
 			}
 		}
 		
 	}
 	
 }
