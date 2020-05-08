 package regionPreserve;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class RegionLoading {
 		
 	public static List<ActiveRegion> LoadRegions()
 	{
 		List<ActiveRegion> regions = new ArrayList<ActiveRegion>();
 		FileConfiguration regionsData = null;
 		File regionsFile = new File("plugins/RegionPreserve", "regions.yml");
 		regionsData = YamlConfiguration.loadConfiguration(regionsFile);
 		Set<String> keys = regionsData.getKeys(false);
 		for (String key : keys)
 		{
 			if(Bukkit.getWorld(regionsData.getString(key + ".world")) != null)
 			{
 				Location pos1 = regionsData.getVector(key + ".pos1").toLocation(Bukkit.getWorld(regionsData.getString(key + ".world")));
 				Location pos2 = regionsData.getVector(key + ".pos2").toLocation(Bukkit.getWorld(regionsData.getString(key + ".world")));
 				String name = key;
 				ActiveRegion ar = new ActiveRegion(name, pos1, pos2);
 				List<Flags.Flag> flags = Flags.stringListToFlagList(regionsData.getStringList(key + ".flags"));
 				ar.flags = flags;
 				ar.enterMessage = regionsData.getString(key + ".enter");
 				ar.leaveMessage = regionsData.getString(key + ".leave");
 				ar.addListener(new EnterLeaveMessages());
 				regions.add(ar);
 			}
 			else
 			{
 				System.out.print("[RegionPreserve] The world '" + regionsData.getString(key + ".world") + "' does not exist!");
 			}
 		}
 		System.out.print("[RegionPreserve] Loaded regions!");
 		return regions;
 	}
 	
 	public static void SaveRegions(List<ActiveRegion> regions)
 	{
 		FileConfiguration regionsData = null;
 		File regionsFile = new File("plugins/RegionPreserve", "regions.yml");
 		regionsData = YamlConfiguration.loadConfiguration(regionsFile);
 		for (ActiveRegion r : regions)
 		{
 			regionsData.set(r.name + ".pos1", r.pointOne.toVector());
 			regionsData.set(r.name + ".pos2", r.pointTwo.toVector());
 			regionsData.set(r.name + ".world", r.pointOne.getWorld().getName());
 			regionsData.set(r.name + ".flags", Flags.flagsListToStringList(r.flags));
 			regionsData.set(r.name + ".enter", r.enterMessage);
 			regionsData.set(r.name + ".leave", r.leaveMessage);
 		}
 		try {
 			regionsData.save(regionsFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.print("[RegionPreserve] Saved regions!");
 	}
 
 }
