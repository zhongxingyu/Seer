 package interfaces;
 
 import java.util.ArrayList;
 
 import data.LoginCred;
 import data.Service;
 
 /**
  * Mediator interface for Gui module.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public interface MediatorGui {
 	public void registerGui(Gui gui);
 
 	public void start();
 	public boolean logIn(LoginCred cred);
 	public void logOut();
 	public String getName();
 	
 	/* Common */
 	public ArrayList<Service> loadOffers();
 	public int addOffer(String service);
 	public int removeOffer(String service);
 	
	public int launchOffer(String service);
	public int dropOffer(String service);
 	
 	/* Buyer */
 	public int acceptOffer();
 	public int refuseOffer();
 	
 	/* Seller */
 	public int makeOffer();
 	public int dropAction();
 }
