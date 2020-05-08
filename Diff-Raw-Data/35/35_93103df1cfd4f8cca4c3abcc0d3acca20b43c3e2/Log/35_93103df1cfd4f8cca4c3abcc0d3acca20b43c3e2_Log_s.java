 package at.roadrunner.android.model;
 
 public class Log {
 
 	public static String TYPE_KEY = "type";
 	public static String LOG_TYPE_KEY = "logType";
 	public static String ITEM_KEY = "item";
 	
 	public static String TYPE_VALUE = "log";
 	
 	public enum LogType {
 		
 		REGISTER,
 		UNREGISTER,
 		LOAD,
 		UNLOAD,
 		TRACK,
 		TEMPSENSOR,
 		POSSENSOR;
		
		
 	}
	
	
	
 }
