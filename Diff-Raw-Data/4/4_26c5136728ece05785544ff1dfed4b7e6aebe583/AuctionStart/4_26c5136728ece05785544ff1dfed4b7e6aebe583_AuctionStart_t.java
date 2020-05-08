 /*
 This file is part of Salesmania.
 
     Salesmania is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Salesmania is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.commands.CommandHandler;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.util.ItemManager;
 import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroup;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class AuctionStart extends CommandHandler {
     AuctionSettings auctionSettings;
     public AuctionStart(Salesmania plugin) {
         super(plugin);
         auctionSettings = plugin.getSettings().getAuctionSettings();
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
 
         // Console check
         if(!(sender instanceof Player)) {
             sender.sendMessage(locale.getMessage("Console.cantStartAuction"));
             return false;
         }
 
         Player player = (Player) sender;
         ItemStack itemStack = player.getItemInHand().clone();
 
         WorldGroup worldGroup = plugin.getWorldGroupManager().getGroup(player);
         if(worldGroup == null) {
             sender.sendMessage(locale.getMessage("Auction.worldDisabled"));
             return false;
         }
 
         // Disable check
         if(!auctionSettings.getEnabled()) {
             sender.sendMessage(locale.getMessage("Auction.disabled"));
             return false;
         }
 
         // Creative check
         if(!auctionSettings.getAllowCreative() && player.getGameMode() == GameMode.CREATIVE) {
             sender.sendMessage(locale.getMessage("Auction.noCreative"));
             return false;
         }
 
         // Syntax check
         if(args.length < 2) {
             sender.sendMessage(locale.getMessage("Syntax.Auction.auctionStart"));
             return false;
         }
 
         float startingBid;
         int quantity = 1;
         int time = auctionSettings.getDefaultTime();
         try {
             startingBid = Float.valueOf(args[1]);
            if(args.length > 2) quantity = Integer.valueOf(args[2]);
            if(args.length > 3) time = Integer.valueOf(args[3]);
         } catch (NumberFormatException ex) {
             sender.sendMessage(locale.getMessage("Syntax.Auction.auctionStart"));
             return false;
         }
 
         // Time check
         if(time > auctionSettings.getMaxTime()) time = auctionSettings.getMaxTime();
         else if (time < auctionSettings.getMinTime()) time = auctionSettings.getMinTime();
 
         // Permission check
         if(!sender.hasPermission("salesmania.auction.start")) {
             sender.sendMessage(String.format(
                     locale.getMessage("Permission.noPermission"),
                     locale.getMessage("Permission.Auction.start")));
             return false;
         }
 
         // Blacklist check
         if(auctionSettings.isBlacklisted(itemStack)) {
             player.sendMessage(String.format(
                     locale.getMessage("Auction.itemBlacklisted"), ItemManager.getName(itemStack)));
             return false;
         }
 
         // Quantity check
         if(quantity > ItemManager.getQuantity(player, itemStack)) {
             player.sendMessage(locale.getMessage("Auction.notEnough"));
             return false;
         }
         if(quantity < 1) {
             sender.sendMessage(locale.getMessage("Syntax.Auction.auctionStart"));
             return false;
         }
         else itemStack.setAmount(quantity);
 
         Auction auction = new Auction(plugin);
         auction.setTimeRemaining(time);
         auction.setWorldGroup(worldGroup);
         switch(auction.queue(player, itemStack, startingBid)) {
             case QUEUE_FULL:
                 player.sendMessage(locale.getMessage("Auction.queueFull"));
                 return false;
             case PLAYER_QUEUE_FULL:
                 player.sendMessage(locale.getMessage("Auction.playerQueueFull"));
                 return false;
             case UNDER_MIN:
                 player.sendMessage(String.format(locale.getMessage("Auction.startUnderMin"),
                         auctionSettings.getMinStart()));
                 return false;
             case OVER_MAX:
                 player.sendMessage(String.format(locale.getMessage("Auction.startOverMax"),
                         auctionSettings.getMaxStart()));
                 return false;
             case CANT_AFFORD_TAX:
                 player.sendMessage(String.format(locale.getMessage("Auction.cantAffordTax"),
                         auction.getStartTax()));
                 return false;
             case QUEUE_SUCCESS:
                 player.sendMessage(locale.getMessage("Auction.queued"));
                 return true;
             case COOLDOWN_SUCCESS:
                  player.sendMessage(locale.getMessage("Auction.cooldown"));
                 return true;
             case SUCCESS:
                 return true;
         }
         return false;
     }
 }
