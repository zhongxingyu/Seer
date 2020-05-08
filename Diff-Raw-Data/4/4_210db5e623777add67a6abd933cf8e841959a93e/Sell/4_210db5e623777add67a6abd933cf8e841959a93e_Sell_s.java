 package net.cubespace.RegionShop.Core;
 
 import com.j256.ormlite.dao.ForeignCollection;
 import net.cubespace.RegionShop.Bukkit.Plugin;
 import net.cubespace.RegionShop.Config.ConfigManager;
 import net.cubespace.RegionShop.Config.Files.Sub.Group;
 import net.cubespace.RegionShop.Database.Database;
 import net.cubespace.RegionShop.Database.ItemStorageHolder;
 import net.cubespace.RegionShop.Database.PlayerOwns;
 import net.cubespace.RegionShop.Database.Repository.TransactionRepository;
 import net.cubespace.RegionShop.Database.Table.ItemStorage;
 import net.cubespace.RegionShop.Database.Table.Items;
 import net.cubespace.RegionShop.Database.Table.Transaction;
 import net.cubespace.RegionShop.Util.ItemName;
 import net.cubespace.RegionShop.Util.Logger;
 import net.cubespace.RegionShop.Util.VaultBridge;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.sql.SQLException;
 import java.util.List;
 
 public class Sell {
     public static void sell(final ItemStack itemStack, List<Items> items, Player player, final ItemStorageHolder region) {
         player.getInventory().removeItem(itemStack);
 
         ForeignCollection<PlayerOwns> playerList = region.getOwners();
         boolean isOwner = false;
 
         for(PlayerOwns player1 : playerList) {
             if(player1.getPlayer().getName().equals(player.getName().toLowerCase())) {
                 isOwner = true;
             }
         }
 
         if(isOwner) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_NotYourItem);
             player.getInventory().setItemInHand(itemStack);
             return;
         }
 
         //Check if there is Place inside the Shop
         Group group = ConfigManager.groups.getGroup(region.getItemStorage().getSetting());
         if(region.getItemStorage().getItemAmount() + itemStack.getAmount() >= group.Storage) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_FullStorage);
             player.getInventory().setItemInHand(itemStack);
 
             return;
         }
 
         //Check all items
         for(final Items item : items) {
             if (item != null && item.getBuy() > 0) {
                 Float price = (itemStack.getAmount() / item.getUnitAmount()) * item.getBuy();
 
                 if (region.getItemStorage().isServershop() || VaultBridge.has(item.getOwner(), itemStack.getAmount() * item.getBuy()) ) {
                     String dataName = ItemName.getDataName(itemStack);
                     String niceItemName;
                     if(dataName.endsWith(" ")) {
                         niceItemName = dataName + ItemName.nicer(itemStack.getType().toString());
                     } else if(!dataName.equals("")) {
                         niceItemName = dataName;
                     } else {
                         niceItemName = ItemName.nicer(itemStack.getType().toString());
                     }
 
                     if(!region.getItemStorage().isServershop()) {
                         OfflinePlayer owner = Plugin.getInstance().getServer().getOfflinePlayer(item.getOwner());
 
                         if (owner != null) {
                             if(owner.isOnline()) {
                                 Plugin.getInstance().getServer().getPlayer(item.getOwner()).sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_OwnerHint.
                                         replace("%player", player.getDisplayName()).
                                         replace("%amount", ((Integer)itemStack.getAmount()).toString()).
                                         replace("%item", niceItemName).
                                         replace("%shop", region.getName()).
                                         replace("%price", price.toString()));
                             }
 
                             TransactionRepository.generateTransaction(owner, Transaction.TransactionType.BUY, region.getName(), player.getWorld().getName(), player.getName(), item.getMeta().getItemID(), itemStack.getAmount(), item.getBuy().doubleValue(), 0.0, item.getUnitAmount());
                         }
 
                         VaultBridge.withdrawPlayer(item.getOwner(), itemStack.getAmount() * item.getBuy());
                     }
 
                     VaultBridge.depositPlayer(player.getName(), itemStack.getAmount() * item.getBuy());
                     player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_PlayerHint.
                             replace("%player", player.getDisplayName()).
                             replace("%amount", ((Integer) itemStack.getAmount()).toString()).
                             replace("%item", niceItemName).
                             replace("%shop", region.getName()).
                             replace("%price", price.toString()).
                             replace("%owner", item.getOwner()));
 
                     item.setCurrentAmount(item.getCurrentAmount() + itemStack.getAmount());
                     item.setBought(item.getBought() + itemStack.getAmount());
 
                     ItemStorage itemStorage = region.getItemStorage();
                     itemStorage.setItemAmount(itemStorage.getItemAmount() + itemStack.getAmount());
 
                     try {
                         Database.getDAO(ItemStorage.class).update(itemStorage);
                         Database.getDAO(Items.class).update(item);
                     } catch (SQLException e) {
                         Logger.error("Could not update Items/ItemStorage", e);
                     }
 
                     TransactionRepository.generateTransaction(player, Transaction.TransactionType.SELL, region.getName(), player.getWorld().getName(), item.getOwner(), item.getMeta().getItemID(), itemStack.getAmount(), 0.0, item.getBuy().doubleValue(), item.getUnitAmount());
 
                     return;
                 }
             }
         }
 
         //No item found :(
         player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_OwnerHasNotEnoughMoney);
         player.getInventory().setItemInHand(itemStack);
     }
 
     public static void sell(final ItemStack itemStack, final Items item, Player player, final ItemStorageHolder region) {
         player.getInventory().removeItem(itemStack);
 
         ForeignCollection<PlayerOwns> playerList = region.getOwners();
         boolean isOwner = false;
 
         for(PlayerOwns player1 : playerList) {
             if(player1.getPlayer().getName().equals(player.getName().toLowerCase())) {
                 isOwner = true;
             }
         }
 
         if(isOwner) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_NotYourItem);
             player.getInventory().setItemInHand(itemStack);
             return;
         }
 
         if(item.getBuy() <= 0.0) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_DoesNotBuy);
             player.getInventory().setItemInHand(itemStack);
             return;
         }
 
         Group group = ConfigManager.groups.getGroup(region.getItemStorage().getSetting());
         if(region.getItemStorage().getItemAmount() + itemStack.getAmount() >= group.Storage) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_FullStorage);
             player.getInventory().setItemInHand(itemStack);
 
             return;
         }
 
         Float price = (itemStack.getAmount() / item.getUnitAmount() ) * item.getBuy();
 
         if (region.getItemStorage().isServershop() || VaultBridge.has(item.getOwner(), price)) {
             String dataName = ItemName.getDataName(itemStack);
             String niceItemName;
             if(dataName.endsWith(" ")) {
                 niceItemName = dataName + ItemName.nicer(itemStack.getType().toString());
             }  else if(!dataName.equals("")) {
                 niceItemName = dataName;
             } else {
                 niceItemName = ItemName.nicer(itemStack.getType().toString());
             }
 
             if(!region.getItemStorage().isServershop()) {
                 OfflinePlayer owner = Plugin.getInstance().getServer().getOfflinePlayer(item.getOwner());
 
                 if (owner != null) {
                     TransactionRepository.generateTransaction(owner,
                             Transaction.TransactionType.BUY,
                             region.getName(),
                             player.getWorld().getName(),
                             player.getName(),
                             item.getMeta().getItemID(),
                             itemStack.getAmount(),
                             item.getBuy().doubleValue(),
                             0.0,
                             item.getUnitAmount());
                 }
 
                 VaultBridge.withdrawPlayer(item.getOwner(), price);
             }
 
             VaultBridge.depositPlayer(player.getName(), price);
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_PlayerHint.
                     replace("%player", player.getDisplayName()).
                     replace("%amount", ((Integer) itemStack.getAmount()).toString()).
                     replace("%item", niceItemName).
                     replace("%shop", region.getName()).
                    replace("%price", price.toString().
                    replace("%owner", item.getOwner())));
 
             item.setCurrentAmount(item.getCurrentAmount() + itemStack.getAmount());
             item.setBought(item.getBought() + itemStack.getAmount());
 
             ItemStorage itemStorage = region.getItemStorage();
             itemStorage.setItemAmount(itemStorage.getItemAmount() + itemStack.getAmount());
 
             try {
                 Database.getDAO(ItemStorage.class).update(itemStorage);
                 Database.getDAO(Items.class).update(item);
             } catch (SQLException e) {
                 Logger.error("Could not update Items/ItemStorage", e);
             }
 
             TransactionRepository.generateTransaction(player,
                     Transaction.TransactionType.SELL,
                     region.getName(),
                     player.getWorld().getName(),
                     item.getOwner(),
                     item.getMeta().getItemID(),
                     itemStack.getAmount(),
                     0.0,
                     item.getBuy().doubleValue(),
                     item.getUnitAmount());
 
         } else {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sell_OwnerHasNotEnoughMoney);
             player.getInventory().setItemInHand(itemStack);
         }
     }
 }
