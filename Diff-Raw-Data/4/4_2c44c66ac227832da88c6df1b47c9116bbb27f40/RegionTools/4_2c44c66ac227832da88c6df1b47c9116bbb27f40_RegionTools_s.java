 package com.randrdevelopment.propertygroup.regions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.randrdevelopment.propertygroup.PropertyGroup;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
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
 	
 	private boolean createRegion(String regionName, World world, int x1, int x2, int y1, int y2, int z1, int z2, String playerName, FileConfiguration propertyConfig, String propertyGroup){
 		RegionManager m = wgp.getRegionManager(world);
 		if (m == null)
 			return false;
 		
 		BlockVector min = new BlockVector(x1, y1, z1);
         BlockVector max = new BlockVector(x2, y2, z2);
 		
         //com.sk89q.worldguard.protection.flags.DefaultFlag.GREET_MESSAGE
         ProtectedRegion region = new ProtectedCuboidRegion(regionName, min, max);
          
         // Set pvp Flag
    		if (propertyConfig.getBoolean(propertyGroup+".pvp"))
    			region.setFlag(DefaultFlag.PVP, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.PVP, State.DENY);
    		
         // Set mob-damage Flag
         if (propertyConfig.getBoolean(propertyGroup+".mob-damage"))
         	region.setFlag(DefaultFlag.MOB_DAMAGE, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.MOB_DAMAGE, State.DENY);
    		
         // Set mob-spawning Flag
         if (propertyConfig.getBoolean(propertyGroup+".mob-spawning"))
         	region.setFlag(DefaultFlag.MOB_SPAWNING, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.MOB_SPAWNING, State.DENY);
         
         // Set creeper-explosion Flag
         if (propertyConfig.getBoolean(propertyGroup+".creeper-explosion"))
         	region.setFlag(DefaultFlag.CREEPER_EXPLOSION, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.CREEPER_EXPLOSION, State.DENY);
         
         // Set ghast-fireball Flag
         if (propertyConfig.getBoolean(propertyGroup+".ghast-fireball"))
         	region.setFlag(DefaultFlag.GHAST_FIREBALL, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.GHAST_FIREBALL, State.DENY);
         
         // Set tnt Flag
         if (propertyConfig.getBoolean(propertyGroup+".tnt"))
         	region.setFlag(DefaultFlag.TNT, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.TNT, State.DENY);
         
         // Set lighter Flag
         if (propertyConfig.getBoolean(propertyGroup+".lighter"))
         	region.setFlag(DefaultFlag.LIGHTER, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.LIGHTER, State.DENY);
         
         // Set fire-spread Flag
         if (propertyConfig.getBoolean(propertyGroup+".fire-spread"))
         	region.setFlag(DefaultFlag.FIRE_SPREAD, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.FIRE_SPREAD, State.DENY);
         
         // Set lava-fire Flag
         if (propertyConfig.getBoolean(propertyGroup+".lava-fire"))
         	region.setFlag(DefaultFlag.LAVA_FIRE, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.LAVA_FIRE, State.DENY);
         
         // Set lightning Flag
         if (propertyConfig.getBoolean(propertyGroup+".lightning"))
         	region.setFlag(DefaultFlag.LIGHTNING, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.LIGHTNING, State.DENY);
         
         // Set chest-access Flag
         if (propertyConfig.getBoolean(propertyGroup+".chest-access"))
         	region.setFlag(DefaultFlag.CHEST_ACCESS, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.CHEST_ACCESS, State.DENY);
         
         // Set water-flow Flag
         if (propertyConfig.getBoolean(propertyGroup+".water-flow"))
         	region.setFlag(DefaultFlag.WATER_FLOW, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.WATER_FLOW, State.DENY);
         
         // Set lava-flow Flag
         if (propertyConfig.getBoolean(propertyGroup+".lava-flow"))
         	region.setFlag(DefaultFlag.LAVA_FLOW, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.LAVA_FLOW, State.DENY);
         
         // Set use Flag
         if (propertyConfig.getBoolean(propertyGroup+".use"))
         	region.setFlag(DefaultFlag.USE, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.USE, State.DENY);
         
         // Set leaf-decay Flag
         if (propertyConfig.getBoolean(propertyGroup+".leaf-decay"))
         	region.setFlag(DefaultFlag.LEAF_DECAY, State.ALLOW);
         else
         	region.setFlag(DefaultFlag.LEAF_DECAY, State.DENY);
         
         // Set Greeting Message
         if (playerName != null){
         	String Greeting = propertyConfig.getString(propertyGroup+".greeting");
         	Greeting = Greeting.replace("%PropertyName%", regionName);
         	Greeting = Greeting.replace("%Owner%", playerName);
         	region.setFlag(DefaultFlag.GREET_MESSAGE, Greeting);
         } else {
         	String Greeting = propertyConfig.getString(propertyGroup+".greeting-noowner");
         	Greeting = Greeting.replace("%PropertyName%", regionName);
         	Greeting = Greeting.replace("%Owner%", "");
         	region.setFlag(DefaultFlag.GREET_MESSAGE, Greeting);
         }
         
         // Set Farewell Message
         if (playerName != null){
         	String Farewell = propertyConfig.getString(propertyGroup+".farewell");
         	Farewell = Farewell.replace("%PropertyName%", regionName);
         	Farewell = Farewell.replace("%Owner%", playerName);
         	region.setFlag(DefaultFlag.FAREWELL_MESSAGE, Farewell);
         } else {
         	String Farewell = propertyConfig.getString(propertyGroup+".farewell");
         	Farewell = Farewell.replace("%PropertyName%", regionName);
         	Farewell = Farewell.replace("%Owner%", "");
         	region.setFlag(DefaultFlag.FAREWELL_MESSAGE, Farewell);
         }
         
         // Set Owner (if player name is set)
         if (playerName != null){
         	DefaultDomain owners = new DefaultDomain();
         	owners.addPlayer(playerName);
         	region.setOwners(owners);
         }
         
         // Set region priority
         int Priority = propertyConfig.getInt(propertyGroup+".priority");
         region.setPriority(Priority);
         
         // Save Region
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
 	
 	private boolean deleteRegion(String regionName, World world){
 		RegionManager m = wgp.getRegionManager(world);
 		if (m == null)
 			return false;
 		
 		RegionManager regionManager = wgp.getRegionManager(world);
 		regionManager.removeRegion(regionName);
 		
 		try {
 			regionManager.save();
 			return true;
 		} catch (ProtectionDatabaseException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private boolean addMember(String regionName, World world, String playerName) {
 		RegionManager m = wgp.getRegionManager(world);
 		if (m == null)
 			return false;
 		
 		RegionManager regionManager = wgp.getRegionManager(world);
 		
 		ProtectedRegion region = regionManager.getRegion(regionName);
		
        DefaultDomain members = new DefaultDomain();
         members.addPlayer(playerName);        
 		region.setMembers(members);
 		
 		regionManager.addRegion(region);
 		
 		try {
 			regionManager.save();
 			return true;
 		} catch (ProtectionDatabaseException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static boolean createProtectedRegion(String regionName, String worldName, int x1, int x2, int y1, int y2, int z1, int z2, String playerName, FileConfiguration propertyConfig, String PropertyGroupName){
 		RegionTools rt = new RegionTools();
 		
 		World world = Bukkit.getWorld(worldName);
 		
 		if (rt.createRegion(regionName, world, x1, x2, y1, y2, z1, z2, playerName, propertyConfig, PropertyGroupName))
 			return true;
 		
 		return false;
 	}
 	
 	public static boolean deleteProtectedRegion(String regionName, String worldName) {
 		RegionTools rt = new RegionTools();
 		
 		World world = Bukkit.getWorld(worldName);
 		
 		if (rt.deleteRegion(regionName, world))
 			return true;
 		
 		return false;
 	}
 	
 	public static boolean addMemberToProtectedRegion(String regionName, String worldName, String playerName) {
 		RegionTools rt = new RegionTools();
 		
 		World world = Bukkit.getWorld(worldName);
 		
 		if (rt.addMember(regionName, world, playerName))
 			return true;
 		
 		return false;
 	}
 }
