 package com.sparkedia.valrix.ColorMe;
 
 import java.io.File;
 import java.util.LinkedHashMap;
 //import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 //import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.craftbukkit.TextWrapper;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.iConomy.iConomy;
 import com.iConomy.system.Account;
 
 public class ColorMe extends JavaPlugin {
     public Logger log;
     //public Map<World, Property> colors;
     public Property colors;
     public String pName;
     public String df;
     public String uf;
     private Property conf;
     public iConomy iconomy = null;
     
     private void mkDir(String...d) {
         for (String f : d) new File(f).mkdir();
     }
     /* TODO: Implement when events can be unregistered.
     private void reload() {
         getServer().getPluginManager().disablePlugin(this);
         getServer().getPluginManager().enablePlugin(this);
     }
     
     private boolean isAdmin(Player p) {
         if (p.getName().equalsIgnoreCase(conf.getString("admin"))) return true;
         return false;
     }*/
     
     private boolean self(Player p, String n) {
         return (p.equals(getServer().getPlayer(n))) ? true : false;
     }
     
     private void list(Player p) {
         p.sendMessage("Color List:");
         String color;
         String msg = "";
         for (int i = 0; i < ChatColor.values().length; i++) {
             color = ChatColor.getByCode(i).name();
             if (msg.length() == 0) {
                 msg = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
                 continue;
             }
             msg += (i == ChatColor.values().length-1) ? ChatColor.valueOf(color)+color.toLowerCase().replace("_", "") : ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
             TextWrapper.wrapText(msg);
         }
         p.sendMessage(msg);
     }
     
     @Override
     public void onDisable() {
         // Clean up the garbage before disable
         colors.clear();
         colors = null;
         conf.clear();
         conf = null;
         log = null;
         pName = null;
         df = null;
         uf = null;
         iconomy = null;
 
        log.info('['+pName+"] has been disabled.");
     }
     
     @Override
     public void onEnable() {
         log = getServer().getLogger();
         pName = getDescription().getName();
         
         df = getDataFolder().toString();
         uf = df+"/../"+getServer().getUpdateFolder();
         mkDir(df, uf);
         
         /* Load each world's color list
         for (World w : getServer().getWorlds()) {
             Property worldFile = new Property(df+'/'+w.getName()+".color", this);
             colors.put(w, worldFile);
         }
         */
         colors = new Property(df+'/'+"players.color", this);
         colors.save();
         
         // Does the config exist, if not then make a new blank one
         if (!(new File(df+"/config.txt").exists())) {
             conf = new Property(df+"/config.txt", this);
             conf.setString("admin", "");
             conf.setNumber("cost", 0);
             conf.save();
         } else {
             conf = new Property(df+"/config.txt", this);
             // Check if they have the updated prefix property file, otherwise update it to new format
             if (!getDescription().getVersion().equalsIgnoreCase(conf.getString(pName+"Version"))) {
                 LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>();
                 conf.remove(pName+"Version");
                 tmp.put("admin", conf.getString("admin"));
                 for (String key : conf.getKeys())
                     tmp.put(key, conf.getString(key));
                 conf.rebuild(tmp);
             }
         }
         getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, new ColorPlayerListener(this), Priority.Lowest, this);
         
         log.info('['+pName+"] v"+getDescription().getVersion()+" has been enabled.");
         
         // /color <color/name> [name] (name is optional since you can color your own name)
         getCommand("colorme").setExecutor(new CommandExecutor() {
             public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
                 if (sender instanceof Player && args.length>=1) {
                     Player player = ((Player)sender);
                     String a0 = args[0];
                     switch(args.length) {
                         case 1:
                             /* TODO: Add when possible to unregister events
                                 if (a0.equalsIgnoreCase("reload") && player.hasPermission("colorme.reload")) {
                                     reload();
                                     return true;
                                 }
                              */
                             if (a0.equalsIgnoreCase("list") && player.hasPermission("colorme.list")) {
                                 list(player);
                                 return true;
                             }
                             if (hasColor(a0) && (player.hasPermission("colorme.remove") || self(player, a0))) {
                                 removeColor(a0);
                                 if (getServer().getPlayer(a0) != null) {
                                     // Update displayname
                                     Player other = getServer().getPlayer(a0);
                                     other.setDisplayName(ChatColor.stripColor(other.getDisplayName()));
                                     other.sendMessage(ChatColor.GREEN+"Your name color has been removed.");
                                     if (other != sender) sender.sendMessage(ChatColor.GREEN+"Removed "+other.getName()+"'s color.");
                                     return true;
                                 }
                                 sender.sendMessage((self(player, a0)) ? ChatColor.GREEN+"Removed your color." : ChatColor.GREEN+"Removed color from"+a0+'.');
                                 return true;
                             }
                             if (!hasColor(a0) && colors.keyExists(a0)) return true; // Trying to remove a color from a color-less player
                             if (player.hasPermission("colorme.self")) {
                                 String color = findColor(a0);
                                 if (color.equals(a0)) {
                                     player.sendMessage(ChatColor.GREEN+"'"+a0+"' is not a supported color.");
                                     return true;
                                 }
                                 if (iconomy != null) {
                                     double cost = conf.getDouble("cost");
                                     Account acct = iConomy.getAccount(player.getName());
                                     if (cost>0 && acct.getHoldings().hasEnough(cost)) {
                                         acct.getHoldings().subtract(cost);
                                         setColor(player.getName(), a0);
                                         player.sendMessage(ChatColor.GREEN+"You have been charged "+ChatColor.RED+iConomy.format(cost)+ChatColor.GREEN+'.');
                                         player.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
                                     } else if (cost>0 && !acct.getHoldings().hasEnough(cost)) {
                                         player.sendMessage(ChatColor.GREEN+"It costs "+ChatColor.GREEN+iConomy.format(cost)+ChatColor.GREEN+" to color your name.");
                                     }
                                 } else {
                                     setColor(player.getName(), a0);
                                     player.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
                                     player.sendMessage(ChatColor.GREEN+"Your name color is now: "+ChatColor.valueOf(color)+a0);
                                 }
                                 return true;
                             } else if (!player.hasPermission("colorme.self")) {
                                 player.sendMessage(ChatColor.GREEN+"You don't have permission to color your own name.");
                                 return true;
                             }
                             break;
                         case 2:
                             String a1 = args[1];
                             String color = findColor(a1);
                             if (color.equals(a1)) {
                                 player.sendMessage(ChatColor.GREEN+"'"+a1+"' is not a supported color.");
                                 return true;
                             }
                             if (self(player, a0) && player.hasPermission("colorme.self")) {
                                 // Coloring self
                                 if (iconomy != null) {
                                     // iConomy enabled
                                     double cost = conf.getDouble("cost");
                                     Account acct = iConomy.getAccount(player.getName());
                                     if (cost>0 && acct.getHoldings().hasEnough(cost)) {
                                         // Player can afford to color their name
                                         acct.getHoldings().subtract(cost);
                                         setColor(player.getName(), a1);
                                         player.sendMessage(ChatColor.GREEN+"You have been charged "+ChatColor.RED+iConomy.format(cost)+ChatColor.GREEN+'.');
                                         player.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
                                     } else if (cost>0 && !acct.getHoldings().hasEnough(cost)) {
                                         // Player can't afford to color their name
                                         player.sendMessage(ChatColor.GREEN+"It costs "+ChatColor.RED+iConomy.format(cost)+ChatColor.GREEN+" to color your name.");
                                     } else if (0 == cost) {
                                         // No cost, color own name
                                         setColor(player.getName(), a1);
                                         player.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
                                         player.sendMessage(ChatColor.GREEN+"Changed your name's color to: "+ChatColor.valueOf(color)+a1);
                                     }
                                     return true;
                                 } else {
                                     // iConomy NOT enabled
                                     setColor(player.getName(), a1);
                                     player.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
                                     player.sendMessage(ChatColor.GREEN+"Changed your name's color to: "+ChatColor.valueOf(color)+a1);
                                     return true;
                                 }
                             } else if (!self(player, a0) && player.hasPermission("colorme.other")) {
                                 // Coloring someone else
                                 setColor(a0, a1);
                                 if (getServer().getPlayer(a0) != null) {
                                     Player other = getServer().getPlayer(a0);
                                     other.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(player.getDisplayName())+ChatColor.WHITE);
                                     player.sendMessage(ChatColor.GREEN+"Changed "+other.getName()+"'s color to: "+ChatColor.valueOf(color)+a1);
                                     return true;
                                 }
                                 player.sendMessage(ChatColor.GREEN+"Changed "+a0+"'s color to: "+ChatColor.valueOf(color)+a1);
                                 return true;
                             } else {
                                 player.sendMessage(ChatColor.GREEN+"You don't have permission to color "+(self(player, a0) ? "your own" : "another player's")+" name.");
                                 return true;
                             }
                         default: return false;
                     }
                 } else if (sender instanceof ConsoleCommandSender && args.length>=1) {
                     String a0 = args[0];
                     switch (args.length) {
                         case 1:
                             /* TODO: Add when possible
                             if (a0.equalsIgnoreCase("reload")) {
                                 reload();
                                 return true;
                             }
                              */
                             if (a0.equalsIgnoreCase("list")) {
                                 sender.sendMessage("Color List:");
                                 String color;
                                 String msg = "";
                                 for (int i = 0; i < ChatColor.values().length; i++) {
                                     color = ChatColor.getByCode(i).name();
                                     if (msg.length() == 0) {
                                         msg = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
                                         continue;
                                     }
                                     msg += (i == ChatColor.values().length-1) ? ChatColor.valueOf(color)+color.toLowerCase().replace("_", "") : ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
                                     TextWrapper.wrapText(msg);
                                 }
                                 sender.sendMessage(msg);
                                 return true;
                             }
                             if (hasColor(a0)) {
                                 // Player has color, remove it
                                 removeColor(a0);
                                 if (getServer().getPlayer(a0) != null) {
                                     // Player is online, update displayname
                                     Player other = getServer().getPlayer(a0);
                                     other.setDisplayName(ChatColor.stripColor(other.getDisplayName()));
                                     other.sendMessage("Your name color has been removed.");
                                     sender.sendMessage("Removed "+other.getName()+"'s color.");
                                     return true;
                                 }
                                 sender.sendMessage("Removed color from "+a0);
                                 return true;
                             }
                             sender.sendMessage(a0+" doesn't have a colored name.");
                             return true;
                         case 2:
                             String a1 = args[1];
                             if (setColor(a0, a1)) {
                                 String color = findColor(a1);
                                 if (getServer().getPlayer(a0) != null) {
                                     // Player is online, change their displayname immediately
                                     Player other = getServer().getPlayer(a0);
                                     other.setDisplayName(ChatColor.valueOf(color)+ChatColor.stripColor(other.getDisplayName())+ChatColor.WHITE);
                                     other.sendMessage("Your name color has been changed to "+a1);
                                     sender.sendMessage("Changed "+other.getName()+"'s color to: "+a1);
                                     return true;
                                 }
                                 sender.sendMessage("Changed "+a0+"'s color to: "+a1);
                                 return true;
                             } else {
                                 sender.sendMessage("'"+a1+"' is not a supported color.");
                                 return false;
                             }
                         default: return false;
                     }
                 }
                 return false;
             }
         });
     }
     
     // Return the player's name color
     public String getColor(String name) {
         return (colors.keyExists(name.toLowerCase())) ? colors.getString(name.toLowerCase()) : "";
     }
     
     // Set player's color and update displayname if online
     public boolean setColor(String name, String color) {
         String newColor = findColor(color); 
         if (newColor.equals(color)) return false;
         colors.setString(name.toLowerCase(), newColor);
         colors.save();
         if (getServer().getPlayer(name) != null) {
             Player p = getServer().getPlayer(name);
             p.setDisplayName(ChatColor.valueOf(newColor)+ChatColor.stripColor(p.getDisplayName())+ChatColor.WHITE);
         }
         return true;
     }
     
     // Iterate through colors to try and find a match (resource expensive)
     public String findColor(String color) {
         String col;
         for (int i = 0; i <= 15; i++) {
             col = ChatColor.getByCode(i).name();
             if (color.equalsIgnoreCase(col.toLowerCase().replace("_", ""))) return col;
         }
         return color;
     }
     
     // Check if a player has a color or not
     public boolean hasColor(String name) {
        return (colors.keyExists(name) && colors.getString(name.toLowerCase()).trim().length()>0) ? true : false;
     }
     
     // Removes a color if exists, otherwise returns false
     public boolean removeColor(String name) {
         name = name.toLowerCase();
        if (colors.keyExists(name)) {
             colors.setString(name, "");
             colors.save();
             return true;
         }
         return false;
     }
 }
