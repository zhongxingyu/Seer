 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eccdh.registry;
 
 import eccdh.registry.data.Client;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.List;
 
 /**
  *
  * @author Alex
  */
 public interface RegistryInterface extends Remote {
     /**
      * register a client with its public keys to the registry
      * 
      * @param client the client containing keys
      * @return true if the client has been registered, false otherwise (i.e. if the name is already taken)
      * @throws RemoteException 
      */
     public boolean registerClient(Client client) throws RemoteException;
     
     /**
      * remove the public key of the client from the registry
      * 
      * @param name the name of the client
      * @return true if the client was removed, false otherwise (i.e. if the key or person does not exist)
      * @throws RemoteException 
      */
     public boolean removeClient(String name) throws RemoteException;
        
     /**
      * retrieve a list of all registered names
      * 
      * @return a list of all registered names
      * @throws RemoteException 
      */
     public List<String> getNames() throws RemoteException;
        
     /**
      * get the client for the registered name
      * 
      * @param name the username
      * @return the client for the username
      * @throws RemoteException 
      */
     public Client getClient(String name) throws RemoteException;
 }
