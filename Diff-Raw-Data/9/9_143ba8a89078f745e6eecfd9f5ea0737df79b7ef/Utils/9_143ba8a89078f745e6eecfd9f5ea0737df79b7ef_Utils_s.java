package pt.piskeirinho.multi_actor_harvester_with_remoting.config_and_utils;
 
 import java.text.DecimalFormat;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 public final class Utils {
 
 	static {
 		ConsoleAppender console = new ConsoleAppender(); // create appender
 		// configure the appender
 		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
 		console.setLayout(new PatternLayout(PATTERN));
 		console.setThreshold(Level.FATAL);
 		console.activateOptions();
 		// add appender to any Logger (here is root)
 		Logger.getRootLogger().addAppender(console);
 	}
 
 	public static double getElapsedTime(long startTime) {
 		return ((double) (System.nanoTime() - startTime)) / 1000000000.0;
 	}
 
 	public static void output(String message, double elapsedTime) {
 		System.out.println("["
 				+ new DecimalFormat("00000.0000000000").format(elapsedTime)
 				+ "] " + message);
 	}
 }
