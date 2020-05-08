 package strategies.util;
 
 import robot.Platform;
 import strategies.Strategy;
 
 /**
  * Just drive forward - for example for reading a barcode
  *
  */
 public class DriveForwardStrategy extends Strategy {
 
 	@Override
 	protected void doInit() {
	    Platform.ENGINE.move(1000);
 	}
 
 	@Override
 	protected void doRun() {
		
 	}
 }
