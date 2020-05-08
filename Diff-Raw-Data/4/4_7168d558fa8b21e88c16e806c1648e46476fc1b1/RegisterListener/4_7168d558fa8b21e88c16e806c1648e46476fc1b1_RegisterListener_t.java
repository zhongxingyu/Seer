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
 
 import java.lang.reflect.Constructor;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.ActionBlockType;
 import de.minestar.castaway.data.BlockVector;
 import de.minestar.castaway.data.Dungeon;
 import de.minestar.castaway.data.RegisterSelection;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class RegisterListener implements Listener {
 
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         // only some actions are handled
         if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
 
         Player player = event.getPlayer();
         RegisterSelection selection = CastAwayCore.playerManager.getBlockTypeToRegister(player.getName());
         if (selection == null) {
             return;
         }
 
         // cancel the event
         event.setCancelled(true);
         event.setUseInteractedBlock(Event.Result.DENY);
         event.setUseItemInHand(Event.Result.DENY);
 
         // handle action
         if (!selection.isSignEdit()) {
             // No HallOfFame-Edit => register block
             Block block = event.getClickedBlock();
             BlockVector vector = new BlockVector(block);
             AbstractActionBlock actionBlock = createInstance(selection.getActionBlockType(), vector, selection.getDungeon());
 
             selection.getDungeon().registerBlocks(actionBlock);
             CastAwayCore.gameManager.registerSingleBlock(actionBlock);
             PlayerUtils.sendSuccess(player, CastAwayCore.NAME, "Der Block wurde registiert.");
         } else {
             // HallOfFame-Edit => register / unregister Signs
             // TODO: implement...
         }
     }
 
     private AbstractActionBlock createInstance(ActionBlockType actionBlockType, BlockVector vector, Dungeon dungeon) {
         AbstractActionBlock block = null;
         try {
             Constructor<? extends AbstractActionBlock> constr = actionBlockType.getClazz().getDeclaredConstructor(BlockVector.class, Dungeon.class);
             block = constr.newInstance(vector, dungeon);
             return block;
         } catch (Exception e) {
             ConsoleUtils.printException(e, CastAwayCore.NAME, "Can't create an instance for the Block " + actionBlockType);
             return null;
         }
     }
 }
