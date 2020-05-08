 package uk.ac.cam.db538.dexter.aux;
 
 public final class RuntimeUtils {
 
 	private RuntimeUtils() { }
 
 	public static void die(String msg) {
	    new Exception(msg).printStackTrace();
 		System.exit(1);
 	}
 }
