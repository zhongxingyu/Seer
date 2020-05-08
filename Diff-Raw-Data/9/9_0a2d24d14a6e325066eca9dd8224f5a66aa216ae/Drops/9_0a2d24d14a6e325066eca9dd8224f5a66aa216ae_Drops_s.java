 package muCkk.DeathAndRebirth.ghost;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import muCkk.DeathAndRebirth.DAR;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Drops {
 
 	private static final Logger log = Logger.getLogger("Minecraft");
 	private FileConfiguration customConfig = null;
 	private File dropsFile;
 	private String dir;
 	private DAR plugin;
 	private Blacklist blacklist;  //selfres
 	
 	public Drops(DAR instance, String dir2) {
 		this.plugin = instance;
 		this.dir = dir2;
 		this.dropsFile = new File(dir+"/drops");
 		this.blacklist = new Blacklist(instance); //selfres
 	}
 	
 	public void reloadCustomConfig() {
 	    if (dropsFile == null) {
 	    	dropsFile = new File(plugin.getDataFolder(), dir+"/drops");
 	    }
 	    customConfig = YamlConfiguration.loadConfiguration(dropsFile);
 	}
 	
 	public FileConfiguration getCustomConfig() {
 	    if (customConfig == null) {
 	        reloadCustomConfig();
 	    }
 	    return customConfig;
 	}
 	
 	public void saveCustomConfig() {
 	    if (customConfig == null || dropsFile == null) {
 	    	return;
 	    }
 	    try {
 	        customConfig.save(dropsFile);
 	    } catch (IOException ex) {
 	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + dropsFile, ex);
 	    }
 	}
 	
 	/**
 	 * Saves the drops of a player
 	 * @param player
 	 * @param drops
 	 */
 	public void put(Player player, PlayerInventory inv) {
 		String playerName = player.getName();
 		
 		int i=0;
 		
 		for (ItemStack itemStack : inv.getContents()) {
 			if(itemStack == null) continue;
 			getCustomConfig().set("drops."+playerName+"."+player.getWorld().getName()+".inventory.item"+String.valueOf(i++), itemStack.serialize());
 		}
 		
 		i=0;
 		
 		for (ItemStack itemStack : inv.getArmorContents()) {
 			if(itemStack == null || itemStack.getTypeId() == 0) continue;
 			getCustomConfig().set("drops."+playerName+"."+player.getWorld().getName()+".armor.armor"+String.valueOf(i++), itemStack.serialize());
 		}		
 		
 		saveCustomConfig();
 	}
 	
 	/**
 	 * Gives the player his inventory he had on death
 	 * @param player
 	 */
 	public void givePlayerInv(Player player) {
 		String playerName = player.getName();
 		String worldName = player.getWorld().getName();
 		ConfigurationSection cfgsel = getCustomConfig().getConfigurationSection("drops."+playerName+"."+worldName);
 		if(cfgsel == null) return;
 		ItemStack [] items = getItemsFromConfig("drops."+playerName+"."+player.getWorld().getName()+".inventory");
 		if (items != null)
 			player.getInventory().setContents(items);
 		
 		items = getItemsFromConfig("drops."+playerName+"."+player.getWorld().getName()+".armor");
 		if (items != null) {
 			HashMap<Integer, ItemStack> dropthis = player.getInventory().addItem(items);
 			for (Entry<Integer, ItemStack> e : dropthis.entrySet()) {
 				player.getWorld().dropItemNaturally(player.getLocation(), e.getValue());
 			}
 		}
 		remove(player);
 		saveCustomConfig();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private ItemStack[] getItemsFromConfig(String path) {
 		if (getCustomConfig().getConfigurationSection(path) == null)  return new ItemStack[0];
 		Set<String> keys = getCustomConfig().getConfigurationSection(path).getKeys(false);
 		ItemStack [] itemstack = new ItemStack[keys.size()];
 		Map<String, Object> item = null;
 		int i = 0;
 		Map<Enchantment, Integer> enchant = null;
 		for (String key : keys) {
 			if (!(getCustomConfig().get(path+"."+key) instanceof LinkedHashMap)) {
 				if (!(getCustomConfig().get(path+"."+key) instanceof MemorySection)) {
 					return null;
 				}
 				Map<String, Object> map = ((MemorySection) getCustomConfig().get(path+"."+key)).getValues(false);
 				item = map;
 				// enchantments
 				if (map.containsKey("enchantments")) {
 					enchant = new HashMap<Enchantment, Integer>();
 		             Object raw = ((MemorySection) map.get("enchantments")).getValues(false);
 		 
 		             if (raw instanceof Map) {
 		                 Map<?, ?> enchants = (Map<?, ?>) raw;
 		 
 		                 for (Map.Entry<?, ?> entry : enchants.entrySet()) {
 		                     Enchantment enchantment = Enchantment.getByName(entry.getKey().toString());
 		 
 		                     if ((enchantment != null) && (entry.getValue() instanceof Integer)) {
 		                         enchant.put(enchantment, (Integer) entry.getValue());
 		                     }
 		                 }
 		             }
 				}
 				else {
 					enchant = null;
 				}
 				// end enchantments
 			}
 			else {
 				item = (LinkedHashMap<String, Object>) getCustomConfig().get(path+"."+key);
 			}
 			itemstack[i] = ItemStack.deserialize(item);
 			if (enchant != null) itemstack[i].addUnsafeEnchantments(enchant);
 			i++;
 		}
 		return itemstack;
 	}
 	
       public void selfResPunish(Player player) {
 		int percent = plugin.getConfig().getInt("PERCENT");
 		if(percent == 0) return;
 		String playerName = player.getName();
 		ItemStack [] daritems = getItemsFromConfig("drops."+playerName+"."+player.getWorld().getName()+".inventory");
 		
 		int r, stopper;
 		int size = daritems.length;
 		int counter = (size/100)*percent;
 		if (counter < 1) counter = 1;
 		Random rand = new Random();
 		
 		while (counter >0) {
 			r = rand.nextInt(size);
 			stopper = 0;
 			
 			while (stopper < 20 || blacklist.contains(new Integer(daritems[r].getTypeId()))) {
 				r = rand.nextInt(size);
 				stopper++;
 			}
 			if(!blacklist.contains(new Integer(daritems[r].getTypeId()))) {
 				daritems[r] = null;
 			}
 			counter--;
 		}
 		
 		getCustomConfig().set("drops."+player.getName()+"."+player.getWorld().getName()+".inventory", null);
 		for (int i=0; i<size; i++) {
 			if (daritems[i] == null) continue;
 			getCustomConfig().set("drops."+playerName+"."+player.getWorld().getName()+".inventory.item"+String.valueOf(i), daritems[i].serialize());
 		}
 		saveCustomConfig();
 	}
 	
 	/**
 	 * Deletes all drops from a player
 	 * @param player
 	 */
 	public void remove(Player player) {
 		if(player == null) {
 			log.info("[Death and Rebirth] Error - Remove Players Inventory From Database");
 			return;
 		}
 		getCustomConfig().set("drops."+player.getName()+"."+player.getWorld().getName(), null);
 		saveCustomConfig();
 	}
 }
