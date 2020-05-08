 package com.tropicalwikis.tuxcraft.plugins.simpleecosigns;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleEcoSigns extends JavaPlugin implements Listener {
 	private Economy econ;
 	private HashMap<String, String> namesToIds = new HashMap<String, String>();
 	@Override
 	public void onEnable() {
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 			if (rsp != null) {
 				econ = rsp.getProvider();
 			}
 		} else {
 			getLogger().severe("Vault not found! Disabling plugin.");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		getServer().getPluginManager().registerEvents(this, this);
 		try {
 			BufferedReader r = new BufferedReader(new FileReader(new File(this.getDataFolder(), "items.csv")));
 			String sd = "";
 			while ((sd = r.readLine()) != null) {
 				if(!sd.contains("#")) {
 					String[] s = sd.split(",");
 					namesToIds.put(s[0].toLowerCase(), s[1] + ":" + s[2]);
 				}
 			}
 			r.close();
 		} catch (IOException e) {
 			getLogger().info("Could not load items.csv; this is okay. We will use Bukkit names for materials.");
 		}
 	}
 	
     @EventHandler
     public void signChange(SignChangeEvent event) {
         if (event.getLine(0).equalsIgnoreCase("[Buy]") || event.getLine(0).equalsIgnoreCase("[Sell]")) {
             // Verify the sign:
         	// [Buy]
         	// 1
         	// Diamond
         	// $5000
         	try {
         		Integer.valueOf(event.getLine(1));
         		String s2 = event.getLine(2);
         		String s3 = s2;
         		if(s2.contains(":")) {
         			s3 = s2.split(":")[0];
         		}
         		try {
         			Integer.valueOf(s3);
         		} catch (NumberFormatException e) {
         			if(!namesToIds.containsKey(s3.toLowerCase()))
         				if(Material.matchMaterial(s3) == null)
         					throw new Exception();
         		}
         		Double.valueOf(event.getLine(3).replace("$", ""));
         	} catch (Exception e) {
         		event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
         		event.getPlayer().sendMessage(ChatColor.RED + "Sign format invalid");
         		return;
         	}
             if (event.getPlayer().hasPermission("simpleecosigns.create")) {
                 event.setLine(0, ChatColor.DARK_BLUE + event.getLine(0));
             } else {
                 event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
             }
         }
     }
     
     @SuppressWarnings("deprecation")
 	@EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player p = event.getPlayer();
         if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Sign s = (Sign) event.getClickedBlock().getState();
             if(this.blockIsValidEcoSign(s)) {
             	if(s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Buy\\]")) {
             		if(econ.getBalance(p.getName()) >= Double.valueOf(s.getLine(3).replace("$", ""))) {
             			econ.withdrawPlayer(p.getName(), Double.valueOf(s.getLine(3).replace("$", "")));
             			if(p.getInventory().firstEmpty() == -1) {
             				p.sendMessage(ChatColor.RED + "You do not have enough inventory space. The items will be dropped on the ground.");
             				p.getWorld().dropItemNaturally(p.getLocation(), nameToItemStack(s.getLine(2), Integer.valueOf(s.getLine(1))));
             			} else {
             				p.getInventory().addItem(nameToItemStack(s.getLine(2), Integer.valueOf(s.getLine(1))));
             			}
             			p.sendMessage(ChatColor.GREEN + "Item purchased.");
             		} else {
             			p.sendMessage(ChatColor.RED + "You do not have enough funds.");
             		}
             		p.updateInventory();
             	}
             	if(s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Sell\\]")) {
             		// I have to do this backwards-ass way because Bukkit sucks.
             		ItemStack[] i = p.getInventory().getContents();
             		ItemStack l = nameToItemStack(s.getLine(2), Integer.valueOf(s.getLine(1)));
             		ItemStack tmp;
             		int amtavail = 0;
                     for (ItemStack item : i) {
                     	if(item != null) {
                     		if(item.getType() == l.getType() && item.getDurability() == l.getDurability()) {
                     			amtavail += item.getAmount();
                     			p.getInventory().remove(item);
                     		}
                     	}
                     }
                     if(amtavail < l.getAmount()) {
                     	p.sendMessage(ChatColor.RED + "You do not have enough items in your inventory.");
                     	return;
                     }
                    amtavail =- l.getAmount();
         			// Check the amount carefully.
         			if(amtavail > 0) {
         				// ...and add it back if needed
         				tmp = new ItemStack(l);
         				tmp.setAmount(amtavail);
            			p.getInventory().addItem(tmp);
         			}
         			econ.depositPlayer(p.getName(), Double.valueOf(s.getLine(3).replace("$", "")));
         			p.sendMessage(ChatColor.GREEN + "Item sold.");
         			p.updateInventory();
         			return;
             	}
             }
         }
     }
     
     private ItemStack nameToItemStack(String name, Integer amt) {
     	// Let's make life a bit easier on us:
     	name = name.toLowerCase();
     	if(name.contains(":")) {
     		try {
     			ItemStack tmp = new ItemStack(Integer.valueOf(name.split(":")[0]), amt, Short.valueOf(name.split(":")[1]));
     			return tmp;
     		} catch (NumberFormatException e) {
 	    		if(!namesToIds.containsKey(name.split(":")[0]))
 	    			return new ItemStack(Material.matchMaterial(name.split(":")[0]), amt, Short.valueOf(name.split(":")[1]));
 	    		else {
 	    			String s2 = name.split(":")[0];
 	    			String s3 = name.split(":")[1];
 	    			return new ItemStack(Integer.valueOf(namesToIds.get(s2).split(":")[0]), amt, Short.valueOf(s3));
 	    		}
     		}
     	} else {
     		try {
     			ItemStack tmp = new ItemStack(Integer.valueOf(name), amt);
     			return tmp;
     		} catch (NumberFormatException e) {
     			if(!namesToIds.containsKey(name.toLowerCase()))
     				return new ItemStack(Material.matchMaterial(name), amt);
     			else
     				return new ItemStack(Integer.valueOf(namesToIds.get(name).split(":")[0]), amt, Short.valueOf(namesToIds.get(name).split(":")[1]));
     		}
     	}
     }
 
     @EventHandler
     public void blockPlace(BlockPlaceEvent event) {
         if (event.getBlock() instanceof Sign) {
             Sign sign = (Sign) event.getBlock().getState();
             // Don't let the user make this an auth'd sign
             if (sign.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Buy\\]") || sign.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Sell\\]")) {
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     @EventHandler
     public void blockBreak(BlockBreakEvent event) {
         if (!(event.getBlock().getState() instanceof Sign)) {
             return;
         }
         Sign s = (Sign) event.getBlock().getState();
         if (blockIsValidEcoSign(s)) {
             if (!event.getPlayer().hasPermission("simpleecosigns.destroy")) {
                 event.setCancelled(true);
             }
         }
     }
     
     private boolean blockIsValidEcoSign(Sign s) {
     	try {
     		Integer.valueOf(s.getLine(1));
     		String s2 = s.getLine(2);
     		String s3 = s2;
     		if(s2.contains(":")) {
     			s3 = s2.split(":")[0];
     		}
     		try {
     			Integer.valueOf(s3);
     		} catch (NumberFormatException e) {
     			if(!namesToIds.containsKey(s3.toLowerCase()))
     				if(Material.matchMaterial(s3) == null)
     					return false;
     		}
     		Double.valueOf(s.getLine(3).replace("$", ""));
     	} catch (Exception e) {
     		return false;
     	}
     	return s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Buy\\]") || s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Sell\\]");
     }
 }
