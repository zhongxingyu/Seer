 package de.hotmail.gurkilein.bankcraft.banking;
 
 import org.bukkit.entity.Player;
 
 import de.hotmail.gurkilein.bankcraft.Bankcraft;
 
 public class MoneyBankingHandler implements BankingHandler<Double>{
 	
 	private Bankcraft bankcraft;
 
 	public MoneyBankingHandler(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 	}
 
 	@Override
 	public boolean transferFromPocketToAccount(Player pocketOwner,
 			String accountOwner, Double amount, Player observer) {
 		if (amount <0) return transferFromAccountToPocket(accountOwner, pocketOwner, -amount, observer);
 		
 		if (Bankcraft.econ.getBalance(pocketOwner.getName()) >= amount && bankcraft.getMoneyDatabaseInterface().getBalance(accountOwner)<= Double.MAX_VALUE-amount) {
 			if (bankcraft.getMoneyDatabaseInterface().getBalance(accountOwner)<= Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.maxBankLimitMoney"))-amount) {
 				Bankcraft.econ.withdrawPlayer(pocketOwner.getName(), amount);
 				bankcraft.getMoneyDatabaseInterface().addToAccount(accountOwner, amount);
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.depositedSuccessfully", amount+"", accountOwner);
 				if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumMoneyInAccount", amount+"", accountOwner);
 			}
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughMoneyInPoket", amount+"", pocketOwner.getName());
 		}
 		return false;
 	}
 
 	@Override
 	public boolean transferFromAccountToPocket(String accountOwner,
 			Player pocketOwner, Double amount, Player observer) {
 		if (amount <0) return transferFromPocketToAccount(pocketOwner, accountOwner, -amount, observer);
 		
 		if (bankcraft.getMoneyDatabaseInterface().getBalance(accountOwner)+bankcraft.getConfigurationHandler().getLoanLimitForPlayer(accountOwner, this) >= amount) {
			if (Bankcraft.econ.getBalance(pocketOwner.getName())<= Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.maxBankLimitMoney"))-amount) {
 				bankcraft.getMoneyDatabaseInterface().removeFromAccount(accountOwner, amount);
 				Bankcraft.econ.depositPlayer(pocketOwner.getName(), amount);
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.withdrewSuccessfully", amount+"", accountOwner);
 				if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumMoneyInPocket", amount+"", pocketOwner.getName());
 			}
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughMoneyInAccount", amount+"", accountOwner);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean transferFromAccountToAccount(String givingPlayer,
 			String gettingPlayer, Double amount, Player observer) {
 		if (amount <0) return transferFromAccountToAccount(gettingPlayer, givingPlayer, -amount, observer);
 		
 		if (!bankcraft.getMoneyDatabaseInterface().hasAccount(gettingPlayer)) {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.accountDoesNotExist", amount+"", gettingPlayer);
 			return false;
 		}
 		if (bankcraft.getMoneyDatabaseInterface().getBalance(givingPlayer)+bankcraft.getConfigurationHandler().getLoanLimitForPlayer(givingPlayer, this) >= amount) {
 			if (bankcraft.getMoneyDatabaseInterface().getBalance(gettingPlayer)<= Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.maxBankLimitMoney"))-amount) {
 				bankcraft.getMoneyDatabaseInterface().removeFromAccount(givingPlayer, amount);
 				bankcraft.getMoneyDatabaseInterface().addToAccount(gettingPlayer, amount);
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.transferedSuccessfully", amount+"", gettingPlayer);
 				if (bankcraft.getServer().getPlayer(givingPlayer) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(givingPlayer));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumMoneyInAccount", amount+"", gettingPlayer);
 			}
 
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughMoneyInAccount", amount+"", givingPlayer);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean grantInterests(Player observer) {
 		String messageKey;
 		for (String accountName: bankcraft.getMoneyDatabaseInterface().getAccounts()) {
 			
 			double interest = bankcraft.getConfigurationHandler().getInterestForPlayer(accountName, this, bankcraft.getMoneyDatabaseInterface().getBalance(accountName)<0);
 			double amount = interest*bankcraft.getMoneyDatabaseInterface().getBalance(accountName);
 			
 			if (bankcraft.getMoneyDatabaseInterface().getBalance(accountName)<= Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.maxBankLimitMoney"))-amount) {
 				bankcraft.getMoneyDatabaseInterface().addToAccount(accountName, amount);
 				messageKey = "message.grantedInterestOnMoney";
 			} else {
 				messageKey = "message.couldNotGrantInterestOnMoney";
 			}
 			Player player;
 			if ((player =bankcraft.getServer().getPlayer(accountName)) != null && (bankcraft.getConfigurationHandler().getString("interest.broadcastMoney").equals("true") || Bankcraft.perms.has(player, "bankcraft.interest.broadcastmoney"))) {
 				bankcraft.getConfigurationHandler().printMessage(player, messageKey, amount+"", player.getName());
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean depositToAccount(String accountOwner, Double amount,
 			Player observer) {
 		if (amount <0) return withdrawFromAccount(accountOwner, -amount, observer);
 		
 		if (!bankcraft.getMoneyDatabaseInterface().hasAccount(accountOwner)) {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.accountDoesNotExist", amount+"", accountOwner);
 			return false;
 		}
 		
 			if (bankcraft.getMoneyDatabaseInterface().getBalance(accountOwner)<= Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.maxBankLimitMoney"))-amount) {
 				bankcraft.getMoneyDatabaseInterface().addToAccount(accountOwner, amount);
 				if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumMoneyInAccount", amount+"", accountOwner);
 			}
 		return false;
 	}
 
 	@Override
 	public boolean withdrawFromAccount(String accountOwner, Double amount,
 			Player observer) {
 		if (amount <0) return depositToAccount(accountOwner, -amount, observer);
 		
 		if (!bankcraft.getMoneyDatabaseInterface().hasAccount(accountOwner)) {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.accountDoesNotExist", amount+"", accountOwner);
 			return false;
 		}
 	
 	if (bankcraft.getMoneyDatabaseInterface().getBalance(accountOwner)+bankcraft.getConfigurationHandler().getLoanLimitForPlayer(accountOwner, this) >= amount) {
 		
 		    bankcraft.getMoneyDatabaseInterface().removeFromAccount(accountOwner, amount);
 		    if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 		    return true;
 	} else {
 		bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughMoneyInAccount", amount+"", accountOwner);
 	}
 	
 	return false;
 	}
 
 
 }
