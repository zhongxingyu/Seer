 package jchazelcast;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 
 
 public class JCListener {
     static Thread listenThread;
     static JCConnection connection;
     static boolean stopListening;
     static  volatile CountDownLatch latch;
     static  String response;
     private JCListener()  {
 
     }
 
     static void init() throws IOException {
         if(connection==null)
             connection=JCHazelcast.getCon();
         stopListening = false;
         if(listenThread==null){
             listenThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     while(!stopListening){
                         try {
                             if( connection.ready()){
                                 JCConnection.Response resp = readEventNResponse();
                                 if(resp instanceof Event)
                                     notifyEvent((Event) resp);
                                 else{
                                     response = resp.responseLine;
                                     latch.countDown();
                                 }
                             }
                         } catch (IOException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         } catch (ClassNotFoundException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
                     }
                 }
             }
 
             );
             listenThread.start();
         }
     }
 
     static void stopListening() throws IOException {
         stopListening =true;
         connection.disconnect();
     }
 
     static boolean addMapListener(String mapName,boolean includeValue) throws IOException, InterruptedException {
             latch = new CountDownLatch(1);
             connection.sendOp("MADDLISTENER listener " + mapName + " " + includeValue + " " + false) ;
            latch.await();
            System.out.println(response);
           return response.startsWith("OK");
 
     }
 
    static boolean removeMapListener(String mapName) throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
             connection.sendOp("MREMOVELISTENER listener " + mapName + " " + false) ;
        latch.await();
        System.out.println(response);
        return response.startsWith("OK");
     }
 
 
 
 
 
     static void notifyEvent(Event e){
         if(e.type.equals("UPDATED")){
                 if(e.structure.equals("map")){
                     for(EntryListener each : JCMap.listeners.get(e.name)){
                         each.onUpdated(e);
                     }
                 }
         }else if(e.type.equals("ADDED")){
             if(e.structure.equals("map")){
                 for(EntryListener each : JCMap.listeners.get(e.name)){
                     each.onAdded(e);
                 }
             }
         }else if(e.type.equals("REMOVED")){
             if(e.structure.equals("map")){
                 for(EntryListener each : JCMap.listeners.get(e.name)){
                     each.onRemoved(e);
                 }
             }
         }else if(e.type.equals("EVICTED")){
             if(e.structure.equals("map")){
                 for(EntryListener each : JCMap.listeners.get(e.name)){
                     each.onEvicted(e);
                 }
             }
         }else {
             throw new IllegalArgumentException("Unknown event type!");
         }
     }
 
     static JCConnection.Response readEventNResponse() throws IOException, ClassNotFoundException {
         String responseLine = connection.readLine();
 //        System.out.println("DEBUG::inthread "+responseLine);
         String[] split = responseLine.split(" ");
 
         int count=0;
         List<Object> values = new ArrayList<Object>();
         if ( split[split.length - 1].startsWith("#")) {
              count = Integer.parseInt(split[split.length - 1].substring(1));
             if(count>0){
                 String sizeLine = connection.readLine();
                 String[] tokens = sizeLine.split(" ");
 //                System.out.println(sizeLine);
                 for (int i = 0; i < count; i++) {
                     values.add(JCSerial.deserialize(connection.readData(Integer.parseInt(tokens[i]))));
                 }
                 connection.readData(2); //read CRLF
 
             }
         }
         if(split[0].equals("EVENT")){
             String eventType = split[4];
             boolean inc= (count > 1);
             if(inc){
                 if(eventType.equals("UPDATED")){
                     return new Event(split[4],split[3],split[2],inc,values.get(0),values.get(1),values.get(2));
                 }else{
                     return new Event(split[4],split[3],split[2],inc,values.get(0),values.get(1));
                 }
             }else{
                 return new Event(split[4],split[3],split[2],inc,values.get(0));
             }
         }
         return new JCConnection.Response(responseLine,values);
     }
 
 
 
 
 }
