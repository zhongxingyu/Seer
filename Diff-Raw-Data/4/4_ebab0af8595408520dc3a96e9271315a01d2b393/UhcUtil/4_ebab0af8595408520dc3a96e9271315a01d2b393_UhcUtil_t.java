 package com.martinbrook.tesseractuhc;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.plugin.Plugin;
 
 import com.wimbli.WorldBorder.BorderData;
 import com.wimbli.WorldBorder.WorldBorder;
 
 public class UhcUtil {
 
 	private UhcUtil() { }
 	
 	public static String formatDuration(Calendar t1, Calendar t2, boolean precise) {
 		// Get duration in seconds
 		int d = (int) (t2.getTimeInMillis() - t1.getTimeInMillis()) / 1000;
 		
 		if (precise) {
 			int seconds = d % 60;
 			d = d / 60;
 			int minutes = d % 60;
 			int hours = d / 60;
 			
 			// The string
 			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
 		} else {
			int minutes = d / 60;
			return minutes + " minute" + (minutes != 1 ? "s" : "");
 			
 		}
 		
 	}
 
 	public static ArrayList<String> readFile(String filename) {
 		File fChat = getPluginDataFile(filename, true);
 		
 		if (fChat == null) return null;
 		
 		ArrayList<String> lines = new ArrayList<String>();
 		try {
 			FileReader fr = new FileReader(fChat);
 			BufferedReader in = new BufferedReader(fr);
 			String s = in.readLine();
 
 			while (s != null) {
 				lines.add(s);
 				s = in.readLine();
 			}
 
 			in.close();
 			fr.close();
 			return lines;
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Initialise the data folder for the UhcTools plugin.
 	 *
 	 * @return The data folder for the plugin, or null if it couldn't be created
 	 */
 	public static File getPluginFolder() {
 	    File pluginFolder = TesseractUHC.getInstance().getDataFolder();
 	    if (!pluginFolder.isDirectory()){
 	        if (!pluginFolder.mkdirs()) {
 	            // failed to create the non existent directory, so failed
 	            return null;
 	        }
 	    }
 	    return pluginFolder;
 	}
 	
 	/**
 	 * Get the folder for the current (main) world
 	 * 
 	 * @return The world folder
 	 */
 	public static File getWorldFolder() {
 		File worldFolder = TesseractUHC.getInstance().getMatch().getStartingWorld().getWorldFolder();
 		if (worldFolder.isDirectory()) return worldFolder;
 		else return null;
 	}
 	 
 	/**
 	 * Retrieve a File from the plugin data folder.
 	 *
 	 * @param filename Filename to retrieve
 	 * @param mustAlreadyExist true if the file should already exist
 	 * @return The specified data file, or null if not found
 	 */
 	public static File getPluginDataFile(String filename, boolean mustAlreadyExist) {
 		return getDataFile(getPluginFolder(), filename, mustAlreadyExist);
 	}
 	
 	/**
 	 * Retrieve a File from the world data folder.
 	 *
 	 * @param filename Filename to retrieve
 	 * @param mustAlreadyExist true if the file should already exist
 	 * @return The specified data file, or null if not found
 	 */
 	public static File getWorldDataFile(String filename, boolean mustAlreadyExist) {
 		return getDataFile(getWorldFolder(), filename, mustAlreadyExist);
 	}
 	
 	
 	/**
 	 * Retrieve a File from a specific folder.
 	 * 
 	 * @param folder The folder to look for the file in
 	 * @param filename Filename to retrieve
 	 * @param mustAlreadyExist true if the file should already exist
 	 * @return The specified data file, or null if not found
 	 */
 	public static File getDataFile(File folder, String filename, boolean mustAlreadyExist) {
 	    if (folder != null) {
 	        File file = new File(folder, filename);
 	        if (mustAlreadyExist) {
 	            if (file.exists()) {
 	                return file;
 	            }
 	        } else {
 	            return file;
 	        }
 	    }
 	    return null;
 	}
 	
 	/**
 	 * Calculates if it's possible for a player to fit in a certain spot.
 	 * 
 	 * @param feetLocation the location where the feet should be
 	 * @return wheter or not there's place for both his feet and head
 	 */
 	public static boolean isSpaceForPlayer(Location feetLocation) {
 		World w = feetLocation.getWorld();
 		int x = feetLocation.getBlockX(), y = feetLocation.getBlockY(), z = feetLocation.getBlockZ();
 		Block b1 = w.getBlockAt(x, y, z);
 		Block b2 = w.getBlockAt(x, y + 1, z);
 		return isSpaceForPlayer(b1) && isSpaceForPlayer(b2);
 	}
 	
 
 
 	/**
 	 * Calculates if it's possible for a player to fit in a certain spot.
 	 * 
 	 * @param w the world in which we need to check if there's space for the
 	 *            player
 	 * @param x the x coordinate of the block to check
 	 * @param y the y coordinate of the block (at the feet) to check
 	 * @param z the z coordinate of the block to check
 	 * @return whether or not there's place for both his feet and head
 	 */
 	public static boolean isSpaceForPlayer(World w, int x, int y, int z) {
 		Block b1 = w.getBlockAt(x, y, z);
 		Block b2 = w.getBlockAt(x, y + 1, z);
 		return isSpaceForPlayer(b1) && isSpaceForPlayer(b2);
 	}
 
 	/**
 	 * Determine whether a given block is a either empty or liquid (but not
 	 * lava)
 	 * 
 	 * @param b the block to check
 	 * @return whether the block is suitable
 	 */
 	public static boolean isSpaceForPlayer(Block b) {
 		return (b.isEmpty() || b.isLiquid()) && b.getType() != Material.LAVA && b.getType() != Material.STATIONARY_LAVA;
 	}
 	
     /**
      * Get a BorderData from the WorldBorder plugin
      * 
      * @param w The world to get borders for
      * @return The WorldBorder BorderData object
      */
     private static BorderData getWorldBorder(World w) {
         Plugin plugin = TesseractUHC.getInstance().getServer().getPluginManager().getPlugin("WorldBorder");
 
         // Check if the plugin is loaded
         if (plugin == null || !(plugin instanceof WorldBorder))
         	return null;
 
         WorldBorder wb = (WorldBorder) plugin;
         return wb.GetWorldBorder(w.getName());
     }
     
     /**
      * Change the WorldBorder radius for a world
      * 
      * @param w The world to be changed
      * @param radius The new radius
      * @return Whether the operation succeeded
      */
     public static boolean setWorldRadius(World w, int radius) {
     	BorderData border = getWorldBorder(w);
     	if (border != null) {
     		border.setRadius(radius);
     		return true;
     	}
     	return false;
     }
 
 	/**
 	 * Attempt to parse a calcstarts command and return a list of start points
 	 * 
 	 * @param args The arguments which were passed to the command
 	 * @return List of start locations, or null if failed
 	 */
 	public static ArrayList<Location> calculateStarts(String[] args) {
 		if (args.length < 1) return null;
 		String method = args[0];
 		if ("radial".equalsIgnoreCase(method)) {
 			if (args.length != 3) return null;
 			int count;
 			int radius;
 			try {
 				count = Integer.parseInt(args[1]);
 				radius = Integer.parseInt(args[2]);
 			} catch (NumberFormatException e) {
 				return null;
 			}
 			return calculateRadialStarts(count, radius);
 			
 		}
 		return null;
 		
 
 	}
 
 	/**
 	 * Generate a list of radial start points
 	 * 
 	 * @param count Number of starts to generate
 	 * @param radius Radius of circle
 	 * @return List of starts
 	 */
 	private static ArrayList<Location> calculateRadialStarts(int count, int radius) {
 		ArrayList<Location> locations = new ArrayList<Location>();
 		
 		double arc = (2*Math.PI) / count;
 		World w = TesseractUHC.getInstance().getMatch().getStartingWorld();
 		
 		for(int i = 0; i < count; i++) {
 			int x = (int) (radius * Math.cos(i*arc));
 			int z = (int) (radius * Math.sin(i*arc));
 			
 			int y = w.getHighestBlockYAt(x, z);
 			locations.add(new Location(w,x,y,z));
 		}
 		return locations;
 	
 	}
 	
 	/**
 	 * Convert a string to a boolean.
 	 * 
 	 * true, on, yes, y, 1 => True
 	 * false, off, no, n, 0 => False
 	 * 
 	 * @param s The string to check
 	 * @return Boolean value, or null if not parsable
 	 */
 	public static Boolean stringToBoolean(String s) {
 		if ("true".equalsIgnoreCase(s) || "on".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "y".equalsIgnoreCase(s) || "1".equals(s))
 			return true;
 		if ("false".equalsIgnoreCase(s) || "off".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "n".equalsIgnoreCase(s) || "0".equals(s))
 			return false;
 		return null;
 		
 	}
 	
 }
