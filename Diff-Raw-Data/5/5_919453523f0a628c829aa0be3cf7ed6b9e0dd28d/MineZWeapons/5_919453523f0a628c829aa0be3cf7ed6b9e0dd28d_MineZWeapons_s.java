 package com.cin316.minezweapons;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.cin316.minezweapons.KikuichimonjiListener;
 import com.cin316.minezweapons.MineZWeapons;
 
 public class MineZWeapons extends JavaPlugin{
 	
 	public static MineZWeapons plugin;
 	public Logger log;
 	PluginDescriptionFile pdfFile;
 	PluginManager pluginManager;
 	public String[] helpText;
 	
 	public void onEnable(){
 		log = Logger.getLogger("Minecraft");
 		pdfFile = this.getDescription();
 		pluginManager = this.getServer().getPluginManager();
 		helpText = new String[]{
 			ChatColor.YELLOW + pdfFile.getName() + " version " + pdfFile.getVersion(),
 			ChatColor.YELLOW + pdfFile.getDescription()
 		};
 		
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled.");
 		
 		pluginManager.registerEvents( new KikuichimonjiListener(this), this );
 	}
 	
 	public void onDisable(){
 		log.info(pdfFile.getName() + " " + " has been disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
		if(args.length>=1){
 			return false;
 		}
 		
 		if( commandLabel.equalsIgnoreCase("minezweapons") ){
 			
 			if( args[0].equalsIgnoreCase("help") ){
 				if( !(sender instanceof Player) ){ //If the console sends the command.
 					ConsoleCommandSender console = (ConsoleCommandSender) sender;
 					
 					console.sendMessage(helpText);
 					
 				}else{ //If a player sends the command.
 					Player player = (Player) sender;
 					
 					if(player.hasPermission("minezweapons.info")){ //If the player has permission.
 						player.sendMessage(helpText);
 					}else{ //If the player doesn't has permission.
 						
 					}
 					
 				}
 			}else if( args[0].equalsIgnoreCase("give") ){
 				if( !(sender instanceof Player) ){ //If the console sends the command.
 					ConsoleCommandSender console = (ConsoleCommandSender) sender;
 					
 					console.sendMessage("You must be a player to use this command.");
 					
 				}else{ //If a player sends the command.
					if( args.length>=2){
 						return false;
 					}
 					
 					if( args[1].equalsIgnoreCase("kikuichimonji") ){
 						Player player = (Player) sender;
 						
 						if(player.hasPermission("minezweapons.use")){ //If the player has permission.
 							ItemStack is = new ItemStack(Material.WOOD_SWORD, 1); //Make a stack of 1 Wood Sword
 							ItemMeta im = is.getItemMeta();
 							im.setDisplayName("\u00a7oKikuichimonji"); //Set its name to KiKuichimonji.
 							is.setItemMeta(im);
 							player.getInventory().addItem(is);
 						}else{ //If the player doesn't has permission.
 							
 						}
 						
 					}
 				}
 			}
 			
 		}
 		
 		return true;
 	}
 	
 }
