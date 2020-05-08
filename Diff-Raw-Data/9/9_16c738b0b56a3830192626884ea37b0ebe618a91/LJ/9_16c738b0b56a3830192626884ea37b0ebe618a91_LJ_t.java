 package com.icbat.game;
 
 import java.util.Date;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 
 /**
  * LJ, or Lumberjack, is a logging utility class to allow seam less logging to either console, file, or both
  * 
  * To use: Instantiate with the appropriate flags for logging, then use log()
  * If you don't instantiate first, console logging (only) will be used
  * 
  * When extending this: a boolean, edit constructor and the appropriate lines to log(string, string, string) 
  * 
  * @author icbat
  * @see log(String, String, String)
  * */
 public class LJ {
 	
 	// Categories
 	public static final int ERROR = 0;
 	public static final int LOG = 1;
 	public static final int DEBUG = 2;
 	
 	private static FileHandle logfile = null;
 	
 	/** Static class, doesn't need to instantiate */
 	private LJ() {}
 	
 	/**
 	 * Workhorse of the class. This method will handle the actual logging of events.
 	 * 
 	 * @param	message		The message to be logged
 	 * @param	className	Class Name of the caller	
 	 * @param	category	int (or constant) specifying ERROR, LOG, or DEBUG mode
 	 * */
 	public static void log( String message, String className, int category ) {
 		String toBeLogged = category + ", " + className + ", " + message;
 		
 		// Console Logging
 		
 		switch ( category ) {
 		case LOG:
 			Gdx.app.log( "", toBeLogged );
 			break;
 		case ERROR:
 			Gdx.app.error( "", toBeLogged);
 			Gdx.app.error( "", "Java heap in bytes:  " + Gdx.app.getJavaHeap() );
 			Gdx.app.error( "", "Native heap in bytes:  " + Gdx.app.getNativeHeap() );
 			break;
 		case DEBUG:
 			Gdx.app.debug( "", toBeLogged );
 			break;
 			// If something's wrong with the category, go ahead and log
 		default:
 			Gdx.app.log( "", toBeLogged );
 			break;
 		}
 		
 		// File logging
 		if ( logfile != null ) {
 			logfile.writeString( toBeLogged, true );
 			if ( category == ERROR ) {
 				logfile.writeString( "Java heap in bytes:  " + Gdx.app.getJavaHeap(), true );
 				logfile.writeString( "Native heap in bytes:  " + Gdx.app.getNativeHeap(), true );
 			}
 		}
 	}
 	
 	/**
 	 * Specify a category but no additional column (like variables, stack types, etc.)
 	 * 
 	 * @param	message		The message to be logged
 	 * @param	category	int (or constant) specifying ERROR, LOG, or DEBUG mode
 	 * */
 	public static void log( String message, int category ) {
 		LJ.log ( message, "", category );
 	}
 	
 	/**
 	 * By default, this will assume LOG-level of sensitivity
 	 * 
 	 * @param	message	The message to be logged
 	 * */
 	public static void log( String message ) {
 		LJ.log( message, LOG );
 	}
 	
 	/** Wrapper so this class can encapsulate all logging
 	 * ONLY pertains to console logging; file logging and any others will still log everything always
 	 * 
 	 * @see	Gdx.app
 	 *  */
 	public static void setLevel( int logLevel ) {
 		Gdx.app.setLogLevel( logLevel );
 	}
 
 	public static FileHandle getLogfile() {
 		return logfile;
 	}
 
 	public static void newLogfile(String gameName) {
		LJ.logfile = new FileHandle(gameName + "." + new Date().toString().replaceAll("\\W", "") + ".log");
		LJ.log("Logfile created at " + LJ.logfile.path());
 	}
 	
 }
