 package lib;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import controllers.File_System_Controller;
 
 
 /**
  * <p>Facilitates the logging of warning, error, etc., messages which
  * can be used by the developer or user to debug errors within the
  * program.
  * 
  * @author Adam Childs
  */
 public class Logger
 {
	static OSProperties osp = new OSProperties();
	static String toFile = new File_System_Controller().getModel().findFile("logs" + osp.getSeparator() + "log.txt");
 
 	/**
 	 * Log level
 	 * @author Adam Childs
 	 * @since 3/10/2013
 	 */
 	public static enum Level
 	{
 		ERROR("ERROR"), // Some programming error occurred (e.g. an exception)
 		INFO("INFO"), // Informational message
 		SYSTEM("SYSTEM"), // System level message
 		USER_ERROR("USER_ERROR"), // User produced error (e.g. required field not filled in)
 		WARNING("WARNING"); // Warning, but not error
 		
 		private String level;
 		Level(String l)
 		{
 			this.level = l;
 		}
 		
 		public String toString()
 		{
 			return this.level;
 		}
 	};
 
 	/**
 	 * <p>General logging constructor when simply trying to write
 	 * out a log message to the log file.
 	 */
 	public Logger()
 	{
		
 	}
 
 	/**
 	 * <p>Writes the given message to the program log file, as well as
 	 * stating the date, time, and status of the message (error, warning, etc.).
 	 * 
 	 * @param message The message to write to the log file
 	 * @param level The message status level (WARNING, ERROR, INFO, etc.)
 	 */
 	public static void write(String message, Level level)
 	{
 		try {
 			FileWriter fw = new FileWriter(toFile, true);
 			BufferedWriter out = new BufferedWriter(fw);
 
 			Date date = new Date();
 
 			String logMessage = "[" + date.toString() + "] [" + level + "]: " + message;
 
 			out.append(logMessage);
 			out.newLine();
 
 			out.close();			
 			fw.close();
 
 			System.out.println(logMessage);
 		} catch (IOException e) {
 			System.out.println( "IOException while writing to the file [" + toFile + "]" );
 		}
 	}
 }
