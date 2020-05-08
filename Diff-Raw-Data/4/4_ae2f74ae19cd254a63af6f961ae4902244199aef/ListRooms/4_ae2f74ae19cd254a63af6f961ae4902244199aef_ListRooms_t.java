 package edu.drexel.cs544.mcmuc.actions;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONObject;
 
 import edu.drexel.cs544.mcmuc.Channel;
 import edu.drexel.cs544.mcmuc.Controller;
 
 /**
  * The list-rooms action is used by clients to update channel use information.
  * If the optional collection of rooms is not included, this signals that the
  * client wants to know about all channels in use. If a collection of rooms is
  * provided, the client wishes to know the status of just those channels.
  * 
  * The JSON format of a ListRooms is either
  * {'uid':'<uid>','action':'list-rooms','rooms':'<rooms>'} or
  * {'uid':'<uid>','action':'list-rooms'}
  */
 public class ListRooms extends RoomAction {
 
     public static final String action = "list-rooms";
 
     public ListRooms(List<Integer> rooms) {
         super(rooms, ListRooms.action);
     }
 
     /**
      * Allows ListRooms to be created without a list of rooms - the only such
      * RoomAction child allowed to.
      */
     public ListRooms() {
         super(ListRooms.action);
     }
 
     public ListRooms(JSONObject json) {
         super(json, ListRooms.action);
     }
 
     @Override
     public void process(Channel channel) {
         class Runner implements Runnable {
             ListRooms message;
 
             Runner(ListRooms m) {
                 message = m;
             }
 
             public void run() {
             	Set<Integer> roomsInUse = Controller.getInstance().portsInUse;
                 roomsInUse.retainAll(message.getRooms());
                 if (!roomsInUse.isEmpty()) {
                    UseRooms useReply = new UseRooms(new ArrayList<Integer>(roomsInUse));
                     Controller.getInstance().send(useReply);
                 }
             }
         }
         Thread t = new Thread(new Runner(this));
         t.start();
     }
 }
