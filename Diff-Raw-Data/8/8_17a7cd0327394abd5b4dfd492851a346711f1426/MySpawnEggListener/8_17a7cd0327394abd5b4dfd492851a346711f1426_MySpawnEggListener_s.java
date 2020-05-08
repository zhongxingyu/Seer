 package net.milkycraft.Listeners;
 
 import java.util.List;
 
 import net.milkycraft.Spawnegg;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class MySpawnEggListener implements Listener {
 	Spawnegg plugin;
 
 	public MySpawnEggListener(Spawnegg instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void OnEggUse(PlayerInteractEvent event) {
 		final int mons = plugin.getConfig().getInt("Economy.charge.monster");
 		final int aml = plugin.getConfig().getInt("Economy.charge.animal");
 		final int oth = plugin.getConfig().getInt("Economy.charge.other");
 		final String playa = event.getPlayer().getName();
 		final Player player = event.getPlayer();
 		final Location loc = event.getPlayer().getLocation();
 		final ChatColor green = ChatColor.GREEN;
 		final ChatColor red = ChatColor.RED;
 		final List<String> worldz = plugin.getConfig().getStringList(
 				"World.Worldname");
 		for (String worldname : worldz) {
 			if (event.getPlayer().getWorld().getName().equals(worldname)) {
 				if (event.getItem() == null) {
 					return;
 				}
 				if (event.getItem().getTypeId() == 383) {
 					if (!(event.getAction() == Action.LEFT_CLICK_AIR
 							|| event.getAction() == Action.RIGHT_CLICK_AIR || event
 								.getAction() == Action.LEFT_CLICK_BLOCK)) {
 						if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
 							if (!Spawnegg.worldguardPlugin
 									.canBuild(player, loc)) {
 								event.setCancelled(true);
 								player.sendMessage(ChatColor.AQUA
 										+ "[EM]"
 										+ ChatColor.RED
 										+ " You dont have permission for this area!");
 								return;
 							}
 						}
 					}
 				}
 				if (event.getItem() == null) {
 					return;
 				}
 				if (event.getItem().getTypeId() == 383) {
 					if (event.getItem().getDurability() == 50) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.creeper")
 								&& !player
 										.hasPermission("entitymanager.creeper")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use creeper eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 51) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.skeleton")
 								&& !player
 										.hasPermission("entitymanager.skeleton")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use skeleton eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 52) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.spider")
 								&& !player.hasPermission("entitymanager.spider")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use spider eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 54) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.zombie")
 								&& !player.hasPermission("entitymanager.zombie")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use zombie eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 55) {
 						if (plugin.getConfig()
 								.getBoolean("disabled.eggs.slime")
 								&& !player.hasPermission("entitymanager.slime")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ "You dont have permission to use slime eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 56) {
 						if (plugin.getConfig()
 								.getBoolean("disabled.eggs.ghast")
 								&& !player.hasPermission("entitymanager.ghast")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use ghast eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 57) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.pigman")
 								&& !player.hasPermission("entitymanager.pigman")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use zombie pigman eggs");
 						}
 						if (!player.hasPermission("antispawnegg.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 58) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.enderman")
 								&& !player
 										.hasPermission("entitymanager.enderman")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use enderman eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 59) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.cavespider")
 								&& !player
 										.hasPermission("entitymanager.cavespider")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use cave spider eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 60) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.silverfish")
 								&& !player
 										.hasPermission("entitymanager.silverfish")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use silverfish eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 61) {
 						if (plugin.getConfig()
 								.getBoolean("disabled.eggs.blaze")
 								&& !player.hasPermission("entitymanager.blaze")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use blaze eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 62) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.magmacube")
 								&& !player
 										.hasPermission("entitymanager.magmacube")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use magma cube eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 90) {
 						if (plugin.getConfig().getBoolean("disabled.eggs.pig")
 								&& !player.hasPermission("entitymanager.pig")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use pig eggs");
 						}
 						if (!player.hasPermission("antispawnegg.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 91) {
 						if (plugin.getConfig()
 								.getBoolean("disabled.eggs.sheep")
 								&& !player.hasPermission("entitymanager.sheep")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use sheep eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 92) {
 						if (plugin.getConfig().getBoolean("disabled.eggs.cow")
 								&& !player.hasPermission("entitymanager.cow")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use cow eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 93) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.chicken")
 								&& !player
 										.hasPermission("entitymanager.chicken")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use chicken eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 94) {
 						if (plugin.getConfig()
 								.getBoolean("disabled.eggs.squid")
 								&& !player.hasPermission("entitymanager.squid")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use squid eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 95) {
 						if (plugin.getConfig().getBoolean("disabled.eggs.wolf")
 								&& !player.hasPermission("entitymanager.wolf")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use wolf eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 96) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.mooshroom")
 								&& !player
 										.hasPermission("entitymanager.mooshroom")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use mooshroom eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, aml);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 98) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.ocelot")
 								&& !player
 										.hasPermission("entitymanager.ocelots")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use ocelot eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, oth);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 120) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.villager")
 								&& !player
 										.hasPermission("entitymanager.villager")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use villager eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, oth);
 							return;
 						}
 					}
 					else if (event.getItem().getDurability() == 200) {
 						if (plugin.getConfig().getBoolean(
 								"disabled.eggs.endercrystal")
 								&& !player
 										.hasPermission("entitymanager.endercrystal")) {
 							event.setCancelled(true);
 							alert(event);
 							player.sendMessage(green
 									+ "[EM]"
 									+ red
 									+ " You dont have permission to use crystal eggs");
 						}
 						if (!player.hasPermission("entitymanager.bypass.charge")
 								&& Spawnegg.econ != null) {
 							Spawnegg.econ.withdrawPlayer(playa, mons);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}

 	public void alert(PlayerInteractEvent e) {
 		boolean alertr = plugin.getConfig().getBoolean("send.alerts");
 		if (alertr) {
			Bukkit.broadcast(ChatColor.GREEN + "[ASE] " + ChatColor.DARK_RED
 					+ e.getPlayer().getDisplayName() + " Tryed to use a  "
 					+ ChatColor.GOLD + e.getItem().getType() + ".",
 					"entitymanager.admin");
 			return;
 		}
 	}
 }
