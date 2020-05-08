 package me.rainoboy97.scrimmage;
 
 import java.util.Iterator;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 @SuppressWarnings("unused")
 public class Start implements Runnable {
 
 	private final JavaPlugin plugin;
 	int count;
 
 	public Start(JavaPlugin plugin, int count) {
 		this.plugin = plugin;
 		this.count = count;
 	}
 
 	@SuppressWarnings({ "rawtypes", "deprecation" })
 	public void run() {
 		if (Scrimmage.starting) {
 			if (count >= 5 && count % 5 == 0) {
 				Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "The match will begin in " + ChatColor.DARK_RED + count + ChatColor.DARK_GREEN + " seconds.");
 				Start start = new Start(plugin, count - 1);
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 20L);
 			} else if (count >= 5) {
 				Start start = new Start(plugin, count - 1);
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 20L);
			} else if (count <= 10 && count >= 6) {
				Bukkit.broadcastMessage(ChatColor.YELLOW + "The match will begin in " + ChatColor.DARK_RED + count + ChatColor.YELLOW + " seconds.");
				Start start = new Start(plugin, count - 1);
				Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 20L);
 			} else if (count <= 5 && count >= 2) {
				Bukkit.broadcastMessage(ChatColor.RED + "The match will begin in " + ChatColor.DARK_RED + count + ChatColor.RED + " seconds.");
 				Start start = new Start(plugin, count - 1);
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 20L);
 			} else if (count == 1) {
 				Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "The match will begin in " + ChatColor.DARK_RED + count + ChatColor.DARK_GREEN + " second.");
 				Start start = new Start(plugin, count - 1);
 				Bukkit.getServer().getScheduler().runTaskLater(plugin, start, 20L);
 			} else {
 				Scrimmage.starting = false;
 				Scrimmage.gameActive = true;
 				Bukkit.broadcastMessage(ChatColor.GREEN + "Let the games begin!");
 				for (Object e : Scrimmage.team) {
 					try {
 						String s = (String) e;
 						Player p = (Player) Bukkit.getOfflinePlayer(s);
 						p.setGameMode(GameMode.SURVIVAL);
 						p.removePotionEffect(PotionEffectType.INVISIBILITY);
 						p.removePotionEffect(PotionEffectType.SATURATION);
 						p.getInventory().clear();
 						ItemStack item;
 						Iterator iterItems = Var.items.iterator();
 						Iterator iterAmounts = Var.amounts.iterator();
 						for (int i = 1; i <= Var.itemAmount; i++) {
 							if (iterItems.hasNext() && iterAmounts.hasNext()) {
 								Object idEntry = iterItems.next();
 								Object amountEntry = iterAmounts.next();
 								item = new ItemStack(Integer.parseInt(idEntry.toString()), Integer.parseInt(amountEntry.toString()));
 								p.getInventory().setItem(i - 1, item);
 							}
 						}
 						if (Var.helmet != 0) {
 							ItemStack helmet = new ItemStack(Var.helmet, 1);
 							p.getInventory().setHelmet(helmet);
 						} else {
 							p.getInventory().setHelmet(null);
 						}
 						if (Var.chestplate != 0) {
 							ItemStack chestplate = new ItemStack(Var.chestplate, 1);
 							p.getInventory().setChestplate(chestplate);
 						} else {
 							p.getInventory().setChestplate(null);
 						}
 						if (Var.leggings != 0) {
 							ItemStack leggings = new ItemStack(Var.leggings, 1);
 							p.getInventory().setLeggings(leggings);
 						} else {
 							p.getInventory().setLeggings(null);
 						}
 						if (Var.boots != 0) {
 							ItemStack boots = new ItemStack(Var.boots, 1);
 							p.getInventory().setBoots(boots);
 						} else {
 							p.getInventory().setBoots(null);
 						}
 						p.updateInventory();
 						Location loc = p.getLocation();
 						loc = Var.teamSpawn;
 						RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, p, loc);
 						Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, 1L);
 					} catch (Exception z) {
 					}
 				}
 				for (Object e : Scrimmage.enemyTeam) {
 					try {
 						String s = (String) e;
 						Player p = (Player) Bukkit.getOfflinePlayer(s);
 						p.setGameMode(GameMode.SURVIVAL);
 						p.removePotionEffect(PotionEffectType.INVISIBILITY);
 						p.removePotionEffect(PotionEffectType.SATURATION);
 						p.getInventory().clear();
 						ItemStack item;
 						Iterator iterItems = Var.items.iterator();
 						Iterator iterAmounts = Var.amounts.iterator();
 						for (int i = 1; i <= Var.itemAmount; i++) {
 							if (iterItems.hasNext() && iterAmounts.hasNext()) {
 								Object idEntry = iterItems.next();
 								Object amountEntry = iterAmounts.next();
 								item = new ItemStack(Integer.parseInt(idEntry.toString()), Integer.parseInt(amountEntry.toString()));
 								p.getInventory().setItem(i - 1, item);
 							}
 						}
 						if (Var.helmet != 0) {
 							ItemStack helmet = new ItemStack(Var.helmet, 1);
 							p.getInventory().setHelmet(helmet);
 						} else {
 							p.getInventory().setHelmet(null);
 						}
 						if (Var.chestplate != 0) {
 							ItemStack chestplate = new ItemStack(Var.chestplate, 1);
 							p.getInventory().setChestplate(chestplate);
 						} else {
 							p.getInventory().setChestplate(null);
 						}
 						if (Var.leggings != 0) {
 							ItemStack leggings = new ItemStack(Var.leggings, 1);
 							p.getInventory().setLeggings(leggings);
 						} else {
 							p.getInventory().setLeggings(null);
 						}
 						if (Var.boots != 0) {
 							ItemStack boots = new ItemStack(Var.boots, 1);
 							p.getInventory().setBoots(boots);
 						} else {
 							p.getInventory().setBoots(null);
 						}
 						p.updateInventory();
 						Location loc = p.getLocation();
 						loc = Var.enemyTeamSpawn;
 						RespawnPlayer respawnPlayer = new RespawnPlayer(plugin, p, loc);
 						for (long i = 1; i <= 20; i++)
 							Bukkit.getServer().getScheduler().runTaskLater(plugin, respawnPlayer, i);
 					} catch (Exception z) {
 					}
 				}
 			}
 		}
 	}
 }
