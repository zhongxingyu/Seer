 package org.uigl.logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 public final class UIGLLog {
 	private final static String className = UIGLLog.class.getName();
 	private final static Logger LOGGER = Logger.getLogger(className);
 
 	public static Logger getLogger() {
 		return LOGGER;
 	}
 	
 	public static void logToFile(final String logFile) throws IOException {
 		final PrintWriter fileOut = new PrintWriter(new File(logFile));
 		
 		Handler logHandler = new Handler() {
 			
 			@Override
 			public void publish(LogRecord record) {
 				Date currentTime = new Date(record.getMillis());
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
 				
 				fileOut.write(sdf.format(currentTime));
 				fileOut.write('\t');
 				fileOut.write(record.getLevel().getName());
 				fileOut.write('\t');
 				fileOut.write(record.getSourceClassName());
 				fileOut.write('\t');
 				fileOut.write(record.getSourceMethodName());
 				fileOut.write('\n');
 				fileOut.write(record.getMessage());
 				if (record.getThrown() != null) {
					fileOut.write('\n');
					fileOut.write(record.getThrown().getMessage());
 					fileOut.write('\n');
 					record.getThrown().printStackTrace(fileOut);
 				}
 				fileOut.write('\n');
 			}
 			
 			@Override
 			public void flush() {
 				fileOut.flush();
 			}
 			
 			@Override
 			public void close() throws SecurityException {
 				fileOut.close();
 			}
 		};
 		LOGGER.addHandler(logHandler);
 	}
 	
 	public static void s(String msg) {
 		LOGGER.severe(msg);
 	}
 	
 	public static void s(String sourceClass, String sourceMethod, String msg) {
 		LOGGER.logp(Level.SEVERE, sourceClass, sourceMethod, msg);
 	}
 	
 	public static void s(String sourceClass, String sourceMethod, String msg, Throwable thrown) {
 		LOGGER.logp(Level.SEVERE, sourceClass, sourceMethod, msg, thrown);
 	}
 	
 	public static void w(String msg) {
 		LOGGER.warning(msg);
 	}
 	
 	public static void w(String sourceClass, String sourceMethod, String msg) {
 		LOGGER.logp(Level.WARNING, sourceClass, sourceMethod, msg);
 	}
 	
 	public static void w(String sourceClass, String sourceMethod, String msg, Throwable thrown) {
 		LOGGER.logp(Level.WARNING, sourceClass, sourceMethod, msg, thrown);
 	}
 	
 	public static void i(String msg) {
 		LOGGER.info(msg);
 	}
 	
 	public static void i(String sourceClass, String sourceMethod, String msg) {
 		LOGGER.logp(Level.INFO, sourceClass, sourceMethod, msg);
 	}
 	
 	public static void i(String sourceClass, String sourceMethod, String msg, Throwable thrown) {
 		LOGGER.logp(Level.INFO, sourceClass, sourceMethod, msg, thrown);
 	}
 	
 	public static void c(String msg) {
 		LOGGER.config(msg);
 	}
 	
 	public static void c(String sourceClass, String sourceMethod, String msg) {
 		LOGGER.logp(Level.CONFIG, sourceClass, sourceMethod, msg);
 	}
 	
 	public static void c(String sourceClass, String sourceMethod, String msg, Throwable thrown) {
 		LOGGER.logp(Level.CONFIG, sourceClass, sourceMethod, msg, thrown);
 	}
 	
 	public static void logp(Level level, String sourceClass, String sourceMethod, String msg) {
 		LOGGER.logp(level, sourceClass, sourceMethod, msg);
 	}
 	
 	public static void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
 		LOGGER.logp(level, sourceClass, sourceMethod, msg, thrown);
 	}
 }
