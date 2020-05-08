 
 import java.io.IOException;
 import java.sql.ClientInfoStatus;
 import java.util.Iterator;
 
 
 import se.nicklasgavelin.sphero.Robot;
 
 
 public class Main {
 	
 	
 	public static void main(String[] args) {
 		//Example_Site_API example = new Example_Site_API();
 		// new Thread( new Example_Site_API() ).start();
 		//	DirectionalKeysPanel DKP = null;// = new DirectionalKeysPanel();if( ct != null )
 		//ct.stopThread();
 		// Create a new thread
 		//		ConnectThread ct = new ConnectThread();
 		//		ct.start();
 		//		while(ct.getRobotArray().size()==0){}
 		
		String bluetoothAddress = "000666440DB8"; //NEW , WBG
		//String bluetoothAddress = "0006664438B8"; //OLD, BBR
 		String ConnectToIP = "130.240.95.209";
		
 		GurrUI gurrui = new GurrUI(new ConnectThread(bluetoothAddress), ConnectToIP);
 		gurrui.fixGUI();
 		
 		
 		//backgroundvariables, do not touch
 		//server sends commands - clients recieves and does them	
 	}
 }
 
