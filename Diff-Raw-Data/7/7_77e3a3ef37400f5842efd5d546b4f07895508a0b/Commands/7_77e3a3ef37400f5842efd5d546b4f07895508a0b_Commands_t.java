 package net.gb.chrizc.giveall;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Commands {
     
     public static void init(Main instance) {
         PluginCommand giveall = instance.getCommand("giveall");
         CommandExecutor commandExecutor = new CommandExecutor() {
             public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
                 if (args.length >= 1) {
                     String amnt;
                     if (args.length == 1) amnt = "64";
                     else amnt = args[1];
                     if (sender instanceof Player) {
                         Player player = (Player)sender;
                         giveall(sender, args[0], amnt, player.getName());
                     } else {
                         giveall(sender, args[0], amnt, "console");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + Main.PREFIX + "Error! Not enough arguments.");
                 }
                   
                 return true;
             }
         };
         
         if (giveall != null) {
             giveall.setExecutor(commandExecutor);
         }
     }
     
     public static void giveall(CommandSender event, String item, String amount, String originalPlayer) {
        if (!event.hasPermission("giveall.give")) {
            event.sendMessage(Main.PREFIX + "You do not have the correct permission to use htis command.");
            return;
        }
        
         Player[] players = Bukkit.getServer().getOnlinePlayers();
         
         String from;
         
         if (originalPlayer.equals("console")) from = "the server";
         else from = ChatColor.YELLOW + originalPlayer + ChatColor.GREEN;
         
         int amnt = 65;
         try {
             amnt = Integer.parseInt(amount);
         } catch (NumberFormatException e) {}
 
         if (amnt > 64) amnt = 64;
         
         ItemStack is = null;
 
         if (item.contains(":")) {
             String[] itemarray = item.split(":");
 
             int itemid = 0;
             try {
                 itemid = Integer.parseInt(itemarray[0]);
             } catch (NumberFormatException e) {}
 
             if (itemid == 0) {
                 event.sendMessage(ChatColor.RED + Main.PREFIX + "There was an error in parsing the item id.");
                 return;
             }
 
             int damage = 0;
             try {
                 damage = Integer.parseInt(itemarray[1]);
             } catch (NumberFormatException e) {}
 
             Material m = Material.getMaterial(itemid);
             if (m == null) {
                 event.sendMessage(ChatColor.RED + Main.PREFIX + "Item ID not found!");
                 return;
             }
             is = new ItemStack(m, amnt, (short)0, (byte)damage);
         } else {
             int itemid = 0;
             try {
                 itemid = Integer.parseInt(item);
             } catch (NumberFormatException e) {}
 
             if (itemid == 0) {
                 event.sendMessage(ChatColor.RED + Main.PREFIX + "There was an error in parsing the item id.");
                 return;
             }
             
             Material m = Material.getMaterial(itemid);
             if (m == null) {
                 event.sendMessage(ChatColor.RED + Main.PREFIX + "Item ID not found!");
                 return;
             }
             
             is = new ItemStack(m, amnt);
         }
         
         for (int i = 0; i < players.length; i++) {
             if (!players[i].getName().equalsIgnoreCase(originalPlayer)) {
                 players[i].getInventory().addItem(is);
                 if (Config.messagePlayers) players[i].sendMessage(ChatColor.GREEN + Main.PREFIX + "You've just been given " + amnt + " " + ChatColor.YELLOW + is.getType().name().toLowerCase() + ChatColor.GREEN + " from " + from + ".");
             }
         }
         
         event.sendMessage(ChatColor.GREEN + Main.PREFIX + "Given everyone " + amnt + " " + is.getType().name().toLowerCase() + "!");
         if (!originalPlayer.equals("console")) Main.log.info(Main.PREFIX + originalPlayer + " gave everyone " + amount + " " + is.getType().name().toLowerCase() + ".");
     }
 }
