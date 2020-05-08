 package pratica01;
 
 //How to use the MultiServer
 //----------------
 //1.    Compile with javac 
 //2.    Run : *java MultiServer [Server RTSP listening port]*
 //
 //      example: `java MultiServer 3000`
 //
 // You can connect clients !
 
 import java.net.ServerSocket;
 
 public class MultiServer {
     //Description
     //------------------------------------
    // This class listen requests on the port "RTSP port", and instantiate a ServerThread in a new Thread for each Client who try to connect.
     public static void main(String argv[]) throws Exception
     {
         boolean listening=true;
         
         //* get RTSP socket port from the command line
         int RTSPport = Integer.parseInt(argv[0]);
 
         //* create the socket to listen to 
         ServerSocket listenSocket = new ServerSocket(RTSPport);
         
        //* listen infinitely on port "RTSP" port, and instantiate a Server in a new Thread if a client wants to be connected 
         while(listening) {
             new ServerThread(listenSocket.accept()).start();
         }
         
     }
 
 }
