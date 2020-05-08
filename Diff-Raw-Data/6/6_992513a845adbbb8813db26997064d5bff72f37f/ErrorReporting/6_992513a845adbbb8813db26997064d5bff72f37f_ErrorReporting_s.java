 package com.comphenix.xp.listeners;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.bukkit.Bukkit;
 
 import com.comphenix.xp.Debugger;
 import com.google.common.primitives.Primitives;
 
 public class ErrorReporting {
 	
 	public static final String SECOND_LEVEL_PREFIX = "  ";
 	public static final String DEFAULT_PREFIX = "  ";
 	public static final String DEFAULT_SUPPORT_URL = "http://dev.bukkit.org/server-mods/experiencemod/";
 	
 	// We don't want to spam the server
 	public static final int DEFAULT_MAX_ERROR_COUNT = 10;
 	
 	/**
 	 * The default error reporting mechanism in ExperienceMod.  
 	 */
 	public static ErrorReporting DEFAULT = new ErrorReporting(DEFAULT_PREFIX, DEFAULT_SUPPORT_URL);
 	
 	protected String prefix;
 	protected String supportURL;
 	
 	protected int errorCount;
 	protected int maxErrorCount;
 	protected Logger logger;
 
 	// Map of global objects
 	protected Map<String, Object> globalParameters = new HashMap<String, Object>();
 	
 	public ErrorReporting(String prefix, String supportURL) {
 		this(prefix, supportURL, DEFAULT_MAX_ERROR_COUNT, getBukkitLogger());
 	}
 
 	public ErrorReporting(String prefix, String supportURL, int maxErrorCount, Logger logger) {
 		this.prefix = prefix;
 		this.supportURL = supportURL;
 		this.maxErrorCount = maxErrorCount;
 		this.logger = logger;
 	}
 
 	// Attempt to get the logger.
 	private static Logger getBukkitLogger() {
 		try {
 			return Bukkit.getLogger();
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Prints a detailed error report about an unhandled exception.
 	 * @param debugger - the listening debugger.
 	 * @param sender - the object containing the caller method.
 	 * @param error - the exception that was thrown in the caller method.
 	 * @param parameters - parameters from the caller method.
 	 */
 	public void reportError(Debugger debugger, Object sender, Throwable error, Object... parameters) {
 		
 		// Do not overtly spam the server!
 		if (++errorCount > maxErrorCount) {
 			String maxReached = String.format("Reached maxmimum error count. Cannot pass error %s from %s.", error, sender);
 			
 			// Print debug and to the log
 			if (debugger != null)
 				debugger.printDebug(sender, maxReached);
 			System.err.println(maxReached);
 			return;
 		}
 		
 		StringWriter text = new StringWriter();
 		PrintWriter writer = new PrintWriter(text);
 
 		// Helpful message
 		writer.println("[ExperienceMod] INTERNAL ERROR!");
 	    writer.println("If this problem has't already been reported, please open a ticket");
 	    writer.println("at " + supportURL + " with the following data:");
 	    
 	    // Now, let us print important exception information
 		writer.println("          ===== STACK TRACE =====");
 
 		if (error != null) 
 			error.printStackTrace(writer);
 		
 		// Data dump!
 		writer.println("          ===== DUMP =====");
 		
 		// Relevant parameters
 		if (parameters != null && parameters.length > 0) {
 			writer.println("Parameters:");
 			
 			// We *really* want to get as much information as possible
 			for (Object param : parameters) {
 				writer.println(addPrefix(getStringDescription(param), SECOND_LEVEL_PREFIX));
 			}
 		}
 		
 		// Global parameters
 		for (String param : globalParameters()) {
 			writer.println(SECOND_LEVEL_PREFIX + param + ":");
 			writer.println(addPrefix(getStringDescription(getGlobalParameter(param)), 
 					SECOND_LEVEL_PREFIX + SECOND_LEVEL_PREFIX));
 		}
 		
 		// Now, for the sender itself
 		writer.println("Sender:");
 		writer.println(addPrefix(getStringDescription(sender), SECOND_LEVEL_PREFIX));
 		
 		// Inform of this occurrence
 		if (debugger != null) {	
 			debugger.printWarning(this, "Error %s occured in %s.", error, sender);
 		}
 				
 		// Make sure it is reported
 		logger.severe(addPrefix(text.toString(), prefix));
 	}
 	
 	/**
 	 * Adds the given prefix to every line in the text.
 	 * @param text - text to modify.
 	 * @param prefix - prefix added to every line in the text.
 	 * @return The modified text.
 	 */
 	protected String addPrefix(String text, String prefix) {
 		
 		return text.replaceAll("(?m)^", prefix);
 	}
 	
 	protected String getStringDescription(Object value) {
 		
 		// We can't only rely on toString.
 		if (value == null) {
 			return "[NULL]";
 		} if (isSimpleType(value)) {
 			return value.toString();
 		} else {
 			return (ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE, false, null));
 		}
 	}
 	
 	/**
 	 * Determine if the given object is a wrapper for a primitive/simple type or not.
 	 * @param test - the object to test.
 	 * @return TRUE if this object is simple enough to simply be printed, FALSE othewise.
 	 */
 	protected boolean isSimpleType(Object test) {
 		return test instanceof String || Primitives.isWrapperType(test.getClass());
 	}
 	
 	public int getErrorCount() {
 		return errorCount;
 	}
 
 	public void setErrorCount(int errorCount) {
 		this.errorCount = errorCount;
 	}
 
 	public int getMaxErrorCount() {
 		return maxErrorCount;
 	}
 
 	public void setMaxErrorCount(int maxErrorCount) {
 		this.maxErrorCount = maxErrorCount;
 	}
 	
 	/**
 	 * Adds the given global parameter. It will be included in every error report.
 	 * @param key - name of parameter.
 	 * @param value - the global parameter itself.
 	 */
 	public void addGlobalParameter(String key, Object value) {
 		globalParameters.put(key, value);
 	}
 	
 	public Object getGlobalParameter(String key) {
 		return globalParameters.get(key);
 	}
 	
 	public void clearGlobalParameters() {
 		globalParameters.clear();
 	}
 	
 	public Set<String> globalParameters() {
 		return globalParameters.keySet();
 	}
 	
 	public String getSupportURL() {
 		return supportURL;
 	}
 
 	public void setSupportURL(String supportURL) {
 		this.supportURL = supportURL;
 	}
 	
 	public String getPrefix() {
 		return prefix;
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 	
 	public Logger getLogger() {
 		return logger;
 	}
 
 	public void setLogger(Logger logger) {
 		this.logger = logger;
 	}
 }
