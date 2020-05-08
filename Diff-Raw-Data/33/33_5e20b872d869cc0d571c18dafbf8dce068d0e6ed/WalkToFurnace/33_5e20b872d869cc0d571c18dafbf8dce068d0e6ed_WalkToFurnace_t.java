 

 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.Tile;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 /**
  *
  * @author PimGame
  */
 public class WalkToFurnace extends Node {
 
     public static final Tile[] path = {
         new Tile(Random.nextInt(3269, 3271), Random.nextInt(3167, 3169), 0),
         new Tile(Random.nextInt(3274, 3278), Random.nextInt(3180, 3183), 0),
         new Tile(Random.nextInt(3272, 3274), Random.nextInt(3189, 3191), 0)
     };
 
     @Override
     public String toString() {
         return "Walking to furnace...";
     }
 
     @Override
     public boolean activate() {
         return !Smelting.playerAtFurnace() && Inventory.getCount() > 0 && Banking.playerAtBank(); //&& (Inventory.getCount(Variables.SELECTED_ORE) || Inventory.contains(Variables.TIN_ID) || Inventory.contains(Variables.COPPER_ID));
     }
 
     @Override
     public void execute() {
         int i = 1;
         while (!Smelting.playerAtFurnace()) {
             if (Walking.getEnergy() >= 15) {
                 Walking.setRun(true);
             }
             if (Smelter.rest == true) {
                 if (Walking.getEnergy() < Smelter.restpercentage) {
                     Widgets.get(750).getChild(2).interact("Rest");
                     while (Walking.getEnergy() < Smelter.restpercentage) {
                         sleep(500);
                     }
                 }
             }
 
 
             Walking.walk(path[i]);
             while (path[i].distanceTo() > 5) {
                 sleep(500);
             }
             i++;
 
         }
     }
 }
