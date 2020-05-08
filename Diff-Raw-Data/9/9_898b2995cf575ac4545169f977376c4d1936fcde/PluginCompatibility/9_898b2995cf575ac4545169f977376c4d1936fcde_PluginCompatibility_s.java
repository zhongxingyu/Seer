 package org.zonedabone.magicchest;
 
 import net.dandielo.api.traders.tNpcAPI;
 import net.dandielo.citizens.traders_v3.bukkit.DtlTraders;
 import com.gmail.filoghost.chestcommands.Main;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.plugin.Plugin;
 
 public class PluginCompatibility {
 	
 	private static DtlTraders getdtlTraders() {
 		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("dtlTraders");
 		// DtlTraders may not be loaded
 		if (plugin == null || !(plugin instanceof DtlTraders)) {
 			return null;
 		}
 		return (DtlTraders) plugin;
 	}
 	
 	private static Main getChestCommands() {
 		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("ChestCommands");
 		// ChestCommands may not be loaded
 		if (plugin == null || !(plugin instanceof Main)) {
 			return null;
 		}
 		return (Main) plugin;
 	}
 	
 	public static String printPluginCompatibilty()
 	{
 		String compat = "";
 		if(getdtlTraders() != null)
 			compat = compat + "Compatibility loaded for dtlTraders!\n";
 		if(getChestCommands() != null)
 			compat = compat + "Compatibility loaded for ChestCommands!";
 		return compat;
 	}
 	
 	public static boolean isCompatibleInventory(InventoryOpenEvent e)
 	{
 		if(getdtlTraders() != null)
 			if(tNpcAPI.isTNpcInventory((Player)e.getPlayer()))
 				return false;
 		if(getChestCommands() != null)
 			if(e.getInventory().getTitle().startsWith("§r"))
 				return false;
 		return true;
 	}
 }
