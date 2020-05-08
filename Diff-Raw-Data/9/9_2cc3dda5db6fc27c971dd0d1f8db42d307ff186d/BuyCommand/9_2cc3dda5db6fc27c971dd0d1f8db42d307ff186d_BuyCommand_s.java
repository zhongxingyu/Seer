 package net.mayateck.BuyCommand;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BuyCommand extends JavaPlugin implements Listener{
 	public static String head = "&8[&3BuyCommand&8]&r ";
 	public static String tag = "";
 	public static String ver = "";
 	public static PluginDescriptionFile pluginFile = null;
 	public static Permission permission = null;
 	public static Economy economy = null;
 	public static char chatChar = '&';
 	
 	@Override
 	public void onEnable(){
 		pluginFile = this.getDescription();
 		ver = pluginFile.getVersion();
 		tag = BuyCommandHandler.parseColor("&8#&f===&8# &9BuyCommand &ev"+ver+" &fby &3&oWehttam664 &8#&f===&8#");
		getLogger().info(tag);
 		getLogger().info("Checking for updates...");
 			// TODO: Check for updates.
 		getLogger().info("Setting up...");
 			this.saveDefaultConfig();
 			getCommand("buycommand").setExecutor(new BuyCommandHandler(this));
 			new EconomyHandler(this);
 			new NodeSetup(this);
 			if (!setupEconomy()){
 	            getLogger().severe(String.format("Unable to load, Vault dependency not present!"));
 	            getServer().getPluginManager().disablePlugin(this);
 	            return;
 	        }
 			setupPermissions();
 			// No need for event handlers.
 		getLogger().info("Ready.");
 		getLogger().info("#================================================#");
 	}
 	
 	@Override
 	public void onDisable(){
		getLogger().info(tag);
 		getLogger().info("Wrapping up...");
 			// Reset these in-case the thread lingers.
 			pluginFile = null;
 			ver = "";
 		getLogger().info("Shut down. Goodbye.");
 		getLogger().info("#================================================#");
 	}
 	
 	private boolean setupPermissions(){
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
 	private boolean setupEconomy(){
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (rsp != null) {
             economy = rsp.getProvider();
             return (economy != null);
         }
         return false;
     }
 }
