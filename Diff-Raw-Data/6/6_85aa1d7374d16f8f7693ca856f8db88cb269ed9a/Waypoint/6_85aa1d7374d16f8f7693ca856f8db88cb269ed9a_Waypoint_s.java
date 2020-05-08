 /*
 	Copyright (C) 2011 by NetherLink
 	
 	Permission is hereby granted, free of charge, to any person obtaining a copy
 	of this software and associated documentation files (the "Software"), to deal
 	in the Software without restriction, including without limitation the rights
 	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 	copies of the Software, and to permit persons to whom the Software is
 	furnished to do so, subject to the following conditions:
 	
 	The above copyright notice and this permission notice shall be included in
 	all copies or substantial portions of the Software.
 	
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 	THE SOFTWARE.
 */
 
 package netherlink.minecraft.netherpoints;
 
 import org.bukkit.block.*;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 public class Waypoint{
 	
 	/*
 	 * Variables
 	 */
 	
 	private Block b; // The central block of the waypoint (the glass....)
 	private Location location; // The location of the central block.
 	private String name; // The name of the waypoint.
 	private String network; // The name of the network the waypoint is on.
 	private Material key; // The name of the key.
 	
 	/*
 	 * Constructor
 	 */
 	
 	public Waypoint(String namex, String networkx, Location locationx, Material keyx){
 		name = namex;
 		network = networkx;
 		b = locationx.getBlock();
 		location = locationx;
 		key = keyx;
 	}
 	
 	/*
 	 * Core Functions
 	 */
 	
 	public void printInfo(Player p){
 		p.sendMessage(ChatColor.GREEN + "Waypoint: " + network + ":" + name + " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ") (" + key.name() + ")");
 	}
 	
 	public void teleport(Player p){
 		// Check the obsidian pattern.
 		if(b.getRelative(-1, 0, 0).getType() != Material.OBSIDIAN || 
 			b.getRelative(0, 0, -1).getType() != Material.OBSIDIAN ||
 			b.getRelative(1, 0, 0).getType() != Material.OBSIDIAN ||
 			b.getRelative(0, 0, 1).getType() != Material.OBSIDIAN){
 			p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint malfunction. Check your obsidian pattern.");
 			return;
 		}
 		int y = 0;
 		while(b.getRelative(0, y++, 0).getTypeId() != 0); // Find nearest free block.
 		p.teleport(new Location(location.getWorld(), location.getX() + 0.5, location.getY() + y, location.getZ() + 0.5));
 	}
 	
 	public boolean verify(){
		if(b.getTypeId() != 20) // If the block at that location is not glass (20) the verification fails.
			return false;
		return true;
 	}
 	
 	/*
 	 * Getters
 	 */
 	
 	public Block getBlock(){return b;}
 	
 	public Material getKey(){return key;}
 	
 	public Location getLocation(){return location;}
 	
 	public String getName(){return name;}
 	
 	public String getNetwork(){return network;}
 	
 }
