 package com.andoutay.admintime;
 
import org.kitteh.vanish.staticaccess.VanishNoPacket;

 import com.earth2me.essentials.Essentials;
 
 import ru.tehkode.permissions.PermissionManager;
 
 public class ATClassManager
 {
 	public static PermissionManager getPexManager(AdminTime plugin) throws ClassNotFoundException, NoClassDefFoundError
 	{
 		return plugin.getServer().getServicesManager().load(PermissionManager.class);
 	}
 	
 	public static Essentials getEssentials(AdminTime plugin)
 	{
 		return (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
 	}
	
	public static VanishNoPacket getVanishNoPacket(AdminTime plugin)
	{
		return (VanishNoPacket) plugin.getServer().getPluginManager().getPlugin("VanishNoPacket");
	}
 }
