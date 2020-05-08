 package mud.geography;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import mud.AreaManager;
import mud.NPC;
import mud.Player;
 
 /**
  * Represents a room for an area of the MUD.
  *
  * @author Japhez
  */
 public class Room implements Serializable {
 
     private Area area;
     private int roomID;
     private String name;
     private String description;
     private Room north;
     private Room east;
     private Room south;
     private Room west;
     private Room up;
     private Room down;
     //private HashMap<String, Room> hiddenExits; //TODO: Milestone #2
     //private ArrayList<Item> items; //TODO: Milestone #2
     private ArrayList<NPC> NPCs;
     private ArrayList<Player> players;
 
     public Room(Area area, AreaManager areaManger) {
         //Set the area
         this.area = area;
         //Get a unique room ID
         this.roomID = areaManger.getUniqueRoomID();
         //Set a default name
         name = "Unnamed Room";
         //Set a default description
         description = "You're in a nondescript room.";
         //Initialize player list
         players = new ArrayList<>();
         //Initialize NPC list
         NPCs = new ArrayList<>();
         //Add the room to the area
         area.addRoom(this);
         //Add the room to the global list
         areaManger.addRoom(this);
     }
 
     /**
      * Links this room through the passed direction to the passed room. They are
      * linked both ways.
      *
      * @param direction
      * @param room
      * @return true if successful, false if not
      */
     public boolean linkToRoom(Direction direction, Room room) {
         //If either room is already linked with the necessary direction, return false
         Room roomInDirection = getRoomInDirection(direction);
         if (roomInDirection != null) {
             return false;
         }
         if (room.getRoomInDirection(Direction.getOppositeDirection(direction)) != null) {
             return false;
         } else {
             //Link this room to that
             setRoomInDirection(direction, room);
             //Link that room to this
             room.setRoomInDirection(Direction.getOppositeDirection(direction), this);
             return true;
         }
     }
 
     /**
      * Unlinks the passed exists from this room and the connected rooms.
      *
      * @param exits the exits to unlink
      * @param room the room whose exits to unlink
      */
     public void unlinkExits(Direction[] exits) {
         //For each direction
         for (Direction d : exits) {
             //If the direction isn't null
             if (getRoomInDirection(d) != null) {
                 //Unlink the linked room's connection
                 getRoomInDirection(d).setRoomInDirection(Direction.getOppositeDirection(d), null);
                 //Unlink this direction
                 setRoomInDirection(d, null);
             }
         }
     }
 
     public Area getArea() {
         return area;
     }
 
     public synchronized void addNPC(NPC character) {
         NPCs.add(character);
     }
 
     public synchronized void addPlayer(Player player) {
         players.add(player);
     }
 
     public ArrayList<NPC> getNPCs() {
         return NPCs;
     }
 
     public synchronized ArrayList<Player> getPlayers() {
         return players;
     }
 
     public int getRoomID() {
         return roomID;
     }
 
     public String getName() {
         return name;
     }
 
     public synchronized void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public synchronized void setDescription(String description) {
         this.description = description;
     }
 
     public synchronized void removePlayer(Player player) {
         players.remove(player);
     }
 
     public synchronized void removeNPC(NPC character) {
         NPCs.remove(character);
     }
 
     /**
      * Returns whatever room is in the given direction, or null if there is no
      * room there.
      *
      * @param direction
      * @return the room, or null if there is no room
      */
     public synchronized Room getRoomInDirection(Direction direction) {
         if (direction.equals(Direction.NORTH)) {
             return north;
         }
         if (direction.equals(Direction.EAST)) {
             return east;
         }
         if (direction.equals(Direction.SOUTH)) {
             return south;
         }
         if (direction.equals(Direction.WEST)) {
             return west;
         }
         if (direction.equals(Direction.UP)) {
             return up;
         }
         if (direction.equals(Direction.DOWN)) {
             return down;
         }
         return null;
     }
 
     /**
      * @return the exists to this room in a formatted string, or null if there
      * are no exits
      */
     public synchronized String getFormattedExits() {
         if (north == null && east == null && south == null && west == null && up == null && down == null) {
             return "You're trapped!  You can't see any exits!";
         }
         String exits = "";
         if (north != null) {
             exits += "| North ";
         }
         if (east != null) {
             exits += "| East ";
         }
         if (south != null) {
             exits += "| South ";
         }
         if (west != null) {
             exits += "| West ";
         }
         if (up != null) {
             exits += "| Up ";
         }
         if (down != null) {
             exits += "| Down ";
         }
         //Add last pipe
         exits += "|";
         return exits;
     }
 
     /**
      * Sets the exit in the given direction to the given room.
      *
      * @param direction
      * @param newRoom
      */
     public synchronized void setRoomInDirection(Direction direction, Room newRoom) {
         if (direction.equals(Direction.NORTH)) {
             north = newRoom;
         }
         if (direction.equals(Direction.EAST)) {
             east = newRoom;
         }
         if (direction.equals(Direction.SOUTH)) {
             south = newRoom;
         }
         if (direction.equals(Direction.WEST)) {
             west = newRoom;
         }
         if (direction.equals(Direction.UP)) {
             up = newRoom;
         }
         if (direction.equals(Direction.DOWN)) {
             down = newRoom;
         }
     }
 
     /**
      * Sends the passed message to all players in the room.
      *
      * @param message
      */
     public void sendMessageToRoom(String message) {
         for (Player p : players) {
             p.sendMessage(message);
         }
     }
 }
