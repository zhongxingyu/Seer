 package me.arboriginal.Insurance;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Insurance extends JavaPlugin {
   protected FileConfiguration config;
 
   @Override
   public void onDisable() {
   }
 
   @Override
   public void onEnable() {
     initConfig();
     getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH,
         new InsuranceEntityListener(this), Priority.Normal, this);
   }
 
   @Override
   public void reloadConfig() {
     super.reloadConfig();
     initConfig();
   }
 
   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     if (command.getName().equals("insurance-calculate")) {
       return runCommandCalculate(sender, args);
     }
 
     if (command.getName().equals("insurance-reload")) {
       return runCommandReload(sender, args);
     }
 
     return false;
   }
 
   public float readStuff(List<ItemStack> stuff, String player) {
     float amount = 0;
     boolean log = config.getBoolean("Insurance.log_prime");
     List<ItemStack> paid = new ArrayList<ItemStack>();
     String prime = null;
 
     if (log) {
       prime = player + "\n";
     }
 
     Iterator<ItemStack> i = stuff.iterator();
 
     while (i.hasNext()) {
       ItemStack stack = i.next();
       float price = calculatePrice(stack);
 
       if (price > 0) {
         paid.add(stack);
         amount += price;
 
         if (log) {
           Material type = stack.getType();
           List<String> subMaterials = getSubMaterials(type);
           float condition = calculateCondition(stack);
           String details = "";
 
           if (subMaterials.size() > 0) {
             details += subMaterials.get(stack.getData().getData()) + " ";
           }
 
           if (condition < 1) {
             details += "condition: " + (100 * condition) + "%";
           }
 
           if (!details.equals("")) {
             details = " (" + details.trim() + ") ";
           }
 
           prime += stack.getAmount() + "x " + type + details + ": " + price + "\n";
         }
       }
     }
 
     stuff.removeAll(paid);
 
     if (log) {
       prime += "-------------------------------------------------------------\n";
       prime += "Total: " + amount + "\n";
 
       logPrime(prime);
     }
 
     return amount;
   }
 
   private void logPrime(String prime) {
     Logger logger = Logger.getLogger("Insurance");
 
     FileHandler fh = null;
 
     try {
       fh = new FileHandler(getDataFolder() + "/primesLog.txt",
           config.getInt("Insurance.log_filesize"), 1, true);
       fh.setFormatter(new InsuranceLogFormatter());
 
       logger.addHandler(fh);
       logger.log(Level.OFF, prime);
 
       if (fh != null) {
         fh.close();
       }
     }
     catch (SecurityException e) {
       e.printStackTrace();
     }
     catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   private float calculatePrime(Player player) {
     float amount = 0;
     ItemStack[] stuff = player.getInventory().getContents();
 
     if (stuff.length > 0) {
       for (int i = 0; i < stuff.length; i++) {
         ItemStack stack = stuff[i];
         float prime = calculatePrice(stack);
 
         if (prime > 0) {
           amount += prime;
         }
       }
     }
 
     return amount;
   }
 
   private float calculatePrice(ItemStack stack) {
     float price = getItemPrice(stack);
 
     if (price > 0) {
       price *= stack.getAmount();
     }
 
     return price;
   }
 
   private float getItemPrice(ItemStack stack) {
     float finalPrice = 0;
 
     if (stack != null) {
       String key = "Insurance.primes." + stack.getType();
 
       if (config.contains(key)) {
         Object price = config.get(key);
 
         if (!(price instanceof Double)) {
           key += "." + getSubMaterials(stack.getType()).get(stack.getData().getData());
           price = config.getDouble(key);
         }
 
         finalPrice += (Double) price * calculateCondition(stack);
       }
     }
 
     return finalPrice;
   }
 
   public void payPlayer(Player player, float amount) {
     player.sendMessage(renderString("pay_message", player.getName(), amount));
 
     getServer().dispatchCommand(getServer().getConsoleSender(),
         renderString("pay_command", player.getName(), amount));
   }
 
   private void initConfig() {
     config = getConfig();
     boolean edited = checkConfigValue("Insurance.log_prime", true);
 
     edited = checkConfigValue("Insurance.log_filesize", 10000) || edited;
     edited = checkConfigValue("Insurance.calc_message",
         "<player>, if you die now, you will receive <amount> dollars.") || edited;
     edited = checkConfigValue("Insurance.pay_message",
         "<player>, you will receive soon <amount> dollars for your lost stuff.") || edited;
     edited = checkConfigValue("Insurance.pay_command",
         "tell <player> Your admin didn't make his job, you loose <amount> dollars :D") || edited;
     edited = checkConfigValue("Insurance.consider_condition", true) || edited;
 
     Material[] values = Material.values();
 
     for (int i = 0; i < values.length; i++) {
       Double defaultValue = 0.0;
       List<String> subMaterials = getSubMaterials(values[i]);
 
       if (subMaterials.size() == 0) {
         edited = checkConfigValue("Insurance.primes." + values[i], defaultValue) || edited;
       }
       else {
         if (config.contains("Insurance.primes." + values[i])) {
           Object value = config.get("Insurance.primes." + values[i]);
 
           if (value instanceof Double) {
             defaultValue += (Double) value;
           }
         }
 
         for (String sm : subMaterials) {
           edited = checkConfigValue("Insurance.primes." + values[i] + "." + sm, defaultValue)
               || edited;
         }
       }
     }
 
     if (edited) {
       saveConfig();
       System.out.println("[Insurance] Omitted values has been added to your config file.");
     }
   }
 
   private List<String> getSubMaterials(Material material) {
     List<String> values = new ArrayList<String>();
 
     switch (material) {
       case SAPLING:
       case LOG:
       case LEAVES:
         values.add(0, "Usual");
         values.add(1, "Spruce");
         values.add(2, "Birch");
         break;
 
       case HUGE_MUSHROOM_1:
       case HUGE_MUSHROOM_2:
         values.add(0, "Fleshy");
         values.add(1, "Top_north_west");
         values.add(2, "Top_north");
         values.add(3, "Top_north_east");
         values.add(4, "Top_west");
         values.add(5, "Top");
         values.add(6, "Top_east");
         values.add(7, "Top_south_west");
         values.add(8, "Top_south");
         values.add(9, "Top_south_east");
         values.add(10, "Stem");
         break;
 
       case COAL:
         values.add(0, "Coal");
         values.add(1, "Charcoal");
         break;
 
       case INK_SACK:
         values.add(0, "Ink_Sac");
         values.add(1, "Rose_Red");
         values.add(2, "Cactus_Green");
         values.add(3, "Cocoa_Beans");
         values.add(4, "Lapis_Lazuli");
         values.add(5, "Purple_Dye");
         values.add(6, "Cyan_Dye");
         values.add(7, "Light_Gray_Dye");
         values.add(8, "Gray_Dye");
         values.add(9, "Pink_Dye");
         values.add(10, "Lime_Dye");
         values.add(11, "Dandelion_Yellow");
         values.add(12, "Light_Blue_Dye");
         values.add(13, "Magenta_Dye");
         values.add(14, "Orange_Dye");
         values.add(15, "Bone_Meal");
         break;
 
       case WOOL:
         values.add(0, "White");
         values.add(1, "Orange");
         values.add(2, "Magenta");
         values.add(3, "Light_Blue");
         values.add(4, "Yellow");
         values.add(5, "Lime");
         values.add(6, "Pink");
         values.add(7, "Gray");
         values.add(8, "Light_Gray");
         values.add(9, "Cyan");
         values.add(10, "Purple");
         values.add(11, "Blue");
         values.add(12, "Brown");
         values.add(13, "Green");
         values.add(14, "Red");
         values.add(15, "Black");
         break;
 
       case STEP:
       case DOUBLE_STEP:
         values.add(0, "Stone_Slab");
         values.add(1, "Sandstone_Slab");
         values.add(2, "Wooden_Slab");
         values.add(3, "Cobblestone_Slab");
         values.add(4, "Brick_Slab");
         values.add(5, "Stone_Brick_Slab");
         values.add(6, "Stone_Slab");
         break;
 
       case SMOOTH_BRICK:
         values.add(0, "Normal");
         values.add(1, "Mossy");
         values.add(2, "Cracked");
         break;
     }
 
     return values;
   }
 
   private float calculateCondition(ItemStack stack) {
     if (!config.getBoolean("Insurance.consider_condition")) {
      return 1;
     }
 
     int maxDurability = getMaxDurability(stack.getType());
 
     if (maxDurability == 0) {
      return 1;
     }
 
     return 1 - stack.getDurability() / (float) maxDurability;
   }
 
   private int getMaxDurability(Material material) {
     switch (material) {
       case FLINT_AND_STEEL:
       case FISHING_ROD:
         return 65;
 
       case SHEARS:
         return 239;
 
       case GOLD_SWORD:
       case GOLD_PICKAXE:
       case GOLD_SPADE:
       case GOLD_AXE:
       case GOLD_HOE:
         return 33;
 
       case WOOD_SWORD:
       case WOOD_PICKAXE:
       case WOOD_SPADE:
       case WOOD_AXE:
       case WOOD_HOE:
         return 60;
 
       case STONE_SWORD:
       case STONE_PICKAXE:
       case STONE_SPADE:
       case STONE_AXE:
       case STONE_HOE:
         return 132;
 
       case IRON_SWORD:
       case IRON_PICKAXE:
       case IRON_SPADE:
       case IRON_AXE:
       case IRON_HOE:
         return 251;
 
       case DIAMOND_SWORD:
       case DIAMOND_PICKAXE:
       case DIAMOND_SPADE:
       case DIAMOND_AXE:
       case DIAMOND_HOE:
         return 1562;
 
       case LEATHER_HELMET:
         return 34;
       case LEATHER_CHESTPLATE:
         return 49;
       case LEATHER_LEGGINGS:
         return 46;
       case LEATHER_BOOTS:
         return 40;
 
       case CHAINMAIL_HELMET:
         return 67;
       case CHAINMAIL_CHESTPLATE:
         return 96;
       case CHAINMAIL_LEGGINGS:
         return 92;
       case CHAINMAIL_BOOTS:
         return 79;
 
       case GOLD_HELMET:
         return 68;
       case GOLD_CHESTPLATE:
         return 96;
       case GOLD_LEGGINGS:
         return 92;
       case GOLD_BOOTS:
         return 80;
 
       case IRON_HELMET:
         return 136;
       case IRON_CHESTPLATE:
         return 192;
       case IRON_LEGGINGS:
         return 184;
       case IRON_BOOTS:
         return 160;
 
       case DIAMOND_HELMET:
         return 272;
       case DIAMOND_CHESTPLATE:
         return 384;
       case DIAMOND_LEGGINGS:
         return 368;
       case DIAMOND_BOOTS:
         return 320;
 
       default:
         return 0;
     }
   }
 
   private boolean checkConfigValue(String key, Object defaultValue) {
     if (!config.contains(key)) {
       config.set(key, defaultValue);
 
       return true;
     }
 
     return false;
   }
 
   private boolean runCommandCalculate(CommandSender sender, String[] args) {
     Player player = null;
 
     if (args.length == 1 && sender.hasPermission("Insurance.calculatePrime")) {
       player = getServer().getPlayer(args[0]);
 
       if (player == null) {
         sender.sendMessage(ChatColor.RED + "This player is not online.");
 
         return true;
       }
     }
     else if (args.length == 0 && sender.hasPermission("Insurance.calculateMyPrime")) {
       if (sender instanceof Player) {
         player = (Player) sender;
       }
       else {
         sender.sendMessage(ChatColor.RED + "You need to specify a playername.");
 
         return true;
       }
     }
     else {
       return false;
     }
 
     sender.sendMessage(ChatColor.GREEN
         + renderString("calc_message", player.getName(), calculatePrime(player)));
 
     return true;
   }
 
   private boolean runCommandReload(CommandSender sender, String[] args) {
     if (!sender.hasPermission("Insurance.reload")) {
       sender.sendMessage("Unknown command. Type \"help\" for help.");
 
       return true;
     }
 
     if (args.length == 0) {
       reloadConfig();
       sender.sendMessage(ChatColor.GREEN + "Insurance config has been reloaded!");
 
       return true;
     }
 
     return false;
   }
 
   private String renderString(String key, String player, float amount) {
     return config.getString("Insurance." + key).replace("<player>", player)
         .replace("<amount>", "" + amount);
   }
 
   // Internal class
 
   private class InsuranceLogFormatter extends Formatter {
     @Override
     public String format(LogRecord record) {
       return new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(new Date()) + " - "
           + formatMessage(record) + "\n\n";
     }
 
   }
 }
