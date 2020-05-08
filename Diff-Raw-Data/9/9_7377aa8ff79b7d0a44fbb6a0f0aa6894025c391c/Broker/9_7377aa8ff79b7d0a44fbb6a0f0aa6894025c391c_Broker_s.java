 package me.ellbristow.broker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import me.ellbristow.broker.utils.BrokerDb;
 import me.ellbristow.broker.utils.ItemSerialization;
 import me.ellbristow.broker.utils.Metrics;
 import me.ellbristow.broker.utils.vaultBridge;
 import org.bukkit.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
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
     private String[] pendingColumns = {"id", "playerName", "itemName", "damage", "quant"};
     private String[] pendingDims = {"INTEGER PRIMARY KEY ASC AUTOINCREMENT", "TEXT NOT NULL", "TEXT NOT NULL", "INTEGER NOT NULL DEFAULT 0", "INTEGER NOT NULL DEFAULT 1"};
     protected vaultBridge vault;
     protected BrokerDb brokerDb;
     protected HashMap<String, HashMap<ItemStack, String>> pending = new HashMap<String, HashMap<ItemStack, String>>();
     protected double taxRate;
     protected boolean taxIsPercentage;
     protected double taxMinimum;
     protected boolean taxOnBuyOrders;
     protected int maxOrders;
     protected int vipMaxOrders;
     protected boolean brokerVillagers;
     protected boolean brokerPlayers;
     private HashMap<String, Object[]> itemAliases = new HashMap<String, Object[]>();
 
     @Override
     public void onEnable() {
         config = getConfig();
 
         taxRate = config.getDouble("salesTaxRate", 0.0);
         config.set("salesTaxRate", taxRate);
         taxIsPercentage = config.getBoolean("taxIsPercentage", false);
         config.set("taxIsPercentage", taxIsPercentage);
         taxMinimum = config.getDouble("minimumTaxable", 0.0);
         config.set("minimumTaxable", taxMinimum);
         taxOnBuyOrders = config.getBoolean("taxOnBuyOrders", true);
         config.set("taxOnBuyOrders", taxOnBuyOrders);
         maxOrders = config.getInt("maxOrdersPerPlayer", 0);
         config.set("maxOrdersPerPlayer", maxOrders);
         vipMaxOrders = config.getInt("vipMaxOrders", 0);
         config.set("vipMaxOrders", vipMaxOrders);
         brokerVillagers = config.getBoolean("villagersAreBrokers", true);
         config.set("villagersAreBrokers", brokerVillagers);
         brokerPlayers = config.getBoolean("playersAreBrokers", true);
         config.set("playersAreBrokers", brokerPlayers);
 
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
             boolean pre_1_6 = false;
             if (!brokerDb.tableContainsColumn("BrokerOrders", "perItems")) {
                 brokerDb.addColumn("BrokerOrders", "perItems INTEGER NOT NULL DEFAULT 1");
             }
             if (!brokerDb.tableContainsColumn("BrokerOrders", "meta")) {
                 brokerDb.addColumn("BrokerOrders", "meta TEXT NOT NULL DEFAULT ''");
             }
             if (config.getString("version") == null) {
                 convertMeta();
             }
         }
         
         if (!brokerDb.checkTable("BrokerPending")) {
             brokerDb.createTable("BrokerPending", pendingColumns, pendingDims);
         }
 
         config.set("version", getDescription().getVersion());
         saveConfig();
 
         loadAliases();
 
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
         if (brokerDb != null) {
             brokerDb.close();
         }
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
                     sender.sendMessage(ChatColor.GOLD + "/broker buy {Seller Name}" + ChatColor.GRAY + ": Open the broker window to buy items");
                 }
                 if (sender.hasPermission("broker.commands.buy.orders")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker buy [Item Name:ID{:data}] [Quantity] [Max Price Each]");
                     sender.sendMessage(ChatColor.GRAY + " Place a Buy Order");
                     sender.sendMessage(ChatColor.GOLD + "/broker buy cancel" + ChatColor.GRAY + ": Cancel a Buy Order");
                 }
                 if (sender.hasPermission("broker.commands.sell")) {
                     sender.sendMessage(ChatColor.GOLD + "/broker sell" + ChatColor.GRAY + ": Browse outstanding Buy Orders");
                     sender.sendMessage(ChatColor.GOLD + "/broker sell [Price] {Per # Items}");
                     sender.sendMessage(ChatColor.GRAY + " List the item in your hand for sale");
                     sender.sendMessage(ChatColor.GRAY + " Optional: how many items for this price");
                     sender.sendMessage(ChatColor.GRAY + " Either set a price or sell to the highest current bidder");
                     sender.sendMessage(ChatColor.GOLD + "/broker sell cancel" + ChatColor.GRAY + ": Cancel a sell order");
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
 
                     if (!sender.hasPermission("broker.commands.buy")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use the buy command!");
                         return false;
                     }
 
                     if (args.length >= 2) {
                         if (args.length == 2) {
                             
                             if (args[1].equalsIgnoreCase("cancel")) {
                                 if (!player.hasPermission("broker.commands.buy.cancel")) {
                                     player.sendMessage(ChatColor.RED + "You do not have permission to cancel orders!");
                                     return true;
                                 }
                                 player.sendMessage(ChatColor.GOLD + "Cancelling Buy Orders");
                                 player.openInventory(getBrokerInv("0", player, player.getName(), true));
                                 return true;
                             }
                             
                             if (args[1].equalsIgnoreCase("admincancel")) {
                                 if (!player.hasPermission("broker.commands.buy.admincancel")) {
                                     player.sendMessage(ChatColor.RED + "You do not have permission to cancel orders as an admin!");
                                     return true;
                                 }
                                 player.sendMessage(ChatColor.GOLD + "Cancelling Buy Orders");
                                 player.openInventory(getBrokerInv("0", player, "ADMIN", true));
                                 return true;
                             }
                             
                             // Buy From Player
 
                             OfflinePlayer seller = getServer().getOfflinePlayer(args[1]);
                             if (!seller.hasPlayedBefore()) {
                                 player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + args[1] + ChatColor.RED + " not found!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy {Player Name}");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
                             player.openInventory(getBrokerInv("0", player, seller.getName(), false));
 
                         } else {
 
                             // Place Buy Order
                             
                             if (!player.hasPermission("broker.commands.buy.orders")) {
                                 player.sendMessage(ChatColor.RED + "You do not have permission to place Buy Orders!");
                                 return false;
                             }
 
                             if (args.length != 4) {
                                 player.sendMessage(ChatColor.RED + "Buy Order format incorrect!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
 
                             ItemStack item = checkMaterial(args[1]);
                             if (item == null) {
                                 player.sendMessage(ChatColor.RED + "Item Name or ID not recognised!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
                             
                             int quant;
                             try {
                                 quant = Integer.parseInt(args[2]);                                
                             } catch (NumberFormatException ex) {
                                 player.sendMessage(ChatColor.RED + "Quantity must be a number!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
                             
                             if (quant <= 0) {
                                 player.sendMessage(ChatColor.RED + "Quantity must be greater than 0!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
                             
                             double price;
                             try {
                                 price = Double.parseDouble(args[3]);
                             } catch (NumberFormatException ex) {
                                 player.sendMessage(ChatColor.RED + "Max Price Each must be a number!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
                             
                             if (price <= 0) {
                                 player.sendMessage(ChatColor.RED + "Max Price Each must be greater than 0!");
                                 player.sendMessage(ChatColor.GRAY + "/broker buy [Item Name|ID{:data}] [Quantity] [Max Price Each]");
                                 return false;
                             }
 
                             // Format correct, Check Balance
                             
                             double fee = 0;
                             double totPrice = price * quant;
                             
                             if (taxOnBuyOrders && totPrice >= taxMinimum) {
                                 if (taxIsPercentage) {
                                     fee = totPrice / 100 * taxRate;
                                 } else {
                                     fee = taxRate;
                                 }
                             }
                             
                             if (vault.economy.getBalance(player.getName()) < totPrice + fee) {
                                 player.sendMessage(ChatColor.RED + "You cannot afford to place that Buy Order!");
                                 player.sendMessage(ChatColor.GRAY + "Item Cost: " + vault.economy.format(totPrice));
                                 if (fee != 0) {
                                     player.sendMessage(ChatColor.GRAY + "Buy Order Fee: " + vault.economy.format(fee));
                                     player.sendMessage(ChatColor.GRAY + "Total Cost: " + vault.economy.format(totPrice + fee));
                                 }
                                 return true;
                             }
                             
                             // Add Order
 
                             String query = "INSERT INTO BrokerOrders (orderType, playerName, itemName, enchantments, damage, price, quant, timeCode, perItems, meta) VALUES (1, '" + player.getName() + "', '" + item.getType() + "', '', " + item.getDurability() + ", " + price + ", " + quant + ", " + new Date().getTime() + ", 1, '')";
                             brokerDb.query(query);
                             player.sendMessage(ChatColor.GOLD  + "Buy Order for "+item.getType()+" placed!");
                             vault.economy.withdrawPlayer(player.getName(), totPrice + fee);
                             if (fee != 0) {
                                 player.sendMessage(ChatColor.GOLD  + "Funds Witheld: "+vault.economy.format(totPrice + fee) + " (including fee of "+vault.economy.format(fee)+")");
                             } else {
                                 player.sendMessage(ChatColor.GOLD  + "Funds Witheld: "+vault.economy.format(totPrice));
                             }
                             
                             matchBuyOrders();
                             
                             return true;
 
                         }
                     } else {
                         // Buy from general store
                         player.openInventory(getBrokerInv("0", player, null, false));
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
                         if (!player.hasPermission("broker.commands.sell.buyorders")) {
                             sender.sendMessage(ChatColor.RED + "You do not have permission to open Buy Orders on command!");
                             return false;
                         }
                         player.sendMessage(ChatColor.GOLD + "<BROKER> Buy Orders");
                         player.sendMessage(ChatColor.GOLD + "Choose an Item Type");
                         player.openInventory(getBrokerInv("0", player, null, true));
                         return true;
                     } else if (args.length >= 2) {
                         if (args[1] != null && args[1].equalsIgnoreCase("cancel")) {
                             if (!player.hasPermission("broker.commands.sell.cancel")) {
                                 player.sendMessage(ChatColor.RED + "You do not have permission to cancel orders!");
                                 return true;
                             }
                             player.sendMessage(ChatColor.GOLD + "Cancelling Sell Orders");
                             player.openInventory(getBrokerInv("0", player, player.getName(), false));
                             return true;
                         }
                         if (args[1] != null && args[1].equalsIgnoreCase("admincancel")) {
                             if (!player.hasPermission("broker.commands.sell.admincancel")) {
                                 player.sendMessage(ChatColor.RED + "You do not have permission to cancel orders as an admin!");
                                 return true;
                             }
                             player.sendMessage(ChatColor.GOLD + "Cancelling Sell Orders");
                             player.openInventory(getBrokerInv("0", player, "ADMIN", false));
                             return true;
                         }
                         if ((player.hasPermission("broker.vip") && vipMaxOrders != 0 && sellOrderCount(player.getName()) >= vipMaxOrders) || (!player.hasPermission("broker.vip") && maxOrders != 0 && sellOrderCount(player.getName()) >= maxOrders)) {
                             sender.sendMessage(ChatColor.RED + "You may only place a maximum of " + ChatColor.WHITE + vipMaxOrders + ChatColor.RED + " sale orders!");
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
                         if (perItems < 1) {
                             perItems = 1;
                         }
 
                         // List item for sale
                         int quant = (int) (itemInHand.getAmount() / perItems) * perItems;
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
                                 enchantmentString += ((Enchantment) ench).getId() + "@" + enchantments.get((Enchantment) ench);
                             }
                         }
 
                         String meta = "";
                         if (itemInHand.hasItemMeta()) {
                             ItemMeta itemMeta = itemInHand.getItemMeta();
                             meta = ItemSerialization.saveMeta(itemMeta).replace("'", "\\'");
                         }
 
                         if (isDamageableItem(itemInHand) && damage != 0) {
                             itemDisplayName += "(Used)";
                         }
 
                         String query = "INSERT INTO BrokerOrders (orderType, playerName, itemName, enchantments, damage, price, quant, timeCode, perItems, meta) VALUES (0, '" + player.getName() + "', '" + itemName + "', '" + enchantmentString + "', " + damage + ", " + price + ", " + quant + ", " + new Date().getTime() + ", " + perItems + ", '" + meta + "')";
                         brokerDb.query(query);
 
                         if (itemInHand.getAmount() > quant) {
                             player.getItemInHand().setAmount(player.getItemInHand().getAmount() - quant);
                         } else {
                             player.setItemInHand(null);
                         }
                         player.sendMessage(quant + " x " + itemDisplayName);
                         player.sendMessage(ChatColor.GOLD + " listed for sale for " + ChatColor.GREEN + vault.economy.format(price) + ChatColor.GOLD + " " + each + "!");
                         
                         matchBuyOrders();
                         
                     }
                     return true;
                 } else {
                     sender.sendMessage(ChatColor.RED + "Um... sorry... I don't recognise " + ChatColor.WHITE + args[0] + ChatColor.RED + "!");
                     return true;
                 }
             }
         }
         sender.sendMessage(ChatColor.RED + "Command not recognised! Type " + ChatColor.GOLD + "/broker" + ChatColor.RED + " for help");
         return false;
     }
 
     protected Inventory getBrokerInv(String page, Player buyer, String seller, boolean buyOrder) {
         Inventory inv;
         int buyOrders = 0;
         String priceOrder = "ASC";
         
         if (seller == null) {
             seller = "";
         }
         
         if (buyOrder) {
             buyOrders = 1;
             priceOrder = "DESC";
             if (seller.equals(buyer.getName())) {
                 inv = Bukkit.createInventory(buyer, 54, "<Broker> Buy Cancel");
             } else if (seller.equals("ADMIN")) {
                 inv = Bukkit.createInventory(buyer, 54, "<Broker> Buy AdminCancel");
             } else {
                 inv = Bukkit.createInventory(buyer, 54, "<Broker> Buy Orders");
             }
         } else if (seller.equals("")) {
             inv = Bukkit.createInventory(buyer, 54, "<Broker> Buy");
         } else if (seller.equalsIgnoreCase(buyer.getName())) {
             inv = Bukkit.createInventory(buyer, 54, "<Broker> Sell Cancel");
         } else if (seller.equals("ADMIN")) {
             inv = Bukkit.createInventory(buyer, 54, "<Broker> Sell AdminCancel");
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
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = "+buyOrders, "price, perItems, damage, enchantments, meta", "price/perItems "+priceOrder+", damage ASC");
                 } else {
                     if (seller.equals("ADMIN")) {
                         sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = "+buyOrders, "price, perItems, damage, enchantments,meta", "price/perItems "+priceOrder+", damage ASC");
                     } else {
                         sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "playerName = '" + seller + "' AND itemName = '" + subSplit[0] + "' AND orderType = "+buyOrders, "price, perItems, damage, enchantments,meta", "price/perItems "+priceOrder+", damage ASC");
                     }
                 }
             } else {
                 if (seller == null || seller.equals("")) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = "+buyOrders+" AND damage = " + subSplit[1], "price, perItems, damage, enchantments,meta", "price/perItems "+priceOrder+", damage ASC");
                 } else {
                     if (seller.equals("ADMIN")) {
                         sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = "+buyOrders+" AND damage = " + subSplit[1], "price, perItems, damage, enchantments,meta", "price/perItems "+priceOrder+", damage ASC");
                     } else {
                         sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price, meta", "BrokerOrders", "playerName = '" + seller + "' AND itemName = '" + subSplit[0] + "' AND orderType = "+buyOrders+" AND damage = " + subSplit[1], "price, perItems, damage, enchantments,meta", "price/perItems "+priceOrder+", damage ASC");
                     }
                 }
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                 int added = 0;
                 for (int i = 0; i < rows; i++) {
                     // Items may vary (Show top 45 prices)
                     if (added < 45) {
                         ItemStack stack = new ItemStack(Material.getMaterial((String) sellOrders.get(i).get("itemName")));
                         stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                         if ((String) sellOrders.get(i).get("enchantments") != null && !"".equals((String) sellOrders.get(i).get("enchantments"))) {
                             String[] enchSplit = ((String) sellOrders.get(i).get("enchantments")).split(";");
                             for (String ench : enchSplit) {
                                 String[] enchantment = ench.split("@");
                                 stack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(enchantment[0])), Integer.parseInt(enchantment[1]));
                             }
                         }
                         stack.setAmount((Integer) sellOrders.get(i).get("totquant"));
                         String meta = (String) sellOrders.get(i).get("meta");
                         if (!meta.equals("")) {
                             stack.setItemMeta(ItemSerialization.loadMeta(meta));
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
                             if (j < 8) {
                                 inv.setItem(j + 45, new ItemStack(Material.PAPER));
                             }
                         }
                     }
                 }
             }
         } else {
             // Load Main Page
             HashMap<Integer, HashMap<String, Object>> sellOrders;
             if (seller == null || seller.equals("")) {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "orderType = "+buyOrders, "itemName, damage", "itemName, damage ASC");
             } else {
                 if (seller.equals("ADMIN")) {
                     sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "orderType = "+buyOrders, "itemName, damage", "itemName, damage ASC");
                 } else {
                     sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "playerName = '" + seller + "' AND orderType = "+buyOrders, "itemName, damage", "itemName, damage ASC");
                 }
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                 int checkedrows = 0;
                 int added = 0;
                 String lastItem = "";
                 int skipped = 0;
                 for (int i = 0; i < rows; i++) {
                     if (added < 45) {
                         ItemStack stack = new ItemStack(Material.getMaterial((String) sellOrders.get(i).get("itemName")));
                         if (!isDamageableItem(stack)) {
                             stack.setDurability(Short.parseShort(sellOrders.get(i).get("damage").toString()));
                         } else {
                             if (((String) sellOrders.get(i).get("itemName")).equals(lastItem)) {
                                 skipped++;
                             }
                         }
                         if (!inv.contains(stack) && checkedrows >= pageNo * (45 + skipped)) {
                             inv.addItem(stack);
                             added++;
                         }
                         lastItem = (String) sellOrders.get(i).get("itemName");
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
                         if (i < 9) {
                             inv.setItem(i + 45, new ItemStack(Material.PAPER));
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
 
     private void convertMeta() {
         HashMap<Integer, HashMap<String, Object>> results = brokerDb.select("*", "BrokerOrders", null, null, null);
         for (int i = 0; i < results.size(); i++) {
             HashMap<String, Object> result = results.get(i);
 
             String id = result.get("id").toString();
 
             ItemStack stack = new ItemStack(Material.getMaterial((String) result.get("itemName")));
             stack.setDurability(Short.parseShort(result.get("damage").toString()));
             if ((String) result.get("enchantments") != null && !"".equals((String) result.get("enchantments"))) {
                 String[] enchSplit = ((String) result.get("enchantments")).split(";");
                 for (String ench : enchSplit) {
                     String[] enchantment = ench.split("@");
                     stack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(enchantment[0])), Integer.parseInt(enchantment[1]));
                 }
             }
             String meta = (String) result.get("meta");
             if (!meta.equals("")) {
                 String[] metaSplit = meta.split(":META:");
                 if (metaSplit[0].equals("BOOK")) {
                     BookMeta book = (BookMeta) stack.getItemMeta();
                     book.setTitle(metaSplit[1]);
                     book.setAuthor(metaSplit[2]);
                     String[] pages = metaSplit[3].split(":PAGE:");
                     for (int p = 0; p < pages.length; p++) {
                         book.addPage(pages[p]);
                     }
                     stack.setItemMeta(book);
                 } else if (metaSplit[0].equals("ARMOR")) {
                     LeatherArmorMeta armor = (LeatherArmorMeta) stack.getItemMeta();
                     armor.setColor(Color.fromRGB(Integer.parseInt(metaSplit[2]), Integer.parseInt(metaSplit[3]), Integer.parseInt(metaSplit[4])));
                     if (!metaSplit[1].equals("")) {
                         armor.setDisplayName(metaSplit[1]);
                     }
                     stack.setItemMeta(armor);
                 } else if (metaSplit[0].equals("MAP")) {
                     MapMeta map = (MapMeta) stack.getItemMeta();
                     if (!metaSplit[1].equals("")) {
                         map.setDisplayName(metaSplit[1]);
                     }
                     map.setScaling(Boolean.parseBoolean(metaSplit[2]));
                     stack.setItemMeta(map);
                 } else if (metaSplit[0].equals("EBOOK")) {
                     EnchantmentStorageMeta ench = (EnchantmentStorageMeta) stack.getItemMeta();
                     if (metaSplit.length != 1) {
                         for (String e : metaSplit[1].split(":ENCH:")) {
                             String[] enchantment = e.split(":");
                             ench.addStoredEnchant(Enchantment.getById(Integer.parseInt(enchantment[0])), Integer.parseInt(enchantment[1]), false);
                         }
                     }
                     stack.setItemMeta(ench);
                 } else if (metaSplit[0].equals("ITEM")) {
                     stack.getItemMeta().setDisplayName(metaSplit[1]);
                 }
             }
             String itemMeta = "";
             if (stack.hasItemMeta()) {
                 itemMeta = ItemSerialization.saveMeta(stack.getItemMeta()).replace("'", "\\'");
             }
             brokerDb.query("UPDATE BrokerOrders SET meta = '" + itemMeta + "' WHERE id = " + id);
         }
         getLogger().info("Database converted to Broker 1.6.0 format!");
     }
 
     private void loadAliases() {
         File aliasFile = new File(getDataFolder(), "itemNames.yml");
         FileConfiguration aliasConfig = YamlConfiguration.loadConfiguration(aliasFile);
 
         for (String key : aliasConfig.getKeys(false)) {
             int id = aliasConfig.getInt(key + ".id");
             short data = (short) aliasConfig.getInt(key + ".data");
             Object[] itemArray = new Object[2];
             itemArray[0] = id;
             itemArray[1] = data;
             itemAliases.put(key.toLowerCase(), itemArray);
 
             aliasConfig.set(key + ".id", id);
             aliasConfig.set(key + ".data", data);
 
             saveCustomConfig(aliasFile, aliasConfig);
 
         }
 
     }
 
     private void saveCustomConfig(File file, FileConfiguration config) {
         if (config == null || file == null) {
             return;
         }
         try {
             config.save(file);
         } catch (IOException ex) {
             getLogger().log(Level.SEVERE, "Could not save " + file, ex);
         }
     }
 
     private ItemStack checkMaterial(String itemName) {
 
         ItemStack item = null;
         short damage = 0;
         itemName = itemName.toLowerCase();
 
         if (itemName.contains(":")) {
 
             // Split out item damage modifier
 
             String[] split = itemName.split(":");
 
             try {
                 damage = Short.parseShort(split[1]);
             } catch (NumberFormatException ex) {
                 // Failed to match damage modifier
                 damage = 0;
             }
 
             itemName = split[0];
 
         }
 
         try {
 
             // Attempt match numerical Item ID
 
             int itemId = Integer.parseInt(itemName);
             Material mat = Material.getMaterial(itemId);
 
             if (mat != null) {
                 item = new ItemStack(mat);
             }
 
         } catch (NumberFormatException ex) {
 
             // Attempt match to item name
 
             Material mat = Material.matchMaterial(itemName);
 
             if (mat == null) {
                 
                 // Try removing any trailing s
                 if (itemName.endsWith("s")) {
                     // Remove trailing s
                     itemName = itemName.substring(0, itemName.length() - 1);
                     // Try again
                     mat = Material.matchMaterial(itemName);
                     if (mat == null) {
                         itemName += "s";
                     } else {
                         item = new ItemStack(mat);
                     }
                 }
                 
                 if (item == null) {
                     
                     // Attempt match to item alias
 
                     if (itemAliases.containsKey(itemName)) {
                         Object[] alias = itemAliases.get(itemName);
                         item = new ItemStack((Material) alias[0]);
                         if ((Short) alias[1] != 0) {
                             damage = (Short) alias[1];
                         }
                     } else if (itemName.endsWith("s")) {
                         // Remove trailing s
                         itemName = itemName.substring(0, itemName.length() - 1);
                         // Try again
                         if (itemAliases.containsKey(itemName)) {
                             Object[] alias = itemAliases.get(itemName);
                             item = new ItemStack((Material) alias[0]);
                             if ((Short) alias[1] != 0) {
                                 damage = (Short) alias[1];
                             }
                         }
                     }
                 }
 
             } else {
 
                 item = new ItemStack(mat);
 
             }
 
         }
 
         if (damage != 0) {
             item.setDurability(damage);
         }
 
         // Failed to match item name
         return item;
 
     }
     
     protected void matchBuyOrders() {
         
         // Fetch Buy Orders        
         HashMap<Integer, HashMap<String, Object>> orders = brokerDb.select("*", "BrokerOrders", "orderType = 1", null, "price DESC, timeCode ASC");
        for (int orderId : orders.keySet()) {
            HashMap<String, Object> order = orders.get(orderId);
             int id = Integer.parseInt(order.get("id").toString());
             String buyerName = (String)order.get("playerName");
             Material mat = Material.getMaterial(order.get("itemName").toString());
             short damage = Short.parseShort(order.get("damage").toString());
             double price = Double.parseDouble(order.get("price").toString());
             int quant = Integer.parseInt(order.get("quant").toString());
             
             int startQuant = quant;
             double budget = price * quant;
             double paid = 0;
             
             // Match Sell Orders
             HashMap<Integer, HashMap<String, Object>> sellOrders = brokerDb.select("id, playerName, quant, price, perItems, meta", "BrokerOrders", "orderType = 0 AND itemName = '"+mat+"' AND damage = " + damage + " AND price/perItems <= " + price + " AND enchantments = '' AND meta = ''", null, "price/perItems ASC, timeCode ASC");
             
            for (Integer sellOrderId : sellOrders.keySet()) {
                HashMap<String, Object> sellOrder = sellOrders.get(sellOrderId);
                 int sid = Integer.parseInt(sellOrder.get("id").toString());
                 String sellerName = (String)sellOrder.get("playerName");
                 int squant = Integer.parseInt(sellOrder.get("quant").toString());
                 double sprice = Double.parseDouble(sellOrder.get("price").toString());
                 int perItems = Integer.parseInt(sellOrder.get("perItems").toString());
                 double itemPrice = sprice/perItems;
                 
                 int thisSale = 0;
                 if (perItems <= quant) {
                     if (squant <= quant) {
                         quant -= squant;
                         thisSale += squant;
                     } else {
                         int items = 0;
                         while (items + perItems <= quant) {
                             items += perItems;
                         }
                         quant -= items;
                         thisSale += items;
                     }
                 }
                 
                 if (thisSale != 0) {
                     if (thisSale == squant) {
                         brokerDb.query("DELETE FROM BrokerOrders WHERE id = " + sid);
                     } else {
                         brokerDb.query("UPDATE BrokerOrders SET quant = " + (squant - thisSale) + " WHERE id = " + sid);
                     }
                     
                     double thisPrice = itemPrice * thisSale;
                     paid += thisPrice;
                     double fee = 0;
                     if (taxRate != 0) {
                         if (taxIsPercentage) {
                             fee = thisPrice / 100 * taxRate;
                         } else {
                             fee = taxRate;
                         }
                     }
                     
                     vault.economy.depositPlayer(sellerName, thisPrice - fee);
                     
                     OfflinePlayer seller = Bukkit.getOfflinePlayer(sellerName);
                     if (seller.isOnline()) {
                         seller.getPlayer().sendMessage(buyerName + ChatColor.GOLD + " bought " + ChatColor.WHITE + thisSale + " " + mat + ChatColor.GOLD + " for " + ChatColor.WHITE + vault.economy.format(thisPrice));
                         if (fee != 0) {
                             seller.getPlayer().sendMessage(ChatColor.GOLD + "You were charged a broker fee of " + ChatColor.WHITE + vault.economy.format(fee));
                         }
                     }
                 }
                 
             }
             
             int bought = startQuant - quant;
             OfflinePlayer buyer = Bukkit.getOfflinePlayer(buyerName);
             if (bought != 0) {
                 // Some items have sold
                 if (buyer.isOnline()) {
                     Player onlineBuyer = buyer.getPlayer();
                     onlineBuyer.sendMessage(ChatColor.GOLD + "You bought " + ChatColor.WHITE + bought + " " + mat + ChatColor.GOLD + "!");
                     ItemStack stack = new ItemStack(mat);
                     stack.setAmount(bought);
                     stack.setDurability(damage);
                     HashMap<Integer, ItemStack> dropped = onlineBuyer.getInventory().addItem(stack);
                     if (!dropped.isEmpty()) {
                         for (ItemStack dropStack : dropped.values()) {
                             onlineBuyer.getWorld().dropItem(onlineBuyer.getLocation(), dropStack);
                         }
                         onlineBuyer.sendMessage(ChatColor.RED + "Not all bought items fit in your inventory! Check the floor!");
                     }
                 } else {
                     // Add to pending sales
                     brokerDb.query("INSERT INTO BrokerPending (playerName, itemName, damage, quant) VALUES ('"+buyerName+"', '"+mat+"', "+damage+", "+bought+")");
                 }
             }
             
             if (quant == 0) {
                 // Refund any excess funds
                 double refund = budget - paid;
                 if (taxOnBuyOrders) {
                     if (taxIsPercentage) {
                         refund += refund / 100 * taxRate;
                     }
                 }
                 if (refund != 0) {
                     vault.economy.depositPlayer(buyerName, refund);
                     if (buyer.isOnline()) {
                         buyer.getPlayer().sendMessage(ChatColor.GOLD + "You were refunded " + ChatColor.WHITE + vault.economy.format(refund));
                     }
                 }
                 
                 // Close Buy Order
                 brokerDb.query("DELETE FROM BrokerOrders WHERE id = " + id);
             } else {
                 // Update Buy Order
                 brokerDb.query("UPDATE BrokerOrders SET quant = " + quant + " WHERE id = " + id);
                 
                 double refund = (price * bought) - paid;
                 if (taxOnBuyOrders) {
                     if (taxIsPercentage) {
                         refund += refund / 100 * taxRate;
                     }
                 }
                 if (refund != 0) {
                     vault.economy.depositPlayer(buyerName, refund);
                     if (buyer.isOnline()) {
                         buyer.getPlayer().sendMessage(ChatColor.GOLD + "You were refunded " + ChatColor.WHITE + vault.economy.format(refund));
                     }
                 }
             }
             
         }
         
     }
     
 }
