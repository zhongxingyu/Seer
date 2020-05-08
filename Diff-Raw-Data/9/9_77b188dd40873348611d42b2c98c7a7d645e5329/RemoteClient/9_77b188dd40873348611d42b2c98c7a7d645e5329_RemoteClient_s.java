 package Ligretto;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 /**
 * Stellt Client-Informationen dem Server zur Verfuegung.
  * @author 
  */
 public interface RemoteClient extends Remote {
 	
 	public boolean ping() throws RemoteException;
 	
 }
