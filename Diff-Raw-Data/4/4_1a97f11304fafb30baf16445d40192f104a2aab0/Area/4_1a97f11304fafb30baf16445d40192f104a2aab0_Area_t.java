 package alshain01.Flags.area;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import alshain01.Flags.Flag;
 import alshain01.Flags.Flags;
 import alshain01.Flags.Message;
 import alshain01.Flags.economy.BaseValue;
 import alshain01.Flags.economy.PurchaseType;
 import alshain01.Flags.economy.TransactionType;
 import alshain01.Flags.events.FlagChangedEvent;
 import alshain01.Flags.events.MessageChangedEvent;
 import alshain01.Flags.events.TrustChangedEvent;
 
 public abstract class Area implements Comparable<Area> {
 	protected final static String valueFooter = ".Value";
 	protected final static String trustFooter = ".Trust";
 	protected final static String messageFooter = ".Message";
 	
 	/* 
 	 * @return The data path of the data storage system for the area
 	 */
 	protected abstract String getDataPath();
 	
 	/**
 	 * Retrieve the land system's ID for this area.
 	 * 
 	 * @return the area's ID in the format provided by the land management system.
 	 */
 	public abstract String getSystemID();
 	
 	/**
 	 * Retrieve the friendly name of the area type.
 	 * 
 	 * @return the area's type as a user friendly name.
 	 */
 	public abstract String getAreaType();
 	
 	/**
 	 * Retrieve a set of owners for the area.  On many systems, there will only be one.
 	 * 
 	 * @return the player name of the area owner.
 	 */
 	public abstract Set<String> getOwners();
 	
 	/**
 	 * Retrieve the world for the area.
 	 * 
 	 * @return the world associated with the area.
 	 */
 	public abstract org.bukkit.World getWorld();
 
 	/**
 	 * Retrieve whether or not the area exists on the server.
 	 * Null Areas return false.
 	 * 
 	 * @return true if the area exists.
 	 */
 	public abstract boolean isArea();
 	
 	/**
 	 * Gets the players permission to set flags at this location.
 	 * 
 	 * @param player The player to check.
 	 * @return true if the player has permissions.
 	 */
 	public boolean hasPermission(Player player) {
 		if(!isArea()) { return false; }
 		
 		if (getOwners().contains(player.getName())) {
 			if (player.hasPermission("flags.flag.set")) { return true; }
 			return false;
 		}
 		
 		if(this instanceof Administrator && ((Administrator)this).isAdminArea()) {
 			if (player.hasPermission("flags.flag.set.admin")) {	return true; }
 			return false;
 		}
 		
 		if (player.hasPermission("flags.flag.set.others")) { return true; }
 		return false;
 	}
 	
 	/**
 	 * Gets the players permission to set bundles at this location
 	 * 
 	 * @param player The player to check.
 	 * @return true if the player has permissions.
 	 */
 	public boolean hasBundlePermission(Player player) {
 		if(!isArea()) { return false; }
 		
 		if (getOwners().contains(player.getName())) {
 			if (player.hasPermission("flags.bundle.set")) {	return true; }
 			return false;
 		}
 		
 		if(this instanceof Administrator && ((Administrator)this).isAdminArea()) {
 			if (player.hasPermission("flags.bundle.set.admin")) { return true; }
 			return false;
 		}
 		
 		if (player.hasPermission("flags.bundle.set.others")) { return true;	}
 		return false;
 	}
 	
 	/**
 	 * Returns the value of the flag for this area.
 	 * 
 	 * @param flag The flag to retrieve the value for.
 	 * @param absolute True if you want a null value if the flag is not defined. False if you want the inherited default (ensures not null).
 	 * @return The value of the flag or the inherited value of the flag from defaults if not defined.
 	 */
 	public Boolean getValue(Flag flag, boolean absolute) {
 		if(!isArea()) { return false; }
 		
     	Boolean value = null;
     	if(isArea()) { 
 	    	String valueString = Flags.instance.dataStore.read(getDataPath() + "." + flag.getName() + valueFooter);
 	    	
 	    	if (valueString != null && valueString.toLowerCase().contains("true")) { 
 	    		value = true;
 	    	} else if (valueString != null) {
 	    		value = false;
 	    	}
     	}
     	
     	if(absolute) { return value; }
         return (value != null) ? value :
         	new Default(getWorld()).getValue(flag, false);
 	}
 	
 	/**
 	 * Sets the value of the flag for this area.
 	 * 
 	 * @param flag The flag to set the value for.
 	 * @param value The value to set, null to remove.
 	 * @param sender The command sender for event call, may be null if no associated player or console.
 	 * @return False if the event was canceled.
 	 */
 	public final boolean setValue(Flag flag, Boolean value, CommandSender sender) {
 		if(!isArea()) { return false; }
 		
 		TransactionType transaction = null;
 		
         // Check to see if this is a purchase or deposit
         if(Flags.instance.economy != null					// No economy 
         		&& flag.getPrice(PurchaseType.Flag) != 0	// No defined price
         		&& !(this instanceof World)					// No charge for world flags
         		&& !(this instanceof Default)				// No charge for defaults
         		&& !(this instanceof Administrator && ((Administrator)this).isAdminArea())) // No charge for admin areas 
         {
     		if (BaseValue.ALWAYS.isSet()
    				|| (BaseValue.PLUGIN.isSet() && getValue(flag, true) != null && getValue(flag, true) != flag.getDefault()) 
    				|| (BaseValue.DEFAULT.isSet() && getValue(flag, true) != null && getValue(flag, true) != new Default(((Player)sender).getLocation()).getValue(flag, true)))
     	    { 
         		// Is the flag being deleted?			
         		if(value == null) {
         			// Check whether or not to refund the account for removing the flag
         			if (PurchaseType.Flag.isRefundable()) {
         				transaction = TransactionType.Deposit;
         			}
         		} else {
     	    		// Check whether or not to charge the account
         			if(!isFundingAvailable(PurchaseType.Flag, flag, (Player)sender)) { return false; }
         			transaction = TransactionType.Withdraw;
         		}
     	    } else {
         		// Check whether or not to refund the account for setting the flag value
         		if (PurchaseType.Flag.isRefundable() && !BaseValue.ALWAYS.isSet()) {
         			transaction = TransactionType.Deposit;
         		}
     	    }
         }
         
     	FlagChangedEvent event = new FlagChangedEvent(this, flag, sender, value);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) { return false; }
         
         // Delay making the transaction in case the event is cancelled.
         if(transaction != null) {
         	makeTransaction(transaction, PurchaseType.Flag, flag, (Player)sender);
         }
         
         if(value == null) {
         	// Remove the flag
         	Flags.instance.dataStore.write(getDataPath() + "." + flag.getName() + valueFooter, (String)null);
         } else {
             // Set the flag
         	Flags.instance.dataStore.write(getDataPath() + "." + flag.getName() + valueFooter, String.valueOf(value));
         }
         return true;
 	}
 	
 	/**
 	 * Retrieves the list of trusted players
 	 * 
 	 * @param flag The flag to retrieve the trust list for.
 	 * @return The list of players
 	 */
 	public Set<String> getTrustList(Flag flag) {
 		if(!isArea()) { return null; }
 		
     	Set<String> trustedPlayers = Flags.instance.dataStore.readSet(getDataPath() + "." + flag.getName() + trustFooter);
     	if(trustedPlayers == null) { trustedPlayers = new HashSet<String>(); }
     	trustedPlayers.addAll(getOwners());
     	return trustedPlayers;
 	}
 	
 	/**
 	 * Adds or removes a player from the trust list.
 	 * 
 	 * @param flag The flag to change trust for.
 	 * @param trustee The player being trusted or distrusted
 	 * @param trusted True if adding to the trust list, false if removing.
 	 * @param sender CommandSender for event, may be null if no associated player or console.
 	 * @return True if successful.
 	 */
 	public final boolean setTrust(Flag flag, String trustee, boolean trusted, CommandSender sender) {
 		if(!isArea()) { return false; }
 		
 		final String path = getDataPath() + "." + flag.getName() + trustFooter;
 		Set<String> trustList = Flags.instance.dataStore.readSet(path);
 		
 		// Set player to trusted.
     	if (trusted) {
     		if (trustList == null) {
     			trustList = new HashSet<String>(Arrays.asList(trustee.toLowerCase()));
     		} else {
     			if (trustList.contains(trustee.toLowerCase())) { return false; } // Player was already in the list!
     			trustList.add(trustee.toLowerCase());
     		}
     		
     		TrustChangedEvent event = new TrustChangedEvent(this, flag, trustee, true, sender);
     		Bukkit.getServer().getPluginManager().callEvent(event);
     		if (event.isCancelled()) { return false; }
        
     		//Set the list
     		Flags.instance.dataStore.write(path, trustList);
     		return true;
     	}
     	
     	// Remove player from trusted.
  	   if (trustList == null || !trustList.contains(trustee.toLowerCase())) { return false; }
 	   
  	   TrustChangedEvent event = new TrustChangedEvent(this, flag, trustee, false, sender);
  	   Bukkit.getServer().getPluginManager().callEvent(event);
  	   if (event.isCancelled()) { return false; }
  	   
  	   trustList.remove(trustee.toLowerCase());
  	   Flags.instance.dataStore.write(path, trustList);
  	   return true;
 	}
 	
 	/**
 	 * Gets the message associated with a player flag.
 	 * Translates the color codes and populates instances of {AreaType} and {Owner}
 	 * 
 	 * @param flag The flag to retrieve the message for.
 	 * @return The message associated with the flag.
 	 */
 	public final String getMessage(Flag flag) {
 		return getMessage(flag, true);
 	}
 
 	/**
 	 * Gets the message associated with a player flag and parses
 	 * {AreaType}, {Owner}, {World}, and {Player}
 	 * 
 	 * @param flag The flag to retrieve the message for.
 	 * @param player The player name to insert into the messsage.
 	 * @return The message associated with the flag.
 	 */
 	public final String getMessage(Flag flag, String player) {
 		return getMessage(flag, true).replaceAll("\\{Player\\}", player);
 	}
 	
 	/**
 	 * Gets the message associated with a player flag.
 	 * 
 	 * @param flag The flag to retrieve the message for.
 	 * @param parse True if you wish to populate instances of {AreaType}, {Owner}, and {World} and translate color codes
 	 * @return The message associated with the flag.
 	 */
 	public String getMessage(Flag flag, boolean parse) {
 		if(!isArea()){ return null; }
 		String message = Flags.instance.dataStore.read(getDataPath() + "." + flag.getName() + messageFooter);
 	 	   
 		if (message == null) {
 			message = new Default(getWorld()).getMessage(flag);
 		}
 		
 		if (parse) {
 			message = message
 					.replaceAll("\\{AreaType\\}", getAreaType().toLowerCase())
 					.replaceAll("\\{Owner\\}", getOwners().toArray()[0].toString());
 			message = ChatColor.translateAlternateColorCodes('&', message);
 		}
 		return message;
 	}
 	
 	/**
 	 * Sets or removes the message associated with a player flag.
 	 * 
 	 * @param flag The flag to set the message for.
 	 * @param message The message to set, null to remove.
 	 * @param sender CommandSender for event, may be null if no associated player or console.
 	 * @return True if successful
 	 */
 	public final boolean setMessage(Flag flag, String message, CommandSender sender) {
 		if(!isArea()) { return false; }
 	 	
 		TransactionType transaction = null;
 		
         // Check to see if this is a purchase or deposit
         if(Flags.instance.economy != null					// No economy 
         		&& flag.getPrice(PurchaseType.Message) != 0	// No defined price
         		&& !(this instanceof World)					// No charge for world flags
         		&& !(this instanceof Default)				// No charge for defaults
         		&& !(this instanceof Administrator && ((Administrator)this).isAdminArea())) // No charge for admin areas 
         {
 
     		// Check to make sure we aren't removing the message
         	if(message != null) {
     			// Check to make sure the message isn't identical to what we have
     			// (if they are just correcting caps, don't charge, I hate discouraging bad spelling & grammar)
     			if(!(getMessage(flag, false).equalsIgnoreCase(message))) { 
     				if(!isFundingAvailable(PurchaseType.Message, flag, (Player)sender)) { return false;	}
     				transaction = TransactionType.Withdraw;
     			}
         	} else {
         		// Check whether or not to refund the account
         		if(PurchaseType.Message.isRefundable()) {
         			// Make sure the message we are refunding isn't identical to the default message
         			if (!(getMessage(flag, false).equals(flag.getDefaultAreaMessage()))) {
     					transaction = TransactionType.Deposit;
         			}
         		}
         	}
         }
 
 		MessageChangedEvent event = new MessageChangedEvent(this, flag, message, sender);
 		Bukkit.getServer().getPluginManager().callEvent(event);
 		if (event.isCancelled()) { return false; }
 
         // Delay making the transaction in case the event is cancelled.
 		if(transaction != null) {
 			makeTransaction(transaction, PurchaseType.Message, flag, (Player)sender);
 		}
 		
 		Flags.instance.dataStore.write(getDataPath() + "." + flag.getName() + messageFooter, message.replaceAll("", "&"));
 		return true;
 	}
 	
 	/*
 	 * Check to make sure the player can afford the item.
 	 * If false, the player is automatically notified.
 	 */
 	private static boolean isFundingAvailable(PurchaseType product, Flag flag, Player player) {
 		double price = flag.getPrice(product);
 		
 		if (price > Flags.instance.economy.getBalance(player.getName())) {
 			player.sendMessage(Message.LowFunds.get()
 					.replaceAll("\\{PurchaseType\\}", product.getLocal().toLowerCase())
 					.replaceAll("\\{Price\\}", Flags.instance.economy.format(price))
 					.replaceAll("\\{Flag\\}", flag.getName()));
 			return false;
 		}
 		return true;
 	}
 	
 	/*
 	 * Makes the final purchase transaction.
 	 */
 	private static boolean makeTransaction(TransactionType transaction, PurchaseType product, Flag flag, Player player) {
 		double price = flag.getPrice(product);
 		
 		EconomyResponse r;
 		if (transaction == TransactionType.Withdraw) {
 			// Withdrawal
 			r = Flags.instance.economy.withdrawPlayer(player.getName(), price);
 		} else {
 			// Deposit
 			r = Flags.instance.economy.depositPlayer(player.getName(), price);
 		}
 		
 		if (r.transactionSuccess()) {
 			player.sendMessage(transaction.getLocal()
 					.replaceAll("\\{Price\\}", Flags.instance.economy.format(price)));
 			return false;
 		}
 		
 		// Something went wrong if we made it this far.
 		Flags.instance.getLogger().severe(String.format("An error occured: %s", r.errorMessage));
 		player.sendMessage(Message.Error.get()
 				.replaceAll("\\{Error\\}", r.errorMessage));
 		return true;
 	}
 }
