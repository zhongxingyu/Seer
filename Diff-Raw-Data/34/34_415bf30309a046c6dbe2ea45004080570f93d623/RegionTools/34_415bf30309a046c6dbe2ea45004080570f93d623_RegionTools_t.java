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
 	
	private boolean createRegion(String regionName, World world, int x1, int x2, int y1, int y2, int z1, int z2, int Priority){
 		RegionManager m = wgp.getRegionManager(world);
 		if (m == null)
 			return false;
 		
		BlockVector min = new BlockVector(x1, y1, z1);
        BlockVector max = new BlockVector(x2, y2, z2);
 		
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
 	
	public static boolean createProtectedRegion(String regionName, String worldName, int x1, int x2, int y1, int y2, int z1, int z2, int Priority){
 		RegionTools rt = new RegionTools();
 		
 		World world = Bukkit.getWorld(worldName);
 		
		if (rt.createRegion(regionName, world, x1, x2, y1, y2, z1, z2, Priority))
 			return true;
 		
 		return false;
 	}
 }
