 package com.turt2live.antishare.inventory;
 
 import java.io.File;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.MaterialData;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.storage.SQL;
 
 /**
  * AntiShare Inventory
  * 
  * @author turt2live
  */
 public class ASInventory {
 
 	/**
 	 * An enum to represent inventory types
 	 * 
 	 * @author turt2live
 	 */
 	public static enum InventoryType{
 		PLAYER("players"),
 		REGION("regions"),
 		TEMPORARY("temporary");
 
 		private String relativeFolderName;
 
 		private InventoryType(String relativeFolderName){
 			this.relativeFolderName = relativeFolderName;
 		}
 
 		/**
 		 * Gets the relative folder name
 		 * 
 		 * @return the folder
 		 */
 		public String getRelativeFolderName(){
 			return relativeFolderName;
 		}
 	}
 
 	/**
 	 * Generates an AntiShare Inventory from a player
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 */
 	public static ASInventory generate(Player player, InventoryType type){
 		ASInventory inventory = new ASInventory(type, player.getName(), player.getWorld(), player.getGameMode());
 		ItemStack[] contents = player.getInventory().getContents();
 		int slot = 0;
 		for(ItemStack item : contents){
 			inventory.set(slot, item);
 			slot++;
 		}
 		return inventory;
 	}
 
 	/**
 	 * Generates an inventory list
 	 * 
 	 * @param name inventory name
 	 * @param type the Inventory Type
 	 * @return the inventories
 	 */
 	public static List<ASInventory> generateInventory(String name, InventoryType type){
 		// Setup
 		List<ASInventory> inventories = new ArrayList<ASInventory>();
 
 		if(AntiShare.getInstance().useSQL()){
 			// SQL load
 
 			// Setup
 			for(World world : Bukkit.getWorlds()){
 				for(GameMode gamemode : GameMode.values()){
 					try{
 						ResultSet items = AntiShare.getInstance().getSQL().getQuery(AntiShare.getInstance().getSQL().getConnection().prepareStatement("SELECT * FROM " + SQL.INVENTORIES_TABLE + " WHERE name='" + name + "' AND type='" + type.name() + "' AND gamemode='" + gamemode.name() + "' AND world='" + world.getName() + "'"));
 						ASInventory inventory = new ASInventory(type, name, world, gamemode);
 
 						// Get items
 						if(items != null){
 							while (items.next()){
 								int slot = items.getInt("slot");
 
 								// Item properties
 								int id = items.getInt("itemID");
 								String durability = items.getString("itemDurability");
 								int amount = items.getInt("itemAmount");
 								byte data = Byte.parseByte(items.getString("itemData"));
 
 								// Create item
 								ItemStack item = new ItemStack(id);
 								item.setAmount(amount);
 								MaterialData itemData = item.getData();
 								itemData.setData(data);
 								item.setData(itemData);
 								item.setDurability(Short.parseShort(durability));
 								String enchants[] = items.getString("itemEnchant").split(" ");
 								if(items.getString("itemEnchant").length() > 0){
 									for(String enchant : enchants){
 										String parts[] = enchant.split("\\|");
 										String enchantID = parts[0];
 										int level = Integer.parseInt(parts[1]);
 										Enchantment e = Enchantment.getById(Integer.parseInt(enchantID));
 										item.addEnchantment(e, level);
 									}
 								}
 
 								// Set
 								inventory.set(slot, item);
 							}
 						}
 
 						// Save item to map
 						inventories.add(inventory);
 					}catch(SQLException e){
 						e.printStackTrace();
 					}
 				}
 			}
 		}else{
 			// Flat-File (YAML) load
 
 			// Setup
 			File dir = new File(AntiShare.getInstance().getDataFolder(), "inventories" + File.separator + type.getRelativeFolderName());
 			dir.mkdirs();
 			File saveFile = new File(dir, name + ".yml");
 			if(!saveFile.exists()){
 				return inventories;
 			}
 			EnhancedConfiguration file = new EnhancedConfiguration(saveFile, AntiShare.getInstance());
 			file.load();
 
 			// Load data
 			// Structure: yml:world.gamemode.slot.properties
 			for(String world : file.getKeys(false)){
 				for(String gamemode : file.getConfigurationSection(world).getKeys(false)){
					World worldV = Bukkit.getWorld(world);
					if(worldV == null){
						AntiShare.getInstance().getLogger().severe("World '" + world + "' does not exist (Inventory: " + type.name() + ", " + name + ".yml! AntiShare is ignoring this world.");
						continue;
					}
					ASInventory inventory = new ASInventory(type, name, worldV, GameMode.valueOf(gamemode));
 					for(String strSlot : file.getConfigurationSection(world + "." + gamemode).getKeys(false)){
 						Integer slot = Integer.valueOf(strSlot);
 						inventory.set(slot, file.getItemStack(world + "." + gamemode + "." + strSlot));
 					}
 					inventories.add(inventory);
 				}
 			}
 		}
 
 		// return
 		return inventories;
 	}
 
 	private ConcurrentHashMap<Integer, ItemStack> inventory = new ConcurrentHashMap<Integer, ItemStack>();
 	private AntiShare plugin;
 	private InventoryType type = InventoryType.PLAYER;
 	private String inventoryName = "UNKNOWN";
 	private World world;
 	private GameMode gamemode;
 
 	/**
 	 * Creates a new AntiShare Inventory
 	 */
 	public ASInventory(InventoryType type, String inventoryName, World world, GameMode gamemode){
 		plugin = AntiShare.getInstance();
 		this.type = type;
 		this.inventoryName = inventoryName;
 		this.world = world;
 		this.gamemode = gamemode;
 	}
 
 	/**
 	 * Sets a slot to an item
 	 * 
 	 * @param slot the slot
 	 * @param item the item
 	 */
 	public void set(int slot, ItemStack item){
 		if(item == null){
 			item = new ItemStack(Material.AIR, 1);
 		}
 		inventory.put(slot, item);
 	}
 
 	/**
 	 * Sets the player's inventory to this inventory
 	 * 
 	 * @param player the player
 	 */
 	@SuppressWarnings ("deprecation")
 	public void setTo(Player player){
 		PlayerInventory pInventory = player.getInventory();
 		pInventory.clear();
 		for(Integer slot : inventory.keySet()){
 			ItemStack item = inventory.get(slot);
 			if(item == null){
 				inventory.put(slot, new ItemStack(Material.AIR, 1));
 				item = new ItemStack(Material.AIR, 1);
 			}
 			pInventory.setItem(slot, item);
 		}
 		player.getInventory().setContents(pInventory.getContents());
 		player.updateInventory();
 	}
 
 	/**
 	 * Saves the inventory to disk
 	 */
 	public void save(){
 		if(plugin.useSQL()){
 			// SQL save
 
 			// Loop
 			for(Integer slot : inventory.keySet()){
 				try{
 					// Setup
 					PreparedStatement statement = plugin.getSQL().getConnection().prepareStatement("INSERT INTO " + SQL.INVENTORIES_TABLE + " (type, name, gamemode, world, slot, itemID, itemName, itemDurability, itemAmount, itemData, itemEnchant) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 					ItemStack item = inventory.get(slot);
 					int itemID = item.getTypeId();
 					String itemName = item.getType().name();
 					int itemDurability = item.getDurability();
 					int itemAmount = item.getAmount();
 					int itemData = item.getData().getData();
 
 					// Setup enchants
 					String enchant = "";
 					Set<Enchantment> enchantsSet = item.getEnchantments().keySet();
 					Map<Enchantment, Integer> enchantsMap = item.getEnchantments();
 					for(Enchantment e : enchantsSet){
 						enchant = enchant + e.getId() + "|" + enchantsMap.get(e) + " ";
 					}
 					if(enchant.length() > 0){
 						enchant = enchant.substring(0, enchant.length() - 1);
 					}
 
 					// Insert into query
 					statement.setString(1, type.name());
 					statement.setString(2, inventoryName);
 					statement.setString(3, gamemode.name());
 					statement.setString(4, world.getName());
 					statement.setInt(5, slot);
 					statement.setInt(6, itemID);
 					statement.setString(7, itemName);
 					statement.setInt(8, itemDurability);
 					statement.setInt(9, itemAmount);
 					statement.setInt(10, itemData);
 					statement.setString(11, enchant);
 
 					// Save
 					plugin.getSQL().insertQuery(statement);
 				}catch(SQLException e){
 					e.printStackTrace();
 				}
 			}
 		}else{
 			// Flat-File (YAML) save
 
 			// Setup
 			File dir = new File(plugin.getDataFolder(), "inventories" + File.separator + type.getRelativeFolderName());
 			dir.mkdirs();
 			File saveFile = new File(dir, inventoryName + ".yml");
 			EnhancedConfiguration file = new EnhancedConfiguration(saveFile, plugin);
 			file.load();
 
 			// Save data
 			// Structure: yml:world.gamemode.slot.properties
 			for(Integer slot : inventory.keySet()){
 				if(inventory.get(slot) == null){
 					continue;
 				}
 				file.set(world.getName() + "." + gamemode.name() + "." + String.valueOf(slot), inventory.get(slot));
 			}
 			file.save();
 		}
 	}
 
 	/**
 	 * Gets the world of this inventory
 	 * 
 	 * @return the world
 	 */
 	public World getWorld(){
 		return world;
 	}
 
 	/**
 	 * Gets the game mode of this inventory
 	 * 
 	 * @return the game mode
 	 */
 	public GameMode getGameMode(){
 		return gamemode;
 	}
 
 	/**
 	 * Changes the type of this inventory
 	 * 
 	 * @param type the new type
 	 */
 	public void setType(InventoryType type){
 		this.type = type;
 	}
 
 	/**
 	 * Gets the inventory type
 	 * 
 	 * @return the type
 	 */
 	public InventoryType getType(){
 		return type;
 	}
 
 	@Override
 	public ASInventory clone(){
 		ASInventory newI = new ASInventory(this.type, this.inventoryName, this.world, this.gamemode);
 		for(int slot : this.inventory.keySet()){
 			newI.set(slot, this.inventory.get(slot));
 		}
 		return newI;
 	}
 }
