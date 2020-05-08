 package to.joe.strangeweapons.command;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import to.joe.strangeweapons.StrangeWeapons;
 import to.joe.strangeweapons.meta.StrangeWeapon;
 
 public class CratesCommand implements CommandExecutor {
 
     private StrangeWeapons plugin;
 
     public CratesCommand(StrangeWeapons plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage(ChatColor.RED + "Only players may use this command");
             return true;
         }
         if (args.length == 1) {
             Set<String> allCrates;
             if (plugin.getConfig().contains("crates")) {
                 allCrates = plugin.getConfig().getConfigurationSection("crates").getKeys(false);
             } else {
                 allCrates = new HashSet<String>();
             }
             int maxCrate = 0;
             if (!allCrates.isEmpty()) {
                 for (String c : allCrates) {
                     int crate = Integer.parseInt(c);
                     if (crate > maxCrate) {
                         maxCrate = crate;
                     }
                 }
             }
             if (args[0].equalsIgnoreCase("newcrate")) {
                 maxCrate++;
                 plugin.getConfig().set("crates." + maxCrate + ".drops", false);
                 plugin.saveConfig();
                 sender.sendMessage(ChatColor.GOLD + "Added new crate series " + ChatColor.AQUA + maxCrate);
                 return true;
             }
             if (args[0].equalsIgnoreCase("listcrates")) {
                 if (allCrates.isEmpty()) {
                     sender.sendMessage(ChatColor.RED + "No crate series exist yet");
                 } else {
                     sender.sendMessage(ChatColor.GREEN + "" + maxCrate + " series exist");
                     for (String c : allCrates) {
                         sender.sendMessage(ChatColor.GOLD + "Series " + ChatColor.AQUA + c + ChatColor.GOLD + ", drops: " + ChatColor.AQUA + plugin.getConfig().getBoolean("crates." + c + ".drops"));
                     }
                 }
                 return true;
             }
         }
         if (args.length == 2) {
             if (args[0].equalsIgnoreCase("listcontents")) {
                 int series = 0;
                 try {
                     series = Integer.parseInt(args[1]);
                 } catch (NumberFormatException e) {
                     sender.sendMessage(ChatColor.RED + "That's not a number");
                     return true;
                 }
                 if (!plugin.getConfig().contains("crates")) {
                     sender.sendMessage(ChatColor.RED + "No crate series exist yet");
                     return true;
                 }
                 if (plugin.getConfig().getConfigurationSection("crates").contains(series + "")) {
                     if (plugin.getConfig().contains("crates." + series + ".contents")) {
                         sender.sendMessage(ChatColor.GOLD + "Series " + ChatColor.AQUA + series + ChatColor.GOLD + " contains the following");
                         ConfigurationSection cs = plugin.getConfig().getConfigurationSection("crates." + series + ".contents");
                         for (String item : cs.getKeys(false)) {
                             ConfigurationSection i = cs.getConfigurationSection(item);
                             sender.sendMessage(ChatColor.AQUA + item + ChatColor.GOLD + " | " + ChatColor.AQUA + ChatColor.stripColor(i.getItemStack("item").serialize().toString()) + ChatColor.GOLD + " with weight " + ChatColor.AQUA + i.getDouble("weight"));
                         }
                     } else {
                         sender.sendMessage(ChatColor.RED + "Crate is empty");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "Crate does not exist");
                 }
                 return true;
             }
         }
         if (args.length == 3) {
             if (args[0].equalsIgnoreCase("add")) {
                 int series = 0;
                 double weight = 1;
                 try {
                     series = Integer.parseInt(args[1]);
                     weight = Double.parseDouble(args[2]);
                 } catch (NumberFormatException e) {
                     sender.sendMessage(ChatColor.RED + "That's not a number");
                     return true;
                 }
                 if (plugin.getConfig().getConfigurationSection("crates").contains(series + "")) {
                     ConfigurationSection cs = plugin.getConfig().getConfigurationSection("crates." + series + ".contents");
                     int maxItem = 0;
                     if (plugin.getConfig().contains("crates." + series + ".contents")) {
                         for (String c : cs.getKeys(false)) {
                             int crate = Integer.parseInt(c);
                             if (crate > maxItem) {
                                 maxItem = crate;
                             }
                         }
                     }
                     maxItem++;
                     ItemStack item = ((Player)sender).getItemInHand().clone();
                     if (StrangeWeapon.isStrangeWeapon(item)) {
                         item = new StrangeWeapon(item).clone();
                     }
                     plugin.getConfig().set("crates." + series + ".contents." + maxItem + ".item", item);
                     plugin.getConfig().set("crates." + series + ".contents." + maxItem + ".weight", weight);
                     plugin.getConfig().set("crates." + series + ".contents." + maxItem + ".hidden", false);
                     sender.sendMessage(ChatColor.GOLD + "Added " + ChatColor.AQUA + ChatColor.stripColor(item.serialize().toString()) + ChatColor.GOLD + " with weight " + ChatColor.AQUA + weight + ChatColor.GOLD + " to series " + ChatColor.AQUA + series);
                     plugin.saveConfig();
                 } else {
                     sender.sendMessage(ChatColor.RED + "Crate does not exist");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("remove")) {
                 int series = 0;
                 int item = 1;
                 try {
                     series = Integer.parseInt(args[1]);
                     item = Integer.parseInt(args[2]);
                 } catch (NumberFormatException e) {
                     sender.sendMessage(ChatColor.RED + "That's not a number");
                     return true;
                 }
                 if (plugin.getConfig().getConfigurationSection("crates").contains(series + "")) {
                     ConfigurationSection cs = plugin.getConfig().getConfigurationSection("crates." + series + ".contents");
                     cs.set(item + "", null);
                     plugin.saveConfig();
                     sender.sendMessage(ChatColor.GOLD + "Deleted item " + ChatColor.AQUA + item + ChatColor.GOLD + " from crate " + ChatColor.AQUA + series);
                 } else {
                     sender.sendMessage(ChatColor.RED + "Crate does not exist");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("setdrop")) {
                 int series = 0;
                 boolean drops = Boolean.parseBoolean(args[1]);
                 try {
                     series = Integer.parseInt(args[2]);
                 } catch (NumberFormatException e) {
                     sender.sendMessage(ChatColor.RED + "That's not a number");
                     return true;
                 }
                 if (plugin.getConfig().getConfigurationSection("crates").contains(series + "")) {
                     if (plugin.getConfig().contains("crates." + series + ".contents")) {
                         plugin.getConfig().set("crates." + series + ".drops", drops);
                         plugin.saveConfig();
                         sender.sendMessage(ChatColor.GOLD + "Crate series " + ChatColor.AQUA + series + ChatColor.GOLD + " drops set to " + ChatColor.AQUA + drops);
                     } else {
                         plugin.getConfig().set("crates." + series + ".drops", false);
                         plugin.saveConfig();
                         sender.sendMessage(ChatColor.RED + "Crate series " + series + " is empty. Cannot set drop");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "Crate does not exist");
                 }
                 return true;
             }
         }
         sender.sendMessage(new String[] { ChatColor.RED + "Valid commands are:", ChatColor.GOLD + "newcrate " + ChatColor.AQUA + "- " + ChatColor.RED + "Create a new crate series", ChatColor.GOLD + "listcrates " + ChatColor.AQUA + "- " + ChatColor.RED + "List all of the crate series", ChatColor.GOLD + "listcontents " + ChatColor.YELLOW + "<series> " + ChatColor.AQUA + "- " + ChatColor.RED + "List the contents of a crate and their weights", ChatColor.GOLD + "add " + ChatColor.YELLOW + "<series> <weight> " + ChatColor.AQUA + "- " + ChatColor.RED + "Add the item you are holding to the specified crate", ChatColor.GOLD + "remove " + ChatColor.YELLOW + "<series> <id> " + ChatColor.AQUA + "- " + ChatColor.RED + "Remove the specified item from the crate",
                 ChatColor.GOLD + "setdrop " + ChatColor.YELLOW + "<true/false> <series> " + ChatColor.AQUA + "- " + ChatColor.RED + "Set if the specified crate should drop" });
         return true;
     }
 }
