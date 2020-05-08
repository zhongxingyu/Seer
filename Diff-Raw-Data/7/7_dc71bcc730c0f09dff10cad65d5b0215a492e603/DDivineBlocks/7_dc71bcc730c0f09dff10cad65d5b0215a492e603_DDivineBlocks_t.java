 package com.legit2.Demigods;
 
 import java.util.ArrayList;
 
 import org.bukkit.Location;
 
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DDataUtil;
 
 public class DDivineBlocks
 {	
 	/* ---------------------------------------------------
 	 * Begin Shrine-related Methods
 	 * ---------------------------------------------------
 	 * 
 	 *  createShrine() : Creates a shrine at (Location)location.
 	 */
 	public static void createShrine(int charID, Location location) throws Exception
 	{
 		ArrayList<Location> charShrines = getShrines(charID);
		if(charShrines != null) charShrines.add(location);
		else
		{
			charShrines = new ArrayList<Location>();
			charShrines.add(location);
		}
 		DDataUtil.saveCharData(charID, "char_shrines", charShrines);
 	}
 	
 	/*
 	 *  removeShrine() : Removes the shrine at (Location)location.
 	 */
 	public static void removeShrine(Location location) throws Exception
 	{
 		int charID = getOwnerOfShrine(location);
 		ArrayList<Location> charShrines = getShrines(charID);
 		charShrines.remove(location);
 		DDataUtil.saveCharData(charID, "char_shrines", charShrines);
 	}
 	
 	/*
 	 *  getShrines() : Returns an ArrayList<Location> of (int)charID's Shrines.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Location> getShrines(int charID) throws Exception
 	{		
 		if(DDataUtil.hasCharData(charID, "shrines")) return (ArrayList<Location>) DDataUtil.getCharData(charID, "shrines");
 		else return null;
 	}
 	
 	/*
 	 *  getShrines() : Returns an ArrayList<Location> of (Player)player's Shrines.
 	 */
 	public static ArrayList<Location> getAllShrines() throws Exception
 	{		
 		ArrayList<Location> shrines = new ArrayList<Location>();
 		
 		for(int charID : DDataUtil.getAllChars().keySet())
 		{
 			for(Location shrine : getShrines(charID))
 			{
 				if(!shrines.contains(shrine)) shrines.add(shrine);
 			}
 		}
 		
 		return shrines;
 	}
 	
 	/*
 	 *  getOwnerOfShrine() : Returns the owner of the shrine at (Location)location.
 	 */
 	public static int getOwnerOfShrine(Location location) throws Exception
 	{
 		for(int charID : DDataUtil.getAllChars().keySet())
 		{
 			for(Location knownLoc : getShrines(charID))
 			{
 				if(knownLoc.equals(location)) return charID;
 			}
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getDeityAtShrine() : Returns the deity of the shrine at (Location)location.
 	 */
 	public static String getDeityAtShrine(Location location) throws Exception
 	{
 		int charID = getOwnerOfShrine(location);
 		return DCharUtil.getDeity(charID);
 	}
 	
 	/* ---------------------------------------------------
 	 * Begin Altar-related Methods
 	 * ---------------------------------------------------
 	 * 
 	 *  createAltar() : Creates an altar at (Location)location.
 	 */
 	public static void createAltar(Location location)
 	{
 		// TODO
 	}
 	
 	/*
 	 *  removeAltar() : Removes the altar from (Location)location.
 	 */
 	public static void removeAltar(Location location)
 	{
 		// TODO
 	}
 	
 	/*
 	 *  getAltars() : Returns an ArrayList<Location> the server's Altars.
 	 */
 	public static ArrayList<Location> getAllAltars() throws Exception
 	{		
 		return null; //TODO
 	}
 	
 	public static ArrayList<Location> getAllDivineBlocks() throws Exception
 	{
 		ArrayList<Location> divineBlocks = new ArrayList<Location>();
 		
 		// Get all Shrines
 		for(Location shrine : getAllShrines())
 		{
 			if(!divineBlocks.contains(shrine)) divineBlocks.add(shrine);
 		}
 		
 		// Get all Altars
 		for(Location altar : getAllAltars())
 		{
 			if(!divineBlocks.contains(altar)) divineBlocks.add(altar);
 		}
 		
 		return divineBlocks;
 	}
 
 }
