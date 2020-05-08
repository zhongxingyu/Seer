 package com.podts.towns.server.console;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 
 import com.podts.towns.server.Server;
 import com.podts.towns.server.threading.impl.ConsoleInputHandler;
 
 public class LogFormatter extends Formatter {
 	
 	public static void init() {
 		
 		Server.getLogger().setUseParentHandlers(false);
 		Handler loghandler = new ConsoleHandler();
 		loghandler.setFormatter(new LogFormatter());
 		Server.getLogger().addHandler(loghandler);
 		
 	}
 	
 	@Override
 	public String format(LogRecord record) {
 		
 		Date date = new Date(record.getMillis());
 		Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(date);
 		
 		if (ConsoleInputHandler.getReader() != null) {
			return "\r[" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "] "
 					+ record.getLoggerName() + " " + record.getLevel() + ": " +record.getMessage() + "\n" ;
 		}
 		
 		return "[" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "] "
 		+ record.getLoggerName() + " " + record.getLevel() + ": " +record.getMessage() + "\n" ;
 		
 	}
 
 }
