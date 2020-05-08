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
 import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroup;
 import net.milkbowl.vault.item.Items;
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class AuctionBid extends CommandHandler {
     AuctionSettings auctionSettings;
     public AuctionBid(Salesmania plugin) {
         super(plugin);
         auctionSettings = settings.getAuctionSettings();
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
 
         // Console check
         if(!(sender instanceof Player)) {
             sender.sendMessage(locale.getMessage("Console.cantBid"));
             return false;
         }
 
         Player player = (Player) sender;
         WorldGroup worldGroup = plugin.getWorldGroupManager().getGroup(player);
         if(worldGroup == null) {
             sender.sendMessage(locale.getMessage("Auction.worldDisabled"));
             return false;
         }
 
         // Permission check
         if(!sender.hasPermission("salesmania.auction.bid")) {
             sender.sendMessage(String.format(
                     locale.getMessage("Permission.noPermission"),
                     locale.getMessage("Permission.Auction.bid")));
             return false;
         }
 
         // Syntax check
         double bidAmount;
         if(args.length < 2) {
             sender.sendMessage(locale.getMessage("Syntax.Auction.auctionBid"));
             return false;
         }
         try {
             bidAmount = Double.valueOf(args[1]);
         }   catch (NumberFormatException ex) {
             sender.sendMessage(locale.getMessage("Syntax.Auction.auctionBid"));
             return false;
         }
 
         // Creative check
         if(!auctionSettings.getAllowCreative() && player.getGameMode() == GameMode.CREATIVE) {
             sender.sendMessage(locale.getMessage("Auction.noCreative"));
             return false;
         }
 
         // Funds check
         if(!plugin.getEconomy().has(player.getName(), bidAmount)) {
             player.sendMessage(locale.getMessage("Auction.Bidding.notEnoughMoney"));
             return false;
         }
 
         Auction auction = plugin.getWorldGroupManager().getGroup(player).getAuctionQueue().getCurrentAuction();
         switch(auction.bid(player, bidAmount)) {
             case SUCCESS:
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.bidSuccess"),
                         bidAmount, Items.itemById(auction.getItemStack().getTypeId()).getName()));
                 return true;
             case OVER_MAX:
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.overMax"),
                         auction.getMaxBid()));
                 return false;
             case UNDER_MIN:
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.underMin"),
                         auction.getMinBid()));
                 return false;
             case NOT_RUNNING:
                 player.sendMessage(locale.getMessage("Auction.notRunning"));
                 return false;
             case WINNING:
                 player.sendMessage(locale.getMessage("Auction.Bidding.playerWinning"));
                 return false;
             case OWNER:
                 player.sendMessage(locale.getMessage("Auction.Bidding.playerOwner"));
                 return false;
         }
 
         return false;
     }
 }
