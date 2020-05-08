 /**
  * A subclass of tile representing the town
  **/
 public class TownTile extends Tile {
 
     /**
      * A constructor that just calls the super constructor
      **/
     public TownTile() {
         super("town");
     }
 
     /**
      * Give the provided player the resources for the tile, which is known for the town
      *
      * @param player the player to whom the resources will be added
      **/
     public void collectResources(Player player) {
     	
     }
 
     /**
      * Return the image string for the tile
      *
      * @return null, just use the background of the map
     **/
     public String image() {
         return null;
     }
 }
