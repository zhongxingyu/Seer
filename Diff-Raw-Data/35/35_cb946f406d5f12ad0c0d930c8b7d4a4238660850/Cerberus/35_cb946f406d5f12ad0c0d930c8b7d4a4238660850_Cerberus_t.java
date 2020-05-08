 package deus.gatekeeper.cerberus;
 
 import deus.gatekeeper.puddle.LoginCredentials;
import deus.model.user.id.UserId;
 
 public interface Cerberus {
 
	public UserId login(LoginCredentials credentials);
 
 
 	public void logout(String localUsername);
 
 	
 	public void addUserLoginStateObserver(UserLoginStateObserver observer);
 	
 	public void removeUserLoginStateObserver(UserLoginStateObserver observer);
 }
