 /*
  *  Copyright Mattias Liljeson Sep 7, 2011
  */
 package racecourseserver;
 
 import common.*;
 import java.awt.Rectangle;
 import java.awt.geom.Line2D;
 import java.io.*;
 import java.net.*;
 import javax.swing.ImageIcon;
 
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class RaceCourseServer {
     public final static String SERVER_HOSTNAME = "localhost";
     public final static int COMM_PORT = 5678;
 //    ServerSocket servSock;
     RaceCourse raceCourse;
     Channel channel;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         System.out.println("Race Course Server started");
         
         RaceCourseServer raceCourseServ = new RaceCourseServer();
         raceCourseServ.run(); 
     }
     
     public RaceCourseServer(){
 //        try{
 //            servSock = new ServerSocket(COMM_PORT);
 //        }catch(IOException ignore){}
         
         channel = new Channel(COMM_PORT);
         channel.startServer();
         ImageIcon raceCourseImg = new ImageIcon("../res/level1_colors.png");
         ImageIcon frictionMaskImg = new ImageIcon("../res/level1_frictionmask.png");
         Line2D[] checkpoints = new Line2D[3];
 		checkpoints[0] = new Line2D.Double(640,100,530,220);
 		checkpoints[1] = new Line2D.Double(440,420,400,540);
 		checkpoints[2] = new Line2D.Double(85,280,210,270);
        raceCourse = new RaceCourse(raceCourseImg, frictionMaskImg, checkpoints, 100, 100, 100, 1);
     }
     
     public void run(){
         //Create accepter thread to handle new map requests
         Thread accepter = new Thread(new RaceCourseAccepter());
         accepter.start();
         
         // Enter main loop which listens to the 'exit' signal
         // TODO: ugly way of shutting down subthreads, fix this
         boolean done = false;
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String input = "";
         while(!done){
             try{
                 input = br.readLine();
             }catch(IOException ex){
                 System.out.println("Input stream failure. Exiting server.");
                 done = true;
                 input = "";
             }
             
             if(input.equals("exit")){
                 System.out.println("Exiting from race course server");
                 done = true;
             }
         }
         System.exit(0);
     }
     private class RaceCourseAccepter implements Runnable{
 
         @Override
         public void run() {
             Socket clientSock = null;
             while(true){
                 clientSock = channel.accept();
                 System.out.println("A new client connected ("+clientSock.getInetAddress()+")");
 
                 if(clientSock != null){
                     Thread thread = new Thread(new RaceCourseSend(clientSock, raceCourse));
                     thread.start();
                     clientSock = null;
                 }
             }
         }
         
     }
     private class RaceCourseSend implements Runnable{
         private Socket sock;
         private RaceCourse payload;
         private Channel channel;
         
         public RaceCourseSend(Socket socket, RaceCourse payload){ 
             this.sock = socket;
             this.payload = payload;
             
             channel = new Channel(socket);
         }
 
         @Override
         public void run() {
             channel.openStreams();
             boolean success = true;
             try{
                 success = channel.sendObject(payload);
             }catch(Channel.ConnectionLostException ex){
                 success = false;
             }
             if(success){
                 System.out.println("Race course successfully sent");
             } else {
                 System.out.println("Failed to send race course");
             }
             channel.closeStreams();
             
             try{
                 // Leave time for the client to close its input stream
                 Thread.sleep(1000); 
             }catch(InterruptedException ignore){}
             finally{
                 channel.closeSockets();
                 System.out.println("Client disconnected ("+sock.getInetAddress()+")");
             }
             
         }
     }
 }
