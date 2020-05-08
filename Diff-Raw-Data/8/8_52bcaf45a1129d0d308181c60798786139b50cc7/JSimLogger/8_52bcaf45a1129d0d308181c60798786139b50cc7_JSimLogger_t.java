 /**
   * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano
 
   * This program is free software; you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation; either version 2 of the License, or
   * (at your option) any later version.
 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
 
   * You should have received a copy of the GNU General Public License
   * along with this program; if not, write to the Free Software
   * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
   */
 
 package jmt.engine.log;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Properties;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * <p>Title: JSimLogger, a Log4J wrapper</p>
  * <p>Description: This class wraps the "Log4J" API by limiting
  * and extending the functionality provided by the original API,
  * for use with LogTunnel.</p>
  *
  * @author Gourry
  *         Date: 06-feb-2008
  *         Time: 15.56.05
  * @author Michael Fercu
  *         Date: 18-lug-2008
  *         Time: 09.40.33
  *
  */
 public class JSimLogger implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private static final short DEBUG = 0;
 	private static final String LOG4J_CONF = "log4j.conf";
 	public static final String LOGTUNNEL_LOGGER = "jmt.engine.NodeSections.LogTunnel";
 	public static final String STD_LOGGER = "JSim.log";
 	public static final String FILESEPARATOR = "/";
 	private static volatile LoggerManagerObject logmgr;
 
 	private transient org.apache.log4j.Logger logger;
 	static {
 		// Initialize only if somebody didn't already initialize this.
 		if (!LogManager.getCurrentLoggers().hasMoreElements()) {
 			URL props = JSimLogger.class.getResource(LOG4J_CONF);
 			if (props != null) {
 				PropertyConfigurator.configure(props);
 			} else {
 				System.err.println("Cannot find logProperties, using defaults");
 				//set stdout defaults
 				Properties p = new Properties();
 				p.setProperty("log4j.rootLogger", "DEBUG, stdout");
 				//p.setProperty("log4j.rootLogger","ALL, stdout");
 				p.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
 				p.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
 				p.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-5p [%t]- %m (%F:%L)%n");
 				PropertyConfigurator.configure(p);
 			}
 		}
 	}
 
 	/* Constructors */
 	private JSimLogger() {
 		logger = Logger.getRootLogger();
 		if (logmgr == null) {
 			logmgr = new LoggerManagerObject();
 		}
 	}
 
 	private JSimLogger(String loggerName) {
 		logger = Logger.getLogger(loggerName);
 		if (logmgr == null) {
 			logmgr = new LoggerManagerObject();
 		}
 
 	}
 
 	/* Methods */
 	public static JSimLogger getRootLogger() {
 		JSimLogger mylogger = new JSimLogger();
 		return mylogger;
 	}
 
 	public static JSimLogger getLogger() {
 		return getLogger(JSimLogger.STD_LOGGER);
 	}
 
 	public static JSimLogger getLogger(Object caller) {
 		JSimLogger mylogger = null;
 		if (caller != null) {
 			mylogger = JSimLogger.getLogger(caller.getClass());
 		} else {
 			mylogger = JSimLogger.getLogger();
 		}
 		return mylogger;
 	}
 
 	public static JSimLogger getLogger(Class callerClass) {
 		JSimLogger mylogger = getLogger(callerClass.getName());
 		return mylogger;
 	}
 
 	public static JSimLogger getLogger(String loggerName) {
 		JSimLogger mylogger;
 		if (loggerName == STD_LOGGER || loggerName.startsWith(LOGTUNNEL_LOGGER)) {
 			mylogger = new JSimLogger(loggerName);
 		} else {
			return getLogger(JSimLogger.STD_LOGGER);
 		}
 		return mylogger;
 	}
 
 	/**
 	 * Creates a new logger that uses FileAppender.  Called by LogTunnel.
 	 * @param className Name of sub-logger. (e.g., log4j.logger.<B>jmt.someengine.somenewclass</B>.Logger69)
 	 * @param filepath String with path to save the log File to (with or without trailing slash).
 	 * @param LPName Name of leaf of sub-logger and appender. (e.g., log4j.logger.jmt.engine.somenewclass.<B>Logger69</B>).
 	 * @param append Selects whether to append or replace the file in question.
 	 * @author Michael Fercu
 	 *		   Date: 08-aug-2008
 	 * @see jmt.engine.NodeSections.LogTunnel
 	 * @see Log4J#FileLogger
 	 * @return One ready-to-use JSimLogger with given parameters.
 	 */
 	public static JSimLogger makeNewFileLogger(String className, String filepath, String lpname, Boolean append) {
 		// Do a quick check for trailing slash on pathname
 		if (filepath != null) {
 			if ((filepath.endsWith(File.separator) == false) && (filepath.endsWith(FILESEPARATOR) == false) && (filepath.equals("") == false)) {
 				filepath = filepath + File.separator;
 			}
 		}
 
 		if (DEBUG >= 2) {
 			System.out.println("DBG [JSL] Made new file logger (" + className + ") at " + filepath + lpname + " (" + append + ") for ");
 			StackTraceElement[] e = new Exception().getStackTrace();
 			System.out.println("\t" + e[0].toString() + "\n\t" + e[1].toString() + "\n\t" + e[2].toString() + "\n");
 			e = null;
 		}
 
 		// Assemble the properties to reconfigure the logger with
 		Properties p = new Properties();
 		p.setProperty("log4j.logger." + className + lpname, "ALL,LT" + lpname);
 		p.setProperty("log4j.appender.LT" + lpname + "", "org.apache.log4j.FileAppender");
 		p.setProperty("log4j.appender.LT" + lpname + ".File", filepath + lpname + "");
 		p.setProperty("log4j.appender.LT" + lpname + ".append", append.toString());
 		p.setProperty("log4j.appender.LT" + lpname + ".threshold", "info");
 		p.setProperty("log4j.appender.LT" + lpname + ".layout", "org.apache.log4j.PatternLayout");
 		p.setProperty("log4j.appender.LT" + lpname + ".layout.ConversionPattern", "%m%n");
 		PropertyConfigurator.configure(p);
 
 		logmgr.add(filepath, lpname);
 
 		// as convenience, return the reconfigured logger
 		JSimLogger r = new JSimLogger(className + lpname);
 		return r;
 	}
 
 	/**
 	 * ReSets a FileAppender's filename and append properties.  Called by LogTunnel.
 	 * @param className Name of sub-logger. (e.g., log4j.logger.<B>jmt.someengine.somenewclass</B>.Logger69)
 	 * @param appenderName Name of leaf of sub-logger and appender. (e.g., log4j.appender.<B>some_appenderName</B>.etc.etc).
 	 * @param filepath String with path of the File to log to (with or without trailing slash) (extension is not appended)
 	 * @param filename String with name of the File to log to (without Path, current directory is used).
 	 * @param append This Boolean selects whether to append or replace the chosen File.
 	 * @author Michael Fercu
 	 * @see Log4J#FileLogger
 	 * @return One ready-to-use already-existing JSimLogger with given parameters.
 	 */
 	public static JSimLogger changeAppenderParameters(String className, String appenderName, String filepath, String filename, Boolean append) {
 		// Do a quick check to see if the C:\logdir\ or "/home/jmt/logs/" has a slash on the end.
 		if (filepath != null) {
 			if ((filepath.endsWith(File.separator) == false) && (filepath.equals("") == false)) {
 				filepath = filepath + File.separator;
 			}
 		}
 
 		if (DEBUG >= 2) {
 			System.out.println("DBG [JSL] Change file logger (" + className + "," + appenderName + ")");
 			System.out.println("           at <" + filepath + "><" + filename + "> (" + (append.booleanValue() ? "append" : "replace") + ") by :");
 			StackTraceElement[] e = new Exception().getStackTrace();
 			System.out.println("   " + e[0].toString() + "\n" + "   " + e[1].toString() + "\n" + "   " + e[2].toString() + "\n");
 			e = null;
 		}
 
 		// re-configure the selected logger
 		Properties p = new Properties();
 		p.setProperty("log4j.logger." + className, "ALL," + appenderName);
 		p.setProperty("log4j.appender." + appenderName, "org.apache.log4j.FileAppender");
 		p.setProperty("log4j.appender." + appenderName + ".File", filepath + filename + "");
 		p.setProperty("log4j.appender." + appenderName + ".append", append.toString());
 		p.setProperty("log4j.appender." + appenderName + ".threshold", "info");
 		p.setProperty("log4j.appender." + appenderName + ".layout", "org.apache.log4j.PatternLayout");
 		p.setProperty("log4j.appender." + appenderName + ".layout.ConversionPattern", "%m%n");
 		PropertyConfigurator.configure(p);
 
 		logmgr.add(filepath, filename);
 
 		// as convenience, return the reconfigured logger
 		JSimLogger r = getLogger(className);
 		return r;
 	}
 
 	// file methods: close this appender instance -- not implemented - this is debug code:
 	public void closeCustomAppender(String className, String filepath, String filename) {
 		/*
 		java.util.Enumeration e = logger.getAllAppenders();
 		int i=0;
 		
 		while (e.hasMoreElements() == true)
 		{
 			i++;
 			System.out.println(i + ") " + ((org.apache.log4j.FileAppender)e.nextElement()).getName() );
 		}
 		
 		logger.getAppender("LOGTUNNEL").close();
 		System.out.println("Closed file with close("+filename+").");
 		*/
 	}
 
 	// printing methods:
 	public void debug(Object message) {
 		if (logger.isDebugEnabled() == true) {
 			logger.debug(message);
 		}
 	}
 
 	public void info(Object message) {
 		if (DEBUG >= 5) {
 			System.out.println("[JSL].info(" + logger.isInfoEnabled() + "): " + message);
 		}
 
 		if (logger.isInfoEnabled() == true) {
 			logger.info(message);
 		}
 	}
 
 	public void warn(Object message) {
 		logger.warn(message);
 	}
 
 	public void error(Object message) {
 		logger.error(message);
 	}
 
 	public void error(Throwable th) {
 		StringWriter sw = new StringWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		th.printStackTrace(pw);
 		String buffer = sw.toString();
 		logger.error(buffer);
 	}
 
 	public void fatal(Object message) {
 		logger.fatal(message);
 	}
 
 	public boolean isDebugEnabled() {
 		return logger.isDebugEnabled();
 
 	}
 
 	public boolean isInfoEnabled() {
 		return logger.isInfoEnabled();
 	}
 
 	/**
 	 * Access methods for logmgr.
 	 */
 	public int getHeaderWrittenFlag(String fp, String fn) {
 		return logmgr.getHeaderWrittenFlag(fp, fn);
 	}
 
 	public boolean setHeaderWrittenFlag(String fp, String fn, boolean value) {
 		return logmgr.setHeaderWrittenFlag(fp, fn, value);
 	}
 
 	/**
 	 * Manages the loggers for LogTunnel.
 	 */
 	private class LoggerManagerObject {
 		private volatile int numLoggers;
 		private volatile String[] loggerName;
 		private volatile String[] loggerPath;
 		private volatile boolean[] headerWritten;
 
 		LoggerManagerObject() {
 			numLoggers = 1;
 			loggerName = new String[1];
 			loggerPath = new String[1];
 			headerWritten = new boolean[1];
 
 			loggerName[0] = "global.csv";
 			loggerPath[0] = "";
 			headerWritten[0] = false;
 
 			System.out.println("");
 		}
 
 		// called when a new logger is created 
 		public void add(String filepath, String fn) {
 			String[] loggerNameNew = new String[numLoggers + 1];
 			String[] loggerPathNew = new String[numLoggers + 1]; // needed for static composition
 			boolean[] headerWrittenNew = new boolean[numLoggers + 1];
 
 			// stop if the item has already been added
 			for (int i = 0; i < numLoggers; i++) {
 				if (loggerName[i].equalsIgnoreCase(fn) == true) {
 					if (DEBUG > 0) {
 						System.out.println("LoggerManager add() called by \n  " + new Exception().getStackTrace()[1] + "\n  "
 								+ new Exception().getStackTrace()[2]);
 						System.out.println("Contents of " + numLoggers + " items: " + toString());
 					}
 
 					return;
 				}
 			}
 
 			// to add it we need to make the data-structure bigger 
 			for (int i = 0; i < numLoggers; i++) {
 				loggerNameNew[i] = loggerName[i];
 				loggerPathNew[i] = loggerPath[i];
 				headerWrittenNew[i] = headerWritten[i];
 			}
 
 			loggerName = loggerNameNew;
 			loggerPath = loggerPathNew;
 			headerWritten = headerWrittenNew;
 			numLoggers++;
 
 			// and then to add the new filename and path, and we're done!
 			loggerPath[numLoggers - 1] = filepath;
 			loggerName[numLoggers - 1] = fn;
 
 			// just a debug message
 			if (DEBUG > 0) {
 				System.out.println("LoggerManager add() called by \n  " + new Exception().getStackTrace()[1] + "\n  "
 						+ new Exception().getStackTrace()[2]);
 				System.out.println("Contents of " + numLoggers + " items: " + toString());
 			}
 		}
 
 		public int getHeaderWrittenFlag(String filepath, String filename) {
 			int retval = -1;
 
 			for (int i = 0; i < numLoggers; i++) {
 				if (loggerName[i].equalsIgnoreCase(filename)) {
 					retval = headerWritten[i] ? 1 : 0;
 				}
 			}
 
 			return retval;
 		}
 
 		public boolean setHeaderWrittenFlag(String filepath, String filename, boolean newvalue) {
 			// filepath is not yet implemented
 			filepath = "";
 
 			if (DEBUG > 0) {
 				System.out.println("LoggerManager setHdrWr() called by \n  " + new Exception().getStackTrace()[1] + "\n  "
 						+ new Exception().getStackTrace()[2]);
 				System.out.println("Contents of " + numLoggers + " items: " + toString());
 			}
 
 			// search to see if this file has already been set 
 			for (int i = 0; i < numLoggers; i++) {
 				if (loggerName[i].equalsIgnoreCase(filename)) {
 					headerWritten[i] = newvalue;
 					return headerWritten[i];
 				}
 			}
 
 			// if no such file has been set, try to add it, and set it
 			add(filepath, filename);
 			for (int i = 0; i < numLoggers; i++) {
 				if (loggerName[i].equalsIgnoreCase(filename)) {
 					headerWritten[i] = newvalue;
 					return headerWritten[i];
 				}
 			}
 
 			// on fail to add, exit shamefully
 			return false;
 		}
 
 		public final String toString() {
 			String s = "";
 
 			for (int i = 0; i < numLoggers; i++) {
 				s += "  [" + i + "] " + loggerName[i] + " = " + headerWritten[i] + ", ";
 			}
 
 			if (s.endsWith(", ") == true) {
 				return s.substring(0, s.length() - 2);
 			} else if (s.length() == 0) {
 				return "(no loggers)";
 			} else {
 				return s;
 			}
 		}
 
 	}
 
 }
