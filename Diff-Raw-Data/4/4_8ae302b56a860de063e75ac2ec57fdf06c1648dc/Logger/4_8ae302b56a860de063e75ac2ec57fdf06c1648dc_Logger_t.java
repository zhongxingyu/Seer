 package main;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * This class is used to write messages to the user. If setVerbos(true) was
  * called, all messages will be printed, if not, only messages with
  * Severity.ERROR will be printed.
  * 
  * @author Daniel
  * 
  */
 public class Logger {
 
 	private final SimpleDateFormat formatter = new SimpleDateFormat(
 			"yyyyMMdd-hhmmss");
 	private boolean verbose = false;
 
 	public enum Severity {
 		ERROR, NORMAL;
 	}
 
 	/**
 	 * Writes messaged to the out stream.
 	 * 
 	 * @param message
 	 *            the message needed to be written.
 	 * @param severity
 	 *            the severity of the message.
 	 */
 	public void sendLog(String message, Severity severity) {
		if (severity.equals(Severity.ERROR)) {
			System.err.println(getDate() + " " + message);
		} else if (verbose ) {
 			System.out.println(getDate() + " " + message);
 		}
 	}
 
 	/**
 	 * Sets verbose to the new value.
 	 * 
 	 * @param verbose
 	 *            new value for verbose
 	 */
 	public void setVerbos(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	private String getDate() {
 		return formatter.format(new Date());
 	}
 }
