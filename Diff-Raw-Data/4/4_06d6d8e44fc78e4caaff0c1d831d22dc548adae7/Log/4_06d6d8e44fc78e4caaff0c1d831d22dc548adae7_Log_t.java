 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Properties;
 
 import android.content.Context;
 
 /**
  * @author David Bauduin
  */
 public class Log {
 
 	///////////////////////
 	//     Variables     //
 	///////////////////////
 	
 	public final static byte NONE = 0;
 	public final static byte ERROR = 1;
 	public final static byte WARNING = ERROR << 1;
 	public final static byte INFO = WARNING << 1;
 	public final static byte DEBUG = INFO << 1;
 	public final static byte VERBOSE = DEBUG << 1;
 
 	public final static byte ALL = ERROR + WARNING + INFO + DEBUG + VERBOSE;
 	public final static byte DEBUG_AND_LOWER = ERROR + WARNING + INFO + DEBUG;
 	public final static byte INFO_AND_LOWER = ERROR + WARNING + INFO;
 	public final static byte WARNING_AND_LOWER = ERROR + WARNING;
 
 	protected static boolean showLogs = false;
 	protected static String prefix = "";
 	protected static boolean logClassSimpleName = false;
 	protected static String dateFormatPattern = "MM/dd-kk:mm:ss";
 	
 	protected static boolean showVerbose = true;
 	protected static boolean showDebug = true;
 	protected static boolean showInfo = true;
 	protected static boolean showWarning = true;
 	protected static boolean showError = true;
 
 	protected static SimpleDateFormat dateFormat = null;
 	protected static HashSet<String> mutedClasses;
 	protected static HashSet<String> mutedPackages;
 	
 	private final static String EMPTY_MESSAGE = "NULL";
 	
 	private final static String fileName = "log.properties";
 	private final static String logKey = "showLogs";
 	private final static String prefixKey = "logPrefix";
 	private final static String logClassSimpleNameKey = "logClassSimpleName";
 	private final static String dateFormatKey = "dateFormat";
 	
 	///////////////////////
 	//    Constructors   //
 	///////////////////////
 	
 	protected Log() {
 		
 	}
 	
 	///////////////////////
 	//      Methods      //
 	///////////////////////
 	
 	public static void init(Context context, LogConfig logConfig) {
 		if (context != null) {
 			try {
 				Properties p = new Properties();
 				p.load(context.getAssets().open(fileName));
 				String value = p.getProperty(logKey);
 				if (value != null) {
 					showLogs = Boolean.valueOf(value);
 				}
 				String prefixValue = p.getProperty(prefixKey);
 				if (prefixValue != null) {
 					prefix = prefixValue;
 				}
 				String logClassSimpleNameValue = p.getProperty(logClassSimpleNameKey);
 				if (logClassSimpleNameValue != null) {
 					logClassSimpleName = Boolean.valueOf(logClassSimpleNameValue);
 				}
 				String _dateFormatPattern = p.getProperty(dateFormatKey);
 				if (_dateFormatPattern != null) {
 					dateFormatPattern = _dateFormatPattern;
 				}
 			} catch (Exception e) {
 				showLogs = false;
 				logClassSimpleName = false;
 			}
 		}
 		if (logConfig != null) {
 			dateFormat = new SimpleDateFormat(dateFormatPattern, Locale.getDefault());
 			
 			byte levels = logConfig.getLogLevel();
 			showVerbose = ((levels & VERBOSE) > 0);
 			showDebug = ((levels & DEBUG) > 0);
 			showInfo = ((levels & INFO) > 0);
 			showWarning = ((levels & WARNING) > 0);
 			showError = ((levels & ERROR) > 0);
 			
 			HashSet<Class<?>> classesToMute = logConfig.getClassesToMute();
 			if (classesToMute != null) {
 				HashSet<String> _mutedClasses = new HashSet<String>(classesToMute.size());
 				for (Class<?> c : classesToMute) {
 					_mutedClasses.add(c.getName());
 				}
 				mutedClasses = _mutedClasses;
 			}
 			mutedPackages = logConfig.getPackagesToMute();
 		}
 	}
 	
 	// Verbose
 	/**
 	 * Send a VERBOSE log message.
 	 * @param message The message you would like logged.
 	 */
 	public static void v(Object message) {
 		if (showLogs && showVerbose) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.v(getTag(tagAndClassName), getMessage(message));
 			}
 		}
 	}
 
 	/**
 	 * Send a VERBOSE log message and log the exception.
 	 * @param message The message you would like logged.
 	 * @param t An exception to log.
 	 */
 	public static void v(Object message, Throwable t) {
 		if (showLogs && showVerbose) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.v(getTag(tagAndClassName), getMessage(message), t);
 			}
 		}
 	}
 
 	// Debug
 	/**
 	 * Send a DEBUG log message.
 	 * @param message The message you would like logged.
 	 */
 	public static void d(Object message) {
 		if (showLogs && showDebug) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.d(getTag(tagAndClassName), getMessage(message));
 			}
 		}
 	}
 
 	/**
 	 * Send a DEBUG log message and log the exception.
 	 * @param message The message you would like logged.
 	 * @param t An exception to log.
 	 */
 	public static void d(Object message, Throwable t) {
 		if (showLogs && showDebug) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.d(getTag(tagAndClassName), getMessage(message), t);
 			}
 		}
 	}
 
 	// Info
 	/**
 	 * Send a INFO log message.
 	 * @param message The message you would like logged.
 	 */
 	public static void i(Object message) {
 		if (showLogs && showInfo) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.i(getTag(tagAndClassName), getMessage(message));
 			}
 		}
 	}
 
 	/**
 	 * Send a INFO log message and log the exception.
 	 * @param message The message you would like logged.
 	 * @param t An exception to log.
 	 */
 	public static void i(Object message, Throwable t) {
 		if (showLogs && showInfo) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.i(getTag(tagAndClassName), getMessage(message), t);
 			}
 		}
 	}
 
 	// Warning
 	/**
 	 * Send a WARNING log message.
 	 * @param message The message you would like logged.
 	 */
 	public static void w(Object message) {
 		if (showLogs && showWarning) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.w(getTag(tagAndClassName), getMessage(message));
 			}
 		}
 	}
 
 	/**
 	 * Send a WARNING log message and log the exception.
 	 * @param message The message you would like logged.
 	 * @param t An exception to log.
 	 */
 	public static void w(Object message, Throwable t) {
 		if (showLogs && showWarning) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.w(getTag(tagAndClassName), getMessage(message), t);
 			}
 		}
 	}
 
 	// Error
 	/**
 	 * Send a ERROR log message.
 	 * @param message The message you would like logged.
 	 */
 	public static void e(Object message) {
 		if (showLogs && showError) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.e(getTag(tagAndClassName), getMessage(message));
 			}
 		}
 	}
 
 	/**
 	 * Send a ERROR log message and log the exception.
 	 * @param message The message you would like logged.
 	 * @param t An exception to log.
 	 */
 	public static void e(Object message, Throwable t) {
 		if (showLogs && showError) {
 			String[] tagAndClassName = getTagAndClassName();
 			if (canLog(getClassName(tagAndClassName))) {
 				android.util.Log.e(getTag(tagAndClassName), getMessage(message), t);
 			}
 		}
 	}
 
 	// Log authorization
 	/**
 	 * Checks to see whether or not a log for the specified class and level is possible.
 	 * @param classToLog The class to check.
 	 */
 	public static boolean canLog(Class<?> classToLog, byte level) {
 		if (!showLogs || classToLog == null) {
 			return false;
 		}
 
 		boolean canLog = true;
 		if ((level & ERROR) > 0) {
 			canLog = showError;
 		} else if ((level & WARNING) > 0) {
 			canLog = showWarning;
 		} else if ((level & INFO) > 0) {
 			canLog = showInfo;
 		} else if ((level & DEBUG) > 0) {
 			canLog = showDebug;
 		} else if ((level & VERBOSE) > 0) {
 			canLog = showVerbose;
 		} else {
 			return false;
 		}
 		
 		if (canLog) {
 			String tag = classToLog.getName();
 			if (mutedClasses != null) {
 				canLog = !mutedClasses.contains(tag);
 			}
 			if (canLog && mutedPackages != null) {
 				String subPackage = tag;
 				int pointIndex = subPackage.lastIndexOf('.');
 				while (canLog && pointIndex != -1) {
 					subPackage = subPackage.substring(0, pointIndex);
 					canLog = mutedPackages.contains(subPackage);
 					pointIndex = tag.lastIndexOf('.');
 				}
 			}
 		}
 		
 		return canLog;
 	}
 	
 	protected static boolean canLog(String tag) {
 		if (tag == null) {
 			return false;
 		}
 		boolean canLog = true;
 		if (mutedClasses != null) {
 			canLog = !mutedClasses.contains(tag);
 		}
 		if (canLog && mutedPackages != null) {
 			String subPackage = tag;
 			int pointIndex = subPackage.lastIndexOf('.');
 			while (canLog && pointIndex != -1) {
 				subPackage = subPackage.substring(0, pointIndex);
				canLog = !mutedPackages.contains(subPackage);
				pointIndex = subPackage.lastIndexOf('.');
 			}
 		}
 		return canLog;
 	}
 	
 	///////////////////////
 	// Getters / Setters //
 	///////////////////////
 	
 	protected static String[] getTagAndClassName() {
 		String tag = null;
 		StackTraceElement[] stackStrace = Thread.currentThread().getStackTrace();
 		StackTraceElement element = stackStrace[4];
 		String fullClassName = element.getClassName();
 		String className = fullClassName;
 		if (logClassSimpleName) {
 			try {
 				className = Class.forName(fullClassName).getSimpleName();
 			} catch (Exception e) { }
 		}
 		if (prefix != null && prefix.length() > 0) {
 			tag = prefix + className;
 		} else {
 			tag = className;
 		}
 		if (dateFormatPattern != null && dateFormatPattern.length() > 0) {
 			tag += " (" + dateFormat.format(new Date()) + ")";
 		}
 		
 		String[] tagAndClassName = {tag, fullClassName}; 
 		return tagAndClassName;
 	}
 	
 	protected static String getTag(String[] tagAndClassName) {
 		return tagAndClassName[0];
 	}
 	
 	protected static String getClassName(String[] tagAndClassName) {
 		return tagAndClassName[1];
 	}
 	
 	protected static String getMessage(Object message) {
 		return (message == null ? EMPTY_MESSAGE : message.toString()); 
 	}
 
 }
