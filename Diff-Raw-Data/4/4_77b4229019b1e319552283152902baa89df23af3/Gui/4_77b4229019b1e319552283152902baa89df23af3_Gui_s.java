 package interfaces;
 
 import data.LoginCred;
 
 /**
  * Gui interface.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public interface Gui {
 	public void login();
 	public void signIn(LoginCred cred);
 	public void signOut();
 	
 	public void addService(Service service);
 	public void addServices(ArrayList<Service> services);
 	public void removeService(Service service);
 	public void removeServices(ArrayList<Service> services);
 }
