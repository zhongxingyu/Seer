 
 package us.azkedar;
 
 // We use the guts of VirtualShop's DB, so as not to reinvent the wheel
 // That is, we pull its configuration, and then run our own connection, since
 // VS doesn't want us messing with theirs. :-P
 
 import org.blockface.virtualshop.managers.ConfigManager;
 import org.blockface.virtualshop.managers.EconomyManager;
 import org.blockface.virtualshop.util.InventoryManager;
 import org.bukkit.inventory.ItemStack;
 
 import lib.PatPeter.SQLibrary.*;
 import com.LRFLEW.register.payment.Method;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.command.CommandSender;
 import org.bukkit.ChatColor;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 import java.util.ArrayList;
 
 public class ItemExchangeDB {
     private static MySQL  mdb;
     private static SQLite sdb;
     private static Logger logger;
     
     private static int itemId;
     private static int itemDurability;
     private static double basePrice;
     private static double maxPrice;
     private static double minPrice;
     private static double priceIncrement;
     private static int    prevDays;
     private static String itemName;
     private static int    maxTransactionSize;
     private static int    transactionCooldown;
 
     public static void Initialize(Plugin plugin)
     {
         Configuration config = plugin.getConfiguration();
         config.load();
         itemName = config.getString("item-name","Gold");
         itemId   = config.getInt("item-id", 266 );
         itemDurability = config.getInt("item-durability", 0);
         basePrice = config.getDouble("base-price", 100.0);
         maxPrice = config.getDouble("max-price", 200.0);
         minPrice = config.getDouble("min-price", 50.0);
         priceIncrement = config.getDouble("price-increment",0.1);
         prevDays = config.getInt("prev-days", 7);
         maxTransactionSize = config.getInt("max-transaction-size",-1);
         transactionCooldown = config.getInt("transaction-cooldown", 0);
         logger = Logger.getLogger("minecraft");
         if(ConfigManager.UsingMySQL()) {
             // MySQL
              logger.info("Using MySQL.");
              mdb = new MySQL(logger, "[ItemExchange]", ConfigManager.MySQLHost(), ConfigManager.getPort().toString(), ConfigManager.MySQLdatabase(), ConfigManager.MySQLUserName(), ConfigManager.MySQLPassword());
              try {
                  mdb.open();
                  if(mdb.checkConnection())
                  {
                     logger.info("Successfully connected to MySQL Database");
                     if(!mdb.checkTable("exchange"))
                     {
                             String query = "create table exchange(`id` integer primary key auto_increment,`player` varchar(80) not null,`type` varchar(10), `price` float not null,`amount` integer not null, ts timestamp)";
                             mdb.createTable(query);
                             logger.info("Created exchange table.");
                     }
                     return;
                  }
              }
              catch (Exception e) { 
              }
             logger.info("Could not connect to MySQL Database. Check settings.");   
         } else {
            // SQLite
                 sdb = new SQLite(logger, "[ItemExchange]", "ItemExchange", "plugins/ItemExchange/");
 		sdb.open();
 		if(!sdb.checkConnection())
 		{
 			logger.info("FlatFile creation failed!");
                         return;
 		}
                 
 		logger.info("Using flat files.");
 		try {
                     if(!sdb.checkTable("exchange"))
                     {
                             String query = "create table exchange('id' integer primary key,'player' varchar(80) not null,'type' varchar(10), 'price' float not null,'amount' integer not null, ts datetime)";
                             sdb.createTable(query);
                             logger.info("Created exchange table.");
                     }
                 }
                 catch (Exception e) {
                 }
         }
     }
 
     public static void Close() {
         if(ConfigManager.UsingMySQL()) {
             mdb.close();
         } else {
             sdb.close();
         }
     }
     
     public static double current_rate() {
         ResultSet rs;
         int count = 0;
         try {
             if(ConfigManager.UsingMySQL()) {
                 rs = mdb.query(" select type, amount from exchange where ts >= DATE_SUB(CURDATE(), INTERVAL " + prevDays + " DAY)");
             } else {
                 rs = sdb.query(" select type, amount from exchange where ts >= (SELECT DATETIME('now', '-" + prevDays +" day'))");
             }
             while (rs.next()) {
                 if (rs.getString("type").equalsIgnoreCase("bought")) {
                     count += rs.getInt("amount");
                 } else {
                     count -= rs.getInt("amount");
                 }
             }
         } catch (Exception e) {
             logger.info("SQL Error during current_rate()");
         }
         double tempPrice = basePrice + count * priceIncrement;
         if (tempPrice > maxPrice) 
             return maxPrice;
         else if (tempPrice < minPrice) 
             return minPrice;
         else return tempPrice;
     }
     
     public static void history (CommandSender sender, int page) {
         ResultSet rs;
         int pageSize = 10;
         int count = 0;
         int offset = ((page-1) * pageSize);
         int maxPage = 1;
 
         try {
             if(ConfigManager.UsingMySQL()) {
                 rs = mdb.query(" select count(*) from exchange ");
                 if( rs.next() ) {
                     count = rs.getInt(1);
                     maxPage = ((count/pageSize)+1);
                 }
                 if( page < 1) {
                     sender.sendMessage("Please specify a valid page number greater than 0");
                     return;
                 } else if (page > maxPage ) {
                     sender.sendMessage("No records that far out, max page is " + maxPage);
                     return;
                 }
                 rs = mdb.query(" select player, type, amount, price from exchange order by ts desc limit " + pageSize + " offset " + offset);
             } else {
                 rs = sdb.query(" select count(*) from exchange ");
                 if( rs.next() ) {
                     count = rs.getInt(1);
                     maxPage = ((count/pageSize)+1); //NOOO! Copy-pasted code!  Cannot RESIST!!~!!1oneone
                 }
                 if( page < 1) {
                     sender.sendMessage("Please specify a valid page number greater than 0");
                     return;
                 } else if (page > maxPage ) {
                     sender.sendMessage("No records that far out, max page is " + maxPage);
                     return;
                 }
                 logger.info(" select player, type, amount, price from exchange order by ts desc limit " + pageSize + " offset " + offset);
                 rs = sdb.query(" select player, type, amount, price from exchange order by ts desc limit " + pageSize + " offset " + offset);
             }
             sender.sendMessage(ChatColor.BLUE + " |------- " + ChatColor.AQUA + "Page " + ChatColor.GREEN + page + ChatColor.AQUA + " of " + ChatColor.GREEN + maxPage + ChatColor.BLUE + " -------|");
             while (rs.next()) {
                 String type = rs.getString(2);
                 ChatColor rowcolor = ChatColor.GOLD;
                 if (type.equalsIgnoreCase("sold")) {
                     rowcolor = ChatColor.BLUE;
                 }
                 sender.sendMessage(String.format(ChatColor.GREEN + "%s  " + rowcolor + "%s " + ChatColor.WHITE + "%s " + rowcolor + itemName + " for " + ChatColor.WHITE + "%s",
                     rs.getString(1),
                     type,
                     String.format("%d",rs.getInt(3)),
                     EconomyManager.getMethod().format(rs.getDouble(4))));
                
             }
         } catch (Exception e) {
             logger.info("SQL Error during history():" + e.toString());
         }
     }
     
     private static boolean transactionCheck(CommandSender sender, int amount, String permission) {
         if(!(sender instanceof Player))
         {
             sender.sendMessage("Can't buy or sell from console.");
             return false;
         }        
         if (!sender.hasPermission(permission)) {
             sender.sendMessage("You don't have permission to do that.");
             return false;
         }
         if(maxTransactionSize > 0 && amount > maxTransactionSize) {
             sender.sendMessage("Transaction too large.  Maximum size:" + maxTransactionSize);
             return false;
         }
         else if (amount < 1) {
             sender.sendMessage("You must specify a quantity of at least 1.");
             return false;
         }
         try {
             ResultSet rs;
             if(ConfigManager.UsingMySQL()) {
                 rs = mdb.query(" select count(*) from exchange where player = '" + ((Player)sender).getName() + "' and ts >= DATE_SUB(NOW(), INTERVAL " + transactionCooldown + " SECOND)");
             } else {
                 rs = sdb.query(" select count(*) from exchange where player = '" + ((Player)sender).getName() + "' and ts >= (SELECT DATETIME('now', '-" + transactionCooldown +" second'))");
             }
             if (rs.next() && rs.getInt(1) > 0) {
                 sender.sendMessage("Transaction cooldown limit imposed.  You must wait " + transactionCooldown + " seconds between transactions.");
                 return false;
             }
         } catch (Exception e) {
             logger.info("SQL Error during current_rate()");
         }
         return true;
     }
     
     public static void buy (CommandSender sender, int amount) {
         if (!transactionCheck(sender,amount,"itemexchange.buy")) {
             return;
         }
         Player player = (Player)sender;
         Method.MethodAccount account = EconomyManager.getMethod().getAccount(player.getName());
         double rate        = current_rate();
         double totalCost   = 0;
         int    amountLeft  = amount;
 
         while (amountLeft > 0) {
             rate += priceIncrement;      
            
            if (!account.hasEnough(rate)) {
                 sender.sendMessage("Ran out of money!");
                 break;
             }
             
            totalCost += rate;
             amountLeft -= 1;
             
 
         }
 
         int actualAmount = amount - amountLeft;
         
         account.subtract(totalCost);
         InventoryManager im = new InventoryManager(player);
         ItemStack purchasedItem = new ItemStack(itemId,actualAmount,(short)itemDurability);
         im.addItem(purchasedItem).getAmount();
             
         if (actualAmount > 0) {
             try {
                  if(ConfigManager.UsingMySQL()) {
                     mdb.query(" insert into exchange (player,price,type,amount,ts) values ('" + player.getName() + "'," + totalCost + ",'bought'," + actualAmount + ",now())");
                 } else {
                     sdb.query(" insert into exchange (player,price,type,amount,ts) values ('" + player.getName() + "'," + totalCost + ",'bought'," + actualAmount + ",datetime('now'))");
                 }
             }
             catch (Exception e) {
                 logger.info("SQL Error during buy()");
             }
 
             sender.sendMessage(ChatColor.GOLD + "[Exchange] " + ChatColor.WHITE + " Bought " + ChatColor.GREEN + String.format("%d",actualAmount) + " " + ChatColor.AQUA + itemName + ChatColor.WHITE + " for " + ChatColor.YELLOW + EconomyManager.getMethod().format(totalCost));
         } else {
             sender.sendMessage("Unable to purchase any " + itemName + ".");
         }
     }
  
     public static void sell (CommandSender sender, int amount) {
         if (!transactionCheck(sender,amount,"itemexchange.sell")) {
             return;
         }
         Player player = (Player)sender;
         Method.MethodAccount account = EconomyManager.getMethod().getAccount(player.getName());
         InventoryManager im = new InventoryManager(player);
         ItemStack sellItem = new ItemStack(itemId,amount,(short)itemDurability);
         if (!im.contains(sellItem,true,true)) {
             sender.sendMessage("You don't have that quantity to sell.");
             return;
         }
 
         ItemStack purchasedItem = new ItemStack(itemId,amount,(short)itemDurability);
         im.remove(sellItem,true,true);
         int amountLeft = amount;
         double rate = current_rate();
         double totalCost = 0;
         
         while (amountLeft > 0) {
             amountLeft -= 1;
             totalCost += rate;
             rate -= priceIncrement;
         }
         
         account.add(totalCost);
 
         try {
              if(ConfigManager.UsingMySQL()) {
                 mdb.query(" insert into exchange (player,price,type,amount,ts) values ('" + player.getName() + "'," + totalCost + ",'sold'," + amount + ",now())");
             } else {
                 sdb.query(" insert into exchange (player,price,type,amount,ts) values ('" + player.getName() + "'," + totalCost + ",'sold'," + amount + ",datetime('now'))");
             }
         }
         catch (Exception e) {
             logger.info("SQL Error during buy()");
         }
 
         sender.sendMessage(ChatColor.GOLD + "[Exchange] " + ChatColor.WHITE + " Sold " + ChatColor.GREEN + String.format("%d",amount) + " " + ChatColor.AQUA + itemName + ChatColor.WHITE + " for " + ChatColor.YELLOW + EconomyManager.getMethod().format(totalCost));
     }
 }
