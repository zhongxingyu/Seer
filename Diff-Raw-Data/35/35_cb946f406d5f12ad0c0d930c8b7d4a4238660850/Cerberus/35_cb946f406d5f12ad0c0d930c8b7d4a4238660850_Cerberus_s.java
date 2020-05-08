 package deus.gatekeeper.cerberus;
 
 import deus.gatekeeper.puddle.LoginCredentials;
 
 public interface Cerberus {
 
	public void login(LoginCredentials credentials);
 
 
 	public void logout(String localUsername);
 
 	
 	public void addUserLoginStateObserver(UserLoginStateObserver observer);
 	
 	public void removeUserLoginStateObserver(UserLoginStateObserver observer);
 }
