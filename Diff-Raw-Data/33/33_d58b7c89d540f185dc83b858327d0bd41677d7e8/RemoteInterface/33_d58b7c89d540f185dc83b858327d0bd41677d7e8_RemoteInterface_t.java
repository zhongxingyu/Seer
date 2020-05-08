 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package RMINetwork;
 
 import java.rmi.Remote;
import java.rmi.RemoteException;
 
 /**
  *
  * @author the
  */
 public interface RemoteInterface extends Remote {
    public void connect(String username) throws RemoteException;
    public void disconnect(String username) throws RemoteException;
 }
