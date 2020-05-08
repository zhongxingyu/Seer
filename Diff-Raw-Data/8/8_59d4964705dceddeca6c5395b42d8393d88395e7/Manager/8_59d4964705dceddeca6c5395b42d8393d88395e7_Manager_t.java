 package com.timvisee.manager;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.timvisee.manager.economymanager.EconomyManager;
 import com.timvisee.manager.permissionsmanager.PermissionsManager;
 
 public class Manager extends JavaPlugin {
 	private static final Logger log = Logger.getLogger("Minecraft");
 
 	// Permissions and Economy manager
 	private PermissionsManager pm;
 	private EconomyManager em;
 	
 	public void onEnable() {
 		// Setup the managers
 		setupPermissionsManager();
 		setupEconomyManager();
 		
 		// Plugin started
 		
 		log.info("[Manager] Manager Started");
 	}
 
 	public void onDisable() {
 		log.info("[Manager] Manager Disabled");
 	}
 	
 	/**
 	 * Setup the economy manager
 	 */
 	public void setupEconomyManager() {
 		// Setup the economy manager
 		this.em = new EconomyManager(this.getServer(), this);
 		this.em.setup();
 	}
 	
 	/**
 	 * Get the economy manager
 	 * @return economy manager
 	 */
 	public EconomyManager getEconomyManager() {
 		return this.em;
 	}
 	
 	/**
 	 * Setup the permissions manager
 	 */
 	public void setupPermissionsManager() {
 		// Setup the permissions manager
 		this.pm = new PermissionsManager(this.getServer(), this);
 		this.pm.setup();
 	}
 	
 	/**
 	 * Get the permissions manager
 	 * @return permissions manager
 	 */
 	public PermissionsManager getPermissionsManager() {
 		return this.pm;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// Commands to test whether or not Manager is able to properly communicate with the permission/economy system currently running
 		if (sender instanceof Player){
 			if (cmd.getName().equalsIgnoreCase("manager")){
 				if (args.length>0){
 					if (args[0].equalsIgnoreCase("node")){
 						if (args.length>1){
 							if (this.pm.hasPermission((Player) sender, args[1]))
 								sender.sendMessage("Passed");
 							else
 								sender.sendMessage("Failed");
 							return true;
 						} else {
 							sender.sendMessage("No permission node provided.");
 							return false;
 						}
 					} else if (args[0].equalsIgnoreCase("groups")){
 						for(Object o: this.pm.getGroups((Player) sender)){
 							sender.sendMessage(o.toString());
 						}
 						return true;
 					} else if (args[0].equalsIgnoreCase("addmoney")){
						if (args.length>1){
 							this.em.depositMoney(sender.getName(), Double.parseDouble(args[1]));
 							return true;
 						} else
 							return false;
 					} else if (args[0].equalsIgnoreCase("submoney")){
						if (args.length>1){
 							this.em.withdrawMoney(sender.getName(), Double.parseDouble(args[1]));
 							return true;
 						} else
 							return false;
 					} else if (args[0].equalsIgnoreCase("getmoney")){
						if (args.length>1){
 							Double balance = this.em.getBalance(args[1]);
 							sender.sendMessage("Balance: "+Double.toString(balance)+" "+this.em.getCurrencyName(balance));
 							return true;
 						} else {
 							Double balance = this.em.getBalance(sender.getName());
 							sender.sendMessage("Balance: "+Double.toString(balance)+" "+this.em.getCurrencyName(balance));
 							return true;
 						}
 					}
 				}
 			} else return false;
 		}
 		return false;
 	}
 }
