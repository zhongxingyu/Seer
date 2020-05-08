 package me.lucariatias.plugins.itemenchanting;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class LegacyEnchantmentCommand implements CommandExecutor {
 
 	private ItemEnchanting plugin;
 	
 	public LegacyEnchantmentCommand(ItemEnchanting plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("enchant")) {
 			sender.sendMessage(plugin.getConfig().getString("messages.command").replaceAll("&", "").replaceAll("%command%", "/" + cmd.getName().toLowerCase()));
 			if (sender.hasPermission("itemenchanting.command.enchant")) {
 				if (args.length == 2) {
 					if (Enchantment.getByName(args[0].toUpperCase()) != null) {
 						Enchantment enchantment = Enchantment.getByName(args[0].toUpperCase());
 						if (enchantment.canEnchantItem(((Player) sender).getItemInHand())) {
 							try {
 								Integer.parseInt(args[1]);
 								if (Integer.parseInt(args[1]) < enchantment.getMaxLevel()) {
 									Player player = (Player) sender;
 						    		Inventory inventory = player.getInventory();
 						    		if (plugin.getConfig().getBoolean("legacy.use-flat-rate")) {
 						    			ItemStack itemstack = new ItemStack(Material.getMaterial(plugin.getConfig().getString("legacy.item-name")), plugin.getConfig().getInt("legacy.amount"));
 						    			if (inventory.contains(Material.getMaterial(plugin.getConfig().getString("legacy.item-name")), plugin.getConfig().getInt("legacy.amount"))) {
 						    				inventory.removeItem(itemstack);
 						    			} else {
 						    				Integer itemAmount = plugin.getConfig().getInt("legacy.amount");
 						    				player.sendMessage(plugin.getConfig().getString("legacy.fail-message").replaceAll("&", "").replaceAll("%item-amount%", itemAmount.toString()).replaceAll("%item-name%", Material.getMaterial(plugin.getConfig().getString("legacy.item-name")).toString()));
 						    			}
 						    		} else {
 						    			ItemStack itemstack = new ItemStack(Material.getMaterial(plugin.getConfig().getString("legacy.item-name")), plugin.getConfig().getInt("misc.exp-level-for-command-enchant") * plugin.getConfig().getInt("legacy.amount"));
 						    			if (inventory.contains(Material.getMaterial(plugin.getConfig().getString("legacy.item-name")), plugin.getConfig().getInt("misc.exp-level-for-command-enchant") * plugin.getConfig().getInt("legacy.amount"))) {
 						    				inventory.removeItem(itemstack);
 						    			} else {
 						    				Integer itemAmount = plugin.getConfig().getInt("legacy.amount") * plugin.getConfig().getInt("misc.exp-level-for-command-enchant");
 						    				player.sendMessage(plugin.getConfig().getString("legacy.fail-message").replaceAll("&", "").replaceAll("%item-amount%", itemAmount.toString()).replaceAll("%item-name%", Material.getMaterial(plugin.getConfig().getString("legacy.item-name")).toString()));
 						    			}
 						    		}
 								} else {
 									sender.sendMessage(plugin.getConfig().getString("messages.invalid-level").replaceAll("&", ""));
 								}
 							} catch(NumberFormatException exception) {
 								sender.sendMessage(plugin.getConfig().getString("messages.invalid-level").replaceAll("&", ""));
 							}
 						} else {
 							sender.sendMessage(plugin.getConfig().getString("messages.invalid-enchantment").replaceAll("&", ""));
 						}
 					} else {
 						sender.sendMessage(plugin.getConfig().getString("messages.invalid-enchantment").replaceAll("&", ""));
 					}
 				} else {
 					sender.sendMessage(plugin.getConfig().getString("messages.incorrect-args").replaceAll("&", "").replaceAll("%usage%", "/enchant [enchantment] [level]"));
 				}
 			} else {
 				sender.sendMessage(plugin.getConfig().getString("messages.no-permission").replaceAll("&", "").replaceAll("%permission-node%", "itemenchanting.command.enchant"));
 			}
 			return true;
 		}
 		return false;
 	}
 
 }
