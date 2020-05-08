 /**
    ClientThread.java
    @author Nilo Redini
    @author Davide Pellegrino
 
    This class manages server and client threads both.
    User can choose to connecting or to wait connection; this class
    implements this chose.
 */
 
 import SecureChat.file.*;
 import SecureChat.login.*;
 import java.io.*;
 import java.util.*;
 import java.lang.*;
 import java.util.concurrent.locks.*;
 import java.net.*;
 import java.security.*;
 import javax.crypto.SecretKey;
 
 public class ClientThread extends Thread {
     
     private boolean type;
     private User usr;
     static boolean  connect;
     static boolean accepted;
     static Lock  sem ;
     static Condition WaitCall; 	
     private boolean PresentKey;
     private Path path;
 
     /**
        Main constructor
        @param usr : User object
        @param path : path of the keyfiles and the credentials
        @param type: kind of class istance (server/client)
        @throws IOException
      */
     public ClientThread (User usr, boolean type, Path path)throws IOException{
         this.sem = new ReentrantLock();
         this.type = type;
         this.usr = usr;
         WaitCall = sem.newCondition();
         accepted = false;
         PresentKey = true;
 	this.path = path;
     }
     
     /**
        Retrieves the connection state
        @return boolean: connection state
      */
     public boolean IsConnected(){
         boolean test;
         sem.lock();
         test = connect;
         sem.unlock();
         return test;
     }
 
     /**
        Wakes up a thread which is waiting a connection
      */
     public void signalConnected(){
         sem.lock();
         try{
             WaitCall.signal();
         }
         finally{
             sem.unlock();
         }	
     }
     
     /**
        Exit function, used to quit this thread by main thread
      */
     public void exitThread(){
         System.exit(0);	
     }
     
     /**
        Reset connection parameter
      */
     private void resetConnect(){
         sem.lock();
         connect = false;							
         sem.unlock();
     }
     
     /**
        Set connection parameter
      */
     private void setConnect(){
         sem.lock();
         connect = true;							
         sem.unlock();
     }
     
     /**
        Set a parameter according to decision of accept a connect 
        or not.
        @param value: true if connection is goingo to be accepted, false otherwise
      */
     public void setAccepted(boolean value){
         accepted = value;
     }
 
     /**
        Return state of connection
        @return boolean: true if connection has been accepted, false otherwise
      */
     public boolean isAccepted(){
         return accepted;
     }
 
     /**
        Converts byte array in String
        @param arr: byte array to convert
        @return String: String converted
        @throws UnsupportedEncodingException
      */
     public String getText (byte[] arr) throws UnsupportedEncodingException
     {
         String s = new String( arr, "UTF-8" );
         return s;
     }
     
     public void run() throws RuntimeException{
         Socket csock;
         ServerSocket ss;
         String FName = null;
         ObjectOutputStream StreamOut=null;
         byte[] Buff = null;
         ObjectInputStream ois;
         BufferedReader stdIn = new BufferedReader ( new InputStreamReader (System.in));
         try{
             
             //server body
             if(type){
                 while(true){
                     resetConnect();
                                         
                     ss = new ServerSocket(usr.getServerPort());
                     csock = ss.accept();
                     if(IsConnected())System.exit(0); //whether the user has already connect the process has killed.
                    
                     StreamOut = new ObjectOutputStream( csock.getOutputStream() );
                     ois = new ObjectInputStream( csock.getInputStream() );
                     Buff = (byte[]) ois.readObject();
                     
                     FName = getText(Buff);
                     System.out.print("[CHAT] "+ FName + ": "); //accept message
                     Buff = (byte[]) ois.readObject();
                     FName = getText(Buff);
                     usr.setFriendName(FName);
 
                     sem.lock();
                     connect = true;							
                     WaitCall.await();//wait user decision (see other thread)				
                     sem.unlock();
                     
                     if(!isAccepted())StreamOut.writeObject(("NACK").getBytes());						
                     else {
                         StreamOut.writeObject((usr.getUserName()).getBytes());
                         System.out.println("[CHAT] Connected with " + usr.getFriendName());
                         break;
                     }
                 }
                 
             }
             
             //client's body
             //receive the name on connect success
             else{
                 System.out.println("Trying to connect on port: " + usr.getClientPort());
                 if(IsConnected())System.exit(0); //whether the server mode in on, the client mode have to be closed
                 csock = new Socket(usr.getClientIp(),usr.getClientPort());
                 StreamOut = new ObjectOutputStream( csock.getOutputStream() );
                 ois = new ObjectInputStream( csock.getInputStream() );
                 
                 StreamOut.writeObject(("\n" + usr.getUserName() + " would talk with you, please press \'y\' to accept").getBytes());    
                 StreamOut.writeObject((usr.getUserName()).getBytes());
 
                 Buff = (byte[]) ois.readObject();
                 FName = getText(Buff);
                 
                 
                 if(FName.compareTo("NACK")==0){
                     System.out.println("[CHAT] Connection not accepted");
                     System.exit(0);
                 }
 
                 else{/*Initializes the friend's parameters*/
                     System.out.println("[CHAT] Connected with " + FName);
                     usr.setFriendName(FName);
                 }
                 
             }
             SecretKey key = usr.createDiffieHellman(path.PATHDH,StreamOut,ois);                       
           
             if(key == null)
                 {
                     System.out.println("[Error] Unable to complete Diffie-Hellman algorhitm");
                     System.exit(-1);
                 }
   
             usr.desInstance(key);
 
             //gestire meglio, se un utente esce deve farlo anche l'altro.
             if(!usr.isRsaPresent(usr.getFriendName()))
               PresentKey = false;
             else PresentKey = true;
 
             /*receive messages*/
             new ReceiveMessage(ois,usr).start();
             
             /*send messages*/
             while (true)
                 StreamOut.writeObject(usr.Encrypt(stdIn.readLine()));
                 
         }
         catch (Exception e) {
             if(PresentKey){ 
                 System.out.println("[Error] User appears to be offline");
                 System.err.println(e.getMessage());
             }
 
             else System.out.println("Unable to find public key of " + usr.getFriendName() + ", fecth the key first.");            
             System.exit(0);
         }	
     }
 }
