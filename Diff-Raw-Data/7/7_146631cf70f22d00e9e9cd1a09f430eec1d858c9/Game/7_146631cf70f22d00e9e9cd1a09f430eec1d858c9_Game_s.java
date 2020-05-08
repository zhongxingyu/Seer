 package zuul;
 
 import java.io.IOException;
 import Builders.*;
 import View.View;
 
 /**
  * This class is the main class of the "World of Zuul" application.
  * "World of Zuul" is a very simple, text based adventure game. Users can walk
  * around some scenery. That's all. It should really be extended to make it more
  * interesting! To play this game, invoke the static main method with no string
  * parameters. This main class creates and initialises all the others: it
  * creates all rooms, creates the parser and starts the game. It also evaluates
  * and executes the commands that the parser returns.
  * 
  * @author Vinayak Bansal
  * @version 2012.10.22
  * @param <ItemBuilder>
  * @param <ItemBuilder>
  */
 
 public class Game {
 
   public static final String KEY = "Key";
   public static final String FLAMETHROWER = "Flamethrower";
   public static final String BOSS2 = "Boss";
   public static final String CLAWS = "Claws";
   public static final String MAP = "Map";
   public static final String ALIEN2 = "Alien";
   public static final String PEAR = "Pear";
   public static final String ORANGE = "Orange";
   public static final String APPLE = "Apple";
   public static final String SWORD = "Sword";
   public static final String NORTH_ROOM2 = "NorthRoom2";
   public static final String NORTH_WEST_ROOM = "NorthWestRoom";
   public static final String WEST_ROOM = "WestRoom";
   public static final String EAST_ROOM = "EastRoom";
   public static final String SOUTH_ROOM = "SouthRoom";
   public static final String BREAD = "Bread";
   public static final String NORTH_ROOM1 = "NorthRoom1";
   public static final String START_ROOM = "StartRoom";
   
 
   /**
    * Initialization method, to set up the game. Sets up the rooms, the monsters
    * and the items in the game.
    * @param <ItemBuilder>
    * @param <MapBuilder>
    * @param <RoomBuilder>
    */
   public static Room initialize(Builder b) {
     ItemBuilder ib = b.getItemBuilder();
     MonsterBuilder mb = b.getMonsterBuilder();
     RoomBuilder rb = b.getRoomBuilder();
     
     //Old implementation as a default if builder not used
     if(ib == null || mb == null || rb == null) {
       Room startRoom = new Room(START_ROOM);
       Room northRoom1 = new Room(NORTH_ROOM1);
       northRoom1.addItem(new Item(BREAD, 30, 0, false));
       Room southRoom = new Room(SOUTH_ROOM);
       Room eastRoom = new Room(EAST_ROOM);
       Room westRoom = new Room(WEST_ROOM);
       Room northWestRoom = new Room(NORTH_WEST_ROOM);
       Room northRoom2 = new Room(NORTH_ROOM2);
       startRoom.addItem(new Item(SWORD, 50, 0, true));
       startRoom.setExit(Direction.NORTH, northRoom1);
       startRoom.setExit(Direction.SOUTH, southRoom);
       startRoom.setExit(Direction.EAST, eastRoom);
       startRoom.setExit(Direction.WEST, westRoom);
       westRoom.addItem(new Item(APPLE, 10, 0, false));
       westRoom.addItem(new Item(ORANGE, 15, 0, false));
       westRoom.addItem(new Item(PEAR, 20, 0, false));
 
       eastRoom.setExit(Direction.WEST, startRoom);
 
       Monster alien = new Monster(Humanoid.MAX_HEALTH, Monster.DEFAULT_LEVEL,
           ALIEN2, eastRoom);
       eastRoom.addMonster(alien);
       alien.addItem(new Item(MAP, 0, 0, true));
       alien.addItem(new Item(CLAWS, 10, 0, true));
 
       Monster boss = new Monster(100, 2, BOSS2, southRoom);
       southRoom.addMonster(boss);
       boss.addItem(new Item(FLAMETHROWER, 30, 0, true));
       boss.addItem(new Item(KEY, 0, 0, true));
 
       westRoom.setExit(Direction.EAST, startRoom);
       northRoom1.setExit(Direction.SOUTH, startRoom);
       northRoom1.setExit(Direction.NORTH, northRoom2);
       southRoom.setExit(Direction.NORTH, startRoom);
 
       northRoom2.setExit(Direction.SOUTH, northRoom1);
       northRoom2.setExit(Direction.WEST, northWestRoom);
       northWestRoom.setExit(Direction.EAST, northRoom2);
       northWestRoom.setLocked(true);
       northWestRoom.addItem(new Item("Treasure", 100, 0, true));
       return startRoom;
     }
     else {
       Room[] rooms = ib.getRooms();
       boolean[] roomStatuses = rb.getRooms();
       Room[] monsterRooms = mb.getRooms();
       
       Room startRoom = null;
       Room currentRoom = null;
       for(int i = 0; i < 16; i++) {
         if(roomStatuses[i] == true) {
           currentRoom = rooms[i];
           if(monsterRooms[i].hasMonsters() && rooms[i].getItems().contains(new Item("Treasure", 100, 0, true)) == false) currentRoom.addMonster(monsterRooms[i].getMonster());
           if(hasNeighbour(i, Direction.NORTH) && roomStatuses[i-4] == true) currentRoom.setExit(Direction.NORTH, rooms[i-4]);
           if(hasNeighbour(i, Direction.EAST) && roomStatuses[i+1] == true) currentRoom.setExit(Direction.EAST, rooms[i+1]);
           if(hasNeighbour(i, Direction.SOUTH) && roomStatuses[i+4] == true) currentRoom.setExit(Direction.SOUTH, rooms[i+4]);
           if(hasNeighbour(i, Direction.WEST) && roomStatuses[i-1] == true) currentRoom.setExit(Direction.WEST, rooms[i-1]);
         }
         if(i == 9) startRoom = currentRoom;
       }
       return startRoom;
     }
   }
   
   public static boolean hasNeighbour(int id, Direction d) {
     switch(id) {
       case 0:
         if(d.equals(Direction.NORTH)) return false;
         if(d.equals(Direction.WEST)) return false;
         return true;
       case 1:
         if(d.equals(Direction.NORTH)) return false;
         return true;
       case 2:
         if(d.equals(Direction.NORTH)) return false;
         return true;
       case 3:
         if(d.equals(Direction.NORTH)) return false;
         if(d.equals(Direction.EAST)) return false;
         return true;
       case 4:
         if(d.equals(Direction.WEST)) return false;
         return true;
       case 5:
         return true;
       case 6:
         return true;
       case 7:
         if(d.equals(Direction.EAST)) return false;
         return true;
       case 8:
         if(d.equals(Direction.WEST)) return false;
         return true;
       case 9:
         return true;
       case 10:
         return true;
       case 11:
         if(d.equals(Direction.EAST)) return false;
         return true;
       case 12:
         if(d.equals(Direction.SOUTH)) return false;
         if(d.equals(Direction.WEST)) return false;
         return true;
       case 13:
         if(d.equals(Direction.SOUTH)) return false;
         return true;
       case 14:
         if(d.equals(Direction.SOUTH)) return false;
         return true;
       case 15:
         if(d.equals(Direction.EAST)) return false;
         if(d.equals(Direction.SOUTH)) return false;
         return true;
     }
     return false;
   }
 
   public static void main(String[] args) throws IOException {
	  View view = View.getInstance(new Builder());
    view.update();
    view.setVisible(true);
   }
 
 }
