 package com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.runetooncraft.plugins.EasyMobArmory.EMA;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.Eggs;
 
 public class SpawnerHandler {
 	public static Eggs eggs = EMA.eggs;
 	public static SpawnerConfig Spawners = EMA.Spawners;
 	public static HashMap<Location, SpawnerCache> SpawnerCache = new HashMap<Location, SpawnerCache>();
 	public static Boolean IsEMASpawner(Location loc) {
 		if(Spawners.getList("Spawners.List").contains(Spawners.LocString(loc))) {
 			return true;
 		}else{
 			return false;
 		}
 	}
 	public static void NewEMASpawner(Block b,Player p) {
 		Inventory inv = Bukkit.createInventory(p, 54, "Spawnerinv");
 		SpawnerCache.put(b.getLocation(), new SpawnerCache(b,b.getLocation(),inv));
 		String LocString = Spawners.LocString(b.getLocation());
 		Spawners.addtolist("Spawners.List", LocString);
 		Spawners.SetString("Spawners." + LocString + ".Inventory", InventorySerializer.tobase64(inv));
 		ArrayList<String> EggList = new ArrayList<String>();
 		Spawners.SetList("Spawners." + LocString + ".EggList",EggList);
 	}
 	public static void OpenSpawnerInventory(Block b,Player p) {
 		Inventory inv = Bukkit.createInventory(p, 54, "Spawnerinv");
 		if(SpawnerCache.get(b.getLocation()) == null) {
 			LoadSpawner(b.getLocation());
 			SpawnerCache spawner = SpawnerCache.get(b.getLocation());
 			inv.setContents(spawner.getInventory().getContents());
 		}else{
 			SpawnerCache spawner = SpawnerCache.get(b.getLocation());
 			inv.setContents(spawner.getInventory().getContents());
 			p.openInventory(inv);
 		}
 	}
 	public static void SetSpawnerInventory(Inventory i, SpawnerCache sc) {
 		sc.getInventory().setContents(i.getContents());
		SaveSpawnerCache(sc);
 	}
 	private static void LoadSpawner(Location SpawnerLocation) {
 		World world = SpawnerLocation.getWorld();
 		Block b = world.getBlockAt(SpawnerLocation);
 		String LocString = Spawners.LocString(SpawnerLocation);
 		Inventory inv = InventorySerializer.frombase64(Spawners.getString("Spawners." + LocString + ".Inventory"));
 		SpawnerCache.put(SpawnerLocation, new SpawnerCache(b,SpawnerLocation,inv));
 	}
 	public static SpawnerCache getSpawner(Location SpawnerLocation) {
 		World world = SpawnerLocation.getWorld();
 		Block b = world.getBlockAt(SpawnerLocation);
 		String LocString = Spawners.LocString(SpawnerLocation);
 		Inventory inv = InventorySerializer.frombase64(Spawners.getString("Spawners." + LocString + ".Inventory"));
 		return new SpawnerCache(b,SpawnerLocation,inv);
 	}
 	public static void SaveSpawnerCache(SpawnerCache sc) {
 		String LocString = Spawners.LocString(sc.getLocation());
 		Inventory i = sc.getInventory();
 		Spawners.SetString("Spawners." + LocString + ".Inventory", InventorySerializer.tobase64(i));
 		ItemStack[] EggsStack = i.getContents();
 		for(ItemStack is : EggsStack) {
 			Spawners.addtolist("Spawners." + LocString + ".EggList", EggHandler.getEggID(is));
 		}
 	}
 }
