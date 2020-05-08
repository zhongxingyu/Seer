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
 
 package net.invisioncraft.plugins.salesmania.listeners;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.channels.ChannelManager;
 import net.invisioncraft.plugins.salesmania.configuration.*;
 import net.invisioncraft.plugins.salesmania.event.auction.*;
 import net.invisioncraft.plugins.salesmania.event.auction.queue.AuctionQueuedEvent;
 import net.invisioncraft.plugins.salesmania.event.salesmania.AuctionDisableEvent;
 import net.invisioncraft.plugins.salesmania.event.salesmania.AuctionEnableEvent;
 import net.invisioncraft.plugins.salesmania.util.ItemManager;
 import net.invisioncraft.plugins.salesmania.util.MsgUtil;
 import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroup;
 import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroupManager;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class AuctionEventListener implements Listener {
     Salesmania plugin;
     AuctionIgnoreList auctionIgnoreList;
     AuctionSettings auctionSettings;
     Economy economy;
     LocaleHandler localeHandler;
     ChannelManager channelManager;
     WorldGroupManager worldGroupManager;
     RegionSettings regionSettings;
     Logger logger;
 
     public AuctionEventListener(Salesmania plugin) {
         this.plugin = plugin;
         auctionSettings =  plugin.getSettings().getAuctionSettings();
         auctionIgnoreList = plugin.getAuctionIgnoreList();
         economy = plugin.getEconomy();
         localeHandler = plugin.getLocaleHandler();
         channelManager = plugin.getChannelManager();
         worldGroupManager = plugin.getWorldGroupManager();
         logger = plugin.getLogger();
         regionSettings = plugin.getSettings().getRegionSettings();
     }
 
     @EventHandler
     public void onAuctionTimerEvent(final AuctionTimerEvent auctionEvent) {
         int timeRemaining = auctionEvent.getAuction().getTimeRemaining();
         List<Integer> notifyTimes = auctionSettings.getNofityTime();
         WorldGroup worldGroup = auctionEvent.getAuction().getWorldGroup();
         if(notifyTimes.contains(timeRemaining)) {
             for(Locale locale : localeHandler.getLocales()) {
                 String message =
                         locale.getMessage("Auction.tag") +
                         locale.getMessage("Auction.timeRemaining");
                 auctionEvent.getAuction().updateInfoTokens();
                 message = auctionEvent.getAuction().infoReplace(message);
                 message = auctionEvent.getAuction().enchantReplace(message, locale.getMessage("Auction.enchantInfo"), locale.getMessage("Auction.enchantInfo"), locale);
                 channelManager.broadcast(worldGroup, message, locale.getPlayers());
             }
 
         }
     }
 
     @EventHandler
     public void onAuctionStartEvent(final AuctionStartEvent auctionEvent) {
         Auction auction = auctionEvent.getAuction();
         OfflinePlayer player = auctionEvent.getAuction().getOwner();
         WorldGroup worldGroup = auctionEvent.getAuction().getWorldGroup();
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             ArrayList<String> infoList = locale.getMessageList("Auction.startInfo");
             infoList = auction.infoReplace(infoList);
             infoList = auction.enchantReplace(infoList,
                     locale.getMessage("Auction.enchant"),
                     locale.getMessage("Auction.enchantInfo"), locale);
             infoList = MsgUtil.addPrefix(infoList, locale.getMessage("Auction.tag"));
             String[] message = infoList.toArray(new String[infoList.size()]);
             channelManager.broadcast(worldGroup, message, locale.getPlayers());
         }
 
         logger.info(String.format("Started auction for player '%s'", player.getName()));
         logger.info(String.format("World group: '%s'", worldGroup.getGroupName()));
         logger.info(String.format("Item stack: '%s'", auction.getItemStack().toString()));
         logger.info(String.format("Starting Bid: %,.2f", auction.getBid()));
     }
 
     @EventHandler
     public void onAuctionQueueEvent(final AuctionQueuedEvent auctionEvent) {
         Auction auction = auctionEvent.getAuction();
 
         // Take item
         ItemManager.takeItem(auction.getOwner().getPlayer(), auction.getItemStack());
         logger.info(String.format("Player '%s' has queued an auction for '%s'",
                 auction.getOwner().getName(), auction.getItemStack().toString()));
 
         // Tax
         processTax(new AuctionStartEvent(auction));
     }
 
     @EventHandler
     public void onAuctionBidEvent(final AuctionBidEvent auctionEvent) {
         Auction auction = auctionEvent.getAuction();
         String ecoWorld = auction.getWorldGroup().getWorlds().get(0).getName();
         // Anti-Snipe
         if(auction.getTimeRemaining() < auctionSettings.getSnipeTime()) {
             auction.setTimeRemaining(auction.getTimeRemaining() + auctionSettings.getSnipeValue());
         }
 
         // Give back last bid
         if(auction.getLastWinner() != null) {
             OfflinePlayer player = auction.getLastWinner();
 
             economy.depositPlayer(player.getName(), ecoWorld, auction.getLastBid());
             logger.info(String.format("Returned %,.2f to player '%s' for previous bid.",
                     auction.getLastBid(), player.getName()));
 
             if(player.getPlayer().isOnline()) {
                 Locale locale = plugin.getLocaleHandler().getLocale(player.getPlayer());
                 player.getPlayer().sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.outBid"), auction.getWinner().getName()));
             }
         }
 
         // Take new bid
         economy.withdrawPlayer(auction.getWinner().getName(), ecoWorld, auction.getBid());
         logger.info(String.format("Removed %,.2f from player '%s' for auction bid.",
                 auction.getBid(), auction.getWinner().getName()));
 
         WorldGroup worldGroup = auction.getWorldGroup();
         worldGroup.getAuctionQueue().update();
 
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             String message = locale.getMessage("Auction.tag") +
             String.format(locale.getMessage("Auction.bidRaised"),
                 auction.getBid(), auction.getWinner().getName());
             channelManager.broadcast(worldGroup, message, locale.getPlayers());
         }
     }
 
     @EventHandler
     public void onAuctionEndEvent(final AuctionEndEvent auctionEvent) {
         Auction auction = auctionEvent.getAuction();
         WorldGroup worldGroup = auction.getWorldGroup();
         String ecoWorld = auction.getWorldGroup().getWorlds().get(0).getName();
 
         // NO BIDS
         if(auctionEvent.getAuction().getWinner() == null) {
             // Broadcast
             for(Locale locale : localeHandler.getLocales()) {
                 String message =
                         locale.getMessage("Auction.tag") +
                         locale.getMessage("Auction.noBids");
                 channelManager.broadcast(worldGroup, message, locale.getPlayers());
             }
 
             // Tax
             if(auctionSettings.taxIfNoBids()) {
                 processTax(auctionEvent);
             }
 
             // Give back item to owner
             giveItem(auction.getOwner(), auction.getItemStack(), auction.getWorldGroup());
             logger.info(String.format("No bids for auction, item stack '%s' returned to player '%s'",
                     auction.getItemStack().toString(), auction.getOwner().getName()));
         }
 
         // BIDS
         else  {
 
             // Broadcast
             for(Locale locale : localeHandler.getLocales()) {
                 ArrayList<String> infoList = locale.getMessageList("Auction.endInfo");
                 infoList = auction.infoReplace(infoList);
                 infoList = auction.enchantReplace(infoList,
                         locale.getMessage("Auction.enchant"),
                         locale.getMessage("Auction.enchantInfo"), locale);
                 infoList = MsgUtil.addPrefix(infoList, locale.getMessage("Auction.tag"));
                 String[] message = infoList.toArray(new String[infoList.size()]);
                 channelManager.broadcast(worldGroup, message, locale.getPlayers());
             }
 
             // Give money to owner
             economy.depositPlayer(auction.getOwner().getName(), ecoWorld, auction.getBid());
             logger.info(String.format("Auction finished, %,.2f given to player '%s'",
                     auction.getBid(), auction.getOwner().getName()));
 
             // Tax
             processTax(auctionEvent);
 
             // Give item to winner
             giveItem(auction.getWinner(), auction.getItemStack(), auction.getWorldGroup());
             logger.info(String.format("Item stack '%s' given to auction winner '%s'",
                     auction.getItemStack(), auction.getWinner()));
         }
 
         worldGroup.getAuctionQueue().remove();
         worldGroup.getAuctionQueue().startCooldown();
     }
 
     @EventHandler
     public void onAuctionCancelEvent(final AuctionCancelEvent auctionEvent) {
         Auction auction = auctionEvent.getAuction();
         WorldGroup worldGroup = auction.getWorldGroup();
         String ecoWorld = auction.getWorldGroup().getWorlds().get(0).getName();
 
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             String message = locale.getMessage("Auction.tag") +
                     locale.getMessage("Auction.canceled");
             channelManager.broadcast(worldGroup, message, locale.getPlayers());
         }
 
         // Give back bid
         if(auction.getWinner() != null) {
             economy.depositPlayer(auction.getWinner().getName(), ecoWorld, auction.getBid());
             logger.info(String.format("Returned %,.2f to player '%s' for canceled auction bid.",
                     auction.getBid(), auction.getWinner().getName()));
         }
 
         // Give back item to owner
         giveItem(auction.getOwner(), auction.getItemStack(), auction.getWorldGroup());
         logger.info(String.format("Returned item stack '%s' to auction owner '%s' for canceled auction.",
                 auction.getItemStack(), auction.getOwner().getName()));
 
         worldGroup.getAuctionQueue().remove();
         worldGroup.getAuctionQueue().startCooldown();
     }
 
     // TODO allow enable/disable in specific world groups
     @EventHandler
     public void onAuctionEnableEvent(final AuctionEnableEvent auctionEvent) {
         for(WorldGroup worldGroup : worldGroupManager.getWorldGroups()) {
             worldGroup.getAuctionQueue().start();
             // Broadcast
             for(Locale locale : localeHandler.getLocales()) {
                 String message = locale.getMessage("Auction.tag") +
                         locale.getMessage("Auction.enabled");
                 channelManager.broadcast(worldGroup, message, locale.getPlayers());
             }
         }
         logger.info("Auction enabled, queue processing started.");
     }
 
     @EventHandler
     public void onAuctionDisableEvent(final AuctionDisableEvent auctionEvent) {
         for(WorldGroup worldGroup : worldGroupManager.getWorldGroups()) {
             worldGroup.getAuctionQueue().stop();
             // Broadcast
             for(Locale locale : localeHandler.getLocales()) {
                 String message = locale.getMessage("Auction.tag") +
                         locale.getMessage("Auction.disabled");
                 channelManager.broadcast(worldGroup, message, locale.getPlayers());
             }
         }
         logger.info("Auction disabled, queue processing stopped.");
 
     }
 
     private void giveItem(OfflinePlayer player, ItemStack itemStack, WorldGroup worldGroup) {
         if(player.isOnline()) {
             Locale locale = plugin.getLocaleHandler().getLocale(player.getPlayer());
             // Region
            if(regionSettings.shouldStash(player.getPlayer())) {
                 plugin.getItemStash().store(player, itemStack, worldGroup);
                player.getPlayer().sendMessage(locale.getMessage("Auction.regionStashed"));
                 return;
             }
 
             // World group
             if(worldGroupManager.getGroup(player) != worldGroup) {
                 plugin.getItemStash().store(player, itemStack, worldGroup);
                 player.getPlayer().sendMessage(String.format(locale.getMessage("Stash.itemsWaitingInGroup"), worldGroup.getGroupName()));
             }
             else {
                 HashMap<Integer, ItemStack> remainingItems = player.getPlayer().getInventory().addItem(itemStack);
                 if(!remainingItems.isEmpty()) {
                     plugin.getItemStash().store(player, new ArrayList<ItemStack>(remainingItems.values()), worldGroup);
                     player.getPlayer().sendMessage(locale.getMessage("Stash.itemsWaiting"));
                 }
             }
         }
         else plugin.getItemStash().store(player, itemStack, worldGroup);
     }
 
     private void processTax(AuctionEvent auctionEvent) {
         Auction auction = auctionEvent.getAuction();
         OfflinePlayer owner = auction.getOwner();
         String ecoWorld = auction.getWorldGroup().getWorlds().get(0).getName();
 
         double taxAmount;
         if(auctionEvent instanceof AuctionStartEvent) {
             taxAmount = auction.getStartTax();
         }
         else if (auctionEvent instanceof AuctionEndEvent) {
             taxAmount = auction.getEndTax();
         }
         else return;
 
         if(taxAmount != 0) {
             economy.withdrawPlayer(owner.getName(), ecoWorld, taxAmount);
             if(auctionSettings.useTaxAccount()) {
                 economy.depositPlayer(auctionSettings.getTaxAccount(), taxAmount);
             }
             if(owner.isOnline()) {
                 Locale locale = localeHandler.getLocale(owner.getPlayer());
                 owner.getPlayer().sendMessage(String.format(locale.getMessage("Auction.tax"),
                         taxAmount));
             }
         }
     }
 }
