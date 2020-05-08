 package de.autoit4you.bankaccount;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import de.autoit4you.bankaccount.api.API;
 import de.autoit4you.bankaccount.commands.*;
 
 import de.autoit4you.bankaccount.internal.Upgrade;
 import de.autoit4you.bankaccount.tasks.AccountsUpdate;
 import de.autoit4you.bankaccount.tasks.Interest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 	public class BankAccount extends JavaPlugin {
 		//variables
 		public static boolean debug = false;
 	    public static final Logger log = Logger.getLogger("Minecraft");
 	    private Database db = null;
 	    private Vault vault = null;
 	    private Permissions perm = null;
 		private BukkitTask sinterest;
         private static LangManager lang = null;
         private API api = null;
 
 	    @Override
 	    public void onEnable() {
 	    	//initialize languageManager
             lang = new LangManager(this);
 	    	//loading or creating config.yml
 	    	if(!new File("plugins/BankAccount/config.yml").exists())
 	    		saveDefaultConfig();
             //check for need of upgrades
             Upgrade upgrade = Upgrade.getUpgrade(getConfig().getInt("version", -1), this);
             if(upgrade != null) {
                 try {
                     upgrade.performUpgrade();
                 } catch (Throwable t) {
                     t.printStackTrace();
                     getServer().getPluginManager().disablePlugin(this);
                     return;
                 }
             }
             //setting the debug value
 	    	debug = getConfig().getBoolean("debug", false);
 	    	//connecting to database
 	    	try{
 	    		if(getConfig().getString("database.type").equalsIgnoreCase("mysql")){
 	    			String dbserver = getConfig().getString("database.server");
 	    	    	String dbdatabase = getConfig().getString("database.database");
 	    	    	String dbuser = getConfig().getString("database.username");
 	    	    	String dbpwd = getConfig().getString("database.password");
 	    			db = new MySQL(dbserver, dbdatabase, dbuser, dbpwd);
 	    		}else{
 	    			String file = getConfig().getString("database.sqlitefile");
 	    			db = new SQLite("plugins/BankAccount/" + file);
 	    		}
 	    	} catch (Exception e) {
 	    		log.severe(e.getMessage());
 	    		log.severe(String.format("Cannot access database. Are you sure that it is available? Disabling..."));
 	    		getServer().getPluginManager().disablePlugin(this);
 	    		return;
 	    	}
 	    	//Register usage of an economy plugin
 	    	vault = new Vault();
 	    	if(!vault.setupEconomy()){
 	    		log.severe(String.format("Cannot setup economy system. Disabling..."));
 	    		getServer().getPluginManager().disablePlugin(this);
 	    		return;
 	    	}
             //Initialize api
             api = new API(this);
 	    	//Register usage of a permission plugin
 	    	perm = new Permissions();
	    	//Register commands
             getCommand("account").setExecutor(new BankAccountAccountExecutor(this));
             getCommand("account").setTabCompleter(this);
 	    	//Setting up listeners
 	    	if(getConfig().getBoolean("interest.enabled")){
 	    		for (String key : getConfig().getConfigurationSection("interest").getKeys(false)) {
                     try {
                         boolean online = getConfig().getBoolean("interest." + key + ".ownerMustOnline");
                         double percentage = getConfig().getDouble("interest." + key + ".percentage");
                         int minutes = getConfig().getInt("interest." + key + ".minutes");
                         getServer().getScheduler().runTaskTimer(this, new Interest(this, online, percentage, key), 20 * 60 * minutes, 20 * 60 * minutes);
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                 }
 	    	}
             getServer().getScheduler().runTaskTimer(this, new AccountsUpdate(this), 20 * 60 * 5, 20 * 60 * 5);
 	    }
 	    
 	    @Override
 	    public void onDisable() {
            try {
	    	    this.getServer().getScheduler().cancelTasks(this);
 
                api.saveData();
            } catch (NullPointerException e) {
            } catch (Throwable t) {
                t.printStackTrace();
            }
 	    }
 	    
 	    @Override
 	    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
 			return new ArrayList<String>();
 	    }
 
         public LangManager getLanguageManager() {
             return lang;
         }
 
         public static LangManager getLangManager() {
             return lang;
         }
 
         public API getAPI() {
             return api;
         }
 
         public Database getDB() {
             return db;
         }
 
         public Vault getVault() {
             return vault;
         }
 
         public Permissions getPermissions() {
             return perm;
         }
 	}
