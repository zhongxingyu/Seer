 package com.resbah.WirelessChests;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WirelessChests extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public void onEnable(){
 		log.info("WirelessChests has been Enabled");
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 	 
 	public void onDisable(){ 
 		this.saveConfig();
 		log.info("WirelessChests has been Disabled");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 	 
 		if (cmd.getName().equalsIgnoreCase("wc")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 	    		Location loc = player.getLocation();
 	    		World w = loc.getWorld();
 	    		loc.setY(loc.getY() + 5);
 	    		Block b = w.getBlockAt(loc);
 	    		b.setTypeId(1);
 				player.sendMessage("Look up!");
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("dchest")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 	    		if(args.length == 2){
 	    			String group = args[1];
 	    			String chest = args[0];
 	    			if(this.getConfig().get("group."+group) != null){
 	    				if(this.getConfig().get("group."+group+"."+chest) != null){
 	    					this.getConfig().set("defaults."+group, chest);
 	    					player.sendMessage("Done!");
 	    					this.saveConfig();
 	    					this.reloadConfig();
 	    				}else{
 	    					player.sendMessage("Chest does not exist!");
 	    				}
 	    			}else{
 	    				player.sendMessage("Group does not exist!");
 	    			}
 	    			
 	    		}
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("sync")){
 			if (player == null) {
 				sender.sendMessage("this command can only be run by a player");
 			} else {
 			if(args.length == 1){
 				String group = args[0];
 				if(this.getConfig().getString("defaults."+group) != null){
 				String dc = this.getConfig().getString("defaults."+group);
 				int dx = this.getConfig().getInt("group."+group+"."+dc+".x");
 				int dy = this.getConfig().getInt("group."+group+"."+dc+".y");
 				int dz = this.getConfig().getInt("group."+group+"."+dc+".z");
 				World dw = player.getWorld();
 				Block db = dw.getBlockAt(dx, dy, dz);
 				int dbty = db.getTypeId();
 				player.sendMessage("World of Block:" +dw);
 				player.sendMessage("Location of block: "+db);
 				player.sendMessage("ID of block: "+dbty);
 				Chest dchst = (Chest)db;
 				if(dbty != 54){
 					player.sendMessage("Error! Main Chest is not located here! Are you sure your in the same world as the chest?");
 				}else{
 					Inventory maininv = dchst.getInventory();
					ItemStack[] mainstack = maininv.getContents();
 				
 				
 					Set<String> keys = this.getConfig().getConfigurationSection("group."+group).getKeys(false);
 					Iterator<String> iter = keys.iterator();
 						while (iter.hasNext()) {
 							String cnam = iter.next();
 							player.sendMessage(iter.next());
 							int x = this.getConfig().getInt("group."+group+"."+cnam+".x");
 							int y = this.getConfig().getInt("group."+group+"."+cnam+".y");
 							int z = this.getConfig().getInt("group."+group+"."+cnam+".z");
 							Location loc = player.getLocation();
 							World w = loc.getWorld();
 							Block b = w.getBlockAt(x, y, z);
 							int bty = b.getTypeId();
 							Chest chst = (Chest)b;
 							
 							if(bty != 54){
 								player.sendMessage("Error! Chest is not located here! Are you sure your in the same world as the chest?");
 								}else{
 								Inventory tchst = chst.getInventory();
								tchst.setContents(mainstack);
 								player.sendMessage(cnam+" Synced with Main Chest "+dc+".");
 						
 					}
 					}
 				}
 				
 				
 				}else{
 					player.sendMessage("Error! You haven't set a Main chest yet. Do so with /dchest [chest name] [group name]");
 				}
 				
 				
 				
 				
 			}
 			}
 			return true;
 		}
 
 		if(cmd.getName().equalsIgnoreCase("namechest")){
 			if(player == null) {
 				sender.sendMessage("This command can only be run by a player.");
 				return false;
 			}else{
 			if(args.length == 2){
 			String chestname = args[0];
 			String groupname = args[1];
 			Location loc = player.getLocation();
 			World w = loc.getWorld();
 			loc.setY(loc.getY() - 1);
 			Block b = w.getBlockAt(loc);
 			b.setType(Material.CHEST);
 			int bn = w.getBlockTypeIdAt(loc);
 			player.sendMessage("Below you is a Chest with the following parameters:");
 			player.sendMessage("Location : " + loc);
 			player.sendMessage("Block : " + b);
 			player.sendMessage("New Type : " + bn);
 			player.sendMessage("Chest Name : " + chestname);
 			this.getConfig().set("chest." + chestname, chestname);
 			if (this.getConfig().get("group." + groupname) == null){
 			this.getConfig().set("group." + groupname, true);
 			}
 			this.getConfig().set("group." + groupname +"."+chestname+".chestname", chestname);
 			this.getConfig().set("group." + groupname +"."+chestname+".x", loc.getBlockX());
 			this.getConfig().set("group." + groupname +"."+chestname+".y", loc.getBlockY());
 			this.getConfig().set("group." + groupname +"."+chestname+".z", loc.getBlockZ());
 			this.saveConfig();
 			this.reloadConfig();
 			}else{
 				player.sendMessage("Sorry! Your formatting is invalid. Please try again.");
 				return false;
 			}
 		}
 			return true;
 		}
 		return false;
 	}
 	private final WirelessChestsPlayerListener playerListener = new WirelessChestsPlayerListener(this);
 	private final WirelessChestsBlockListener blockListener = new WirelessChestsBlockListener(this);
 
 }
