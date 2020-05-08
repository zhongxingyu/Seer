 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of CastAway.
  * 
  * CastAway is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * CastAway is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CastAway.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.castaway.listener;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.castaway.blocks.AbstractBlock;
 import de.minestar.castaway.blocks.DungeonEndBlock;
 import de.minestar.castaway.blocks.DungeonStartBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.BlockVector;
 import de.minestar.castaway.data.Dungeon;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class RegisterListener implements Listener {
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         // only some actions are handled
         if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
 
         // is the ItemInHand correct?
         if (event.getPlayer().getItemInHand().getType().equals(Material.BONE)) {
             // check permissions?
             if (!UtilPermissions.playerCanUseCommand(event.getPlayer(), "castaway.admin")) {
                 PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "You are not allowed to do this!");
                 event.setCancelled(true);
                 return;
             }
 
             event.setCancelled(true);
             event.setUseInteractedBlock(Event.Result.DENY);
             event.setUseItemInHand(Event.Result.DENY);
 
             Player player = event.getPlayer();
             boolean isLeftClick = (event.getAction() == Action.LEFT_CLICK_BLOCK);
             if (player.isSneaking()) {
                 Dungeon dungeon = CastAwayCore.gameManager.getDungeonByName("test");
                 if (dungeon != null) {
                     if (isLeftClick && event.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
                         AbstractBlock actionBlock = new DungeonStartBlock(new BlockVector(event.getClickedBlock()), dungeon);
                         if (CastAwayCore.databaseManager.addActionBlock(actionBlock)) {
                             CastAwayCore.databaseManager.addActionBlock(actionBlock);
                             CastAwayCore.gameManager.addBlock(actionBlock.getVector(), actionBlock);
                             PlayerUtils.sendSuccess(event.getPlayer(), CastAwayCore.NAME, "Start block added for '" + dungeon.getDungeonName() + "'.");
                         } else {
                             PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "Error creating block in database!");
                         }
                     } else if (!isLeftClick && event.getClickedBlock().getType().equals(Material.STONE_BUTTON)) {
                         AbstractBlock actionBlock = new DungeonEndBlock(new BlockVector(event.getClickedBlock()), dungeon);
                         if (CastAwayCore.databaseManager.addActionBlock(actionBlock)) {
                             CastAwayCore.gameManager.addBlock(actionBlock.getVector(), actionBlock);
                             PlayerUtils.sendSuccess(event.getPlayer(), CastAwayCore.NAME, "End block added for '" + dungeon.getDungeonName() + "'.");
                         } else {
                             PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "Error creating block in database!");
                         }
                     }
                 }
            } else {
                PlayerUtils.sendError(event.getPlayer(), CastAwayCore.NAME, "Dungeon 'test' not found!");
             }
         }
     }
 }
