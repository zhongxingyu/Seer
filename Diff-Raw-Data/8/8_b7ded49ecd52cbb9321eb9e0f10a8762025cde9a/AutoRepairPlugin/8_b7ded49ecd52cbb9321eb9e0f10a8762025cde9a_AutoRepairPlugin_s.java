 package com.mrockey28.bukkit.ItemRepair;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.Plugin;
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 
 
 /**
  * testing for Bukkit
  *
  * @author lostaris, mrockey28
  */
 public class AutoRepairPlugin extends JavaPlugin {
 	private final AutoRepairBlockListener blockListener = new AutoRepairBlockListener(this);
 	private static HashMap<String, Integer> durabilityCosts;
 	private static HashMap<String, ArrayList<ItemStack>> repairRecipies;
 	private static HashMap<String, Integer> iConCosts;
 	private HashMap<String, String> settings;
 	private static boolean useiConomy;
 	private static String isiCon; //are we using icon, both or not at all
 	private static boolean autoRepair;
 	private static boolean repairCosts;
 	public static boolean isPermissions = false;
 	
 	public static Economy econ = null;
 	public static String item_rounding = "flat";
 	public static String econ_fractioning = "off";
 	public static final Logger log = Logger.getLogger("Minecraft");
 	//public AutoRepairSupport support = new AutoRepairSupport(this);
 	public Repair repair = new Repair(this);
 
 
 	
 	public enum operationType {
 		QUERY,
 		WARN,
 		MANUAL_REPAIR,
 		AUTO_REPAIR
 	}
 	
 	//public AutoRepairPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
 	//	      super(pluginLoader, instance, desc, folder, plugin, cLoader);
 		// TODO: Place any custom initialisation code here
 
 		// NOTE: Event registration should be done in onEnable not here as all events are
 		// unregistered when a plugin is disabled
 	//}
 
 	public void onEnable() {
 		//  Place any custom enable code here including the registration of any events
 
 		// Register our events
 		getServer().getPluginManager().registerEvents(blockListener, this);
 
 		// EXAMPLE: Custom code, here we just output some info so we can check all is well
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
 		refreshConfig();
 		
 		if (!setupEconomy() ) {
 	         log.info(String.format("[%s] - Economy not linked, Vault not found", getDescription().getName()));
 	         AutoRepairPlugin.useiConomy = false;
 		 }
 		else
 		{
 			AutoRepairPlugin.useiConomy = true;
 		}
 		
 	}
 	 
 
 
 	private boolean setupEconomy() {
 	     if (getServer().getPluginManager().getPlugin("Vault") == null) {
 	         return false;
 	     }
 	     RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 	     if (rsp == null) {
 	         return false;
 	     }
 	     
 	     
 	     econ = rsp.getProvider();
 	     return econ != null;
 	}	
 	
 	public void onDisable() {
 		//  Place any custom disable code here
 
 		// NOTE: All registered events are automatically unregistered when a plugin is disabled
 
 		// EXAMPLE: Custom code, here we just output some info so we can check all is well
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled" );
 	}
 
 	@Override
 	public boolean onCommand(org.bukkit.command.CommandSender sender,
 			org.bukkit.command.Command command, String commandLabel, String[] args) {
 		Player player = null;
 
 		if(sender instanceof Player) {
 			player = (Player) sender;
 		}
 		PlayerInventory inven = player.getInventory();
 		String[] split = args;
 		String commandName = command.getName().toLowerCase();
 		AutoRepairSupport support = new AutoRepairSupport(this, player);
 
 		if (commandName.equals("repair")) {
 			if (!isAllowed(player, "access")) {
 				return true;
 			}
 
 			ItemStack tool;
 			int itemSlot = 0;
 			if (split.length == 0) {
 				if (isAllowed(player, "repair")) {
 					tool = player.getItemInHand();
 					repair.manualRepair(tool, inven.getHeldItemSlot() );
 				} else {
 					player.sendMessage("cYou dont have permission to do the repair command.");
 				}
 			} else if (split.length == 1) {
 				try {
 					char repairList = split[0].charAt(0);					
 					if (repairList == '?') {
 						support.toolReq(player.getItemInHand(), player.getInventory().getHeldItemSlot());
 					} else if (split[0].equalsIgnoreCase("dmg")) {						
 						support.durabilityLeft(inven.getItem(inven.getHeldItemSlot()));
 					} else if (split[0].equalsIgnoreCase("arm")) {						
 						repair.repairArmour();
 					} else if(split[0].equalsIgnoreCase("reload")) {
 						if (isAllowed(player, "reload")){ 
 							refreshConfig();
 							player.sendMessage("3Re-loaded AutoRepair config files");
 						} else {
 							player.sendMessage("cYou dont have permission to do the reload command.");
 						}
 					}else {
 						if (isAllowed(player, "repair")) {
 							itemSlot = Integer.parseInt(split[0]);
 							if (itemSlot >0 && itemSlot <=9) {
 								tool = inven.getItem(itemSlot -1);
 								repair.manualRepair(tool, itemSlot -1);
 							} else {
 								player.sendMessage("6ERROR: Slot must be a quick bar slot between 1 and 9");
 							}	
 						} else {
 							player.sendMessage("cYou dont have permission to do the repair command.");
 						}
 					}
 				} catch (Exception e) {
 					return false;
 				}
 			}else if (split.length == 2 && split[0].equalsIgnoreCase("arm") && split[1].length() ==1) {
 				if (isAllowed(player, "info")) { 
 					support.repArmourInfo(split[1]);
 				} else {
 					player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 				}
 			}else if ((split.length == 2 && split[1].length() ==1)) {
 				try {
 					char getRecipe = split[1].charAt(0);
 					itemSlot = Integer.parseInt(split[0]);
 					if (getRecipe == '?' && itemSlot >0 && itemSlot <=9) {
 						if (isAllowed(player, "info")) {
 							support.toolReq(inven.getItem(itemSlot-1), itemSlot-1 );
 						} else {
 							player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 						}
 					}
 				} catch (Exception e) {
 					return false;
 				} 
 			} else if (split.length == 2 && split[1].equalsIgnoreCase("dmg")) {
 				try {
 					if (isAllowed(player, "info")) {
 						itemSlot = Integer.parseInt(split[0]);
 						if (itemSlot >0 && itemSlot <=9) {
 							support.durabilityLeft(inven.getItem(itemSlot -1));
 						} else {
 							player.sendMessage("6ERROR: Slot must be a quick bar slot between 1 and 9");
 						}
 					} else {
 						player.sendMessage("cYou dont have permission to do the ? or dmg commands.");
 					}
 				} catch (Exception e) {
 					return false;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isOpAllowed (Player player, operationType op)
 	{
 		switch(op)
 		{
 			case QUERY:
 				if (!isAllowed(player, "info"))
 				{
 					player.sendMessage("cYou dont have permission to do repair query commands.");
 					return false;
 				}
 				else return true;
 			case WARN:
 				if (!isAllowed(player, "warn")) return false;
 				else return true;
 			case MANUAL_REPAIR:
 				if (!isAllowed(player, "repair"))
 				{
 					
 					player.sendMessage("cYou dont have permission to do the repair command.");
 					return false;
 				}
 				else return true;
 			case AUTO_REPAIR:
 				if (!isAllowed(player, "auto")) return false;
 				else return true;	
 		}
 		return false;
 	}
 	
 	public static boolean isAllowed(Player player, String com) {		
 		boolean allowed = false;
 		if(isPermissions == true) {
 			if(player.hasPermission("AutoRepair.access") && player.hasPermission("AutoRepair."+com)) {
				log.info("Player has " +com+ " permission");
 				allowed = true;
 			} else {
 				allowed = false;
 			}
 		}else if(isPermissions == false && !com.equalsIgnoreCase("none")) {
 			allowed = true;
 		}
 		
 		return allowed;
 	}
 
 	public HashMap<String, String> readConfig() {
 		String fileName = "plugins/AutoRepair/Config.properties";
 		HashMap<String, String> settings = new HashMap<String, String>();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(fileName));
 			String line;
 			String setting = null;
 			String value = null;
 			while ((line = reader.readLine()) != null) {
 				if ((line.trim().length() == 0) || 
 						(line.charAt(0) == '#')) {
 					continue;
 				}
 				int keyPosition = line.indexOf('=');
 				setting = line.substring(0, keyPosition).trim();
 				value = line.substring(keyPosition +1, line.length());
 				settings.put(setting, value);
 			}			
 		}catch (Exception e) {
 			log.info("Error reading AutoRepair config");
 		}		
 		return settings;
 
 	}
 
 	public void refreshConfig() {
 		try {
 			readProperties();
 			setSettings(readConfig());
 			if (getSettings().containsKey("auto-repair")) {
 				if (getSettings().get("auto-repair").equals("false")) {
 					setAutoRepair(false);
 				}
 				//If somebody fucks up, the default is true
 				else
 				{
 					setAutoRepair(true);
 				}
 			}
 			if (getSettings().containsKey("repair-costs")) {
 				if (getSettings().get("repair-costs").equals("false")) {
 					setRepairCosts(false);
 				}
 				//If somebody fucks up, the default is true
 				else
 				{
 					setRepairCosts(true);
 				}
 			}
 			if (getSettings().containsKey("economy")) {
 				if (getSettings().get("economy").equals("true")) {
 					setIsICon("true");
 				} else if (getSettings().get("economy").equals("both")) {
 					setIsICon("both");
 				}
 				//If somebody fucks up, the default is false
 				else{
 					setIsICon("false");
 				}
 			}
 			if (getSettings().containsKey("permissions")) {
 				if (getSettings().get("permissions").equals("true")) {
 					AutoRepairPlugin.isPermissions = true;
 				}
 				//If somebody fucks up, the default is false
 				else
 				{
 					AutoRepairPlugin.isPermissions = false;
 				}
 			}
 			if (getSettings().containsKey("item_rounding")) {
 				if (getSettings().get("item_rounding").equals("min")) {
 					item_rounding = "min";
 				} if (getSettings().get("item_rounding").equals("round")) {
 					item_rounding = "round";
 				}
 				//If somebody fucks up, the default is "flat"
 				else {
 					item_rounding = "flat";
 				} 
 			}
 			if (getSettings().containsKey("econ_fractioning")) {
 				if (getSettings().get("econ_fractioning").equals("on")) {
 					econ_fractioning = "on";
 				} 
 				//If somebody fucks up, the default is "off"
 				else {
 					econ_fractioning = "off";
 				}	
 			}
 		} catch (Exception e){
 			log.info("Error reading AutoRepair config files");
 		}
 	}
 
 	public static void readProperties() throws Exception {
 		HashMap<String, ArrayList<ItemStack> > map = new HashMap<String, ArrayList<ItemStack> >();
 		HashMap<String, Integer> iConomy = new HashMap<String, Integer>();
 		HashMap<String, Integer> durab = new HashMap<String, Integer>();
 		String fileName = "plugins/AutoRepair/RepairCosts.properties";
 		BufferedReader reader = new BufferedReader(new FileReader(fileName));
 		String line;
 		while ((line = reader.readLine()) != null) {
 			if ((line.trim().length() == 0) || 
 					(line.charAt(0) == '#')) {
 				continue;
 			}
 			int keyPosition = line. indexOf('=');
 			String[] reqs;
 			ArrayList<ItemStack> itemReqs = new ArrayList<ItemStack>();
 			String item = line.substring(0, keyPosition).trim();
 			String recipiesString;
 			if (line.indexOf(' ') != -1) {
 				recipiesString = line.substring(keyPosition+1, line.indexOf(' '));
 				try {
 					int amount = Integer.parseInt(line.substring(line.lastIndexOf("=") +1, line.length()));
 					iConomy.put(item, amount);
 				} catch (Exception e) {
 				}
 			} else {
 				recipiesString = line.substring(keyPosition+1, line.length()).trim();
 			}
 
 			String[] allReqs = recipiesString.split(":");
 			int durability = 0;
 			for (int i =0; i < allReqs.length; i++) {
 				if (i==0)
 				{
 					durability = Integer.parseInt(allReqs[0]);
 				}
 				else
 				{
 					reqs = allReqs[i].split(",");
 					ItemStack currItem = new ItemStack(Integer.parseInt(reqs[0]), Integer.parseInt(reqs[1]));
 					itemReqs.add(currItem);
 				}
 			}
 			map.put(item, itemReqs);
 			durab.put(item, durability);
 		}
 		reader.close();
 		setiConCosts(iConomy);
 		setRepairRecipies(map);
 		setDurabilityCosts(durab);
 	}
 
 	public void setIsICon(String b) {
 		AutoRepairPlugin.isiCon = b;		
 	}
 
 	public static String getiSICon() {
 		return AutoRepairPlugin.isiCon;
 	}
 
 	public static boolean getUseIcon() {
 		return AutoRepairPlugin.useiConomy;
 	}
 
 	public static void setRepairRecipies(HashMap<String, ArrayList<ItemStack>> hashMap) {
 		AutoRepairPlugin.repairRecipies = hashMap;
 	}
 
 	public static HashMap<String, ArrayList<ItemStack>> getRepairRecipies() {
 		return repairRecipies;
 	}
 
 	public void setSettings(HashMap<String, String> settings) {
 		this.settings = settings;
 	}
 
 	public HashMap<String, String> getSettings() {
 		return settings;
 	}
 
 	public void setAutoRepair(boolean autoRepair) {
 		AutoRepairPlugin.autoRepair = autoRepair;
 	}
 
 	public static boolean isAutoRepair() {
 		return autoRepair;
 	}
 
 	public void setRepairCosts(boolean repairCosts) {
 		AutoRepairPlugin.repairCosts = repairCosts;
 	}
 
 	public static boolean isRepairCosts() {
 		return repairCosts;
 	}
 
 	public static void setiConCosts(HashMap<String, Integer> iConomy) {
 		AutoRepairPlugin.iConCosts = iConomy;
 	}
 
 	public static HashMap<String, Integer> getiConCosts() {
 		return iConCosts;
 	}
 	
 	public static void setDurabilityCosts(HashMap<String, Integer> durab) {
 		AutoRepairPlugin.durabilityCosts = durab;
 	}
 	
 	public static HashMap<String, Integer> getDurabilityCosts() {
 		return durabilityCosts;
 	}
 }
