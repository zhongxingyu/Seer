 package me.ellbristow.broker;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Broker extends JavaPlugin {
     
     private static FileConfiguration config;
     private String[] tableColumns = {"id", "playerName", "orderType", "timeCode", "itemName", "enchantments", "damage", "price", "quant", "perItems"};
     private String[] tableDims = {"INTEGER PRIMARY KEY ASC AUTOINCREMENT", "TEXT NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL", "TEXT NOT NULL", "TEXT", "INTEGER NOT NULL DEFAULT 0", "DOUBLE NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL DEFAULT 1"};
     protected vaultBridge vault;
     protected BrokerDb brokerDb;
     protected HashMap<String,HashMap<Integer,String>> priceCheck = new HashMap<String, HashMap<Integer,String>>();
     protected HashMap<String,HashMap<ItemStack,String>> pending = new HashMap<String, HashMap<ItemStack,String>>();
     
     protected double taxRate;
     protected boolean taxIsPercentage;
     protected double taxMinimum;
     protected int maxOrders;
     protected int vipMaxOrders;
 
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
         }
         if (!brokerDb.tableContainsColumn("BrokerOrders", "perItems")) {
             brokerDb.addColumn("BrokerOrders", "perItems INTEGER NOT NULL DEFAULT 1");
         }
         getServer().getPluginManager().registerEvents(new BrokerListener(this), this);
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
                     sender.sendMessage(ChatColor.GOLD + "/broker buy"+ChatColor.GRAY + ": Open the broker window to buy items");
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
                     player.openInventory(getBrokerInv("0", player, false));
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
                         sender.sendMessage(ChatColor.RED + "Try:" + ChatColor.WHITE + "/broker sell [price]!");
                     } else if (args.length >= 2) {
                         if (args[1] != null && args[1].equalsIgnoreCase("cancel")) {
                             player.sendMessage(ChatColor.GOLD + "Cancelling Sell Orders");
                             player.openInventory(getBrokerInv("0", player, true));
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
                             sender.sendMessage(ChatColor.RED + "/broker sell [price]");
                             return false;
                         }
                         
                         int perItems = 1;
                         String each = "each";
                         if (args.length > 2) {
                             try {
                                 perItems = Integer.parseInt(args[2]);
                                 each = "per " + perItems + " items";
                             } catch (NumberFormatException nfe) {
                                 sender.sendMessage(ChatColor.RED + "Per # Items must be a whoel number!");
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
                         if (isDamageableItem(itemInHand) && damage != 0) {
                             itemDisplayName += "(Used)";
                         }
                         String query = "INSERT INTO BrokerOrders (orderType, playerName, itemName, enchantments, damage, price, quant, timeCode, perItems) VALUES (0, '" + player.getName() + "', '" + itemName + "', '" + enchantmentString + "', " + damage + ", " + price + ", " + quant + ", " + new Date().getTime() + ", "+perItems+")";
                         brokerDb.query(query);
                         if (player.getItemInHand().getAmount() > quant) {
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
     
     protected Inventory getBrokerInv(String page, Player player, boolean forPlayer) {
         Inventory inv;
         if (!forPlayer) {
             inv = Bukkit.createInventory(player, 54, "<Broker> Buy");
         } else {
             inv = Bukkit.createInventory(player, 54, "<Broker> Cancel");        }
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
                 if (!forPlayer) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price", "BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = 0", "price, perItems, damage, enchantments", "price/perItems ASC, damage ASC");
                 } else {
                    sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price", "BrokerOrders", "playerName = '" + player.getName() + "' AND itemName = '" + subSplit[0] + "' AND orderType = 0", "price, perItems, damage, enchantments", "price/perItems ASC, damage ASC");
                 }
             } else {
                 if (!forPlayer) {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price","BrokerOrders", "itemName = '" + subSplit[0] + "' AND orderType = 0 AND damage = "+subSplit[1], "price, perItems, damage, enchantments", "price/perItems ASC, damage ASC");
                 } else {
                     sellOrders = brokerDb.select("itemName, enchantments, damage, SUM(quant) as totquant, price","BrokerOrders", "playerName = '" + player.getName() + "' AND itemName = '" + subSplit[0] + "' AND orderType = 0 AND damage = "+subSplit[1], "price, damage, enchantments", "price/perItems ASC, damage ASC");
                 }
             }
             if (sellOrders != null) {
                 int rows = sellOrders.size();
                 int added = 0;
                 for (int i = 0; i < rows; i++) {
                     if (!isDamageableItem(new ItemStack(Material.getMaterial(sellOrders.get(i).get("itemName").toString())))) {
                         // All items the same (Show top 5 prices)
                         if (added < 5) {
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
                             int column = 0;
                             while (stack.getAmount() > 0 && column < 9) {
                                 inv.setItem((added*9)+column, stack);
                                 stack.setAmount(stack.getAmount()-64);
                                 column++;
                             }
                             added++;
                         }
                     } else {
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
                             inv.setItem(added, stack);
                             added++;
                         }
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
             if (!forPlayer) {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "orderType = 0", "itemName, damage", "itemName, damage ASC");
             } else {
                 sellOrders = brokerDb.select("itemName, damage", "BrokerOrders", "playerName = '" + player.getName() + "' AND orderType = 0", "itemName, damage", "itemName, damage ASC");
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
