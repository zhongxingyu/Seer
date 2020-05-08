 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes;
 
 import brutes.net.Network;
 import brutes.net.Protocol;
 import brutes.user.Session;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.concurrent.Task;
 
 /**
  *
  * @author Karl
  */
 public class LoginTask extends Task{
     private String login;
     private String password;
     private String host;
    private Session session;
 
     public LoginTask(String host, String login, String password) {
         this.login = login;
         this.password = password;
         this.host = host;
     }
     
     @Override
     protected Void call() throws Exception {
         try{
             if(this.login.isEmpty() || this.password.isEmpty()){
                 throw new Exception("Bad login");
             }
             ScenesContext.getInstance().setNetwork(new Network(new Socket(host, Protocol.CONNECTION_PORT)));
             String token = ScenesContext.getInstance().getNetwork().login(this.login, this.password);
             ScenesContext.getInstance().setSession(new Session(token));
         } catch(Exception ex){
             Logger.getLogger(LoginTask.class.getName()).log(Level.WARNING, "Task failed", ex);
             throw new Exception("Task failed");
         }
         return null;
     }
 }
