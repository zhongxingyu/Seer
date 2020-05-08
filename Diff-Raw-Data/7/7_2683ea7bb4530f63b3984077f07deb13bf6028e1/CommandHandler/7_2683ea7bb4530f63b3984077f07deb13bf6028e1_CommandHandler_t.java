 package com.m0pt0pmatt.bettereconomy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 public class CommandHandler {
 	
 	/**
 	 * command handler. handles all commands
 	 */
 	public static boolean commands(CommandSender sender, Command cmd, String label, String[] args){
 		
 		/**
 		 * player wants to deposit money
 		 */
 		if(cmd.getName().equalsIgnoreCase("deposit")){
 			if (args.length == 3){
 				if (!(sender instanceof BlockCommandSender)){
 					return false;
 				}
 				//ADDED 2/21/13 @author Lucas Stuyvesant
 				//deposit [currency] all 
 				if (args[2].equalsIgnoreCase("all")){
 					BetterEconomy.economy.depositAll(Bukkit.getPlayer(args[0]), args[1]);
 					return true;
 				}
 				else{
 					BetterEconomy.economy.deposit(Bukkit.getPlayer(args[0]), args[1],Integer.parseInt(args[2]));
 					return true;	
 				}
 				
 			}
 			if (args.length == 2){
 				if (args[1].equalsIgnoreCase("all")){
 					BetterEconomy.economy.depositAll(sender, args[0]);
 					return true;
 				}
 				else{
 					BetterEconomy.economy.deposit(sender, args[0],Integer.parseInt(args[1]));
 					return true;
 				}
 			}
 			if (args.length == 1){
 				if(args[0].equalsIgnoreCase("all")){
 					BetterEconomy.economy.depositEverything(sender);
 					return true;
 				}
 				else{
 					sender.sendMessage("Wrong number of arguments");
 					return false;
 				}
 			}
 			else{
 				sender.sendMessage("Wrong number of arguments.");
 				return false;
 			}
 		}
 		
 		/**
 		 * player wants to withdraw currency
 		 */
 		if(cmd.getName().equalsIgnoreCase("withdraw")){
 			if (args.length == 3){
 				if (!(sender instanceof BlockCommandSender)){
 					return false;
 				}
 				if(args[2].equalsIgnoreCase("all")){
 					BetterEconomy.economy.greedyWithdraw(Bukkit.getPlayer(args[0]), BetterEconomy.economy.getCurrency(args[1]));
 					return true;
 				}
 				else{
 					BetterEconomy.economy.withdraw(Bukkit.getPlayer(args[0]), args[1],Integer.parseInt(args[2]));
 					return true;
 				}
 			}
 			if (args.length == 2){
 				if(args[1].equalsIgnoreCase("all")){
 					BetterEconomy.economy.greedyWithdraw(sender, BetterEconomy.economy.getCurrency(args[0]));
 					return true;
 				}
 				else{
 					BetterEconomy.economy.withdraw(sender, args[0],Integer.parseInt(args[1]));
 					return true;
 				}
 			}
 			if (args.length == 1){
 				if(args[0].equalsIgnoreCase("all")){
 					BetterEconomy.economy.withdrawAll(sender);
 					return true;
 				}
 				else{
 					sender.sendMessage("Wrong number of arguments");
 					return false;
 				}
 			}
 			else{
 				sender.sendMessage("Wrong number of arguments.");
 				return false;
 			}
 		}
 		
 		/**
 		 * player wants to see how much wealth they are carrying
 		 */
 		if(cmd.getName().equalsIgnoreCase("wealth")){
 			if (args.length != 0){
 				sender.sendMessage("Wrong number of arguments.");
 				return false;
 			}
 			else{
 				BetterEconomy.economy.calculateWealth(sender);
 			}
 			return true;
 		}
 		
 		/**
 		 * player wanted to check the value of something
 		 */
 		if(cmd.getName().equalsIgnoreCase("value")){
 			if (args.length == 1){
 				BetterEconomy.economy.checkValue(sender, args[0], 1);
 				return true;
 			}
 			if (args.length == 2){
 				BetterEconomy.economy.checkValue(sender, args[0], Integer.parseInt(args[1]));
 				return true;
 			}
 			
 			sender.sendMessage("Wrong number of arguments.");
 			return false;
 		}
 		
 		/**
 		 * player wants to check their balance
 		 */
 		if(cmd.getName().equalsIgnoreCase("money")){
 			if (args.length == 0){
 				BetterEconomy.economy.showBalance(sender);
 				return true;
 			}
 			
 			sender.sendMessage("Wrong number of arguments.");
 			return false;
 		}
 		
 		/**
 		 * server wants to set a players balance
 		 */
 		if(cmd.getName().equalsIgnoreCase("setbalance")){
 			if (args.length == 2){
 				BetterEconomy.economy.setBalance(sender, args[0], Double.parseDouble(args[1]));
 				return true;
 			}
 			
 			sender.sendMessage("Wrong number of arguments.");
 			return false;
 		}
 		
 		/**
 		 * player wants to give another player a certain amount
 		 */
 		if(cmd.getName().equalsIgnoreCase("pay")){
 			if (args.length == 2){
				double d;
				if((d = Double.parseDouble(args[1])) <= 0){
					sender.sendMessage("Invalid payment amount.");
					return false;
				}
				BetterEconomy.economy.pay(sender, args[0], d);
 				return true;
 			}
 			
 			sender.sendMessage("Wrong number of arguments.");
 			return false;
 		}
 		
 		/**
 		 * player wants to see the top accounts on the server
 		 */
 		if(cmd.getName().equalsIgnoreCase("top")){
 			if (args.length == 1){
 				BetterEconomy.economy.top(sender, Integer.parseInt(args[0]));
 				return true;
 			}
 			
 			sender.sendMessage("Wrong number of arguments.");
 			return false;
 		}	
 		
 		return false;
 	}
 }
