 package Ligretto;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 /**
 * 
  * @author 
  */
 public interface RemoteClient extends Remote {
 	
 	public boolean ping() throws RemoteException;
 	
 }
