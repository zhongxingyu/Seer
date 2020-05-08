 package org.github.craftfortress2;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 import java.util.ArrayList;
 public class CFCommandExecutor implements CommandExecutor {
 	int count = 0;
	ArrayList<String> names = new ArrayList<String>();
	ArrayList<String> teams = new ArrayList<String>();
 	ArrayList<String> classes = new ArrayList<String>();
 	private CraftFortress2 cf2;
 	public CFCommandExecutor(CraftFortress2 cf2) {
 		this.cf2 = cf2;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 			Player player = null;
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			}
 			if (cmd.getName().equalsIgnoreCase("cfstart")) {
 				if (sender.hasPermission("cf.start")) {
 					CFStart.startGame();
 					return true;
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 			}
 			if (cmd.getName().equalsIgnoreCase("cfend")) {
 				if (sender.hasPermission("cf.end")) {
 					CFEnd.endGame();
 					return true;
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfhelp")) {
 				if (sender.hasPermission("cf.help")) {
 					sender.sendMessage("CRAFT FORTRESS 2 HELP");
 					sender.sendMessage("/cfstart - force start a game of CraftFortress");
 					sender.sendMessage("/cfend - force end a game of CraftFortress");
 					sender.sendMessage("/cfjoin <team> <class> - join a team and class.");
 					sender.sendMessage("/cfclass - change your class.");
 					return true;
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfjoin")) {
 				if (sender.hasPermission("cf.join")) {
 					if (args.length > 1 && args.length < 3) {
 						if (args[0].equalsIgnoreCase("blue")) {
 							if (!args[1].equalsIgnoreCase("scout") || !args[1].equalsIgnoreCase("soldier") || !args[1].equalsIgnoreCase("pyro") || !args[1].equalsIgnoreCase("demoman")
 									|| !args[1].equalsIgnoreCase("heavy") || !args[1].equalsIgnoreCase("engineer") || !args[1].equalsIgnoreCase("sniper")
 									|| !args[1].equalsIgnoreCase("medic") || !args[1].equalsIgnoreCase("spy")) {
 								sender.sendMessage("That is not a valid class!");
 								return false;
 							} else {
 								saveInfo(sender, "blue", args[1]);
 								return true;
 							}
 						} else if (args[0].equalsIgnoreCase("red")) {
 							if (!args[1].equalsIgnoreCase("scout") || !args[1].equalsIgnoreCase("soldier") || !args[1].equalsIgnoreCase("pyro") || !args[1].equalsIgnoreCase("demoman")
 									|| !args[1].equalsIgnoreCase("heavy") || !args[1].equalsIgnoreCase("engineer") || !args[1].equalsIgnoreCase("sniper")
 									|| !args[1].equalsIgnoreCase("medic") || !args[1].equalsIgnoreCase("spy")) {
 								sender.sendMessage("That is not a valid class!");
 								return false;
 							} else {
 								saveInfo(sender, "red", args[1]);
 								return true;
 							}
 					} else if (args.length > 3) {
 						sender.sendMessage("Too many arguments!");
 						return false;
 					} else if (args.length < 1) {
 						sender.sendMessage("Not enough arguments!");
 						return false;
 					}
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 				return false;
 			}
 			if(cmd.getName().equalsIgnoreCase("cfclass")) {
 				if (sender.hasPermission("cf.class")) {
 					if (args.length > 0 && args.length < 2) {
 						if (!args[1].equalsIgnoreCase("scout") || !args[1].equalsIgnoreCase("soldier") || !args[1].equalsIgnoreCase("pyro") || !args[1].equalsIgnoreCase("demoman")
 								|| !args[1].equalsIgnoreCase("heavy") || !args[1].equalsIgnoreCase("engineer") || !args[1].equalsIgnoreCase("sniper")
 								|| !args[1].equalsIgnoreCase("medic") || !args[1].equalsIgnoreCase("spy")) {
 							sender.sendMessage("That's not a valid class!");
 							return false;
 						} else {
 							changeClass(sender, args[1]);
 							return true;
 						}
 					} else if (args.length > 3) {
 						sender.sendMessage("Too many arguments!");
 						return false;
 					} else if (args.length < 1) {
 						sender.sendMessage("Not enough arguments!");
 						return false;
 					}
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 			}
 			return false;
 		}
 			return false;
 	}
 	public void saveInfo(CommandSender sender, String team, String cls) { //saves player names, teams, and classes
 		if (names.contains(sender.toString())) {
 			sender.sendMessage("You already joined the game!");
 			return;
 		}
 		names.add(sender.getName());
 		teams.add(team);
 		classes.add(cls);
 		sender.sendMessage("You joined team " + team + " as " + cls);
 	}
 	public void changeClass(CommandSender sender, String cls) { //Changes your class
 		int index = names.lastIndexOf(sender.toString());
 		if (index == -1) {
 			sender.sendMessage("You did not join the game!");
 			return;
 		}
 		classes.set(index, cls);
 		sender.sendMessage("Your class has been changed to " + cls);
 	}
 	public String getClass(Player player) {
 		int index = names.lastIndexOf(player.toString());
 		if (index == -1) {
 			return "Player did not join the game";
 		}
 		return classes.get(index);
 	}
 	public String getName(Player player) {
 		int index = names.lastIndexOf(player.toString());
 		if (index == -1) {
 			return "Player did not join the game";
 		}
 		return names.get(index);
 	}
	public String getTeam(Player player) {
 		int index = names.lastIndexOf(player.toString());
 		if (index == -1) {
 			return "Player did not join the game";
 		}
 		return teams.get(index);
 	}
 }
