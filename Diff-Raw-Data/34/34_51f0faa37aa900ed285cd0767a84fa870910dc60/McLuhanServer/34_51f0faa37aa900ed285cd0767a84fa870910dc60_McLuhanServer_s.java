 package com.navmenu;
 
 import java.io.IOException;
 
 import com.communication.Engine_Server;
 
 
 public class McLuhanServer {
 	private static Engine_Server eserver;
 	private static boolean isConnected = true;
 
 	public static void main(String[] args) {
 		McLuhanMain ml = new McLuhanMain();
 		ml.setVisible(true);
 		
 		if (isConnected) {
 			openConnection();
 		
 			try {
 				while (true) {
 					eserver.recvData();
 					
 					switch(ml.mode) {
 						case 0:
							// Grabber
 							break;
 						case 1:
 							ml.recvThemeData(eserver.getPosition()[0], eserver.getPosition()[1], eserver.getPosition()[2],
 											 eserver.getSelection()[0], eserver.getCommand());
 							break;
 						case 2:
 							ml.recvPhysicsData(eserver.getPosition()[0], eserver.getPosition()[1], eserver.getPosition()[2],
 											   eserver.getSelection()[0], eserver.getCommand());
 							break;
 						default:
 							closeConnection();
 							System.exit(-1);
 					}
 				}
 			} catch (IOException ioe) {
 				closeConnection();
 				System.exit(-1);
 			}
 			
 			closeConnection();
 		}
 	}
 	
 	/*
 	 * 
 	*/
 	private static void openConnection() {
 		try {
 			eserver = new Engine_Server();
 		} catch (IOException ioe) {
 			System.exit(-1);
 		}
 	}
 	
 	private static void closeConnection() {
 		try {
 			eserver.endServer();
 		} catch (IOException ioe) {
 			System.exit(-1);
 		}
 	}
 }
 
 
