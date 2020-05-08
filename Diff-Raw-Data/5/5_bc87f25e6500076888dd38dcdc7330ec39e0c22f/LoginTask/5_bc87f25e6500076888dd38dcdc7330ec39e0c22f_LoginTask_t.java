 package brutes.client.gui;
 
 import brutes.client.ScenesContext;
 import brutes.client.net.ErrorResponseException;
 import brutes.client.net.InvalidResponseException;
 import brutes.client.net.NetworkClient;
 import brutes.client.user.Session;
 import brutes.net.Protocol;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.beans.property.ReadOnlyBooleanProperty;
 import javafx.beans.property.ReadOnlyBooleanWrapper;
 import javafx.beans.property.ReadOnlyObjectProperty;
 import javafx.beans.property.ReadOnlyObjectWrapper;
 import javafx.concurrent.Task;
 
 /**
  *
  * @author Karl
  */
 public class LoginTask extends Task{
     private String login;
     private String password;
     private String host;
     private ReadOnlyBooleanWrapper loginError;
     private ReadOnlyBooleanWrapper passwordError;
     private ReadOnlyBooleanWrapper hostError;
 
     public LoginTask(String host, String login, String password) {
         this.loginError = new ReadOnlyBooleanWrapper();
         this.passwordError = new ReadOnlyBooleanWrapper();
         this.hostError = new ReadOnlyBooleanWrapper();
         this.login = login;
         this.password = password;
         this.host = host;
     }
 
     public ReadOnlyBooleanProperty getLoginErrorProperty(){
         return this.loginError.getReadOnlyProperty();
     }
     public ReadOnlyBooleanProperty getPasswordErrorProperty(){
         return this.passwordError.getReadOnlyProperty();
     }
     public ReadOnlyBooleanProperty getHostErrorProperty(){
         return this.hostError.getReadOnlyProperty();
     }
     
     @Override
     protected Void call() throws Exception {
         try (NetworkClient connection = new NetworkClient(new Socket(this.host, Protocol.CONNECTION_PORT))) {
             String token;
             token = connection.sendLogin(this.login, this.password);
             NetworkClient.clearCache();
             ScenesContext.getInstance().setSession(new Session(this.host, token));
             ScenesContext.getInstance().getSession().netLoadMyBrute();
             ScenesContext.getInstance().getSession().netLoadChallengerBrute();
         } catch (UnknownHostException ex) {
             this.hostError.set(true);
             throw ex;
         } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.WARNING, null, ex);
            this.hostError.set(true);
             throw ex;
         } catch(ErrorResponseException ex){
             if(ex.getErrorCode() == Protocol.ERROR_LOGIN_NOT_FOUND){
                 this.loginError.set(true);
             }
             if(ex.getErrorCode() == Protocol.ERROR_WRONG_PASSWORD){
                 this.passwordError.set(true);
             }
             throw ex;
         } catch(InvalidResponseException ex){
             Logger.getLogger(LoginController.class.getName()).log(Level.WARNING, null, ex);
            this.hostError.set(true);
             throw ex;
         } catch(Exception ex){
             ex.printStackTrace();
             throw ex;
         }
         return null;
     }
 }
