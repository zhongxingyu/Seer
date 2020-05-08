 package to.joe.bungee.commands;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 import to.joe.bungee.Util;
 
 public class CommandAlert extends Command {
 
     public CommandAlert() {
         super("alert", "j2.srstaff");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (args.length == 0) {
             sender.sendMessage(ChatColor.RED + "You must supply a message.");
         } else {
             final String message = ChatColor.translateAlternateColorCodes('&', Util.combineSplit(0, args, " "));
             final String normal = "[" + ChatColor.BLUE + "ALERT" + ChatColor.RESET + "] " + message;
             final String admin = "[" + ChatColor.BLUE + sender.getName() + ChatColor.RESET + "] " + message;
             for (final ProxiedPlayer con : ProxyServer.getInstance().getPlayers()) {
                con.sendMessage(con.hasPermission("j2.admin") ? admin : normal);
             }
         }
     }
 }
