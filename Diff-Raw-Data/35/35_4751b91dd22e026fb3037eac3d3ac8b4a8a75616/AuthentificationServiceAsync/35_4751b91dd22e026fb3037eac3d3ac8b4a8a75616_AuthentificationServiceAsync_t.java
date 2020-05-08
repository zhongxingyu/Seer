 package org.daum.javase.webportal.client;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
import org.daum.common.genmodel.Agent;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: pdespagn
  * Date: 7/13/12
  * Time: 4:10 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface AuthentificationServiceAsync {
       void authenticateAgent(String name, String password, AsyncCallback<Boolean> callback);
       void createAgent(String nom, String prenom, String matricule , String password,AsyncCallback<Agent> callback);
 }
