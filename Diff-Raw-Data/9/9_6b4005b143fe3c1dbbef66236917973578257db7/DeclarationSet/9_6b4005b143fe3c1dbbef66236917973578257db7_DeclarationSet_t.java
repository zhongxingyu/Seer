 package edgruberman.bukkit.doorman.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import edgruberman.bukkit.doorman.Doorman;
 import edgruberman.bukkit.doorman.Main;
 import edgruberman.bukkit.doorman.RecordKeeper;
 
 public final class DeclarationSet implements CommandExecutor {
 
     private final Doorman doorman;
     private final RecordKeeper records;
 
     public DeclarationSet(final Doorman doorman, final RecordKeeper records) {
         this.doorman = doorman;
         this.records = records;
     }
 
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
         if (args.length == 0) {
            Main.courier.send(sender, "requires-argument", "<Message>");
             return false;
         }
 
         final String text = DeclarationSet.join(args, " ");
         if (text == null) return false;
 
         final long set = System.currentTimeMillis();
         final String from = (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName());
 
         this.records.add(set, from, ChatColor.translateAlternateColorCodes('&', text));
         this.doorman.clearLast();
 
         for (final Player player : Bukkit.getOnlinePlayers()) {
             this.records.declare(player);
             this.doorman.updateLast(player.getName());
         }
 
         if (!(sender instanceof Player)) {
             this.records.declare(sender);
             this.doorman.updateLast(sender.getName());
         }
 
         return true;
     }
 
     private static String join(final String[] args, final String delim) {
         if (args == null || args.length == 0) return "";
 
         final StringBuilder sb = new StringBuilder();
         for (final String s : args) sb.append(s + delim);
         sb.delete(sb.length() - delim.length(), sb.length());
         return sb.toString();
     }
 
 }
