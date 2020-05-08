 /**
    ReceiveMessage.java
    @author Nilo Redini
    @author Davide Pellegrino
    
    this class allows to receive messages from a secure connection
 */
 
 import SecureChat.login.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.lang.*;
 import java.security.*;
 
 public class ReceiveMessage extends Thread {
     /** Socket input stream */
     private  ObjectInputStream input;
     /** User ID*/
     private User usr;
    private final String EXITUSER = "EXITUSERNOW";
       


     /**
        Main constructor
        @param input: input stream
        @param user: an User object
      */
     public ReceiveMessage (ObjectInputStream input,User usr){
         this.usr = usr;
         this.input = input;
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
     
     public void run(){
         byte[] data = null;
         String str = null;
         while (true){
             try{
                 data = (byte[])input.readObject();
                 str = usr.Decrypt(data);
                if(str==null || (str.compareTo(EXITUSER)==0))break;                
                 System.out.println(usr.getUserName() + "> " + str);
                 System.out.flush();
             }catch(Exception x){}            
         }
         System.out.println(usr.getUserName() + " has disconnected");
         System.exit(0);
     }
     
 
 }
