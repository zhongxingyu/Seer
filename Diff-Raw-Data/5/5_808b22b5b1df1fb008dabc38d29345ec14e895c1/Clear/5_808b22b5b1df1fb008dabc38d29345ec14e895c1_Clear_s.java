 package com.wwsean08.clear;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import static java.nio.file.StandardCopyOption.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.logging.Logger;
 
 import me.kalmanolah.extras.OKUpdater;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Clear extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	public final String VERSION = "1.8.7";
 	public String DBV = "1.1.3";
 	private final String NAME = "ClearInv New";
 	private final String PREFIX = "[ClearInv]";
 	private File itemFile = null;
 	private File configFile = null;
 	private Server server;
 	public PermissionHandler ph;
 	private YamlConfiguration config;
 	private HashMap<Player, ItemStack[]> originalInventory;
 	private PluginManager pm;
 	private ClearPlayerListener pl;
 	private LinkedList<ClearItemHolder> items;
 	private boolean usesSP = true;
 
 	@Override
 	public void onEnable() {
 		initVariables();
 		getDBV();
 		createConfig();
 		if(config.getBoolean("autoupdate", true))
 			autoUpdate();
 		if(config.getBoolean("superperm", true) != true)
 			usesSP = false;
 		getPerm();
 		pm.registerEvent(Event.Type.PLAYER_QUIT, pl, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, pl, Event.Priority.Monitor, this);
 		items = new LinkedList<ClearItemHolder>();
 		loadItems();
 		log.info(PREFIX + " clear inventory version " + VERSION + " enabled");
 	}
 
 	@Override
 	public void onDisable() {
 		Player[] player = server.getOnlinePlayers();
 		for(Player p : player){
 			unpreview(p);
 		}
 		log.info(PREFIX + " clear inventory version " + VERSION + " disabled");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("clear")) {
 			if (sender instanceof Player) {
 				if(usesSP)
 					superPerm(sender, args);
 				else if (ph != null) 
 					playerPerm(sender, args);
 
 				// if no permissions set up
 				else 
 					playerNoPerm(sender, args);
 
 			} else if (sender instanceof ConsoleCommandSender) 
 				consoleClear(sender, args);
 		}
 		if(commandLabel.equalsIgnoreCase("preview")){
 			if(sender instanceof Player){
 				if(args.length == 0){
 					Player player = (Player) sender;
 					unpreview(player);
 				}
 				if(args.length == 1){
 					Player player = (Player) sender;
 					Player affected = server.matchPlayer(args[0]).get(0);
 					if(affected!=null){
 						if(usesSP){
 							if(player.hasPermission("clear.other")){
 								preview(player, affected);
 								ClearRunnable run = new ClearRunnable(this, player);
 								server.getScheduler().scheduleSyncDelayedTask(this, run, 6000);
 							}
 						}
 						else if(ph != null && ph.has(player, "clear.other")){
 							preview(player, affected);
 							ClearRunnable run = new ClearRunnable(this, player);
 							server.getScheduler().scheduleSyncDelayedTask(this, run, 6000);
 						}
 						else if(player.isOp()){
 							preview(player, affected);
 							ClearRunnable run = new ClearRunnable(this, player);
 							server.getScheduler().scheduleSyncDelayedTask(this, run, 6000);
 						}
 						else{
 							sender.sendMessage("You do not have permission to use this command");
 							log.warning(PREFIX + player.getDisplayName() + " Attempted to preview another players inventory");
 						}
 					}
 				}
 			}
 			else{
 				if(args.length == 0){
 					sender.sendMessage("Error: I need a name of who to preview");
 				}else{
 					Player player = server.getPlayer(args[0]);
 					ArrayList<String> contains = preview(player);
 					sender.sendMessage("Here is the inventory of " + player.getDisplayName());
 					for(String s : contains){
 						sender.sendMessage(s);
 					}
 				}
 			}
 		}
 		if(commandLabel.equalsIgnoreCase("unpreview") || commandLabel.equalsIgnoreCase("revert")){
 			if(sender instanceof Player){
 				Player player = (Player) sender;
 				unpreview(player);
 			}
 		}
 		return true;
 	}
 	/**
 	 * is used to display a players inventory from the server console
 	 * @param player the player whose inventory is being previewed
 	 * @return a list of items to display to the server console
 	 */
 	private ArrayList<String> preview(Player player) {
 		ArrayList<String> inventoryList = new ArrayList<String>();
 		ArrayList<Integer> itemNumbers = new ArrayList<Integer>();
 		HashMap<Integer, Integer> ammount = new HashMap<Integer, Integer>();
 		PlayerInventory inv = player.getInventory();
 		ItemStack[] contents = inv.getContents();
 		for(ItemStack a : contents){
 			if(a != null){
 				Integer value = a.getTypeId();
 				if(itemNumbers.contains(Integer.valueOf(value))){
 					int i = ammount.get(value);
 					i += a.getAmount();
 					ammount.put(value, i);
 				}else{
 					itemNumbers.add(value);
 					ammount.put(value, a.getAmount());
 				}
 			}
 		}
 		for(Integer i : itemNumbers){
 			StringBuilder out = new StringBuilder();
 			out.append(ammount.get(i) + "x ");
 			for(int j = 0; j < items.size(); j++){
 				if(items.get(j).getItem() == i.intValue()){
 					out.append(items.get(j).getOutput());
 					break;
 				}
 			}
 			inventoryList.add(out.toString());
 		}
 		return inventoryList;
 	}
 
 	/**
 	 * used if the server uses superperm (aka bukkit perms)
 	 * @param sender is the player who sends the command
 	 * @param args is the arguments of the command
 	 * 
 	 */
 
 	private void superPerm(CommandSender sender, String[] args) {
 		Player player = (Player) sender;
 		if (player.hasPermission("clear.self") || player.hasPermission("clear.other")) {
 			if (args.length == 0)
 				clearAll(player);
 			else if(args[0].trim().equals("*")){
 				if(player.hasPermission("clear.admin")){
 					Player[] online = server.getOnlinePlayers();
 					if(args.length == 1){
 						for(Player p : online)
 							clearAllRemote(sender, p);
 					}
 					else if(args[1].trim().equalsIgnoreCase("except")){
 						for(Player p : online)
 							clearExceptRemote(sender, p, args);
 					}
 					else if(args[1].equalsIgnoreCase("armor")){
 						for(Player p : online)
 							clearArmorRemote(sender, p);
 					}
 					else{
 						for(Player p : online)
 							clearItemRemote(sender, p, args);
 					}
 				}
 			}
 			else if (args[0].equalsIgnoreCase("except")){
 				clearExcept(player, args);
 			}
 			else if (args[0].equalsIgnoreCase("armor"))
 				clearArmor(player);
 			else if(args[0].equalsIgnoreCase("boots") || args[0].equalsIgnoreCase("boot")){
 				player.getInventory().setBoots(null);
 				player.sendMessage("Boots removed");
 			}else if(args[0].equalsIgnoreCase("helmet") || args[0].equalsIgnoreCase("helm")){
 				player.getInventory().setHelmet(null);
 				player.sendMessage("Helmet removed");
 			}else if(args[0].equalsIgnoreCase("pants") || args[0].equalsIgnoreCase("leggings")){
 				player.getInventory().setLeggings(null);
 				player.sendMessage("Leggings removed");
 			}else if(args[0].equalsIgnoreCase("shirt") || args[0].equalsIgnoreCase("chestplate") || args[0].equalsIgnoreCase("chest")){
 				player.getInventory().setChestplate(null);
 				player.sendMessage("Chestplate removed");
 			}else if (args[0].equalsIgnoreCase("help")) {
 				playerHelp(sender);
			} else if ((server.matchPlayer(args[0]) != null) && (player.hasPermission("clear.other") || sender.isOp())) {
 				Player affectedPlayer = server.matchPlayer(args[0]).get(0);
 				if (args.length == 1) {
 					clearAllRemote(player, affectedPlayer);
 				} else if (args[1].equalsIgnoreCase("except")) {
 					clearExceptRemote(player, affectedPlayer, args);
 				} else if(args[1].equalsIgnoreCase("armor")){
 					clearArmorRemote(player, affectedPlayer);
 				} else
 					clearItemRemote(player, affectedPlayer, args);
			} else if ((server.matchPlayer(args[0]) != null) && !((player.hasPermission("clear.other") || !sender.isOp()))) {
 				sender.sendMessage("You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			} else 
 				clearItem(player, args);
 		}
 	}
 
 	/**
 	 * takes care of commands if permissions system is detected
 	 * @param sender is the player who sent the command
 	 * @param args are the arguments for the command
 	 * 
 	 */
 	private void playerPerm(CommandSender sender, String[] args){
 		Player player = (Player) sender;
 		if (ph.has(player, "clear.self") || ph.has(player, "clear.other") || ph.has(player, "clear.admin")) {
 			if (args.length == 0)
 				clearAll(player);
 			else if(args[0].trim().equals("*")){
 				if(ph.has(player, "clear.admin")){
 					Player[] online = server.getOnlinePlayers();
 					if(args.length == 1){
 						for(Player p : online)
 							clearAllRemote(sender, p);
 					}
 					else if(args[1].trim().equalsIgnoreCase("except")){
 						for(Player p : online)
 							clearExceptRemote(sender, p, args);
 					}
 					else if(args[1].equalsIgnoreCase("armor")){
 						for(Player p : online)
 							clearArmorRemote(sender, p);
 					}
 					else{
 						for(Player p : online)
 							clearItemRemote(sender, p, args);
 					}
 				}
 			}
 			else if (args[0].equalsIgnoreCase("except")) {
 				clearExcept(player, args);
 			}
 			else if (args[0].equalsIgnoreCase("armor"))
 				clearArmor(player);
 			else if(args[0].equalsIgnoreCase("boots") || args[0].equalsIgnoreCase("boot")){
 				player.getInventory().setBoots(null);
 				player.sendMessage("Boots removed");
 			}else if(args[0].equalsIgnoreCase("helmet") || args[0].equalsIgnoreCase("helm")){
 				player.getInventory().setHelmet(null);
 				player.sendMessage("Helmet removed");
 			}else if(args[0].equalsIgnoreCase("pants") || args[0].equalsIgnoreCase("leggings")){
 				player.getInventory().setLeggings(null);
 				player.sendMessage("Leggings removed");
 			}else if(args[0].equalsIgnoreCase("shirt") || args[0].equalsIgnoreCase("chestplate") || args[0].equalsIgnoreCase("chest")){
 				player.getInventory().setChestplate(null);
 				player.sendMessage("Chestplate removed");
 			}else if (args[0].equalsIgnoreCase("help")) {
 				playerHelp(sender);
 			} else if ((server.getPlayer(args[0]) != null) && (ph.has(player, "clear.other") || sender.isOp())) {
 				Player affectedPlayer = server.matchPlayer(args[0]).get(0);
 				if (args.length == 1) {
 					clearAllRemote(player, affectedPlayer);
 				} else if (args[1].equalsIgnoreCase("except")) {
 					clearExceptRemote(player, affectedPlayer, args);
 				} else if(args[1].equalsIgnoreCase("armor")){
 					clearArmorRemote(player, affectedPlayer);
 				} else
 					clearItemRemote(player, affectedPlayer, args);
 			} else if ((server.getPlayer(args[0]) != null) && !((ph.has(player, "clear.other") || !sender.isOp()))) {
 				sender.sendMessage("You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			} else if (args.length >= 1 && (ph.has(player, "clear.self") || ph.has(player, "clear.other"))) {
 				clearItem(player, args);
 			}
 		}
 	}
 
 	/**
 	 * takes care of commands if no permissions system is detected
 	 * @param sender is the player who sent the command
 	 * @param args are the arguments for the command
 	 * 
 	 */
 	private void playerNoPerm(CommandSender sender, String[] args){
 		Player player = (Player) sender;
 		if (args.length == 0) 
 			clearAll(player);
 		else if (args[0].equalsIgnoreCase("except")) 
 			clearExcept(player, args);
 		else if(args[0].trim().equals("*")){
 			if(sender.isOp()){
 				Player[] online = server.getOnlinePlayers();
 				if(args.length == 1){
 					for(Player p : online)
 						clearAllRemote(sender, p);
 				}else if(args[1].trim().equalsIgnoreCase("except")){
 					for(Player p : online){
 						clearExceptRemote(sender, p, args);
 					}
 				}else if(args[1].trim().equalsIgnoreCase("armor")){
 					for(Player p : online){
 						clearArmorRemote(sender, p);
 					}
 				}
 				else{
 					for(Player p : online){
 						clearItemRemote(sender, p, args);
 					}
 				}
 			}else{
 				sender.sendMessage("You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			}
 		}
 		else if (args[0].equalsIgnoreCase("help")) 
 			playerHelp(sender);
 		//begin armor removal
 		else if(args[0].equalsIgnoreCase("armor"))
 			clearArmor(player);
 		else if(args[0].equalsIgnoreCase("boots") || args[0].equalsIgnoreCase("boot")){
 			player.getInventory().setBoots(null);
 			player.sendMessage("Boots removed");
 		}else if(args[0].equalsIgnoreCase("helmet") || args[0].equalsIgnoreCase("helm")){
 			player.getInventory().setHelmet(null);
 			player.sendMessage("Helmet removed");
 		}else if(args[0].equalsIgnoreCase("pants") || args[0].equalsIgnoreCase("leggings")){
 			player.getInventory().setLeggings(null);
 			player.sendMessage("Leggings removed");
 		}else if(args[0].equalsIgnoreCase("shirt") || args[0].equalsIgnoreCase("chestplate") || args[0].equalsIgnoreCase("chest")){
 			player.getInventory().setChestplate(null);
 			player.sendMessage("Chestplate removed");
 		}
 		//end armor removal
 		else if (server.getPlayer(args[0]) != null) {
 			if (sender.isOp()) {
 				Player affectedPlayer = server.matchPlayer(args[0]).get(0);
 				if (args.length == 1)
 					clearAllRemote(sender, affectedPlayer);
 				else if (args[1].equalsIgnoreCase("except")) 
 					clearExceptRemote(sender, affectedPlayer, args);
 				else if (args[1].trim().equalsIgnoreCase("armor"))
 					clearArmorRemote(sender, affectedPlayer);
 				else 
 					clearItemRemote(sender, affectedPlayer, args);
 			} else {
 				sender.sendMessage("You do not have permission to use that command");
 				log.warning(PREFIX + player.getDisplayName() + " tried to clear another players inventory without the necessary permissions");
 			}
 		} else 
 			clearItem(player, args);
 	}
 
 	/**
 	 * takse care of commands sent by the console
 	 * @param sender is the console which sent the command
 	 * @param args are the arguments for the command
 	 */
 	private void consoleClear(CommandSender sender, String[] args){
 		if (args.length >= 1) {
 			if (args[0].equalsIgnoreCase("help")){
 				consoleHelp(sender);
 				return;
 			}else if(args[0].equalsIgnoreCase("*")){
 				Player[] online = server.getOnlinePlayers();
 				if(args.length == 1){
 					for(Player p : online)
 						clearAllRemote(sender, p);
 				}
 				else if(args[1].trim().equalsIgnoreCase("except")){
 					for(Player p : online)
 						clearExceptRemote(sender, p, args);
 				}
 				else if(args[1].equalsIgnoreCase("armor")){
 					for(Player p : online)
 						clearArmorRemote(sender, p);
 				}
 				else{
 					for(Player p : online)
 						clearItemRemote(sender, p, args);
 				}
 			}else if (args.length == 1){
 				Player player = server.getPlayer(args[0]);
 				clearAllRemote(sender, player);
 			}
 			else if (args[1].equalsIgnoreCase("except")){
 				Player player = server.getPlayer(args[0]);
 				clearExceptRemote(sender, player, args);
 			}
 			else if(args[1].equalsIgnoreCase("armor")){
 				Player player = server.getPlayer(args[0]);
 				clearArmorRemote(sender, player);
 			}
 			else if(args[1].equalsIgnoreCase("boots") || args[1].equalsIgnoreCase("boot")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setBoots(null);
 				sender.sendMessage("Boots removed from " + player.getDisplayName());
 			}else if(args[1].equalsIgnoreCase("helmet") || args[1].equalsIgnoreCase("helm")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setHelmet(null);
 				sender.sendMessage("Helmet removed from " + player.getDisplayName());
 			}else if(args[1].equalsIgnoreCase("pants") || args[1].equalsIgnoreCase("leggings")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setLeggings(null);
 				sender.sendMessage("Leggings removed from " + player.getDisplayName());
 			}else if(args[1].equalsIgnoreCase("shirt") || args[1].equalsIgnoreCase("chestplate") || args[1].equalsIgnoreCase("chest")){
 				Player player = server.getPlayer(args[0]);
 				player.getInventory().setChestplate(null);
 				sender.sendMessage("Chestplate removed from " + player.getDisplayName());
 			}else {
 				Player player = server.getPlayer(args[0]);
 				clearItemRemote(sender, player, args);
 			}
 		}
 	}
 	/**
 	 * displays the help text if the console sent the command
 	 * @param sender is the person who sent the command
 	 */
 	private void consoleHelp(CommandSender sender){
 		sender.sendMessage(ChatColor.BLUE + "Because you are the server console, you don't have an inventory, however you can clear other players inventories");
 		sender.sendMessage(ChatColor.BLUE + "To clear a players inventory completely type:");
 		sender.sendMessage(ChatColor.RED + "/clear <player>");
 		sender.sendMessage(ChatColor.BLUE + "To clear certain items from a players inventory type:");
 		sender.sendMessage(ChatColor.RED + "/clear <player> <item1> [item2] [item3]...");
 		sender.sendMessage(ChatColor.BLUE + "And if you want to clear everything bu8t certain items from a players inventory type this:");
 		sender.sendMessage(ChatColor.RED + "/clear <player> except <item1> [item2] [item3]...");
 		sender.sendMessage(ChatColor.GOLD + "Thank you for using wwsean08's inventory clearing plugin");
 	}
 	/**
 	 * displays the help text if the player sent the command
 	 * @param sender Is the person who sent the command
 	 */
 	private void playerHelp(CommandSender sender){
 		sender.sendMessage(ChatColor.AQUA + "you can clear your inventory of everything like this:");
 		sender.sendMessage(ChatColor.RED + "/clear");
 		sender.sendMessage(ChatColor.AQUA + "You can exclude items using the except keyword as the first argument like this:");
 		sender.sendMessage(ChatColor.RED + "/clear except sand");
 		sender.sendMessage(ChatColor.AQUA + "You can delete select items by naming them as arguments like this:");
 		sender.sendMessage(ChatColor.RED + "/clear sand gravel");
 		if (sender.isOp() || ph.has((Player) sender, "clear.other")) {
 			sender.sendMessage(ChatColor.AQUA + "And you have permission to clear other peoples invetories, and view them");
 			sender.sendMessage(ChatColor.RED + "/clear name item1 item2...");
 			sender.sendMessage(ChatColor.AQUA + "Tp view them the command is preview and to unview them its unpreview");
 			sender.sendMessage(ChatColor.RED + "/preview <user> to put the players inventory in yours");
 			sender.sendMessage(ChatColor.RED + "/unpreview to restore your inventory");
 		}
 	}
 	/**
 	 * Allows an admin to preview a players inventory
 	 * @param previewer The admin that will be previewing the inventory
 	 * @param previewee The player whose inventory is being previewed
 	 */
 	private void preview(Player previewer, Player previewee){
 		ItemStack[] preview = previewee.getInventory().getContents();
 		if(!originalInventory.containsKey(previewer))
 			originalInventory.put(previewer, previewer.getInventory().getContents());
 		previewer.getInventory().setContents(preview);
 		ClearRunnable runner = new ClearRunnable(this, previewer);
 		server.getScheduler().scheduleSyncDelayedTask(this, runner, 6000);
 
 	}
 
 	/**
 	 * Restores the content of an admins inventory if they are previewing one
 	 * @param previewer
 	 */
 	public void unpreview(Player previewer){
 		if(originalInventory.containsKey(previewer)){
 			previewer.getInventory().clear();
 			previewer.getInventory().setContents(originalInventory.get(previewer));
 			originalInventory.remove(previewer);
 		}
 	}
 
 	/**
 	 * Clears all the items out of the players inventory
 	 * @param sender is the player who sent the command
 	 */
 	public void clearAll(Player sender) {
 		sender.getInventory().clear();
 		sender.sendMessage("Inventory Cleared");
 	}
 
 	/**
 	 * Clears all the items out of another user's inventory
 	 * @param sender is the player who sent the command.
 	 * @param affected is the player who's inventory gets cleared.
 	 */
 	public void clearAllRemote(CommandSender sender, Player affected) {
 		affected.getInventory().clear();
 		sender.sendMessage(affected.getDisplayName() + "'s inventory has been cleared.");
 	}
 
 	/**
 	 * clears all the items except for the ones specified by the player.
 	 * @param sender is the player who sent the command
 	 * @param args the list of items to exclude (either in number of name form).
 	 */
 	public void clearExcept(Player sender, String[] args) {
 		PlayerInventory pi = sender.getInventory();
 		ArrayList<Integer> clear = new ArrayList<Integer>();
 		ArrayList<String> successful = new ArrayList<String>();
 		for(int i = 0; i<pi.getSize(); i++)
 			clear.add(i);
 		for(String a : args){
 			for(int j = 0; j < items.size(); j++){
 				if(items.get(j).getInput().equalsIgnoreCase(a)){
 					if(hasData(items.get(j).getItem())){
 						for(int k = 0; k<pi.getSize(); k++){
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								if(checkData(pi.getItem(k).getData().getData(), items.get(j).getDamage())){
 									clear.remove((Integer)k);
 									if(!successful.contains(items.get(j).getOutput())){
 										successful.add(items.get(j).getOutput());
 									}
 								}
 							}
 						}
 					}else{
 						for(int k = 0; k<pi.getSize();k++){
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								clear.remove((Integer)k);
 								if(!successful.contains(items.get(j).getOutput())){
 									successful.add(items.get(j).getOutput());
 								}
 							}
 						}
 					}
 					break;
 				}
 			}
 		}
 
 		for(Integer slot : clear){
 			pi.clear(slot);
 		}
 		StringBuilder output = new StringBuilder();
 		if(successful.size() != 1){
 			for(int i = 0; i<successful.size()-1; i++){
 				output.append(successful.get(i) + ", ");
 			}
 			output.append("and " + successful.get(successful.size()-1));
 		}else{
 			output.append(successful.get(0));
 		}
 		sender.sendMessage("Successfully removed everything except " + output);
 	}
 
 	/**
 	 * clears all the items except for the ones specified by the player.
 	 * @param sender is the player who sent the command
 	 * @param args the list of items to exclude (either in number of name form).
 	 */
 	public void clearExceptRemote(CommandSender sender, Player affected, String[] args) {
 		PlayerInventory pi;
 		if(affected != null)
 			pi = affected.getInventory();
 		else{
 			sender.sendMessage(PREFIX + " Error: player variable was null!");
 			return;
 		}
 		ArrayList<Integer> clear = new ArrayList<Integer>();
 		ArrayList<String> successful = new ArrayList<String>();
 		for(int i = 0; i<pi.getSize(); i++){
 			clear.add(i);
 		}
 		for(String a : args){
 			for(int j = 0;j<items.size(); j++){
 				if(items.get(j).getInput().equalsIgnoreCase(a)){
 					for(int k = 0; k<35; k++){
 						if(hasData(items.get(j).getItem())){
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								if(checkData(pi.getItem(k).getData().getData(), items.get(j).getDamage())){
 									clear.remove(k);
 									if(!successful.contains(items.get(j).getOutput())){
 										successful.add(items.get(j).getOutput());
 									}
 								}
 							}
 						}else{
 							if(pi.getItem(k).getTypeId() == items.get(j).getItem()){
 								clear.remove(k);
 								if(!successful.contains(items.get(j).getOutput())){
 									successful.add(items.get(j).getOutput());
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		for(Integer i : clear){
 			if(pi != null)
 				pi.clear(i);
 		}
 		StringBuilder output = new StringBuilder();
 		if(successful.size() != 1){
 			for(int i = 0; i<successful.size()-1; i++){
 				output.append(successful.get(i) + ", ");
 			}
 			output.append("and " + successful.get(successful.size()-1));
 		}else{
 			output.append(successful.get(0));
 		}
 		sender.sendMessage("Successfully removed everything except " + output);
 	}
 
 	/**
 	 * clears all of the selected items by the player
 	 * @param Sender the player who sent the command
 	 * @param args is the list or item(s) that the user wants to delete from their inventory
 	 */
 	public void clearItem(Player sender, String[] args) {
 		PlayerInventory pi = sender.getInventory();
 		for(String a : args){
 			for(int i = 0; i<items.size(); i++){
 				if(a.equalsIgnoreCase(items.get(i).getInput())){
 					if(!hasData(items.get(i).getItem())){
 						pi.remove(items.get(i).getItem());
 					}else{
 						for(int j = 0; j<pi.getSize(); j++){
 							ItemStack IS = pi.getItem(j);
 							if(hasData(IS.getTypeId())){
 								if(checkData(IS.getData().getData(), items.get(i).getDamage())){
 									pi.clear(j);
 								}
 							}
 						}
 					}
 					sender.sendMessage("Cleared all " + items.get(i).getOutput());
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * clears all of the selected items by the player
 	 * @param Sender is the player who sent the command.
 	 * @param affected is the player who's items are being removed
 	 * @param args is the list or item(s) that the user wants to delete from their inventory
 	 */
 	public void clearItemRemote(CommandSender sender, Player affected, String[] args) {
 		PlayerInventory pi;
 		if(affected != null)
 			pi = affected.getInventory();
 		else{
 			sender.sendMessage(PREFIX + " Error: player variable was null!");
 			return;
 		}
 		for(String a : args){
 			for(int i = 0; i<items.size(); i++){
 				if(a.equalsIgnoreCase(items.get(i).getInput())){
 					if(!hasData(items.get(i).getItem())){
 						pi.remove(items.get(i).getItem());
 					}else{
 						for(int j = 0; j<pi.getSize(); j++){
 							ItemStack IS = pi.getItem(j);
 							if(hasData(IS.getTypeId())){
 								if(checkData(IS.getData().getData(), items.get(i).getDamage())){
 									pi.clear(j);
 								}
 							}
 						}
 					}
 					sender.sendMessage("Cleared all " + items.get(i).getOutput());
 					break;
 				}
 			}
 		}
 	}
 
 	private void clearArmor(Player sender){
 		sender.getInventory().setBoots(null);
 		sender.getInventory().setChestplate(null);
 		sender.getInventory().setHelmet(null);
 		sender.getInventory().setLeggings(null);
 		sender.sendMessage("Armor removed");
 	}
 
 	private void clearArmorRemote(CommandSender sender, Player affected){
 		affected.getInventory().setBoots(null);
 		affected.getInventory().setChestplate(null);
 		affected.getInventory().setHelmet(null);
 		affected.getInventory().setLeggings(null);
 		sender.sendMessage(affected.getName() + " has had his armor removed by you");
 	}
 
 	/**
 	 * Checks the data to see if the two pieces of data given are the same
 	 * @param data is the data of the item from the inventory
 	 * @param damage is the data we want it to be
 	 */
 	private boolean checkData(byte data, int damage) {
 		Byte testByte = data;
 		return testByte.intValue() == damage;
 	}
 	/**
 	 * checks against known items which have data
 	 * @param the item ID used to determine if it has data (or can have data).
 	 */
 	private boolean hasData(int ID) {
 		switch(ID){
 		case 17:
 		case 18:
 		case 26:
 		case 35:
 		case 43:
 		case 92:
 		case 93:
 		case 263:
 		case 351:
 		case 373:
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * A method for getting the old permissions handler for the permissions plugin, its in here for legacy support
 	 */
 	private void getPerm() {
 		Plugin perm = server.getPluginManager().getPlugin("Permissions");
 		if (ph == null) {
 			if (perm != null) {
 				ph = ((Permissions) perm).getHandler();
 				log.info(PREFIX + " Permission system detected");
 			} else {
 				log.warning(PREFIX + " No permissions system detected, elevated commands dafaulting to OP");
 			}
 		}
 	}
 
 	/** 
 	 * A method for getting the version of the items.csv file.
 	 */
 	private void getDBV(){
 		try {
 			FileReader reader = new FileReader(itemFile);
 			BufferedReader in = new BufferedReader(reader);
 			String line = in.readLine();
 			DBV = line;
 		}catch(Exception e){
 		}
 	}
 
 	/**
 	 * This method loads the csv into the ClearItemHolder object
 	 */
 	private void loadItems(){
 		int i=1;
 		try {
 			FileReader reader = new FileReader(itemFile);
 			BufferedReader in = new BufferedReader(reader);
 			in.readLine();	//version line
 			String line = in.readLine();
 			while(line != null){
 				String[] args = line.split(",");
 				int item = Integer.parseInt(args[1].trim());
 				int damage = Integer.parseInt(args[2].trim());
 				ClearItemHolder newItem = new ClearItemHolder(args[0], item, damage, args[3]);
 				items.add(newItem);
 				line = in.readLine();
 				i++;
 			}
 		} catch (FileNotFoundException e) {
 			log.warning(ChatColor.RED + "You have not downloaded the items.csv, make sure to download the new one as it is necessary for the 1.8 update");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch(NumberFormatException e){
 			log.warning("If you did NOT edit the items.csv tell wwsean08 in the bukkit forums that there is an error at line " + i+1);
 		}
 	}
 	/**
 	 * creates the config.yml if one doesn't exist or is outdated
 	 * 
 	 */
 	private void createConfig(){
 		try{
 			if(configFile.exists()){
 				config = YamlConfiguration.loadConfiguration(configFile);
 				if(!config.contains("autoupdate"))
 					config.set("autoupdate", true);
 				if(!config.contains("superPerms"))
 					config.set("superPerms", true);
 				config.save(configFile);
 			}else{
 				if(configFile.createNewFile()){
 					config = YamlConfiguration.loadConfiguration(configFile);
 					config.set("autoupdate", true);
 					config.set("superPerms", true);
 					config.save(configFile);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This initializes the instnace variables in order to clean up the onEnable method
 	 */
 	private void initVariables() {
 		configFile = new File(this.getDataFolder() + File.separator + "config.yml");
 		itemFile = new File(this.getDataFolder() + File.separator + "items.csv");
 		pl = new ClearPlayerListener(this);
 		originalInventory = new HashMap<Player, ItemStack[]>();
 		server = Bukkit.getServer();
 		pm = server.getPluginManager();
 	}
 
 	/**
 	 * This method runs the auto-updater, seperated it from the onEnable as part of code cleanup
 	 */
 	private void autoUpdate() {
 		String name = NAME;
 		String version = VERSION;
 		String checklocation = "http://dcp.wwsean08.com/check.php";
 		String downloadlocation = "http://dcp.wwsean08.com/dl.php";
 		String logprefix = PREFIX;
 		OKUpdater.update(name, version, checklocation, downloadlocation, log, logprefix);
 		//check for items.csv update because they don't mind checking
 		name = "items new";
 		version = DBV;
 		File updatedFile = OKUpdater.update(name, version, checklocation, downloadlocation, log, logprefix);
 		if(updatedFile != null){
 			Path currentPath = updatedFile.toPath();
 			Path newPath = itemFile.toPath();
 			try {
 				System.gc();		//hate doing that but it prevents the file being in use and causing exceptions to be thrown
 				Files.move(currentPath, newPath, REPLACE_EXISTING);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			updatedFile.delete();
 		}		
 	}
 }
