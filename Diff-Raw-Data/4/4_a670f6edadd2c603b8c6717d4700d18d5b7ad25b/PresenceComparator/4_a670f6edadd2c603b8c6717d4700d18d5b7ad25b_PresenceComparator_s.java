 package directi.androidteam.training.chatclient.Roster;
 
 import java.util.Comparator;
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rajat
  * Date: 10/11/12
  * Time: 2:21 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PresenceComparator implements Comparator<RosterItem> {
     private HashMap<String, Integer> presencePriorityMap;
 
     public PresenceComparator() {
         this.presencePriorityMap = new HashMap<String, Integer>();
         this.presencePriorityMap.put("chat", 1);
         this.presencePriorityMap.put("dnd", 2);
         this.presencePriorityMap.put("away", 3);
         this.presencePriorityMap.put("xa", 3);
         this.presencePriorityMap.put("unavailable", 4);
     }
 
     @Override
     public int compare(RosterItem rosterItem1, RosterItem rosterItem2) {
         return this.presencePriorityMap.get(rosterItem1.getPresence()).compareTo(this.presencePriorityMap.get(rosterItem2.getPresence()));
     }
 }
