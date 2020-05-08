 package com.wolvencraft.prison.region;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.util.Vector;
 
 import com.wolvencraft.prison.util.Message;
 
 @SerializableAs("PrisonRegion")
 public class PrisonRegion implements ConfigurationSerializable {
 	private Location minimum, maximum;
 	
 	public PrisonRegion(Location pos1, Location pos2) {
 		minimum = pos1.clone();
 		maximum = pos2.clone();
 		
 		sortCoordinates();
 	}
 	
 	public PrisonRegion(PrisonSelection selection) {
 		minimum = selection.getPos1();
 		maximum = selection.getPos2();
 		
 		sortCoordinates();
 	}
 	
 	public PrisonRegion(Map<String, Object> map) {
 		World world = Bukkit.getServer().getWorld((String) map.get("world"));
 		minimum = ((Vector) map.get("minimum")).toLocation(world);
 		maximum = ((Vector) map.get("maximum")).toLocation(world);
 	}
 	
 	public Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("world", minimum.getWorld().getName());
 		map.put("minimum", minimum.toVector());
 		map.put("maximum", maximum.toVector());
 		return map;
 	}
 	
 	private void sortCoordinates() {
 		Location pos1 = minimum.clone();
 		Location pos2 = maximum.clone();
 		
 		if(pos1.getBlockX() > pos2.getBlockX()) {
 			minimum.setX(pos2.getBlockX());
 			maximum.setX(pos1.getBlockX());
 		}
 		
 		if(pos1.getBlockY() > pos2.getBlockY()) {
 			minimum.setY(pos2.getBlockY());
 			maximum.setY(pos1.getBlockY());
 		}
 		
 		if(pos1.getBlockZ() > pos2.getBlockZ()) {
 			minimum.setZ(pos2.getBlockZ());
 			maximum.setZ(pos1.getBlockZ());
 		}
 	}
 	
 	public Location getMinimum() { return minimum; }
 	public Location getMaximum() { return maximum; }
 	
 	public int getBlockCount() {
 		int xdist = Math.round(maximum.getBlockX() - minimum.getBlockX()) + 1;
 		int ydist = Math.round(maximum.getBlockY() - minimum.getBlockY()) + 1;
 		int zdist = Math.round(maximum.getBlockZ() - minimum.getBlockZ()) + 1;
 		int blockCount = xdist * ydist * zdist;
 		return blockCount;
 	}
 	
 	public int getBlockCountSolid() {
 		World world = minimum.getWorld();
 		int count = 0;
 		for(int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++) {
 			for(int y = minimum.getBlockY(); y <= maximum.getBlockY(); y++) {
 				for(int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
					try { if(!world.getBlockAt(x, y, z).isEmpty()) { count++; } }
					catch(Exception e) { continue; }
					catch(Error er) { continue; }
 				}
 			}
 		}
 		return count;	
 	}
 	
 	public void setCoordinates(Location pos1, Location pos2) {
 		minimum = pos1.clone();
 		maximum = pos2.clone();
 		
 		sortCoordinates();
 	}
 	
 	public void setCoordinates(PrisonSelection sel) {
 		minimum = sel.getPos1().clone();
 		maximum = sel.getPos2().clone();
 		
 		sortCoordinates();
 	}
 	
 	public PrisonRegion clone() {
 		return new PrisonRegion(this.getMinimum(), this.getMaximum());
 	}
 	
 	public boolean isLocationInRegion(Location loc) {
 		Message.debug(minimum.getBlockX() + " <= " + loc.getBlockX() + " <= " + maximum.getBlockX() + " :: " + (loc.getBlockX() >= minimum.getBlockX() && loc.getBlockX() <= maximum.getBlockX()));
 		Message.debug(minimum.getBlockY() + " <= " + loc.getBlockY() + " <= " + maximum.getBlockY() + " :: " + (loc.getBlockY() >= minimum.getBlockY() && loc.getBlockY() <= maximum.getBlockY()));
 		Message.debug(minimum.getBlockZ() + " <= " + loc.getBlockZ() + " <= " + maximum.getBlockZ() + " :: " + (loc.getBlockZ() >= minimum.getBlockZ() && loc.getBlockZ() <= maximum.getBlockZ()));
 		return (loc.getWorld().equals(minimum.getWorld())
         		&& (loc.getBlockX() >= minimum.getBlockX() && loc.getBlockX() <= maximum.getBlockX())
                 && (loc.getBlockY() >= minimum.getBlockY() && loc.getBlockY() <= maximum.getBlockY())
                 && (loc.getBlockZ() >= minimum.getBlockZ() && loc.getBlockZ() <= maximum.getBlockZ()));
 	}
 }
