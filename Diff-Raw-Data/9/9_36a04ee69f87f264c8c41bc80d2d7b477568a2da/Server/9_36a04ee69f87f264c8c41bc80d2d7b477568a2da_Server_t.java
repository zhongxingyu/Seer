 import java.util.*; 
 import java.io.*; 
 import java.net.*;
 
 public class Server
 {
     public static void main(String[] args)
     {
         System.out.println("Starting server...");
         
         // create a new instance of the directory server
        DirectoryServer ds = new DirectoryServer(6900); 
 		
         Thread t1 = new Thread(ds);        
         t1.start();
 
         // create a new instance of the heartbeat monitor
         if ((args.length > 0 && args[0].equals("--disable-keep-alive") == false) ||
             (args.length == 0))
         {
             HeartbeatMonitor hm = new HeartbeatMonitor(ds);
             
             Thread t3 = new Thread(hm);
             t3.start();
         }
                 
         // create a new instance of the voice server
         VoiceServer vs = new VoiceServer(6901);
         
         Thread t2 = new Thread(vs);        
         t2.start();
     }
 }
 
