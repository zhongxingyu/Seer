 package com.thepastimers.TradeSign;
 
 import com.thepastimers.Coord.Coord;
 import com.thepastimers.Coord.CoordData;
 import com.thepastimers.Database.Database;
 import com.thepastimers.ItemName.ItemName;
 import com.thepastimers.Money.Money;
 import com.thepastimers.Permission.Permission;
 import com.thepastimers.Rank.Rank;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 3/4/13
  * Time: 6:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TradeSign extends JavaPlugin implements Listener {
     Database database;
     Rank rank;
     Money money;
     ItemName itemName;
     Permission permission;
     Coord coord;
 
     @Override
     public void onEnable() {
         getLogger().info("TradeSign init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         rank = (Rank)getServer().getPluginManager().getPlugin("Rank");
 
         if (rank == null) {
             getLogger().warning("Unable to load Rank plugin. Some functionality will not be available.");
         }
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
 
         if (database == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality will not be available.");
         }
 
         money = (Money)getServer().getPluginManager().getPlugin("Money");
 
         if (money == null) {
             getLogger().warning("Unable to load Money plugin. Some functionality will not be available.");
         }
 
         itemName = (ItemName)getServer().getPluginManager().getPlugin("ItemName");
         if (itemName == null) {
             getLogger().warning("Unable to load ItemName plugin. Some functionality will not be available.");
         }
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
         if (permission == null) {
             getLogger().warning("Unable to load Permission plugin. Some functionality will not be available.");
         }
 
         coord = (Coord)getServer().getPluginManager().getPlugin("Coord");
         if (coord == null) {
             getLogger().warning("Unable to load Coord plugin. Some functionality will not be available.");
         }
 
         getLogger().info("Table info: ");
         getLogger().info(SignData.getTableInfo());
         SignData.refreshCache(database,getLogger());
 
         getLogger().info("TradeSign init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("TradeSign disabled");
     }
 
     public SignData getSignAt(int x, int y, int z, String world) {
         if (database == null) {
             return null;
         }
 
         List<SignData> signDataList = (List<SignData>)database.select(SignData.class,"x = " + x + " and y = " + y + " and z = " + z + " and world = '" + database.makeSafe(world) + "'");
 
         if (signDataList.size() == 0) {
             return null;
         }
 
         return signDataList.get(0);
     }
 
     @EventHandler
     public void placeSign(SignChangeEvent event) {
         //getLogger().info(event.getBlock().getType().name());
         //if (event.getBlock().getType() == Material.WALL_SIGN) {
             //if ("[sell]".equalsIgnoreCase(event.getLine(0))) {
                // event.getPlayer().sendMessage(ChatColor.RED + "Please note that wall signs do not properly work as trade signs yet");
                // return;
             //}
         //}
         if (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
             String[] lines = handleSign((Sign)event.getBlock().getState(),event.getPlayer(),event.getLines());
             for (int i=0;i<lines.length;i++) {
                 event.setLine(i,lines[i]);
             }
         }
     }
 
     @EventHandler
     public void breakSign(BlockBreakEvent event) {
         if (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
             SignData data = getSignAt(event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ(),event.getBlock().getWorld().getName());
             if (data != null) {
                 //getLogger().info("Trade sign broken");
                 if (event.getPlayer().getName().equals(data.getPlayer())) {
                     Player p = event.getPlayer();
                     if (itemName == null) {
                         p.sendMessage(ChatColor.RED + "This action is not currently possible");
                         event.setCancelled(true);
                     } else {
                        if ("".equalsIgnoreCase(data.getContains()) || itemName.giveItem(p,data.getContains(),data.getAmount())) {
                             if (!data.delete(database)) {
                                 p.sendMessage(ChatColor.RED + "Unable to remove sign");
                                 itemName.takeItem(p,data.getContains(),data.getAmount());
                             } else {
                                 p.sendMessage(ChatColor.GREEN + "Successfully emptied and removed sign!");
                             }
                         } else {
                             p.sendMessage(ChatColor.RED + "Could not empty sign into your inventory");
                             event.setCancelled(true);
                         }
                     }
                 } else {
                     event.getPlayer().sendMessage(ChatColor.RED + "This block is marked as a trade sign. Only the owner can break a trade sign.");
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler
     public void playerInteract(BlockDamageEvent event) {
         handleBlock(event.getPlayer(),event.getBlock());
     }
 
     private boolean handleBlock(Player p, Block b) {
         if (b.getType() == Material.SIGN || b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
             Location l = b.getLocation();
             SignData data = getSignAt(l.getBlockX(),l.getBlockY(),l.getBlockZ(),l.getWorld().getName());
             if (data != null) {
                 getLogger().info("Handle purchase event: " + b);
                 handlePurchase(p,data,b);
                 return true;
             }
         }
 
         return false;
     }
 
     private String[] handleSign(Sign s, Player p, String[] lines) {
         String line1 = lines[0];
         String line2 = lines[1];
 
         String[] retLines = new String[4];
 
         //for (int i=0;i<s.getLines().length;i++) {
         //    getLogger().info(i + " " + s.getLines()[i]);
         //}
         if ("[SELL]".equalsIgnoreCase(line1)) {
 
             if (permission == null || !permission.hasPermission(p.getName(),"sign_place")) {
                 p.sendMessage(ChatColor.RED + "You do not have permission to create trade signs (sign_place)");
                 return lines;
             }
 
             if (!"economy".equalsIgnoreCase(p.getWorld().getName())){
                 p.sendMessage(ChatColor.RED + "You can only place trade signs in the economy world.");
                 return lines;
             }
 
             // creating a sell sign!
             String contains = "";
             if ("XP".equalsIgnoreCase(line2)) {
                 contains = "XP";
             }
 
             SignData sd = new SignData();
             sd.setAmount(0);
             sd.setCost(0);
             sd.setDispense(0);
             sd.setContains(contains);
             sd.setX(s.getX());
             sd.setY(s.getY());
             sd.setZ(s.getZ());
             sd.setPlayer(p.getName());
             sd.setWorld(s.getWorld().getName());
 
             if (sd.save(database)) {
                 p.sendMessage(ChatColor.GREEN + "Successfully created trade sign");
                 retLines[0] = "[SELL]";
                 if ("".equals(contains)) {
                     contains = "EMPTY";
                 }
                 retLines[1] = contains;
                 s.update();
             } else {
                 p.sendMessage(ChatColor.RED + "Failed to create sign");
             }
         } else {
             return lines;
         }
 
         return retLines;
     }
 
     public void handlePurchase(Player p, SignData data, Block b) {
         if (p.getGameMode() == GameMode.CREATIVE) {
             p.sendMessage(ChatColor.RED + "This action cannot be taken while in creative mode.");
             return;
         }
         Sign s = (Sign)b.getState();
         boolean owner = false;
         if (p.getName().equalsIgnoreCase(data.getPlayer())) {
             ItemStack is = p.getItemInHand();
             boolean purchase = false;
             if (is == null || is.getType() == Material.AIR) {
                 purchase = true;
             }
             //getLogger().info("Player is owner of sign");
 
             owner = true;
             if (!purchase) {
                 //getLogger().info("Owner is adding");
                 if (itemName == null) {
                     p.sendMessage(ChatColor.RED + "That action is not possible at this time");
                     return;
                 }
                 String item = itemName.getItemName(is);
                 if ("".equals(data.getContains()) || data.getContains() == null) {
                     data.setContains(item);
                 } else {
                     if (!item.equalsIgnoreCase(data.getContains())) {
                         p.sendMessage(ChatColor.RED + "This sign already contains " + data.getContains());
                         return;
                     }
                 }

                if (itemName.isEnchanted(is)) {
                    p.sendMessage(ChatColor.RED + "You cannot sell this item in a trade sign");
                    return;
                }

                 int count = itemName.countInInventory(item,p.getName());
                 if (itemName.takeItem(p,item,count,true)) {
                     data.setAmount(data.getAmount() + count);
 
                     if (!data.save(database)) {
                         p.sendMessage(ChatColor.RED + "Unable to update sign");
                         itemName.giveItem(p,item,count);
                     } else {
                         p.sendMessage(ChatColor.GREEN + "Sign filled!");
                         s.setLine(1,item);
                         s.setLine(2,"Has: " + data.getAmount());
                         s.update();
                         getLogger().info("Owner added " + count);
                     }
                 } else {
                     p.sendMessage(ChatColor.RED + "Unable to remove items from your inventory");
                 }
                 return;
             }
         }
 
         if (data.getDispense() <= 0 || data.getCost() <= 0) {
             p.sendMessage(ChatColor.RED + "This sign does not have a cost/dispense amount set.");
             return;
         }
         if (data.getAmount() >= data.getDispense()) {
             if ((money == null || money.getBalance(p.getName()) < data.getCost()) && !owner) {
                 p.sendMessage(ChatColor.RED + "You don't have enough money to make this purchase");
                 return;
             }
 
             if (itemName == null || !itemName.giveItem(p,data.getContains(),data.getDispense())) {
                 p.sendMessage(ChatColor.RED + "Unable to take item from sign");
                 return;
             }
 
             if (!owner) {
                 if (!money.setBalance(p.getName(),money.getBalance(p.getName())-data.getCost())) {
                     p.sendMessage(ChatColor.RED + "Unable to complete transaction-internal failure");
                     itemName.takeItem(p,data.getContains(),data.getDispense());
                     return;
                 }
                 money.setBalance(data.getPlayer(),money.getBalance(data.getPlayer())+data.getCost());
             }
 
             data.setAmount(data.getAmount()-data.getDispense());
             if (!data.save(database)) {
                 p.sendMessage(ChatColor.RED + "Unable to update sign");
                 itemName.takeItem(p,data.getContains(),data.getDispense());
                 money.setBalance(p.getName(),money.getBalance(p.getName())+data.getCost());
             } else {
                 s.setLine(2,"Has: " + data.getAmount());
                 s.update();
                 String purchaseString = ChatColor.GREEN + "You have purchased " + data.getDispense() + " " + data.getContains() + " for $" + data.getCost();
                 if (owner) {
                     purchaseString = ChatColor.GREEN + "You have removed " + data.getDispense() + " " + data.getContains() + " from this trade sign";
                 } else {
                     Purchase purchase = new Purchase();
                     purchase.setPurchaser(p.getName());
                     purchase.setFromPlayer(data.getPlayer());
                     purchase.setItem(data.getContains());
                     purchase.setAmount(data.getCostAsInt());
                     purchase.setCount(data.getDispense());
                     purchase.setSign(data.getId());
                     Date date = new Date();
                     purchase.setTime(new Timestamp(date.getTime()));
                     if (!purchase.save(database)) {
                         getLogger().warning("Unable to save purchase to database.");
                     }
                 }
                 p.sendMessage(purchaseString);
                 getLogger().info(p.getName() + " purchased " + data.getDispense() + " from sign at (" + data.getX() + "," + data.getY() + "," + data.getZ() + ")");
             }
         } else {
             p.sendMessage(ChatColor.RED + "This sign does not have enough in it to dispense");
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         String command = cmd.getName();
 
         if ("sign".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"sign_sign")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (sign_sign)");
                 return true;
             }
 
             if (args.length > 0) {
                 String subCommand = args[0];
                 if ("format".equalsIgnoreCase(subCommand)) {
                     sender.sendMessage("Format for an item sell sign:");
                     sender.sendMessage("Line 1: [SELL]");
                     sender.sendMessage("Then to set a price, use /sign price");
                     //sender.sendMessage("To sell XP in a sign:");
                     //sender.sendMessage("Line 1: [SELL]");
                     //sender.sendMessage("Line 2: XP");
                     sender.sendMessage("To fill a sign, hold the item in your hand and left click the sign. All items of that type in your inventory will be put into the sign");
                     sender.sendMessage("To purchase/withdraw from a sign, if you are the owner, left click the sign with an empty hand. You will not be charged.");
                     sender.sendMessage("Anyone else purchasing from the sign can left click it with anything in their hand");
                     sender.sendMessage(ChatColor.RED + "Please report any unusual behavior from any trade sign to a staff member");
                 } else if ("price".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"sign_place") || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (sign_place)");
                         return true;
                     }
 
                     if (args.length > 2) {
                         double price;
                         int dispense;
                         try {
                             price = Double.parseDouble(args[1]);
                         } catch (NumberFormatException e) {
                             sender.sendMessage(ChatColor.RED + "Price needs to be a number");
                             return true;
                         }
                         try {
                             dispense = Integer.parseInt(args[2]);
                         } catch (NumberFormatException e) {
                             sender.sendMessage(ChatColor.RED + "Dispense needs to be a number");
                             return true;
                         }
                         if (coord == null) {
                             sender.sendMessage(ChatColor.RED + "This action is currently unavailable");
                             return true;
                         }
 
                         List<CoordData> coordDataList = coord.popCoords(playerName,1);
                         if (coordDataList.size() < 1) {
                             sender.sendMessage(ChatColor.RED + "You must have 1 coordinate set to use this action");
                             return true;
                         }
                         int x = (int)Math.floor(coordDataList.get(0).getX());
                         int y = (int)Math.floor(coordDataList.get(0).getY());
                         int z = (int)Math.floor(coordDataList.get(0).getZ());
 
                         Player p = (Player)sender;
 
                         SignData sd = getSignAt(x,y,z,p.getWorld().getName());
                         if (sd == null) {
                             sender.sendMessage(ChatColor.RED + "There is no set trade sign at (" + x + "," + y + "," + z + ")");
                             return true;
                         }
 
                         if (!playerName.equalsIgnoreCase(sd.getPlayer())) {
                             sender.sendMessage(ChatColor.RED + "You do not have permission to edit this sign");
                             return true;
                         }
 
                         sd.setDispense(dispense);
                         sd.setCost(price);
 
                         if (!sd.save(database)) {
                             sender.sendMessage(ChatColor.RED + "Unable to update sign");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + "Sign updated!");
                             Block b = getServer().getWorld(sd.getWorld()).getBlockAt(sd.getX(),sd.getY(),sd.getZ());
                             try {
                                 Sign s = (Sign)b.getState();
                                 s.setLine(3,"$" + sd.getCost() + " for " + sd.getDispense());
                                 s.update();
                             } catch (Exception e) {
                                 sender.sendMessage(ChatColor.RED + "Unable to update sign message");
                             }
                         }
                     } else {
                         sender.sendMessage("/sign price <price> <dispense amount>");
                     }
                 } else if ("report".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"sign_place")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (sign_place)");
                         return true;
                     }
 
                     String player = playerName;
 
                     if (args.length > 2) {
                         if (permission == null || !permission.hasPermission(playerName,"sign_report_other")) {
                             sender.sendMessage(ChatColor.RED + "You don't have permission to do this (sign_report_other)");
                             return true;
                         }
                         player = args[1];
                     }
 
                     List<Purchase> purchaseList = (List<Purchase>)database.select(Purchase.class,"from_player = '" + database.makeSafe(player) + "' and `time` >= NOW() - interval 1 day");
 
                     Map<String,Integer> amountMap = new HashMap<String, Integer>();
                     Map<String,Integer> revenueMap = new HashMap<String, Integer>();
 
                     for (Purchase purchase : purchaseList) {
                         if (!amountMap.containsKey(purchase.getItem())) {
                             amountMap.put(purchase.getItem(),0);
                         }
                         if (!revenueMap.containsKey(purchase.getItem())) {
                             revenueMap.put(purchase.getItem(),0);
                         }
                         amountMap.put(purchase.getItem(),amountMap.get(purchase.getItem())+purchase.getCount());
                         revenueMap.put(purchase.getItem(),revenueMap.get(purchase.getItem())+purchase.getAmount());
                     }
 
                     sender.sendMessage("Your trade sign revenue for the last 24 hours:");
                     for (String key : amountMap.keySet()) {
                         sender.sendMessage(amountMap.get(key) + " " + key + " for $" + ((double)revenueMap.get(key))/100);
                     }
                 } else if("reload".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"sign_reload")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (sign_reload)");
                         return true;
                     }
 
                     List<SignData> signDataList = (List<SignData>)database.select(SignData.class,"");
 
                     for (SignData sd : signDataList) {
                         Block b = getServer().getWorld(sd.getWorld()).getBlockAt(sd.getX(),sd.getY(),sd.getZ());
                         if (b.getType() ==  Material.SIGN_POST || b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN) {
                             Sign s = (Sign)b.getState();
                             s.setLine(0,"[SELL]");
                             s.setLine(1,sd.getContains());
                             s.setLine(2,"Has: " + sd.getAmount());
                             s.setLine(3,"$" + sd.getCost() + " for " + sd.getDispense());
                             s.update();
                         }
                     }
 
                 }
             } else {
                 sender.sendMessage("/sign <format" + /*|price|restore*/ "|report|reload>");
             }
         } else {
             return false;
         }
 
         return true;
     }
 }
