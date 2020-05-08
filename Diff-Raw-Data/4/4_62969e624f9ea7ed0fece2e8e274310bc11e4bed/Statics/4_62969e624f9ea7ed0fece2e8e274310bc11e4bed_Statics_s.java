 package uk.co.thefailboat.TFBWalls;
 
 import java.util.logging.Logger;
 
 /**
  * Statics is a class containing commonly used static variables
  */
 public class Statics {
 	/**
 	 * The Version should be prefixed with -DEV if it is not fully tested for release.
 	 */
 	public static String Version = "0.2-DEV";
 	/**
 	 * The logging prefix for every message. Should be left as the plugin name.
 	 */
     public static String prefix = "[TFB-Walls] ";
     /**
      * The Logger instance that is used to output data to the Minecraft console.
      */
 	public static Logger log = Logger.getLogger("Minecraft");
 	
 	public static int mapx = 286;
 	public static int mapz = -853;
	public static int mapdx = Math.abs(286 - 408);
	public static int mapdz = Math.abs(-853 - -731);
 }
