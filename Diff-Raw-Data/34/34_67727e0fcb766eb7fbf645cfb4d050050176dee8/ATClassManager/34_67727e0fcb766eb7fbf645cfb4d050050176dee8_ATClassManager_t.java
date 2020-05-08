 package com.andoutay.admintime;
 
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
 }
