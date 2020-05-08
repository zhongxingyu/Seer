 package tzer0.Money2XP;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
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
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.gmail.nossr50.mcMMO;
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Account;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import cosine.boseconomy.BOSEconomy;
 
 /**
  * Plugin for bukkit allowing players to purchase mcMMO-experience points using iConomy/BOSEconomy-money.
  * 
  * @author TZer0
  */
 public class Money2XP extends JavaPlugin {
     private final Money2XPPlayerListener listener = new Money2XPPlayerListener();
 
     String []names = {"acrobatics", "archery", "axes", "excavation", "herbalism", "mining", "repair", "swords", "unarmed", "woodcutting"};
     PluginDescriptionFile pdfFile;
     public PermissionHandler permissions;
     public LinkedHashSet<String> skillnames = new LinkedHashSet<String>();
     public LinkedHashSet<TrainingArea> areas;
     HashMap<Player, AreaSelect> as;
     HashMap<CommandSender, TrainingArea> selected;
     Configuration conf;
     public mcMMO mcmmo;
     public boolean trainingzones;
     @SuppressWarnings("unused")
     private final String name = "Money2XP";
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onDisable()
      */
     public void onDisable() {
         System.out.println(pdfFile.getName() + " disabled.");
         getServer().getScheduler().cancelTasks(this);
     }
 
     /* (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onEnable()
      */
     public void onEnable() {
         selected = new HashMap<CommandSender, TrainingArea>();
         as = new HashMap<Player, AreaSelect>();
         pdfFile = this.getDescription();
         if (skillnames.size() == 0) {
             for (String name : names) {
                 skillnames.add(name);
             }
         }
         setupPermissions();
         conf = getConfiguration();
         listener.setPointers(conf, this, permissions);
         trainingzones = conf.getBoolean("zones", false);
         PluginManager tmp = getServer().getPluginManager();
         tmp.registerEvent(Event.Type.PLAYER_JOIN, listener, Priority.Normal, this);
         tmp.registerEvent(Event.Type.PLAYER_QUIT, listener, Priority.Normal, this);
 
         System.out.println(pdfFile.getName() + " version "
                 + pdfFile.getVersion() + " is enabled!");
         mcmmo = (mcMMO) tmp.getPlugin("mcMMO");
         reloadAreas(null);
     }
     /**
      * Getting areas from config.
      */
     public void reloadAreas(CommandSender sender) {
         areas = new LinkedHashSet<TrainingArea>();
         int xf,yf,zf,xt,yt,zt;
         Map<String, ConfigurationNode> nodes = conf.getNodes("areas.");
         for (String area : nodes.keySet()) {
             xf = conf.getInt("areas."+area+".xf", 0);
             yf = conf.getInt("areas."+area+".yf", 0);
             zf = conf.getInt("areas."+area+".zf", 0);
             xt = conf.getInt("areas."+area+".xt", 0);
             yt = conf.getInt("areas."+area+".yt", -1);
             zt = conf.getInt("areas."+area+".zt", 0);
             if (yt == -1) {
                 if (sender != null) {
                     sender.sendMessage(ChatColor.RED + String.format("%s is invalid, please resize", area));
                 }
             }
             AreaSelect tmp = new AreaSelect();
             tmp.b1 = getServer().getWorld(conf.getString("areas."+area+".world", "world")).getBlockAt(xf, yf, zf);
             tmp.b2 = getServer().getWorld(conf.getString("areas."+area+".world", "world")).getBlockAt(xt, yt, zt);
             TrainingArea out = new TrainingArea(tmp, null, area);
             for (String skill : conf.getStringList("areas."+area+".skills", null)) {
                 out.modSkill(skill, true, null);
             }
             areas.add(out);
         }
     }
     /* (non-Javadoc)
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
         }
         int l = args.length;
         boolean help = false;
         if (player != null) {
             if (commandLabel.equalsIgnoreCase("m2x")) {
                 // user-accessible commands
                 if (!(permissions == null || (permissions != null && permissions.has(player, "money2xp.user")))) {
                     player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                     return true;
                 }
                 if (l >= 1 && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l"))) {
                     // lists available skills
                     showSkills((CommandSender)player);
                 } else if (l == 3 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("c"))) {
                     // checks if the skill exists and whether the player has enough money to purchase the required
                     // amount of xp.
                     xpMod(args[1], args[2], player, true, false);
                 } else if (l == 2) {
                     xpMod(args[0], args[1], player, false, false);
                 } else if (l == 3) {
                     xpMod(args[0], args[1], player, false, args[2].equalsIgnoreCase("o"));
                 } else {
                     help = true;
                 }
             } else if (commandLabel.equalsIgnoreCase("m2xset") || commandLabel.equalsIgnoreCase("m2xs")) {
                 if (!((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.admin")))) {
                     player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                     return true;
                 }
                 if (l == 2) {
                     if (!modValue(args[0], args[1], player)) {
                         help = true;
                     }
                 } else {
                     help = true;
                 }
             } else if (commandLabel.equalsIgnoreCase("m2xtrain") || commandLabel.equalsIgnoreCase("m2xt")) {
                 if (!((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.admin")))) {
                     player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                     return true;
                 }
                 if (l >= 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r"))) {
                     reloadAreas(sender);
                     sender.sendMessage(ChatColor.GREEN + "Done.");
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("zones") || args[0].equalsIgnoreCase("z"))) {
                     String status = "";
                     if (l == 2) {
                         trainingzones = args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("t");
                     } else {
                         trainingzones = !trainingzones;
                     }
                     if (trainingzones) {
                         status = "on";
                     } else {
                         status = "off";
                     }
                     conf.setProperty("zones", trainingzones);
                     conf.save();
                     player.sendMessage(ChatColor.GREEN + "Training zones have been turned " + status);
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("c"))) {
                     AreaSelect area = as.get(player);
                     if (area == null) {
                         player.sendMessage(ChatColor.RED + "No area selected.");
                         return true;
                     } else if (area.b1 == null) {
                         player.sendMessage(ChatColor.RED + "First node is missing");
                         return true;
                     } else if (area.b2 == null) {
                         player.sendMessage(ChatColor.RED + "Second node is missing");
                         return true;
                     }
                     if (l == 2) {
                         if (area.b1.getWorld().equals(area.b2.getWorld())) {
                             TrainingArea ta = new TrainingArea(area, player, args[1]);
                             areas.add(ta);
                         } else {
                             player.sendMessage(ChatColor.RED + "Nodes are in different worlds.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "Requires a name.");
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("delarea") || args[0].equalsIgnoreCase("da"))) {
                     if (l == 2) {
                         for (TrainingArea area : areas) {
                             if (area.areaname.equalsIgnoreCase(args[1])) {
                                 areas.remove(area);
                                 if (selected.get((CommandSender)player) == area) {
                                     selected.put((CommandSender) player, null); 
                                 }
                                 conf.removeProperty("areas."+area.areaname);
                                 conf.save();
                                 break;
                             }
                         }
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("node") || args[0].equalsIgnoreCase("n"))) {
                     AreaSelect area = as.get(player);
                     if (area == null) {
                         area = new AreaSelect();
                         as.put(player, area);
                     }
                     if (l == 2) {
                         int i = toInt(args[1]);
                         if (i == 1) {
                             area.b1 = player.getLocation().getBlock();
                             player.sendMessage("Node 1 set");
                         } else if (i == 2) {
                             area.b2 = player.getLocation().getBlock();
                             player.sendMessage("Node 2 set");
                         } else {
                             player.sendMessage("No such node.");
                         }
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("s"))) {
                     if (l == 2) {
                         for (TrainingArea area : areas) {
                             if (area.areaname.equalsIgnoreCase(args[1])) {
                                 sender.sendMessage(ChatColor.GREEN+String.format("Selected %s", args[1]));
                                 selected.put(sender, area);
                                 return true;
                             }
                         }
                         sender.sendMessage(ChatColor.RED+"No training area found.");
                     } else {
                         sender.sendMessage(ChatColor.RED+"Requires area-name");
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("resize") || args[0].equalsIgnoreCase("rs"))) {
                     TrainingArea tarea = selected.get(sender);
                     if (tarea == null) {
                         player.sendMessage(ChatColor.RED + "No training area selected!");
                         return true;
                     }
                     AreaSelect area = as.get(player);
                     if (area == null) {
                         player.sendMessage(ChatColor.RED + "No area selected.");
                         return true;
                     } else if (area.b1 == null) {
                         player.sendMessage(ChatColor.RED + "First node is missing");
                         return true;
                     } else if (area.b2 == null) {
                         player.sendMessage(ChatColor.RED + "Second node is missing");
                         return true;
                     }
                     tarea.resize(area, player);
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("area") || args[0].equalsIgnoreCase("a"))) {
                     TrainingArea area = selected.get(sender);
                     if (area != null) {
                         sender.sendMessage(ChatColor.GREEN + String.format("Area %s @ %d.%d.%d, %d.%d.%d in %s", 
                                 area.areaname, area.xf, area.yf, area.zf, area.xt, area.yt, area.zt, area.world.getName()));
                         sender.sendMessage(ChatColor.GREEN + "Available skills:");
                         for (Integer i : area.skills) {
                             sender.sendMessage(ChatColor.GREEN + names[i]);
                         }
                     } else {
                         sender.sendMessage(ChatColor.YELLOW+"No area selected");
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("ad"))) {
                     TrainingArea area = selected.get(sender);
                     if (area != null && l == 2) {
                         area.modSkill(args[1], true, sender);
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("d"))) {
                     TrainingArea area = selected.get(sender);
                     if (area != null && l == 2) {
                         area.modSkill(args[1], false, sender);
                     }
                 } else if (l >= 1 && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l"))) {
                     int page = 0;
                     if (l == 2) {
                         page = toInt(args[1]);
                     }
                     int i = page*10;
                     boolean first = true;
                     Iterator<TrainingArea> iter = areas.iterator();
                     sender.sendMessage(ChatColor.GREEN + String.format(ChatColor.GREEN + "Showing %d to %d", 
                             page*10, Math.min((page+1)*10, areas.size())));
                     if (i == 0) {
                         i = 10;
                         first = false;
                     }
                     while (i > 0 && iter.hasNext()) {
                         TrainingArea tmp = iter.next();
                         i--;
                         if (i <= 0 && first) {
                             i = 10;
                         }
                         if (!first) {
                             sender.sendMessage(ChatColor.YELLOW + String.format("%s @ %d.%d.%d to %d.%d.%d in %s", 
                                     tmp.areaname, tmp.xf, tmp.yf, tmp.zf, tmp.xt, tmp.yt, tmp.zt, tmp.world.getName()));
                         }
                     }
                     if ((page+1)*10 < areas.size()) {
                         sender.sendMessage(ChatColor.GREEN + String.format("/m2xtrain l %d for the next page", page+1));
                     }
                 } else {
                     help = true;
                 }
             }
         } else {
             sender.sendMessage("Only in-game players have access to this command");
         }
         if (help) {
             player.sendMessage(ChatColor.GREEN + "Money2XP by TZer0 (TZer0.jan@gmail.com)");
             player.sendMessage(ChatColor.YELLOW + "Commands (paranthesis denote aliases, braces optional args):");
             player.sendMessage(ChatColor.YELLOW + "/m2x (l)ist - list skills and prices");
             player.sendMessage(ChatColor.YELLOW + "/m2x [skillname] [amount]");
             player.sendMessage(ChatColor.YELLOW + "/m2x (c)heck [skillname] [amount]");
             if ((permissions == null && player.isOp()) || permissions.has(player, "money2xp.admin")) {
                 player.sendMessage(ChatColor.YELLOW + "Admin commands:");
                 player.sendMessage(ChatColor.YELLOW + "/m2xset [skillname] [price_per_xp] - sets xp-cost for a skill");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (z)ones {t/true/f/false} - turn training zones on/off");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (n)ode [1/2] - sets nodes for area-creation");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (c)reate [name] - creates a zone using nodes.");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (l)ist {#} - shows training zones");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (s)elect [name] - select an area");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (a)rea - shows information about the selected area");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (r)e(s)ize - resize selected area");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (ad)d [skillname] - adds a skill to sel. area");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (d)el [skillname] - removes a skill from sel. area");
                player.sendMessage(ChatColor.YELLOW + "/m2xtrain (d)elete(a)rea name - deletes an area");
                 player.sendMessage(ChatColor.YELLOW + "/m2xtrain (r)eload - reload config, reports errors");
                 player.sendMessage(ChatColor.YELLOW + "You may use /m2xt and /m2xs too.");
             }
         }
         return true;
     }
 
     /**
      * Modifies a skill if test is false, if test is true, displays cost of modification
      * 
      * @param skill The skill to be modified
      * @param xpstring How much xp should be added
      * @param player Player receving the xp and taking the costs
      * @param test If this is a test or not.
      */
 
     public void xpMod(String skill, String xpstring, Player player, boolean test, boolean override) {
         Plugin BOS = getServer().getPluginManager().getPlugin("BOSEconomy");
         skill = skill.toLowerCase();
         if (checkInt(xpstring)) {
             if (!skillnames.contains(skill)) {
                 player.sendMessage(ChatColor.RED+"This skill does not exist!");
                 return;
             }
             if (override && !((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.ignorezones")))) {
                 player.sendMessage(ChatColor.RED+"You are not allowed to override training zones!");
             }
             if (!test && !override && trainingzones) {
                 TrainingArea area = listener.activity.get(player);
                 boolean failed = false;
                 if (area == null) {
                     player.sendMessage(ChatColor.RED+"You are not in a training zone!");
                     failed = true;
                 } else if (!area.skills.contains(getSkillIndex(skill))) {
                     player.sendMessage(ChatColor.RED+"This skill is not available here, find a training region");
                     failed = true;
                 }
                 if (failed) {
                     if (((permissions == null && player.isOp()) || (permissions != null && permissions.has(player, "money2xp.ignorezones")))) {
                         player.sendMessage(ChatColor.GREEN + "However, you may override this using the following command:");
                         player.sendMessage(ChatColor.GREEN + String.format("/m2x %s %s o", skill, xpstring));
                     }
                     return;
                 }
             }
             int xp = Integer.parseInt(xpstring);
             int xpcost = conf.getInt(skill.toLowerCase(), conf.getInt("default", 100));
             if (xpcost <= 0) {
                 player.sendMessage(String.format(ChatColor.RED + "Training %s using money has been disabled.", skill));
                 return;
             } else if (Integer.MAX_VALUE/xpcost < xp || xp <= 0) {
                 // Prevents overflows and underflows and adds a disapproving comment.
                 player.sendMessage(ChatColor.RED+String.format("Nice try."));
                 return;
             }
             int bal = 0;
             if (getServer().getPluginManager().isPluginEnabled("iConomy")) {
                 Account acc = iConomy.getBank().getAccount(player.getName());
                 bal = (int)acc.getBalance();
             } else if (BOS != null) {
                 bal = ((BOSEconomy) BOS).getPlayerMoney(player.getName());
             } else {
                 player.sendMessage(ChatColor.RED + "No economy system disabled, cancelled.");
                 return;
             }
 
             if (!test && bal < xp*xpcost) {
                 player.sendMessage(ChatColor.RED+String.format("You cannot afford %d %s xp (@%d) since ", 
                         xp, skill, xpcost));
                 player.sendMessage(ChatColor.RED+String.format("your current balance is %d, and this costs %d!", 
                         bal, xpcost*xp));
             } else if (test) {
                 player.sendMessage(ChatColor.YELLOW+String.format("%d %s-xp (@%d) would cost you %d,", 
                         xp, skill, xpcost, xp*xpcost));
                 player.sendMessage(ChatColor.YELLOW+String.format("leaving you with %d money.",
                         ((int) bal)-xp*xpcost));
             } else {
                 if (getServer().getPluginManager().isPluginEnabled("iConomy")) {
                     iConomy.getBank().getAccount(player.getName()).subtract(xp*xpcost);
                 } else {
                     ((BOSEconomy) BOS).addPlayerMoney(player.getName(), -xp*xpcost, true);
                 }
                 bal -= xp*xpcost;
                 player.sendMessage(ChatColor.GREEN+String.format("You received %d %s-xp(@%d) for %d money!", 
                         xp, skill, xpcost, xp*xpcost));
                 player.sendMessage(ChatColor.GREEN+String.format("You have %d money left", 
                         (int) bal));
                 mcmmo.addXp(player, skill, xp);
             }
         } else {
             player.sendMessage(ChatColor.RED + "Value must be a number.");
         }
     }
     /**
      * Modifies a settings-value.
      *
      * @param key The key to modify or delete
      * @param value The value which will be set
      * @param sender Whoever is sending doing this command 
      * @return true, Whether it worked or not.
      */
     public boolean modValue(String key, String value, CommandSender sender) {
         if (checkInt(value)) {
             if (!(key.equals("default") || skillnames.contains(key))) {
                 sender.sendMessage(ChatColor.RED + "This skill does not exist.");
                 return true;
             }
             int i = Integer.parseInt(value);
             if (i == 0) {
                 getConfiguration().removeProperty(key);
                 value = String.format("default(%d)",
                         getConfiguration().getInt("default", 100));
             } else {
                 if (i == -1) {
                     value = "unavailable";
                 }
                 getConfiguration().setProperty(key.toLowerCase(), i);
             }
             getConfiguration().save();
             sender.sendMessage(ChatColor.GREEN
                     + String.format("Price per xp for %s has been set to %s",
                             key, value));
             return true;
         } else {
             return false;
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
     /**
      * Displays a page containing price-information about xp
      * 
      * @param page The page to display
      * @param player
      */
     public void showSkills(CommandSender sender) {
         int def = getConfiguration().getInt("default", 100);
         int value = 0;
         sender.sendMessage(ChatColor.WHITE + "Name - Cost");
         for (String name : skillnames) {
             value = getConfiguration().getInt(name, def);
             if (value > 0) {
                 sender.sendMessage(ChatColor.GREEN + String.format("%s - %d", name, value));
             } else {
                 sender.sendMessage(ChatColor.RED + String.format("%s is not available", name));
             }
         }
     }
     public int toInt(String in) {
         int out = 0;
         if (checkInt(in)) {
             out = Integer.parseInt(in);
         }
         return out;
     }
     /**
      * Check if the string is valid as an int (accepts signs).
      *
      * @param in The string to be checked
      * @return boolean Success
      */
     public boolean checkInt(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             if (!(Character.isDigit(chars[i]) || (i == 0 && chars[i] == '-'))) {
                 return false;
             }
         }
         return true;
     }
     /**
      * Translate the string to an int
      * @param skill
      * @return
      */
     public int getSkillIndex(String skill) {
         for (int i = 0; i < names.length; i++) {
             if (skill.equalsIgnoreCase(names[i])) {
                 return i;
             }
         }
         return -1;
     }
 
     class AreaSelect {
         Block b1;
         Block b2;
         public AreaSelect() {
             b1 = null;
             b2 = null;
         }
     }
     class TrainingArea {
         LinkedList<Integer> skills;
         int xf, yf, zf, xt, yt, zt;
         String areaname;
         World world;
         public TrainingArea(AreaSelect area, Player player, String areaname) {
             this.areaname = areaname;
             setBounds(area);
             if (player != null) {
                 player.sendMessage(ChatColor.GREEN + String.format("Area %s has been created.", areaname));
             }
             skills = new LinkedList<Integer>();
         }
         public void resize(AreaSelect area, Player player) {
             setBounds(area);
             if (player != null) {
                 player.sendMessage(ChatColor.GREEN + "Resized/moved.");
             }
         }
         public void modSkill(String skill, boolean add, CommandSender sender) {
             Integer i = getSkillIndex(skill);
             if (i != -1) {
                 if (add) {
                     if (!skills.contains(i)) {
                         skills.add(i);
                         if (sender != null) {
                             sender.sendMessage(ChatColor.GREEN + String.format("%s has been added to %s", skill.toLowerCase(), areaname));
                         }
                     } else {
                         if (sender != null) {
                             sender.sendMessage(ChatColor.RED + String.format("Skill already exists. in %s!", areaname));
                         }
                     }
                     
                 } else {
                     if (skills.contains(i)) {
                         skills.remove(i);
                         if (sender != null) {
                             sender.sendMessage(ChatColor.GREEN + "Skill has been removed.");
                         }
                     } else {
                         if (sender != null) {
                             sender.sendMessage(ChatColor.RED + "Skill does not exist in area.");
                         }
                     }
                 }
             } else {
                 if (sender != null) {
                     sender.sendMessage(ChatColor.RED + "No such skill.");
                 }
             }
             String[] tmp = new String[skills.size()];
             int k = 0;
             for (Integer j : skills) {
                 tmp[k] = names[j];
                 k++;
             }
             conf.setProperty(String.format("areas."+areaname+".skills"), tmp);
             conf.save();
         }
         public void setBounds(AreaSelect area) {
             conf.setProperty(String.format("areas.%s.world", areaname), area.b1.getWorld().getName());
             world = area.b1.getWorld();
             xf = Math.min(area.b1.getX(), area.b2.getX())-1;
             xt = Math.max(area.b1.getX(), area.b2.getX())+1;
             yf = Math.min(area.b1.getY(), area.b2.getY())-1;
             yt = Math.max(area.b1.getY(), area.b2.getY())+1;
             zf = Math.min(area.b1.getZ(), area.b2.getZ())-1;
             zt = Math.max(area.b1.getZ(), area.b2.getZ())+1;
             conf.setProperty(String.format("areas.%s.xf", areaname), xf+1);
             conf.setProperty(String.format("areas.%s.xt", areaname), xt-1);
             conf.setProperty(String.format("areas.%s.yf", areaname), yf+1);
             conf.setProperty(String.format("areas.%s.yt", areaname), yt-1);
             conf.setProperty(String.format("areas.%s.zf", areaname), zf+1);
             conf.setProperty(String.format("areas.%s.zt", areaname), zt-1);
             conf.save();
         }
         public boolean isInArea(Location loc) {
             if (!loc.getWorld().equals(world)) {
                 return false;
             }
             if (xf < loc.getX() && loc.getX() < xt && yf < loc.getY() && loc.getY() < yt && zf < loc.getZ() && loc.getZ() < zt) {
                 return true;
             } else {
                 return false;
             }
         }
     }
 }
