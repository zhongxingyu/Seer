 package dk.gabriel333.BITBackpack;
 
 import dk.gabriel333.BukkitInventoryTools.BIT;
 import dk.gabriel333.Library.BITConfig;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.SpoutManager;
 
 public class BITBackpackInventorySaveTask implements Runnable {
 
 	private BIT plugin;
 
 	public void SBInventorySaveTask(BIT plugin) {
 	 this.plugin = plugin;
 	}
 
 	public static void saveAll() {
 		Player[] players = Bukkit.getServer().getOnlinePlayers();
 		HashMap<String, ItemStack[]> invs = new HashMap<String, ItemStack[]>(
 				BITBackpack.inventories);
 		for (Player player : players) {
 			saveInventory(player, player.getWorld());
 			if (invs.containsKey(player.getName())) {
 				invs.remove(player.getName());
 			}
 		}
 	}
 
 	public static void saveInventory(Player player, World world) {
 		
 		//TODO: Insert SAVE_TO_SQL. BITInventory.saveBitInventory(SpoutPlayer sPlayer, SpoutBlock block,
 		//String owner, String name, String coowners, Inventory inventory,
 		//int useCost);
 		
 		//BITInventory.saveBitInventory(player, null,
 		        //String owner, String name, String coowners, Inventory inventory,
 				//int useCost);
 		
 		
 		File saveFile;
 		if (BITConfig.getBooleanParm("SBP.InventoriesShare."
 				+ player.getWorld().getName(), true)) {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + ".yml");
 		} else {
 			saveFile = new File(BIT.plugin.getDataFolder() + File.separator
 					+ "inventories", player.getName() + "_" + world.getName()
 					+ ".yml");
 		}
 		YamlConfiguration config = new YamlConfiguration();
 		if (BITBackpack.inventories.containsKey(player.getName())) {
 			int size=BITBackpack.inventories.get(player.getName()).length;
 			//if (size>0){
 			//int sizeInConfig = BITBackpack.sizeInConfig(player.getWorld(), player);
 			//if (sizeInConfig > 0) {
                         //int size=BITBackpack.sizeInConfig(player.getWorld(), player);
                         //BITMessages.showInfo("Size =" + size);
                         if (size>0){
                                 Inventory inv = SpoutManager.getInventoryBuilder().construct(						size, BITBackpack.inventoryName);
 				inv.setContents(BITBackpack.inventories.get(player.getName()));
 				Integer i;
 				for (i = 0; i < size; i++) {
					//ItemStack item = inv.getItem(i);
					ItemStack item = inv.getContents()[i]; //New Line 3-9-12
					if (item == null) continue; //New Line 3-9-12
 					config.set(i.toString() + ".amount", item.getAmount());
 					Short durab = item.getDurability();
 					config.set(i.toString() + ".durability", durab.intValue());
 					config.set(i.toString() + ".type", item.getTypeId());
 					 int pos = 0;
 	                    for(Enchantment enchantment : item.getEnchantments().keySet()){
 	                        config.set(i.toString() + ".enchant" + pos, enchantment.getName());
 	                        config.set(i.toString() + ".level" + pos, item.getEnchantmentLevel(enchantment));
 	                        pos ++;
 	                    }
 					config.set("Size",
 							size);
 					try {
 						config.save(saveFile);
                                                 //player.sendMessage("The backpack is now Saved.");
 					} catch (IOException e) {
 						player.sendMessage("The backpack could not be saved.");
 						e.printStackTrace();
 					}
 				}
 			}
 		} else {
 			BITBackpack.inventories.remove(player.getName());
 		}
 	}
 
 	@Override
 	public void run() {
 		if (Bukkit.getServer().getOnlinePlayers().length != 0) {
 			saveAll();
 		}
 	}
 }
