 package mk.edu.ii.app;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 public class Application {
 
 	private static Properties properties;
 
 	public Application() {
 		init();
 	}
 
 	public void init() {
 		Logger globalLogger = Logger.getLogger("");
 		properties = new Properties();
 		
 		boolean inputError = false;
 		try {
 			properties.load(new FileInputStream("app.properties"));
 		} catch (FileNotFoundException e) {
 			globalLogger.fine("app.properties does not exist.");
 			inputError = true;
 			e.printStackTrace();
 		} catch (IOException e) {
 			globalLogger.fine("reading app.properties I/O exception.");
 			inputError = true;
 			e.printStackTrace();
 		}
 
 		if (inputError) {
 			globalLogger.setLevel(Constants.defLogLevel);
 			// TODO: Set the engine maximum number of threads
 		} else {
 			String value;
 
 			// seting up logging level
 			try {
 				if ((value = properties.getProperty(Constants.logLevel)) != null)
					Level level = toLogLevel(Integer.parseInt(value));
					globalLogger.setLevel(level);
					globalLogger.info("Logging level set to:" + level.getName());
 			} catch (NumberFormatException e) {
 				globalLogger.setLevel(Constants.defLogLevel);
 			}
 
 			// setting up maximum number of thread per engine
 			try {
 				if ((value = properties
 						.getProperty(Constants.maxNumberOfThreads)) != null) {
 
 				}
 			} catch (NumberFormatException e) {
 				// Engine defualt maximum number of threads
 			}
 
 		}
 
 		try {
 			FileHandler fileHandler = new FileHandler("log.txt");
 			SimpleFormatter formatter = new SimpleFormatter();
 			fileHandler.setFormatter(formatter);
 			globalLogger.addHandler(fileHandler);
 		} catch (SecurityException e) {
 			globalLogger
 					.fine("Probobly the application does not have permissions to write logs to the file system");
 			e.printStackTrace();
 		} catch (IOException e) {
 			globalLogger.fine("I/O error while trying to write to log.txt");
 			e.printStackTrace();
 		}
 
 	}
 
 	private Level toLogLevel(Integer level) {
 		if (level <= 200)
 			return Level.ALL;
 		if (level == 300)
 			return Level.FINEST;
 		if (level == 400)
 			return Level.FINER;
 		if (level == 500)
 			return Level.FINE;
 		if (level == 700)
 			return Level.CONFIG;
 		if (level == 800)
 			return Level.INFO;
 		if (level == 900)
 			return Level.WARNING;
 		if (level == 1000)
 			return Level.SEVERE;
 		if (level > 1000)
 			return Level.OFF;
 
 		//default level but it is should never get to here
 		return Level.INFO;
 	}
 }
