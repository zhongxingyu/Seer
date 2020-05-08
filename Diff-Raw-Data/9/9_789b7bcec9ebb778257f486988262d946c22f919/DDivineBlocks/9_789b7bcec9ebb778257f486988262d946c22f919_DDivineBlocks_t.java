 package com.legit2.Demigods;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 
 import com.legit2.Demigods.Libraries.DivineLocation;
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DDataUtil;
 import com.legit2.Demigods.Utilities.DMiscUtil;
 import com.legit2.Demigods.Utilities.DObjUtil;
 
 public class DDivineBlocks
 {	
 	/* ---------------------------------------------------
 	 * Begin Shrine-related Methods
 	 * ---------------------------------------------------
 	 * 
 	 *  createShrine() : Creates a shrine at (Location)location.
 	 */
 	public static void createShrine(int charID, ArrayList<Location> locations)
 	{
 		int blockID = DObjUtil.generateInt(5);
 		
 		ArrayList<DivineLocation> shrines = new ArrayList<DivineLocation>();
 		for(Location location : locations)
 		{
 			DivineLocation shrine = new DivineLocation(location);
 			shrines.add(shrine);
 		}
 		
 		DDataUtil.saveBlockData(blockID, "block_type", "shrine");
 		DDataUtil.saveBlockData(blockID, "block_owner", charID);
 		DDataUtil.saveBlockData(blockID, "block_deity", DCharUtil.getDeity(charID));
 		DDataUtil.saveBlockData(blockID, "block_location", shrines);
 	}
 	
 	/*
 	 *  removeShrine() : Removes the shrine at (Location)location.
 	 */
 	public static void removeShrine(Location location)
 	{
 		int blockID = getID(location);
 		DMiscUtil.serverMsg("Block ID: " + blockID);
 		DDataUtil.removeAllBlockData(blockID);
 	}
 	
 	/*
 	 *  getShrines() : Returns an ArrayList<Location> of (int)charID's Shrines.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Location> getShrines(int charID)
 	{
 		ArrayList<Location> shrines = new ArrayList<Location>();
 		
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{			
 			if(divineBlock.getValue().get("block_owner").equals(charID))
 			{
 				for(DivineLocation block : (ArrayList<DivineLocation>) divineBlock.getValue().get("block_location"))
 				{
 					shrines.add(block.toLocation());
 				}
 			}
 		}
 		return shrines;
 
 	}
 	
 	/*
 	 *  getAllShrines() : Returns an ArrayList<Location> of (Player)player's Shrines.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Location> getAllShrines()
 	{		
 		ArrayList<Location> shrines = new ArrayList<Location>();
 		
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			for(DivineLocation block : (ArrayList<DivineLocation>) divineBlock.getValue().get("block_location"))
 			{
 				shrines.add(block.toLocation());
 			}
 		}
 		
 		return shrines;
 	}
 	
 	/*
 	 *  getOwnerOfShrine() : Returns the owner of the shrine at (Location)location.
 	 */
 	@SuppressWarnings("unchecked")
 	public static int getShrineOwner(Location location)
 	{		
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{	
 			for(DivineLocation divineLoc : (ArrayList<DivineLocation>) divineBlock.getValue().get("block_location"))
 			{				
 				if(divineLoc.toLocation().equals(location)) return DObjUtil.toInteger(divineBlock.getValue().get("block_owner"));
 			}
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getDeityAtShrine() : Returns the deity of the shrine at (Location)location.
 	 */
 	@SuppressWarnings("unchecked")
 	public static String getShrineDeity(Location location)
 	{
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{			
 			for(DivineLocation divineLoc : (ArrayList<DivineLocation>) divineBlock.getValue().get("block_location"))
 			{
 				if(divineLoc.toLocation().equals(location)) return divineBlock.getValue().get("block_deity").toString();
 			}
 		}
 		return null;
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
 	public static ArrayList<Location> getAllAltars()
 	{		
 		return null; //TODO
 	}
 	
 	/* ---------------------------------------------------
 	 * Begin Overall DivineBlock Methods
 	 * ---------------------------------------------------
 	 *
 	 *  getID() : Returns the (int)blockID for the (Location)location.
 	 */
	@SuppressWarnings("unchecked")
 	public static int getID(Location location)
 	{
 		HashMap<Integer, HashMap<String, Object>> divineBlocks = DDataUtil.getAllBlockData();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : divineBlocks.entrySet())
 		{
 			// Define character-specific variables
 			int blockID = divineBlock.getKey();
			for(DivineLocation divineLoc : (ArrayList<DivineLocation>) divineBlock.getValue().get("block_location")) 
			{
				if(divineLoc.toLocation().equals(location)) return blockID;
			}
 		}
 		return -1;
 	}
 	 
 	/*
 	 *  isDivineBlock() : Returns a boolean for if the (Location)location is a divine block.
 	 */
 	public static boolean isDivineBlock(Location location)
 	{
 		if(getAllShrines() != null) for(Location shrine : getAllShrines()) if(shrine.equals(location)) return true;
 		if(getAllAltars() != null) for(Location altar : getAllAltars()) if(altar.equals(location)) return true;
 		return false;
 	}
 	
 	/*
 	 *  getAllDivineBlocks() : Returns an ArrayList of all divine block locations.
 	 */
 	public static ArrayList<Location> getAllDivineBlocks()
 	{
 		ArrayList<Location> divineBlocks = new ArrayList<Location>();
 		
 		// Get all Shrines
 		if(getAllShrines() != null)
 		{
 			for(Location shrine : getAllShrines())
 			{
 				divineBlocks.add(shrine);
 			}
 		}
 			
 		// Get all Altars
 		if(getAllAltars() != null)
 		{
 			for(Location altar : getAllAltars())
 			{
 				divineBlocks.add(altar);
 			}
 		}
 		
 		return divineBlocks;
 	}
 }
