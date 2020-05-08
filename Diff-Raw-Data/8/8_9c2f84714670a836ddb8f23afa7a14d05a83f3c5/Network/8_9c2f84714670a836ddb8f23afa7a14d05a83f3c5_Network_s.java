 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net;
 
 import brutes.ScenesContext;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Karl
  */
 public class Network{
     private Socket connection;
     
     public Network(Socket connection){
        
     }
     
     public String login(String user, String password){
         try {
             ByteArrayOutputStream datas = new ByteArrayOutputStream();
             datas.write(ByteBuffer.allocate(8).putInt(Protocol.D_LOGIN).array());
             datas.write(user.getBytes(Charset.forName("UTF-8")));
             datas.write(password.getBytes(Charset.forName("UTF-8")));
             ByteArrayOutputStream message = new ByteArrayOutputStream();
             message.write(ByteBuffer.allocate(32).putInt(datas.size()).array());
             message.write(datas.toByteArray());
             //this.socket.getOutputStream().write(message.toByteArray());
             for(int i = 0; i < message.toByteArray().length; i++){
                 System.out.print(Byte.toString(message.toByteArray()[i]));
             }
             //TODO : input stream
             return "dummyToken";
         } catch (IOException ex) {
             Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public void disconnect(){
         try {
             this.connection.close();
         } catch (IOException ex) {
             Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
