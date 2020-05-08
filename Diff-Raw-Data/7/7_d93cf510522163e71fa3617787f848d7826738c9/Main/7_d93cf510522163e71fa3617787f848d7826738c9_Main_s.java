 package server.main;
 
 import java.rmi.RemoteException;
 
 import ctrl.game.RemoteCommunicationController;
 
 
 
 
 
 public class Main {
 	
 	
 	public static void main(String[] args) {
 
 		
 		try {
			RemoteCommunicationController remoteCommunicationController =
 					new RemoteCommunicationController();
 			
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		
 		
 	}
 }
