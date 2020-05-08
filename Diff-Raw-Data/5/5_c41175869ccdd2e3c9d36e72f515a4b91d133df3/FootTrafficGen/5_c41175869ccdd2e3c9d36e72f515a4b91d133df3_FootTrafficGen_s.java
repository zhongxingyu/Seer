 import java.util.Random;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 public class FootTrafficGen {
     public static void main(String[] args) {        
         int EVENT_COUNT = Integer.parseInt(args[0]);
        System.out.println(EVENT_COUNT);
         int VISITOR_COUNT = Integer.parseInt(args[1]) + 1;
         int ROOM_COUNT = Integer.parseInt(args[2]) + 1;
         int START_TIME = Integer.parseInt(args[3]);
         int END_TIME = Integer.parseInt(args[4]);
         Random rand = new Random(System.nanoTime());
         
         int[] visitors = new int[VISITOR_COUNT];
         for (int i = 0; i < VISITOR_COUNT; i++) {
             visitors[i] = i;
         }        
 
        int countdown = EVENT_COUNT;
         while (countdown > 0) {
             Map<Integer, List<Visitor>> rooms = new HashMap<>();
             for (int visitorNum : visitors) {
                 int enterTime = rand.nextInt(END_TIME - START_TIME + 1) + START_TIME;
                 Visitor visitor = new Visitor(visitorNum, enterTime);
                 int roomNum = rand.nextInt(ROOM_COUNT);
                 if (rooms.containsKey(roomNum)) {
                     rooms.get(roomNum).add(visitor);
                 } else {                
                     rooms.put(roomNum, new ArrayList<Visitor>());
                     rooms.get(roomNum).add(visitor);
                 }   
                 System.out.println(visitorNum + " " + roomNum + " I " + enterTime);
                 countdown--;
             }
             for (Integer room : rooms.keySet()) {
                 List<Visitor> leaving = rooms.get(room);
                 for (Visitor v : leaving) {
                     int exitTime = rand.nextInt(END_TIME - v.getEnterTime() + 1) + v.getEnterTime();
                     System.out.println(v.getNum() + " " + room + " O " + exitTime);
                 }
                 countdown--;
             }            
         }
     }
             
 }
