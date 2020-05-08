 package me.shock.playervaults;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
import me.shock.playervaults.Commands;
import me.shock.playervaults.Listeners;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
 	public Map<String, String> invaultPlayers = new HashMap<String, String>();
 	public File datafolder;
 	public File datafile;
 	public Main plugin;
 	public FileConfiguration vaults;
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new Listeners(this), this);
 		getCommand("vault").setExecutor(new Commands(this));
 		loadVaults();
 	}
 
 	public void onDisable() {
 		saveData();
 	}
 
 	/**
 	 * Different way of loading custom configs.
 	 */
 	public void loadVaults() {
 		datafolder = getDataFolder();
 		datafile = new File(this.datafolder, "vaults.yml");
 		vaults = new YamlConfiguration();
 
 		if (!datafolder.exists()) {
 			try {
 				getServer().getLogger().info(
 						"[PlayerVaults] Creating vaults.yml");
 				datafolder.mkdir();
 				datafile.createNewFile();
 			} catch (Exception e) {
 				getServer().getLogger().severe(
 						"[PlayerVaults] Could not create vaults.yml: " + e);
 			}
 		} else if (!datafile.exists())
 			try {
 				getServer().getLogger().info(
 						"[PlayerVaults] Creating vaults.yml");
 				datafile.createNewFile();
 			} catch (Exception e) {
 				getServer().getLogger().severe(
 						"[PlayerVaults] Could not create vaults.yml: " + e);
 			}
 		try {
 			vaults.load(datafile);
 		} catch (Exception e) {
 			getServer().getLogger().severe(
 					"[PlayerVaults] Could not load vaults.yml: " + e);
 		}
 
 		saveData();
 	}
 
 	public void saveData() {
 		try {
 			vaults.save(datafile);
 		} catch (IOException e) {
 			getServer().getLogger().severe(
 					"[PlayerVaults] Couldn't save vaults.yml: " + e);
 		}
 	}
 
 	/**
 	 * Load the vault from the vaults.yml
 	 * 
 	 * @param vaultnum
 	 * @param player
 	 */
 	public void loadDefaultDirectory(String vaultnum, Player player) {
 		if (!vaults.getKeys(false).contains(player.getName())) {
 			vaults.createSection(player.getName());
 		}
 		if (!vaults.getConfigurationSection(player.getName()).getKeys(false)
 				.contains("vault" + vaultnum)) {
 			vaults.createSection(player.getName() + ".vault" + vaultnum);
 			vaults.set(player.getName() + ".vault" + vaultnum, "empty");
 		}
 		saveData();
 	}
 
 	public void clearDefaultDirectory(String vaultnum, Player player) {
 		if (vaults.getKeys(true).contains(player.getName())) {
 			vaults.getConfigurationSection(player.getName());
 			vaults.set("", "");
 		}
 		if (vaults.getConfigurationSection(player.getName()).getKeys(true)
 				.contains("vault" + vaultnum)) {
 			vaults.getConfigurationSection(player.getName() + ".vault"
 					+ vaultnum);
 			vaults.set(player.getName() + ".vault" + vaultnum, "empty");
 		}
 		saveData();
 		reloadConfig();
 	}
 
 	/**
 	 * Serialize the player's inventory. Will work to store anything except
 	 * books. Books needs NBT tag compounds for now. Leave that out for cross
 	 * version support.
 	 * 
 	 * @param invInventory
 	 * @param player
 	 * @return
 	 */
 	public String InventoryToString(Inventory invInventory, Player player) {
 
 		invInventory.addItem(player.getInventory().getArmorContents());
 		String serialization = invInventory.getSize() + ";";
 		for (int i = 0; i < invInventory.getSize(); i++) {
 			ItemStack is = invInventory.getItem(i);
 			if (is != null) {
 				String serializedItemStack = new String();
 
 				String isType = String.valueOf(is.getType().getId());
 				serializedItemStack += "t@" + isType;
 
 				if (is.getDurability() != 0) {
 					String isDurability = String.valueOf(is.getDurability());
 					serializedItemStack += ":d@" + isDurability;
 				}
 
 				if (is.getAmount() != 1) {
 					String isAmount = String.valueOf(is.getAmount());
 					serializedItemStack += ":a@" + isAmount;
 				}
 
 				Map<Enchantment, Integer> isEnch = is.getEnchantments();
 				if (isEnch.size() > 0) {
 					for (Entry<Enchantment, Integer> ench : isEnch.entrySet()) {
 						serializedItemStack += ":e@" + ench.getKey().getId()
 								+ "@" + ench.getValue();
 					}
 				}
 
 				serialization += i + "#" + serializedItemStack + ";";
 			}
 		}
 		return serialization;
 	}
 
 	/**
 	 * Get the inventory back from serialization. Doesn't work with written
 	 * books.
 	 * 
 	 * @param invString
 	 * @return
 	 */
 	public Inventory StringToInventory(String invString) {
 		String[] serializedBlocks = invString.split(";");
 		Inventory deserializedInventory = getServer().createInventory(null, 54);
 
 		for (int i = 1; i < serializedBlocks.length; i++) {
 			String[] serializedBlock = serializedBlocks[i].split("#");
 			int stackPosition = Integer.valueOf(serializedBlock[0]).intValue();
 
 			if (stackPosition < deserializedInventory.getSize()) {
 				ItemStack is = null;
 				Boolean createdItemStack = Boolean.valueOf(false);
 
 				String[] serializedItemStack = serializedBlock[1].split(":");
 				for (String itemInfo : serializedItemStack) {
 					String[] itemAttribute = itemInfo.split("@");
 					if (itemAttribute[0].equals("t")) {
 						is = new ItemStack(Material.getMaterial(Integer
 								.valueOf(itemAttribute[1]).intValue()));
 						createdItemStack = Boolean.valueOf(true);
 					} else if ((itemAttribute[0].equals("d"))
 							&& (createdItemStack.booleanValue())) {
 						is.setDurability(Short.valueOf(itemAttribute[1])
 								.shortValue());
 					} else if ((itemAttribute[0].equals("a"))
 							&& (createdItemStack.booleanValue())) {
 						is.setAmount(Integer.valueOf(itemAttribute[1])
 								.intValue());
 					} else if ((itemAttribute[0].equals("e"))
 							&& (createdItemStack.booleanValue())) {
 						is.addEnchantment(Enchantment.getById(Integer.valueOf(
 								itemAttribute[1]).intValue()),
 								Integer.valueOf(itemAttribute[2]).intValue());
 					} else if ((itemAttribute[0].equals("i"))
 							&& (createdItemStack.booleanValue())) {
 						ItemMeta meta = is.getItemMeta();
 						meta.setDisplayName(itemAttribute[1]);
 						is.setItemMeta(meta);
 					}
 				}
 				deserializedInventory.setItem(stackPosition, is);
 			}
 		}
 		return deserializedInventory;
 	}
 
 	/**
 	 * Let a player open a large vault.
 	 * 
 	 * @param vaultnum
 	 * @param player
 	 */
 	public void openLargeVault(String vaultnum, Player player) {
 		invaultPlayers.put(player.getName(), "vault" + vaultnum);
 		loadDefaultDirectory(vaultnum, player);
 		Inventory chest = getServer().createInventory(null, 54);
 		if (!vaults.getString(player.getName() + ".vault" + vaultnum)
 				.equalsIgnoreCase("empty")) {
 			chest.setContents(StringToInventory(
 					vaults.getString(player.getName() + ".vault" + vaultnum))
 					.getContents());
 		}
 		player.openInventory(chest);
 	}
 
 	public void deletePlayerVault(String vaultnum, Player player) {
 		invaultPlayers.remove(player.getName());
 		clearDefaultDirectory(vaultnum, player);
 	}
 
 }
