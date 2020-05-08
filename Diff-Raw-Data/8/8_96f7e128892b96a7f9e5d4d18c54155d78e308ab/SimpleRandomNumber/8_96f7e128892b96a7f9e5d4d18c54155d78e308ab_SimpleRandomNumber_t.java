 package com.gmail.jameshealey1994.simplerandomnumber;
 
 import java.util.Random;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Simple Random Number Generation Plugin for Bukkit.
  *
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public final class SimpleRandomNumber extends JavaPlugin {
 
     /**
      * Fallback value for defaultMin incase a new value is attempted but
      * invalid.
      */
     private final static int FALLBACK_MIN = 1;
 
     /**
      * Fallback value for defaultMax incase a new value is attempted but
      * invalid.
      */
     private final static int FALLBACK_MAX = 6;
    
    /**
     * Default broadcast message, incase the specified value is invalid.
     */
    private final static String DEFAULT_BROADCAST_MESSAGE = "-sender &3rolled (-min to -max) and got &6'-result'!";
 
     /**
      * Minimum of the random number generated, if a minimum is not specified.
      */
     private int defaultMin = FALLBACK_MIN;
 
     /**
      * Maximum of the random number generated, if a maximum is not specified.
      */
     private int defaultMax = FALLBACK_MAX;
 
     @Override
     public void onEnable() {
 
         // Save a copy of the default config.yml if one is not there
         this.saveDefaultConfig();
 
         // Set defaults from config.yml
         setDefaultsFromConfig(getServer().getConsoleSender());
     }
 
     /**
      * Sets defaultMin and defaultMax values from the configuration file.
      *
      * @param sender    sender of the command
      */
     private void setDefaultsFromConfig(CommandSender sender) {
         setDefaults(sender, getConfig().getInt("DefaultMinimum"), getConfig().getInt("DefaultMaximum"));
     }
 
     /**
      * Carries out commands.
      *
      * @param sender        sender of the command
      * @param cmd           command the user sent
      * @param commandLabel  exact command the user sent
      * @param args          arguments given with the command
      * @return              if a command was used correctly
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("srn")) {
             if (args.length > 0) {
                 try {
                     if (args[0].equalsIgnoreCase("setmin")) { // Set default minimum
                         return setDefaultMin(sender, Integer.valueOf(args[1]));
                     } else if (args[0].equalsIgnoreCase("setmax")) { // Set default maximum
                         return setDefaultMax(sender, Integer.valueOf(args[1]));
                     }
                 } catch (ArrayIndexOutOfBoundsException ex) {
                     sender.sendMessage(ChatColor.RED + "Please specify a new value!");
                     return true;
                 }
 
                 if (args[0].equalsIgnoreCase("reload")) { // Reload values from config
                     return reload(sender);
                 } else if (areInts(args)) {
                     return rollDice(sender, convertToIntArray(args));
                 } else {
                     sender.sendMessage(ChatColor.RED + "Custom range values need to be integers!");
                     return true;
                 }
             } else {
                 return rollDice(sender, new int[0]);
             }
         }
         return false;
     }
 
     /**
      * Convert string array to integer array.
      * Assumes strings in the input string array are valid integers
      *
      * @param strings   array of strings to be converted
      * @return          integer values of the input strings, as an array
      */
     private int[] convertToIntArray(String[] strings) {
         final int[] ints = new int[strings.length];
         for (int i = 0; i < strings.length; i++) {
             ints[i] = Integer.valueOf(strings[i]);
         }
         return ints;
     }
 
     /**
      * Displays a random number between 2 limits, either default or specified.
      *
      * @param sender    sender of the command
      * @param args      arguments given with the command, possibly including min
      *                  and max values
      * @return          if a command was used correctly
      */
     private boolean rollDice(CommandSender sender, int[] args) {
         if (args.length == 0) { // roll with default values
             if (sender.hasPermission("srn.defaults")) {
                 broadcastResult(sender, getDefaultMin(), getDefaultMax()); // /roll - Roll dice with default values
                 return true;
             } else {
                 sender.sendMessage(ChatColor.RED + "You need permission 'srn.defaults' to use this command.");
                 return false;
             }
         }
 
         if (!sender.hasPermission("srn.custom")) {
             sender.sendMessage(ChatColor.RED + "You need permission 'srn.custom' to use this command.");
             return false;
         }
 
         if (args.length == 1) { // roll <max>
             final int max = args[0];
             if (getDefaultMin() <= max) {
                 broadcastResult(sender, getDefaultMin(), max);
                 return true;
             } else {
                 sender.sendMessage(ChatColor.RED + "Max (" + max + ") lower than the default min (" + getDefaultMin() + ")!");
                 return false;
             }
         } else if (args.length == 2) { // roll <min> <max>
             final int min = args[0];
             final int max = args[1];
             if (min <= max) {
                 broadcastResult(sender, min, max);
                 return true;
             } else {
                 sender.sendMessage(ChatColor.RED + "Max (" + max + ") lower than your min (" + min + ")!");
                 return false;
             }
         } else { // not defaults and not custom
             sender.sendMessage(ChatColor.RED + "Invalid number of arguments!");
             return false;
         }
     }
 
     /**
      * Checks if the strings passed can be converted to integers.
      *
      * @param strings   array of strings that can possibly be converted to
      *                  integers
      * @return          if the array of strings passed can be converted to
      *                  integers
      */
     private boolean areInts(String[] strings) {
         for (int i = 0; i < strings.length; i++) {
             try {
                 Integer.parseInt(strings[i]);
             } catch (NumberFormatException ex) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Sets defaultMin and defaultMax values. Used when defaultMin and
      * defaultMax values are changed at the same time
      *
      * @param sender            sender of the command
      * @param newDefaultMin     attempted new defaultMin value
      * @param newDefaultMax     attempted new defaultMax value
      */
     public void setDefaults(CommandSender sender, int newDefaultMin, int newDefaultMax) {
         // Check config values for defaults are ok (max >= min)
         if (newDefaultMax >= newDefaultMin) {
             // If config values are OK, use as defaults
             defaultMin = newDefaultMin;
             defaultMax = newDefaultMax;
         } else {
             // If not, send message and continue using valid defaults
             sender.sendMessage("Default values in config are invalid. Using " + getDefaultMin() + " and " + getDefaultMax() + " as defaults.");
         }
     }
 
     /**
      * Reloads values from configuration file.
      *
      * @param sender    sender of the command
      * @return          if reload was completed correctly.
      */
     public boolean reload(CommandSender sender) {
         if (sender.hasPermission("srn.admin")) {
             reloadConfig();
             sender.sendMessage(ChatColor.LIGHT_PURPLE + "Configuration reloaded.");
             setDefaultsFromConfig(sender);
             sender.sendMessage(ChatColor.GRAY + "Current default min: '" + getDefaultMin() + "'");
             sender.sendMessage(ChatColor.GRAY + "Current default max: '" + getDefaultMax() + "'");
             return true;
         } else {
             sender.sendMessage(ChatColor.RED + "You need permission 'srn.admin' to use this command.");
             return false;
         }
     }
 
     /**
      * Broadcasts a random number between 2 limits.
      *
      * @param sender    sender of the command
      * @param min       minimum value the random number can be
      * @param max       maximum value the random number can be
      */
     private void broadcastResult(CommandSender sender, int min, int max) {
         final int result = getRandom(min, max);
 
        String message = getConfig().getString("BroadcastMessage", DEFAULT_BROADCAST_MESSAGE);
         message = ChatColor.translateAlternateColorCodes('&', message);
         message = message.replaceAll("-sender", sender.getName());
         message = message.replaceAll("-min", String.valueOf(min));
         message = message.replaceAll("-max", String.valueOf(max));
         message = message.replaceAll("-result", String.valueOf(result));
 
         getServer().broadcastMessage(message);
     }
 
     /**
      * Generates a random number between 2 limits.
      *
      * @param min   minimum value the random number can be
      * @param max   maximum value the random number can be
      * @return      random number between min and max
      */
     public int getRandom(int min, int max) {
         final Random rand = new Random();
 
         // nextInt is normally exclusive of the top value, so add 1 to make it inclusive
         return rand.nextInt(max - min + 1) + min;
     }
 
     /**
      * Gets the current defaultMin.
      *
      * @return  current defaultMin
      */
     public int getDefaultMin() {
         return defaultMin;
     }
 
     /**
      * Sets the default minimum.
      *
      * @param sender            sender of the command
      * @param newDefaultMin     attempted new defaultMin value
      * @return                  if new default minimum was set
      */
     public boolean setDefaultMin(CommandSender sender, int newDefaultMin) {
         if (sender.hasPermission("srn.admin")) {
             if (newDefaultMin > getDefaultMax()) {
                 sender.sendMessage(ChatColor.RED + "Attempted new default min (" + newDefaultMin + ") higher than current default max (" + getDefaultMax() + ")!");
                 return false;
             }
             this.defaultMin = newDefaultMin;
             getConfig().set("DefaultMinimum", getDefaultMin());
             saveConfig();
             sender.sendMessage(ChatColor.GRAY + "Default Minimum set to '" + getDefaultMin() + "'");
             return true;
         } else {
             sender.sendMessage(ChatColor.RED + "You need permission 'srn.admin' to use this command.");
             return false;
         }
     }
 
     /**
      * Gets the current defaultMax.
      *
      * @return  current defaultMax
      */
     public int getDefaultMax() {
         return defaultMax;
     }
 
     /**
      * Sets the default maximum.
      *
      * @param sender            sender of the command
      * @param newDefaultMax     attempted new defaultMax value
      * @return                  if new default maximum was set
      */
     public boolean setDefaultMax(CommandSender sender, int newDefaultMax) {
         if (sender.hasPermission("srn.admin")) {
             if (newDefaultMax < getDefaultMin()) {
                 sender.sendMessage(ChatColor.RED + "Attempted new default max (" + newDefaultMax + ") lower than current default min (" + getDefaultMin() + ")!");
                 return false;
             }
             this.defaultMax = newDefaultMax;
             getConfig().set("DefaultMaximum", getDefaultMax());
             saveConfig();
             sender.sendMessage(ChatColor.GRAY + "Default Maximum set to '" + getDefaultMax() + "'");
             return true;
         } else {
             sender.sendMessage(ChatColor.RED + "You need permission 'srn.admin' to use this command.");
             return false;
         }
     }
 }
