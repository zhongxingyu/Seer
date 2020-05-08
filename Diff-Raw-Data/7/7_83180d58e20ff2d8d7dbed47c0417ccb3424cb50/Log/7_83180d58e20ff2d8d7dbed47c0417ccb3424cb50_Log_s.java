 package stockmaster.util;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Log {
 
 	public static LogLevel logLevel = LogLevel.INFO;
 	private static String logFile = "StockMaster_";
 	private final static DateFormat dateTimeFormat = new SimpleDateFormat(
 			"yyyyMMdd hh:mm:ss");
 	
 	private final static SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyyMMdd");
 
 	public enum LogLevel {
 		NONE, INFO, DEBUG, ERROR, ALGO_TESTING
 	}
 
 	public static void debug(Object obj, String msg) {
 		if (logLevel == LogLevel.DEBUG)
 			msg(obj, msg, "DEBUG");
 	}
 
 	public static void info(Object obj, String debug) {
 		if ((logLevel == LogLevel.INFO) || (logLevel == LogLevel.DEBUG))
 			msg(obj, debug, "INFO");
 	}
 	
 	public static void algoTesting(Object obj, String debug) {
 		if (logLevel == LogLevel.ALGO_TESTING)
 			msg(obj, debug, "ALGO TESTING");
 	}
 	
 	public static void error(Object obj, String debug) {
 		if (logLevel != LogLevel.NONE)
 			msg(obj, debug, "ERROR");
 	}
 
 	private static void msg(Object obj, String msg, String level) {
 		String timestamp = Log.dateTimeFormat.format(new Date());
		System.out.println("[" + level + "/"+timestamp+"] " + obj.getClass().getName()
 				+ " - " + msg);
 		
 		write("[" + level + "/"+timestamp+"] " + obj.getClass().getName()
 				+ " - " + msg);
 	}
 
 	public static void setLogFilename(String filename) {
 		logFile = filename;
 		new File(filename).delete();
 
 		try {
 			write("LOG file : " + filename);
 		} catch (Exception e) {
 			System.out.println(stack2string(e));
 		}
 
 	}
 
 	public static void write(String msg) {
 		write(logFile, msg);
 	}
 
 	public static void write(Exception e) {
 		write(logFile, stack2string(e));
 	}
 
 	public static void write(String file, String msg) {
 		try {
 			Date now = new Date();
 			String currentTime = Log.dateTimeFormat.format(now);
 			FileWriter aWriter = new FileWriter(file+getCurrentDate(new Date())+".txt", true);
 			aWriter.write(currentTime + " " + msg
 					+ System.getProperty("line.separator"));
 			aWriter.flush();
 			aWriter.close();
 		} catch (Exception e) {
 			System.out.println(stack2string(e));
 		}
 	}
 
 	private static String stack2string(Exception e) {
 		try {
 			StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			e.printStackTrace(pw);
 			return "------\r\n" + sw.toString() + "------\r\n";
 		} catch (Exception e2) {
 			return "bad stack2string";
 		}
 	}
 	
 	public static String getCurrentDate(Date date){
 		return dateOnlyFormat.format(date);
 	}
 	
 	public static String formateDateTime(Date date) {
 		return dateTimeFormat.format(date);
 	}
 	
 	public static Date getFormattedDateTime(String date){
 		try {
 			return dateTimeFormat.parse(date);
 		} catch (ParseException e) {
 			Log.error(null,"Invalid date conversion");
 		}
 		return null;
 	}
 }
