 package me.sirtyler.JunkyardCreek;
 
 import java.util.Random;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class FishListener implements Listener {
 	private static JunkyardCreek plugin;
 	private FileConfiguration config;
 	public Permission perm = null;
 	public Economy eco = null;
 	Random rand = new Random();
 
 	public FishListener(JunkyardCreek instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerFish(PlayerFishEvent event) {
 		Player player = event.getPlayer();
 		String state = event.getState().name();
 		config = plugin.config;
 		perm = plugin.permission;
 		eco = plugin.economy;
 		boolean spawnMob = false;
 		boolean useMoney = false;
 		String mob = null;
 		String money = null;
 		if (perm.playerHas(player, "junkyardcreek.fish")) {
 			if (state.equalsIgnoreCase("CAUGHT_FISH")) {
 				int itemNum = 349;
 				ItemStack item = new ItemStack(itemNum, 1);
 				String itemName = item.getType().name();
 				if (config.contains("Junk.Items")) {
 					try {
 						String con = config.get("Junk.Items").toString();
 						String start = con.split(",")[0].split("\\[")[1];
 						String end = con.split(",")[con.split(",").length - 1]
 								.split("\\]")[0];
 						String[] items = con.split(",");
 						for (int i = 0; i < items.length; i++) {
 							if (i == 0) {
 								items[0] = start;
 								items[i].replace(" ", "");
 							}
 							if (i == items.length - 1) {
 								items[i] = end;
 								items[i].replace(" ", "");
 							}
 							items[i].replace(" ", "");
 						}
 						int max = items.length;
 						int pickedNumber = rand.nextInt(max);
 						if (items[pickedNumber].contains(">")) {
 							String[] test = items[pickedNumber].split(">");
 							itemNum = Integer.parseInt(test[0]);
 							item = new ItemStack(itemNum, 1);
 							item.setDurability(Short.valueOf(test[1]));
 						} else if (items[pickedNumber].contains("@")) {
 							mob = items[pickedNumber].split("@")[1];
 							spawnMob = true;
 						} else if(items[pickedNumber].contains("\\$")) {
 							money = items[pickedNumber].split("\\$")[1];
 							useMoney = true;
 						} else {
 							itemNum = Integer.parseInt(items[pickedNumber]);
 							item = new ItemStack(itemNum, 1);
 						}
 						itemName = item.getType().name();
 					} catch (Exception e) {
 						Exception p = new Exception(
 								"Could not understand Config");
 						e.printStackTrace();
 						p.printStackTrace();
 						return;
 					}
 					event.setCancelled(true);
 					Location oLoc = event.getCaught().getLocation();
 					Location pLoc = player.getLocation();
 					Entity ent = null;
 					if (spawnMob == true) {
 						try {
 							ent = player.getWorld().spawnCreature(oLoc,
 									CreatureType.valueOf(mob));
 							itemName = mob.toLowerCase();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
					} 
					if (useMoney == true) {
 						eco.depositPlayer(player.getName(), Double.parseDouble(money));
 					} else {
						player.getWorld().dropItem(oLoc, item);
 					}
 
 					// Velocity from Minecraft Source + MCP Decompiler. Thank
 					// you Notch and MCP :3
 					double d1 = pLoc.getX() - oLoc.getX();
 					double d3 = pLoc.getY() - oLoc.getY();
 					double d5 = pLoc.getZ() - oLoc.getZ();
 					double d7 = ((float) Math
 							.sqrt((d1 * d1 + d3 * d3 + d5 * d5)));
 					double d9 = 0.10000000000000001D;
 					double motionX = d1 * d9;
 					double motionY = d3 * d9 + (double) ((float) Math.sqrt(d7))
 							* 0.080000000000000002D;
 					double motionZ = d5 * d9;
 					ent.setVelocity(new Vector(motionX, motionY, motionZ));
 					player.sendMessage(ChatColor.GOLD + "You got a " + itemName);
 				} else {
 					player.sendMessage(ChatColor.RED + "Error! Config mishap let Admin know");
 				}
 			} else if (state.equalsIgnoreCase("FAILED_ATTEMPT")) {
 				player.sendMessage(ChatColor.GRAY
 						+ "Awww, you didn't get anything");
 			}
 		}
 	}
 }
