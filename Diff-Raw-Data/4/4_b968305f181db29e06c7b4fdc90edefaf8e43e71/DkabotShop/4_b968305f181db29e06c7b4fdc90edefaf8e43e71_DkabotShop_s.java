 package com.dkabot.DkabotShop;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DkabotShop extends JavaPlugin {
 	public static DkabotShop plugin;
 	Logger log = Logger.getLogger("Minecraft");
 	private Sellers Sell;
 	private Buyers Buy;
 	private History Hist;
 	public static Vault vault = null;
 	public Economy economy = null;
 	
 	@Override
 	public void onEnable() {
 		//Vault dependency checker
         Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
         if(x != null & x instanceof Vault) {
             vault = (Vault) x;
             log.info(String.format("[%s] Hooked %s %s", getDescription().getName(), vault.getDescription().getName(), vault.getDescription().getVersion()));
         } else {
             log.severe(String.format("Vault dependency not found! Disabling..."));
             getPluginLoader().disablePlugin(this);
            return;
         }
 		if(!setupEconomy()) {
 			log.severe("No economy system found. You need one to use this!");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
         //Configuration Validator
 		List<String> result = validateConfig();
 		if(result == null) getServer().getPluginManager().disablePlugin(this);
 		if(!result.isEmpty()) {
 			log.severe("[DkabotShop] Error(s) in configuration!");
 			for(int i = 0; i < result.size();) {
 				String error = result.get(i).split(",")[0];
 				String area = result.get(i).split(",")[1];
 				log.severe("[DkabotShop] Error on " + error + " in the " + area + " section!");
 				i++;
 			}
 			log.severe("[DkabotShop] Disabling due to above errors...");
 			getServer().getPluginManager().disablePlugin(this);
 		}
         //The rest of onEnable()
 		setupDatabase();
 		Sell = new Sellers(this);
 		Buy = new Buyers(this);
 		Hist = new History(this);
 		getCommand("buy").setExecutor(Buy);
 		getCommand("stock").setExecutor(Buy);
 		getCommand("sell").setExecutor(Sell);
 		getCommand("cancel").setExecutor(Sell);
 		getCommand("price").setExecutor(Sell);
 		getCommand("sales").setExecutor(Hist);
 		defaultConfig();
 		log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is now enabled,");
 	}
 	
 	@Override
 	public void onDisable() {
 		log.info(getDescription().getName() + " is now disabled.");
 	}
 	    
 	boolean isInt(String s) {
 		try {
 			Integer.parseInt(s);
 				return true;
 		    }
 		    catch(NumberFormatException nfe) {
 		    	return false;
 		    }
 	    }
 	    
 	    public void setupDatabase() {
 	        try {
 	            getDatabase().find(DB_ForSale.class).findRowCount();
 	            getDatabase().find(DB_History.class).findRowCount();
 	        } catch (PersistenceException ex) {
 	            log.info("Installing database due to first time usage");
 	            installDDL();
 	        }
 	    }
 	    
 	    @Override
 	    public ArrayList<Class<?>> getDatabaseClasses() {
 	        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
 	        list.add(DB_ForSale.class);
 	        list.add(DB_History.class);
 	        return list;
 	    }
 		
 		boolean isDecimal(double v) {
 	      return (Math.floor(v) != v);
 	      //If true, decimal, else whole number.
 		}
 		
 		private Boolean setupEconomy() {
 	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 	        if (economyProvider != null) {
 	            economy = economyProvider.getProvider();
 	        }
 	        return (economy != null);
 	    }
 		
 		Material getMaterial(String materialString, boolean allowHand, Player player) {
 			Material material = null;
 			for(int i = 0; i < (getConfig().getStringList("ItemAlias")).size();) {
 				List<String> itemAlias = getConfig().getStringList("ItemAlias");
 				if(materialString.equalsIgnoreCase(itemAlias.get(i).split(",")[0])) {
 					String actualMaterial = itemAlias.get(i).split(",")[1];
 					//In case of an item ID
 					if(isInt(actualMaterial)) material = Material.getMaterial(Integer.parseInt(actualMaterial));
 					//Must be a material name
 					else material = Material.getMaterial(actualMaterial.toUpperCase());
 					//Should be an actual material
 					return material;
 				}
 				i++;
 			}
 			if(materialString.equalsIgnoreCase("hand")) {
 				if(allowHand) {
 					material = player.getItemInHand().getType();
 					//Could be null or a material... either.
 				}
 				return material;
 			}
 			else if(Material.getMaterial(materialString.toUpperCase()) == null) {
 				if(isInt(materialString)) {
 					material = Material.getMaterial(Integer.parseInt(materialString));
 				}
 				//Could return null or not.
 				return material;
 			}
 			//Actually does something.
 			else material = Material.getMaterial(materialString.toUpperCase());
 			return material;
 		}
 		
 		Double getMoney(String s) {
 			try {
 				Double d = Double.parseDouble(s);
 				DecimalFormat twoDForm = new DecimalFormat("#.00");
 				return Double.valueOf(twoDForm.format(d));
 			}
 			catch(NumberFormatException e) {
 				return null;
 			}
 		}
 		
 		private void defaultConfig() {
 			//Create and set arrays
 			List<String> blacklistAlways = new ArrayList<String>();
 			List<String> blacklistIfDamaged = new ArrayList<String>();
 			List<String> itemAlias = new ArrayList<String>();
 			blacklistAlways.add(Material.AIR.toString());
 			itemAlias.add("nothing,AIR");
 			//Add default config and save
 			getConfig().addDefault("Blacklist.Always", blacklistAlways);
 			getConfig().addDefault("Blacklist.IfDamaged", blacklistIfDamaged);
 			getConfig().addDefault("ItemAlias", itemAlias);
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}
 		
 		private List<String> validateConfig() {
 			try {
 				List<String> itemsWrong = new ArrayList<String>();
 				for(int i = 0; i < (getConfig().getStringList("ItemAlias")).size();) {
 					List<String> itemAlias = getConfig().getStringList("ItemAlias");
 					if(itemAlias.get(i).split(",").length != 2) itemsWrong.add("formatting,ItemAlias");
 					else {
 						String materialString = itemAlias.get(i).split(",")[1];
						if(isInt(materialString)) if(Material.getMaterial(Integer.parseInt(materialString)) == null) itemsWrong.add("itemname,ItemAlias");
 						else if(Material.getMaterial(materialString) == null) itemsWrong.add("itemname,ItemAlias");
 					}
 					i++;
 				}
 				for(int i = 0; i < (getConfig().getStringList("Blacklist.Always").size());) {
 					if(Material.getMaterial(getConfig().getStringList("Blacklist.Always").get(i).toUpperCase()) == null) itemsWrong.add(getConfig().getStringList("Blacklist.Always").get(i) + ",Blacklist Always");
 					i++;
 				}
 				for(int i = 0; i < (getConfig().getStringList("Blacklist.IfDamaged").size());) {
 					if(Material.getMaterial(getConfig().getStringList("Blacklist.IfDamaged").get(i).toUpperCase()) == null) itemsWrong.add(getConfig().getStringList("Blacklist.IfDamaged").get(i) + ",Blacklist IfDamaged");
 					i++;
 				}
 				return itemsWrong;
 			}
 			catch (Exception e) {
 				log.severe("[DkabotShop] Exception occurred while processing the configuration! Printing stacktrace and disabling...");
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		int illegalItem(Material material) {
 			for(int i = 0; i < getConfig().getStringList("Blacklist.Always").size();) {
 				if(Material.getMaterial(getConfig().getStringList("Blacklist.Always").get(i).toUpperCase()) == material) return 1;
 				i++;
 			}		
 			for(int i = 0; i < getConfig().getStringList("Blacklist.IfDamaged").size();) {
 				if(Material.getMaterial(getConfig().getStringList("Blacklist.IfDamaged").get(i).toUpperCase()) == material) return 2;
 				i++;
 			}
 			return 0;
 		}
 
 		Integer itemUsedAmount(Material material, Player player) {
 			try {
 				if(!player.getInventory().contains(material)) return 0;
 				HashMap<Integer, ? extends ItemStack> instancesOfItem = player.getInventory().all(material);
 				Integer itemUsedCount = 0;
 				for(int i = 0; i < instancesOfItem.size();) {
 					ItemStack instanceOfItem = instancesOfItem.get(i);
 					if(instanceOfItem.getDurability() != 0) itemUsedCount++;
 					i++;
 				}
 				return itemUsedCount;
 			}
 			catch (Exception e) {
 				log.severe("An item in the used item blacklist is not compatible with the methods this uses to get durability!");
 				return null;
 			}
 		}
 		
 		Integer giveItem(ItemStack item, Player player) {
 			Integer fullItemStacks = item.getAmount() / item.getMaxStackSize();
 			Integer fullItemStacksRemaining = fullItemStacks;
 			Integer nonFullItemStack = item.getAmount() % item.getMaxStackSize();
 			Integer amountNotReturned = 0;
 			Integer notReturnedAsInt = 0;
 			for(int i = 0; i < fullItemStacks;) {
 				HashMap<Integer, ItemStack> notReturned = player.getInventory().addItem(new ItemStack(item.getType(), item.getMaxStackSize()));
 				fullItemStacksRemaining--;
 				if(notReturned.isEmpty()) i++;
 				else {
 					for(int j = 0; j < notReturned.size();) {
 						notReturnedAsInt = notReturnedAsInt + notReturned.get(j).getAmount();
 						j++;
 					}
 					break;
 				}
 			}
 			if(notReturnedAsInt != 0) notReturnedAsInt = notReturnedAsInt + nonFullItemStack;
 			else {
 				HashMap<Integer, ItemStack> notReturned = player.getInventory().addItem(new ItemStack(item.getType(), nonFullItemStack));
 				for(int i = 0; i < notReturned.size();) {
 					notReturnedAsInt = notReturnedAsInt + notReturned.get(i).getAmount();
 					i++;
 				}
 			}
 			amountNotReturned = amountNotReturned + (fullItemStacksRemaining * item.getMaxStackSize()) + notReturnedAsInt;
 			return amountNotReturned;		
 		}
 }
