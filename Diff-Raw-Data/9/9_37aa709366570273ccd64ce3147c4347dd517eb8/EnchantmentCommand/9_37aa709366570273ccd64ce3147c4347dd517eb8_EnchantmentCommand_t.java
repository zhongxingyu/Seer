 package me.lucariatias.plugins.itemenchanting;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class EnchantmentCommand implements CommandExecutor {
 
 	private ItemEnchanting plugin;
 	
 	public EnchantmentCommand(ItemEnchanting plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("itemenchant")) {
 			sender.sendMessage(plugin.getConfig().getString("messages.command").replaceAll("&", "").replaceAll("%command%", "/" + cmd.getName().toLowerCase()));
 			if (sender.hasPermission("itemenchanting.command.enchant")) {
 				if (args.length == 2) {
 					if (Enchantment.getByName(args[0].toUpperCase()) != null) {
 						Enchantment enchantment = Enchantment.getByName(args[0].toUpperCase());
 						if (enchantment.canEnchantItem(((Player) sender).getItemInHand())) {
 							try {
 								Integer.parseInt(args[1]);
 								if (Integer.parseInt(args[1]) < enchantment.getMaxLevel()) {
 									String enchantmentName = enchantment.getName().toLowerCase();
 									Integer enchantmentLevel = Integer.parseInt(args[1]);
 									Material item = Material.getMaterial(plugin.getConfig().getString("enchantments." + enchantmentName + ".item-name"));
 									Integer numItems = 0;
 									String failMsg = plugin.getConfig().getString("enchantments." + enchantmentName + ".fail-message");
 									Player player = (Player) sender;
 									Inventory inventory = player.getInventory();
 									
 									switch (plugin.getConfig().getInt("enchantments." + enchantmentName + ".mode")) {
 										case 0:
 											//Flatrates
 											numItems = plugin.getConfig().getInt("enchantments." + enchantmentName + ".amount");
 											break;
 										case 1:
 											//Exp level multiplier
 											numItems = plugin.getConfig().getInt("enchantments." + enchantmentName + ".amount") * plugin.getConfig().getInt("misc.exp-level-for-command-enchant");
 											break;
 										case 2:
 											//Enchant level multiplier
 											numItems = plugin.getConfig().getInt("enchantments." + enchantmentName + ".amount") * enchantmentLevel;
 											break;
 										default:
 											//When anything else is given
 											numItems = 0;
 									}
 									
 									ItemStack cost = new ItemStack(item, numItems);
 									
 									if (enchantmentLevel > 0) {
 										if (inventory.contains(cost.getType(), cost.getAmount())) {
 											inventory.removeItem(cost);
 											player.getItemInHand().addEnchantment(enchantment, enchantmentLevel);
 											player.sendMessage(plugin.getConfig().getString("messages.successful-enchant").replaceAll("&", "").replaceAll("%item-amount%", numItems.toString()).replaceAll("%item-name%", item.toString().toLowerCase()).replaceAll("%player%", player.getName()).replaceAll("%enchantment-name%", enchantmentName).replaceAll("%enchantment-level%", enchantmentLevel.toString()));
 										} else {
 											player.sendMessage(failMsg.replaceAll("&", "").replaceAll("%item-amount%", numItems.toString()).replaceAll("%item-name%", item.toString().toLowerCase()).replaceAll("%player%", player.getName()).replaceAll("%enchantment-name%", enchantmentName).replaceAll("%enchantment-level%", enchantmentLevel.toString()));
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
