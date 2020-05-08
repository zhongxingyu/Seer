 package me.Xiretza.EPsell.Commands;
 
 import org.bukkit.entity.Player;
 
 import me.Xiretza.EPsell.EPsell;
 
 public class CommandEPS {
 
 	EPsell pl;
 	Player p;
 	String[] args;
 
 	public CommandEPS(EPsell pl, Player p, String[] args) {
 
 		this.pl = pl;
 		this.args = args;
 		this.p = p;
 	}
 
 	public boolean execute() {
 		
 		if (args.length == 0 || args.length > 1) {
 			p.sendMessage(pl.nameP + "Please do /eps <amount|all>");
 			return true;
 		}
 
 		if (args.length == 1) {
 
 			if (args[0].equalsIgnoreCase("all")) {
 
 				if (p.getGameMode().getValue() == 1 && p.getLevel() == 0) {
 
 					p.sendMessage(pl.nameP
 							+ "You dont have unlimited XP in creative mode.");
 					p.sendMessage(pl.nameP
 							+ "Please use /eps <amount> to sell a specific amount of XP.");
 					return true;
 				}
 
 				if (p.getLevel() == 0) {
 
 					p.sendMessage(pl.nameP + "You dont have any XP to sell.");
 					return true;
 				}
 
 				int lvl = p.getLevel();
 
 				pl.addMoney(p.getName(), new Double(lvl));
 
 				return true;
 
 			} else {
 
 				int lvl;
 
 				try {
 					lvl = Integer.parseInt(args[0]);
 				} catch (NumberFormatException e) {
 					p.sendMessage(pl.nameP + "You have to enter a number!");
 					return false;
 				}
 
 				if (lvl == 0) {
 
 					p.sendMessage(pl.nameP + "You cant sell 0 XP.");
 					return true;
 				}
 
				if (lvl < 0) {
					p.sendMessage(pl.nameP + "You can't provide a negative number");
					return true;
				}

 				if (p.getLevel() < lvl && p.getGameMode().getValue() != 1) {
 
 					p.sendMessage(pl.nameP + "Not enough XP!");
 					return true;
 				}
 
 				pl.addMoney(p.getName(), new Double(lvl));
 
 			}
 		}
 		return true;
 	}
 }
