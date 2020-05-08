 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.CommandHandler;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Owner: Justin
  * Date: 5/17/12
  * Time: 10:25 AM
  */
 public class AuctionStart extends CommandHandler {
     public AuctionStart(Salesmania plugin) {
         super(plugin);
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
         if(!(sender instanceof Player)) {
             sender.sendMessage(localeHandler.getDefaultLocale().
                     getMessage("Console.cantStartAuction"));
             return false;
         }
 
         Player player = (Player) sender;
         Auction auction = plugin.getAuction();
         ItemStack itemStack = player.getItemInHand();
         if(!player.hasPermission("salesmania.auction.start")) {
             player.sendMessage(String.format(
                     locale.getMessage("Permission.noPermission"),
                     locale.getMessage("Permisson.Auction.start")));
             return false;
         }
        switch(auction.start(player, itemStack, Long.valueOf(args[1]))) {
             case RUNNING:
                 player.sendMessage(locale.getMessage("Auction.alreadyStarted"));
                 return false;
             case COOLDOWN:
                 player.sendMessage(locale.getMessage("Auction.cooldown"));
                 return false;
         }
         return false;
     }
 }
