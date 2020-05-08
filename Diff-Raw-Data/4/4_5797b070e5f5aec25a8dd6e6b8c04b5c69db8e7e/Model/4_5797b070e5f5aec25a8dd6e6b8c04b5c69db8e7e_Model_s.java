 package delightex.server.model;
 
 import delightex.client.model.Room;
 
 import java.util.*;
 
 public class Model {
   private final static long MAX_LIVE_TIME = 1000 * 60 * 30; //30 mins
 
   private Map<String, RoomContainer> myRooms = new HashMap<String, RoomContainer>();
 
   private Map<String, RoomContainer> getRooms() {
     for (RoomContainer roomContainer : new ArrayList<RoomContainer>(myRooms.values())) {
       Room room = roomContainer.getRoom();
       if (room.getStamp() < System.currentTimeMillis() - MAX_LIVE_TIME) {
         myRooms.remove(room.getName());
       }
     }
     return myRooms;
   }
 
   public void addRoom(String name) {
     Room room = new Room(name);
     getRooms().put(name, new RoomContainer(room));
   }
 
   public RoomContainer getRoom(String name) {
     return getRooms().get(name);
   }
 
   public Set<String> getRoomNames() {
    return getRooms().keySet();
   }
 }
 
