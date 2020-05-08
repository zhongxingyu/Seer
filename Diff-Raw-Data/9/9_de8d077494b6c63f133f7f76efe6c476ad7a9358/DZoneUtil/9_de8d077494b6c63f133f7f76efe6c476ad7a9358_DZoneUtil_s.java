 package com.legit2.Demigods.Utilities;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.legit2.Demigods.DDivineBlocks;
 import com.legit2.Demigods.Libraries.DivineBlock;
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.FLocation;
 import com.massivecraft.factions.Faction;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class DZoneUtil
 {
 	public static int SHRINE_RADIUS = 8;
 	public static int ALTAR_RADIUS = 16;
 	
 	/* --------------------------------------------
 	 *  No PVP Zones
 	 * --------------------------------------------
 	 * 
 	 *  zoneNoPVP() : Returns true if (Location)location is within a no PVP zone.
 	 */
 	public static boolean zoneNoPVP(Location location)
 	{
 		if(DConfigUtil.getSettingBoolean("allow_skills_anywhere")) return false;
 		if(DConfigUtil.getSettingBoolean("use_dynamic_pvp_zones"))
 		{
 			// Currently only supports WorldGuard for dynamic PVP zones
 			if(DMiscUtil.getPlugin().WORLDGUARD != null) return !canWorldGuardDynamicPVPAndNotAltar(location);
 			else return zoneAltar(location) != null;
 		}
 		else
 		{
 			if(DMiscUtil.getPlugin().WORLDGUARD != null && DMiscUtil.getPlugin().FACTIONS != null) return !canWorldGuardAndFactionsPVP(location);
 			else if(DMiscUtil.getPlugin().WORLDGUARD != null) return !canWorldGuardFlagPVP(location);
 			else if(DMiscUtil.getPlugin().FACTIONS != null) return !canFactionsPVP(location);
 			else return false;
 		}
 	}
 	
 	/* 
 	 *  enterZoneNoPVP() : Returns true if entering a no PVP zone.
 	 */
 	public static boolean enterZoneNoPVP(Location to, Location from)
 	{
 		if(!DZoneUtil.zoneNoPVP(from) && DZoneUtil.zoneNoPVP(to)) return true;
 		else return false;
 	}
 	
 	/* 
 	 *  exitZoneNoPVP() : Returns true if exiting a no PVP zone.
 	 */
 	public static boolean exitZoneNoPVP(Location to, Location from)
 	{
 		return enterZoneNoPVP(from, to);
 	}
 	
     /*
 	 *  canWorldGuardAndFactionsPVP() : Returns true if PVP is allowed at (Location)location.
 	 */
     public static boolean canWorldGuardAndFactionsPVP(Location location)
     {
     	if(canFactionsPVP(location) && canWorldGuardFlagPVP(location)) return true;
     	else if(!canFactionsPVP(location)) return false;
     	else if(!canWorldGuardFlagPVP(location)) return false;
     	else return true;
     }
 	
     /*
 	 *  canWorldGuardDynamicPVPAndNotAltar() : Returns true if PVP is allowed at (Location)location, and it's not an Altar.
 	 */
     public static boolean canWorldGuardDynamicPVPAndNotAltar(Location location)
     {
     	if(!(zoneAltar(location) != null) && canWorldGuardDynamicPVP(location)) return true;
     	else if(!canWorldGuardDynamicPVP(location)) return false;
     	else if(zoneAltar(location) != null) return false;
     	else return true;
     }
     
     /*
 	 *  canWorldGuardDynamicPVP() : Returns true if PVP is allowed at (Location)location.
 	 */
     public static boolean canWorldGuardDynamicPVP(Location location)
     {	    
 	    ApplicableRegionSet set = DMiscUtil.getPlugin().WORLDGUARD.getRegionManager(location.getWorld()).getApplicableRegions(location);
 	    for (ProtectedRegion region : set)
 		{
 	    	if(region.getId().toLowerCase().contains("nopvp")) return false;
 		}
 	    return true;
     }
     
     /*
 	 *  canWorldGuardFlagPVP() : Returns true if PVP is allowed at (Location)location.
 	 */
     public static boolean canWorldGuardFlagPVP(Location location)
     {	    
     	ApplicableRegionSet set = DMiscUtil.getPlugin().WORLDGUARD.getRegionManager(location.getWorld()).getApplicableRegions(location);
 		return !set.allows(DefaultFlag.PVP);
     }
             
     /*
 	 *  canFactionsPVP() : Returns true if PVP is allowed at (Location)location.
 	 */
     public static boolean canFactionsPVP(Location location)
     {
 		Faction faction = Board.getFactionAt(new FLocation(location.getBlock()));
 		return !(faction.isPeaceful() || faction.isSafeZone());
 	}
     
 
     
     /* --------------------------------------------
 	 *  No Build Zones
 	 * --------------------------------------------
 	 * 
 	 *  zoneNoBuild() : Returns true if (Location)location is within a no Build zone for (Player)player.
 	 */
 	public static boolean zoneNoBuild(Player player, Location location)
 	{
 		if(DMiscUtil.getPlugin().WORLDGUARD != null && DMiscUtil.getPlugin().FACTIONS != null) return !canWorldGuardAndFactionsBuild(player, location);
 		else if(DMiscUtil.getPlugin().WORLDGUARD != null) return !canWorldGuardBuild(player, location);
 		else if(DMiscUtil.getPlugin().FACTIONS != null) return !canFactionsBuild(player, location);
 		else return false;
 	}
 	
 	/* 
 	 *  enterZoneNoBuild() : Returns true if entering a no Build zone.
 	 */
 	public static boolean enterZoneNoBuild(Player player, Location to, Location from)
 	{
 		if(!DZoneUtil.zoneNoBuild(player, from) && DZoneUtil.zoneNoBuild(player, to)) return true;
 		else return false;
 	}
 	
 	/* 
 	 *  exitZoneNoBuilt() : Returns true if exiting a no Build zone.
 	 */
 	public static boolean exitZoneNoBuild(Player player, Location to, Location from)
 	{
 		return enterZoneNoBuild(player, from, to);
 	}
 	
     /*
 	 *  canWorldGuardAndFactionsBuild() : Returns true if (Player)player is allowed to build at (Location)location.
 	 */
     public static boolean canWorldGuardAndFactionsBuild(Player player, Location location)
     {
     	if(canFactionsBuild(player, location) && canWorldGuardBuild(player, location)) return true;
     	else if(!canFactionsBuild(player, location)) return false;
     	else if(!canWorldGuardBuild(player, location)) return false;
     	else return true;
     }
 	
     /*
 	 *  canWorldGuardBuild() : Returns true if (Player)player can build at (Location)location.
 	 */
     public static boolean canWorldGuardBuild(Player player, Location location)
     {
         return DMiscUtil.getPlugin().WORLDGUARD.canBuild(player, location);
     }
     
     /*
 	 *  canFactionsBuild() : Returns true if (Player)player can build at (Location)location.
 	 */
     public static boolean canFactionsBuild(Player player, Location location)
     {
         return DMiscUtil.getPlugin().FACTIONS.isPlayerAllowedToBuildHere(player, location);
     }
     
     /* --------------------------------------------
 	 *  DivineBlock Zones
 	 * --------------------------------------------
 	 * 
 	 *  zoneShrine() : Returns a DivineBlock if (Location)location is within a Shrine's zone.
 	 */
     public static DivineBlock zoneShrine(Location location)
     {
     	for(DivineBlock divineBlock : DDivineBlocks.getAllShrineBlocks())
 		{	
    		 if(location.distance(divineBlock.getLocation()) <= SHRINE_RADIUS) return divineBlock;
 		}
     	return null;
     }
     
 	/* 
 	 *  zoneShrineOwner() : Returns the owner of a Shrine from (Location)location.
 	 */
 	public static int zoneShrineOwner(Location location)
 	{
 		for(Location divineBlock : DDivineBlocks.getAllShrines())
 		{	
    		 if(location.distance(divineBlock) <= SHRINE_RADIUS) return DDivineBlocks.getShrineOwner(divineBlock);
 		}
 		return -1;
 	}
     
 	/* 
 	 *  enterZoneShrine() : Returns true if entering a Shrine zone.
 	 */
 	public static boolean enterZoneShrine(Location to, Location from)
 	{
 		if(!(DZoneUtil.zoneShrine(from) != null) && DZoneUtil.zoneShrine(to) != null) return true;
 		else return false;
 	}
 	
 	/* 
 	 *  exitZoneShrine() : Returns true if exiting a Shrine zone.
 	 */
 	public static boolean exitZoneShrine(Location to, Location from)
 	{
 		return enterZoneShrine(from, to);
 	}
     
 	/* 
 	 *  zoneAltar() : Returns true if (Location)location is within an Altar's zone.
 	 */
     public static DivineBlock zoneAltar(Location location)
     {
     	for(DivineBlock divineBlock : DDivineBlocks.getAllAltarBlocks())
 		{	
     		DivineBlock parentBlock = DDivineBlocks.getDivineBlock((divineBlock.getParent()));
     		if(location.distance(parentBlock.getLocation()) <= ALTAR_RADIUS) return parentBlock;
 		}
     	return null;
     }
     
 	/* 
 	 *  enterZoneAltar() : Returns true if entering an Altar zone.
 	 */
 	public static boolean enterZoneAltar(Location to, Location from)
 	{
 		if(!(DZoneUtil.zoneAltar(from) != null) && DZoneUtil.zoneAltar(to) != null) return true;
 		else return false;
 	}
 	
 	/* 
 	 *  exitZoneAltar() : Returns true if exiting an Altar zone.
 	 */
 	public static boolean exitZoneAltar(Location to, Location from)
 	{
 		return enterZoneAltar(from, to);
 	}
 }
