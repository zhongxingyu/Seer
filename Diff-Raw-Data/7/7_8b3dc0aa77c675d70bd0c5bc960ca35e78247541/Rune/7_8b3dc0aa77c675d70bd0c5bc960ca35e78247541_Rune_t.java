 package com.monkeyinabucket.bukkit.blink.rune;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 /**
  * General Rune class. This class could eventually be the base class for other Runes.
  * @author Donald Tyler (chekote69@gmail.com)
  */
 public class Rune {
 
   /**
    * Determines if the specified Block is the center of a rune (has a "Rune Shell"). Currently a
    * rune shell is defined as the obsidian shell of a BlinkRune: (X == Obsidian)
    * 
    *  X X X X X
    *  X X   X X
    *  X   X   X
    *  X X X X X
    *  X X   X X
    *  X X X X X
    * 
    * @param center the Block to check
    * @return true if the Block has a rune shell, false if not
    */
   public static boolean hasRuneShell(Block center) {
     // most of the time, the block clicked is not obsidian (which the center of a rune always is)
     // so for performance reasons, we check this first.
     if (center.getType() != Material.OBSIDIAN) {
       return false;
     }
 
     Block topLeft = center.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.WEST, 2);
 
     // loop over the columns and rows, checking for obsidian
     // note: the first row is row 0, not row 1. same for columns.
     for (int col = 0; col < 5; ++col) {
       for (int row = 0; row < 5; ++row) {
         Block block = topLeft.getRelative(BlockFace.EAST, col).getRelative(BlockFace.SOUTH, row);
 
        // column 3, row 2 & 4 and , column 2 & 4, cannot be obsidian
         if ((col == 2 && (row == 1 || row == 3)) || row == 2 && (col == 1 || col == 3)) {
          if (block.getType() == Material.OBSIDIAN) {
            return false;
          }

           continue;
         }
 
         if (block.getType() != Material.OBSIDIAN) {
           return false;
         }
       }
     }
 
     return true;
   }
 }
