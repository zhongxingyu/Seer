 package net.worldoftomorrow.noitem.permissions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import net.milkbowl.vault.permission.Permission;
 import net.worldoftomorrow.noitem.NoItem;
 
 public final class VaultHook {
 	protected static Permission permission = null;
	private static boolean loaded;
 	private static VaultHook instance;
 	
 	public VaultHook() {
 		setInstance(this);
 		try {
 			RegisteredServiceProvider<Permission> permissionProvider = 
 					Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 			if (permissionProvider != null) permission = permissionProvider.getProvider();
 			return;
 		} catch (NoClassDefFoundError e) {
 			// Do nothing, vault just isn't loaded.
 		} finally {
 			loaded = (permission != null);
 		}
 		if(!NoItem.getInstance().getConfig().getBoolean("CheckVault")) return;
 	}
 	
 	private static void setInstance(VaultHook i) {
 		instance = i;
 	}
 	
 	public static VaultHook getInstance() {
 		return instance;
 	}
 	
 	public static boolean isLoaded() {
		return loaded;
 	}
 }
