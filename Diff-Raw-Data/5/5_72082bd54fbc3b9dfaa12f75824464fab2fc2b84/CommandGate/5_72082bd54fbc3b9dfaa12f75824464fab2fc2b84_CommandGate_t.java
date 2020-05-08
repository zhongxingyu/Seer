 package de.xcraft.engelier.XcraftGate.Commands;
 
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.xcraft.engelier.XcraftGate.XcraftGate;
 import de.xcraft.engelier.XcraftGate.XcraftGateCommandHandler;
 import de.xcraft.engelier.XcraftGate.XcraftGateGate;
 
 public class CommandGate extends XcraftGateCommandHandler {
 
 	public CommandGate(XcraftGate instance) {
 		super(instance);
 	}
 
 	public void printUsage() {
 		player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getNameBrackets() + "by Engelier");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate create <name>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "creates a new gate at your location");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate link <name1> <name2>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "links <name1> to <name2>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate loop <name1> <name2>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "links <name1> to <name2> and vice versa");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate unlink <name>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "removes destination from <name>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate unloop <name1> <name2>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "removes double-link between <name1> and <name2>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate delete <name>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "removes gate <name>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate listsolo" + ChatColor.WHITE + " | " + ChatColor.AQUA + "list gates with no source/destination");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN + "/gate warp <name>" + ChatColor.WHITE + " | " + ChatColor.AQUA + "teleports you to gate <name>");
 	}
 
 	public boolean gateExists(String name) {
 		return plugin.gates.containsKey(name);
 	}
 	
 	public boolean gateExists(Location location) {
 		return plugin.gateLocations.get(plugin.getLocationString(location)) != null;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender instanceof Player) {
 			player = (Player)sender;
 		} else {
 			plugin.log.warning(plugin.getNameBrackets() + " this command cannot be used from the console");
 			return true;
 		}
 		
 		if (!isPermitted("gate", null)) {
 			error("You don't have permission to use this command");
 			return true;
 		}
 		
 		if (args.length == 0) {
 			printUsage();
 		} else if (args[0].equals("create")) {
 			if (!isPermitted("gate", "create")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {			
 				if (gateExists(args[1])) {
 					reply("Gate " + args[1] + " already exists.");
 				} else if (gateExists(player.getLocation())) {
 					reply("There is already a gate at this location!");
 				} else {
 					plugin.createGate(player.getLocation(), args[1]);
 					reply("Gate " + args[1] + " created: " + plugin.getLocationString(player.getLocation()));
 				}
 			}
 		} else if (args[0].equals("link")) {
 			if (!isPermitted("gate", "link")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 3)) {
 				printUsage();
 			} else {			
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else if (!gateExists(args[2])) {
 					reply("Gate " + args[2] + " not found.");
 				} else {
 					plugin.createGateLink(args[1], args[2]);
 					reply("Linked Gate " + args[1] + " to " + args[2]);
 				}
 			}
 		} else if (args[0].equals("loop")) {
 			if (!isPermitted("gate", "link")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 3)) {
 				printUsage();
 			} else {			
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else if (!gateExists(args[2])) {
 					reply("Gate " + args[2] + " not found.");
 				} else {
 					plugin.createGateLoop(args[1], args[2]);
 					reply("Looped Gates " + args[1] + " <=> " + args[2]);
 				}
 			}
 		} else if (args[0].equals("unlink")) {
 			if (!isPermitted("gate", "unlink")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {			
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else {
 					plugin.removeGateLink(args[1]);
 					reply("removed link from gate " + args[1]);
 				}
 			}
 		} else if (args[0].equals("unloop")) {
 			if (!isPermitted("gate", "unlink")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 3)) {
 				printUsage();
 			} else {			
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else if (!gateExists(args[2])) {
 					reply("Gate " + args[2] + " not found.");
 				} else if (!plugin.gates.get(args[1]).gateTarget.equals(args[2]) || !plugin.gates.get(args[2]).gateTarget.equals(args[1])) {
 					reply("Gates " + args[1] + " and " + args[2] + " aren't linked together");
 				} else {
 					plugin.removeGateLoop(args[1], args[2]);
 					reply("removed gate loop " + args[1] + " <=> " + args[2]);
 				}
 			}
 		} else if (args[0].equals("delete")) {
 			if (!isPermitted("gate", "delete")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {			
 			
 				if (!gateExists(args[1])) {
 					reply("Gate not found: " + args[1]);
 				} else {
 					plugin.gateLocations.remove(plugin.getLocationString(plugin.gates.get(args[1]).gateLocation));
 					plugin.gates.remove(args[1]);
 					for (Map.Entry<String, XcraftGateGate> sourceGate: plugin.gates.entrySet()) {
 						if (sourceGate.getValue().gateTarget != null && sourceGate.getValue().gateTarget.equals(args[1])) {
 							sourceGate.getValue().gateTarget = null;
 						}
 					}
 					reply("Gate " + args[1] + " removed.");
 				}
 			}
 		} else if (args[0].equals("listsolo")) {
			if (!isPermitted("gate", "info")) {
 				error("You don't have permission to use this command.");
 			} else {			
 				for (Map.Entry<String, XcraftGateGate> thisGate: plugin.gates.entrySet()) {
 					if (thisGate.getValue().gateTarget == null) {
 						boolean hasSource = false;
 						for (Map.Entry<String, XcraftGateGate> sourceGate: plugin.gates.entrySet()) {
 							if (sourceGate.getValue().gateTarget != null && sourceGate.getValue().gateTarget.equals(thisGate.getValue().gateName)) {
 								hasSource = true;
 							}
 						}	
 						if (!hasSource)
 							reply("Found orphan: " + thisGate.getKey());
 					}
 				}
 			}
 		} else if (args[0].equals("warp")) {
			if (!isPermitted("gate", "warp")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {			
 				if (!gateExists(args[1])) {
 					reply("Gate not found: " + args[1]);
 				} else {
 					plugin.gates.get(args[1]).portHere(player);
 				}
 			}
 		} else {
 			printUsage();
 		}
 		
 		return true;
 	}
 }
