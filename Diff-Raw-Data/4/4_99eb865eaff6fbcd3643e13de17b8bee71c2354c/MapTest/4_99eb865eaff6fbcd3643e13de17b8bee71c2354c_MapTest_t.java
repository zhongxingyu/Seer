 package cs2114.tiletraveler;
 
 import student.TestCase;
 
 /**
  * // -------------------------------------------------------------------------
 /**
  *  Tester for the Map class
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards  (MrZchuck)
  * @author Jacob Stenzel  (sjacob95)
  * @version 2013.12.08
  */
 public class MapTest
     extends TestCase
 {
     private Map map1;
     private Map map2;
     private Map map3;
 /**
  * Sets up the map for testing.
  */
     public void setUp()
     {
         map1 = new Map(5);
         map1.setTile(0, 0, Tile.FLOOR);
         map1.setTile(1, 1, Tile.WALL);
         map1.setTile(2, 2, Tile.PILLAR);
         map1.setTile(3, 1, Tile.LILY);
         map1.setTile(4, 0, Tile.WALL);
         map1.setTile(2, 4, Tile.WATER);
 
         map2 = new Map("  ~  ",
                        "     ",
                        "  I  ",
                        " W Q ",
                        "O   W");
 
         map3 = new Map("  ",
                        "~~",
                        "OO");
     }
 
     /**
      * Tests the getTile() method
      */
     public void testGetTile()
     {
         assertEquals(map1.getTile(0, 0), Tile.FLOOR);
         assertEquals(map1.getTile(1, 1), Tile.WALL);
         assertEquals(map1.getTile(2, 2), Tile.PILLAR);
         assertEquals(map1.getTile(3, 1), Tile.LILY);
         assertEquals(map1.getTile(4, 0), Tile.WALL);
         assertEquals(map1.getTile(2, 4), Tile.WATER);
         assertEquals(map1.getTile(0, 2), Tile.EMPTY);
         assertEquals(map1.getTile(42, 42), Tile.INVALID);
         Tile[][] tiles = map1.get();
         assertEquals(tiles[0][0], Tile.FLOOR);
         assertEquals(map1.getTile(new Location(3, 1)), Tile.LILY);
         assertEquals(map1.getTile(new Location(2, 143)), Tile.INVALID);
         map1.setTile(new Location(0, 0), Tile.DOOR);
         assertEquals(map1.getTile(0, 0), Tile.DOOR);
         map1.setTile(0, 0, 'Q');
         assertEquals(map1.getTile(0, 0), Tile.LILY);
         map1.setTile(new Location(0, 0), 'O');
         assertEquals(map1.getTile(0, 0), Tile.FLOOR);
         map1.toString();
         assertFalse(map1.equals(new Location(0, 0)));
        map1.equals(map2);
        map1.equals(map3);
 
         assertEquals(Map.convertToChar(Tile.FLOOR), 'O');
         assertEquals(Map.convertToChar(Tile.DOOR), 'D');
         assertEquals(Map.convertToChar(Tile.PILLAR), 'I');
         assertEquals(Map.convertToChar(Tile.LILY), 'Q');
         assertEquals(Map.convertToChar(Tile.WALL), 'W');
         assertEquals(Map.convertToChar(Tile.WATER), '~');
         assertEquals(Map.convertToChar(Tile.EMPTY), ' ');
 
         setUp();
     }
 
     /**
      * Tests the secondary constructor
      */
     public void testSecondaryConstructor()
     {
         assertEquals(map1.getTile(0, 0), Tile.FLOOR);
         assertEquals(map1.getTile(1, 1), Tile.WALL);
         assertEquals(map1.getTile(2, 2), Tile.PILLAR);
         assertEquals(map1.getTile(3, 1), Tile.LILY);
         assertEquals(map1.getTile(4, 0), Tile.WALL);
         assertEquals(map1.getTile(2, 4), Tile.WATER);
         assertEquals(map1.getTile(0, 2), Tile.EMPTY);
         assertEquals(map1.getTile(42, 42), Tile.INVALID);
 
         assertEquals(map1, map2);
     }
 
 }
