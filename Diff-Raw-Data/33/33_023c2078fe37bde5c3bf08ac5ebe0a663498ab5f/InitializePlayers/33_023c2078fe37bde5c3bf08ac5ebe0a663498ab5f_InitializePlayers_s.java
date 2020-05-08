 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest.action;
 
 import com.mahn42.anhalter42.quest.PlayerPosition;
 import com.mahn42.anhalter42.quest.QuestInventory;
 import com.mahn42.framework.BlockPosition;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  *
  * @author andre
  */
 public class InitializePlayers extends Action {
     @Override
     public void execute() {
         int lIndex = 0;
         if (quest.playerPositions.size() > 0) {
             for(String lPlayerName : quest.players) {
                 if (lIndex >= quest.playerPositions.size()) {
                     lIndex = 0;
                 }
                 Player lPlayer = quest.getPlayer(lPlayerName);
                 PlayerPosition lPPos = quest.playerPositions.get(lIndex);
                 BlockPosition lPos = new BlockPosition(lPPos.pos);
                 lPos.add(quest.edge1);
                 lPlayer.teleport(lPos.getLocation(quest.world), PlayerTeleportEvent.TeleportCause.PLUGIN);
                 quest.log(type + ": teleport player " + (lIndex+1) + " to " + lPPos.pos);
                 lIndex++;
             }
         }
         lIndex = 0;
         for(String lPlayerName : quest.players) {
             QuestInventory lInv = quest.inventories.get(Integer.toString(lIndex+1));
             if (lInv != null) {
                 lIndex = 0;
                 lInv = quest.inventories.get(Integer.toString(lIndex+1));
             }
             if (lInv != null) {
                 Player lPlayer = quest.getPlayer(lPlayerName);
                 PlayerInventory lPI = lPlayer.getInventory();
                 lPI.clear();
                 lInv.toInventory(lPI);
                 quest.log(type + ": change player inventory for " + (lIndex+1));
             } else {
                 break;
             }
             lIndex++;
         }
     }
 }
