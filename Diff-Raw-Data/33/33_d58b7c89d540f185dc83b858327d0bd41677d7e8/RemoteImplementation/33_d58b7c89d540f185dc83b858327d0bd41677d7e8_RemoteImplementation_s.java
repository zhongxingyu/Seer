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
    private final RMIServer rServer = new RMIServer();
     public RemoteImplementation()throws RemoteException{}
    
 }
