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
 
 package de.minestar.castaway.manager;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import de.minestar.castaway.blocks.AbstractActionBlock;
 import de.minestar.castaway.core.CastAwayCore;
 import de.minestar.castaway.data.BlockVector;
 import de.minestar.castaway.data.Dungeon;
 
 public class GameManager {
     private Map<BlockVector, AbstractActionBlock> blockMap;
 
     public void init() {
         blockMap = new HashMap<BlockVector, AbstractActionBlock>();
         // REGISTER ALL ACTION BLOCKS
         Collection<Dungeon> dungeons = CastAwayCore.dungeonManager.getDungeons();
         for (Dungeon dungeon : dungeons)
             blockMap.putAll(dungeon.getRegisteredBlocks());
     }
 
     public AbstractActionBlock getBlock(BlockVector vector) {
         return this.blockMap.get(vector);
     }
 
     public void registerSingleBlock(AbstractActionBlock block) {
         if (this.getBlock(block.getVector()) == null) {
             this.blockMap.put(block.getVector().clone(), block);
             block.getDungeon().registerBlocks(block);
             CastAwayCore.databaseManager.addActionBlock(block);
         }
     }
 
    public boolean unRegisterSingleBlock(AbstractActionBlock block) {
         if (this.getBlock(block.getVector()) != null) {
             blockMap.remove(block.getVector());
             block.getDungeon().unRegisterBlocks(block.getVector());
            return CastAwayCore.databaseManager.deleteSingleRegisteredBlock(block);
         }
        return false;
     }
 
     public void unRegisterBlocks(Collection<AbstractActionBlock> actionBlocks) {
         for (AbstractActionBlock actionBlock : actionBlocks) {
             if (this.getBlock(actionBlock.getVector()) != null) {
                 blockMap.remove(actionBlock.getVector());
                 actionBlock.getDungeon().unRegisterBlocks(actionBlock.getVector());
                 CastAwayCore.databaseManager.deleteSingleRegisteredBlock(actionBlock);
             }
         }
     }
 }
