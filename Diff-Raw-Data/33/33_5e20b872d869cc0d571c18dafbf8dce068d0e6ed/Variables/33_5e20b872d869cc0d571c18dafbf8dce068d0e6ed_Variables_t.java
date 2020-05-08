 

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import org.powerbot.game.api.util.Filter;
 import org.powerbot.game.api.wrappers.node.Item;
 
 /**
  *
  * @author PimGame
  */
 public class Variables {
 
     public static String bar = "";
     public static int COAL_IDS = 453;
     public static int[] ALL_ORES = {453, 25630, 438, 436, 440, 20903, 447, 449, 20915, 451, 444, 20904, 442};
     public static int TIN_ID = 438;
     public static int COPPER_ID = 436;
     public static int FURNACE = 76293;
     public static int BANK = 76274;
     public static int RING_OF_FORGING = 2568;
     public static int[] BAR_WIDGETS = {2, 10, 18, 30, 34, 58, 22, 14};
     public static int SELECTED_ORE = 999999;
     public static Filter<Item> INV_FILTER = new Filter<Item>() {
 
         @Override
         public boolean accept(Item t) {
             for (int i : ALL_ORES) {
                 if (t.getId() == i) {
                     return true;
                 }
             }
             return false;
         }
     };
     public static int oresAmount = 0;
 }
