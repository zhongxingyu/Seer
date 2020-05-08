 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import net.milkbowl.vault.permission.Permission;
 
 public class Vault {
 
     private NoItem plugin;
     protected static boolean vaultPerms = false;
 
     public Vault(NoItem plugin) {
 	this.plugin = plugin;
 	vaultPerms = this.setupPerms();
     }
 
     private static Permission permission = null;
 
     private boolean setupPerms() {
 	try {
 	    RegisteredServiceProvider<Permission> permProvider = plugin
 		    .getServer()
 		    .getServicesManager()
 		    .getRegistration(
 			    net.milkbowl.vault.permission.Permission.class);
 	    if (permProvider != null) {
 		permission = permProvider.getProvider();
		Log.info("[NoItem] Hooked into vault for permissions.");
 	    }
 	} catch (NoClassDefFoundError e) {
	    Log.info("[NoItem] Vault not found, using superPerms.");
 	}
 	return permission != null;
     }
 
     protected static boolean has(Player p, String perm) {
 	return permission.has(p, perm);
     }
 }
