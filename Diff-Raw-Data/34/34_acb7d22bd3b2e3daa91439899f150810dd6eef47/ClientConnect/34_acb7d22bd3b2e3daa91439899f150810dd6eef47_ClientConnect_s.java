 package com.ds.maze;
 
import java.io.Serializable;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 
public interface ClientConnect extends Remote, Serializable{
 	 public void onSuccess(HashMap<String,Object> gameState) throws RemoteException;
 	   public void onFailure() throws RemoteException;
 
 }
