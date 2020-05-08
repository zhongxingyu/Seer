 package au.com.addstar.marketsearch;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.maxgamer.QuickShop.QuickShop;
 import org.maxgamer.QuickShop.Shop.Shop;
 import org.maxgamer.QuickShop.Shop.ShopChunk;
 import org.maxgamer.QuickShop.Shop.ShopManager;
 import org.maxgamer.QuickShop.Shop.ShopType;
 
 import com.earth2me.essentials.Essentials;
 import com.earth2me.essentials.IEssentials;
 import com.worldcretornica.plotme.Plot;
 import com.worldcretornica.plotme.PlotManager;
 import com.worldcretornica.plotme.PlotMe;
 
 public class MarketSearch extends JavaPlugin {
 	public static MarketSearch instance;
 	
 	public static Economy econ = null;
 	public static Permission perms = null;
 	public static Chat chat = null;
 	public boolean VaultEnabled = false;
 	public boolean DebugEnabled = false;
 	public String MarketWorld = null;
 	public ShopManager QSSM = null;
 	
 	private static final Logger logger = Logger.getLogger("Minecraft");
 	public PluginDescriptionFile pdfFile = null;
 	public PluginManager pm = null;
 
 	private Boolean QSHooked = false;
 	private Boolean PlotMeHooked = false;
 
 	public IEssentials EssPlugin;
 	
 	static class ShopResult {
 		String PlotOwner;
 		String ShopOwner;
 		String ItemName;
 		Integer ItemID;
 		String Type;
 		Integer Stock;
 		Double Price;
 		Boolean Enchanted = false;
 	}
 	
 	@Override
 	public void onEnable(){
 		// Register necessary events
 		pdfFile = this.getDescription();
 		pm = this.getServer().getPluginManager();
 		QSSM = QuickShop.instance.getShopManager();
 		
 		MarketWorld = "market";
 		
 		getCommand("marketsearch").setExecutor(new CommandListener(this));
 		getCommand("marketsearch").setAliases(Arrays.asList("ms"));
 		
 		final Plugin ess = this.pm.getPlugin("Essentials");
 		if (ess != null) {
 			EssPlugin = (IEssentials) ess;
 			Log("Found Essentials - It will be used for item lookup");
 		} else {
 			EssPlugin = null;
 			Log("Essentials not found! Using native Bukkit item lookup");
 		}
 		
 		Log(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled");
 	}
 		
 	@Override
 	public void onDisable() {
 		// Nothing yet
 	}
 
 	/*
 	 * Detect/configure Vault
 	 */
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
 
 	public static class ShopResultSort {
 		public static Comparator<ShopResult> ByPrice = new Comparator<ShopResult>() {
 			@Override
 			public int compare(ShopResult shop1, ShopResult shop2) {
 				//Log("Compare: " + shop1.ShopOwner + " $" + shop1.Price + " / " + shop2.ShopOwner + " $" + shop2.Price);
 				if (shop1.Price.equals(shop2.Price)) {
 					//Log(" - Same!");
 					if (shop1.Stock > shop2.Stock) {
 						return -1;
 					} else {
 						return 1;
 					}
 				}
 				
 				//Log(" - Not same!");
 				if (shop1.Price > shop2.Price) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 		};
 
 		public static Comparator<ShopResult> ByStock = new Comparator<ShopResult>() {
 			@Override
 			public int compare(ShopResult shop1, ShopResult shop2) {
 				if (shop1.Stock == shop2.Stock) return 0;
 				
 				if (shop1.Stock > shop2.Stock) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 		};
 	}
 	
 	public class ShopResultSortByStock implements Comparator<ShopResult> {
 		@Override
 		public int compare(ShopResult shop1, ShopResult shop2) {
 			//Log("Compare: " + shop1.ShopOwner + " $" + shop1.Price + " / " + shop2.ShopOwner + " $" + shop2.Price);
 			if (shop1.Price.equals(shop2.Price)) {
 				//Log(" - Same!");
 				if (shop1.Stock > shop2.Stock) {
 					return -1;
 				} else {
 					return 1;
 				}
 			}
 			
 			//Log(" - Not same!");
 			if (shop1.Price > shop2.Price) {
 				return 1;
 			} else {
 				return -1;
 			}
 		}
 	}
 	
 	public List<ShopResult> SearchMarket(ItemStack SearchItem, ShopType SearchType) {
 		List<ShopResult> results = new ArrayList<ShopResult>(); 
 		for(Entry<ShopChunk, HashMap<Location, Shop>> chunks : QSSM.getShops(MarketWorld).entrySet()) {
 			
 		    for(Entry<Location, Shop> inChunk : chunks.getValue().entrySet()) {
 		    	Shop shop = inChunk.getValue();
 
 		    	if (shop.getItem().getTypeId() != SearchItem.getTypeId()) { continue; }	// Wrong item
 
 		    	// Only compare data/durability for items with no real durability (blocks, etc)
 		    	if (SearchItem.getType().getMaxDurability() == 0) {
 		    		if (shop.getItem().getDurability() != SearchItem.getDurability()) { continue; }
 		    	}
 
 		    	if (shop.getRemainingStock() == 0) { continue; }			// No stock
 		    	if (shop.getShopType() != SearchType) { continue; }			// Wrong shop type
 		    	
 		    	ShopResult result = new ShopResult();
 			    result.ShopOwner = shop.getOwner();
 			    result.ItemName = shop.getItem().getType().name();
 			    result.ItemID = shop.getItem().getTypeId();
 			    result.Stock = shop.getRemainingStock();
 			    result.Price = shop.getPrice();
 
 			    // Is this item enchanted?
 			    if (shop.getItem().getEnchantments().size() > 0) {
 			    	result.Enchanted = true;
 			    }
 
 			    Plot p = PlotManager.getPlotById(shop.getLocation());
 			    if (p != null) {
 			    	result.PlotOwner = p.owner;
			    	results.add(result);
 			    } else {
 			    	Warn("Unable to find plot! " + shop.getLocation().toString());
 			    }
 		    }
 		}
 
 		// Order results here
 		Collections.sort(results, ShopResultSort.ByPrice);
 		return results;
 	}
 
 	public List<ShopResult> getPlayerShops(String player) {
 		List<ShopResult> results = new ArrayList<ShopResult>();
 		for(Entry<ShopChunk, HashMap<Location, Shop>> chunks : QSSM.getShops(MarketWorld).entrySet()) {
 			
 		    for(Entry<Location, Shop> inChunk : chunks.getValue().entrySet()) {
 		    	Shop shop = inChunk.getValue();
 		    	if (shop.getOwner().equals(player)) {
 			    	ShopResult result = new ShopResult();
 				    result.ShopOwner = shop.getOwner();
 				    result.ItemName = shop.getItem().getType().name();
 				    result.ItemID = shop.getItem().getTypeId();
 				    result.Stock = shop.getRemainingStock();
 				    result.Price = shop.getPrice();
 	
 				    Plot p = PlotManager.getPlotById(shop.getLocation());
 				    if (p != null) {
 				    	result.PlotOwner = p.owner;
				    	results.add(result);
 				    } else {
 				    	Warn("Unable to find plot! " + shop.getLocation().toString());
 				    }
 		    	}
 		    }
 		}
 		return results;
 	}
 
 	public void Log(String data) {
 		logger.info(pdfFile.getName() + " " + data);
 	}
 
 	public void Warn(String data) {
 		logger.warning(pdfFile.getName() + " " + data);
 	}
 	
 	public void Debug(String data) {
 		if (DebugEnabled) {
 			logger.info(pdfFile.getName() + " " + data);
 		}
 	}
 
 	/*
 	 * Check if the player has the specified permission
 	 */
 	public boolean HasPermission(Player player, String perm) {
 		if (player instanceof Player) {
 			// Real player
 			if (player.hasPermission(perm)) {
 				return true;
 			}
 		} else {
 			// Console has permissions for everything
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * Check required permission and send error response to player if not allowed
 	 */
 	public boolean RequirePermission(Player player, String perm) {
 		if (!HasPermission(player, perm)) {
 			if (player instanceof Player) {
 				player.sendMessage(ChatColor.RED + "Sorry, you do not have permission for this command.");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/*
 	 * Check if player is online
 	 */
 	public boolean IsPlayerOnline(String player) {
 		if (player == null) { return false; }
 		if (player == "") { return false; }
 		if (this.getServer().getPlayer(player) != null) {
 			// Found player.. they must be online!
 			return true;
 		}
 		return false;
 	}
 
 	public Material GetMaterial(String name) {
 		Material mat = Material.matchMaterial(name);
 		if (mat != null) {
 			return mat;
 		}
 		return null;
 	}
 	
 	public void SendHelp(CommandSender sender) {
 		sender.sendMessage(ChatColor.GREEN + "Available MarketSearch commands:");
 
 		if (!(sender instanceof Player) || (HasPermission((Player) sender, "marketsearch.find"))) {
 			sender.sendMessage(ChatColor.AQUA + "/ms find <item> :" + ChatColor.WHITE + " Search for an item for sale in the market");
 		}
 		
 		if (!(sender instanceof Player) || (HasPermission((Player) sender, "marketsearch.stock"))) {
 			sender.sendMessage(ChatColor.AQUA + "/ms stock :" + ChatColor.WHITE + " Get a summary of your stock levels");
 			sender.sendMessage(ChatColor.AQUA + "/ms stock empty :" + ChatColor.WHITE + " List your shops with NO stock");
 			sender.sendMessage(ChatColor.AQUA + "/ms stock lowest :" + ChatColor.WHITE + " List your shops with lowest stock");
 		}
 		if (!(sender instanceof Player) || (HasPermission((Player) sender, "marketsearch.stock.others"))) {
 			sender.sendMessage(ChatColor.AQUA + "/ms pstock <player> :" + ChatColor.WHITE + " Get another player's stock levels");
 			sender.sendMessage(ChatColor.AQUA + "/ms pstock <player> empty :" + ChatColor.WHITE + " Other player's shops with NO stock");
 			sender.sendMessage(ChatColor.AQUA + "/ms pstock <player> lowest :" + ChatColor.WHITE + " Other player's shops with lowest stock");
 		}
 	}
 	
 }
