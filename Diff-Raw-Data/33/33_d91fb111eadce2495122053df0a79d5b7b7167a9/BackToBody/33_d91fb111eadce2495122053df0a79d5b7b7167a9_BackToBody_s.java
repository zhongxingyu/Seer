 package com.sparkedia.valrix.BackToBody;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.register.payment.Method;
 import com.nijikokun.register.payment.Methods;
 import com.nijikokun.register.payment.Method.MethodAccount;
 
 public class BackToBody extends JavaPlugin {
     protected static Logger log;
     protected static String df;
     protected static String name;
     protected static String version;
     protected static Property conf;
     protected static Property bodies;
    private Property bodyCache;
     protected static HashMap<String, Boolean> delayed = new HashMap<String, Boolean>();
     protected static BackToBody btb;
     
     // Function to do the symbol replacement
     private String format(String s) {
         return s.replace("&0", "\u00A70") // Black
                 .replace("&1", "\u00A71") // Dark Blue
                 .replace("&2", "\u00A72") // Dark Green
                 .replace("&3", "\u00A73") // Teal
                 .replace("&4", "\u00A74") // Dark Red
                 .replace("&5", "\u00A75") // Purple
                 .replace("&6", "\u00A76") // Gold
                 .replace("&7", "\u00A77") // Gray
                 .replace("&8", "\u00A78") // Dark Gray
                 .replace("&9", "\u00A79") // Blue
                 .replace("&A", "\u00A7A") // Green
                 .replace("&B", "\u00A7B") // Aqua
                 .replace("&C", "\u00A7C") // Red
                 .replace("&D", "\u00A7D") // Pink
                 .replace("&E", "\u00A7E") // Yellow
                 .replace("&F", "\u00A7F");// White
     }
     
     // Remove player's corpse
     protected static void despawn(final String n) {
         btb.getServer().getScheduler().scheduleAsyncDelayedTask(btb, new Runnable() {
             public void run() {
                 bodies.remove(n);
             }
         }, 6000);
     }
     
     // Make player wait until they can return to their corpse
     protected static void delay(final String n) {
         btb.getServer().getScheduler().scheduleAsyncDelayedTask(btb, new Runnable() {
             public void run() {
                 delayed.remove(n);
             }
         }, conf.getInt("delay")*20);
     }
     
     @Override
     public void onDisable() {
         final String s = "[BackToBody] v"+version+" has been disabled.";
         // Clean up
         conf = null;
         log = null;
         df = null;
         name = null;
         version = null;
         bodies = null;
         bodyCache = null;
         delayed.clear();
         delayed = null;
         btb = null;
         getServer().getLogger().info(s);
     }
     
     @Override
     public void onEnable() {
         btb = this;
         log = getServer().getLogger();
         
         final PluginDescriptionFile pdf = getDescription();
         name = pdf.getName();
         version = pdf.getVersion();
         
         df = getDataFolder().toString();
         bodies = new Property(df+"/bodies.loc");
         
         // Plugin manager
         final PluginManager pm = getServer().getPluginManager();
         
         // Register events to economy listener, only if Register is found
         if (pm.getPlugin("Register") != null) {
             final BodyServer bs = new BodyServer(pm);
             pm.registerEvent(Type.PLUGIN_ENABLE, bs, Priority.Monitor, this);
             pm.registerEvent(Type.PLUGIN_DISABLE, bs, Priority.Monitor, this);
         } else {
             log.info('['+name+"] Register is required in order to charge for returning to their body.");
         }
         
         // Register events to the listeners
         pm.registerEvent(Type.ENTITY_DEATH, new BEntityListener(), Priority.Lowest, this);
         
         // Create data folder if it doesn't exist
         if (!(new File(df)).isDirectory()) {
             new File(df).mkdir();
         }
         
         // Then make the config if it doesn't exist, otherwise load it
         conf = new Property(df+"/config.txt");
         if (!(new File(df+"/config.txt").exists())) {
             conf.setString("#0", "Amount to charge player to return to body")
                 .setNumber("cost", 0)
                 .setString("#1", "Amount of time player must wait to return to body")
                 .setNumber("delay", 0)
                 .setString("#2", "Player's body location is removed after 5 minutes. Options: true or false")
                 .setString("decay", "true")
                 .setString("#3", "How often player locations are saved. Options: always, never, time in seconds (30, 60, etc.)")
                 .setString("save", "never") // always, never, # of seconds
                 .save();
         }
         final Property opts = new Property(df+"/alerts.txt");
         if (!(new File(df+"/alerts.txt").exists())) {
             opts.setString("#0", "Player doesn't have a body to teleport to.")
                 .setString("no_body", "")
                 .setString("#1", "Return player and charge them for it.")
                 .setString("return", "")
                 .setString("#2", "Return player for free.")
                 .setString("free_return", "")
                 .setString("#3", "Player doesn't have enough money.")
                 .setString("no_cash", "")
                 .setString("#4", "Admin wants to charge, but no economy plugin is set up.")
                 .setString("no_econ", "")
                 .setString("#5", "Player isn't allowed to use the /btb command.")
                 .setString("no_perm", "")
                 .setString("#6", "--------------------------")
                 .setString("#7", "--- Examples (default) ---")
                 .setString("#8", "--------------------------")
                 .setString("#9", "no_body=&4The Reaper's voice echos, &5\"Your flesh still lives...for now.\"")
                 .setString("#10", "return=&4The Reaper&5 returns you to your corpse, but for a price. You forfeit &A%cost%&5.")
                 .setString("#11", "free_return=&4The Reaper&5 takes your corpse as you return to it...")
                 .setString("#12", "no_cash=&5You hear a sinister laugh, &4\"The toll is &A%cost%&4 to return to your corpse.\"")
                 .setString("#13", "no_econ=&4The Reaper:&5 \"Your admin would like to charge you, but needs an economy. This passage is free.\"")
                 .setString("#14", "no_perm=&4The Reaper screams,&5 \"You don't have permission to use &C/btb&5, mortal!\"")
                 .save();
         }
         
         // Set up the auto-save timer if a time is specified in the config
         if (!conf.getString("save").equalsIgnoreCase("never") && !conf.getString("save").equalsIgnoreCase("always") && !conf.getString("save").equals("0")) {
            final int d = conf.getInt("delay")*20;
             getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
                 public void run() {
                    if (bodyCache.equals(bodies)) return;
                     if (!conf.getString("save").equalsIgnoreCase("never")) {
                         bodies.save();
                     }
                    bodyCache = bodies;
                 }
             }, d, d);
         }
         
         getCommand("btb").setExecutor(new CommandExecutor() {
             public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
                 if (!(sender instanceof Player) || !cmd.getName().equalsIgnoreCase("btb")) return false;
                 // Make sure we always have the newest flavor text and config loaded
                 conf.load();
                 opts.load();
                 
                 final Player player = ((Player)sender);
                 final String name = player.getName();
                 // Permission check
                if (player.hasPermission("backtobody.btb")) {
                     player.sendMessage(opts.isEmpty("no_perm") ? ChatColor.DARK_RED+"The Reaper screams,"+ChatColor.DARK_PURPLE+" \"You do not have permission to use "+ChatColor.RED+"/btb"+ChatColor.DARK_PURPLE+", mortal!\"" : format(opts.getString("no_perm")));
                     return true;
                 }
                 final Location loc = player.getLocation();
                 final Property bodies = new Property(df+"/bodies.loc");
                 // They didn't die yet, so they can't go back to their body
                 if (!bodies.keyExists(name)) {
                     player.sendMessage(opts.isEmpty("no_body") ? ChatColor.DARK_RED+"The Reaper's voice echos,"+ChatColor.DARK_PURPLE+" \"Your flesh still lives...for now.\"" : format(opts.getString("no_body")));
                     return true;
                 }
                 final double cost = conf.getDouble("cost");
                 // Free return
                 if (cost <= 0) {
                     final String[] s = bodies.getString(name).split("~");
                     // Return player to corpse's location
                     loc.setX(Double.parseDouble(s[0]));
                     loc.setY(Double.parseDouble(s[1]));
                     loc.setZ(Double.parseDouble(s[2]));
                     loc.setYaw(Float.parseFloat(s[3]));
                     loc.setWorld(getServer().getWorld(s[4]));
                     player.teleport(loc);
                     player.sendMessage(opts.isEmpty("free_return") ? ChatColor.DARK_RED+"The Reaper"+ChatColor.DARK_PURPLE+" takes your corpse as you return to it..." : format(opts.getString("free_return")));
                     bodies.remove(name);
                     if (!conf.getString("save").equalsIgnoreCase("never")) {
                         bodies.save();
                     }
                     return true;
                 }
                 if (Methods.hasMethod()) {
                     final Method economy = Methods.getMethod();
                     final MethodAccount acct = economy.getAccount(name);
                     if (acct.hasEnough(cost)) {
                         // Take money
                         acct.subtract(cost);
                         
                         final String[] s = bodies.getString(name).split("~");
                         // Return player to where they died
                         loc.setX(Double.parseDouble(s[0]));
                         loc.setY(Double.parseDouble(s[1]));
                         loc.setZ(Double.parseDouble(s[2]));
                         loc.setYaw(Float.parseFloat(s[3]));
                         loc.setWorld(getServer().getWorld(s[4]));
                         player.teleport(loc);
                         
                         // Send message and remove .loc file
                         player.sendMessage(opts.isEmpty("return") ? ChatColor.DARK_RED+"The Reaper"+ChatColor.DARK_PURPLE+" returns you to your corpse, but for a price. You forfeit "+ChatColor.GREEN+economy.format(cost)+ChatColor.DARK_PURPLE+'.' : format(opts.getString("return")).replace("%cost%", economy.format(cost)));
                         bodies.remove(name);
                         if (!conf.getString("save").equalsIgnoreCase("never")) {
                             bodies.save();
                         }
                         return true;
                     }
                     // Lacks funds to pay
                     player.sendMessage(opts.isEmpty("no_cash") ? ChatColor.DARK_PURPLE+"You hear a sinister laugh,"+ChatColor.DARK_RED+" \"The toll is "+ChatColor.GREEN+economy.format(cost)+ChatColor.DARK_RED+" to return to your corpse.\"" : format(opts.getString("no_cash")).replace("%cost%", economy.format(cost)));
                     return true;
                 }
                 // Wants to charge, but no economy is set up
                 player.sendMessage(opts.isEmpty("no_econ") ? ChatColor.DARK_RED+"The Reaper:"+ChatColor.DARK_PURPLE+" \"Your admin would like to charge you, but needs an economy. This passage is free.\"" : format(opts.getString("no_econ")));
                 log.severe('['+name+"]: No economy is enabled. Remove the price or enable a Register-supported economy.");
                 final String[] s = bodies.getString(name).split("~");
                 loc.setX(Double.parseDouble(s[0]));
                 loc.setY(Double.parseDouble(s[1]));
                 loc.setZ(Double.parseDouble(s[2]));
                 loc.setYaw(Float.parseFloat(s[3]));
                 loc.setWorld(getServer().getWorld(s[4]));
                 player.teleport(loc);
                 bodies.remove(name);
                 if (!conf.getString("save").equalsIgnoreCase("never")) {
                     bodies.save();
                 }
                 return true;
             }
         });
         
         log.info('['+name+"] v"+version+" has been enabled.");
     }
 }
