 package me.greatman.Craftconomy.commands.bank;
 
 import me.greatman.Craftconomy.Account;
 import me.greatman.Craftconomy.AccountHandler;
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.commands.BaseCommand;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class BankDepositCommand extends BaseCommand{
 	
 	public BankDepositCommand() {
 		this.command.add("deposit");
 		this.requiredParameters.add("Amount");
 		permFlag = ("Craftconomy.bank.deposit");
 		helpDescription = "Deposit money in your bank account";
 	}
 	
 	public void perform() {
 		double amount;
 		if (AccountHandler.exists((Player)sender))
 		{
 			Account receiverAccount = AccountHandler.getAccount((Player)sender);
 			if (Craftconomy.isValidAmount(this.parameters.get(0)))
 			{
 				amount = Double.parseDouble(this.parameters.get(0));
 				if (receiverAccount.hasEnough(amount))
 				{
 					receiverAccount.substractMoney(amount);
 					receiverAccount.getBank().addMoney(amount);
 					sendMessage("You put " + ChatColor.WHITE + Craftconomy.format(amount) + " into your bank account!");
 				}
				else
					sendMessage(ChatColor.RED + "You don't have " + ChatColor.WHITE + Craftconomy.format(amount) + ChatColor.GREEN + "!");
 			}
 			else
 				sendMessage(ChatColor.RED + "Positive number expected. Received something else.");
 			
 		}
 	}
 }
