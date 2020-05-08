 package com.github.yukinoraru.ToggleInventory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ToggleInventory extends JavaPlugin implements Listener {
 
     private static final String CONFIG_INVENTORY_CURRENT_INDEX          = "inv_current_index";
     private static final String CONFIG_INVENTORY_SPCIAL_INV_INDEX       = "special_inv_index";
     private static final int    CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT  = 1;
     private static final int    CONFIG_INVENTORY_MAX_INDEX_DEFAULT      = 4;
     private static final int    CONFIG_INVENTORY_MAXIMUM                = 30;
     private static final String CONFIG_FILENAME_SPECIAL_INV             = "special_inventories.yml";
 
     public void onEnable(){
 
         // save config files if not exist
         saveDefaultConfig();
         saveResource(CONFIG_FILENAME_SPECIAL_INV, false);
 
         // MCStats cf.http://mcstats.org/plugin/ToggleInventory
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
             // Failed to submit the stats :-(
         }
     }
 
     public void onDisable(){
     }
 
     private void outputError(String msg, CommandSender sender){
         sender.sendMessage(msg);
         getLogger().warning(msg);
     }
 
     // maximum one precedes
     private int getMaxInventoryIndex(CommandSender sender){
         int max = -1;
         for(int i=2; i <= CONFIG_INVENTORY_MAXIMUM; i++){
             String permissionPath = "toggle_inventory." + Integer.toString(i);
             if(sender.hasPermission(permissionPath)){
                 max = i;
             }
         }
         return (max <= 1) ? CONFIG_INVENTORY_MAX_INDEX_DEFAULT : max;
     }
 
     // create File instance for saving inventory
     // path = plugins/ToggleInventory/players/[player-name]/inv[n].yml
     private File getInventoryFile(String playerName, int num){
         String parentPath = getDataFolder() + File.separator + "players" + File.separator + playerName;
         String childPath  = "inv" + num + ".yml";
         return new File(parentPath, childPath);
     }
 
     // create File instance for player's config
     // path = plugins/ToggleInventory/players/[player-name]/config.yml
     private File getPlayerConfigFile(String playerName){
         String parentPath = getDataFolder() + File.separator + "players" + File.separator + playerName;
         String childPath  = "config.yml";
         return new File(parentPath, childPath);
     }
 
     //
     private int getCurrentInventoryIndex(String playerName){
         File configFile         = getPlayerConfigFile(playerName);
         FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
         int invCurrentIndex     = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);
         return invCurrentIndex;
     }
 
     // calculate and set next inventory index
     // inventory index must be loop like a ring: 1 -> 2 -> 3 -> 1 -> .....
     private int setNextInventoryIndex(CommandSender sender, int nextInvIndex) throws IndexOutOfBoundsException{
         String playerName = sender.getName();
 
         int maxIndex = getMaxInventoryIndex(sender);
         int invCurrentIndex = getCurrentInventoryIndex(playerName);
 
         if(nextInvIndex < 0){
             nextInvIndex = (invCurrentIndex+1 > maxIndex) ? 1 : invCurrentIndex+1;
         }
         else if(nextInvIndex > maxIndex){
             throw new IndexOutOfBoundsException("[ERROR] Max inventory index is " + ChatColor.RED+Integer.toString(maxIndex));
         }
 
         // save next index
         File configFile         = getPlayerConfigFile(playerName);
         FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
 
         try {
             pConf.set(CONFIG_INVENTORY_CURRENT_INDEX, nextInvIndex);
             pConf.save(configFile);
         } catch (IOException e) {
             outputError("Something went wrong when setting next inventory index.", sender);
             e.printStackTrace();
         }
 
         return nextInvIndex;
     }
 
 
     private FileConfiguration reloadSpecialInventories() {
 
         FileConfiguration specialInventoriesConfig = null;
         File specialInventoriesConfigFile = null;
 
         specialInventoriesConfigFile = new File(getDataFolder(), CONFIG_FILENAME_SPECIAL_INV);
         specialInventoriesConfig = YamlConfiguration.loadConfiguration(specialInventoriesConfigFile);
 
         // Look for defaults in the jar
         InputStream defConfigStream = this.getResource(CONFIG_FILENAME_SPECIAL_INV);
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             specialInventoriesConfig.setDefaults(defConfig);
         }
 
         return specialInventoriesConfig;
     }
 
     // serialize 'Enchantment' to 'String[]'
     private String []getEnchantmentsString(ItemStack item){
         Map<Enchantment,Integer> enchantments = item.getEnchantments();
         Iterator<Entry<Enchantment,Integer>> iter = enchantments.entrySet().iterator();
         ArrayList<String> listOfEnchantment      = new ArrayList<String>();
         while(iter.hasNext()){
             Entry<Enchantment,Integer> entry = iter.next();
             String enchantmentName = entry.getKey().getName();
             int    echantmentLevel = entry.getValue();
             listOfEnchantment.add(enchantmentName + "," + echantmentLevel);
         }
         if(listOfEnchantment.size() > 0){
             String [] arrayOfEnchantment = listOfEnchantment.toArray(new String[listOfEnchantment.size()]);
             return arrayOfEnchantment;
         }
         return null;
     }
 
     // save inventory to YAML
     private void saveInventory(CommandSender sender){
 
         Player player = (Player)sender;
         String playerName = player.getName();
         PlayerInventory inventory = player.getInventory();
 
         // load current inventory index
         int invCurrentIndex     = getCurrentInventoryIndex(playerName);
 
         // load saved currentInventory
         File inventoryFile      = getInventoryFile(playerName, invCurrentIndex);
 
         // before save, file must be filled with empty
         PrintWriter writer;
         try {
        	// file doesn't exist, create new file
            if(!inventoryFile.exists()){
            	inventoryFile.createNewFile();
            }
             writer = new PrintWriter(inventoryFile);
             writer.print("");
             writer.close();
        } catch (Exception e) {
             outputError("Something went wrong when saving inventory.", sender);
             e.printStackTrace();
         }
 
         FileConfiguration pInv  = YamlConfiguration.loadConfiguration(inventoryFile);
 
         // CAUTION: inventory is separated two div: armor slots, normal slots
         HashMap<String, ItemStack[]> invs = new HashMap<String, ItemStack[]>();
         invs.put("item", inventory.getContents());
         invs.put("armor", inventory.getArmorContents());
         Iterator<String> it = invs.keySet().iterator();
         while (it.hasNext()){
             int i = 0;
             String key = (String)it.next();
             for (ItemStack item : invs.get(key)) {
                 i++;
                 if(item == null){
                     continue;
                 }
 
                 // create prefix: e.g.) 'item1' or 'armor1'
                 String prefix = key + Integer.toString(i);
 
                 // get/set basic info for item
                 int itemID = item.getTypeId();
                 pInv.set(prefix + ".id", itemID);
                 pInv.set(prefix + ".amount", item.getAmount());
                 pInv.set(prefix + ".durability", item.getDurability());
 
                 // name and lore
                 if( itemID != 0 ){
                     try {
                         // get encode name and lores
                         String name = Namer.getNameInURLEncoded(item);
                         String []lores = Namer.getLoreInURLEncoded(item);
                         pInv.set(prefix + ".name", name);
                         pInv.set(prefix + ".lore", lores);
                     } catch (UnsupportedEncodingException e) {
                         e.printStackTrace();
                     }
                     // save colored leather armors
                     if(Armor.isApplicable(item)){
                         // convert int to hex color; e.g.) 16711680 -> 0xff0000
                         int color = Armor.getColor(item);
                         if(color > 0){
                             String hexColor = String.format("#%06X", (0xFFFFFF & color));
                             pInv.set(prefix + ".color", hexColor);
                         }
                     }
                 }
 
                 // enchantment
                 String [] arrayOfEnchantment = getEnchantmentsString(item);
                 if(arrayOfEnchantment != null){
                     pInv.set(prefix + ".enchantment", Arrays.asList(arrayOfEnchantment));
                 }
 
                 // skull
                 if(Skull.isApplicable(item)){
                     pInv.set(prefix + ".skullOwner", Skull.getSkin(item));
                 }
 
                 //written book
                 if(itemID == 387) {
                     try {
                         Book book = new Book(item);
                         pInv.set(prefix + ".book" + ".title", book.getTitle());
                         pInv.set(prefix + ".book" + ".author", book.getAuthor());
                         pInv.set(prefix + ".book" + ".pages", Arrays.asList(book.getPages()));
                     } catch (UnsupportedEncodingException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
 
         // save
         try{
             pInv.save(inventoryFile);
         }
         catch(Exception e){
             outputError("Something went wrong when saving inventory.", sender);
             e.printStackTrace();
         }
         return;
     }
 
     // desirialize inventory from ConfigurationSection, not ConfigFile.
     private void deserializeInventoryConfig(PlayerInventory inventory, ConfigurationSection pInv){
 
         inventory.clear();
 
         int armorIndex = 0;
         ItemStack[] armorContents = new ItemStack[4];
 
         Set<String> item_keys = pInv.getKeys(false);
         for (String key: item_keys) {
 
             int index        = (key.startsWith("item")) ? Integer.parseInt(key.substring("item".length())) - 1 : Integer.parseInt(key.substring("armor".length())) - 1 ;
             int itemID       = pInv.getInt(key + ".id");
             int amount       = pInv.getInt(key + ".amount");
             short durability = (short)pInv.getInt(key + ".durability");
 
             ItemStack item = new ItemStack(itemID);
             item.setDurability(durability);
 
             // restore name
             try {
                 String encoded_name   = pInv.getString(key + ".name");
                 if(encoded_name != null && encoded_name.length() > 0){
                     item = Namer.setNameInURLEncoded(item, encoded_name);
                 }
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             // restore lore
             try {
                 List<String> lore     = pInv.getStringList(key + ".lore");
                 String[] encoded_lores = lore.toArray(new String[lore.size()]);
                 if(encoded_lores.length > 0){
                     item = Namer.setLoreInURLEncoded(item, encoded_lores);
                 }
             } catch (UnsupportedEncodingException e1) {
                 e1.printStackTrace();
             }
 
             // restore colored leather armors
             if(Armor.isApplicable(item)){
                 String hexColor = pInv.getString(key + ".color", null);
                 if(hexColor != null){
                     int color = Integer.parseInt(hexColor.replaceFirst("#", ""), 16);
                     item = Armor.setColor(item, color);
                 }
             }
 
             // restore enchantments
             List<String> enchantments = pInv.getStringList(key + ".enchantment");
             for (String e : enchantments){
                 String[] tmp = e.split(",");
                 if(tmp.length != 2){
                     getLogger().warning("Something wrong with enchantments.");
                     continue;
                 }
                 Enchantment enchantment = Enchantment.getByName(tmp[0]);
                 item.addUnsafeEnchantment(enchantment, Integer.parseInt(tmp[1]));
             }
 
             // restore skull owner
             if(Skull.isApplicable(item)){
                 String owner = pInv.getString(key + ".skullOwner");
                 if(owner.length() > 0){
                     item = Skull.setSkin(item, owner);
                 }
             }
 
             // written book
             if(itemID == 387) {
                 //getLogger().info("Load from written book.");
                 String author  = pInv.getString(key + ".book.author");
                 String title   = pInv.getString(key + ".book.title");
                 List<String> pages     = pInv.getStringList(key + ".book.pages");
                 String[] pagesInString = pages.toArray(new String[pages.size()]);
                 try {
                     Book book = new Book(title, author, pagesInString, false);
                     item = book.generateItemStack();
                 } catch (UnsupportedEncodingException e1) {
                     e1.printStackTrace();
                 }
             }
 
             if(key.startsWith("item")){
                 item.setAmount(amount);
                 inventory.setItem(index, item);
             }
             else if(key.startsWith("armor")){
                 armorContents[armorIndex++] = item;
             }
         }
 
         inventory.setArmorContents(armorContents);
         return ;
     }
 
     // load inventory from specified index
     private void loadInventory(CommandSender sender, int inventoryIndex){
         Player player = (Player)sender;
         PlayerInventory inventory = player.getInventory();
 
         //
         File inventoryFile = getInventoryFile(player.getName(), inventoryIndex);
         FileConfiguration pInv  = YamlConfiguration.loadConfiguration(inventoryFile);
 
         deserializeInventoryConfig(inventory, pInv.getRoot());
 
         return ;
     }
 
     // this method generates string like '[1 2 3 4 ... n]'
     private String makeInventoryMessage(CommandSender sender){
         String playerName = sender.getName();
         String msg = ChatColor.GRAY+"[";
         File configFile         = getPlayerConfigFile(playerName);
         FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
 
         int invCurrentIndex = pConf.getInt(CONFIG_INVENTORY_CURRENT_INDEX, CONFIG_INVENTORY_CURRENT_INDEX_DEFAULT);
         int maxIndex = getMaxInventoryIndex(sender);
 
         for(int i=1; i <= maxIndex; i++){
             if(i == invCurrentIndex){
                 msg += ChatColor.WHITE+Integer.toString(i);
             }
             else{
                 msg += ChatColor.GRAY+Integer.toString(i);
             }
             msg += ChatColor.RESET+" ";
         }
         return msg + ChatColor.GRAY+"] ";
     }
 
     //
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 
         // make sure the sender is a Player before casting
         Player player = null;
         String playerName = null;
         if (sender instanceof Player) {
             player = (Player)sender;
             playerName = player.getName();
          } else {
             sender.sendMessage("You must be a player!");
             return false;
          }
 
         // check whether player is using special inventories
         File configFile         = getPlayerConfigFile(playerName);
         FileConfiguration pConf = YamlConfiguration.loadConfiguration(configFile);
         String currentSpecialInv = pConf.getString(CONFIG_INVENTORY_SPCIAL_INV_INDEX, "");
         boolean isSpecialInvEnabled = currentSpecialInv.length() > 0;
 
         // ------------------------------------------------
         // implement /togglei command.
         // ------------------------------------------------
         if(cmd.getName().equalsIgnoreCase("togglei")){
             if(!sender.hasPermission("toggle_inventory.toggle")){
                 sender.sendMessage("You don't have permission to toggle inventory.");
                 return true;
             }
             int index = -1;
 
             // toggle to same inventory is prohibit
             if(args.length >= 1 && args[0].length() > 0){
                 try{
                     index = Integer.parseInt(args[0]);
                 }
                 catch(Exception e){
                     sender.sendMessage("Inventory index is wrong.");
                     return true;
                 }
                 int invCurrentIndex = getCurrentInventoryIndex(playerName);
                 if(index == invCurrentIndex){
                     sender.sendMessage(makeInventoryMessage(sender) + "It's your current inventory index.");
                     return true;
                 }
             }
 
             // if a user using special inventory, just load last inventory.
             if(isSpecialInvEnabled){
                 try{
                     pConf.set(CONFIG_INVENTORY_SPCIAL_INV_INDEX, ""); // set empty
                     pConf.save(configFile);
                 }catch (Exception e){
                     outputError("Unknown error occured.", sender);
                     e.printStackTrace();
                     return true;
                 }
                 int inventoryIndex = getCurrentInventoryIndex(playerName);
                 loadInventory(sender, inventoryIndex);
                 sender.sendMessage(makeInventoryMessage(sender) + "inventory restored.");
                 return true;
             }
             // if a user is not using special inv, toggle inv.
             else{
 
                 saveInventory(sender);  // save current inventory
 
                 // calculate next inv
                 int nextInvIndex;
                 if(index > 0){
                     // set next inventory
                     try{
                         nextInvIndex = setNextInventoryIndex(sender, index);
                     }
                     catch(Exception e){
                         sender.sendMessage(e.getMessage()); // out of bounds exception
                         return true;
                     }
                 }
                 else{
                     // toggle inventory like a ring
                     nextInvIndex = setNextInventoryIndex(sender, -1);
                 }
 
                 // load from currentIndex
                 loadInventory(sender, nextInvIndex);
 
                 sender.sendMessage(makeInventoryMessage(sender) + "inventory toggled.");
             }
 
             return true;
         }
 
         // ------------------------------------------------
         // implement /toggleis command
         // ------------------------------------------------
         if(cmd.getName().equalsIgnoreCase("toggleis")){
             // check permission
             if(!sender.hasPermission("toggle_inventory.toggle_special")){
                 sender.sendMessage("You don't have permission to toggle special inventories.");
                 return true;
             }
 
             // always reload special inv from file.
             FileConfiguration specialInventoriesConfig = reloadSpecialInventories();
 
             // find target inventory
             String targetInv = null;
             Set<String> nameList = specialInventoriesConfig.getKeys(false);
 
             /* output debug info
             getLogger().info(
                 ((args.length > 0) ? "args=" + args[0] : "") +
                 " , sp_inv_list=" + nameList.toString()
             );//*/
 
             // when type /tis [query]
             if(args.length == 1 && args[0].length() > 0){
 
                 // calculate similarity between sp-inv-name and query
                 // using Levenshtein distance.
                 int minDist = 0;
                 boolean isFirst = true;
                 for(String name : nameList){
                     int dist = LevenshteinDistance.computeLevenshteinDistance(name, args[0]);
                     if(name.startsWith(args[0])){
                         dist -= args[0].length() * 2;  // matching weight is twice
                     }
                     if(isFirst){
                         targetInv = name;
                         minDist = dist;
                         isFirst = false;
                     }
                     else if(dist < minDist){
                         targetInv = name;
                         minDist = dist;
                     }
                     //getLogger().info("A=" + name + " , B=" + args[0] + " , Distance=" + Integer.toString(dist));
                 }
             }
             // when type just /tis
             else{
                 // select special inv from file in order when user is using special inv.
                 String[] nameListString = nameList.toArray(new String[0]);
                 if(nameListString.length == 0){
                     getLogger().warning("There are no special inventories in " + CONFIG_FILENAME_SPECIAL_INV + ". Please check it.");
                     sender.sendMessage("There are no special inventories!");
                     return true;
                 }
                 if(isSpecialInvEnabled){
                     int targetIndex = -1;
                     for(int i=0; i < nameListString.length; i++){
                         String name = nameListString[i];
                         if(name.equals(currentSpecialInv)){
                             targetIndex = i+1;
                         }
                     }
                     if(targetIndex >= nameListString.length || targetIndex < 0){
                         targetIndex = 0;
                     }
                     targetInv = nameListString[targetIndex];
                 }
                 // if user was not using special-inv, select first inv.
                 else{
                     targetInv = nameListString[0];
                 }
             }
 
             //if target inv was found, load it.
             if(targetInv != null){
 
                 // save inventory if needed.
                 try{
                     if(!isSpecialInvEnabled){
                         saveInventory(sender);
                     }
                     pConf.set(CONFIG_INVENTORY_SPCIAL_INV_INDEX, targetInv);
                     pConf.save(configFile);
                 }
                 catch (Exception e){
                     sender.sendMessage(ChatColor.RED + "Cannot save your inventory." + ChatColor.RESET);
                     return true;
                 }
 
                 // load
                 deserializeInventoryConfig(
                         player.getInventory(),
                         specialInventoriesConfig.getConfigurationSection(targetInv)
                         );
 
                 // create inventories list message
                 String msg = ChatColor.GRAY+"[";
                 for(String name: nameList){
                     if(name.equals(targetInv)){
                         msg += ChatColor.GREEN+name;
                     }
                     else{
                         msg += ChatColor.DARK_GREEN+name;
                     }
                     msg += ChatColor.RESET+" ";
                 }
 
                 sender.sendMessage(msg + ChatColor.GRAY + "]  is toggled.");
                 return true;
             }
             else{
                 sender.sendMessage("There is no matched inventory.");
                 return true;
             }
 
         }
         return false;
     }
 
 }
 
 // string match for /tis command.
 // when distance=0: equals
 // when distance=1: 1 character is different
 class LevenshteinDistance {
     private static int minimum(int a, int b, int c) {
         return Math.min(Math.min(a, b), c);
     }
 
     public static int computeLevenshteinDistance(CharSequence str1,
             CharSequence str2) {
         int[][] distance = new int[str1.length() + 1][str2.length() + 1];
 
         for (int i = 0; i <= str1.length(); i++)
             distance[i][0] = i;
         for (int j = 1; j <= str2.length(); j++)
             distance[0][j] = j;
 
         for (int i = 1; i <= str1.length(); i++)
             for (int j = 1; j <= str2.length(); j++)
                 distance[i][j] = minimum(
                         distance[i - 1][j] + 1,
                         distance[i][j - 1] + 1,
                         distance[i - 1][j - 1]
                                 + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                         : 1));
 
         return distance[str1.length()][str2.length()];
     }
 }
