 package com.webkonsept.bukkit.simplechestlock;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.Location;
import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 public class SimpleChestLockList {
 	SimpleChestLock plugin;
 	HashMap<Location,String> list = new HashMap<Location,String>();
 	
 	public SimpleChestLockList(SimpleChestLock instance) {
 		plugin = instance;
 	}
 	public void load (String filename) {
 		File chestFile = new File (plugin.getDataFolder().toString()+"/"+filename);
 		plugin.babble("Reading chests from "+plugin.getDataFolder().getName()+"/"+filename);
 		list.clear();
 		if (!chestFile.exists()){
 			plugin.getDataFolder().mkdir();
 			try {
 				plugin.babble("Attempting to create "+chestFile.getName());
 				chestFile.createNewFile();
 				plugin.babble("Attempting to create"+plugin.getDataFolder().getName()+"/"+filename);
 			} catch (IOException e) {
 				e.printStackTrace();
 				plugin.crap("FAILED TO CREATE CHESTFILE ("+filename+"): "+e.getMessage());
 			}
 		}
 		
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(chestFile));
 			String line = "";
 			while (line != null){
 				line = in.readLine();
 				plugin.babble(line);
 				if (line != null){
 					String[] elements = line.split(",", 5);
 					String playerName = elements[0];
 					World world = plugin.server.getWorld(elements[1]);
 					Double X = null;
 					Double Y = null;
 					Double Z = null;
 					
 					try {
 						X = Double.parseDouble(elements[2]);
 						Y = Double.parseDouble(elements[3]);
 						Z = Double.parseDouble(elements[4]);
 					}
 					catch(NumberFormatException e){
 						e.printStackTrace();
 						plugin.crap("I got an unparsable number from the chest file: "+e.getMessage());
 					}
 					
 					if (world != null && X != null && Y != null && Z != null){
 						Location location = new Location(world,X,Y,Z);
 						if ( ! list.containsKey(location)){
							if(location.getBlock().getType().equals(Material.CHEST)){
								plugin.babble("Added location to protection list:Player("+playerName+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")");
 								list.put(location, playerName);
 							}
 							else {
 								plugin.crap("Protected location has no chest!  Outside the scope of this plugin, so I'm --NOT-- protecting it!");
 							}
 						}
 					}
 					else {
 						plugin.crap("Error in chestfile:  Player("+playerName+") World("+world+") X("+X+") Y("+Y+") Z("+Z+")");
 					}
 				}
 				else {
 					plugin.babble("Done reading protected locations!");
 				}
 			}
 			in.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			plugin.crap("OMG JAVA!  The file I just created DOES NOT EXIST?!  Damn.");
 		} catch (IOException e) {
 			e.printStackTrace();
 			plugin.crap("Okay, crap, IOException while reading "+filename+": "+e.getMessage());
 		}
 	}
 	public void save(String filename) {
 		File chestFile = new File (plugin.getDataFolder().toString()+"/"+filename);
 		if (!chestFile.exists()){
 			plugin.out("Attempting to create "+chestFile.getName());
 			plugin.getDataFolder().mkdir();
 			plugin.out("Attempting to create "+plugin.getDataFolder().getName()+"/"+filename);
 			try {
 				chestFile.createNewFile();
 			} catch (IOException e) {
 				plugin.crap("FAILED TO CREATE CHESTFILE");
 				e.printStackTrace();
 			}
 		}
 		
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
 			Iterator<Location> iterator = list.keySet().iterator();
 			while (iterator.hasNext()){
 				Location location = iterator.next();
 				String playerName = list.get(location);
 				out.write(playerName+","+location.getWorld().getName()+","+location.getX()+","+location.getY()+","+location.getZ());
 				out.newLine();
 			}
 			out.flush();
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			plugin.crap("OMG JAVA!  The file I just created DOES NOT EXIST?!  Damn.");
 		} catch (IOException e) {
 			e.printStackTrace();
 			plugin.crap("Okay, crap, IOExeption while writing "+filename+": "+e.getMessage());
 		}
 	}
 	public String getOwner(Block block){
 		if (block == null) return null;
 		if (list.containsKey(block.getLocation())){
 			return list.get(block.getLocation());
 		}
 		else {
 			return null;
 		}
 	}
 	public boolean isLocked(Block block){
 		if (block == null) return false;
 		if (list == null) return false;
 		if (list.containsKey(block.getLocation())){
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	public Integer lock(Player player,Block block){
 		if (player == null || block == null || list == null) return 0;
 		if (plugin.canLock(block)){
 			list.put(block.getLocation(), player.getName());
 			Integer additionalLockedChests = 0;
 			if (plugin.lockpair && plugin.canDoubleLock(block)){
 				additionalLockedChests += this.addNeighboringChests(block,player.getName());
 			}
 			return 1 + additionalLockedChests;
 		}
 		else {
 			return 0;
 		}
 	}
 	public Integer unlock(Block block){
 		if (block == null) return 0;
 		if (this.isLocked(block)){
 			if (list != null){
 				list.remove(block.getLocation());
 				Integer additionalUnlockedChests = 0;
 				if (plugin.lockpair && plugin.canDoubleLock(block)){
 					additionalUnlockedChests += this.removeNeighboringChests(block);
 				}
 				return 1 + additionalUnlockedChests;
 			}
 			else {
 				return 0;
 			}
 		}
 		return 0;
 	}
 	private ArrayList<Block> getNeighbours (Block block) {
 		ArrayList<Block> neighbours = new ArrayList<Block>();
 		neighbours.add(block.getFace(BlockFace.NORTH));
 		neighbours.add(block.getFace(BlockFace.SOUTH));
 		neighbours.add(block.getFace(BlockFace.EAST));
 		neighbours.add(block.getFace(BlockFace.WEST));
 		
 		return neighbours;
 	}
 	private Integer removeNeighboringChests (Block block) {
 		ArrayList<Block> neighbours = this.getNeighbours(block);
 		Iterator<Block> iterateNeighbours = neighbours.iterator();
 		Integer additionalChestsUnlocked = 0;
 		while (iterateNeighbours.hasNext()){
 			Block currentNeighbour = iterateNeighbours.next();
 			if (currentNeighbour.getType().equals(block.getType())){
 				list.remove(currentNeighbour.getLocation());
 				additionalChestsUnlocked++;
 			}
 		}
 		return additionalChestsUnlocked;
 	}
 	private Integer addNeighboringChests (Block block,String playerName) {
 		ArrayList<Block> neighbours = this.getNeighbours(block);
 		Iterator<Block> iterateNeighbours = neighbours.iterator();
 		Integer additionalChestsLocked = 0;
 		while (iterateNeighbours.hasNext()){
 			Block currentNeighbour = iterateNeighbours.next();
 			if (currentNeighbour.getType().equals(block.getType())){
 				list.put(currentNeighbour.getLocation(), playerName);
 				additionalChestsLocked++;
 			}
 		}
 		return additionalChestsLocked;
 	}
 }
