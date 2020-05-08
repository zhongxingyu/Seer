 package me.lucariatias.plugins.kaisocraft;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 public class PartyCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("party")) {
 			if (args.length > 0) {
 				sender.sendMessage(ChatColor.YELLOW + "==" + ChatColor.BLUE + "/" + cmd.getName().toLowerCase() + " " + ChatColor.DARK_BLUE + args[0].toLowerCase() + ChatColor.YELLOW + "==");
 				if (args[0].equalsIgnoreCase("members")) {
 					if (sender.hasPermission("kaisocraft.command.party.members")) {
 						sender.sendMessage(ChatColor.GREEN + "==" + ChatColor.DARK_GREEN + "Your party" + ChatColor.GREEN + "==");
 						for (Player player : ((Player) sender).getWorld().getPlayers()) {
 							if (KaisoCraft.getPlayerGuild(player.getName()) == KaisoCraft.getPlayerGuild(sender.getName())) {
 								if (player.getLocation().distance(((Player) sender).getLocation()) <= 32) {
 									sender.sendMessage(ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.DARK_PURPLE + " : Lvl" + player.getLevel() + " " + ChatColor.LIGHT_PURPLE + player.getType().toString());
 								}
 							}
 						}
 						sender.sendMessage(ChatColor.RED + "==" + ChatColor.DARK_RED + "Enemy party" + ChatColor.RED + "==");
 						for (Entity entity : ((Player) sender).getWorld().getEntities()) {
 							if (entity instanceof LivingEntity) {
 								if (!(entity instanceof Player)) {
 									if (((Player) sender).getLocation().distance(entity.getLocation()) <= 32) {
 										sender.sendMessage(ChatColor.DARK_PURPLE + "Lvl" + KaisoCraft.getEntityLevel(entity) + ChatColor.LIGHT_PURPLE  + " " + entity.getType().toString());
 									}
 								} else {
 									if (KaisoCraft.getPlayerGuild(((Player) entity).getName()) != null) {
 										if (KaisoCraft.getPlayerGuild(sender.getName()) != null) {
 											if (KaisoCraft.getPlayerGuild(((Player) entity).getName()) != KaisoCraft.getPlayerGuild(sender.getName())) {
 												sender.sendMessage(ChatColor.LIGHT_PURPLE + ((Player) entity).getName() + ChatColor.DARK_PURPLE + " : Lvl" + ((Player) entity).getLevel() + " " + ChatColor.LIGHT_PURPLE + ((Player) entity).getType().toString());
 											}
 										}
 									} else {
										if ((Player) entity != (Player) sender) {
											sender.sendMessage(ChatColor.LIGHT_PURPLE + ((Player) entity).getName() + ChatColor.DARK_PURPLE + " : Lvl" + ((Player) entity).getLevel() + " " + ChatColor.LIGHT_PURPLE + ((Player) entity).getType().toString());
										}
 									}
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You do not have permission!");
 						sender.sendMessage(ChatColor.GREEN + "Permission node: " + ChatColor.DARK_GREEN + "kaisocraft.command.party.members");
 					}
 				}
 				
 				if (args[0].equalsIgnoreCase("stats")) {
 					if (sender.hasPermission("kaisocraft.command.party.stats")) {
 						if (args.length >= 2) {
 							if (Bukkit.getServer().getPlayer(args[1]) != null) {
 								Player player = Bukkit.getServer().getPlayer(args[1]);
 								if (KaisoCraft.getPlayerGuild(player.getName()) == KaisoCraft.getPlayerGuild(sender.getName())) {
 									if (((Player) sender).getWorld() == player.getWorld()) {
 										if (((Player) sender).getLocation().distance(player.getLocation()) <= 32) {
 											sender.sendMessage(ChatColor.GREEN + "==" + ChatColor.DARK_GREEN + player.getName() + ChatColor.GREEN + "==");
 											sender.sendMessage(ChatColor.DARK_AQUA + "Level: " + ChatColor.AQUA + player.getLevel());
 											sender.sendMessage(ChatColor.DARK_AQUA + "Exp: " + ChatColor.AQUA + KaisoCraft.getExp(player) + "/" + player.getExpToLevel());
 											sender.sendMessage(ChatColor.DARK_AQUA + "HP: " + ChatColor.AQUA + player.getHealth() + "/" + player.getMaxHealth());
 											sender.sendMessage(ChatColor.DARK_AQUA + "Attack: " + ChatColor.AQUA + KaisoCraft.getPlayerAttack(player));
 											sender.sendMessage(ChatColor.DARK_AQUA + "Defence: " + ChatColor.AQUA + KaisoCraft.getPlayerDefence(player));
 										} else {
 											sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not currently in your party!");
 										}
 									} else {
 										sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not currently in your party!");
 									}
 								} else {
 									sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not a member of your guild!");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "That player is not currently online!");
 							}
 						} else {
 							Player player = (Player) sender;
 							sender.sendMessage(ChatColor.GREEN + "==" + ChatColor.DARK_GREEN + player.getName() + ChatColor.GREEN + "==");
 							sender.sendMessage(ChatColor.DARK_AQUA + "Level: " + ChatColor.AQUA + player.getLevel());
 							sender.sendMessage(ChatColor.DARK_AQUA + "Exp: " + ChatColor.AQUA + KaisoCraft.getExp(player) + "/" + player.getExpToLevel());
 							sender.sendMessage(ChatColor.DARK_AQUA + "HP: " + ChatColor.AQUA + player.getHealth() + "/" + player.getMaxHealth());
 							sender.sendMessage(ChatColor.DARK_AQUA + "Attack: " + ChatColor.AQUA + KaisoCraft.getPlayerAttack(player));
 							sender.sendMessage(ChatColor.DARK_AQUA + "Defence: " + ChatColor.AQUA + KaisoCraft.getPlayerDefence(player));
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You do not have permission!");
 						sender.sendMessage(ChatColor.GREEN + "Permission node: " + ChatColor.DARK_GREEN + "kaisocraft.command.party.stats");
 					}
 				}
 				
 				if (args[0].equalsIgnoreCase("assist")) {
 					if (sender.hasPermission("kaisocraft.command.party.assist")) {
 						if (args.length >= 2) {
 							if (Bukkit.getServer().getPlayer(args[1]) != null) {
 								Player player = Bukkit.getServer().getPlayer(args[1]);
 								if (KaisoCraft.getPlayerGuild(Bukkit.getServer().getPlayer(args[1]).getName()) == KaisoCraft.getPlayerGuild(sender.getName())) {
 									if (((Player) sender).getWorld() == player.getWorld()) {
 										if (((Player) sender).getLocation().distance(player.getLocation()) <= 32) {
 											sender.sendMessage(ChatColor.GREEN + "Assisting " + ChatColor.DARK_GREEN + player.getName() + ChatColor.GREEN + "...");
 											((Player) sender).teleport(player.getLocation());
 										} else {
 											sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not currently in your party!");
 										}
 									} else {
 										sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not currently in your party!");
 									}
 								} else {
 									sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not a member of your guild!");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "That player is not currently online!");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "Incorrect usage!");
 							sender.sendMessage(ChatColor.GREEN + "Usage: /party assist [player]");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You do not have permission!");
 						sender.sendMessage(ChatColor.GREEN + "Permission node: " + ChatColor.DARK_GREEN + "kaisocraft.command.party.stats");
 					}
 				}
 				
 				if (args[0].equalsIgnoreCase("trade")) {
 					if (sender.hasPermission("kaisocraft.command.party.trade")) {
 						if (args.length >= 2) {
 							if (Bukkit.getServer().getPlayer(args[1]) != null) {
 								Player player = Bukkit.getServer().getPlayer(args[1]);
 								if (KaisoCraft.getPlayerGuild(Bukkit.getServer().getPlayer(args[1]).getName()) == KaisoCraft.getPlayerGuild(sender.getName())) {
 									if (((Player) sender).getWorld() == player.getWorld()) {
 										if (((Player) sender).getLocation().distance(player.getLocation()) <= 32) {
 											if (KaisoCraft.trades.get(args[1]) != null) {
 												sender.sendMessage(ChatColor.GREEN + "Opened trade inventory " + ChatColor.DARK_GREEN + args[0]);
 												((Player) sender).openInventory(KaisoCraft.trades.get(args[1]));
 												if (KaisoCraft.trades.get(args[1]).getViewers().isEmpty()) {
 													sender.sendMessage(ChatColor.RED + "No one is currently trading in this inventory!");
 												} else {
 													sender.sendMessage(ChatColor.GREEN + "Currently trading with:");
 													for (HumanEntity entity : KaisoCraft.trades.get(args[1]).getViewers()) {
 														sender.sendMessage(ChatColor.DARK_GREEN + entity.getName());
 													}
 												}
 											} else {
 												sender.sendMessage(ChatColor.GREEN + "Created trade inventory " + ChatColor.DARK_GREEN + args[1]);
 												KaisoCraft.trades.put(args[0], Bukkit.getServer().createInventory(null, 27, "Trade"));
 												((Player) sender).openInventory(KaisoCraft.trades.get(args[1]));
 												sender.sendMessage(ChatColor.RED + "No one is currently trading in this inventory!");
 											}
 										} else {
 											sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not currently in your party!");
 										}
 									} else {
 										sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not currently in your party!");
 									}
 								} else {
 									sender.sendMessage(ChatColor.DARK_RED + player.getName() + ChatColor.RED + " is not a member of your guild!");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "That player is not currently online!");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "Incorrect usage!");
 							sender.sendMessage(ChatColor.GREEN + "Usage: /party assist [player]");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You do not have permission!");
 						sender.sendMessage(ChatColor.GREEN + "Permission node: " + ChatColor.DARK_GREEN + "kaisocraft.command.party.stats");
 					}
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 }
