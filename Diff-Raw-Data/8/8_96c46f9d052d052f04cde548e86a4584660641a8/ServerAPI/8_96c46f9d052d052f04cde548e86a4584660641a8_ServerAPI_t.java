 package dblike.api;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import dblike.server.ActiveClient;
 import java.rmi.*;
 
 /**
  *
  * @author wenhanwu
  */
 public interface ServerAPI extends Remote {
 
     public boolean addClient(String clientID, String deviceID, String clientIP, int clientPort) throws RemoteException;
 
    public boolean removeClient(String clientID, String deviceID) throws RemoteException;
 
    public int checkClientbyID(String clientID, String deviceID) throws RemoteException;
 
     public int checkServerByIP_Port(String serverIP, int port) throws RemoteException;
 
    public void callClient(String clientID, String deviceID, String content) throws RemoteException;
 
     public void displayClient(ActiveClient target, String message) throws RemoteException;
 
     public boolean beatFromServer(String serverIP, int port) throws RemoteException;
 
     public boolean beatFromClient(String clientID, String deviceID) throws RemoteException;
 }
