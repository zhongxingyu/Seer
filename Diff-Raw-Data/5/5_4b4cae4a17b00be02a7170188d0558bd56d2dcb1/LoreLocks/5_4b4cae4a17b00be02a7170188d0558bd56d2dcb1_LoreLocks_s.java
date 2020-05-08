 package com.github.derwisch.loreLocks;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LoreLocks extends JavaPlugin {
 	  
 	public static final String NEW_MAIL_GUI_TITLE = ChatColor.BLACK + "PaperMail: New Mail" + ChatColor.RESET;
 	public static final String INBOX_GUI_TITLE = ChatColor.BLACK + "PaperMail: Inbox" + ChatColor.RESET;
 	public static final String LOCK_IDENTIFIER_DUMMY = "LOCK_IDENTIFIER_DUMMY";
 	
 	public static LoreLocks instance;
 	public static Server server;
 	public static Logger logger;
 	
 	private LoreLocksListener listener;
 	private FileConfiguration configuration;
 	
     @Override
     public void onEnable() {
     	instance = this;
     	server = this.getServer();
     	logger = this.getLogger();
     	
     	saveDefaultConfig();
     	configuration = this.getConfig();
     	Settings.LoadConfiguration(configuration);
     	
     	listener = new LoreLocksListener();
         this.getServer().getPluginManager().registerEvents(listener, this);
         
         initializeRecipes();
         
     	logger.info("Enabled LoreLocks");
     }
     
     public ShapedRecipe LockPickRecipe; 
     
 	private void initializeRecipes() {
 		ItemStack lockPick = new ItemStack(Material.getMaterial(Settings.LockPickID));
 		ItemMeta lockPickMeta = lockPick.getItemMeta();
 		lockPick.setDurability((short)Settings.LockPickDV);
 		lockPickMeta.setDisplayName(ChatColor.WHITE + Settings.LockPickName + ChatColor.RESET);
 		lockPick.setItemMeta(lockPickMeta);
 		
 		lockPick.setAmount(3);
 		
 		LockPickRecipe = new ShapedRecipe(lockPick).shape("GI").setIngredient('G', Material.GOLD_INGOT).setIngredient('I', Material.IRON_INGOT);
 		
 		AddShapedRecipe(LockPickRecipe);
 	}
 
 	@Override
     public void onDisable() {
 		Settings.SaveConfiguration(configuration);
 		this.saveConfig();
     	getLogger().info("Disabled LoreLocks");
     }
     
 	public ItemStack FactoryLock(Lock lock) {
 		ItemStack lockStack = new ItemStack(Material.getMaterial(lock.LockID));
 		ItemMeta lockMeta = lockStack.getItemMeta();
 		ArrayList<String> lockLore = new ArrayList<String>();
 		
 		ChatColor color = ChatColor.WHITE;
 		
 		switch (lock.Difficulty) {
 			case 1:
 				color = ChatColor.WHITE;
 				break;
 			case 2:
 				color = ChatColor.AQUA;
 				break;
 			case 3:
 				color = ChatColor.DARK_PURPLE;
 				break;
 			case 4:
 				color = ChatColor.GOLD;
 				break;
 			case 5:
 				color = ChatColor.DARK_GREEN;
 				break;
 			case 6:
 				color = ChatColor.DARK_RED;
 				break;
 		}
 		
 		lockMeta.setDisplayName(color + lock.LockName + ChatColor.RESET);
 		lockLore.add(ChatColor.GRAY + "Difficulty: " + lock.Difficulty + ChatColor.RESET);
 		lockMeta.setLore(lockLore);
 		lockStack.setItemMeta(lockMeta);
 		lockStack.setDurability(lock.LockDV);
 		
 		return lockStack;
 	}
 	
 	public boolean IsLock(ItemStack stack) {
 		ItemMeta meta = stack.getItemMeta();
 		if (meta == null) {
 			return false;
 		}
 		
 		String displayName = meta.getDisplayName();
 		if (displayName == null || displayName == "") {
 			return false;
 		}
 		
 		List<String> lore = meta.getLore();
 		for (int i = 1; i < 7; i++) {
 			if (lore.contains(ChatColor.GRAY + "Difficulty: " + i + ChatColor.RESET)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean IsLockPick(ItemStack stack) {
 		ItemMeta meta = stack.getItemMeta();
 		if (meta == null) {
 			return false;
 		}
 		
 		String displayName = meta.getDisplayName();
 		if (displayName == null || displayName == "") {
 			return false;
 		}
 		
 		return displayName.equals(ChatColor.WHITE + Settings.LockPickName + ChatColor.RESET);
 	}
 	
 	public ItemStack CreateKey(ItemStack lock) {
 		if (!IsLock(lock)) {
 			return null;
 		}
 		
 		int hash = lock.hashCode();
 		String keyInfo = ChatColor.BLACK.toString() + "#" + ChatColor.MAGIC.toString() + hash + ChatColor.RESET.toString(); 
 		
 		ItemMeta meta = lock.getItemMeta();
 		List<String> lore = meta.getLore();
 		lore.add(keyInfo);
 		meta.setLore(lore);
 		lock.setItemMeta(meta);
 		
 		ItemStack key = new ItemStack(Material.getMaterial(Settings.KeyID));
 		key.setDurability((short)Settings.KeyDV);
 		ItemMeta keyMeta = key.getItemMeta();
 		keyMeta.setDisplayName(ChatColor.WHITE + Settings.KeyName + ChatColor.RESET);
 		List<String> keyLore = new ArrayList<String>();
 		keyLore.add(keyInfo);
 		keyMeta.setLore(keyLore);
 		key.setItemMeta(keyMeta);
 		
 		return key;
 	}
 	
 	public boolean PlayerHasKey(Player player, ItemStack lock) {
 		Inventory inventory = player.getInventory();
 
 		for (int i = 0; i < inventory.getSize(); i++) {
 			ItemStack stack = inventory.getItem(i);
 			ItemMeta stackMeta = (stack != null) ? stack.getItemMeta() : null;
 			String stackName = (stackMeta != null) ? ((stackMeta.getDisplayName() != null) ? stackMeta.getDisplayName() : "") : "";
 			String requiredStackName = ChatColor.WHITE + Settings.KeyName + ChatColor.RESET;
 
 			if (stackName.equals(requiredStackName) && stack.getTypeId() == Settings.KeyID && stack.getDurability() == Settings.KeyDV) {
 				List<String> keyLore = stackMeta.getLore();
 				List<String> lockLore = lock.getItemMeta().getLore();
 				
 				for (String line : lockLore) {
 					if (line.startsWith(ChatColor.BLACK.toString() + "#" + ChatColor.MAGIC.toString())) {
 						if (keyLore.contains(line)) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	public int GetDifficulty(ItemStack lock) {
 		ItemMeta meta = lock.getItemMeta();
 		if (meta == null) {
 			return -1;
 		}
 		
 		String displayName = meta.getDisplayName();
 		if (displayName == null || displayName == "") {
 			return -1;
 		}
 		
 		List<String> lore = meta.getLore();
 		for (int i = 1; i < 7; i++) {
 			if (lore.contains(ChatColor.GRAY + "Difficulty: " + i + ChatColor.RESET)) {
 				return i;
 			}
 		}
 		
 		return -1;
 	}
 	
     public void AddShapedRecipe(ShapedRecipe recipe) {
 		this.getServer().addRecipe(recipe);
     }
 }
