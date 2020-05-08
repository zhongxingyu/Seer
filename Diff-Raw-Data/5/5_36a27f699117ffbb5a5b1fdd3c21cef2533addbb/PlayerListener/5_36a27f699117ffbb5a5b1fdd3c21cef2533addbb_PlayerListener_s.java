 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package de.minestar.AdminStuff.listener;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.bukkit.gemo.utils.BlockUtils;
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.manager.PlayerManager;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class PlayerListener implements Listener {
 
     private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 
     public static Map<String, ItemStack> queuedFillChest = new TreeMap<String, ItemStack>();
 
     private final PlayerManager pManager;
 
     public PlayerListener(PlayerManager pManager) {
         this.pManager = pManager;
     }
 
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         if (BlockUtils.LocationEquals(event.getTo(), event.getFrom()))
             return;
 
         // check glue position
         MinestarPlayer mPlayer = MinestarCore.getPlayer(event.getPlayer());
         Location gluePosition = mPlayer.getLocation("adminstuff.glue");
         if (gluePosition != null)
             event.setTo(gluePosition);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        String name = event.getName().toLowerCase();
 
         MinestarPlayer mPlayer = MinestarCore.getPlayer(name);
 
         // check whether player is banned
         Boolean permBanned = mPlayer.getBoolean("banned");
         if (permBanned != null && permBanned == true) {
             event.disallow(Result.KICK_BANNED, "Du bist gebannt!");
             return;
         }
 
         // check time bann expire
         Long timeBann = mPlayer.getLong("tempBann");
         if (timeBann != null) {
             // timeBann is expired
             if (timeBann <= System.currentTimeMillis())
                 mPlayer.setLong("tempBann", 0L);
             // player is timebanned
             else
                 event.disallow(Result.KICK_BANNED, ("Du bist temporaer gebannt bis " + new Date(timeBann + 1000)));
         }
     }
 
     @EventHandler
     public void onPlayerKick(PlayerKickEvent event) {
         if (event.isCancelled())
             return;
         handleQuit(event.getPlayer());
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         handleQuit(event.getPlayer());
 
         MinestarPlayer mPlayer = MinestarCore.getPlayer(event.getPlayer());
         // Fake quit
         Boolean isHidden = mPlayer.getBoolean("adminstuff.hide");
         if (isHidden != null && isHidden) {
             event.setQuitMessage("");
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
 
         MinestarPlayer mPlayer = MinestarCore.getPlayer(event.getPlayer());
         // Fake quit
         Boolean isHidden = mPlayer.getBoolean("adminstuff.hide");
         if (isHidden != null && isHidden) {
             event.setJoinMessage("");
             pManager.hidePlayer(event.getPlayer());
             PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "Du bist fuer alle anderen Spieler unsichtbar!");
         } else
             pManager.updateHidePlayer(event.getPlayer());
 
     }
 
     private void handleQuit(Player player) {
         MinestarPlayer mPlayer = MinestarCore.getPlayer(player);
         mPlayer.setString("adminstuff.lastseen", dateFormat.format(new Date()));
         // remove temponary values
         mPlayer.removeValue("adminstuff.slapped", Boolean.class);
        mPlayer.removeValue("admisntuff.afk", Boolean.class);
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         // ONLY CLICKS ON A BLOCK
         if (!event.hasBlock())
             return;
 
         Player player = event.getPlayer();
         // FILL CHEST
         if (queuedFillChest.containsKey(player.getName())) {
 
             // CLICKED ON A CHEST?
             if (event.getClickedBlock().getTypeId() != Material.CHEST.getId()) {
                 queuedFillChest.remove(player.getName());
                 PlayerUtils.sendError(player, Core.NAME, "Chestfill abgebrochen!");
                 return;
             } else {
                 event.setUseInteractedBlock(Event.Result.DENY);
                 event.setUseItemInHand(Event.Result.DENY);
                 event.setCancelled(true);
 
                 // FILL CHEST / DOUBLECHEST
                 ItemStack item = queuedFillChest.get(event.getPlayer().getName()).clone();
                 Chest chest = (Chest) event.getClickedBlock().getState();
                 fillChest(chest, item);
 
                 Chest dChest = BlockUtils.isDoubleChest(chest.getBlock());
                 if (dChest != null)
                     fillChest(dChest, item);
 
                 // SEND MESSAGE
                 PlayerUtils.sendSuccess(player, Core.NAME, "Kiste wurde mit '" + item.getType().name() + "' gefuellt!");
                 queuedFillChest.remove(event.getPlayer().getName());
             }
             return;
         }
 
         // Block counting
         if (pManager.isInSelectionMode(player)) {
             pManager.setSelectedBlock(player, event.getClickedBlock(), event.getAction().equals(Action.LEFT_CLICK_BLOCK));
             event.setCancelled(true);
         }
     }
 
     private void fillChest(Chest chest, ItemStack item) {
         if (item.getTypeId() == Material.AIR.getId()) {
             chest.getInventory().clear();
             return;
         }
         for (int i = 0; i < chest.getInventory().getSize(); i++)
             chest.getInventory().addItem(item);
     }
 
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event) {
 
         // Mute Handeling
         MinestarPlayer mPlayer = MinestarCore.getPlayer(event.getPlayer());
         Boolean muted = mPlayer.getBoolean("adminstuff.muted");
 
         // Player is muted -> only ops and player with right permission can read
         if (muted != null && muted) {
             event.setCancelled(true);
 
             String message = ChatColor.RED + "[STUMM] " + mPlayer.getNickName() + ChatColor.WHITE + ": " + event.getMessage();
             Player current = null;
 
             Iterator<Player> i = event.getRecipients().iterator();
             while (i.hasNext()) {
                 current = i.next();
                 if (UtilPermissions.playerCanUseCommand(current, "adminstuff.chat.read.muted"))
                     PlayerUtils.sendBlankMessage(current, message);
             }
         }
 
         // want to chat to a few people
         Set<String> recs = pManager.getRecipients(event.getPlayer().getName());
         // player has used "chat" command
         if (recs != null) {
             Iterator<Player> i = event.getRecipients().iterator();
             Player current = null;
             while (i.hasNext()) {
                 current = i.next();
                 if (!recs.contains(current.getName().toLowerCase()))
                     i.remove();
             }
         }
 
         // reset afk
         Boolean afk = mPlayer.getBoolean("adminstuff.afk");
         if (afk != null && afk) {
             mPlayer.removeValue("adminstuff.afk", Boolean.class);
             pManager.updatePrefix(event.getPlayer(), mPlayer);
         }
     }
 }
