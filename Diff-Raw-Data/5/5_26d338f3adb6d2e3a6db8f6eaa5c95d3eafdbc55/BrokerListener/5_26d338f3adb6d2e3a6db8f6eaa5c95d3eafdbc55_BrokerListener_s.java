 package me.ellbristow.broker;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Sign;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Villager;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class BrokerListener implements Listener {
     
     private static Broker plugin;
     
     public BrokerListener (Broker instance) {
         plugin = instance;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClick(InventoryClickEvent event) {
         if (event.isCancelled()) return;
         Inventory inv = event.getView().getTopInventory();
         if (inv.getName().startsWith("<Broker>")) {
             String seller = "";
             boolean buyOrders = false;
             if (inv.getName().equals("<Broker> Buy Orders")) {
                 buyOrders = true;
             } else if (inv.getName().equals("<Broker> Buy Cancel")) {
                 buyOrders = true;
                 seller = "Cancel";
             } else if (inv.getName().equals("<Broker> Sell Cancel")) {
                 seller = "Cancel";
             } else if (inv.getName().equals("<Broker> Buy AdminCancel")) {
                 buyOrders = true;
                 seller = "ADMIN";
             } else if (inv.getName().equals("<Broker> Sell AdminCancel")) {
                 seller = "ADMIN";
             } else {
                 seller = inv.getName().split(" ")[1];
             }
             if (!seller.equals("Cancel") && !seller.equals("ADMIN")) {
                 if (seller.equals("Buy")) seller = "";
                 Player player = (Player)event.getWhoClicked();
                 Player buyer;
                 buyer = player;
                 int slot = event.getRawSlot();
                 if (slot >= 45 && slot <54) {
                     event.setCancelled(true);
                     // Clicked navigation slot
                     Material itemType = inv.getItem(slot).getType();
                     if (itemType == Material.BOOK) {
                         // Main Page
                         inv.setContents(plugin.getBrokerInv("0", buyer, seller, buyOrders).getContents());
                         player.sendMessage(ChatColor.GOLD + "Main Page");
                     } else if (itemType == Material.PAPER) {
                         // Change Page
                         if (inv.getItem(0).getType() != Material.BOOK) {
                             // On Main Page
                             inv.setContents(plugin.getBrokerInv((slot-45)+"", buyer, seller, buyOrders).getContents());
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         } else {
                             // On Sub Page
                             String itemName = inv.getItem(0).getType().name();
                             inv.setContents(plugin.getBrokerInv(itemName+"::"+(slot-45), buyer, seller, buyOrders).getContents());
                             player.sendMessage(ChatColor.GOLD + itemName);
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         }
                     }
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null && inv.getItem(45).getType() != Material.BOOK) {
                     // Clicked item on Main Page
                     event.setCancelled(true);
                     Material itemType = inv.getItem(slot).getType();
                     String itemName = itemType.name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", buyer, seller, buyOrders).getContents());
                     player.sendMessage(ChatColor.GOLD + itemType.name());
                     player.sendMessage(ChatColor.GOLD + "Page 1");
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null) {
                     // Clicked item on sub-page
                     event.setCancelled(true);
                     String priceString = getPrice(inv,slot, null);
                     String[] priceSplit = priceString.split(":");
                     double price = Double.parseDouble(priceSplit[0]);
                     int perItems = Integer.parseInt(priceSplit[1]);
                     String each = "each";
                     if (perItems != 1) {
                         each = "for " + perItems;
                     }
                     if (buyOrders) {
                         player.sendMessage(ChatColor.GOLD + "Buy Order Details:");
                         player.sendMessage(ChatColor.GOLD + " Max Price Each: " + ChatColor.WHITE + plugin.vault.economy.format(price));
                         player.sendMessage(ChatColor.GOLD + " Max Quant: " + ChatColor.WHITE + perItems);
                         player.sendMessage(ChatColor.GOLD + "To sell an item, Try:");
                         player.sendMessage(" /broker sell [price] {Per # Items}!");
                         final String playerName = player.getName();
                         plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                             @Override
                             public void run() {
                                 Player runPlayer = plugin.getServer().getPlayer(playerName);
                                 runPlayer.closeInventory();
                                 runPlayer.updateInventory();
                             }
                         }, 5L);
                     } else if (price != 0.00) {
                         player.sendMessage(ChatColor.GOLD + "Price: " + ChatColor.WHITE + plugin.vault.economy.format(price) + " ("+each+")");
                         HashMap<Integer,String> slotPrice = new HashMap<Integer,String>();
                         slotPrice.put(slot,price+":"+perItems);
                         final HashMap<ItemStack,String> pending = new HashMap<ItemStack,String>();
                         pending.put(inv.getItem(slot),price+":"+perItems);
                         plugin.pending.put(player.getName(), pending);
                         player.sendMessage("Enter quantity to buy at this price");
                         player.sendMessage("(Enter 0 to cancel)");
                         final String playerName = player.getName();
                         plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                             @Override
                             public void run() {
                                 Player runPlayer = plugin.getServer().getPlayer(playerName);
                                 runPlayer.closeInventory();
                                 runPlayer.updateInventory();
                             }
                         }, 5L);
                         plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                             @Override
                             public void run () {
                                 if (plugin.pending.containsKey(playerName)) {
                                     HashMap<ItemStack, String> thisPending = plugin.pending.get(playerName);
                                     if (thisPending.equals(pending)) {
                                         Player runPlayer = plugin.getServer().getPlayer(playerName);
                                         if (runPlayer != null) {
                                             runPlayer.sendMessage(ChatColor.RED + "You took too long to specify a quantity. Order Cancelled!");
                                         }
                                         plugin.pending.remove(playerName);
                                     }
                                 }
                             }
                         }, 200L);
                     } else {
                         final String playerName = player.getName();
                         plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                         @Override
                         public void run() {
                                 Player runPlayer = plugin.getServer().getPlayer(playerName);
                                 runPlayer.closeInventory();
                                 runPlayer.updateInventory();
                             }
                         }, 5L);
                         player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                         player.sendMessage(ChatColor.RED + "Please try again.");
                     }
                 } else if (event.isShiftClick() && event.isLeftClick()) {
                     event.setCancelled(true);
                 } else if (slot >= 0 && slot < 54 && event.getCursor() != null) {
                     event.setCancelled(true);
                 }
             } else {
                 
                 // Cancelling Orders
                 
                 Player player = (Player)event.getWhoClicked();
                 
                 if (!seller.equals("ADMIN")) {
                     seller = player.getName();
                 }
                 
                 int slot = event.getRawSlot();
                 if (slot >= 45 && slot <54) {
                     event.setCancelled(true);
                     // Clicked navigation slot
                     Material itemType = inv.getItem(slot).getType();
                     if (itemType == Material.BOOK) {
                         // Main Page
                         inv.setContents(plugin.getBrokerInv("0", player, seller, buyOrders).getContents());
                         player.sendMessage(ChatColor.GOLD + "Main Page");
                     } else if (itemType == Material.PAPER) {
                         // Change Page
                         if (inv.getItem(0).getType() != Material.BOOK) {
                             // On Main Page
                             inv.setContents(plugin.getBrokerInv((slot-45)+"", player, seller, buyOrders).getContents());
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         } else {
                             // On Sub Page
                             String itemName = inv.getItem(0).getType().name();
                             inv.setContents(plugin.getBrokerInv(itemName+"::"+(slot-45), player, seller, buyOrders).getContents());
                             player.sendMessage(ChatColor.GOLD + itemName);
                             player.sendMessage(ChatColor.GOLD + "Page " + (slot-44));
                         }
                     }
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null && inv.getItem(45).getType() != Material.BOOK) {
                     // Clicked item on Main Page
                     event.setCancelled(true);
                     Material itemType = inv.getItem(slot).getType();
                     String itemName = itemType.name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", player, seller, buyOrders).getContents());
                     player.sendMessage(ChatColor.GOLD + itemType.name());
                     player.sendMessage(ChatColor.GOLD + "Page 1");
                 } else if (slot >= 0 && slot < 45 && inv.getItem(slot) != null) {
                     // Clicked item on sub-page
                     // Cancel Order
                     int orderType = 0;
                     String perItemsString = " AND perItems = ";
                     if (buyOrders) {
                         orderType = 1;
                         perItemsString  = "";
                     }
                     
                     event.setCancelled(true);
                     ItemStack stack = inv.getItem(slot);
                     Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                     String enchantmentString = "";
                     if (!enchantments.isEmpty()) {
                         enchantmentString = " AND enchantments = '";
                         Object[] enchs = enchantments.keySet().toArray();
                         for (Object ench : enchs) {
                             if (!" AND enchantments = '".equals(enchantmentString)) {
                                 enchantmentString += ";";
                             }
                             enchantmentString += ((Enchantment)ench).getId() + "@" + enchantments.get((Enchantment)ench);
                         }
                         enchantmentString += "'";
                     }
                     String priceString;
                     if (seller.equals("ADMIN")) {
                         priceString = getPrice(inv, slot, "ADMIN");
                     } else {
                         priceString = getPrice(inv, slot, player.getName());
                     }
                     String[] priceSplit = priceString.split(":");
                     double price = Double.parseDouble(priceSplit[0]);
                     int perItems = Integer.parseInt(priceSplit[1]);
                     if (!buyOrders) {
                         perItemsString += perItems;
                     }
                     HashMap<Integer, HashMap<String, Object>> orders;
                     if (seller.equals("ADMIN")) {
                         orders = plugin.brokerDb.select("id, quant, playerName","BrokerOrders","orderType = "+orderType+" AND itemName = '" + stack.getType().name() + "' AND damage = " + stack.getDurability() + enchantmentString + " AND price = " + price + perItemsString,null,null);
                     } else {
                         orders = plugin.brokerDb.select("id, quant, playerName","BrokerOrders","playerName = '" + player.getName() + "' AND orderType = "+orderType+" AND itemName = '" + stack.getType().name() + "' AND damage = " + stack.getDurability() + enchantmentString + " AND price = " + price + perItemsString,null,null);
                     }
                     int totQuant = 0;
                     HashMap<String, Double> refunds = new HashMap<String,Double>();
                     for (int i = 0; i < orders.size(); i++) {
                         int orderId = (Integer)orders.get(i).get("id");
                         int quant = (Integer)orders.get(i).get("quant");
                         totQuant += quant;
                         String buyerName = (String)orders.get(i).get("playerName");
                         
                         double thisRefund = quant * price;
                         if (plugin.taxOnBuyOrders) {
                             double fee = plugin.calcTax(thisRefund);
                             thisRefund += fee;
                             plugin.distributeTax(fee);
                         }
                         
                         if (refunds.containsKey(buyerName)) {
                             double oldRefund = refunds.get(buyerName);
                             refunds.put(buyerName, oldRefund + thisRefund);
                         } else {
                             refunds.put(buyerName, thisRefund);
                         }
                         String query = "DELETE FROM BrokerOrders WHERE id = " + orderId;
                         plugin.brokerDb.query(query);
                     }
                     stack.setAmount(totQuant);
                     String itemName = stack.getType().name();
                     if (!plugin.isDamageableItem(new ItemStack(Material.getMaterial(itemName)))) {
                         itemName += ":"+inv.getItem(slot).getDurability();
                     }
                     inv.setContents(plugin.getBrokerInv(itemName+"::0", player, seller, buyOrders).getContents());
                     if (inv.getItem(0) == null) {
                         inv.setContents(plugin.getBrokerInv("0", player, seller, buyOrders).getContents());
                     }
                     if (buyOrders) {
                         if (seller.equals("ADMIN")) {
                             player.sendMessage(ChatColor.GOLD + "Buy Order(s) Cancelled");
                         }
                         for (String buyerName : refunds.keySet()) {
                             OfflinePlayer buyer = Bukkit.getOfflinePlayer(buyerName);
                             double refund = refunds.get(buyerName);
                             plugin.vault.economy.depositPlayer(buyerName, refund);
                             if (buyer.isOnline()) {
                                 buyer.getPlayer().sendMessage(ChatColor.GOLD + "Buy Order Cancelled");
                                 buyer.getPlayer().sendMessage(ChatColor.GRAY + "You were refunded " + ChatColor.WHITE + plugin.vault.economy.format(refund));
                             }
                             
                         }
                     } else {
                         player.sendMessage(ChatColor.GOLD + "Sell Order Cancelled");
                     }
                     if (!buyOrders) {
                         HashMap<Integer, ItemStack> dropped = player.getInventory().addItem(stack);
                         if (!dropped.isEmpty()) {
                             player.sendMessage(ChatColor.RED + "Not all items could fit in your Inventory!");
                             player.sendMessage(ChatColor.RED + "Look on the floor!");
                             for (int i = 0; i < dropped.size(); i++) {
                                 player.getWorld().dropItem(player.getLocation(), dropped.get(i));
                             }
                         }
                     }
                 } else if (event.isShiftClick() && event.isLeftClick()) {
                     event.setCancelled(true);
                 } else if (slot >= 0 && slot < 54 && event.getCursor() != null) {
                     event.setCancelled(true);
                 }
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerChat(AsyncPlayerChatEvent event) {
         if (!event.isCancelled()) {
             Player player = event.getPlayer();
             if (plugin.pending.containsKey(player.getName())) {
                 event.setCancelled(true);
                 int quantity = 0;
                 try {
                     quantity = Integer.parseInt(event.getMessage());
                 } catch (NumberFormatException nfe) {
                     plugin.pending.remove(player.getName());
                     player.sendMessage(ChatColor.RED + "Invalid quantity. Order Cancelled!");
                 }
                 if (quantity <= 0) {
                     plugin.pending.remove(player.getName());
                     player.sendMessage(ChatColor.RED + "Order Cancelled!");
                 } else {
                     HashMap<ItemStack, String> pending = plugin.pending.get(player.getName());
                     Object[] items = pending.keySet().toArray();
                     ItemStack stack = (ItemStack)items[0];
                     Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                     String enchantmentString = "";
                     String enchanted = "";
                     if (!enchantments.isEmpty()) {
                         enchantmentString = " AND enchantments = '";
                         enchanted = " (Enchanted)";
                         Object[] enchs = enchantments.keySet().toArray();
                         for (Object ench : enchs) {
                             if (!" AND enchantments = '".equals(enchantmentString)) {
                                 enchantmentString += ";";
                             }
                             enchantmentString += ((Enchantment)ench).getId() + "@" + enchantments.get((Enchantment)ench);
                         }
                         enchantmentString += "'";
                     }
                     if (plugin.isDamageableItem(stack) && stack.getDurability() != 0) {
                         enchanted += "(Damaged)";
                     }
                     String priceString = pending.get(stack);
                     String[] priceSplit = priceString.split(":");
                     double price = Double.parseDouble(priceSplit[0]);
                     int perItems = Integer.parseInt(priceSplit[1]);
                     quantity = (int)(quantity / perItems) * perItems;
                     if (quantity == 0) {
                         plugin.pending.remove(player.getName());
                         player.sendMessage(ChatColor.RED + "You must buy at least "+perItems+" items for this order!");
                         player.sendMessage(ChatColor.RED + "Order Cancelled!");
                         return;
                     }
                     HashMap<Integer, HashMap<String, Object>> sellOrders = plugin.brokerDb.select("SUM(quant) as totQuant","BrokerOrders", "orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND perItems = "+perItems+" AND damage = " + stack.getDurability() + enchantmentString, null, "timeCode ASC");
                     if (sellOrders != null) {
                         try {
                             int tot = 0;
                             for (int i = 0; i < sellOrders.size(); i++) {
                                 if (tot == 0) {
                                     tot = (Integer)sellOrders.get(i).get("totQuant");
                                 }
                             }
                             if (quantity > tot) {
                                 player.sendMessage(ChatColor.RED + "Only " + ChatColor.WHITE + tot + ChatColor.RED + " were available at this price!");
                                 quantity = tot;
                             }
                             double totPrice = quantity * price / perItems;
                             if (plugin.vault.economy.getBalance(player.getName()) < totPrice) {
                                 player.sendMessage(ChatColor.RED + "You cannot afford " + quantity + " of those!");
                                 player.sendMessage(ChatColor.RED + "Total Price: " + plugin.vault.economy.format(totPrice));
                                 player.sendMessage(ChatColor.RED + "Order Cancelled!");
                                 plugin.pending.remove(player.getName());
                             } else {
                                 stack.setAmount(quantity);
                                 plugin.vault.economy.withdrawPlayer(player.getName(), totPrice);
                                 HashMap<Integer, ItemStack> drop = player.getInventory().addItem(stack);
                                 if (!drop.isEmpty()) {
                                     player.sendMessage(ChatColor.YELLOW + "Some items did not fit in your inventory! Look on the floor!");
                                     for (int i = 0; i < drop.size(); i++) {
                                         ItemStack dropStack = drop.get(i);
                                         player.getWorld().dropItem(player.getLocation(), dropStack);
                                     }
                                 }
                                 player.sendMessage(ChatColor.GOLD + "You bought " + ChatColor.WHITE + quantity + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(totPrice));
                                 HashMap<Integer, HashMap<String, Object>> playerOrders = plugin.brokerDb.select("playerName, SUM(quant) AS quant, timeCode", "BrokerOrders", "orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString,"playerName", "timeCode ASC");
                                 int allocated = 0;
                                 HashMap<String,Integer> allSellers = new HashMap<String, Integer>();
                                 for (int i =0; i < playerOrders.size(); i++) {
                                     String playerName = (String)playerOrders.get(i).get("playerName");
                                     int playerQuant = (Integer)playerOrders.get(i).get("quant");
                                     allSellers.put(playerName,playerQuant);
                                 }
                                 Object[] sellers = allSellers.keySet().toArray();
                                 for (int i = 0; i < sellers.length; i++) {
                                     String sellerName = (String)sellers[i];
                                     int quant = allSellers.get(sellerName);
                                     OfflinePlayer seller = plugin.getServer().getOfflinePlayer(sellerName);
                                     if (quantity - allocated >= quant) {
                                         allocated += quant;
                                         double tax = plugin.calcTax(quant * price);
                                         plugin.vault.economy.depositPlayer(sellerName, (quant * price) - tax);
                                         plugin.distributeTax(tax);
                                         String thisquery = "DELETE FROM BrokerOrders WHERE playername = '" + sellerName + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString;
                                         plugin.brokerDb.query(thisquery);
                                         if (seller.isOnline()) {
                                             seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " bought " + ChatColor.WHITE + quant + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(quant * price));
                                             if (tax > 0) {
                                                 seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] You were charged sales tax of " + ChatColor.WHITE + plugin.vault.economy.format(tax));
                                             }
                                         }
                                     } else {
                                         int deduct = quantity - allocated;
                                         int selling = deduct;
                                         double tax = plugin.calcTax(deduct * price);
                                         plugin.vault.economy.depositPlayer(sellerName, (deduct * price) - tax);
                                         plugin.distributeTax(tax);
                                         HashMap<Integer, HashMap<String, Object>> sellerOrders = plugin.brokerDb.select("id, quant","BrokerOrders", "playername = '" + sellerName + "' AND orderType = 0 AND itemName = '" + stack.getType().name() + "' AND price = " + price + " AND damage = " + stack.getDurability() + enchantmentString, null, "timeCode ASC");
                                         Set<String> queries = new HashSet<String>();
                                         for (int j = 0; j < sellerOrders.size(); j++) {
                                             if (deduct != 0) {
                                                 int sellQuant = (Integer)sellerOrders.get(j).get("quant");
                                                 if (sellQuant <= deduct) {
                                                     queries.add("DELETE FROM BrokerOrders WHERE id = " + (Integer)sellerOrders.get(j).get("id"));
                                                     deduct -= sellQuant;
                                                 } else {
                                                     queries.add("UPDATE BrokerOrders SET quant = quant - " + deduct + " WHERE id = " + (Integer)sellerOrders.get(j).get("id"));
                                                     deduct = 0;
                                                 }
                                             }
                                         }
                                         Object[]queryStrings = queries.toArray();
                                         for (Object thisquery : queryStrings) {
                                             plugin.brokerDb.query((String)thisquery);
                                         }
                                         if (seller.isOnline()) {
                                             seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] " + ChatColor.WHITE + player.getName() + ChatColor.GOLD + " bought " + ChatColor.WHITE + selling + " " + stack.getType().name() + enchanted + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(selling * price));
                                             if (tax > 0) {
                                                 seller.getPlayer().sendMessage(ChatColor.GOLD + "[Broker] You were charged sales tax of " + ChatColor.WHITE + plugin.vault.economy.format(tax));
                                             }
                                         }
                                         allocated = quantity;
                                     }
                                 }
                             }
                         } catch(Exception e) {
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "Sorry! This item may not be available any more!");
                         player.sendMessage(ChatColor.RED + "Please try again.");
                     }
                     plugin.pending.remove(player.getName());
                 }
                 plugin.pending.remove(player.getName());
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onSignChange(SignChangeEvent event) {
         if (event.isCancelled())
             return;
         String line0 = event.getLine(0);
         String line3 = event.getLine(3);
         if (line0.equalsIgnoreCase("[Broker]")) {
             Player player = event.getPlayer();
             if (!player.hasPermission("broker.sign") && !player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign.personal.others") && !player.hasPermission("broker.sign.buyorders") && !player.hasPermission("broker.sign.autosell") && !player.hasPermission("broker.sign.pricecheck")) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to create broker signs!");
                 event.getBlock().breakNaturally();
                 return;
             } else if (player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign") && !player.hasPermission("broker.sign.personal.others") && !player.hasPermission("broker.sign.buyorders") && !player.hasPermission("broker.sign.autosell") && !player.hasPermission("broker.sign.pricecheck")) {
                 event.setLine(3, player.getName());
             } else if ((player.hasPermission("broker.sign.personal") || player.hasPermission("broker.sign.personal.others") || player.hasPermission("broker.sign.buyorders") || player.hasPermission("broker.sign.autosell") || player.hasPermission("broker.sign.pricecheck") || player.hasPermission("broker.sign")) && !line3.equals("")) {
                 if (line3.equalsIgnoreCase("Buy Orders") || line3.equalsIgnoreCase("BuyOrders")) {
                     if (player.hasPermission("broker.sign.buyorders")) {
                         event.setLine(3, "Buy Orders");
                     } else {
                         player.sendMessage(ChatColor.RED + "You do not have permission to create Broker Buy Order signs!");
                         event.getBlock().breakNaturally();
                         return;
                     }
                 } else if (line3.equalsIgnoreCase("Auto Sell") || line3.equalsIgnoreCase("AutoSell")) {
                     if (player.hasPermission("broker.sign.autosell")) {
                         event.setLine(3, "Auto Sell");
                     } else {
                         player.sendMessage(ChatColor.RED + "You do not have permission to create Broker Auto Sell signs!");
                         event.getBlock().breakNaturally();
                         return;
                     }
                 } else if (line3.equalsIgnoreCase("Price Check") || line3.equalsIgnoreCase("PriceCheck")) {
                     if (player.hasPermission("broker.sign.pricecheck")) {
                         event.setLine(3, "Price Check");
                     } else {
                         player.sendMessage(ChatColor.RED + "You do not have permission to create Broker Price Check signs!");
                         event.getBlock().breakNaturally();
                         return;
                     }
                 } else if (!player.hasPermission("broker.sign.personal.others")) {
                     event.setLine(3, player.getName());
                 } else {
                     OfflinePlayer target = plugin.getServer().getOfflinePlayer(line3);
                     if (!target.hasPlayedBefore()) {
                         player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + line3 + ChatColor.RED + " not found!");
                         event.getBlock().breakNaturally();
                         return;
                     }
                     event.setLine(3, target.getName());
                 }
             }
             event.setLine(0, "[Broker]");
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled())
             return;
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().equals(Material.SIGN_POST) || event.getClickedBlock().getType().equals(Material.WALL_SIGN)) && ((Sign)event.getClickedBlock().getState()).getLine(0).equalsIgnoreCase("[Broker]")) {
             Player player = event.getPlayer();
             if (!player.hasPermission("broker.use")) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to use the broker!");
                 event.setCancelled(true);
                 return;
             }
             Sign sign = (Sign)event.getClickedBlock().getState();
             String sellerName = sign.getLine(3);
             String openString = "<Broker> Main Page";
             if (sellerName.equals("")) {
                 player.openInventory(plugin.getBrokerInv("0", player, null, false));
             } else {
                 if (!sellerName.equals("Buy Orders") && !sellerName.equals("Auto Sell")) {
                     OfflinePlayer seller = plugin.getServer().getOfflinePlayer(sellerName);
                     if (!seller.hasPlayedBefore()) {
                         player.sendMessage(ChatColor.RED + "Sorry! This shop appears to be closed!");
                         event.setCancelled(true);
                         return;
                     }
                     player.openInventory(plugin.getBrokerInv("0", player, seller.getName(), false));
                 } else if (sellerName.equals("Buy Orders")) {
                     player.openInventory(plugin.getBrokerInv("0", player, "", true));
                     openString = "<Broker> Buy Orders";
                 } else if (sellerName.equals("Auto Sell")) {
                     // Attempt Auto Sell
                     ItemStack stack = player.getItemInHand();
                     if (stack == null || stack.getType().equals(Material.AIR)) {
                         player.sendMessage(ChatColor.RED + "You are not holding anything to Auto Sell!");
                         event.setCancelled(true);
                         return;
                     }
                     if (!stack.getEnchantments().isEmpty() || stack.hasItemMeta()) {
                         player.sendMessage(ChatColor.RED + "You cannot Auto Sell items with enchantments or with ItemMeta data!");
                         event.setCancelled(true);
                         return;
                     }
                     HashMap<Integer, HashMap<String, Object>> buyOrders = plugin.brokerDb.select("id, playerName, price, quant", "BrokerOrders", "orderType = 1 AND itemName = '"+stack.getType()+"' AND damage = " + stack.getDurability() + " AND enchantments = '' AND meta = ''", null, "price DESC, timeCode ASC");
                     if (buyOrders.isEmpty()) {
                         player.sendMessage(ChatColor.RED + "No valid Buy Orders were found for that item!");
                         event.setCancelled(true);
                         return;
                     }
                     // Orders Found
                     int sold = 0;
                     int cost = 0;
                     for (Integer buyOrderId : buyOrders.keySet()) {
                         HashMap<String, Object> buyOrder = buyOrders.get(buyOrderId);
                         int id = Integer.parseInt(buyOrder.get("id").toString());
                         String buyerName = (String)buyOrder.get("playerName");
                         double price = Double.parseDouble(buyOrder.get("price").toString());
                         int quant = Integer.parseInt(buyOrder.get("quant").toString());
                         
                         int thisSale = 0;
                         ItemStack boughtStack = stack.clone();
                         
                         if (quant <= stack.getAmount()) {
                             thisSale += quant;
                             plugin.brokerDb.query("DELETE FROM BrokerOrders WHERE id = " + id);
                         } else {
                             thisSale += stack.getAmount();
                             plugin.brokerDb.query("UPDATE BrokerOrders SET quant = "+(quant-thisSale)+" WHERE id = " + id);
                         }
                         
                         if (thisSale != 0) {
                             sold += thisSale;
                             boughtStack.setAmount(thisSale);
                            double thisCost = boughtStack.getAmount() * price;
                             cost += thisCost;
                             OfflinePlayer buyer = Bukkit.getOfflinePlayer(buyerName);
                             if (buyer.isOnline()) {
                                 Player onlineBuyer = buyer.getPlayer();
                                onlineBuyer.sendMessage(ChatColor.GOLD + "You bought " + ChatColor.WHITE + boughtStack.getAmount() + " " + boughtStack.getType() + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(thisCost));
                                 HashMap<Integer, ItemStack> dropped = onlineBuyer.getInventory().addItem(boughtStack);
                                 if (!dropped.isEmpty()) {
                                     for (ItemStack dropStack : dropped.values()) {
                                         onlineBuyer.getWorld().dropItem(onlineBuyer.getLocation(), dropStack);
                                     }
                                     onlineBuyer.sendMessage(ChatColor.RED + "Not all bought items fit in your inventory! Check the floor!");
                                 }
                             } else {
                                 // List as pending
                                 plugin.brokerDb.query("INSERT INTO BrokerPending (playerName, itemName, damage, quant) VALUES ('"+buyerName+"', '"+boughtStack.getType()+"', "+boughtStack.getDurability()+", "+boughtStack.getAmount()+")");
                             }
                         }
                     }
                     if (sold == stack.getAmount()) {
                         player.setItemInHand(null);
                     } else {
                         stack.setAmount(stack.getAmount() - sold);
                     }
                     double fee = plugin.calcTax(cost);
                     plugin.vault.economy.depositPlayer(player.getName(), cost - fee);
                     plugin.distributeTax(fee);
                     player.sendMessage(ChatColor.GOLD + "You sold " + ChatColor.WHITE + sold + " " + stack.getType() + ChatColor.GOLD + " for " + ChatColor.WHITE + plugin.vault.economy.format(cost));
                     if (fee != 0) {
                         player.sendMessage(ChatColor.GOLD + "Broker Fee : " + ChatColor.WHITE + plugin.vault.economy.format(fee));
                     }
                     event.setCancelled(true);
                     return;
                 } else if (sellerName.equals("Price Check")) {
                     if (!player.hasPermission("broker.sign.pricecheck.update")) {
                         player.sendMessage(ChatColor.RED + "You don not have permission to update Price Check signs!");
                         event.setCancelled(true);
                         return;
                     }
                     ItemStack stack = player.getItemInHand();
                     if (stack == null || stack.getType().equals(Material.AIR)) {
                         player.sendMessage(ChatColor.RED + "You are not holding anything to Price Check!");
                         event.setCancelled(true);
                         return;
                     }
                     if (!stack.getEnchantments().isEmpty() || stack.hasItemMeta()) {
                         player.sendMessage(ChatColor.RED + "You cannot Price Check items with enchantments or with ItemMeta data!");
                         event.setCancelled(true);
                         return;
                     }
                     HashMap<Integer, HashMap<String, Object>> buyOrders = plugin.brokerDb.select("price, quant", "BrokerOrders", "orderType = 1 AND itemName = '"+stack.getType()+"' AND damage = " + stack.getDurability() + " AND enchantments = '' AND meta = ''", null, "price DESC, timeCode ASC");
                     if (buyOrders.isEmpty()) {
                         player.sendMessage(ChatColor.RED + "No valid Buy Orders were found for that item!");
                         event.setCancelled(true);
                         return;
                     }
                     HashMap<String, Object> order = buyOrders.get(0);
                     sign.setLine(1, stack.getType().name());
                     sign.setLine(2, order.get("price").toString() + " (" + order.get("quant") + ")");
                     sign.update();
                     event.setCancelled(true);
                     return;
                 }
             }
             plugin.pending.remove(player.getName());
             player.sendMessage(ChatColor.GOLD + openString);
             player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
             event.setCancelled(true);
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerTrade(PlayerInteractEntityEvent event) {
         
         if (event.isCancelled()) return;
         
         if (!event.getPlayer().isSneaking()) return;
         
         if (!event.getPlayer().hasPermission("broker.use")) return;
         
         Entity entity = event.getRightClicked();
         
         if (!(entity instanceof Player) && !(entity instanceof Villager)) return;
         
         Player player = event.getPlayer();
         
         if (entity instanceof Player && plugin.brokerPlayers) {
             Player target = (Player)entity;
             Inventory inv = plugin.getBrokerInv("0", player, target.getName(), false);
             if (inv == null || inv.getItem(0) == null) {
                 player.sendMessage(ChatColor.RED + "This player is not selling anything!");
                 return;
             }
             player.openInventory(inv);
             plugin.pending.remove(player.getName());
             player.sendMessage(ChatColor.GOLD + "<BROKER> Main Page");
             player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
             event.setCancelled(true);
         } else if (entity instanceof Villager && plugin.brokerVillagers) {
             player.openInventory(plugin.getBrokerInv("0", player, null, false));
             plugin.pending.remove(player.getName());
             player.sendMessage(ChatColor.GOLD + "<BROKER> Main Page");
             player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
             event.setCancelled(true);
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled())
             return;
         if ((event.getBlock().getType().equals(Material.SIGN_POST) || event.getBlock().getType().equals(Material.WALL_SIGN)) && ((Sign)event.getBlock().getState()).getLine(0).equalsIgnoreCase("[Broker]")) {
             String owner = ((Sign)event.getBlock().getState()).getLine(3);
             Player player = event.getPlayer();
             Sign sign = (Sign)event.getBlock().getState();
             if (!player.hasPermission("broker.sign") && !player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign.personal.others") && !player.hasPermission("broker.sign.buyorders") && !player.hasPermission("broker.sign.autosell")) {
                 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break Broker signs!");
                 event.setCancelled(true);
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             } else if (sign.getLine(3).equals("Buy Orders") && !player.hasPermission("broker.sign.buyorders")) {
                 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break Broker Buy Order signs!");
                 event.setCancelled(true);
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             } else if (sign.getLine(3).equals("Auto Sell") && !player.hasPermission("broker.sign.autosell")) {
                 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break Broker Auto Sell signs!");
                 event.setCancelled(true);
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             } else if (sign.getLine(3).equals("Price Check") && !player.hasPermission("broker.sign.pricecheck")) {
                 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break Broker Price Check signs!");
                 event.setCancelled(true);
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             } else if (player.hasPermission("broker.sign.personal") && !player.hasPermission("broker.sign.personal.others") && !owner.equalsIgnoreCase(player.getName())) {
                 event.getPlayer().sendMessage(ChatColor.RED + "This is not your sign to break!");
                 event.setCancelled(true);
                 sign.setLine(0, sign.getLine(0));        
                 sign.update();
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onVillagerDamage(EntityDamageByEntityEvent event) {
         
         if (event.isCancelled()) return;
         
         if (!plugin.brokerVillagers) return;
         
         Entity damager = event.getDamager();
         Entity target = event.getEntity();
         
         if (damager instanceof Player && target instanceof Villager) {
             Player player = (Player)damager;
             if (!player.hasPermission("broker.admin")) {
                 event.setCancelled(true);
             }
         }
         
     }
     
     private String getPrice(Inventory inv, int slot, String sellerName) {
         double price = 0.00;
         int perItems = 1;
         ItemStack stack = inv.getItem(slot);
         String sellerString = "";
         if (sellerName != null && !sellerName.equals("") && !sellerName.equals("ADMIN")) {
             sellerString = " AND playerName = '" + sellerName + "'";
         }
         int orderType = 0;
         String priceOrder = "ASC";
         String per = "perItems";
         String perGroup = ", perItems";
         if (inv.getName().equals("<Broker> Buy Orders") || inv.getName().equals("<Broker> Buy Cancel") || inv.getName().equals("<Broker> Buy AdminCancel")) {
             orderType = 1;
             priceOrder = "DESC";
             per = "SUM(quant) AS perItems";
             perGroup = "";
         }
         HashMap<Integer, HashMap<String, Object>> orders = plugin.brokerDb.select("price, " + per, "BrokerOrders", "orderType = " + orderType + sellerString + " AND itemName = '" + stack.getType().name() + "'", "price"+perGroup+", damage, enchantments, meta", "price/perItems "+priceOrder+", damage ASC");
         if (!orders.isEmpty()) {
             int counter = 0;
             for (HashMap<String, Object> order : orders.values()) {
                 if (counter == slot) {
                     price = Double.parseDouble(order.get("price")+"");
                     perItems = Integer.parseInt(order.get("perItems")+"");
                 }
                 counter++;
             }
         }
         return price+":"+perItems;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerLogin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         HashMap<Integer, HashMap<String, Object>> orders = plugin.brokerDb.select("*", "BrokerPending", "playerName = '"+player.getName()+"'", null, null);
         if (!orders.isEmpty()) {
             for (Integer orderId : orders.keySet()) {
                 HashMap<String, Object> order = orders.get(orderId);
                 int id = Integer.parseInt(order.get("id").toString());
                 Material mat = Material.getMaterial(order.get("itemName").toString());
                 ItemStack stack = new ItemStack(mat);
                 short damage = Short.parseShort(order.get("damage").toString());
                 stack.setDurability(damage);
                 int quant = Integer.parseInt(order.get("quant").toString());
                 stack.setAmount(quant);
                 player.sendMessage(ChatColor.GOLD + "You bought " + ChatColor.WHITE + quant + " " + mat + ChatColor.GOLD + "!");
                 HashMap<Integer, ItemStack> dropped = player.getInventory().addItem(stack);
                 if (!dropped.isEmpty()) {
                     for (ItemStack dropStack : dropped.values()) {
                         player.getWorld().dropItem(player.getLocation(), dropStack);
                     }
                     player.sendMessage(ChatColor.RED + "Not all bought items fit in your inventory! Check the floor!");
                 }
                 plugin.brokerDb.query("DELETE FROM BrokerPending WHERE id = " + id);
             }
         }
     }
     
 }
