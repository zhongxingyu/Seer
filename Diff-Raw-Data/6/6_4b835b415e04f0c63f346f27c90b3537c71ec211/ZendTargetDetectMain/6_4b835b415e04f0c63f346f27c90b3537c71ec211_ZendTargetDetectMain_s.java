 package org.zend.sdklib.internal.target;
 
 import org.zend.sdklib.logger.ILogger;
 import org.zend.sdklib.logger.Log;
 import org.zend.sdklib.manager.DetectionException;
 import org.zend.sdklib.manager.TargetsManager;
 
 /**
  * Simplified command line util to detect localhost target.
  *
  */
 public class ZendTargetDetectMain {
 
 	public static void main(String[] args) throws DetectionException {
 		String targetId = null;
 		String key = null;
 		
 		ILogger logger = new ILogger() {
 
 			@Override
 			public void debug(Object message) {
 				System.err.println(message);
 			}
 
 			@Override
 			public void info(Object message) {
 				System.out.println(message);
 			}
 
 			@Override
 			public void warning(Object message) {
 				System.out.println(message);
 			}
 
 			@Override
 			public void error(Object message) {
 				System.err.println(message);
 			}
 
 			@Override
 			public ILogger getLogger(String creatorName, boolean verbose) {
 				return this;
 			}
 			
 		};
 		Log.getInstance().registerLogger(logger );
 		
 		if (args.length >= 1) {
 			targetId = args[0];
 		}
 		if (args.length >= 2) {
 			key = args[1];
 		}
 		
 		TargetsManager tm = new TargetsManager();
 		tm.detectLocalhostTarget(targetId, key, false);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 }
