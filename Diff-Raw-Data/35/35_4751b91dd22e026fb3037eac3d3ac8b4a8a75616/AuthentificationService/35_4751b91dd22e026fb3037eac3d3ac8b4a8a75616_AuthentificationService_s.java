 package org.daum.javase.webportal.client;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.sitac.Agent;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: pdespagn
  * Date: 7/13/12
  * Time: 4:09 PM
  * To change this template use File | Settings | File Templates.
  */
 @RemoteServiceRelativePath(value = "authentifService")
 public interface AuthentificationService extends RemoteService {
 
     public boolean authenticateAgent(String name, String password);
 
     public Agent createAgent(String nom, String prenom, String matricule, String password);
 
 
 }
