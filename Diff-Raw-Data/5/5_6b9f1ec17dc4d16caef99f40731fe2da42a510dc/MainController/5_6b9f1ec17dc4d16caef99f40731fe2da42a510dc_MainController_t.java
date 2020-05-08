 package ch.bfh.univoteverifier.common;
 
 import ch.bfh.univoteverifier.gui.StatusSubject;
 import ch.bfh.univoteverifier.verification.IndividualVerification;
 import ch.bfh.univoteverifier.verification.UniversalVerification;
 import ch.bfh.univoteverifier.verification.Verification;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This class is the controller who is responsible for the communication between
  * the GUI and the internal infrastructure
  * @author snake
  */
 public class MainController {
 	
 	private Verification v;
         private GUIMessenger msgr;
 
 
 	public MainController(){
 		this.msgr = new GUIMessenger();
 		//initialize the root logger - maybe this should be placed into the main method
 		Handler h;
 		
 		try {
 			h = new FileHandler("verification.log");
 			Logger.getLogger("").addHandler(h);
 			
 		} catch (	IOException | SecurityException ex) {
 			Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		
 	}
 	
         
 	/**
 	 * Run an universal verification
 	 * @param eID String the ID of a election
 	 */
 	public void universalVerification(String eID){
		this.v = new UniversalVerification(eID,msgr);
 		
 		v.runVerification();
 	}
 	
 	
 	/**
 	 * Run an individual verification
 	 * @param eID
 	 */
 	public void individualVerification(String eID){
             
		this.v = new IndividualVerification(eID,msgr);
 	}
 	
 	/**
 	 *
 	 * @return
 	 */
 	public StatusSubject getStatusSubject() {
 		return msgr.getStatusSubject();
 	}
 	
 	
         public void testMsgSystem(){
             this.msgr = new GUIMessenger();
             msgr.sendErrorMsg("Test to make sure obsever pattern is up and running");
         }
         
 	public String decodeQRCode(File filepath){
 		QRCode qr = new QRCode();
 		//            String decodedStr = qr.decode(filepath);
 		return "";
 	}
 
         
 }
