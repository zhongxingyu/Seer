 package info.somethingodd.bukkit.OddGive;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import info.somethingodd.bukkit.OddItem.OddItem;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * OddGive for Bukkit
  *
  * @author petteyg359
  */
 
 public class OddGive extends JavaPlugin {
     private static Logger log;
     private static PluginDescriptionFile info;
     private PermissionHandler Permissions = null;
     private OddItem oddItem = null;
     private static String dataDir = "plugins" + File.separator + "Odd";
     private static String config = dataDir + File.separator + "give.txt";
     private static Set<String> itemlist = new HashSet<String>();
     private static boolean blacklist;
     private static int defaultQuantity;
     private static String logPrefix;
 
    private void give(CommandSender sender, String[] args) {
         Set<Player> players = new HashSet<Player>();
         Set<ItemStack> items = new HashSet<ItemStack>();
         if (args.length == 0)
             return;
        if (args[0].equals("*")) {
             if (args.length > 1)
                 players.addAll(Arrays.asList(getServer().getOnlinePlayers()));
         } else {
             int i = 0;
            while (getServer().getPlayer(args[i]) != null) {
                players.add(getServer().getPlayer(args[i]));
                i++;
             }
             for (; i < args.length; i++) {
                 ItemStack is = null;
                 try {
                     is = oddItem.getItemStack(args[i]);
                     try {
                         is.setAmount(Integer.decode(args[i + 1]));
                         i++;
                     } catch (NumberFormatException e) {
                         is.setAmount(defaultQuantity);
                     } catch (ArrayIndexOutOfBoundsException e) {
                         is.setAmount(defaultQuantity);
                     }
                    items.add(is);
                 } catch (IllegalArgumentException e) {
                     sender.sendMessage(logPrefix + "Invalid item: " + args[i] + " Closest match: " + e.getMessage());
                 }
             }
             if (players.isEmpty())
                 players.add((Player) sender);
             for (Player p : players) {
                 Inventory inventory = p.getInventory();
                 for (ItemStack m : items) {
                    boolean deny = false;
                    if ((!blacklist && !itemlist.contains(m.getType().name())) || (blacklist && itemlist.contains(m.getType().name())))
                        deny = true;
                    if (!(deny && !(sender.isOp() || (Permissions != null && Permissions.has((Player) sender, "odd.give.override")))))
                        inventory.addItem(m);
                 }
             }
         }
     }
 
     private void take(CommandSender sender, String[] args) {
         Set<Player> players = new HashSet<Player>();
         Set<ItemStack> items = new HashSet<ItemStack>();
         if (args.length == 0) {
             if (!(sender instanceof Player))
                 sender.sendMessage("You have no inventory, silly!");
             else
                 ((Player) sender).getInventory().clear();
             return;
         }
         if (args[0].equals("*")) {
             if (args.length > 1) {
                 players.addAll(Arrays.asList(getServer().getOnlinePlayers()));
             } else {
                 if (!(sender instanceof Player))
                     sender.sendMessage("You have no inventory, silly!");
                 else
                     ((Player) sender).getInventory().clear();
             }
         } else {
             int i = 0;
             for (; i < args.length && getServer().getPlayer(args[i]) != null; i++) {
                 players.add(getServer().getPlayer(args[i]));
             }
             if (args.length == i || args[i].equals("*")) {
                 for (Player p : players)
                     p.getInventory().clear();
                 return;
             } else {
                 for (; i < args.length; i++) {
                     ItemStack is;
                     try {
                         is = oddItem.getItemStack(args[i]);
                         try {
                             is.setAmount(Integer.decode(args[i + 1]));
                             i++;
                         } catch (NumberFormatException e) {
                             is.setAmount(Integer.MAX_VALUE);
                         } catch (ArrayIndexOutOfBoundsException e) {
                             is.setAmount(Integer.MAX_VALUE);
                         }
                         items.add(is);
                     } catch (IllegalArgumentException iae) {
                         sender.sendMessage(logPrefix + "Invalid item: " + args[i]);
                     }
                 }
             }
             if (players.isEmpty())
                 players.add((Player) sender);
             for (Player p : players) {
                 Inventory inventory = p.getInventory();
                 for (ItemStack m : items) {
                     ItemStack[] is = inventory.getContents();
                     int q = m.getAmount();
                     for (int j = 0; (q > 0 && j < is.length); j++) {
                         if (is[j] != null && is[j].getType().equals(m.getType()) && (is[j].getData() == null || (is[j].getData() != null && is[j].getData().getData() == m.getData().getData()))) {
                             if (is[j].getAmount() >= q) {
                                 is[j].setAmount(is[j].getAmount() - q);
                                 q -= is[j].getAmount();
                             } else {
                                 q -= is[j].getAmount();
                                 inventory.clear(j);
                             }
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         if (Permissions == null && !sender.isOp())
             return true;
         if (!sender.isOp() && sender instanceof Player && Permissions != null && !Permissions.has((Player) sender, "odd.give." + commandLabel))
             return true;
         if (commandLabel.equals("i0")) {
             if (args.length > 0 && getServer().getPlayer(args[0]) != null)
                 if (!sender.isOp() && sender instanceof Player && Permissions != null && !Permissions.has((Player) sender, "odd.give.i0.other"))
                     return true;
             take(sender, args);
        } else if (commandLabel.equals("i") || commandLabel.equals("give")) {
            give(sender, args);
         } else if (commandLabel.equals("og")) {
             if (args.length == 1 && args[0].equals("list")) {
                 sender.sendMessage(itemlist.toString());
                 return true;
             }
             return false;
         }
         return true;
     }
 
     @Override
     public void onDisable() {
         log.info(logPrefix + "disabled" );
     }
 
     @Override
     public void onEnable() {
         Plugin p;
         p = getServer().getPluginManager().getPlugin("OddItem");
         if (p != null) {
             oddItem = (OddItem) p;
         } else {
             log.severe(logPrefix + "Couldn't find OddItem.");
             getServer().getPluginManager().disablePlugin(this);
         }
         log.info(logPrefix + info.getVersion() + " enabled");
         p = getServer().getPluginManager().getPlugin("Permissions");
         if (p != null) {
             getServer().getPluginManager().enablePlugin(p);
             Permissions = ((Permissions) p).getHandler();
         } else {
             log.info(logPrefix + "Permissions not found. Using op-only mode.");
         }
         parseConfig(readConfig());
     }
 
     @Override
     public void onLoad() {
         info = getDescription();
         log = getServer().getLogger();
         logPrefix = "[" + info.getName() + "] ";
     }
 
     private void parseConfig(String s) {
         defaultQuantity = 64;
         blacklist = true;
         String[] l = s.split(System.getProperty("line.separator"));
         for (String Al : l) {
             String[] m = Al.split(" ", 2);
             if (m[0].equals("type:")) {
                 if (m[1].equals("blacklist"))
                     blacklist = true;
                 else if (m[1].equals("whitelist"))
                     blacklist = false;
                 else
                     log.warning(logPrefix + "Invalid value for type: " + m[1]);
             } else if (m[0].equals("quantity:")) {
                 try {
                     defaultQuantity = Integer.decode(m[1]);
                 } catch (NumberFormatException nfe) {
                     log.warning(logPrefix + "Invalid quantity: " + m[1]);
                 }
             } else if (m[0].equals("items:")) {
                 String[] n = m[1].split(" ");
                 for (String An : n) {
                     itemlist.add(An);
                     String[] aliases = {};
                     aliases = oddItem.getAliases(An).toArray(aliases);
                     itemlist.addAll(Arrays.asList(aliases));
                 }
                 log.info(logPrefix + (blacklist ? "Black" : "White") + "listed " + itemlist.size() + " items.");
             }
         }
     }
 
     private static String readConfig() {
         boolean dirExists = new File(dataDir).exists();
         if (!dirExists) {
             try {
                 new File(dataDir).mkdir();
             } catch (SecurityException se) {
                 log.severe(se.getMessage());
                 return null;
             }
         }
         boolean fileExists = new File(config).exists();
         if (!fileExists) {
             try {
                 new File(config).createNewFile();
             } catch (IOException ioe) {
                 log.severe(ioe.getMessage());
                 return null;
             }
         }
         File file = new File(config);
         StringBuilder contents = new StringBuilder();
         try {
             BufferedReader input = new BufferedReader(new FileReader(file));
             try {
                 String line = input.readLine();
                 while (line != null) {
                     contents.append(line);
                     contents.append(System.getProperty("line.separator"));
                     line = input.readLine();
                 }
             } catch (IOException ioe) {
                 log.warning(logPrefix + "Error reading config: " + ioe.getMessage());
             } finally {
                 input.close();
             }
         } catch (IOException ie) {
             log.severe(ie.getMessage());
         }
         return contents.toString();
     }
 
 }
