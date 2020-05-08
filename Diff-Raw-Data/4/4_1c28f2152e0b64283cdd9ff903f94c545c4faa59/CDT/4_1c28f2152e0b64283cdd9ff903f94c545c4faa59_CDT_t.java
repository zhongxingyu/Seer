 package com.runetooncraft.plugins.CostDistanceTeleporter;
 
 import java.io.File;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.earth2me.essentials.IEssentials;
 
 public class CDT extends JavaPlugin {
 	/* Currently CDT depends on Vault and for now, Essentials. It depends on essentials right now because
 	 * it is the only warp plugin that CDT works with at the moment. More could possibly come later.
 	 */
 	Config config = null;
 	static HandleOutsideConfigs spawnyml = null;
 	static HandleOutsideConfigs essconf = null;
 	public static IEssentials ess;
 	public static Economy econ;
 	public static Permission perms;
 	public static boolean EssentialsSpawn;
 	public static boolean Essentials;
 	public static boolean PerWarpPermissions;
 	public static boolean permsenabled;
 	public void onEnable() {
 		loadconfig();
 		checkessentials();
 		checkessentialsspawn();
 		checkvault();
 		getServer().getPluginManager().registerEvents(new Teleportlistener(config), this);
 		getCommand("cdt").setExecutor(new Commandlistener(config));
 		checkperms();
 	}
 
 	private void checkperms() {
		if(perms.getName() != null) {
			Messenger.info("Permissions logged, with Per-Warp-Permissions set to " + PerWarpPermissions);
 			permsenabled = true;
 		}else{
 			Messenger.info("Permissions integration disabled, no permissions plugin found.");
 			permsenabled = false;
 		}
 	}
 
 	private void checkessentialsspawn() { //Can now support /spawn
 		if(getServer().getPluginManager().isPluginEnabled("EssentialsSpawn")) {
 			Messenger.info("Essentials-Spawn integration enabled");
 			EssentialsSpawn = spawnymlcheck();
 		}else{
 			EssentialsSpawn = false;
 		}
 	}
 	public static Location parseSpawnYmlLoc() {
 		String worldstring = spawnyml.getConfig().getString("spawns.default.world");
 		int x = spawnyml.getConfig().getInt("spawns.default.x");
 		int y = spawnyml.getConfig().getInt("spawns.default.y");
 		int z = spawnyml.getConfig().getInt("spawns.default.z");
 		return new Location(Bukkit.getWorld(worldstring), x, y, z);
 	}
 	private boolean spawnymlcheck() {
 		File file = new File(ess.getDataFolder(), "spawn.yml");
 		spawnyml = new HandleOutsideConfigs(file);
 		if (!spawnyml.load()) {
 			return false;
 		}else{
 			return true;
 		}
 		
 	}
 	private boolean essymlcheck() {
 		File file = new File(ess.getDataFolder(), "config.yml");
 		essconf = new HandleOutsideConfigs(file);
 		if (!essconf.load()) {
 			return false;
 		}else{
 			return true;
 		}
 		
 	}
 	private void setupessconfig() {
 		PerWarpPermissions = essconf.getConfig().getBoolean("per-warp-permission");
 	}
 
 	private void checkvault() {
 		if(getServer().getPluginManager().getPlugin("Vault") == null) {
 			Messenger.severe("Vault not found, CDT disabling");
 			this.getServer().getPluginManager().disablePlugin(this);
 		}else{
 			Messenger.info("Found Vault");
 			RegisteredServiceProvider<Economy> eco = getServer().getServicesManager().getRegistration(Economy.class);
 			econ = eco.getProvider();
 			 RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
 		    perms = rsp.getProvider();
 		}
 		
 	}
 
 	private void loadconfig() {
 		File dir = this.getDataFolder();
 		if (!dir.exists()) dir.mkdir();
 		File file = new File(this.getDataFolder(), "config.yml");
 		config = new Config(file);
 		if (!config.load()) {
 			this.getServer().getPluginManager().disablePlugin(this);
 			throw new IllegalStateException("The config-file was not loaded correctly!");
 		}
 		passconfig();
 //		config.save(); This was just for debugging purposes
 	}
 
 	private void passconfig() {
 		Messenger m = new Messenger(config);
 		parseLocation pl = new parseLocation(config);
 	}
 
 	private void checkessentials() {
 		Plugin essPlugin;
 		if(getServer().getPluginManager().isPluginEnabled("Essentials")) {
 			Messenger.info("Essentials integration enabled");
 			essPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
 			ess = (IEssentials)essPlugin;
 			Essentials = essymlcheck();
 			if(essymlcheck()) {
 				setupessconfig();
 			}
 		}else{
 			Messenger.severe("Essentials not found, CDT disabling");
 			this.getServer().getPluginManager().disablePlugin(this);
 		}
 		
 	}
 }
