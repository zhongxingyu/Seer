 package me.autoit4you.bankaccount;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import me.autoit4you.bankaccount.commands.*;
 
 import me.autoit4you.bankaccount.exceptions.*;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 import org.mcstats.MetricsLite;
 
 	public class BankAccount extends JavaPlugin {
 		//variables
 		public static boolean debug = false;
 	    public static final Logger log = Logger.getLogger("Minecraft");
 	    public static Database db = null;
 	    public static Vault vault = null;
 	    public static Permissions perm = null;
 		private BukkitTask sinterest;
 
 	    @Override
 	    public void onEnable() {
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
 	    	//Register usage of a permission plugin
 	    	perm = new Permissions();
 	    	if(!perm.setupPermissions()){
 	    		log.severe(String.format("Cannot setup permissions system. Disabling..."));
 	    		getServer().getPluginManager().disablePlugin(this);
 	    		return;
 	    	}
 	    	//Enable metrics
 	    	try{
 	    		MetricsLite metrics = new MetricsLite(this);
 	    		metrics.start();
 	    	}catch(IOException e){
 	    		log.warning("Could not enable metrics!");
 	    	}
 	    	//Setting up listeners
 	    	if(getConfig().getBoolean("interest.enabled")){
 	    		this.sinterest = getServer().getScheduler().runTaskTimer(this, new Interest(getConfig().getDouble("interest.percentage"), getConfig().getBoolean("interest.ownerMustOnline")), 20, 20 * 60 * getConfig().getInt("interest.minutes"));
 	    	}
 	    }
 	    
 	    @Override
 	    public void onDisable() {
 	    	if(this.sinterest != null)
 	    		this.sinterest.cancel();
 	    }
 	    
 	    @Override
 	    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 	    	try{
 	    		if(!(sender instanceof Player)){
 	    			sender.sendMessage("You must be a player to use BankAccount!");
 	    			return false;
 	    		}else if(cmd.getName().equalsIgnoreCase("account")){
 	    			if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
 	    				new CommandAccountHelp().run(sender, args);
 	    			} else if(args[0].equalsIgnoreCase("open")) {
 	    				new CommandAccountOpen().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("close")){
 	    				new CommandAccountClose().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("list")){
 	    				new CommandAccountList().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("withdraw")){
 	    				new CommandAccountWithdraw().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("deposit")){
 	    				new CommandAccountDeposit().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("balance")){
 	    				new CommandAccountBalance().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("transfer")){
 	    				new CommandAccountTransfer().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("adduser")) {
 	    				new CommandAccountAdduser().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("removeuser")) {
 	    				new CommandAccountRemoveuser().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("addadmin")) {
	    				new CommandAccountRemoveuser().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("removeadmin")) {
	    				new CommandAccountRemoveuser().run(sender, args);
 	    			}else if(args[0].equalsIgnoreCase("transferownership")) {
	    				new CommandAccountRemoveuser().run(sender, args);
 	    			}else{
 	    				throw new BAArgumentException("That command is not recognized. Please type /account help for help.");
 	    			}
 	    			return true;
 	    		}
 	    	} catch(BankAccountException banke) {
 	    		banke.print(sender, args);
 	    		return true;
 	    	}
 				return false;
 	    }
 	    
 	    /*@Override
 	    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
 			return null;
 	    }*/
 	}
