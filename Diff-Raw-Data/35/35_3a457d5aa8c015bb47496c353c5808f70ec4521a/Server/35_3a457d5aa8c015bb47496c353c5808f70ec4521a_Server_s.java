 package net;
 
 import util.Log;
 import util.PotentialNinjaException;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mayacat
  * Date: 9/2/13
  * Time: 12:29 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Server {
 
     private ServerSocket serverSocket;
 
     public Server() throws PotentialNinjaException{
         try{
         serverSocket = new ServerSocket(NetConfig.port);
         }catch(IOException e){
             System.err.println();
            throw new PotentialNinjaException("ServerSocket unable to instantiate.")
         }
 
         Log.initLogFile();
     }
 
     class ConnectionThread{
         Socket socket;
         ObjectInputStream ois;
         ObjectOutputStream oos;
 
         public ConnectionThread(Socket socket)
         {
             this.socket = socket;
         }
 
     }
 }
