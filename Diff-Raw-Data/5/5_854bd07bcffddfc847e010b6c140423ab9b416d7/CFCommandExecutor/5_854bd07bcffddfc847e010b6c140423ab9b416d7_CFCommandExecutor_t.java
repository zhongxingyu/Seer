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
 			if(cmd.getName().equalsIgnoreCase("cfscout")){
 				if(sender.hasPermission("cf.scout")){
 					if(args.length > 0 && args.length , 2){
 						sender.sendMessage("You will be a scout")
 					}
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfhelp")) {
 				if (sender.hasPermission("cf.help")) {
 					sender.sendMessage("CRAFT FORTRESS 2 HELP");
 					sender.sendMessage("/cfstart - force start a game of CraftFortress");
 					sender.sendMessage("/cfend - force end a game of CraftFortress");
 					sender.sendMessage("/cfjoin <team> - join a team. Choose from red or blue.");
 					return true;
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfjoin")) {
 				if (sender.hasPermission("cf.join")) {
 					if (args.length > 0 && args.length < 2) {
 						if (args[0] == "blue") {
 							saveInfo(sender, "blue");
 							sender.sendMessage("You joined team blue.");
 							sender.sendMessage("The game will start when all 24 players have joined.");
 							return true;
 						} else if (args[0] == "red") {
 							saveInfo(sender, "red");
 							sender.sendMessage("You joined team red.");
 							sender.sendMessage("The game will start when all 24 players have joined.");
 							return true;
 						} else {
 							sender.sendMessage("That's not a valid team! Valid teams are red and blue.");
 							return false;
 						}
 					} else {
 						sender.sendMessage("Too many arguments!");
 						return false;
 					}
 				} else {
 					sender.sendMessage("You don't have permission!");
 					return false;
 				}
 			}
 			return false;
 		}
 	public void saveInfo(CommandSender sender, String team) { //saves player names and teams
 		names.add(sender.getName());
 		teams.add(team);
 	}
	public void saveClasses(String cls) {
		classes.add(cls);
	}
 }
