 package com.asylumsw.bukkit.waypoints;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author jonathan
  */
 public class Waypoints extends JavaPlugin {
 	public static Server serverInstance;
 	public final static String DATABASE = "jdbc:sqlite:waypoints.db";
 	
 	@Override
 	public void onEnable() {
 		serverInstance = this.getServer();
 		Homes.loadHomes();
 		Gates.loadGates();
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		System.out.println("Waypoints Disabled.");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if( !(sender instanceof Player) ) return false;
 		if( !cmd.getName().equalsIgnoreCase("wp") ) return false;
 		if( 1 > args.length ) return false;
 
 		String action = args[0];
 
 		if( action.equalsIgnoreCase("home") ) {
			if( 2 > args.length ) {
 				if( args[1].equalsIgnoreCase("activate") ) {
 					Homes.setHomePoint((Player)sender);
 					return true;
 				}
 				else if( args[1].equalsIgnoreCase("remove") ) {
 					Homes.unsetHomePoint((Player)sender);
 					return true;
 				}
 				return false;
 			}
 			else {
 				Homes.sendPlayerHome((Player)sender, false, false);
 				return true;
 			}
 		}
		else if(action.equalsIgnoreCase("gate") && 2 > args.length ) {
 			String gateAction = args[1];
 			if( gateAction.equalsIgnoreCase("list") ) {
 				Gates.listPlayerGates((Player)sender);
 				return true;
 			}
 			else if( gateAction.equalsIgnoreCase("remove") ) {
 				sender.sendMessage(ChatColor.RED+"Error: Removeing gates is not yet implemented.");
 				return true;
 			}
 			else if( gateAction.equalsIgnoreCase("activate") ) {
				if( 3 > args.length ) {
 					sender.sendMessage(ChatColor.RED + "Error: Must supply gate name.");
 					return false;
 				}
 				Gates.activateGate((Player)sender, args[2]);
 				return true;
 			}
 			else {
 				Gates.sendPlayerToGate((Player)sender, gateAction);
 			}
 		}
 		
 		return false;
 	}
 
 	public static void warpPlayerTo(Player player, Location loc, int delay) {
 		int sleepTime = 0;
 		while (sleepTime < delay) {
 			try {
 				Thread.sleep(1000);
 				player.sendMessage(ChatColor.GRAY+"* Teleport in " + (delay - sleepTime));
 				sleepTime += 1;
 			}
 			catch (InterruptedException ex) {
 			}
 		}
 
 		player.teleportTo(loc);
 	}
 
 }
