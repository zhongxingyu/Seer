 package com.github.mat8913.LegitMinecraft;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 
 public class BlockBreakListener implements Listener {
   //What tool you need to destroy these blocks
   //Not handled: 0, 7, 8, 9, 10, 11, 34, 36, 51, 90, 119, 122
   private final int[] pick = {1, 4, 14, 15, 16, 20, 21, 22, 23, 24, 41, 42, 43, 44, 45, 48, 49, 52, 56, 57, 61, 62, 67, 69, 70, 71, 73, 74, 77, 79, 82, 87, 89, 93, 94, 97, 98, 102, 101, 108, 109, 112, 113, 114, 116, 117, 118, 120, 121, 129, 130, 133};
   private final int[] axe = {5, 17, 25, 26, 27, 28, 29, 33, 35, 47, 50, 53, 54, 58, 63, 64, 65, 66, 68, 72, 75, 76, 84, 85, 92, 95, 96, 99, 100, 107, 125, 126, 131, 134, 135, 136};
   private final int[] shovel = {2, 3, 12, 13, 46, 55, 59, 60, 78, 80, 82, 88, 110};
   private final int[] shears = {6, 31, 32, 37, 38, 39, 40, 83, 104, 105, 106, 111, 115, 127, 132};
   private final int[] sword = {19, 30, 35, 81, 123, 124};
  private final int[] hoe = {86, 91, 103};
   //special 18
 
   private final int[] pick_tool = {270, 274, 257, 285, 278};
   private final int[] axe_tool = {271, 275, 258, 286, 279};
   private final int[] shovel_tool = {269, 273, 256, 284, 277};
   private final int[] shears_tool = {359};
   private final int[] sword_tool = {268, 272, 267, 283, 276};
   private final int[] hoe_tool = {290, 291, 292, 294, 293};
 
   /**
    * @param  blockIds  an array of blocks which require a tools to break
    * @param  toolIds   an array of tools which can successfully break the block
    * @param  block     the block which has been broken 
    * @param  tool      the tool which was used to break the block
    * @return           0 if the correct tool was used for the block, 1 if the incorrect tool was used for the block, 2 if the block broken doesn't apply to this tool.
    */
   public int correctTool(int[] blockIds, int[] toolIds, int block, int tool) {
     for (int i = 0; i < blockIds.length; i++) {
       if (block == blockIds[i]) {
         for (int x = 0; x < toolIds.length; x++) {
           if (tool == toolIds[x]) {
             x = pick_tool.length;
             return 0;
           }
         }
       return 1;
       }
     } 
     return 2;
   }
 
   @EventHandler(priority = EventPriority.HIGH)
   public void blockBreak(BlockBreakEvent event) {
     //FIXME: Not very efficient I would assume (but then again, Minecraft isn't very efficient)
     int block = event.getBlock().getTypeId();
     int tool = event.getPlayer().getItemInHand().getTypeId();
 
     int correct = correctTool(pick, pick_tool, block, tool);
     if (correct == 0)
       return;
     if (correct == 1) {
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need a pickaxe to break this block.");
       return;
     }
 
     correct = correctTool(axe, axe_tool, block, tool);
     if (correct == 0)
       return;
     if (correct == 1) {
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need an axe to break this block.");
     }
 
     correct = correctTool(shovel, shovel_tool, block, tool);
     if (correct == 0)
       return;
     if (correct == 1) {
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need a shovel to break this block.");
     }
 
     correct = correctTool(shears, shears_tool, block, tool);
     if (correct == 0)
       return;
     if (correct == 1) {
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need shears to break this block.");
     }
 
     correct = correctTool(sword, sword_tool, block, tool);
     if (correct == 0)
       return;
     if (correct == 1) {
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need a sword to break this block.");
     }
 
     correct = correctTool(hoe, hoe_tool, block, tool);
     if (correct == 0)
       return;
     if (correct == 1) {
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need a hoe to break this block.");
     }
 
     if (block == 18) {
       if (tool == shears_tool[0])
         return;
       for (int i = 0; i < shovel_tool.length; i++) {
         if (tool == shovel_tool[i])
           return;
       }
       event.setCancelled(true);
       event.getPlayer().sendMessage("You need shears or a shovel to break this block.");
    }
   }
 
 }
