 package me.greatman.Craftconomy.commands.bank;
 
 import me.greatman.Craftconomy.Account;
 import me.greatman.Craftconomy.AccountHandler;
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.commands.BaseCommand;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class BankWithdrawCommand extends BaseCommand{
 	
 	public BankWithdrawCommand() {
 		this.command.add("withdraw");
 		this.requiredParameters.add("Player Name");
 		this.requiredParameters.add("Amount");
 		permFlag = ("Craftconomy.bank.withdraw");
 		helpDescription = "Withdraw money from a bank account";
 	}
 	
 	public void perform() {
 		double amount;
 		if (AccountHandler.exists((Player) sender))
 		{
 			Account receiverAccount = AccountHandler.getAccount((Player) sender);
 			if (Craftconomy.isValidAmount(this.parameters.get(0)))
 			{
 				amount = Double.parseDouble(this.parameters.get(0));
 				if (receiverAccount.getBank().hasEnough(amount))
 				{
 					receiverAccount.getBank().substractMoney(amount);
 					receiverAccount.addMoney(amount);
 					sendMessage("You put " + ChatColor.WHITE + Craftconomy.format(amount) + ChatColor.GREEN + " into your pocket!");
 				}
 			}
 			else
 				sendMessage(ChatColor.RED + "Positive number expected. Received something else.");
 			
 		}
 	}
 }
