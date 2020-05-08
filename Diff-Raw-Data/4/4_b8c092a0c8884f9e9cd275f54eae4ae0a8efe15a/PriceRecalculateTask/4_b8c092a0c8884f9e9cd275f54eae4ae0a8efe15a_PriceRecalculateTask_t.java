 package net.cubespace.RegionShop.Data.Tasks;
 
 import net.cubespace.RegionShop.Bukkit.Plugin;
 import net.cubespace.RegionShop.Config.ConfigManager;
 import net.cubespace.RegionShop.Config.Files.Sub.Item;
 import net.cubespace.RegionShop.Config.Files.Sub.ServerShop;
 import net.cubespace.RegionShop.Database.Database;
 import net.cubespace.RegionShop.Database.Repository.ItemRepository;
 import net.cubespace.RegionShop.Database.Table.CustomerSign;
 import net.cubespace.RegionShop.Database.Table.ItemMeta;
 import net.cubespace.RegionShop.Database.Table.ItemStorage;
 import net.cubespace.RegionShop.Database.Table.Items;
 import net.cubespace.RegionShop.Database.Table.Region;
 import net.cubespace.RegionShop.Util.ItemName;
 import net.cubespace.RegionShop.Util.Logger;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class PriceRecalculateTask extends BukkitRunnable {
     private HashMap<Integer, HashMap<String, ArrayList<Integer>>> recalcCache = new HashMap<Integer, HashMap<String, ArrayList<Integer>>>();
     private List<Item> items = new ArrayList<Item>();
 
     public PriceRecalculateTask(List<Item> items) {
         this.items = items;
     }
 
     private void prepareCache(Integer id) {
         if (!recalcCache.containsKey(id)) {
             HashMap<String, ArrayList<Integer>> newArrayList = new HashMap<String, ArrayList<Integer>>();
             newArrayList.put("buy", new ArrayList<Integer>());
             newArrayList.put("sell", new ArrayList<Integer>());
 
             for (Integer i = 0; i < 720; i++) {
                 newArrayList.get("buy").add(0);
                 newArrayList.get("sell").add(0);
             }
 
             recalcCache.put(id, newArrayList);
         }
     }
 
     private void addToCache(Integer id, Integer buy, Integer sell) {
         if (recalcCache.containsKey(id)) {
             HashMap<String, ArrayList<Integer>> newArrayList = recalcCache.get(id);
 
             if (newArrayList.get("buy").size() > 720) {
                 newArrayList.get("buy").remove(0);
             }
 
             if (newArrayList.get("sell").size() > 720) {
                 newArrayList.get("sell").remove(0);
             }
 
             newArrayList.get("sell").add(sell);
             newArrayList.get("buy").add(buy);
         }
     }
 
     private Integer getAverage(Integer id, String key) {
         if (recalcCache.containsKey(id)) {
             Integer amount = 0;
             ArrayList<Integer> newArrayList = recalcCache.get(id).get(key);
 
             for (Integer curAmount : newArrayList) {
                 amount += curAmount;
             }
 
             return Math.round(amount / 720);
         }
 
         return 0;
     }
 
     @Override
     public void run() {
         int lastItemStorageId = 0;
         for (Item item : items) {
             Items itemInShop = null;
             try {
                 itemInShop = Database.getDAO(Items.class).queryForId(item.databaseID);
             } catch (SQLException e) {
                 Logger.error("Could not get Item", e);
             }
 
             if (itemInShop == null) {
                 Logger.info("No item found to update");
 
                 continue;
             }
 
             Logger.debug("Item recalc for Item: " + itemInShop.getMeta().getId() + ":" + itemInShop.getMeta().getDataValue());
 
             prepareCache(itemInShop.getId());
 
            Integer sold = (itemInShop.getSold());
            Integer bought = (itemInShop.getBought());
 
             addToCache(itemInShop.getId(), bought, sold);
 
             sold = getAverage(itemInShop.getId(), "sell");
             bought = getAverage(itemInShop.getId(), "buy");
 
             Logger.debug("Item Recalc: " + bought + " / " + sold);
 
             Float sellPriceDiff = (float) sold / item.maxItemRecalc;
             Float buyPriceDiff;
 
             if (bought > 0) {
                 buyPriceDiff = (float) bought / item.maxItemRecalc;
             } else {
                 buyPriceDiff = 2.0F;
             }
 
             if (sellPriceDiff > 1.0) {
                 //Preis geht rauf
                 if (sellPriceDiff > item.limitSellPriceFactor) {
                     sellPriceDiff = item.limitSellPriceFactor;
                 }
             } else {
                 //Preis geht runter
                 if (sellPriceDiff < item.limitSellPriceUnderFactor) {
                     sellPriceDiff = item.limitSellPriceUnderFactor;
                 }
             }
 
             if (buyPriceDiff > 1.0) {
                 //Abgabe geht rauf
                 buyPriceDiff = buyPriceDiff * item.limitBuyPriceFactor;
 
                 if(buyPriceDiff > item.limitBuyPriceFactor) {
                     buyPriceDiff = item.limitBuyPriceFactor;
                 }
             } else {
                 //Abgabe geht runter
                 if (buyPriceDiff < item.limitBuyPriceUnderFactor) {
                     buyPriceDiff = item.limitBuyPriceUnderFactor;
                 }
             }
 
             Logger.debug("Diffs: " + buyPriceDiff + " / " + sellPriceDiff);
 
             Float newSellPrice = Math.round(item.sell * sellPriceDiff * 100) / 100.0F;
             Float newBuyPrice = Math.round(item.buy * buyPriceDiff * 100) / 100.0F;
 
             itemInShop.setBuy(newBuyPrice);
             itemInShop.setSell(newSellPrice);
             itemInShop.setCurrentAmount(99999);
             itemInShop.setBought(0);
             itemInShop.setSold(0);
 
             Logger.debug("New Price: " + newBuyPrice + " / " + newSellPrice);
 
             try {
                 Database.getDAO(Items.class).update(itemInShop);
             } catch (SQLException e) {
                 Logger.error("Could not update Item", e);
             }
 
             //Check if Item has a Sign
             CustomerSign customerSign = null;
             try {
                 customerSign = Database.getDAO(CustomerSign.class).queryBuilder().
                         where().
                         eq("item_id", itemInShop.getId()).
                         queryForFirst();
             } catch (SQLException e) {
                 Logger.error("Could not get Customer Sign", e);
             }
 
             final Items items = itemInShop;
 
             if (customerSign != null) {
                 final CustomerSign syncCustomerSign = customerSign;
                 Plugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Plugin.getInstance(), new Runnable() {
                     @Override
                     public void run() {
                         Block block = Plugin.getInstance().getServer().getWorld(syncCustomerSign.getRegion().getWorld()).getBlockAt(syncCustomerSign.getX(), syncCustomerSign.getY(), syncCustomerSign.getZ());
                         if (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
                             Sign sign = (Sign) block.getState();
 
                             //Get the nice name
                             ItemStack itemStack = ItemRepository.fromDBItem(items);
 
                             String dataName = ItemName.getDataName(itemStack);
                             String niceItemName;
                             if (dataName.endsWith(" ")) {
                                 niceItemName = dataName + ItemName.nicer(itemStack.getType().toString());
                             } else if (!dataName.equals("")) {
                                 niceItemName = dataName;
                             } else {
                                 niceItemName = ItemName.nicer(itemStack.getType().toString());
                             }
 
                             if (itemStack.getItemMeta().hasDisplayName()) {
                                 niceItemName = "(" + itemStack.getItemMeta().getDisplayName() + ")";
                             }
 
                             for (Integer line = 0; line < 4; line++) {
                                 sign.setLine(line, ConfigManager.language.Sign_Customer_SignText.get(line).
                                         replace("%id", items.getId().toString()).
                                         replace("%itemname", ItemName.nicer(niceItemName)).
                                         replace("%amount", items.getUnitAmount().toString()).
                                         replace("%sell", items.getSell().toString()).
                                         replace("%buy", items.getBuy().toString()));
                             }
 
                             sign.update();
                         }
                     }
                 });
             }
 
             if (lastItemStorageId != itemInShop.getItemStorage().getId()) {
                 //Reset the ItemStorage to avoid "Shop is full you cant sell"
                 try {
                     itemInShop.getItemStorage().setItemAmount(0);
                     Database.getDAO(ItemStorage.class).update(itemInShop.getItemStorage());
                 } catch (SQLException e) {
                     Logger.error("Could not reset ItemStorage", e);
                 }
 
                 lastItemStorageId = itemInShop.getItemStorage().getId();
             }
         }
     }
 }
 
