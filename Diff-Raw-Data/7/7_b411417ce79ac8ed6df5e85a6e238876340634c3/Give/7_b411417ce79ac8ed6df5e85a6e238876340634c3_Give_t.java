 package net.madmanmarkau.Give;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 // Permissions:
 // if (iStick.Permissions.has(player, "foo.bar")) {}
 
 public class Give extends JavaPlugin {
 	public final Logger log = Logger.getLogger("Minecraft");
     public PermissionHandler Permissions;
 	public Configuration Config;
     public PluginDescriptionFile pdfFile;
 
 	@Override
 	public void onDisable() {
 	    log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " unloaded");
 	}
 
 	@Override
 	public void onEnable() {
 		this.pdfFile = this.getDescription();
 
 		setupPermissions();
 		
 		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " loaded");
 	}
 
 	public void setupPermissions() {
 		Plugin perm = this.getServer().getPluginManager().getPlugin("Permissions");
 			
 		if (this.Permissions == null) {
 			if (perm!= null) {
 				this.getServer().getPluginManager().enablePlugin(perm);
 				this.Permissions = ((Permissions) perm).getHandler();
 			}
 			else {
 				log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + "not enabled. Permissions not detected");
 				this.getServer().getPluginManager().disablePlugin(this);
 			}
 		}
 	}
 	
     public static Material getMaterial(String m) throws IllegalArgumentException {
         try {
         	int itemId = Integer.decode(m);
         	
             Material material = Material.getMaterial(itemId);
             if (material == null) {
             	throw new IllegalArgumentException("Unknown material " + m);
             }
             return material;
         } catch (NumberFormatException ex) {
             Material material = Material.getMaterial(m.toUpperCase());
             if (material == null) {
             	throw new IllegalArgumentException("Unknown material " + m);
             }
             return material;
         }
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
 		Material material = null;
 		int quantity = 1;
 		Player player = null;
 
 		if ( cmd.getName().compareToIgnoreCase("give") == 0 ) {
 			if (sender instanceof Player) {
 				// Player-sent command
 				player = (Player) sender;
 
 				if (Permissions.has(player, "give")) {
 					if (args.length == 0) {
						sender.sendMessage(ChatColor.RED + "/give <item> [<amount>]");
						return true;
 					}
 
 					if (args.length >= 1) {
 						try {
 							material = getMaterial(args[0]);
 						} catch (IllegalArgumentException ex) {
 							sender.sendMessage(ex.getMessage());
 							return true;
 						}
 
 						if (material == null) {
 							sender.sendMessage("Unknown material " + args[0]);
 							return true;
 						}
 
 						if (material.getId() == -1 || material.getId() == 0) {
 							sender.sendMessage("Unknown or invalid material " + args[0]);
 							return true;
 						}
 					}
 
 					if (args.length >= 2) {
 						try {
 							quantity = Integer.decode(args[1]);
 						} catch (NumberFormatException ex) {
 							sender.sendMessage("Illegal quantity!");
 							return true;
 						}
 					}
 				} else {
 					return false;
 				}
 
 			} else {
 				// Server/plugin sent command
 				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "/give <player> <item> [<amount>]");
					return true;
 				}
 
 				if (args.length >= 1) {
 					try {
 						player = sender.getServer().getPlayer(args[0]);
 					} catch (IllegalArgumentException ex) {
 						sender.sendMessage(ex.getMessage());
 						return true;
 					}
 
 					if (player == null) {
 						sender.sendMessage("Unknown player " + args[0]);
 						return true;
 					}
 				}
 
 				if (args.length >= 2) {
 					try {
 						material = getMaterial(args[1]);
 					} catch (IllegalArgumentException ex) {
 						sender.sendMessage(ex.getMessage());
 						return true;
 					}
 
 					if (material == null) {
 						sender.sendMessage("Unknown material " + args[1]);
 						return true;
 					}
 
 					if (material.getId() == -1 || material.getId() == 0) {
 						sender.sendMessage("Unknown or invalid material " + args[1]);
 						return true;
 					}
 				}
 
 				if (args.length >= 3) {
 					try {
 						quantity = Integer.decode(args[2]);
 					} catch (NumberFormatException ex) {
 						sender.sendMessage("Illegal quantity!");
 						return true;
 					}
 				}
 			}
 			
 			// Valid options
 			if (Permissions.has(player, "give." + material.getId()) || (sender != player)) {
 				PlayerInventory inventory = player.getInventory();
 
 				inventory.addItem(new ItemStack[] { new ItemStack(material, quantity) });
 
 
 				if (sender == player) {
 					sender.sendMessage("Here, have some " + material.toString());
 				} else {
 					player.sendMessage("You have been given some " + material.toString());
 				}
 			} else {
 				sender.sendMessage("You may not have that item!");
 			}
 
 			
 			return true;
 		}
     	return false;
     }
 }
