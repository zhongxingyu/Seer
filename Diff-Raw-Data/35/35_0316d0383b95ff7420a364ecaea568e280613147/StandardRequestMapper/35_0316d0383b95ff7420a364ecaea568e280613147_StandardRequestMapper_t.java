 /*
  * Copyright 2011-2013 Jeroen Meetsma - IJsberg
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.ijsberg.iglu.mvc.component;
 
 import org.ijsberg.iglu.configuration.Assembly;
 import org.ijsberg.iglu.configuration.ConfigurationException;
 import org.ijsberg.iglu.configuration.Startable;
 import org.ijsberg.iglu.logging.LogEntry;
 import org.ijsberg.iglu.mvc.IndentedConfigReaderMapping;
 import org.ijsberg.iglu.mvc.Mapping;
 import org.ijsberg.iglu.mvc.RequestDispatcher;
 import org.ijsberg.iglu.mvc.RequestMapper;
 import org.ijsberg.iglu.mvc.mapping.Process;
 import org.ijsberg.iglu.util.collection.CollectionSupport;
 import org.ijsberg.iglu.util.misc.StringSupport;
 import org.ijsberg.iglu.util.properties.PropertiesSupport;
 
 import java.io.Serializable;
 import java.util.*;
 
 
 /**
  * This component contains the mapping and the wiring to handle MVC requests.
  */
 public class StandardRequestMapper implements RequestMapper, Startable {
 	private Map<String, IndentedConfigReaderMapping> mappingMap = new HashMap();
 	private Properties mappingFilesMap;
 
 	private boolean autoReload;
 	private String defaultMappingName = "default";
 
 	private boolean isStarted = false;
 
 	private boolean loadSucceeded;
 	private ArrayList loadMessages = new ArrayList();
 
 	private Assembly assembly;
 
 	/**
 	 * Default constructor for plain instantiation as service.
 	 */
 	public StandardRequestMapper() {
 	}
 
 	public void register(Assembly assembly) {
 		this.assembly = assembly;
 	}
 
 	/**
 	 * Constructor for dedicated mapper use.
 	 *
 	 * @param mappingFilesMap
 	 * @param defaultMappingName
 	 */
 	public StandardRequestMapper(Assembly assembly, Properties mappingFilesMap, String defaultMappingName) {
 		this.assembly = assembly;
 		this.mappingFilesMap = mappingFilesMap;
 		this.defaultMappingName = defaultMappingName;
 		start();
 	}
 
 	/**
 	 * @return a simple text reporting details on the loading of mapping files
 	 */
 	public String getReport() {
 		StringBuffer result = new StringBuffer("Mappings:\n");
 
 		Iterator i = mappingMap.values().iterator();
 		while (i.hasNext()) {
 			Mapping mapping = (Mapping) i.next();
 
 			result.append("- " + mapping.getName() + ':' + (mapping.isLoaded() ? "loaded" : "not loaded") + System.getProperty("line.separator"));
 		}
 		//report the status of all the various mappings
 		result.append("\n\nMessages\n");
 		result.append(CollectionSupport.format(loadMessages, "\n"));
 
 		return result.toString();
 	}
 
 	/**
 	 * Loads mappings.
 	 */
 	public void start() {
 		boolean succeeded = true;
 		Iterator i = mappingFilesMap.keySet().iterator();
 		while (i.hasNext()) {
 			String mappingName = (String) i.next();
 			String fileName = mappingFilesMap.getProperty(mappingName);
 			System.out.println(new LogEntry("loading mapping '" + mappingName + "' from '" + fileName + '\''));
 			//TODO filename sometimes null
 			IndentedConfigReaderMapping mapping = new IndentedConfigReaderMapping(mappingName, fileName, assembly);
 			succeeded = succeeded && mapping.isLoaded();
 			if (!mapping.isLoaded()) {
 				System.out.println(new LogEntry("loading NOT succeeded", (Serializable) mapping.getLoadMessages()));
 			}
 			mappingMap.put(mappingName, mapping);
 		}
 		loadSucceeded = succeeded;
 		isStarted = true;
 	}
 
 	/**
	 * @return a list containing error- and warning messages that occurred while loading the mapping files
 	 */
 	public List getMessages() {
 		return loadMessages;
 	}
 
 
 	/**
 	 * @return true if loading the mapping definitions has succeeded
 	 */
 	public boolean isLoaded() {
 		return loadSucceeded;
 	}
 
 
 	/**
 	 * @return true if loading a particular mapping definition has succeeded
 	 */
 	public boolean isLoaded(String flowName) {
 		Mapping mapping = mappingMap.get(flowName);
 		if (mapping != null) {
 			return mapping.isLoaded();
 		}
 
 		return false;
 	}
 
 
 	public boolean isStarted() {
 		return isStarted;
 	}
 
 	/**
 	 * Doesn't do anything.
 	 */
 	public void stop() {
 		isStarted = false;
 	}
 
 	/**
 	 * Loads mapping values.
 	 *
 	 * @throws ConfigurationException if no properties section exists where mapping definition files are specified
 	 */
 	public void setProperties(Properties properties) throws ConfigurationException {
 //		initialRequest = application.getCurrentRequest();
 		autoReload = Boolean.valueOf(properties.getProperty("autoreload", "true"));
 
 		defaultMappingName = properties.getProperty("defaultmapping", defaultMappingName);
 
 /*		strict = section.getValue("strict", new GenericValue(strict), "abort mapping loading on errors").toBoolean().booleanValue();
         Environment.System.out.println("checking mapping strictly: " + strict);*/
 
 		mappingFilesMap = PropertiesSupport.getSubsection(properties, "mapping");
 	}
 
 
 	/**
 	 * @return true if one of the mapping definitions reports that it's been modified
 	 */
 	public boolean reloadIfUpdated() {
 		boolean result = false;
 
 		if (autoReload) {
 			synchronized (mappingMap) {
 				Iterator i = mappingMap.values().iterator();
 				while (i.hasNext()) {
 					IndentedConfigReaderMapping mapping = (IndentedConfigReaderMapping) i.next();
 
 					if (mapping.isModified()) {
 						System.out.println(new LogEntry("mapping '" + mapping.getName() + "' is modified, attempting reload"));
 						result = true;
 
 						mapping = new IndentedConfigReaderMapping(mapping.getName(), mapping.getFileName(), assembly);
 						//depends on strict
 						loadSucceeded = mapping.isLoaded();
 
 						if (mapping.isLoaded()) {
 							System.out.println(new LogEntry("mapping '" + mapping.getName() + "' reloaded"));
 							mappingMap.put(mapping.getName(), mapping);
 						} else {
 							System.out.println(new LogEntry("reload of mapping '" + mapping.getName() + "' failed\n" + mapping.getLoadMessages()));
 						}
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param processPath
 	 * @param requestProperties
 	 * @param frh
 	 * @return
 	 * @throws ConfigurationException
 	 */
 	public boolean processRequest(String processPath, Properties requestProperties, RequestDispatcher frh) throws ConfigurationException {
 		Collection processColl = StringSupport.split(processPath, "/");
 		String[] commandsArray = (String[]) processColl.toArray(new String[processColl.size()]);
 		return this.processRequest(commandsArray, requestProperties, frh);
 	}
 
 
 	/**
 	 * Locates the piece of mapping definition that fits the command,
 	 * being a mvc form, and hands the result exceptionHandler to the form for processing
 	 * <p/>
 	 * The command is a string that consists of maximum three words, separated by dots. The first
 	 * word specifies a mapping definition, the second word specifies a form and the third one
 	 * a particular event. If no form is specified, the RequestMapper will look for the form "index".
 	 * <p/>
 	 * If normal processing somehow does not lead to a dispatch, the request exceptionHandler is handed
 	 * to the error handling form of a mapping specification
 	 *
 	 * @param frh the object that knows how to carry out tasks, evaluate results and perform dispatches
 	 * @return true if the processing lead to a response
 	 * @throws ConfigurationException in case no dispatch has been reached
 	 */
 	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher frh) throws ConfigurationException {
 		String[] subProcessArray;
 		String mappingName;
 		if (processArray.length == 0) {
 			mappingName = this.defaultMappingName;
 			subProcessArray = new String[0];
 		} else {
 			mappingName = processArray[0];
 			subProcessArray = new String[processArray.length - 1];
 			System.arraycopy(processArray, 1, subProcessArray, 0, subProcessArray.length);
 		}
 
 		Mapping mapping = mappingMap.get(mappingName);
 		if (mapping == null) {
 			System.out.println(new LogEntry("can not find mapping '" + mappingName + "': turning to default mapping '" + defaultMappingName + "'"));
 			mapping = mappingMap.get(defaultMappingName);
 			if (mapping == null || !mapping.isLoaded()) {
 				throw new ConfigurationException("can not find mapping '" + mappingName + "',\n" +
 						"default mapping specification '" + defaultMappingName + "' " + (mapping == null ? " absent" : " not loaded") +
 						";\nadd a default mapping to avoid uncontrolled responses");
 			}
 		}
 		if (!mapping.isLoaded()) {
 			throw new ConfigurationException("mapping '" + mappingName + "' not loaded");
 		}
 		Process process = mapping.getRootProcess();
 		boolean hasResponded = false;
 		try {
 			hasResponded = process.processRequest(subProcessArray, requestProperties, frh);
 		} catch (Throwable t) {
 			if (t instanceof RuntimeException) {
 				throw (RuntimeException) t;
 			}
 			throw new ConfigurationException("process not completed due to insufficient error handling", t);
 		}
 		return hasResponded;
 	}
 
 
 	/**
 	 * @return a string representation of mvc management, containing an overview of the usages
 	 *         of mvc tasks and all mapping trees
 	 */
 	public String getExtendedReport() {
 		StringBuffer result = new StringBuffer("");
 
 		Iterator i = mappingMap.keySet().iterator();
 
 		while (i.hasNext()) {
 			String key = (String) i.next();
 			result.append(key + "\n");
 			result.append("{\n");
 
 			Mapping map = mappingMap.get(key);
 			result.append(map.toString());
 
 			result.append("}\n\n");
 
 		}
 		return result.toString();
 	}
 
 	/**
 	 * @param dispatcher
 	 * @return
 	 */
 	public boolean checkSanity(RequestDispatcher dispatcher) {
 		loadMessages.clear();
 		boolean ok = true;
 		Iterator i = mappingMap.values().iterator();
 		while (i.hasNext()) {
 			Mapping mapping = (Mapping) i.next();
 			//ok = mapping.checkSanity(dispatcher) && ok;
 			loadMessages.add("");
 			loadMessages.add("Messages for mapping '" + mapping.getName() + '\'');
 			loadMessages.addAll(mapping.getLoadMessages());
 			loadMessages.addAll(mapping.getSanityCheckMessages());
 		}
 		return ok;
 	}
 
 }
