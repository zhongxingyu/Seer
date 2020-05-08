 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.calendar;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 
 import javax.script.Invocable;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import de.mbaaba.util.Configurator;
 import de.mbaaba.util.Logger;
 
 /**
  * A class that encapsulates some script that acts as a {@link ICalendarFilter}. 
  */
 public class ScriptFilter implements ICalendarFilter {
 
 	/**
 	 * A logger for this class.
 	 */
 	private static final Logger LOG = new Logger(ScriptFilter.class);
 
 	/** The used scripting-engine. */
 	private ScriptEngine scriptEngine;
 
 	/** The file that contains the script. */
 	private File file;
 
 	/** The reader that is used to read the script. */
 	private FileReader reader;
 
 	/**
 	* if set to "yes", failures (exceptions) within a script will be ignored and the entry will be synced.
 	* if set to "no", all failures will lead to a non-sync of the entry. 	 
 	* */
 	private boolean ignoreFailures;
 
 	public ScriptFilter(String aScriptName, Configurator aConfigurator) throws FileNotFoundException {
 		ignoreFailures = aConfigurator.getProperty("filters.ignoreFailures", "yes").equals("yes");
 		file = new File(aScriptName);
 		reader = new FileReader(file);
 
 		final String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
 
 		final ScriptEngineManager manager = new ScriptEngineManager();
 		scriptEngine = manager.getEngineByExtension(extension);
 		scriptEngine.put("configurator", aConfigurator);
 	}
 
 	/**
 	 * This method runs the script and returns the scripts return value as its result.
 	 *
 	 * @see de.mbaaba.calendar.ICalendarFilter#passes(de.mbaaba.calendar.ICalendarEntry)
 	 */
 	@Override
 	public boolean passes(ICalendarEntry aParamCalendarEntry) {
 		scriptEngine.put("calendarEntry", aParamCalendarEntry);
 		boolean returnValue;
 
 		try {
 			scriptEngine.eval(reader);
 			returnValue = (Boolean) ((Invocable) scriptEngine).invokeFunction("filter");
 		} catch (ScriptException e) {
 			LOG.error("Error while executing script " + file.getAbsolutePath() + ": " + e.getMessage(), e);
 			return ignoreFailures;
 		} catch (NoSuchMethodException e) {
 			LOG.error("Error while executing script " + file.getAbsolutePath() + ": " + e.getMessage(), e);
 			return ignoreFailures;
 		}
 		return returnValue;
 	}
 }
