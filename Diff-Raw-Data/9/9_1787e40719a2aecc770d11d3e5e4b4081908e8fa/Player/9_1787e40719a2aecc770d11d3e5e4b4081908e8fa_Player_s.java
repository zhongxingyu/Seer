 package it.chalmers.tendu.gamemodel;
 
 /**
  * Represents the player of this particular device.
  */
 public class Player {
 	private static Player instance = null;
 
	private static String mac;
 
 	private Player() {
 	}
 
 	public static Player getInstance() {
 		if (instance == null) {
 			return instance;
 		}
 		return new Player();
 	}
 	
	public static void setMac(String myMac){
 		mac = myMac;
 	}
 
 	public String getMac() {
 		return mac;
 	}
 
 }
