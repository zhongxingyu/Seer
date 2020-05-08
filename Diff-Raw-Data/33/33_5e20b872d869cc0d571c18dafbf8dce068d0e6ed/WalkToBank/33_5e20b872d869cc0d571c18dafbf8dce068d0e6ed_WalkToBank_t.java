 

 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.tab.Inventory;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 /**
  *
  * @author PimGame
  */
 public class WalkToBank extends Node {
 
     /*
      * public final Tile[] path = { new Tile(3273, 3187, 0), new Tile(3278,
      * 3186, 0), new Tile(3279, 3181, 0), new Tile(3277, 3176, 0), new
      * Tile(3274, 3170, 0), new Tile(3268, 3168, 0) };
      */
     @Override
     public String toString() {
         return "Walking to bank...";
     }
 
     @Override
     public boolean activate() {
         return (!Banking.playerAtBank() && Inventory.getCount(Variables.INV_FILTER) == 0) || (Smelter.msg.equals("Your Ring of Forging has melted.") && Smelter.onDepletion.equals("Walk to bank"));
     }
 
     @Override
     public void execute() {
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
             Walking.newTilePath(WalkToFurnace.path).reverse().traverse();
 
         }
     
 }
