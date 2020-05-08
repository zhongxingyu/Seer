 package cs2114.tiletraveler;
 
 // -------------------------------------------------------------------------
 /**
  *  A stage for the application
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards  (MrZchuck)
  * @author Jacob Stenzel  (sjacob95)
  * @version 2013.12.08
  */
 public class Stage6
     extends Stage
 {
 
     private static Map map = new Map(
                                "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW",
                                "WOOOOOOOOIOOOOOOOOIOOOOOIOOOOOOOIIOOOOOOOOOW",
                                "WOOOOOOOOIOOOOIOOOOOOOOOOOOIIIIOOOOOOOOOOOOW",
                                "WOOOOIOOOOOOOOOOOOIOOOOIOOOOOOOIOOOOOOOOOOOW",
                                "WOOOWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWOOOW",
                                "WOOOW                                  WOOOW",
                                "WOOOW   WWWWWWWWWWWWWWWWWWWWWWWWWWWWW  WOOOW",
                                "WOOOW   W~~Q~Q~~~Q~~~Q~~~Q~~~Q~QOOOOW  WOOOW",
                                "WOOOW   DQ~Q~Q~~~Q~~~Q~Q~Q~Q~Q~QOOOOW  WOOOW",
                                "WOOOW   W~~~~Q~Q~Q~Q~Q~Q~~~Q~Q~QOOOOW  WOOOW",
                                "WOOOW   WWWWWWWWWWWWWWWWWWWWWWWWWOOOW  WOOOW",
                                "WOOOWWWWWWWWWWWWWWWWWWWWWWWWWWWWWOOOW  WOOOW",
                                "WOOOOO  O O OO O   O O  O    OOO   OW  WOOOW",
                                "WOOOOO OO O  O O   O OO OOO  O OO OOW  WOOOW",
                                "WOOOOO  O O  O OO OO O  O OOOO      W  WOOOW",
                                "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW  WOOOW",
                                "                                       WOOOW",
                                "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWOOOW",
                                "WOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOW",
                                "WOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOW",
                                "WOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOW",
                                "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
 
     private static Location startLoc = new Location(1, 2);
 
     // ----------------------------------------------------------
     /**
      * Create a new Stage1 object.
      * @param tileSize
      *            The size of one Tile in pixels.
      */
     public Stage6(float tileSize)
     {
         super(map, startLoc);
        getEnemyMap().addEnemy(new Bug(tileSize, this, new Location(1, 7), new Location(1, 18)));
        getEnemyMap().addEnemy(new Bug(tileSize, this, new Location(2, 13), new Location(2, 26)));
        getEnemyMap().addEnemy(new Bug(tileSize, this, new Location(3, 21), new Location(3, 30)));
     }
 
     public Stage reset(float tileSize)
     {
         return new Stage6(tileSize);
     }
 }
