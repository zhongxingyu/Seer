 package me.flyinglawnmower.simplesort;
 
 /*
  * SimpleSort plugin by:
  * - Shadow1013GL
  * - Pyr0Byt3
  * - pendo324
  */
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfigurationOptions;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleSort extends JavaPlugin implements Listener {
 	private void sortInventory(Inventory inventory, int startIndex, int endIndex) {
 		ItemStack[] items = inventory.getContents();
 		
 		for (int i = startIndex; i < endIndex; i++) {
 			ItemStack item1 = items[i];
 			if (item1 == null) {
 				continue;
 			}
 			int maxStackSize = getConfig().getBoolean("stack-all") ? 64 : item1.getMaxStackSize();
 			if (item1.getAmount() <= 0 || maxStackSize == 1) {
 				continue;
 			}
 			if (item1.getAmount() < maxStackSize) {
 				int needed = maxStackSize - item1.getAmount();
 				for (int j = i + 1; j < endIndex; j++) {
 					ItemStack item2 = items[j];
 					if (item2 == null || item2.getAmount() <= 0 || maxStackSize == 1) {
 						continue;
 					}
					if (item2.getTypeId() == item1.getTypeId() && item1.getDurability() == item2.getDurability() && item1.getEnchantments().equals(item2.getEnchantments())) {
 						if (item2.getAmount() > needed) {
 							item1.setAmount(maxStackSize);
 							item2.setAmount(item2.getAmount() - needed);
 							break;
 						} else {
 							items[j] = null;
 							item1.setAmount(item1.getAmount() + item2.getAmount());
 							needed = maxStackSize - item1.getAmount();
 						}
 					}
 				}
 			}
 		}
 		Arrays.sort(items, startIndex, endIndex, new ItemComparator());
 		inventory.setContents(items);
 	}
 
 	public void onEnable() {
 		FileConfigurationOptions options = this.getConfig().options();
 		
 		options.header("enable-wand: Whether or not to allow sorting with the wand.\nwand: The item ID of the chest-sorting wand (0 for hand).\nstack-all: Whether or not to stack all item types to 64.");
 		options.copyDefaults(true);
 		saveConfig();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		event.getPlayer().setMetadata("CommandSorting", new FixedMetadataValue(this, false));
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (sender instanceof Player) {
 			if (command.getName().equalsIgnoreCase("sort")) {
 				Player player = (Player)sender;
 				Block block = player.getTargetBlock(null, 4);
 				String inventorySortPerm = "simplesort.inventory";
 				
 				if (args.length == 0) {
 					if (block.getType() == Material.CHEST
 							|| block.getType() == Material.TRAPPED_CHEST
 							|| block.getType() == Material.ENDER_CHEST) {
 						player.performCommand("sort chest");
 					} else {
 						player.performCommand("sort top");
 					}
 					return true;
 				}
 				switch (args[0].toLowerCase()) {
 					case "chest":
 						if (player.hasPermission("simplesort.chest")) {
 							if (block.getType() == Material.CHEST
 									|| block.getType() == Material.TRAPPED_CHEST
 									|| block.getType() == Material.ENDER_CHEST) {
 								player.setMetadata("CommandSorting", new FixedMetadataValue(this, true));
 								this.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, new ItemStack(getConfig().getInt("wand")), block, BlockFace.SELF));
 								return true;
 							} else {
 								player.sendMessage(ChatColor.DARK_RED + "Not currently targeting a chest!");
 								return true;
 							}
 						} else {
 							player.sendMessage(ChatColor.DARK_RED + "You don't have permission to sort chests!");
 						}
 						break;
 					case "top":
 						if (player.hasPermission(inventorySortPerm)) {
 							sortInventory(player.getInventory(), 9, 36);
 							player.sendMessage(ChatColor.DARK_GREEN + "Inventory top sorted!");
 						} else {
 							player.sendMessage(ChatColor.DARK_RED + "You don't have permission to sort your inventory!");
 						}
 						return true;
 					case "all":
 						if (player.hasPermission(inventorySortPerm)) {
 							sortInventory(player.getInventory(), 0, 36);
 							player.sendMessage(ChatColor.DARK_GREEN + "Entire inventory sorted!");
 						} else {
 							player.sendMessage(ChatColor.DARK_RED + "You don't have permission to sort your inventory!");
 						}
 						return true;
 					case "hot":
 						if (player.hasPermission(inventorySortPerm)) {
 								sortInventory(player.getInventory(), 0, 9);
 								player.sendMessage(ChatColor.DARK_GREEN + "Hotbar sorted!");
 						} else {
 							player.sendMessage(ChatColor.DARK_RED + "You don't have permission to sort your inventory!");
 						}
 						return true;
 					default:
 						return false;
 				}
 			}
 		} else {
 			sender.sendMessage("You need to be a player to sort your inventory!");
 			return true;
 		}
 		return false;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		
 		if (event.getAction() == Action.LEFT_CLICK_BLOCK
 				&& event.getMaterial().getId() == getConfig().getInt("wand")
 				&& (player.hasPermission("simplesort.chest.wand") || player.getMetadata("CommandSorting").get(0).asBoolean())) {
 			Block block = event.getClickedBlock();
 			Inventory inventory;
 			
 			if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
 				inventory = ((InventoryHolder)block.getState()).getInventory();
 			} else if (block.getType() == Material.ENDER_CHEST) {
 				inventory = player.getEnderChest();
 			} else {
 				return;
 			}
 			player.setMetadata("CommandSorting", new FixedMetadataValue(this, false));
 			sortInventory(inventory, 0, inventory.getSize());
 			player.sendMessage(ChatColor.DARK_GREEN + "Chest sorted!");
 		}
 	}
}
