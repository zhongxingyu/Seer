 package org.melonbrew.fe;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicePriority;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.melonbrew.fe.database.Account;
 import org.melonbrew.fe.database.Database;
 import org.melonbrew.fe.database.databases.FlatFile;
 import org.melonbrew.fe.database.databases.MySQLDB;
 import org.melonbrew.fe.listeners.FePlayerListener;
 
 public class Fe extends JavaPlugin {
 	private Logger log;
 	
 	private API api;
 	
 	private Database database;
 	
 	public void onEnable(){
 		log = getServer().getLogger();
 		
 		getDataFolder().mkdirs();
 		
 		new FePlayerListener(this);
 		
 		getConfig().options().copyDefaults(true);
 		
 		getConfig().options().header("Fe Config - melonbrew.org\n" +
 				"holdings - The amount of money that the player will start out with\n" +
 				"type - The type of database used (mysql or flatfile)\n");
 		
 		saveConfig();
 		
 		api = new API(this);
 		
 		if (!setupDatabase()){
 			return;
 		}
 		
 		setupVault();
 		
 		getCommand("fe").setExecutor(new FeCommand(this));
 	}
 	
 	private void setupVault(){
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
 		
 		if (economyProvider != null){
 			getServer().getServicesManager().unregister(economyProvider.getProvider());
 		}
 		
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		
		if (vault != null){
			getServer().getServicesManager().register(Economy.class, new Economy_Fe(this), this, ServicePriority.Highest);
		}
 	}
 	
 	public void onDisable(){
 		getServer().getServicesManager().unregisterAll(this);
 		
 		getFeDatabase().close();
 	}
 	
 	public void log(String message){
 		log.info("[Fe] " + message);
 	}
 	
 	public void log(Phrase phrase){
 		log(phrase.parse());
 	}
 	
 	public Database getFeDatabase(){
 		return database;
 	}
 	
 	public API getAPI(){
 		return api;
 	}
 	
 	private boolean setupDatabase(){
 		String type = getConfig().getString("type");
 		
 		if (type.equalsIgnoreCase("flatfile")){
 			database = new FlatFile(this);
 		}else {
 			database = new MySQLDB(this);
 		}
 		
 		if (!database.init()){
 			log(Phrase.DATABASE_FAILURE_DISABLE);
 			
 			getServer().getPluginManager().disablePlugin(this);
 			
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public ConfigurationSection getMySQLConfig(){
 		return getConfig().getConfigurationSection("mysql");
 	}
 	
 	public String getReadName(Account account){
 		return getReadName(account.getName());
 	}
 	
 	public String getReadName(String name){
 		name = name.toLowerCase();
 		
 		OfflinePlayer player = getServer().getOfflinePlayer(name);
 		
 		if (player != null){
 			name = player.getName();
 		}
 		
 		return name;
 	}
 	
 	public String getMessagePrefix(){
 		return ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Fe" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
 	}
 	
 	public String getCurrencySingle(){
 		return "Fe";
 	}
 	
 	public String getCurrencyMultiple(){
 		return "Fe";
 	}
 	
 	public String getEqualMessage(String inBetween, int length){
 		return getEqualMessage(inBetween, length, length);
 	}
 	
 	public String getEqualMessage(String inBetween, int length, int length2){
 		String equals = getEndEqualMessage(length);
 		
 		String end = getEndEqualMessage(length2);
 		
 		return equals + ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + inBetween + ChatColor.DARK_GRAY + "]" + end;
 	}
 	
 	public String getEndEqualMessage(int length){
 		String message = ChatColor.GRAY + "";
 		
 		for (int i = 0; i < length; i++){
 			message += "=";
 		}
 		
 		return message;
 	}
 }
