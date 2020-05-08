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
 
 import net.minecraft.server.Packet130UpdateSign;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
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
 import de.minestar.castaway.data.RegisterAction;
 import de.minestar.castaway.data.RegisterSelection;
 import de.minestar.castaway.data.SingleSign;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class RegisterListener implements Listener {
 
     private void handleAddBlock(Block block, Player player, RegisterSelection selection) {
         BlockVector vector = new BlockVector(block);
         AbstractActionBlock actionBlock = createInstance(selection.getActionBlockType(), vector, selection.getDungeon());
         AbstractActionBlock checkBlock = CastAwayCore.gameManager.getBlock(vector);
         if (checkBlock == null) {
             CastAwayCore.gameManager.registerSingleBlock(actionBlock);
             CastAwayCore.playerManager.removeRegisterMode(player.getName());
             PlayerUtils.sendSuccess(player, CastAwayCore.NAME, "Der Block wurde registiert.");
         } else {
             PlayerUtils.sendError(player, CastAwayCore.NAME, "Dieser Block ist bereits ein ActionBlock.");
         }
     }
 
     private void handleRemoveBlock(Block block, Player player, RegisterSelection selection) {
         // Move to normalmode
         CastAwayCore.playerManager.removeRegisterMode(player.getName());
 
         // get the ActionBlock
         BlockVector vector = new BlockVector(block);
         AbstractActionBlock actionBlock = CastAwayCore.gameManager.getBlock(vector);
         if (actionBlock != null) {
             // finally remove it
            if (CastAwayCore.gameManager.unRegisterSingleBlock(actionBlock)) {
                PlayerUtils.sendSuccess(player, CastAwayCore.NAME, "Der Block wurde entfernt.");
            } else {
                PlayerUtils.sendError(player, CastAwayCore.NAME, "Block konnte nicht entfernt werden.");
            }
         } else {
             PlayerUtils.sendError(player, CastAwayCore.NAME, "Dieser Block ist kein ActionBlock.");
         }
     }
     private void handleEditHallOfFame(Block block, Player player, RegisterSelection selection, boolean isLeftClick) {
         if (block.getTypeId() != Material.WALL_SIGN.getId()) {
             PlayerUtils.sendError(player, CastAwayCore.NAME, "Du musst auf ein Schild klicken!");
             CastAwayCore.playerManager.removeRegisterMode(player.getName());
             return;
         }
         BlockVector vector = new BlockVector(block);
 
         if (isLeftClick) {
             // left-click -> register sign
             SingleSign tempSign = selection.getDungeon().getSignByVector(vector);
             if (tempSign == null) {
                 SingleSign sign = new SingleSign(selection.getDungeon(), vector, block.getData());
                 if (CastAwayCore.databaseManager.addSign(sign)) {
                     selection.getDungeon().registerSigns(sign);
                     int place = selection.getDungeon().getPlaceForSign(sign);
                     PlayerUtils.sendSuccess(player, CastAwayCore.NAME, "Schild wurde registriert! ( Platz: " + place + " )");
                     String[] lines = new String[4];
                     lines[0] = "---> " + place + " <---";
                     lines[1] = lines[2] = lines[3] = "";
                     sign.fillWithInformation(lines);
 
                     // SEND UPDATE FOR CLIENTS => NEED HELP OF ORIGINAL
                     // MC-SERVERSOFTWARE
                     CraftPlayer cPlayer = (CraftPlayer) player;
                     Packet130UpdateSign signPacket = null;
                     signPacket = new Packet130UpdateSign(block.getX(), block.getY(), block.getZ(), lines);
                     cPlayer.getHandle().netServerHandler.sendPacket(signPacket);
                 } else {
                     PlayerUtils.sendError(player, CastAwayCore.NAME, "Schild konnte nicht registriert werden!");
                 }
             } else {
                 PlayerUtils.sendError(player, CastAwayCore.NAME, "Dieses Schild ist schon registriert! ( Platz: " + selection.getDungeon().getPlaceForSign(tempSign) + " )");
             }
         } else {
             // right-click -> unregister sign
             SingleSign tempSign = selection.getDungeon().getSignByVector(vector);
             if (tempSign != null) {
                 SingleSign sign = new SingleSign(selection.getDungeon(), vector, block.getData());
                 if (CastAwayCore.databaseManager.deleteSingleSign(sign)) {
                     selection.getDungeon().unRegisterSigns(vector);
                     block.setTypeId(0, true);
                     PlayerUtils.sendSuccess(player, CastAwayCore.NAME, "Schild wurde entfernt!");
                 } else {
                     PlayerUtils.sendError(player, CastAwayCore.NAME, "Schild konnte nicht entfernt werden!");
                 }
             } else {
                 PlayerUtils.sendError(player, CastAwayCore.NAME, "Dieses Schild ist nicht registriert!");
             }
         }
     }
 
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
         if (selection.getAction() == RegisterAction.ADD_BLOCK) {
             // register block
             this.handleAddBlock(event.getClickedBlock(), player, selection);
         } else if (selection.getAction() == RegisterAction.HALL_OF_FAME) {
             // register / unregister Signs
             this.handleEditHallOfFame(event.getClickedBlock(), player, selection, event.getAction() == Action.LEFT_CLICK_BLOCK);
         } else if (selection.getAction() == RegisterAction.REMOVE_BLOCK) {
             // unregister block
             this.handleRemoveBlock(event.getClickedBlock(), player, selection);
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
