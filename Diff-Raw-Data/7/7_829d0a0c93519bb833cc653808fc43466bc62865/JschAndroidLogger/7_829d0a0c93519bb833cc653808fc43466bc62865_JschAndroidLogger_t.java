 package com.example.git;
 
 import android.util.Log;
 
 import com.jcraft.jsch.Logger;
 
 /** This class provides a custom logger for the Java Secure Channel library.
  * 	It forwards all messages to the Android API for sending log output. 
  */
 public class JschAndroidLogger implements Logger {
 
 	/**
 	 * The name thats used as identifier for the log output.
 	 */
 	private final String loggerName = "JschLogger";
 
 	/**
 	 * Checks if logging of some level is actually enabled.
	 * @param level	The level that is checked.
 	 * @return	Always true to enable logging for all levels.
 	 */
 	public boolean isEnabled(int level) {
 		return true;
 	}
 
 	/**
 	 * Used to log the messages from Jsch to the Android API.
	 * @param level The level	of the message.
	 * @param message The message that will be logged.
 	 */
 	public void log(int level, String message) {
 		// There is no documentation about the range of the log level
 		// so there is always the same action performed for all log levels
 		Log.d(loggerName, message);
 	}
 }
