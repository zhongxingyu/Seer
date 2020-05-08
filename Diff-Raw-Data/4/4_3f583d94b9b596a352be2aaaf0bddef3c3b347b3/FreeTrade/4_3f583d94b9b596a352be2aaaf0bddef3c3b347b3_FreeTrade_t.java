 
 package com.exphc.FreeTrade;
 
 import java.util.logging.Logger;
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.SortedSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.command.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.*;
 
 import info.somethingodd.bukkit.OddItem.OddItem;
 
 enum Obtainability 
 { 
     NORMAL, SILKTOUCH, CREATIVE, HACKING, NEVER
 };
 
 class ItemQuery
 {
 
     ItemStack itemStack;
     static Logger log = Logger.getLogger("Minecraft");
 
     // Map between item names/aliases and id;dmg string
     static ConcurrentHashMap<String,String> name2CodeName;
     static ConcurrentHashMap<String,String> codeName2Name;
 
     static ConcurrentHashMap<String,Obtainability> obtainMap;
     static ConcurrentHashMap<Material,Boolean> isDurableMap;
 
     public ItemQuery(String s) {
         //Pattern onp = Pattern.compile( "^(\\d+)"
 
         Pattern p  = Pattern.compile(
             "^(\\d*)" +             // quantity
             "([# :;-]?)" +          // separator / stack flag
             "([^/\\\\]+)" +         // name
             "([/\\\\]?)" +          // separator / damage flag
             "([\\d%]*)" +           // use / damage
             "/?([^/]*)$");          // enchant
         Matcher m = p.matcher(s);
         int quantity;
 
         if (!m.find()) {
             throw new UsageException("Unrecognized item specification: " + s);
         }
 
         String quantityString = m.group(1);
         String isStackString = m.group(2);
         String nameString = m.group(3);
         String dmgOrUsesString = m.group(4);
         String usesString = m.group(5);
         String enchString = m.group(6);
 
         if (quantityString.equals("")) {
             quantity = 1;
         } else {
             quantity = Integer.parseInt(quantityString);
             if (quantity < 1) {
                 throw new UsageException("Invalid quantity: " + quantity);
             }
         }
 
 
         // Name
 
         // Allowed to be any case, with or without any word separators
         nameString = nameString.replaceAll("[ _-]", "").toLowerCase();
 
         if (nameString.contains("*")) {
             // Wildcard expressions
             SortedSet<String> results = wildcardLookupName(nameString);
             if (results.size() == 0) {
                 throw new UsageException("No items match pattern " + nameString);
             }
             if (results.size() > 1) {
                 StringBuffer nameMatches = new StringBuffer();
                 for (String resultName: results) {
                     nameMatches.append(resultName + ", ");
                 }
 
                 throw new UsageException("Found " + results.size() + " matching items: " + nameMatches);
             }
             // Exactly one hit, use it
             nameString = results.first().toLowerCase();
         } 
         // First try built-in name lookup
         itemStack = directLookupName(nameString);
 
         if (itemStack == null) {
             // If available, try OddItem for better names or clever suggestions
             if (Bukkit.getServer().getPluginManager().getPlugin("OddItem") != null) {
                 try {
                     itemStack = OddItem.getItemStack(nameString).clone();
                 } catch (IllegalArgumentException suggestion) {
                     throw new UsageException("No such item '" + nameString + "', did you mean '" + suggestion.getMessage() + "'?");
                 }
             } else {
                 // Worst case, lookup name from Bukkit itself
                 // Not very good because doesn't include damage value subtypes
                 Material material = Material.matchMaterial(nameString);
                 if (material == null) {
                     throw new UsageException("Unrecognized item name: " + nameString + " (no suggestions available)");
                 }
                 itemStack = new ItemStack(material);
             }
         }
 
         if (itemStack == null) {
             throw new UsageException("Unrecognized item name: " + nameString);
         }
 
 
         // Quantity, shorthand 10# = 10 stacks
         if (isStackString.equals("#")) {
             quantity *= Math.abs(itemStack.getType().getMaxStackSize());
         }
         itemStack.setAmount(quantity);
 
         // Damage value aka durability
         // User specifies how much they want left, 100% = unused tool
         short maxDamage = itemStack.getType().getMaxDurability();
         if (usesString != null && !usesString.equals("")) {
             short damage;
             short value;
 
             if (usesString.endsWith("%")) {
                 String percentageString = usesString.substring(0, usesString.length() - 1);
                 double percentage = Double.parseDouble(percentageString);
 
                 value = (short)(percentage / 100.0 * maxDamage);
             } else {
                 value = Short.parseShort(usesString);
             }
 
             // Normally, convenient for user to specify percent or times left (inverse of damage), /
             // Allow \ separator to specify damage itself
             if (dmgOrUsesString.equals("\\")) {
                 damage = value;
             } else {
                 damage = (short)(maxDamage - value);
             }
 
 
             if (damage > maxDamage) {
                 damage = maxDamage;     // Breaks right after one use
             }
 
             if (damage < 0) {
                 damage = 0;             // Completely unused
             }
 
             itemStack.setDurability(damage);
         } else {
             // If they didn't specify a durability, but they want a durable item, assume no damage (0)
             // TODO: only assume 0 for wants. For gives, need to use value from inventory! Underspecified
         }
 
         // TODO: enchantments
         if (enchString != null && !enchString.equals("")) {
             EnchantQuery enchs = new EnchantQuery(enchString);
 
             itemStack.addEnchantments(enchs.all);
         }
     }
 
     public ItemQuery(String s, Player p) {
         if (s.equals("this")) {
             itemStack = p.getItemInHand();
             if (itemStack == null) {
                 throw new UsageException("No item in hand");
             }
         } else {
             itemStack = (new ItemQuery(s)).itemStack;
         }
     }
 
 
     // Return whether an item degrades when used
     public static boolean isDurable(Material m) {
         return isDurableMap.containsKey(m);
     }
 
 
     public static String nameStack(ItemStack itemStack) {
         if (isNothing(itemStack)) {
             return "nothing";
         }
 
         String name, usesString, enchString;
         Material m = itemStack.getType();
        
         // If all else fails, use generic name from Bukkit
         name = itemStack.getType().toString();
 
         if (isDurable(m)) {
             // Percentage remaining
             
             // Round down so '100%' always means completely unused? (1 dmg = 99%)
             //int percentage = Math.round(Math.floor((m.getMaxDurability() - itemStack.getDurability()) * 100.0 / m.getMaxDurability()))
             // but then lower percentages are always one lower..
             // So just special-case 100% to avoid misleading
             int percentage;
             if (itemStack.getDurability() == 0) {
                 percentage = 100;
             } else {
                 percentage = (int)((m.getMaxDurability() - itemStack.getDurability()) * 100.0 / m.getMaxDurability());
                 if (percentage == 100) {
                     percentage = 99;
                 }
             }
 
             usesString = "/" + percentage + "%";
         } else {
             usesString = "";
         }
 
         // Find canonical name of item
         String codeName;
 
         codeName = itemStack.getTypeId() + "";
         name = codeName2Name.get(codeName);
         if (name == null) {
             // durability here actually is overloaded to mean a different item
             codeName = itemStack.getTypeId() + ";" + itemStack.getDurability();
             name = codeName2Name.get(codeName);
         }
 
         if (name == null) {
             name = "unknown="+codeName;
         }
         // Special case: 
         if (itemStack.getType() == Material.MAP) {
             name += "/" + itemStack.getDurability();
         }
 
         // Enchantments
         if (EnchantQuery.hasEnchantments(itemStack)) {
             Map<Enchantment,Integer> enchs = itemStack.getEnchantments();
             enchString = "/" + EnchantQuery.nameEnchs(enchs);
         } else {
             enchString = "";
         }
 
         return itemStack.getAmount() + ":" + name + usesString + enchString;
     }
 
     // Return whether two item stacks have the same item, taking into account 'subtypes'
     // stored in the damage value (ex: blue wool, only same as blue wool) - but will
     // ignore damage for durable items (ex: diamond sword, 50% = diamond sword, 100%)
     public static boolean isSameType(ItemStack a, ItemStack b) {
         if (a.getType() != b.getType()) {
             return false;
         }
 
         Material m = a.getType();
 
         if (isDurable(m)) {
             return true;
         }
     
         return a.getDurability() == b.getDurability();
     }
 
     // Return whether two item stacks are identical - except for amount!
     // Compares type, durability, enchantments
     public static boolean isIdenticalItem(ItemStack a, ItemStack b) {
         if (a == null || b == null) {
             return false;
         }
         if (a == null && b == null) {
             return true;
         }
 
         if (a.getType() != b.getType()) {
             return false;
         }
 
         // Same subtype -or- durability for tools
         if (a.getDurability() != b.getDurability()) {
             return false;
         }
 
         // Same enchantments
         // Compare by name to avoid complex duplicate enchantment traversing code
         String enchNameA = EnchantQuery.nameEnchs(a.getEnchantments());
         String enchNameB = EnchantQuery.nameEnchs(b.getEnchantments());
 
         if (!enchNameA.equals(enchNameB)) {
             return false;
         }
 
         return true;
     }
 
     // Identical and same amounts too
     public static boolean isIdenticalStack(ItemStack a, ItemStack b) {
         return isIdenticalItem(a, b) && a.getAmount() == b.getAmount();
     }
 
 
     // Configuration
 
     public static void loadConfig(YamlConfiguration config) {
         Map<String,Object> configValues = config.getValues(true);
         MemorySection itemsSection = (MemorySection)configValues.get("items");
         int i = 0;
     
         name2CodeName = new ConcurrentHashMap<String, String>();
         codeName2Name = new ConcurrentHashMap<String, String>();
         
         isDurableMap = new ConcurrentHashMap<Material, Boolean>();
         obtainMap = new ConcurrentHashMap<String, Obtainability>();
 
         HashSet<Obtainability> tradeableCategories = new HashSet<Obtainability>();
 
         for (String obtainString: config.getStringList("tradeableCategories")) {
             tradeableCategories.add(Obtainability.valueOf(obtainString.toUpperCase()));
         }
 
         for (String codeName: itemsSection.getKeys(false)) {
             String properName = config.getString("items." + codeName + ".name");
 
             // How this item can be obtained
             String obtainString = config.getString("items." + codeName + ".obtain");
             Obtainability obtain = (obtainString == null) ? Obtainability. NORMAL : Obtainability.valueOf(obtainString.toUpperCase());
             if (!tradeableCategories.contains(obtain)) {
                 log.info("Excluding untradeable " + properName);
                 continue;
                 // XXX: TODO: This doesn't work, since it falls back to Bukkit/OddItem for item names! (which it then can't reverse)
                 // Need to come up with another way to exclude it
             }
 
             obtainMap.put(codeName, obtain);
 
             // Add aliases from config
             List<String> aliases = config.getStringList("items." + codeName + ".aliases");
             if (aliases != null) {
                 for (String alias: aliases) {
                     name2CodeName.put(alias, codeName);
                     i += 1;
                 } 
             }
 
             // Generate 'proper name' alias, preprocessed for lookup
             String smushedProperName = properName.replaceAll(" ","");
             String aliasProperName = smushedProperName.toLowerCase();
             name2CodeName.put(aliasProperName, codeName);
             i += 1;
             codeName2Name.put(codeName, smushedProperName);
 
             // Generate numeric alias
             name2CodeName.put(codeName, codeName);
             i += 1;
 
 
             // Whether loses durability when used or not (include in trades)
             String purpose = config.getString("items." + codeName + ".purpose");
             if (purpose != null && (purpose.equals("armor") || purpose.equals("tool") || purpose.equals("weapon"))) {
                 Material material = codeName2ItemStack(codeName).getType();
                 isDurableMap.put(material, new Boolean(true));
             }
 
 
         }
         log.info("Loaded " + i + " item aliases");
 
     }
 
     // Parse a material code string with optional damage value (ex: 35;11)
     private static ItemStack codeName2ItemStack(String codeName) {
         Pattern p = Pattern.compile("^(\\d+)[;:/]?(\\d*)$");
         Matcher m = p.matcher(codeName);
         int typeCode;
         short dmgCode;
 
         if (!m.find()) {
             // This is an error in the config file (TODO: preparse or detect earlier)
             throw new UsageException("Invalid item code format: " + codeName);
         }
 
         typeCode = Integer.parseInt(m.group(1));
         if (m.group(2) != null && !m.group(2).equals("")) {
             dmgCode = Short.parseShort(m.group(2));
         } else {
             dmgCode = 0;
         }
             
         return new ItemStack(typeCode, 1, dmgCode);
     }
 
     // Get an ItemStack directly from one of its names or aliases, or null
     private static ItemStack directLookupName(String nameString) {
         String materialCode = name2CodeName.get(nameString);
 
         if (materialCode == null) {
             return null;
         }
 
         return codeName2ItemStack(materialCode);
     }
 
     // Get proper names of all aliases matching a wildcard pattern
     private static SortedSet<String> wildcardLookupName(String pattern) {
         SortedSet<String> results = new TreeSet<String>();
 
         Iterator it = name2CodeName.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             String name = (String)pair.getKey();
             String codeName = (String)pair.getValue();
 
             if (matchesWildcard(pattern, name)) {
                 results.add(codeName2Name.get(codeName).replace(" ",""));
             }
         }
 
         return results;
     }
 
     // Return whether a wildcard pattern (with asterisks = anything) matches
     private static boolean matchesWildcard(String needle, String haystack) {
         String[] cards = needle.split("\\*");
 
         for (String card : cards) {
             int i = haystack.indexOf(card);
             if (i == -1) {
                 return false;
             }
 
             haystack = haystack.substring(i + card.length());
         }
 
         return true;
     }
 
     public static boolean isNothing(ItemStack itemStack) {
         return itemStack == null || itemStack.getType() == Material.AIR;
     }
 }
 
 class EnchantQuery
 {
     static Logger log = Logger.getLogger("Minecraft");
 
     Map<Enchantment,Integer> all;
 
     static ConcurrentHashMap<String, Enchantment> name2Code;
     static ConcurrentHashMap<Enchantment, String> code2Name;
 
     public EnchantQuery(String allString) {
         all = new HashMap<Enchantment,Integer>();
 
         String[] enchStrings = allString.split("[, /-]+");
         for (String enchString: enchStrings) {
             Pattern p = Pattern.compile("^([A-Za-z-]*[a-z])([IV0-9]*)$");
             Matcher m = p.matcher(enchString);
 
             if (!m.find()) {
                 throw new UsageException("Unrecognizable enchantment: '" + enchString + "'");
             }
 
             String baseName = m.group(1);
             String levelString = m.group(2);
 
             Enchantment ench = enchFromBaseName(baseName);
             int level = levelFromString(levelString);
 
             // Odd, what's the point of having a separate 'wrapper' class?
             // Either way, it has useful methods for us
             //EnchantmentWrapper enchWrapper = new EnchantmentWrapper(ench.getId());
             EnchantmentWrapper enchWrapper = wrapEnch(ench);
 
             if (level > enchWrapper.getMaxLevel()) {
                 level = ench.getMaxLevel();
             }
 
             log.info("Enchantment: " + ench + ", level="+level);
 
             all.put(enchWrapper, new Integer(level));
         }
     }
 
     // Return whether all the enchantments can apply to an item
     public boolean canEnchantItem(ItemStack item) {
         Iterator it = all.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             EnchantmentWrapper ench = wrapEnch(pair.getKey());
             Integer level = (Integer)pair.getValue();
 
             if (!ench.canEnchantItem(item)) {
                 log.info("Cannot apply enchantment " + ench + " to " + item);
                 return false;
             }
         }
         return true;
     }
 
     public static boolean hasEnchantments(ItemStack item) {
         Map<Enchantment,Integer> enchs = item.getEnchantments();
 
         return enchs.size() != 0;
     }
 
     public String toString() {
         return nameEnchs(all);
     }
 
     public static String nameEnchs(Map<Enchantment,Integer> all) {
         StringBuffer names = new StringBuffer();
         Iterator it = all.entrySet().iterator();
 
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             Object obj = pair.getKey();
 
             EnchantmentWrapper ench = wrapEnch(pair.getKey());
             Integer level = (Integer)pair.getValue();
 
             names.append(nameEnch(ench));
             names.append(levelToString(level));
             names.append(","); 
         }
 
         // Remove the trailing comma
         // Would have liked to just build an array then join it, but not easier in Java either 
         if (names.length() > 1) { 
             names.deleteCharAt(names.length() - 1);
         }
 
         return names.toString();
     }
 
     // Get an EnchantmentWrapper from either an EnchantmentWrapper or Enchantment
     // Not sure why Bukkit chose to have two classes, but EnchantmentWrapper is more functional
     public static EnchantmentWrapper wrapEnch(Object obj) {
         if (obj instanceof EnchantmentWrapper) {
             return (EnchantmentWrapper)obj;
         }
 
         Enchantment ench = (Enchantment)obj;
 
         return new EnchantmentWrapper(ench.getId());
     }
 
 
 
     static Enchantment enchFromBaseName(String name) {
         // Built-in config file database..
         name = name.toLowerCase();
         Enchantment ench = name2Code.get(name);
         if (ench != null) {
             return ench;
         }
       
         // Bukkit itself?
         ench = Enchantment.getByName(name);
         if (ench != null) {
             return ench;
         }
 
         throw new UsageException("Unrecognized enchantment: " + name);
     }
 
     static String nameEnch(EnchantmentWrapper ench) {
         String name = code2Name.get(ench);
         if (name != null) {
             return name;
         }
         return "Unknown(" + ench.getId() + ")";
         // There is ench.getName(), but the names don't match in-game
     }
 
     static int levelFromString(String s) {
         if (s.equals("") || s.equals("I")) {
             return 1;
         } else if (s.equals("II")) { 
             return 2;
         } else if (s.equals("III")) {
             return 3;
         } else if (s.equals("IV")) {
             return 4;
         } else if (s.equals("V")) {
             return 5;
         } else {
             return Integer.parseInt(s);
         }
     }
 
     static String levelToString(int n) {
         switch (n) {
         case 1: return "I";
         case 2: return "II";
         case 3: return "III";
         case 4: return "IV";
         case 5: return "V";
         default: return Integer.toString(n);
         }
     }
 
     // Return wheather itemA has >= enchantments than itemB
     public static boolean equalOrBetter(ItemStack itemA, ItemStack itemB) {
         Map<Enchantment,Integer> enchsB = itemB.getEnchantments();
         Iterator it = enchsB.entrySet().iterator();
 
         while (it.hasNext()) {
             Map.Entry pair = (Map.Entry)it.next();
 
             EnchantmentWrapper enchB = (EnchantmentWrapper)pair.getKey();
             int levelB = ((Integer)pair.getValue()).intValue();
 
             if (!itemA.containsEnchantment(Enchantment.getById(enchB.getId()))) {
                 log.info("Missing enchantment " + nameEnch(enchB) + " not on " + itemA + " (doesn't match " + itemB + ")");
                 return false;
             }
 
             int levelA = itemA.getEnchantmentLevel(Enchantment.getById(enchB.getId()));
             log.info("Level " + levelB + " vs " + levelA);
             if (levelA < levelB) {
                 log.info("Lower enchantment level " + levelA + " < " + levelB);
                 return false;
             }
        } 
        return true;
     }
 
     public static void loadConfig(YamlConfiguration config) {
         Map<String,Object> configValues = config.getValues(true);
         MemorySection enchantsSection = (MemorySection)configValues.get("enchants");
         int i = 0;
     
         name2Code = new ConcurrentHashMap<String, Enchantment>();
         code2Name = new ConcurrentHashMap<Enchantment, String>();
         
         for (String codeString: enchantsSection.getKeys(false)) {
             Enchantment ench = Enchantment.getById(Integer.parseInt(codeString));
             String properName = config.getString("enchants." + codeString + ".name");
 
             List<String> aliases = config.getStringList("enchants." + codeString + ".aliases");
 
             if (aliases != null) {
                 for (String alias: aliases) {
                     name2Code.put(alias, ench);
                     i += 1;
                 } 
             }
 
             // Generate 'proper name' alias, preprocessed for lookup
             String smushedProperName = properName.replaceAll(" ","");
             String aliasProperName = smushedProperName.toLowerCase();
             name2Code.put(aliasProperName, ench);
             i += 1;
             code2Name.put(ench, smushedProperName);
         }
         log.info("Loaded " + i + " enchantment aliases");
     }
 }
 
 class Order implements Comparable
 {
     Player player;
     ItemStack want, give;
     boolean exact;
     boolean free;
 
     public Order(Player p, String wantString, String giveString) {
         player = p;
 
         if (wantString.contains("!")) {
             exact = true;
             wantString = wantString.replace("!", "");
         }
         if (giveString.contains("!")) {
             exact = true;
             giveString = giveString.replace("!", "");
         }
 
         want = (new ItemQuery(wantString, p)).itemStack;
         give = (new ItemQuery(giveString, p)).itemStack;
 
         if (ItemQuery.isIdenticalItem(want, give)) {
             throw new UsageException("You can't trade items for themselves");
         }
     }
 
     public Order(Player p, ItemStack w, ItemStack g, boolean e) {
         player = p;
         want = w;
         give = g;
         exact = e;
     }
 
     public String toString() {
         // TODO: pregenerate in initialization as description, no need to relookup
         return player.getDisplayName() + " wants " + ItemQuery.nameStack(want) + " for " + ItemQuery.nameStack(give) + (exact ? " (exact)" : "");
     }
 
     // Required for ConcurrentSkipListSet - Comparable interface
     public int compareTo(Object obj) {
         if (!(obj instanceof Order)) {
             return -1;
         }
         Order rhs = (Order)obj;
 
         return toString().compareTo(rhs.toString());
 
         //return player.getName().compareTo(rhs.player.getName()) || ItemQuery.isIdenticalStack(want, rhs.want) || ItemQuery.isIdenticalStack(give, rhs.give);
     }
 
 }
 
 // Exception to be reported back to player as invalid usage
 class UsageException extends RuntimeException
 {
     String message;
 
     public UsageException(String msg) {
         message = msg;
     }
 
     public String toString() {
         return "UsageException: " + message;
     }
 
     public String getMessage() {
         return message;
     }
 }
 
 class Market
 {
     ConcurrentSkipListSet<Order> orders;
     static Logger log = Logger.getLogger("Minecraft");
 
     public Market() {
         // TODO: load from file, save to file
         //orders = new ArrayList<Order>();
         orders = new ConcurrentSkipListSet<Order>();
     }
 
     public boolean showOutstanding(CommandSender sender) {
         sender.sendMessage("Open orders:");
            
         int i = 0;
         for (Order order: orders) {
             i += 1;
             sender.sendMessage(i + ". " + order);
         }
 
         sender.sendMessage("To add or fulfill an order:");
 
         return false;
     }
 
     public void cancelOrder(Player player, String s) {
         if (s == null || s.equals("-")) {
             cancelOrders(player);
             return;
         }
 
         ItemStack wanted = (new ItemQuery(s, player)).itemStack;
         int i = 0;
 
         for (Order order: orders) {
             if (order.player.equals(player) && ItemQuery.isIdenticalItem(order.want, wanted)) {
                 cancelOrder(order);
                 i += 1;
             }
         }
         player.sendMessage("Canceled " + i + " orders");
     }
 
     // Cancel all orders for a player
     public void cancelOrders(Player player) {
         int i = 0;
         for (Order order: orders) {
             if (order.player.equals(player)) {
                 cancelOrder(order);
                 i += 1;
             }
         }
         player.sendMessage("Canceled all your " + i + " orders");
     }
 
     public void cancelOrder(Order order) {
         if (!orders.remove(order)) {
             for (Order o: orders) {
                 log.info("Compare " + o + " = " + o.compareTo(order));
             }
 
             throw new UsageException("Failed to find order to cancel: " + order);
         }
         Bukkit.getServer().broadcastMessage("Closed order " + order);
     }
 
     public void placeOrder(Order order) {
 
         if (!order.player.hasPermission("freetrade.trade")) {
             throw new UsageException("You are not allowed to trade");
         }
 
         if (ItemQuery.isNothing(order.give)) {
             if (!order.player.hasPermission("freetrade.conjure")) {
                 throw new UsageException("You must specify or select what you want to trade for");
             }
 
             recvItems(order.player, order.want);
             return;
         }
 
         if (!hasItems(order.player, order.give)) {
             throw new UsageException("You don't have " + ItemQuery.nameStack(order.give) + " to give");
         }
 
         if (matchOrder(order)) {
             // Executed
             return;
         }
 
         // Not fulfilled; add to outstanding to match with future order 
         // Broadcast to all players so they know someone wants something, then add
         Bukkit.getServer().broadcastMessage("Wanted: " + order);
         orders.add(order);
     }
 
     // Transfer items from one player to another
     public static void transferItems(Player fromPlayer, Player toPlayer, ItemStack items) {
         if (!hasItems(fromPlayer, items)) {
             throw new UsageException("Player " + fromPlayer.getDisplayName() + " doesn't have " + ItemQuery.nameStack(items));
         }
 
         int missing = takeItems(fromPlayer, items);
 
         if (missing > 0) {
             // Rollback order
             // TODO: verify
             items.setAmount(items.getAmount() - missing);
             recvItems(fromPlayer, items);
 
             // TODO: try to prevent this from happening, by watching inventory changes, player death, etc
             throw new UsageException("Player " + fromPlayer.getDisplayName() + " doesn't have enough " + ItemQuery.nameStack(items) + ", missing " + missing + ", reverted");
             // TODO: also, detect earlier and cancel order
         }
 
         recvItems(toPlayer, items);
 
         // How did the items transport themselves between the players? Magic, as indicated by smoke.
         toPlayer.playEffect(toPlayer.getLocation(), Effect.SMOKE, 0);
 
         Bukkit.getServer().broadcastMessage(toPlayer.getDisplayName() + " received " + 
             ItemQuery.nameStack(items) + " from " + fromPlayer.getDisplayName());
     }
 
     // Remove items from player's inventory, return # of items player had < amount (insufficient items)
     // Based on OddItem
     public static int takeItems(Player player, ItemStack goners) {
 
         player.saveData();
 
         ItemStack[] inventory = player.getInventory().getContents();
 
         int remaining = goners.getAmount();
         int i = 0;
 
         for (ItemStack slot: inventory) {
             if (ItemQuery.isIdenticalItem(slot, goners)) {
                 if (remaining > slot.getAmount()) {
                     remaining -= slot.getAmount();
                     slot.setAmount(0);
                 } else if (remaining > 0) {
                     slot.setAmount(slot.getAmount() - remaining);
                     remaining = 0;
                 } else {
                     slot.setAmount(0);
                 }
 
                 // If removed whole slot, need to explicitly clear it
                 // ItemStacks with amounts of 0 are interpreted as 1 (possible Bukkit bug?)
                 if (slot.getAmount() == 0) {
                     player.getInventory().clear(i);
                 }
             }
 
             i += 1;
 
             if (remaining == 0) {
                 break;
             }
         }
 
         return remaining;
     }
 
     // Return whether player has at least the items in the stack
     public static boolean hasItems(Player player, ItemStack items) {
         ItemStack[] inventory = player.getInventory().getContents();
 
         int remaining = items.getAmount();
 
         for (ItemStack slot: inventory) {
             if (ItemQuery.isIdenticalItem(slot, items)) {
                 remaining -= slot.getAmount();
             }
         }
         
         return remaining <= 0;
     }
 
     // Have a player receive items in their inventory
     public static void recvItems(Player player, ItemStack items) {
         int remaining = items.getAmount();
 
         // Get maximum size per stack, then add individually
         // Prevents non-stackable items (potions, signs, boats, etc.) and semi-stackable
         // (enderpearls, eggs, snowballs, etc.) from being stacked to 64
         int stackSize;
 
         if (player.hasPermission("freetrade.bigstacks")) {
             stackSize = remaining;
         } else {
             stackSize = Math.abs(items.getType().getMaxStackSize());
             // Surprisingly, this always returns -1, see http://forums.bukkit.org/threads/getmaxstacksize-always-return-1.1154/#post-13147
             //int stackSize = Math.abs(items.getMaxStackSize());
         }
 
         do
         {
             ItemStack oneStack = items.clone();
 
             if (remaining > stackSize) {
                 oneStack.setAmount(stackSize);
                 remaining -= stackSize;
             } else {
                 oneStack.setAmount(remaining);
                 remaining = 0;
             }
        
             HashMap<Integer,ItemStack> excess = player.getInventory().addItem(oneStack);
 
             // If player's inventory if full, drop excess items on the floor
             Iterator it = excess.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry pair = (Map.Entry)it.next();
 
                 int unknown = ((Integer)pair.getKey()).intValue(); // hmm? always 0
                 ItemStack excessItems = (ItemStack)pair.getValue();
 
                 player.getWorld().dropItemNaturally(player.getLocation(), excessItems);
             }
         } while (remaining > 0);
     }
 
 
     public boolean matchOrder(Order newOrder) {
         int i = 0;
         for (Order oldOrder: orders) {
             i += 1;
 
             //log.info("oldOrder: " + oldOrder);
             //log.info("newOrder: " + newOrder);
 
             // Are they giving what anyone else wants?
             if (!ItemQuery.isSameType(newOrder.give, oldOrder.want) ||
                 !ItemQuery.isSameType(newOrder.want, oldOrder.give)) {
                 log.info("Not matched, different types");
                 continue;
             }
 
             double newRatio = (double)newOrder.give.getAmount() / newOrder.want.getAmount();
             double oldRatio = (double)oldOrder.want.getAmount() / oldOrder.give.getAmount();
 
             // Offering a better or equal deal? (Quantity = relative value)
             log.info("ratio " + newRatio + " >= " + oldRatio);
             if (!(newRatio >= oldRatio)) { 
                 log.info("Not matched, worse relative value");
                 continue;
             }
 
             // Is item less damaged or equally damaged than wanted? (Durability)
             if (ItemQuery.isDurable(newOrder.give.getType())) {
                 if (newOrder.give.getDurability() > oldOrder.want.getDurability()) {
                     log.info("Not matched, worse damage new, " + newOrder.give.getDurability() + " < " + oldOrder.want.getDurability());
                     continue;
                 }
             }
             if (ItemQuery.isDurable(oldOrder.give.getType())) {
                 if (oldOrder.give.getDurability() > newOrder.want.getDurability()) {
                     log.info("Not matched, worse damage old, " + oldOrder.give.getDurability() + " < " + newOrder.want.getDurability());
                     continue;
                 }
             }
 
             // TODO: enchantment checks
             if (!EnchantQuery.equalOrBetter(newOrder.give, oldOrder.want)) {
                 log.info("Not matched, insufficient magic new " + EnchantQuery.nameEnchs(newOrder.give.getEnchantments()) + 
                     " < " + EnchantQuery.nameEnchs(oldOrder.want.getEnchantments()));
                 continue;
             }
             if (!EnchantQuery.equalOrBetter(oldOrder.give, newOrder.want)) {
                 log.info("Not matched, insufficient magic old " + EnchantQuery.nameEnchs(oldOrder.give.getEnchantments()) + 
                     " < " + EnchantQuery.nameEnchs(newOrder.want.getEnchantments()));
                 continue;
             }
             
         
             // TODO: Generalize to "betterness"
 
 
         
             // Determine how much of the order can be fulfilled
             int remainingWant = oldOrder.want.getAmount() - newOrder.give.getAmount();
             int remainingGive = oldOrder.give.getAmount() - newOrder.want.getAmount();
 
             log.info("remaining want="+remainingWant+", give="+remainingGive);
 
             // They get what they want!
 
             // Calculate amount that can be exchanged
             ItemStack exchWant = new ItemStack(oldOrder.want.getType(), Math.min(oldOrder.want.getAmount(), newOrder.give.getAmount()), newOrder.give.getDurability());
             ItemStack exchGive = new ItemStack(oldOrder.give.getType(), Math.min(oldOrder.give.getAmount(), newOrder.want.getAmount()), oldOrder.give.getDurability());
             exchWant.addEnchantments(newOrder.give.getEnchantments());
             exchGive.addEnchantments(oldOrder.give.getEnchantments());
 
             log.info("exchWant="+ItemQuery.nameStack(exchWant));
             log.info("exchGive="+ItemQuery.nameStack(exchGive));
 
             transferItems(newOrder.player, oldOrder.player, exchWant);
             transferItems(oldOrder.player, newOrder.player, exchGive);
 
             /*
             oldOrder.player.getInventory().addItem(exchWant);
             newOrder.player.getInventory().remove(exchWant);
             Bukkit.getServer().broadcastMessage(oldOrder.player.getDisplayName() + " received " + 
                 ItemQuery.nameStack(exchWant) + " from " + newOrder.player.getDisplayName());
 
             newOrder.player.getInventory().addItem(exchGive);
             oldOrder.player.getInventory().remove(exchGive);
             Bukkit.getServer().broadcastMessage(newOrder.player.getDisplayName() + " received " + 
                 ItemQuery.nameStack(exchGive) + " from " + oldOrder.player.getDisplayName());
             */
 
    
     
             // Remove oldOrder from orders, if complete, or add partial if incomplete
             if (remainingWant == 0) {
                 // This order is finished, old player got everything they wanted
                 // Note: remainingWant can be negative if they got more than they bargained for
                 // (other player offered a better deal than expected). Either way, done deal.
                 cancelOrder(oldOrder);
                 return true;
             } else if (remainingWant > 0) {
                 oldOrder.want.setAmount(remainingWant);
                 oldOrder.give.setAmount(remainingGive);
 
                 Bukkit.getServer().broadcastMessage("Updated order: " + oldOrder);
                 return true;
             } else if (remainingWant < 0) {
                 // TODO: test better
                 cancelOrder(oldOrder);
 
                 newOrder.want.setAmount(-remainingGive);
                 newOrder.give.setAmount(-remainingWant);
                 log.info("Adding new partial order");
                 return false;
             }
 
         }
         return false;
     }
 }
 
 public class FreeTrade extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
     Market market = new Market();
     YamlConfiguration config;
 
     public void onEnable() {
         loadConfig();
         log.info(getDescription().getName() + " enabled");
     }
 
     public void onDisable() {
         log.info(getDescription().getName() + " disabled");
     }
 
     public void loadConfig() {
         String filename = getDataFolder() + System.getProperty("file.separator") + "FreeTrade.yml";
         File file = new File(filename);
 
         if (!file.exists()) {
             if (!newConfig(file)) {
                 throw new UsageException("Could not create new configuration file");
             }
         }
 
         config = YamlConfiguration.loadConfiguration(new File(filename));
         if (config == null) {
             throw new UsageException("Failed to load configuration file " + filename);
         }
         if (config.getInt("version") < 1) {
             throw new UsageException("Configuration file version is outdated");
         }
 
 
 
         ItemQuery.loadConfig(config);
         EnchantQuery.loadConfig(config);
     }
 
     // Copy default configuration
     public boolean newConfig(File file) {
         FileWriter fileWriter;
         if (!file.getParentFile().exists()) {
             file.getParentFile().mkdir();
         }
 
         try {
             fileWriter = new FileWriter(file);
         } catch (IOException e) {
             log.severe("Couldn't write config file: " + e.getMessage());
             Bukkit.getServer().getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("FreeTrade"));
             return false;
         }
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("FreeTrade.yml"))));
         BufferedWriter writer = new BufferedWriter(fileWriter);
         try {
             String line = reader.readLine();
             while (line != null) {
                 writer.write(line + System.getProperty("line.separator"));
                 line = reader.readLine();
             }
             log.info("Wrote default config");
         } catch (IOException e) {
             log.severe("Error writing config: " + e.getMessage());
         } finally {
             try {
                 writer.close();
                 reader.close();
             } catch (IOException e) {
                 log.severe("Error saving config: " + e.getMessage());
                 Bukkit.getServer().getPluginManager().disablePlugin(Bukkit.getServer().getPluginManager().getPlugin("FreeTrade"));
             }
         }
         return true;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         Player player;
         int n = 0;
 
         if (!cmd.getName().equalsIgnoreCase("want")) {
             return false;
         }
 
         // /want
         if (args.length == 0) {
             return market.showOutstanding(sender);
         }
 
         if (sender instanceof Player) {
             player = (Player)sender;
         } else {
             // Get player name from first argument
             player = Bukkit.getServer().getPlayer(args[0]);
             if (player == null) {
                 sender.sendMessage("no such player");
                 return false;
             }
             n++;
         }
         if (args.length < 1+n) {
             return false;
         }
 
         String wantString, giveString;
         wantString = args[n];
 
         if (args.length < 2+n) {
             // Omitted last arg, use item in hand
             giveString = "this";
         } else {
             if (args[n+1].equalsIgnoreCase("for")) {
                 giveString = args[n+2];
             } else {
                 giveString = args[n+1];
             }
         }
 
         Order order;
 
         try {
             if (wantString.equals("-")) {
                log.info("cancelall");
                 market.cancelOrder(player, null);
             } else if (giveString.equals("-")) {
                market.cancelOrder(player, wantString);
             } else { 
                 order = new Order(player, wantString, giveString);
 
                 sender.sendMessage(order.toString());
                 market.placeOrder(order);
             }
         } catch (UsageException e) {
             log.info("Sending usage exception: " + player.getDisplayName() + " - " + e );
             player.sendMessage(e.getMessage());
             return false;
         } 
 
         return true;
     }
 }
 
 
