 package com.randrdevelopment.propertygroup.regions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import com.randrdevelopment.propertygroup.PropertyGroup;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.flags.Flag;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
 import com.sk89q.worldguard.protection.flags.StringFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class RegionTools {
 	private WorldGuardPlugin wgp;
 
 	private RegionTools () {
 		wgp = PropertyGroup.getWorldGuard();
 		if (wgp == null) {
 			return;
 		}
 	}
 	
	private boolean createRegion(String regionName, World world, Vector L1, Vector L2, int Priority){
 		RegionManager m = wgp.getRegionManager(world);
 		if (m == null)
 			return false;
 		
		BlockVector min = new BlockVector(L1.getX(), L1.getY(), L1.getZ());
        BlockVector max = new BlockVector(L2.getX(), L2.getY(), L2.getZ());
 		
         StateFlag PVP = new StateFlag("pvp", true);
         StateFlag TNT = new StateFlag("tnt", true);
         //com.sk89q.worldguard.protection.flags.DefaultFlag.GREET_MESSAGE
         StringFlag GREET_MESSAGE = new StringFlag("greet_message");
         ProtectedRegion region = new ProtectedCuboidRegion(regionName, min, max);
         region.setFlag(GREET_MESSAGE, "Welcome to Property");
         region.setFlag(PVP, State.ALLOW);
         RegionManager regionManager = wgp.getRegionManager(world);
         regionManager.addRegion(region);
         
 		try {
 			regionManager.save();
 			return true;
 		} catch (ProtectionDatabaseException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
	public static boolean createProtectedRegion(String regionName, String worldName, Location L1, Location L2, int Priority){
 		RegionTools rt = new RegionTools();
		Vector min = new Vector();
		min.setX(L1.getX());
		min.setZ(L1.getZ());
		min.setY(0);
		
		Vector max = new Vector();
		max.setX(L2.getX());
		max.setZ(L2.getZ());
		max.setY(255);
 		
 		World world = Bukkit.getWorld(worldName);
 		
		if (rt.createRegion(regionName, world, min, max, Priority))
 			return true;
 		
 		return false;
 	}
 }
