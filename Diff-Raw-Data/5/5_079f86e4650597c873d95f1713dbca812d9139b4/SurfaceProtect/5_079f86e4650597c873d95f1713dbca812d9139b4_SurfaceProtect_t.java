 package tzer0.SurfaceProtect;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import ca.xshade.bukkit.towny.Towny;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 // TODO: Auto-generated Javadoc
 /**
  * Plugin for bukkit allowing players to purchase mcMMO-experience points using iConomy/BOSEconomy-money.
  * 
  * @author TZer0
  */
 public class SurfaceProtect extends JavaPlugin {
     PluginDescriptionFile pdfFile;
     Configuration conf;
     public HashMap<String, WorldSettings> ws;
     private final SurfaceProtectBlockListener blockListener = new SurfaceProtectBlockListener(this);
     public PermissionHandler permissions;
     public LinkedHashSet<Integer> allow;
     @SuppressWarnings("unused")
     private final String name = "SurfaceProtect";
     Towny towny;
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onDisable()
      */
     public void onDisable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println(pdfFile.getName() + " disabled.");
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onEnable()
      */
     public void onEnable() {
         pdfFile = this.getDescription();
         conf = getConfiguration();
         reloadWorlds();
         allow = new LinkedHashSet<Integer>();
         List<String> whitelist = conf.getKeys("allowed.");
         if (whitelist != null) {
             for (String item : whitelist) {
                 allow.add(toInt(item));
             }
         }
         setupPermissions();
         //   playerListener.setPointers(getConfiguration(), permissions);
         PluginManager tmp = getServer().getPluginManager();
         if (tmp.getPlugin("Towny") != null)  {
             towny = (Towny) tmp.getPlugin("Towny");
        } else {
            towny = null;
         }
         //tmp.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
         tmp.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
         tmp.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
         System.out.println(pdfFile.getName() + " version "
                 + pdfFile.getVersion() + " is enabled!");
         conf.setProperty("version", pdfFile.getVersion());
         conf.save();
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         boolean help = false;
         if (commandLabel.equalsIgnoreCase("alloweditems") || commandLabel.equalsIgnoreCase("ai")) {
             sender.sendMessage("Whitelisted items:");
             Iterator<Integer> wl = allow.iterator();
             Integer tmp;
             String items = "";
             int i = 0;
             while (wl.hasNext()) {
                 if (i % 10 != 0) {
                     items += ", ";
                 }
                 if (i % 10 == 0 && i != 0) {
                     sender.sendMessage(ChatColor.GREEN + items);
                     items = "";
                 }
                 tmp = wl.next();
                 items += tmp;
                 i += 1;
             }
             sender.sendMessage(ChatColor.GREEN + items);
             return true;
         }
         if (commandLabel.equalsIgnoreCase("surfprot") || commandLabel.equalsIgnoreCase("sp")) {
             if (sender instanceof Player) {
                 if ((permissions == null && !((Player) sender).isOp()) 
                         || (permissions != null && !permissions.has(((Player) sender), "surfaceprotect.admin"))) {
                     sender.sendMessage(ChatColor.RED+"You do not have access to this command.");
                     return true;
                 }
             }
             int l = args.length;
             if (l == 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r"))) {
                 reloadWorlds();
                 sender.sendMessage(ChatColor.GREEN + "Done.");
             } else if (l >= 1 && (args[0].equalsIgnoreCase("worlds") || args[0].equalsIgnoreCase("w"))) {
                 int page = 0;
                 if (l == 2) {
                     page = toInt(args[1]);
                 }
                 List<World> worlds = getServer().getWorlds();
                 World tmp;
                 String active = "";
                 sender.sendMessage("Worlds:");
                 if (page*10 < worlds.size()) {
                     for (int i = page*10; i < Math.min((page+1)*10, worlds.size()); i++) {
                         tmp = worlds.get(i);
                         WorldSettings set = ws.get(tmp.getName());
                         if (set != null) {
                             if (set.protect) {
                                 active = "Protected";
                             } else {
                                 active = "Not protected";
                             }
                             sender.sendMessage(ChatColor.GREEN + String.format("%s - %s - %d", tmp.getName(), active,set.ylimit));
                         } else {
                             sender.sendMessage(ChatColor.RED + tmp.getName());
                         }
 
                     }
                     if ((page+1)*10 < worlds.size()) {
                         sender.sendMessage(ChatColor.YELLOW+String.format("/surfprot worlds %d for the next page", page+1));
                     }
                 }
             } else if (l >= 1 && (args[0].equalsIgnoreCase("setlimit") || args[0].equalsIgnoreCase("sl"))) {
                 if (l < 3) {
                     sender.sendMessage(ChatColor.RED + "Must provide world and limit!");
                 } else {
                     WorldSettings set = ws.get(args[1]);
                     if (set != null) {
                         set.setYLimit(toInt(args[2]), sender);
                     } else {
                         sender.sendMessage(ChatColor.RED + "No such world: " + args[1]);
                     }
                 }
             } else if (l >= 1 && (args[0].equalsIgnoreCase("setprotect") || args[0].equalsIgnoreCase("sp"))) {
                 if (l == 3) {
                     WorldSettings set = ws.get(args[1]);
                     if (set != null) {
                        set.setProtect(!(args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("f")), sender);
                     } else {
                         sender.sendMessage(ChatColor.RED + "No such world: " + args[1]);
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "Must provide a world and true or false (or t/f)");
                 }
             } else if (l >= 1 && (args[0].equalsIgnoreCase("toggleprotect") || args[0].equalsIgnoreCase("tp"))) {
                 if (l == 2) {
                     WorldSettings set = ws.get(args[1]);
                     if (set != null) {
                         set.toggleProtect(sender);
                     } else {
                         sender.sendMessage(ChatColor.RED + "No such world: " + args[1]);
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "Must provide a world");
                 }
             } else if (l >= 1 && (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("wl"))) {
                 if (l == 1) {
                     sender.sendMessage(ChatColor.RED+"Requires add/del and anumber");
                 } else if (l == 3 && args[1].equalsIgnoreCase("add")) {
                     Integer item = toInt(args[2]);
                     if (item.equals(0)) {
                         sender.sendMessage(ChatColor.RED + "Invalid item-ID (use integer)!");
                     } else {
                         allow.add(item);
                         sender.sendMessage(ChatColor.GREEN + String.format("%d has been added to the whitelist.",item));
                         conf.setProperty("allowed."+item, null);
                         conf.save();
                     }
                 } else if (l == 3 && args[1].equalsIgnoreCase("del")) {
                     Integer item = toInt(args[2]);
                     if (item.equals(0)) {
                         sender.sendMessage(ChatColor.RED + "Invalid item-ID (use integer)!");
                     } else {
                         if (allow.contains(item)) {
                             allow.remove(item);
                             sender.sendMessage(ChatColor.GREEN + String.format("%d has been removed from the whitelist.",item));
                             if (conf.getKeys("allowed.").contains(item.toString())) {
                                 conf.removeProperty("allowed."+item);
                             }
                             conf.save();
                         } else {
                             sender.sendMessage(ChatColor.RED + String.format("%d is not on whitelist.", item));
                         }
                     }
                 } else {
                     help = true;
                 }
             } else {
                 help = true;
             }
         }
         if (help) {
             sender.sendMessage(ChatColor.GREEN+"SurfaceProtect " + pdfFile.getVersion() + " by TZer0");
             sender.sendMessage(ChatColor.YELLOW+"Help (commands start with /surfprot or /sp):");
             sender.sendMessage(ChatColor.YELLOW+"() denote aliases");
             sender.sendMessage(ChatColor.YELLOW+"(r)eload - reloads the world-settings");
             sender.sendMessage(ChatColor.YELLOW+"(w)orlds - shows a list of worlds and their statuses");
             sender.sendMessage(ChatColor.YELLOW+"(s)et(l)imit world ylimit - ");
             sender.sendMessage(ChatColor.YELLOW+"adjusts how high you're allowed to mine");
             sender.sendMessage(ChatColor.YELLOW+"(s)et(p)protect world (t)rue/(f)alse - ");
             sender.sendMessage(ChatColor.YELLOW+"adjust whether a world is protected");
             sender.sendMessage(ChatColor.YELLOW+"(t)oggle(p)rotect world - adjust protection");
             sender.sendMessage(ChatColor.YELLOW+"(w)hite(l)ist add itemid - add an item to the whitelist");
             sender.sendMessage(ChatColor.YELLOW+"(w)hite(l)ist add itemid - remove an item from the whitelist");
             sender.sendMessage(ChatColor.YELLOW+"Without /sp or /surfprot:");
             sender.sendMessage(ChatColor.YELLOW+"/(a)llowed(i)tems - for players: shows what items are allowed");
         }
         return true;
     }
 
     public void reloadWorlds() {
         ws = new HashMap<String, WorldSettings>();
         for (World tmp : getServer().getWorlds()) {
             ws.put(tmp.getName().toLowerCase(), new WorldSettings(tmp.getName().toLowerCase()));
         } 
     }
     /**
      * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
      */
     private void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (this.permissions == null) {
             if (test != null) {
                 this.permissions = ((Permissions) test).getHandler();
             } else {
                 System.out.println(ChatColor.YELLOW
                         + "Permissons not detected - defaulting to OP!");
             }
         }
     }
     class WorldSettings {
         String world;
         boolean protect;
         int ylimit;
         public WorldSettings(String tmp) {
             world = tmp;
             protect = conf.getBoolean("worlds."+tmp+".protect", false);
             ylimit = conf.getInt("worlds."+tmp+".ylimit", 62);
         }
         public void toggleProtect(CommandSender sender) {
             protect = !protect;
             announceandsave(sender);
         }
         public void setProtect(boolean newstatus, CommandSender sender) {
             protect = newstatus;
             announceandsave(sender);
         }
         public void announceandsave(CommandSender sender) {
             conf.setProperty("worlds."+world+".protect", protect);
             conf.save();
             String stat = "";
             if (protect) {
                 stat = "denied";
             } else {
                 stat = "allowed";
             }
             sender.sendMessage(ChatColor.GREEN + String.format("Modifying blocks above the surface in %s is now %s", world, stat));
         }
         public void setYLimit(int limit, CommandSender sender) {
             if (limit < 0 || limit > 127) {
                 sender.sendMessage(ChatColor.RED + "Invalid input");
             } else {
                 conf.setProperty("worlds."+world+".ylimit", limit);
                 ylimit = limit;
                 sender.sendMessage(ChatColor.GREEN + String.format("Limit for %s set to %d", world, limit));
                 conf.save();
             }
 
         }
     }
     /**
      * Converts to int if valid, if not: returns 0
      * @param in
      * @param sender
      * @return
      */
     public int toInt(String in) {
         int out = 0;
         if (checkInt(in)) {
             out = Integer.parseInt(in);
         }
         return out;
     }
     /**
      * Checks if a string is valid as a representation of an unsigned int.
      */
     public boolean checkInt(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             if (!Character.isDigit(chars[i])) {
                 return false;
             }
         }
         return true;
     }
 }
