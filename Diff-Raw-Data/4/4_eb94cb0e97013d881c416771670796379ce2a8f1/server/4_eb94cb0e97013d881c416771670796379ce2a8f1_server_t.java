 package risk.network;
 
 import java.io.*;
 import java.net.*;
 import risk.*;
 
 public class server {
     public void doit() {
         ServerSocket sock = null;
 
         try {
             sock = new ServerSocket(34343);
 
         } catch (IOException e) {
             System.err.println("Couldn't listen on port.");
             System.exit(-1);
         }
 
         Socket clientSocket = null;
         try {
             clientSocket = sock.accept();
         } catch (IOException e) {
             System.err.println("Couldn't accept on port.");
             System.exit(-1);
         }
 
         try {
            ObjectOutputStream ous = new ObjectOutputStream(clientSocket.getOutputStream());
            Baby p = new Baby("Tamas267", 10, true, false);
 
             while (p != null) {
                 System.out.println(p.toString());
                 ous.writeObject(p);
                 ous.reset();
                 p.Age = p.Age + 1;
                 p.Name = "." + p.Name;
                 p.canWalk = !p.canWalk;
                 p.criesOverNight = !p.criesOverNight;
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
 
                 }
             }
 
             clientSocket.close();
             sock.close();
         } catch (IOException e) {
             System.err.println("IOexception");
             System.err.println(e.getMessage());
             System.err.println(e.getStackTrace());
         }
     }
 
 };
