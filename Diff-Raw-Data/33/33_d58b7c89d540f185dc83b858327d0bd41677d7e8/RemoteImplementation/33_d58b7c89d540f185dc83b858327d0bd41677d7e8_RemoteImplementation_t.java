 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package RMINetwork;
 
 import java.rmi.server.UnicastRemoteObject;
 import StandaloneServer.*;
 import java.rmi.RemoteException;
 
 /**
  *
  * @author the
  */
 public class RemoteImplementation extends UnicastRemoteObject implements RemoteInterface {
    private final RMIServerUI rServer = new RMIServerUI();
     public RemoteImplementation()throws RemoteException{}
    public void connect(String username) throws RemoteException
    {
        rServer.connect(username);
    }
    public void disconnect(String username) throws RemoteException
    {
        rServer.disconnect(username);
    }
 }
