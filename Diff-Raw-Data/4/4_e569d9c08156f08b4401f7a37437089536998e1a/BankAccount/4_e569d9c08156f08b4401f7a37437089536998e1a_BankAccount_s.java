 package de.autoit4you.bankaccount;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import de.autoit4you.bankaccount.api.API;
 import de.autoit4you.bankaccount.commands.*;
 
 import de.autoit4you.bankaccount.exceptions.*;
 
 import de.autoit4you.bankaccount.mcstats.Metrics;
 import de.autoit4you.bankaccount.tasks.AccountsUpdate;
 import de.autoit4you.bankaccount.tasks.Interest;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
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
             //initialize api
             api = new API(this);
 	    	//Register usage of a permission plugin
 	    	perm = new Permissions();
 	    	if(!perm.setupPermissions()){
 	    		log.severe(String.format("Cannot setup permissions system. Disabling..."));
 	    		getServer().getPluginManager().disablePlugin(this);
 	    		return;
 	    	}
 	    	//Enable metrics
 	    	try{
 	    		Metrics metrics = new Metrics(this);
                 metrics.start();
 	    	}catch(IOException e){
 	    		log.warning("Could not enable metrics!");
 	    	}
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
 	    	this.getServer().getScheduler().cancelTasks(this);
 
             api.saveData();
 	    }
 	    
 	    @Override
 	    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 	    	if(!(sender instanceof Player)){
 	    		sender.sendMessage("You must be a player to use BankAccount!");
 	    		return false;
 	    	}else if(cmd.getName().equalsIgnoreCase("account")){
 	    		try {
                     if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
 	    			    new CommandAccountHelp().run(sender, args, this);
 	    	    	} else if(args[0].equalsIgnoreCase("open")) {
 	    		    	new CommandAccountOpen().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("close")){
 	    		    	new CommandAccountClose().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("list")){
 	    		    	new CommandAccountList().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("withdraw")){
 	    		    	new CommandAccountWithdraw().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("deposit")){
 	    		    	new CommandAccountDeposit().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("balance")){
 	    		    	new CommandAccountBalance().run(sender, args, this);
	    		    }else if(args[0].equalsIgnoreCase("transfer")){
 	    		    	new CommandAccountTransfer().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("adduser")) {
 	    		    	new CommandAccountAdduser().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("removeuser")) {
 	    		    	new CommandAccountRemoveuser().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("addadmin")) {
 	    		    	new CommandAccountAddadmin().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("removeadmin")) {
 	    		    	new CommandAccountRemoveadmin().run(sender, args, this);
 	    		    }else if(args[0].equalsIgnoreCase("transferownership")) {
 	    		    	new CommandAccountTransferownership().run(sender, args, this);
                     } else {
                         sender.sendMessage(ChatColor.RED + "That command is not recognized. Please type /account help for help.");
                     }
                 } catch (BAArgumentException argument) {
                     sender.sendMessage(ChatColor.RED + "Please check your arguments!");
                 } catch (CommandPermissionException perm) {
                     sender.sendMessage(ChatColor.RED + "You do not have permissions!");
                 }
                 return true;
             }
 	    	return false;
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
