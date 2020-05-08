 package com.github.omwah.SDFEconomy;
 
 import com.github.omwah.SDFEconomy.location.LocationTranslator;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import org.bukkit.Location;
 import org.bukkit.configuration.Configuration;
 
 /**
  * Provides the interface necessary to implement a Vault Economy.
  * Implements most of Vault.Economy interface but does not declare
  * itself as implementing this interface because there is no easy
  * way in Vault to use this class directly without a proxy class.
  */
 public class SDFEconomyAPI {
     private EconomyStorage storage;
     private Configuration config;
     private LocationTranslator locTrans;
     private Logger logger;
     
     public SDFEconomyAPI(Configuration config, EconomyStorage storage, LocationTranslator locationTrans, Logger logger) {
         this.config = config;
         this.storage = storage;
         this.locTrans = locationTrans;
         this.logger = logger;
         
         this.config.addDefault("api.bank.enabled", true);
         this.config.addDefault("api.bank.initial_balance", 0.00);
         this.config.addDefault("api.player.initial_balance", 10.00);
         this.config.addDefault("api.currency.numerical_format", "#,##0.00");
         this.config.addDefault("api.currency.name.plural", "Simoleons");
         this.config.addDefault("api.currency.name.singular", "Simoleon");
     }
     
     /*
      * Whether bank support is enabled
      */
 
     public boolean hasBankSupport() {
         return this.config.getBoolean("api.bank.enabled");
     }
 
     /*
      * Returns -1 since no rounding occurs.
      */
 
     public int fractionalDigits() {
         return -1;
     }
 
     public String format(double amount) {
         String pattern = this.config.getString("api.currency.numerical_format");
         DecimalFormat formatter = new DecimalFormat(pattern);
         String formatted = formatter.format(amount);
         if(amount == 1.0) {
             formatted += " " + currencyNameSingular();
         } else {
             formatted += " " + currencyNamePlural();
         }
         return formatted;
     }
 
     public String currencyNamePlural() {
         return this.config.getString("api.currency.name.plural");
     }
 
     public String currencyNameSingular() {
          return this.config.getString("api.currency.name.singular");
     }
     
     public String getPlayerLocationName(String playerName) {
         if (playerName == null) {
             return null;
         } else {
             return locTrans.getLocationName(playerName);
         }
     }
 
     public boolean validLocationName(String locationName) {
         if (locationName == null) {
             return false;
         } else {
             return locTrans.validLocationName(locationName);
         }
     }
      
     public String getLocationTranslated(Location location) {
         if(location == null) {
             return null;
         } else {
             return locTrans.getLocationName(location);
         }
     }
     
     public List<String> getPlayers(String locationName) {
         if (locationName != null && validLocationName(locationName)) {
             return storage.getPlayerNames(locationName);
         } else {
             return Collections.<String>emptyList();
         }
     }
     
     public boolean createPlayerAccount(String playerName) {
         return createPlayerAccount(playerName, getPlayerLocationName(playerName));
     }
 
     public boolean createPlayerAccount(String playerName, String locationName) {        
         // Make sure an account can not be created without a location
         if(locationName != null && validLocationName(locationName) && !hasAccount(playerName, locationName)) {
            double initialBalance = (playerName.startsWith("town-") ? 0 : config.getDouble("api.player.initial_balance"));
             PlayerAccount account = storage.createPlayerAccount(playerName, locationName, initialBalance);
             return true;
         } else {
             logger.info("Failed to createPlayerAccount for player: " + playerName + " @ " + locationName);
             return false;
         }
     }
     
     public boolean deletePlayerAccount(String playerName, String locationName) {
         if(hasAccount(playerName, locationName)) {
             storage.deletePlayerAccount(playerName, locationName);
             return !hasAccount(playerName, locationName);
         } else {
             logger.info("Failed to deletePlayerAccount for player: " + playerName + " @ " + locationName);
             return false;
         }
     }
         
     public boolean hasAccount(String playerName) {
         return hasAccount(playerName, getPlayerLocationName(playerName));
     }
     
     public boolean hasAccount(String playerName, String locationName) {
         return playerName != null && locationName != null && storage.hasPlayerAccount(playerName, locationName);
     }
 
     public double getBalance(String playerName) {
         return getBalance(playerName, getPlayerLocationName(playerName));
     }
     
     public double getBalance(String playerName, String locationName) {
         double balance = 0.0;
         if (locationName != null && hasAccount(playerName, locationName)) { 
             PlayerAccount account = storage.getPlayerAccount(playerName, locationName);
             balance = account.getBalance();
         } else {
             logger.info("Failed to getBalance for player: " + playerName + " @ " + locationName);
         }
         return balance;
     }    
     
     /* 
      * Normally only accessed for adminstrative purposes
      * @return true if the balance was changed, false otherwise
      */
     public boolean setBalance(String playerName, double amount) {
         return setBalance(playerName, getPlayerLocationName(playerName), amount);
     }
 
     /* Normally only accessed for adminstrative purposes
      * @return true if the balance was changed, false otherwise
      */
     public boolean setBalance(String playerName, String locationName, double amount) {
         if (locationName != null && hasAccount(playerName, locationName)) { 
             PlayerAccount account = storage.getPlayerAccount(playerName, locationName);
             account.setBalance(amount);
             return true;
         } else {
             logger.info("Failed to setBalance for player: " + playerName + " @ " + locationName);
         }
         return false;
     }
 
     public boolean has(String playerName, double amount) {
         return has(playerName, getPlayerLocationName(playerName), amount);
     }
 
     public boolean has(String playerName, String locationName, double amount) {
         return amount >= 0.0 && amount <= getBalance(playerName, locationName);
     }
 
     public EconomyResponse withdrawPlayer(String playerName, double amount) {
         String locationName = getPlayerLocationName(playerName);
         return withdrawPlayer(playerName, amount, locationName);
     }
         
     public EconomyResponse withdrawPlayer(String playerName, double amount, String locationName) {
         
         EconomyResponse response;
         if (locationName != null && hasAccount(playerName, locationName)) {
             if (has(playerName, locationName, amount)) {
                 PlayerAccount account = storage.getPlayerAccount(playerName, locationName);
                 double new_balance = account.withdraw(amount);
                 response = new EconomyResponse(amount, new_balance, ResponseType.SUCCESS, "");
             } else {
                 logger.info("Failed to withdrawPlayer for player: " + playerName + " @ " + locationName + " amount: " + amount);
                 response = new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Player does not have enough money for transaction");
             }
         } else {
             response = new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Location name is invalid or player does not have an account");
         }
         return response;
     }
 
     public EconomyResponse depositPlayer(String playerName, double amount) {
         String locationName = getPlayerLocationName(playerName);
         return depositPlayer(playerName, amount, locationName);
     }
     
     public EconomyResponse depositPlayer(String playerName, double amount, String locationName) {
         
         EconomyResponse response;
         if (locationName != null) {
             if (!hasAccount(playerName, locationName)) {
                 // Try and create account if it does not already exist, this supports plugins like
                 // Factions which use bogus player accounts as banks
                 boolean success = this.createPlayerAccount(playerName, locationName);
                 if (!success) {
                     return new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Can not deposit to player");
                 }
             }
             PlayerAccount account = storage.getPlayerAccount(playerName, locationName);
             double new_balance = account.deposit(amount);
             response = new EconomyResponse(amount, new_balance, ResponseType.SUCCESS, "");
         } else {
             logger.info("Failed to depositPlayer for player: " + playerName + " @ " + locationName + " amount: " + amount);
             response = new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Can not deposit to player");
         }
         return response;
     }
     
     public List<String> getBankNames() {
         return storage.getBankNames();
     }
     
     /*
      * Retrieve all bank accounts
      */
     public List<BankAccount> getAllBanks() {
         ArrayList<BankAccount> all_banks = new ArrayList<BankAccount>();
         for(String bank_name : storage.getBankNames()) {
             all_banks.add(storage.getBankAccount(bank_name));
         }
         return all_banks;
     }
 
     /*
      * Retrieve only those bank accounts for a player in a specific location
      */
     public List<BankAccount> getPlayerBanks(String playerName, String locationName) {
         ArrayList<BankAccount> player_banks = new ArrayList<BankAccount>();
         for(String bank_name : storage.getBankNames()) {
             BankAccount account = storage.getBankAccount(bank_name);
             if(account.getLocation().equalsIgnoreCase(locationName) &&
                     (account.isOwner(playerName) || account.isMember(playerName))) {
                 player_banks.add(account);
             }
         }
         return player_banks;
     }
 
     /* 
      * Retrieve a bank account by name
      */
     public BankAccount getBankAccount(String accountName) {
         if (storage.hasBankAccount(accountName)) {
             return storage.getBankAccount(accountName);
         } else {
             logger.info("getBankAccount could not find: " + accountName);
             return null;
         }
     }
 
     public EconomyResponse createBank(String name, String playerName) {
         return createBank(name, playerName, getPlayerLocationName(playerName));
     }
 
     public EconomyResponse createBank(String name, String playerName, String locationName) {
         
         // Make sure a bank can not be created without a location
         EconomyResponse response;
         if(storage.hasBankAccount(name)) {
             response = new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Bank account already exists");
         } else if(locationName != null && validLocationName(locationName)) {
             double initialBalance = config.getDouble("api.bank.initial_balance");
             BankAccount account = storage.createBankAccount(name, playerName, locationName, initialBalance);
             response = new EconomyResponse(initialBalance, account.getBalance(), ResponseType.SUCCESS, "");
         } else {
             logger.info("Failed to createBank for player: " + playerName + " @ " + locationName + " name: " + name);
             response = new EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Can not create a bank with an unknown or invalid location");
         }
         return response;
     }
 
     public EconomyResponse deleteBank(String name) {
         EconomyResponse response;
         if (storage.hasBankAccount(name)) {
             double balance = storage.getBankAccount(name).getBalance();
             storage.deleteBankAccount(name);
             if(!storage.hasBankAccount(name)) {
                 response = new EconomyResponse(balance, 0, ResponseType.SUCCESS, "");
             } else {
                 logger.info("Failed to deleteBank for name: " + name);
                 response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Could not delete bank account: " + name);
             }
         } else {
             response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Could find bank account to delete: " + name);
         }
         return response;
     }
 
     public EconomyResponse bankBalance(String name) {
         EconomyResponse response;
         if (storage.hasBankAccount(name)) {
             BankAccount account = storage.getBankAccount(name);
             response = new EconomyResponse(0, account.getBalance(), ResponseType.SUCCESS, "");
         } else {
             logger.info("Failed to get bankBalance for: " + name);
             response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Could not find bank account: " + name);
         }
         return response;
     }
 
     public EconomyResponse bankHas(String name, double amount) {
         EconomyResponse response;
         if(storage.hasBankAccount(name)) {
             BankAccount account = storage.getBankAccount(name);
             if (account.getBalance() >= amount) {
                 response = new EconomyResponse(0, account.getBalance(), ResponseType.SUCCESS, "");
             } else {
                 logger.info("Failed to check bankHas " + amount + " for: " + name);
                 response = new EconomyResponse(0, account.getBalance(), ResponseType.FAILURE, "Account does not enough money");
             }
         } else {
             response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Account does not exist");
         }
         return response;
     }
 
     public EconomyResponse bankWithdraw(String name, double amount) {
         EconomyResponse response = bankHas(name, amount);
         // Only act upon account if it has enough money
         if(response.type == ResponseType.SUCCESS) {
             BankAccount account = storage.getBankAccount(name);
             double new_balance = account.withdraw(amount);
             response = new EconomyResponse(amount, new_balance, ResponseType.SUCCESS, "");
         } else {
             logger.info("Failed to bankWithdraw " + amount + " for: " + name);
         }
         return response;
     }
 
     public EconomyResponse bankDeposit(String name, double amount) {
         EconomyResponse response;
         if (storage.hasBankAccount(name)) {
             BankAccount account = storage.getBankAccount(name);
             double new_balance = account.deposit(amount);
             response = new EconomyResponse(amount, new_balance, ResponseType.SUCCESS, "");
         } else {
             logger.info("Failed to bankDeposit " + amount + " for: " + name);
             response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Account does not exist");
         }
         return response;
     }
 
     public EconomyResponse isBankOwner(String name, String playerName) {
         String location = getPlayerLocationName(playerName);
         return isBankOwner(name, playerName, location);
     }
     
     public EconomyResponse isBankOwner(String name, String playerName, String location) {
         EconomyResponse response;
         if(storage.hasBankAccount(name) && location != null) {
             BankAccount account = storage.getBankAccount(name);
             if(account.getLocation().equalsIgnoreCase(location) && account.isOwner(playerName)) {
                 response = new EconomyResponse(0, account.getBalance(), ResponseType.SUCCESS, "");
             } else {
                 response = new EconomyResponse(0, account.getBalance(), ResponseType.FAILURE, playerName + " is not an owner of " + name);
             }
         } else {
             logger.info("Failed to find bank for isBankOwner, player: " + playerName + " @ " + location + " name: " + name);
             response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Bank account: " + name + " does not exist @ " + location);
         }
         return response;    
     }
     
     public EconomyResponse isBankMember(String name, String playerName) {
         String location = getPlayerLocationName(playerName);
         return isBankMember(name, playerName, location);
     }
 
     public EconomyResponse isBankMember(String name, String playerName, String location) {
         EconomyResponse response;
         if(storage.hasBankAccount(name) && location != null) {
             BankAccount account = storage.getBankAccount(name);
             // An owner should also be a member
             if(account.getLocation().equalsIgnoreCase(location) && (account.isOwner(playerName) || account.isMember(playerName))) {
                 response = new EconomyResponse(0, account.getBalance(), ResponseType.SUCCESS, "");
             } else {
                 response = new EconomyResponse(0, account.getBalance(), ResponseType.FAILURE, playerName + " is not a member of " + name);
             }
         } else {
             logger.info("Failed to find bank for isBankMember, player: " + playerName + " @ " + location + " name: " + name);
             response = new EconomyResponse(0, 0, ResponseType.FAILURE, "Bank account: " + name + " does not exist @ " + location);
         }
         return response;
     }
     
     /*
      * Force a reload of the accounts storage
      */
     public void forceReload() {
         storage.reload();
     }
 
     /*
      * Force a commit of the accounts storage
      */
      public void forceCommit() {
         storage.commit();
     }
 }
