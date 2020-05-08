 /**
  * A subclass of Tile representing a river
  **/
 public class RiverTile extends Tile {
 
     /**
      * A constructor, just calls the super constructor
      **/
     public RiverTile() {
         super("river");
     }
 
     /**
      * Collect resources from this tile
      **/
     public void collectResources() {
         if (hasMule) {
             if (muleType.equals("FoodMule")) {
                 owner.changeFood(4);
             }
             else if (muleType.equals("EnergyMule")) {
                 owner.changeEnergy(2);
             }
         }
     }
 
     /**
      * return a string representing the image string
      *
      * @return always null - uses the background of the map
     public String image() {
         return null;
     }
 }
 
