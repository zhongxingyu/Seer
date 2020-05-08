 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
 This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
 You are Free to:
    to Share: to copy, distribute and transmit the work
    to Remix: to adapt the work
 
 Under the following conditions:
    Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
    Non-commercial: You may not use this work for commercial purposes.
 
 With the understanding that:
    Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
    Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
    Other Rights: In no way are any of the following rights affected by the license:
        Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
        The author's moral rights;
        Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
 Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
 http://creativecommons.org/licenses/by-nc/3.0/
 */
 
 package io.github.alshain01.flags;
 
 import io.github.alshain01.flags.api.Flag;
 import io.github.alshain01.flags.api.area.Area;
 import io.github.alshain01.flags.api.area.Administrator;
 import io.github.alshain01.flags.api.area.Nameable;
 import io.github.alshain01.flags.api.area.Ownable;
 import io.github.alshain01.flags.api.area.Subdividable;
 import io.github.alshain01.flags.api.economy.EconomyBaseValue;
 import io.github.alshain01.flags.api.economy.EconomyPurchaseType;
 import io.github.alshain01.flags.api.economy.EconomyTransactionType;
 import io.github.alshain01.flags.api.event.FlagChangedEvent;
 import io.github.alshain01.flags.api.event.FlagMessageChangedEvent;
 import io.github.alshain01.flags.api.event.FlagPermissionTrustChangedEvent;
 import io.github.alshain01.flags.api.event.FlagPlayerTrustChangedEvent;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.apache.commons.lang.Validate;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permissible;
 import org.bukkit.permissions.Permission;
 
 import javax.annotation.Nonnull;
 
 /**
  * Class for base functions of a specific area.
  */
 abstract class AreaBase implements Area, Comparable<Area> {
     AreaBase() { }
 
     @Override
     public boolean getState(Flag flag) {
         return getState(flag, false);
     }
 
     @Override
     public Boolean getState(Flag flag, boolean absolute) {
         Validate.notNull(flag);
 
         Boolean value = Flags.getDataStore().readFlag(this, flag);
         if (absolute) { return value; }
         return value != null ? value : new AreaDefault(getWorld()).getState(flag, false);
     }
 
     @Override
     public final boolean setState(Flag flag, Boolean value, CommandSender sender) {
         Validate.notNull(flag);
 
         // Check to see if this can be paid for
         EconomyTransactionType transaction = null;
         if (Flags.getEconomy() != null // No economy
                 && sender != null
                 && sender instanceof Player // Need a player to charge
                 && value != getState(flag, true) // The flag isn't actually
                 // changing
                 && flag.getPrice(EconomyPurchaseType.Flag) != 0 // No defined price
                 && !(this instanceof AreaWilderness) // No charge for world flags
                 && !(this instanceof AreaDefault) // No charge for defaults
                 && !(this instanceof Administrator && ((Administrator) this)
                 .isAdminArea())) // No charge for admin areas
         {
             if (value != null
                     && (EconomyBaseValue.ALWAYS.isSet()
                     || EconomyBaseValue.PLUGIN.isSet()
                     && (getState(flag, true) == null || getState(flag, true) != flag.getDefault())
                     || EconomyBaseValue.DEFAULT.isSet()
                     && getState(flag, true) != new AreaDefault(
                     ((Player) sender).getLocation().getWorld())
                     .getState(flag, true))) {
                 // The flag is being set, see if the player can afford it.
                 if (isFundingLow(EconomyPurchaseType.Flag, flag,
                         (Player) sender)) {
                     return false;
                 }
                 transaction = EconomyTransactionType.Withdraw;
             } else {
                 // Check whether or not to refund the account for setting the
                 // flag value
                 if (EconomyPurchaseType.Flag.isRefundable()
                         && !EconomyBaseValue.ALWAYS.isSet()) {
                     transaction = EconomyTransactionType.Deposit;
                 }
             }
         }
 
         final FlagChangedEvent event = new FlagChangedEvent(this, flag, sender, value);
         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) { return false; }
 
         // Delay making the transaction in case the event is cancelled.
         if (transaction != null) {
             if (failedTransaction(transaction, EconomyPurchaseType.Flag, flag,
                     (Player) sender)) {
                 return true;
             }
         }
 
         final Boolean val = value == null ? null : value;
         Flags.getDataStore().writeFlag(this, flag, val);
         return true;
     }
 
     @Override
     public final String getMessage(Flag flag) {
         return getMessage(flag, true);
     }
 
     @Override
     public final String getMessage(Flag flag, String playerName) {
         Validate.notNull(playerName);
         return getMessage(flag, true).replace("{Player}", playerName);
     }
 
     @Override
     public String getMessage(Flag flag, boolean parse) {
         Validate.notNull(flag);
 
 		String message = Flags.getDataStore().readMessage(this, flag);
 
 		if (message == null) {
 			message = new AreaDefault(getWorld()).getMessage(flag);
 		}
 
 		if (parse) {
 			message = message
                     .replace("{World}", getWorld().getName())
                     .replace("{AreaType}", getCuboidPlugin().getCuboidName().toLowerCase());
 
             if(this instanceof Nameable) {
                 message = message.replace("{AreaName}", ((Nameable)this).getName());
             } else {
                 message = message.replace("{AreaName}", getId());
             }
 
             if(this instanceof Ownable) {
                 message = message.replace("{Owner}", ((Ownable) this).getOwnerName().toArray()[0].toString());
             } else {
                 message = message.replace("{Owner}", "the administrator");
             }
 			message = ChatColor.translateAlternateColorCodes('&', message);
 		}
 		return message;
 	}
 
     @Override
     public final boolean setMessage(Flag flag, String message, CommandSender sender) {
         Validate.notNull(flag);
 
         EconomyTransactionType transaction = null;
 
         // Check to see if this is a purchase or deposit
         if (Flags.getEconomy() != null // No economy
                 && sender != null
                 && sender instanceof Player // Need a player to charge
                 && flag.getPrice(EconomyPurchaseType.Message) != 0 // No defined price
                 && !(this instanceof AreaWilderness) // No charge for world flags
                 && !(this instanceof AreaDefault) // No charge for defaults
                 && !(this instanceof Administrator && ((Administrator) this)
                 .isAdminArea())) // No charge for admin areas
         {
             // Check to make sure we aren't removing the message
             if (message != null) {
                 // Check to make sure the message isn't identical to what we
                 // have
                 // (if they are just correcting caps, don't charge, I hate
                 // discouraging bad spelling & grammar)
                 if (!getMessage(flag, false).equalsIgnoreCase(message)) {
                     if (isFundingLow(EconomyPurchaseType.Message, flag, (Player) sender)) {
                         return false;
                     }
                     transaction = EconomyTransactionType.Withdraw;
                 }
             } else {
                 // Check whether or not to refund the account
                 if (EconomyPurchaseType.Message.isRefundable()) {
                     // Make sure the message we are refunding isn't identical to
                     // the default message
                     if (!getMessage(flag, false).equals(
                             flag.getDefaultAreaMessage())) {
                         transaction = EconomyTransactionType.Deposit;
                     }
                 }
             }
         }
 
         final FlagMessageChangedEvent event = new FlagMessageChangedEvent(this, flag, message, sender);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
 
         // Delay making the transaction in case the event is cancelled.
         if (transaction != null) {
             if (failedTransaction(transaction, EconomyPurchaseType.Message, flag, (Player) sender)) {
                 return true;
             }
         }
 
         Flags.getDataStore().writeMessage(this, flag, message);
         return true;
     }
 
     @Override
     public final Map<UUID, String> getPlayerTrustList(Flag flag) {
         Validate.notNull(flag);
 
         return Flags.getDataStore().readPlayerTrust(this, flag);
     }
 
     @Override
     public final Collection<Permission> getPermissionTrustList(Flag flag) {
         Validate.notNull(flag);
 
         return Flags.getDataStore().readPermissionTrust(this, flag);
     }
 
     @Override
     public final boolean setPlayerTrust(Flag flag, Player trustee, CommandSender sender) {
         Validate.notNull(flag);
         Validate.notNull(trustee);
 
         final Map<UUID, String> trustList = Flags.getDataStore().readPlayerTrust(this, flag);
 
         // Set player to trusted.
         if (trustList.containsKey(trustee.getUniqueId())) {
             return false;
         }
         trustList.put(trustee.getUniqueId(), trustee.getName());
 
         final FlagPlayerTrustChangedEvent event = new FlagPlayerTrustChangedEvent(this, flag, trustee.getUniqueId(), true, sender);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
 
         Flags.getDataStore().writePlayerTrust(this, flag, trustList);
         return true;
     }
 
 
     @Override
     public final boolean setPermissionTrust(Flag flag, String permission, CommandSender sender) {
         return setPermissionTrust(flag, new Permission(permission), sender);
     }
 
     @Override
     public final boolean setPermissionTrust(Flag flag, Permission permission, CommandSender sender) {
         Validate.notNull(flag);
         Validate.notNull(permission);
 
        final Collection<Permission> trustList = Flags.getDataStore().readPermissionTrust(this, flag);
 
         // Set player to trusted.
         if (trustList.contains(permission)) {
             return false;
         }
         trustList.add(permission);
 
         final FlagPermissionTrustChangedEvent event = new FlagPermissionTrustChangedEvent(this, flag, permission, true, sender);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
 
         Flags.getDataStore().writePermissionTrust(this, flag, trustList);
         return true;
     }
 
     @Override
     public final boolean removePlayerTrust(Flag flag, UUID trustee, CommandSender sender) {
         Validate.notNull(flag);
         Validate.notNull(trustee);
 
         final Map<UUID, String> trustList = Flags.getDataStore().readPlayerTrust(this, flag);
 
         // Remove player from trusted.
         if (!trustList.containsKey(trustee)) {
             return false;
         }
         trustList.remove(trustee);
 
         final FlagPlayerTrustChangedEvent event = new FlagPlayerTrustChangedEvent(this, flag, trustee, false, sender);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
 
         Flags.getDataStore().writePlayerTrust(this, flag, trustList);
         return true;
     }
 
     @Override
     public final boolean removePermissionTrust(Flag flag, String permission, CommandSender sender) {
         return removePermissionTrust(flag, new Permission(permission), sender);
     }
 
     @Override
     public final boolean removePermissionTrust(Flag flag, Permission permission, CommandSender sender) {
         Validate.notNull(flag);
         Validate.notNull(permission);
 
        final Collection<Permission> trustList = Flags.getDataStore().readPermissionTrust(this, flag);
 
         // Remove player from trusted.
         if (!trustList.contains(permission)) {
             return false;
         }
         trustList.remove(permission);
 
         final FlagPermissionTrustChangedEvent event = new FlagPermissionTrustChangedEvent(this, flag, permission, false, sender);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
             return false;
         }
 
         Flags.getDataStore().writePermissionTrust(this, flag, trustList);
         return true;
     }
 
     @Override
     public final boolean hasTrust(Flag flag, Player player) {
         Validate.notNull(flag);
         Validate.notNull(player);
 
         if (this instanceof Ownable && (((Ownable)this).getOwnerName().contains(player.getName().toLowerCase()))) { return true; }
 
         Map<UUID, String> tl = getPlayerTrustList(flag);
         if(tl.containsKey(player.getUniqueId())) {
             return true;
         }
 
         Collection<Permission> pTl = getPermissionTrustList(flag);
         for(Permission p : pTl) {
             if(player.hasPermission(p)) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public boolean hasPermission(Permissible p) {
         Validate.notNull(p);
 
         if (this instanceof Ownable && p instanceof HumanEntity
                 && ((Ownable)this).getOwnerName().contains(((HumanEntity) p).getName())) {
             return p.hasPermission("flags.command.flag.set");
         }
 
         if (this instanceof Administrator
                 && ((Administrator) this).isAdminArea()) {
             return p.hasPermission("flags.area.flag.admin");
         }
 
         return p.hasPermission("flags.area.flag.others");
     }
 
     @Override
 	public boolean hasBundlePermission(Permissible p) {
         Validate.notNull(p);
 
 		if (this instanceof Ownable && p instanceof HumanEntity
 				&& ((Ownable)this).getOwnerName().contains(((HumanEntity) p).getName())) {
 			return p.hasPermission("flags.command.bundle.set");
 		}
 
 		if (this instanceof Administrator
 				&& ((Administrator) this).isAdminArea()) {
 			return p.hasPermission("flags.area.bundle.admin");
 		}
 
 		return p.hasPermission("flags.area.bundle.others");
 	}
 
     @Override
     final public int compareTo(@Nonnull Area a) {
         Validate.notNull(a);
         if ((a.getClass().equals(this.getClass()))) {
             if (getId().equals(a.getId())) {
                 return 0;
             }
 
             if (this instanceof Subdividable) {
                 if (((Subdividable) this).isSubdivision()) {
                     if (((Subdividable) a).isSubdivision() && ((Subdividable) a).getParent().getId().equals(((Subdividable) this).getParent().getId()))
                         return 2;
                     if (((Subdividable) this).getParent().getId().equals(a.getId()))
                         return -1;
                 } else if (((Subdividable) a).isSubdivision() && ((Subdividable) a).getParent().getId().equals(getId())) {
                     return 1;
                 }
             }
         }
         return 3;
     }
 
     /*
      * Checks to make sure the player can afford the item. If false, the player
      * is automatically notified.
      */
     private static boolean isFundingLow(EconomyPurchaseType product, Flag flag, Player player) {
         final double price = flag.getPrice(product);
 
         if (price > Flags.getEconomy().getBalance(player.getName())) {
             player.sendMessage(Message.LowFunds.get()
                     .replace("{PurchaseType}", product.getLocal().toLowerCase())
                     .replace("{Price}", Flags.getEconomy().format(price))
                     .replace("{Flag}", flag.getName()));
             return true;
         }
         return false;
     }
 
     /*
      * Makes the final purchase transaction.
      */
     private static boolean failedTransaction(EconomyTransactionType transaction,
                                            EconomyPurchaseType product, Flag flag, Player player) {
         final double price = flag.getPrice(product);
 
         final EconomyResponse r = transaction == EconomyTransactionType.Withdraw ? Flags
                 .getEconomy().withdrawPlayer(player.getName(), price) // Withdrawal
                 : Flags.getEconomy().depositPlayer(player.getName(), price); // Deposit
 
         if (r.transactionSuccess()) {
             player.sendMessage(transaction.getMessage().replace("{Price}", Flags.getEconomy().format(price)));
             return false;
         }
 
         // Something went wrong if we made it this far.
         Bukkit.getPluginManager().getPlugin("Flags").getLogger().warning(String.format("[Economy Error] %s", r.errorMessage));
         player.sendMessage(Message.Error.get().replace("{Error}", r.errorMessage));
         return true;
     }
 }
