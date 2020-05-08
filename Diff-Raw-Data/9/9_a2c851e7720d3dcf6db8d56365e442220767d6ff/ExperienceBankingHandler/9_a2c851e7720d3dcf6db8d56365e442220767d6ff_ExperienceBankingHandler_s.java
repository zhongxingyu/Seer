 package de.hotmail.gurkilein.bankcraft.banking;
 
 import org.bukkit.entity.Player;
 
 import de.hotmail.gurkilein.bankcraft.Bankcraft;
 
 public class ExperienceBankingHandler implements BankingHandler<Integer>{
 
 	private Bankcraft bankcraft;
 
 	public ExperienceBankingHandler(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 	}
 	
 	@Override
 	public boolean transferFromPocketToAccount(Player pocketOwner,
 			String accountOwner, Integer amount, Player observer) {
 		if (amount <0) return transferFromAccountToPocket(accountOwner, pocketOwner, -amount, observer);
 		
 		if (ExperienceBukkitHandler.getTotalExperience(pocketOwner) >= amount) {
 			if (bankcraft.getExperienceDatabaseInterface().getBalance(accountOwner)<= Integer.parseInt(bankcraft.getConfigurationHandler().getString("general.maxBankLimitXp"))-amount) {
 				ExperienceBukkitHandler.removeExperienceFromPocket(pocketOwner, amount);
 				bankcraft.getExperienceDatabaseInterface().addToAccount(accountOwner, amount);
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.depositedSuccessfullyXp", amount+"", accountOwner);
 				if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumXpInAccount", amount+"", accountOwner);
 			}
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughXpInPoket", amount+"", pocketOwner.getName());
 		}
 		return false;
 	}
 
 	@Override
 	public boolean transferFromAccountToPocket(String accountOwner,
 			   Player pocketOwner, Integer amount, Player observer) {
 		if (amount <0) return transferFromPocketToAccount(pocketOwner, accountOwner, -amount, observer);
 		
 		if (bankcraft.getExperienceDatabaseInterface().getBalance(accountOwner)+bankcraft.getConfigurationHandler().getLoanLimitForPlayer(accountOwner, this) >= amount) {
			if (ExperienceBukkitHandler.getTotalExperience(pocketOwner)<= Integer.parseInt(bankcraft.getConfigurationHandler().getString("general.maxBankLimitXp"))-amount) {
 			    bankcraft.getExperienceDatabaseInterface().removeFromAccount(accountOwner, amount);
 			    ExperienceBukkitHandler.addExperienceToPocket(pocketOwner, amount);
 			    bankcraft.getConfigurationHandler().printMessage(observer, "message.withdrewSuccessfullyXp", amount+"", accountOwner);
 			    if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 			    return true;
 			   } else {
 			    bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumXpInPocket", amount+"", pocketOwner.getName());
 			   }
 
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughXpInAccount", amount+"", accountOwner);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean transferFromAccountToAccount(String givingPlayer,
 			String gettingPlayer, Integer amount, Player observer) {
 		if (amount <0) return transferFromAccountToAccount(gettingPlayer, givingPlayer, -amount, observer);
 		
 		if (!bankcraft.getExperienceDatabaseInterface().hasAccount(gettingPlayer)) {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.accountDoesNotExist", amount+"", gettingPlayer);
 			return false;
 		}
 		
 		if (bankcraft.getExperienceDatabaseInterface().getBalance(givingPlayer)+bankcraft.getConfigurationHandler().getLoanLimitForPlayer(givingPlayer, this) >= amount) {
 			if (bankcraft.getExperienceDatabaseInterface().getBalance(gettingPlayer)<= Integer.parseInt(bankcraft.getConfigurationHandler().getString("general.maxBankLimitXp"))-amount) {
 				bankcraft.getExperienceDatabaseInterface().removeFromAccount(givingPlayer, amount);
 				bankcraft.getExperienceDatabaseInterface().addToAccount(gettingPlayer, amount);
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.transferedSuccessfullyXp", amount+"", gettingPlayer);
 				if (bankcraft.getServer().getPlayer(givingPlayer) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(givingPlayer));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumXpInAccount", amount+"", gettingPlayer);
 			}
 
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughXpInAccount", amount+"", givingPlayer);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean grantInterests(Player observer) {
 		String messageKey;
 		for (String accountName: bankcraft.getExperienceDatabaseInterface().getAccounts()) {
 			
 			double interest = bankcraft.getConfigurationHandler().getInterestForPlayer(accountName, this, bankcraft.getExperienceDatabaseInterface().getBalance(accountName)<0);
 			
 			
 			
 			int amount = (int)(interest*bankcraft.getExperienceDatabaseInterface().getBalance(accountName));
 			
 			if (bankcraft.getExperienceDatabaseInterface().getBalance(accountName)<= Integer.parseInt(bankcraft.getConfigurationHandler().getString("general.maxBankLimitXp"))-amount) {
 				bankcraft.getExperienceDatabaseInterface().addToAccount(accountName, amount);
 				messageKey = "message.grantedInterestOnXp";
 			} else {
 				messageKey = "message.couldNotGrantInterestOnXp";
 			}
 			Player player;
 			if ((player =bankcraft.getServer().getPlayer(accountName)) != null && (bankcraft.getConfigurationHandler().getString("interest.broadcastXp").equals("true") || Bankcraft.perms.has(player, "bankcraft.interest.broadcastmoney"))) {
 				bankcraft.getConfigurationHandler().printMessage(player, messageKey, amount+"", player.getName());
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean depositToAccount(String accountOwner, Integer amount,
 			Player observer) {
 		if (amount <0) return withdrawFromAccount(accountOwner, -amount, observer);
 		
 		if (!bankcraft.getExperienceDatabaseInterface().hasAccount(accountOwner)) {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.accountDoesNotExist", amount+"", accountOwner);
 			return false;
 		}
 		
 			if (bankcraft.getExperienceDatabaseInterface().getBalance(accountOwner)<= Integer.parseInt(bankcraft.getConfigurationHandler().getString("general.maxBankLimitXp"))-amount) {
 				bankcraft.getExperienceDatabaseInterface().addToAccount(accountOwner, amount);
 				if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 				return true;
 			} else {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.reachedMaximumXpInAccount", amount+"", accountOwner);
 			}
 		return false;
 	}
 
 	@Override
 	public boolean withdrawFromAccount(String accountOwner, Integer amount,
 			Player observer) {
 		if (amount <0) return depositToAccount(accountOwner, -amount, observer);
 		
 		if (!bankcraft.getExperienceDatabaseInterface().hasAccount(accountOwner)) {
 				bankcraft.getConfigurationHandler().printMessage(observer, "message.accountDoesNotExist", amount+"", accountOwner);
 				return false;
 			}
 		
 		if (bankcraft.getExperienceDatabaseInterface().getBalance(accountOwner)+bankcraft.getConfigurationHandler().getLoanLimitForPlayer(accountOwner, this) >= amount) {
 			
 			    bankcraft.getExperienceDatabaseInterface().removeFromAccount(accountOwner, amount);
 			    if (bankcraft.getServer().getPlayer(accountOwner) != null) bankcraft.getDebitorHandler().updateDebitorStatus(bankcraft.getServer().getPlayer(accountOwner));
 			    return true;
 		} else {
 			bankcraft.getConfigurationHandler().printMessage(observer, "message.notEnoughXpInAccount", amount+"", accountOwner);
 		}
 		
 		return false;
 	}
 
 
 }
