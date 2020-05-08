 package com.jmeyer.bukkit.jarena.util;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 /**
  * Generates locations given parameters
  * @author JMEYER
  */
 public class LocationFactory {
 	
 	/**
 	 * Get random location between two given locations
 	 * @param world
 	 * @param loc1
 	 * @param loc2
 	 * @return
 	 */
 	public static Location randomLocation(World world, Location loc1, Location loc2) {
 		double x1 = loc1.getX();
 		double x2 = loc2.getX();
 		double y1 = loc1.getY();
 		double y2 = loc2.getY();
 		double z1 = loc1.getZ();
 		double z2 = loc2.getZ();
 		
 		return randomLocation(world, x1, x2, y1, y2, z1, z2);
 	}
 	
 	/**
 	 * Get random location within two given coordinates
 	 * @param world
 	 * @param x1
 	 * @param x2
 	 * @param y1
 	 * @param y2
 	 * @param z1
 	 * @param z2
 	 * @return
 	 */
 	public static Location randomLocation(World world, double x1, double x2, double y1, 
 				double y2, double z1, double z2) {
 		double x, y, z;
 		
 		if (x1 > x2) {
 			double temp = x1;
 			x1 = x2;
 			x2 = temp;				
 		}
 		
 		if (y1 > y2) {
 			double temp = y1;
 			y1 = y2;
 			y2 = temp;				
 		}
 		
 		if (z1 > z2) {
 			double temp = z1;
 			z1 = z2;
 			z2 = temp;				
 		}
 		
		x = (Math.random()*(x2-x1)) + x1;
		y = (Math.random()*(y2-y1)) + y1;
		z = (Math.random()*(z2-z1)) + z1;
 		
 		return new Location(world, x, y, z);
 	}
 	
 }
