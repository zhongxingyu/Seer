 package me.ellbristow.broker;
 
 import java.io.IOException;
import java.util.*;
 import org.bukkit.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Broker extends JavaPlugin {
     
     private static FileConfiguration config;
     private String[] tableColumns = {"id", "playerName", "orderType", "timeCode", "itemName", "enchantments", "damage", "price", "quant", "perItems", "meta"};
     private String[] tableDims = {"INTEGER PRIMARY KEY ASC AUTOINCREMENT", "TEXT NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL", "TEXT NOT NULL", "TEXT", "INTEGER NOT NULL DEFAULT 0", "DOUBLE NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL DEFAULT 1", "TEXT NOT NULL DEFAULT ''"};
     protected vaultBridge vault;
     protected BrokerDb brokerDb;
     protected HashMap<String,HashMap<ItemStack,String>> pending = new HashMap<String, HashMap<ItemStack,String>>();
     
     protected double taxRate;
     protected boolean taxIsPercentage;
     protected double taxMinimum;
     protected int maxOrders;
     protected int vipMaxOrders;
     protected boolean brokerVillagers;
     protected boolean brokerPlayers;
 
     @Override
     public void onEnable() {
         config = getConfig();
         
         taxRate = config.getDouble("salesTaxRate", 0.0);
         config.set("salesTaxRate", taxRate);
         taxIsPercentage = config.getBoolean("taxIsPercentage", false);
         config.set("taxIsPercentage", taxIsPercentage);
         taxMinimum = config.getDouble("minimumTaxable", 0.0);
         config.set("minimumTaxable", taxMinimum);
         maxOrders = config.getInt("maxOrdersPerPlayer", 0);
         config.set("maxOrdersPerPlayer", maxOrders);
         vipMaxOrders = config.getInt("vipMaxOrders", 0);
         config.set("vipMaxOrders", vipMaxOrders);
         brokerVillagers = config.getBoolean("villagersAreBrokers", true);
         config.set("villagersAreBrokers", brokerVillagers);
         brokerPlayers = config.getBoolean("playersAreBrokers", true);
         config.set("playersAreBrokers", brokerPlayers);
         
         saveConfig();
         
         vault = new vaultBridge(this);
         if (vault.foundEconomy == false) {
             getLogger().severe("Could not find an Economy Plugin via [Vault]!");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         brokerDb = new BrokerDb(this);
         brokerDb.getConnection();
         if (!brokerDb.checkTable("BrokerOrders")) {
             brokerDb.createTable("BrokerOrders", tableColumns, tableDims);
         } else {
             if (!brokerDb.tableContainsColumn("BrokerOrders", "perItems")) {
                 brokerDb.addColumn("BrokerOrders", "perItems INTEGER NOT NULL DEFAULT 1");
             }
             if (!brokerDb.tableContainsColumn("BrokerOrders", "meta")) {
                 brokerDb.addColumn("BrokerOrders", "meta TEXT NOT NULL DEFAULT ''");
             }
         }
         getServer().getPluginManager().registerEvents(new BrokerListener(this), this);
         
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
             // Failed to submit the stats :-(
         }
         
     }
 
     @Override
     public void onDisable() {
         if (brokerDb != null)
             brokerDb.close();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (commandLabel.equalsIgnoreCase("broker")) {
             
             if (!sender.hasPermission("broker.use")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use " + ChatColor.GOLD + "/broker");
                 return false;
             }
             
             if (args.length == 0) {
                 // Command Help
                 sender.sendMessage(ChatColor.GOLD + "== Broker v" + ChatColor.WHITE + getDescription().getVersion() + ChatColor.GOLD + " by " + ChatColor.WHITE + "ellbristow" + ChatColor.GOLD + " ==");
                 if (sender.hasPermission("broker.commands.buy") || sender.hasPermission("broker.commands.sell")) {
                     sender.sendMessage(ChatColor.GRAY + "{optional} [required]");
                 }
                 if (sender.hasPermission("broker.commands.buy")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker buy {Seller Name}"+ChatColor.GRAY + ": Open the broker window to buy items");
                 }
                 if (sender.hasPermission("broker.commands.sell")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker sell [Price] {Per # Items}");
                     sender.sendMessage(ChatColor.GRAY + " List the item in your hand for sale");
                     sender.sendMessage(ChatColor.GRAY + " Optional: how many items for this price");
                     sender.sendMessage(ChatColor.GRAY + " Either set a price or sell to the highest current bidder");
                     sender.sendMessage(ChatColor.GOLD + "/broker sell cancel" + ChatColor.GRAY + ": Cancel an existing sell order");
                 }
                 return true;
             }
             
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.RED + "You cannot use broker from the console!");
                 return false;
             }
             
             Player player = (Player) sender;
             
             // MAIN PROCESS
             
             if (args.length >= 1) {
                 if (args[0].equalsIgnoreCase("buy")) {
                     // Buy help
                     if (!sender.hasPermission("broker.commands.buy")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use the buy command!");
                         return false;
                     }
                     if (args.length >= 2) {
                         OfflinePlayer seller = getServer().getOfflinePlayer(args[1]);
                         if (!seller.hasPlayedBefore()) {
                             player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + args[1] + ChatColor.RED + " not found!");
                             return false;
                         }
                         player.openInventory(getBrokerInv("0", player, seller.getName()));
                     } else {
                         player.openInventory(getBrokerInv("0", player, null));
                     }
                     pending.remove(player.getName());
                     player.sendMessage(ChatColor.GOLD + "<BROKER> Main Page");
                     player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
                     return true;
                 } else if (args[0].equalsIgnoreCase("sell")) {
                     if (!sender.hasPermission("broker.commands.sell")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use the sell command!");
                         return false;
                     }
                     ItemStack itemInHand = player.getItemInHand();
                     if (args.length == 1) {
                         if (itemInHand == null || itemInHand.getTypeId() == 0) {
                             sender.sendMessage(ChatColor.RED + "You're not holding anything to sell!");
                             return false;
                         }
                         sender.sendMessage(ChatColor.RED + "You must set a price to sell items!");
                         sender.sendMessage(ChatColor.RED + "Try:" + ChatColor.WHITE + "/broker sell [price] {Per # Items}!");
                     } else if (args.length >= 2) {
                         if (args[1] != null && args[1].equalsIgnoreCase("cancel")) {
                             if (!player.hasPermission("broker.commands.cancel")) {
                                 player.sendMessage(ChatColor.RED + "You do not have permission to cancel orders!");
                                 return true;
                             }
                             player.sendMessage(ChatColor.GOLD + "Cancelling Sell Orders");
                             player.openInventory(getBrokerInv("0", player, player.getName()));
                             return true;
                         }
                         if ((player.hasPermission("broker.vip") && vipMaxOrders != 0 && sellOrderCount(player.getName()) >= vipMaxOrders) || (!player.hasPermission("broker.vip") && maxOrders != 0 && sellOrderCount(player.getName()) >= maxOrders)) {
                             sender.sendMessage(ChatColor.RED + "You may only place a maximum of "+ChatColor.WHITE+vipMaxOrders+ChatColor.RED+" sale orders!");
                             return false;
                         }
                         if (itemInHand == null || itemInHand.getTypeId() == 0) {
                             sender.sendMessage(ChatColor.RED + "You're not holding anything to sell!");
                             return false;
                         }
                         double price;
                         try {
                             price = Double.parseDouble(args[1]);
                         } catch (NumberFormatException nfe) {
                             sender.sendMessage(ChatColor.RED + "Sale price must be a number!");
                             sender.sendMessage(ChatColor.RED + "/broker sell [price] {Per # Items}");
                             return false;
                         }
                         
                         int perItems = 1;
                         String each = "each";
                         if (args.length > 2) {
                             try {
                                 perItems = Integer.parseInt(args[2]);
                                 each = "per " + perItems + " items";
                             } catch (NumberFormatException nfe) {
                                 sender.sendMessage(ChatColor.RED + "Per # Items must be a whole number!");
                                 sender.sendMessage(ChatColor.RED + "/broker sell [price] {Per # Items}");
                                 return false;
                             }
                         }
                         if (perItems <1) {
                             perItems = 1;
                         }
                         
                         // List item for sale
                         int quant = (int)(itemInHand.getAmount() / perItems) * perItems;
                         String itemName = itemInHand.getType().name();
                         String itemDisplayName = itemName;
                         short damage = itemInHand.getDurability();
                         Map<Enchantment, Integer> enchantments = itemInHand.getEnchantments();
                         String enchantmentString = "";
                         if (!enchantments.isEmpty()) {
                             itemDisplayName += "(Enchanted)";
                             Object[] enchs = enchantments.keySet().toArray();
                             for (Object ench : enchs) {
                                 if (!"".equals(enchantmentString)) {
                                     enchantmentString += ";";
                                 }
                                 enchantmentString += ((Enchantment)ench).getId() + "@" + enchantments.get((Enchantment)ench);
                             }
                         }
                         String meta = "";
                         if (itemInHand.hasItemMeta()) {
                             ItemMeta itemMeta = itemInHand.getItemMeta();
                             if (itemMeta instanceof BookMeta) {
                                 BookMeta book = (BookMeta)itemMeta;
                                 meta = "BOOK:META:"+book.getTitle() + ":META:" + book.getAuthor()+ ":META:";
                                 String pages = "";
                                 for (String page : book.getPages()) {
                                     if (!pages.equals("")) {
                                         pages += ":PAGE:";
                                     }
                                     pages += page;
                                 }
                                 meta += pages;
                             } else if (itemMeta instanceof LeatherArmorMeta) {
                                 LeatherArmorMeta armor = (LeatherArmorMeta)itemMeta;
                                meta = "ARMOR:META:" + armor.getDisplayName() + ":META:" + armor.getColor().getRed() + ":META:" + armor.getColor().getGreen()  + ":META:" + armor.getColor().getBlue();
                             } else if (itemMeta instanceof MapMeta) {
                                 MapMeta map = (MapMeta)itemMeta;
                                meta = "MAP:META:" + map.getDisplayName() + ":META:" + map.isScaling();
                             } else if (itemMeta.hasDisplayName()) {
                                 meta = "ITEM:META:" + itemMeta.getDisplayName();
                             }                              
                         }
                         if (isDamageableItem(itemInHand) && damage != 0) {
                             itemDisplayName += "(Used)";
                         }
                         String query = "INSERT INTO BrokerOrders (orderType, playerName, itemName, enchantments, damage, price, quant, timeCode, perItems, meta) VALUES (0, '" + player.getName() + "', '" + itemName + "', '" + enchantmentString + "', " + damage + ", " + price + ", " + quant + ", " + new Date().getTime() + ", "+perItems+", '"+meta+"')";
                         brokerDb.query(query);
                         if (itemInHand.getAmount() > quant) {
                             player.getItemInHand().setAmount(player.getItemInHand().getAmount() - quant);
                         } else {
                             player.setItemInHand(null);
                         }
                         player.sendMessage(quant + " x " + itemDisplayName);
                         player.sendMessage(ChatColor.GOLD + " listed for sale for " + ChatColor.GREEN + vault.economy.format(price) + ChatColor.GOLD + " " + each + "!");
                     }
                     return true;
                 } else {
                     sender.sendMessage(ChatColor.RED + "Um... sorry... I don't recognise "+ChatColor.WHITE+args[0]+ChatColor.RED+"!");
                     return true;
                 }
             }
         }
         sender.sendMessage(ChatColor.RED + "Command not recognised! Type " + ChatColor.GOLD + "/broker" + ChatColor.RED + " for help");
         return false;
     }
     
     protected Inventory getBrokerInv(String page, Player buyer, String seller) {
         Inventory inv;
         if (seller == null || seller.equals("")) {
             inv = Bukkit.createInventory(buyer, 54, "<Broker> Buy");
         } else if (seller.equalsIgnoreCase(buyer.getName())) {
             inv = Bukkit.createInventory(buyer, 54, "<Broker> Cancel");
         } else {
             inv = Bukkit.createInventory(buyer, 54, "<Broker> " + seller);
         }
         inv.setMaxStackSize(64);
         for (int i = 45; i < 54; i++) {
             inv.setItem(i, new ItemStack(Material.ENDER_PORTAL));
         }
         int pageNo;
         boolean mainPage;
         if (!page.contains("::")) {
             pageNo = Integer.parseInt(page);
             mainPage = true;
         } else {
             pageNo = Integer.parseInt(page.split("::")[1]);
             mainPage = false;
         }
         if (!mainPage) {
             // Load Sub Page
             String[] pageSplit = page.split("::");
             HashMap<Integer, HashMap<String, Object>> sellOrders;
             String[] subSplit = pageSplit[0].split(":");
             if (isDamageableItem(new ItemStack(Material.getMaterial(subSplit[0])))) {
                 if (seller == null || seller.equals("")) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = 0", "price, perItems, damage, enchantments, meta", "price/perItems ASC, damage ASC");
                 } else {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "playerName = '" + seller + "' AND itemName = '" + subSplit[0] + "' AND orderType = 0", "price, perItems, damage, enchantments,meta", "price/perItems ASC, damage ASC");
                 }
             } else {
                 if (seller == null || seller.equals("")) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta","BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = 0 AND damage = "+subSplit[1], "price, perItems, damage, enchantments,meta", "price/perItems ASC, damage ASC");
                 } else {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta","BrokerOrders", "playerName = '" + seller + "' AND itemName = '" + subSplit[0] + "' AND orderType = 0 AND damage = "+subSplit[1], "price, perItems, damage, enchantments,meta", "price/perItems ASC, damage ASC");
                 }
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                 int added = 0;
                 for (int i = 0; i < rows; i++) {
                     // Items may vary (Show top 45 prices)
                     if (added < 45) {
                         ItemStack stack = new ItemStack(Material.getMaterial((String)sellOrders.get(i).get("itemName")));
                         stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                         if ((String)sellOrders.get(i).get("enchantments") != null && !"".equals((String)sellOrders.get(i).get("enchantments"))) {
                             String[] enchSplit = ((String)sellOrders.get(i).get("enchantments")).split(";");
                             for (String ench : enchSplit) {
                                 String[] enchantment = ench.split("@");
                                 stack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(enchantment[0])), Integer.parseInt(enchantment[1]));
                             }
                         }
                         stack.setAmount((Integer)sellOrders.get(i).get("totquant"));
                         String meta = (String)sellOrders.get(i).get("meta");
                         if (!meta.equals("")) {
                             String[] metaSplit = meta.split(":META:");
                             if (metaSplit[0].equals("BOOK")) {
                                 BookMeta book = (BookMeta)stack.getItemMeta();
                                 book.setTitle(metaSplit[1]);
                                 book.setAuthor(metaSplit[2]);
                                 String[] pages = metaSplit[3].split(":PAGE:");
                                 for (int p = 0; p < pages.length; p++) {
                                     book.addPage(pages[p]);
                                 }
                                 stack.setItemMeta(book);
                             } else if (metaSplit[0].equals("ARMOR")) {
                                 LeatherArmorMeta armor = (LeatherArmorMeta)stack.getItemMeta();
                                 armor.setColor(Color.fromRGB(Integer.parseInt(metaSplit[2]), Integer.parseInt(metaSplit[3]), Integer.parseInt(metaSplit[4])));
                                armor.setDisplayName(metaSplit[1]);
                                 stack.setItemMeta(armor);
                             } else if (metaSplit[0].equals("MAP")) {
                                 MapMeta map = (MapMeta)stack.getItemMeta();
                                map.setDisplayName(metaSplit[1]);
                                 map.setScaling(Boolean.parseBoolean(metaSplit[2]));
                                 stack.setItemMeta(map);
                             } else if (metaSplit[0].equals("ITEM")) {
                                 stack.getItemMeta().setDisplayName(metaSplit[1]);
                             }
                         }
                         inv.setItem(added, stack);
                         added++;
                     }
                     inv.setItem(45, new ItemStack(Material.BOOK));
                     if (rows > 45) {
                         int pageCount = 0;
                         while (rows > 0) {
                             pageCount++;
                             rows -= 45;
                         }
                         for (int j = 1; j < pageCount; j++) {
                             if (j<8) {
                                 inv.setItem(j+45, new ItemStack(Material.PAPER));
                             }
                         }
                     }
                 }
             }
         } else {
             // Load Main Page
             HashMap<Integer, HashMap<String, Object>> sellOrders;
             if (seller == null || seller.equals("")) {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "orderType = 0", "itemName, damage", "itemName, damage ASC");
             } else {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "playerName = '" + seller + "' AND orderType = 0", "itemName, damage", "itemName, damage ASC");
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                 int checkedrows = 0;
                 int added = 0;
                 String lastItem = "";
                 int skipped = 0;
                 for (int i = 0; i < rows; i++) {
                     if (added < 45) {
                         ItemStack stack = new ItemStack(Material.getMaterial((String)sellOrders.get(i).get("itemName")));
                         if (!isDamageableItem(stack)) {
                             stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                         } else {
                             if (((String)sellOrders.get(i).get("itemName")).equals(lastItem)) {
                                 skipped++;
                             }
                         }
                         if (!inv.contains(stack) && checkedrows >= pageNo * (45 + skipped)) {
                             inv.addItem(stack);
                             added++;
                         }
                         lastItem = (String)sellOrders.get(i).get("itemName");
                         checkedrows++;
                     }
                 }
                 rows -= skipped;
                 if (rows >= 45) {
                     int pageCount = 0;
                     while (rows > 0) {
                         pageCount++;
                         rows -= 45;
                     }
                     for (int i = 0; i < pageCount; i++) {
                         if (i<9) {
                             inv.setItem(i+45, new ItemStack(Material.PAPER));
                         }
                     }
                 }
             }
         }
         return inv;
     }
     
     protected boolean isDamageableItem(ItemStack stack) {
         int typeId = stack.getTypeId();
         if ((typeId >= 256 && typeId <= 258) || typeId == 259 || typeId == 261 || (typeId >= 267 && typeId <= 279) || (typeId >= 283 && typeId <= 286) || (typeId >= 290 && typeId <= 294) || (typeId >= 298 && typeId <= 317) || typeId == 359) {
             return true;
         }
         return false;
     }
     
     protected int sellOrderCount(String playerName) {
         HashMap<Integer, HashMap<String, Object>> sellOrders = brokerDb.select("id", "BrokerOrders", "playerName = '" + playerName + "' AND orderType = 0", null, null);
         return sellOrders.size();
     }
     
 }
