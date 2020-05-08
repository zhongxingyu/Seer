 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.CommandHandler;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Owner: Justin
  * Date: 5/17/12
  * Time: 10:25 AM
  */
 public class AuctionEnd extends CommandHandler {
     public AuctionEnd(Salesmania plugin) {
         super(plugin);
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
 
         if(!(sender instanceof Player)) {
             plugin.getAuction().end();
             return true;
         }
        Player player = (Player) sender;
 
        if(player.hasPermission("salesmania.auction.end") |
        player == plugin.getAuction().getOwner()) {
            plugin.getAuction().end();
             return true;
         }
         else {
             sender.sendMessage(String.format(
                     locale.getMessage("Permission.noPermission"),
                     locale.getMessage("Permission.Auction.end")));
             return false;
         }
     }
 }
