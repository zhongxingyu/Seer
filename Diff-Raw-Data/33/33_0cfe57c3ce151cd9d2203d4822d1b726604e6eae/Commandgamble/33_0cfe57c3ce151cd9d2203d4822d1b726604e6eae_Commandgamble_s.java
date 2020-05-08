 package net.openprog.gambler.command;
 
 import java.util.Random;
 
 import net.openprog.gambler.Gambler;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commandgamble implements CommandExecutor {
 	private Gambler plugin;
 	public Commandgamble(Gambler plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (cmd.getName().equalsIgnoreCase("gamble")) {
 			if (args.length == 1) {
 				if (player == null) {
 					sender.sendMessage(Gambler.errorPrefix + "Only players can gamble!");
 					return false;
 				} else {
 					// check player balance
 					int playerbalance = (int) Gambler.econ.getBalance(player.getName());
 					if (isInteger(args[0]) == false) {
 						sender.sendMessage(Gambler.errorPrefix + "Your gambling amount must be a valid number!");
 						return false;
 					}
 					int bettingamount = Integer.parseInt(args[0]);
 					
 					if (bettingamount > plugin.getConfig().getInt("max-bet")) {
 						sender.sendMessage(Gambler.errorPrefix + "The amount you have tried to bet is above the configured server maximum for betting, which is " + plugin.getConfig().getInt("max-bet") + ".");
 						return false;
 					}
 					
					if (bettingamount < playerbalance) {
 						Gambler.econ.withdrawPlayer(player.getName(), bettingamount);
 						sender.sendMessage(Gambler.chatPrefix + bettingamount + " has been removed from your balance. Betting...");
 						
						if (gamble()) {
 							double winnings = bettingamount * 2;
 							Gambler.econ.depositPlayer(player.getName(), winnings);
 							sender.sendMessage(Gambler.chatPrefix + "Congratulations! You have won! " + winnings + " has been added to your balance!");
 						} else {
 							sender.sendMessage(Gambler.chatPrefix + "Sorry, you didn't win. :( Better luck next time!");
 						}
 					} else {
 						sender.sendMessage(Gambler.errorPrefix + "You don't have enough money!");
 						return false;
 					}
 				}
 			} else {
 				sender.sendMessage(Gambler.errorPrefix + "Incorrect amount of arguments. Usage: /gamble <amount>");
 				return false;
 			}
 			
 		}
 		return true;
 	}
 	
 	public static boolean isInteger(String i) {
 		try {
 			Integer.parseInt(i);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
	
	public boolean gamble() {
		Random gen = new Random();
		int drm = plugin.getConfig().getInt("dicerollmax") - 1;
		int randomInt = gen.nextInt(drm);
		
		if (randomInt > plugin.getConfig().getInt("winning-diceroll-threshold")) {
			return true;
		} else {
			return false;
		}
	}
 
 }
