 package ligretto;
 
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.List;
 
 /**
 * Stellt alle Server-Informationen fr die Clients zur Verfuegung.
  * @author 
  */
 public interface RemoteServer extends Remote {
 	
 	public EventHandler getGameEventHandler() throws RemoteException;
 	
 	public List<? extends Card> getHandkarten() throws RemoteException;
 
 	public List<? extends Stapel> getStapel() throws RemoteException;
 	
 	public Card legeKarte(Card karte, int stapelNumber) throws RemoteException;
 	
 }
