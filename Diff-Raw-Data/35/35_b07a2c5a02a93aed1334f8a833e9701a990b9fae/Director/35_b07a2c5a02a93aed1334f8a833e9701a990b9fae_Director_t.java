 package alshain01.Flags;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
 import com.massivecraft.factions.event.FactionsEventDisband;
 
 import alshain01.Flags.area.Area;
 import alshain01.Flags.area.FactionsTerritory;
 import alshain01.Flags.area.GriefPreventionClaim78;
 import alshain01.Flags.area.GriefPreventionClaim77;
 import alshain01.Flags.area.InfinitePlotsPlot;
 import alshain01.Flags.area.ResidenceClaimedResidence;
 import alshain01.Flags.area.World;
 import alshain01.Flags.area.WorldGuardRegion;
 
 /**
  * Class for retrieving area system specific information.
  * 
  * @author Alshain01
  */
 public final class Director {
 	private Director(){}
 	
 	public enum LandSystem {
 		NONE(null, null), 
 		GRIEF_PREVENTION("GriefPrevention", "Grief Prevention"),
 		WORLDGUARD("WorldGuard", "WorldGuard"),
 		RESIDENCE("Residence", "Residence"),
 		INFINITEPLOTS("InfinitePlots","InfinitePlots"),
 		FACTIONS("Factions", "Factions");
 		
 		private String pluginName = null;
 		private String displayName = null;
 		
 		private LandSystem(String name, String displayName) {
 			this.pluginName = name;
 			this.displayName = displayName;
 		}
 		
 		/**
 		 * Retrieves the plug-in name as indicated in it's plugin.yml
 		 * 
 		 * @return The case sensitive plugin.yml name for the enumerated value
 		 */
 		@Override
 		public String toString() {
 			return this.pluginName;
 		}
 		
 		/**
 		 * Retrieves a user friendly string, including spaces, for the plug-in.
 		 * 
 		 * @return The user friendly name of the plugin
 		 */
 		public String getDisplayName() {
 			return this.displayName;
 		}
 		
 		/**
 		 * Retrieves the enumeration that matches the case sensitive plugin.yml name.
 		 * 
 		 * @return The enumeration. LandSystem.NONE if no matches found.
 		 */
 		public static LandSystem getByName(String name) {
 			for(LandSystem p : LandSystem.values()) {
 				if(name.equals(p.pluginName)) { return p; }
 			}
 			return LandSystem.NONE;
 		}
 	}
 	
 	protected static void enableMrClean(PluginManager pm) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			if(Float.valueOf(pm.getPlugin(getSystem().toString())
 					.getDescription().getVersion().substring(0,3)) >= 7.8) {
 				
 				pm.registerEvents(new GriefPreventionCleaner(), Flags.instance);
 			}
 		} else if(getSystem() == LandSystem.RESIDENCE) {
 			pm.registerEvents(new ResidenceCleaner(), Flags.instance);
 		} else if(getSystem() == LandSystem.FACTIONS) {
 			pm.registerEvents(new FactionsCleaner(), Flags.instance);
 		}
 	}
 	
 	private static class GriefPreventionCleaner implements Listener {
 		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onClaimDeleted(ClaimDeletedEvent e) {
 			// Cleanup the database, keep the file from growing too large.
 			new GriefPreventionClaim78(e.getClaim().getID()).remove();
 		}
 	}
 	
 	private static class ResidenceCleaner implements Listener {
 		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onResidenceDelete(ResidenceDeleteEvent e) {
 			// Cleanup the database, keep the file from growing too large.
 			new ResidenceClaimedResidence(e.getResidence().getName()).remove();
 		}
 	}
 	
 	private static class FactionsCleaner implements Listener {
 		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onFactionDisband(FactionsEventDisband e) {
 			for(org.bukkit.World w : Bukkit.getServer().getWorlds()) {
 				new FactionsTerritory(e.getFaction(), w).remove();
 			}
 		}
 	}
 	
 	/**
 	 * Retrieves the current land system in use by Flags.
 	 * 
 	 * @return The current Land Management System in use by Flags.
 	 */
 	public static LandSystem getSystem() {
 		return Flags.instance.currentSystem;
 	}
 
 	/**
 	 * Retrieves an area from the data store at a specific location.
 	 *  
 	 * @param location The location to request an area.
 	 * @return An Area from the configured system or the world if no area is defined.
 	 */
 	public static Area getAreaAt(Location location) {
 		Area area = null;
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			Plugin plugin = Flags.instance.getServer().getPluginManager().getPlugin("GriefPrevention");
 			if(Float.valueOf(plugin.getDescription().getVersion().substring(0, 3)) >= 7.8) {
 				area = new GriefPreventionClaim78(location);
			} else if(Float.valueOf(plugin.getDescription().getVersion().substring(0, 3)) == 7.7) {
 				area = new GriefPreventionClaim77(location);
 			} else {
 				Flags.instance.getLogger().warning("Unsupported Grief Prevention version detected. Shutting down integrated support. Only world flags will be available.");
 				Flags.instance.currentSystem = LandSystem.NONE;
 			}
 		}
 		else if(getSystem() == LandSystem.WORLDGUARD) { area = new WorldGuardRegion(location); }
 		else if(getSystem() == LandSystem.RESIDENCE) { area = new ResidenceClaimedResidence(location); }
 		else if(getSystem() == LandSystem.INFINITEPLOTS) { area = new InfinitePlotsPlot(location); }
 		else if(getSystem() == LandSystem.FACTIONS) { area = new FactionsTerritory(location); }
 		
 		if(area == null || !area.isArea()) {
 			area = new World(location);
 		}
 		return area;
 	}
 	
 	/**
 	 * Retrieves an area by system specific name
 	 * GriefPrevention = ID number
 	 * WorldGuard = worldname.regionname
 	 * Residence = Residence name OR ResidenceName.SubzoneName
 	 * InifitePlots = OwnerName.PlotName
 	 * Factions = worldname.FactionID
 	 * 
 	 * @param name The system specific name of the area or world name
 	 * @return The Area requested, may be null in cases of invalid system selection.
 	 * @deprecated Functionality is sketchy
 	 */
 	@Deprecated
 	public static Area getArea(String name) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			Plugin plugin = Flags.instance.getServer().getPluginManager().getPlugin("GriefPrevention");
 			if(Float.valueOf(plugin.getDescription().getVersion()) >= 7.8) {
 				Long ID = Long.parseLong(name);
 				return new GriefPreventionClaim78(ID);
 			} else if(Float.valueOf(plugin.getDescription().getVersion()) == 7.7){
 				Long ID = Long.parseLong(name);
 				return new GriefPreventionClaim77(ID);
 			} else {
 				Flags.instance.getLogger().warning("Unsupported Grief Prevention version detected. Shutting down integrated support. Only world flags will be available.");
 				Flags.instance.currentSystem = LandSystem.NONE;
 			}
 		} else if(getSystem() == LandSystem.WORLDGUARD) { 
 			String[] path = name.split("\\.");
 			name = name.replaceAll(path[0] + ".", "");
 			return new WorldGuardRegion(Bukkit.getServer().getWorld(path[0]), name);
 		} else if(getSystem() == LandSystem.RESIDENCE) { 
 			return new ResidenceClaimedResidence(name);
 		} else if(getSystem() == LandSystem.INFINITEPLOTS) { 
			return new InfinitePlotsPlot(name);
 		} else if(getSystem() == LandSystem.FACTIONS) { 
 			String[] path = name.split("\\.");
			return new FactionsTerritory(path[0], path[1]);
 		}
 		return null;
 	}
 	
 	/**
	 * Returns a list of system specific area names stored in the database
 	 * 
 	 * @return A list containing all the area names.
 	 */
 	public static Set<String> getAreaNames() {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) { return Flags.instance.dataStore.readKeys("GriefPreventionData"); }
		if(getSystem() == LandSystem.RESIDENCE) { return Flags.instance.dataStore.readKeys("ResidenceData"); }
 		
 		if(getSystem() == LandSystem.WORLDGUARD) {
 			Set<String> worlds = Flags.instance.dataStore.readKeys("WorldGuardData");
 			Set<String> areas = new HashSet<String>();
 			for(String world : worlds) {
 				Set<String>localAreas = Flags.instance.dataStore.readKeys("WorldGuardData." + world);
 				for(String localArea : localAreas) {
 					areas.add(world + "." + localArea);
 				}
 			}
 			
 			return areas;
 		}
 		
 		if(getSystem() == LandSystem.INFINITEPLOTS) {
 			Set<String> players = Flags.instance.dataStore.readKeys("InfinitePlotsData");
 			Set<String> areas = new HashSet<String>();
 			for(String player : players) {
 				Set<String>localAreas = Flags.instance.dataStore.readKeys("InifitePlotsData." + player);
 				for(String localArea : localAreas) {
 					areas.add(player + "." + localArea);
 				}
 			}
 			return areas; 
 		}
 		
 		if(getSystem() == LandSystem.FACTIONS) {
 			Set<String> worlds = Flags.instance.dataStore.readKeys("FactionsData");
 			Set<String> areas = new HashSet<String>();
 			for(String world : worlds) {
 				Set<String>localAreas = Flags.instance.dataStore.readKeys("FactionsData." + world);
 				for(String localArea : localAreas) {
 					if(!areas.contains(localArea)) {
 						areas.add(world + "." + localArea);
 					}
 				}
 			}
 			
 			return areas;
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Retrieves a list of all flags set in the area.
 	 * 
 	 * @param area The system specific area name.
 	 * @return a list of all flags for the provided area.
 	 */
 	public static Set<String> getAreaFlags(String area) {
 		return Flags.instance.dataStore.readKeys(area);
 	}
 	
 	
 	/**
 	 * @deprecated Use getSystemAreaType()
 	 * @return The user friendly name.
 	 */
 	@Deprecated
 	public static String getAreaType() {
 		return getSystemAreaType();
 	}
 	
 	/**
 	 * Returns a user friendly name of the area type of the configured system, capitalized.
 	 * For use when the name is required even though an area does not exist (such as error messages).
 	 * If you have an area instance, use Area.getAreaType() instead.
 	 * 
 	 * @return The user friendly name.
 	 */
 	public static String getSystemAreaType() {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) { return Message.GriefPrevention.get(); }
 		if(getSystem() == LandSystem.WORLDGUARD) { return Message.WorldGuard.get(); }
 		if(getSystem() == LandSystem.RESIDENCE) { return Message.Residence.get(); }
 		if(getSystem() == LandSystem.INFINITEPLOTS) { return Message.InfinitePlots.get(); }
 		if(getSystem() == LandSystem.FACTIONS) { return Message.Factions.get(); }
 		// Should never make it here.
 		return Message.World.get();
 	}
 	
 	/**
 	 * Retrieves whether or not a player is in Pvp combat that
 	 * is being monitored by the system
 	 * 
 	 * Only supports Grief Prevention.
 	 * 
 	 * @param player The player to request information for
 	 * @return True if in pvp combat, false is not or if system is unsupported.
 	 */
 	
 	public static boolean inPvpCombat(Player player) {
 		if(getSystem() == LandSystem.GRIEF_PREVENTION) {
 			return me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getPlayerData(player.getName()).inPvpCombat();
 		}
 		return false;
 	}
 }
