 package tk.nekotech.war.events;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import tk.nekotech.war.War;
 
 public class PlayerInteract implements Listener {
 	private War war;
 	
 	public PlayerInteract(War war) {
 		this.war = war;
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
         Random random = new Random();
 		if (event.getClickedBlock() != null) {
 			if (event.getClickedBlock().getState() instanceof Sign) {
 				Sign sign = (Sign) event.getClickedBlock().getState();
 				Player player = event.getPlayer();
 				if (player.getGameMode() == GameMode.SURVIVAL) {
 					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 						return;
 					}
 				}
 				if (ChatColor.stripColor(sign.getLine(0)).equals("[BLU]")) {
 					if (war.teamhelpers.teamName(player) == 0) {
 						war.getServer().broadcastMessage(war.getMessage() + ChatColor.AQUA + event.getPlayer().getName() + " got a buff from the Magical Temple!");
 						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
 						event.getPlayer().setExp(event.getPlayer().getExp() + 300F);
 						war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 						war.sendMessage(player, ChatColor.AQUA + "Whoosh!");
 					} else {
 						war.sendMessage(player, ChatColor.RED + "You can't hit another teams sign! Find your own...");
 					}
 				}
 				if (ChatColor.stripColor(sign.getLine(0)).equals("[RED]")) {
 					if (war.teamhelpers.teamName(player) == 1) {
 						war.getServer().broadcastMessage(war.getMessage() + ChatColor.AQUA + event.getPlayer().getName() + " got a buff from the Magical Temple!");
 						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 1));
 						event.getPlayer().setExp(event.getPlayer().getExp() + 300F);
 						war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 						war.sendMessage(player, ChatColor.AQUA + "Whoosh!");
 					} else {
 						war.sendMessage(player, ChatColor.RED + "You can't hit another teams sign! Find your own...");
 					}
 				}
 				if (ChatColor.stripColor(sign.getLine(0)).equals("[JKS]")) {
					event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HARM, 600, 1));
					event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 1));
 					war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 					war.sendMessage(player, ChatColor.AQUA + "Whoosh!");
 				}
 				if (ChatColor.stripColor(sign.getLine(1)).equals("It's good to")) {
 	                final Inventory inventory = war.getServer().createInventory(event.getPlayer(), 27);
 	                ItemStack[] items = new ItemStack[27];
 	                if (random.nextBoolean()) {
 	                	for (int i = 0; i < 27; i++) {
 		                	int r = random.nextInt(27);
 		                	int amt = random.nextInt(63) + 1;
 		                	if (r == 0) {
 		                		items[i] = new ItemStack(Material.DIAMOND_SWORD, 1);
 		                		items[i].setDurability((short) 1661);
 		                	}
 		                	if (r == 1) {
 		                		items[i] = new ItemStack(Material.GHAST_TEAR, amt);
 		                	}
 		                	if (r == 2) {
 		                		items[i] = new ItemStack(Material.SLIME_BALL, amt);
 		                	}
 		                	if (r == 3) {
 		                		items[i] = new ItemStack(Material.WOOD_SWORD, 1);
 		                		items[i].setDurability((short) 25);
 		                	}
 		                	if (r == 4) {
 		                		items[i] = new ItemStack(Material.DIRT, amt);
 		                	}
 		                	if (r == 5) {
 		                		items[i] = new ItemStack(Material.TNT, amt);
 		                	}
 		                	if (r == 6) {
 		                		items[i] = new ItemStack(Material.BRICK, amt);
 		                	}
 		                	if (r == 7) {
 		                		items[i] = new ItemStack(Material.GLASS_BOTTLE, amt);
 		                	}
 		                }
 		                inventory.setContents(items);
 		                event.getPlayer().openInventory(inventory);
 		                war.inventory.add(event.getPlayer());
 	                } else {
 	                	war.teamhelpers.toSpawn(player, war.teamhelpers.teamName(player));
 						war.sendMessage(player, ChatColor.AQUA + "No prize here.");
 	                }
 				}
 				if (ChatColor.stripColor(sign.getLine(1)).equals("You might find")) {
 					if (random.nextBoolean()) {
 						event.getPlayer().openWorkbench(null, true);
 					} else {
 						event.getPlayer().openEnchanting(null, true);
 					}
 	                war.inventory.add(event.getPlayer());
 				}
 			}
 		}
 	}
 
 }
