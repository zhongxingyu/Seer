 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mmo.Money;
 
 import java.util.ArrayList;
 import org.bukkit.util.config.Configuration;
 
 /**
  *
  * @author Xaymar
  */
 public class Money {
 
 	protected static MMOMoney		plugin;
 	protected static Configuration		cfg;
 	protected static String			templateNoPermission			= "&cYou don't have permission to do this!";
 	protected static String			templateSyntaxError			= "&fSyntax is &6/%s %s";
 	protected static String			templateAccountInvalid			= "&fAccount '%s' does not exist.";
 	protected static String			templateGetOwn				= "&fYou hold onto %s %s.";
 	protected static String			templateGetOther			= "&f%s holds onto %s %s.";
 	protected static String			templateSetOwn				= "&fYou now hold onto %s %s.";
 	protected static String			templateSetOther			= "&f%s now holds onto %s %s.";
 	protected static String			templateDrop				= "&fYou dropped %s %s.";
 	protected static String			templateTake				= "&fYou took %s %s from %s.";
 	protected static String			templateGive				= "&fYou gave %s %s %s.";
 	protected static ArrayList<Account>	usedAccounts				= new ArrayList<Account>();
 	protected static long			defaultAccountMoney			= 0;
 	
 	public Account getAccount(String name) {
 		MoneyDB dbAccount = plugin.getDatabase().find(MoneyDB.class).where().ieq("owner", name).findUnique();
 		if (dbAccount != null) {
 			for (Account account : usedAccounts) { //If ebean is persistent, every entry in the Database has exactly one pointer.
 				if (account.getAccount() == dbAccount) { //Which means I can just check if an Account has the Database entry.
 					return account;
 				}
 			}
 			Account userAccount = new Account();
 			userAccount.setAccount(dbAccount);
 			userAccount.setChanged(true);
 			usedAccounts.add(userAccount);
 			return userAccount;
 		} else {
 			return null;
 		}
 	}
 	
 	public Account createAccount(String name) {
 		return createAccount(name, defaultAccountMoney);
 	}
 	
 	public Account createAccount(String name, long money) {
 		Account newAccount = getAccount(name);
 		if (newAccount == null) {
 			newAccount = new Account();
 			newAccount.setAccount(new MoneyDB());
 			newAccount.setOwner(name.toLowerCase());
 		}
 		newAccount.setMoneyAmount(money);
 		return newAccount;
 	}
 	
 	public void deleteAccount(Account account) {
 		plugin.getDatabase().delete(account.getAccount());
 	}
 }
