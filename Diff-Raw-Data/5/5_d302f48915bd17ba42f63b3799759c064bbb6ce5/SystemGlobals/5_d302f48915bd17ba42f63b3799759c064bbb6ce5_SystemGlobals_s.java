 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creating date: Feb 24, 2003 / 8:25:35 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.util.preferences;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 /**
  * <p>Store global configurations used in the system.
  * This is an helper class used to access the values
  * defined at SystemGlobals.properties and related
  * config files.</p> 
  * 
  * <p>
  * Transient values are stored in a special place, and are not
  * modified when you change a regular key's value. 
  * </p>
  * 
  * @author Rafael Steil
  * @author Pieter
 * @version $Id: SystemGlobals.java,v 1.10 2004/09/25 04:38:04 rafaelsteil Exp $
  */
 public class SystemGlobals implements VariableStore
 {
 	private static SystemGlobals globals;
 
 	private String defaultConfig;
 	private String installationConfig;
 
 	private Properties defaults;
 	private Properties installation;
 	private static List additionalDefaultsList = new ArrayList();
 	private static Properties queries = new Properties();
 	private static Properties transientValues = new Properties();
 
 	private VariableExpander expander;
 	
 	private static final Logger logger = Logger.getLogger(SystemGlobals.class);
 
 	/**
 	 * Initialize the global configuration
 	 * 
 	 * @param appPath The application path (normally the path to the webapp base dir
 	 * @param defaults The file containing system defaults (when null, defaults to <appPath>/WEB-INF/config/default.conf)
 	 * @param installation The specific installation realm (when null, defaults to System.getProperty("user"))
 	 */
 	static public void initGlobals(String appPath, String defaults, String installKey) throws IOException
 	{
 		globals = new SystemGlobals(appPath, defaults, installKey);
 	}
 
 	private SystemGlobals(String appPath, String defaultConfig, String installKey) throws IOException
 	{
 		if (defaultConfig == null) {
 			throw new InvalidParameterException("defaultConfig could not be null");
 		}
 		
 		expander = new VariableExpander(this, "${", "}");
 		
 		if (installKey == null) {
 			installKey = System.getProperty("user.name");
 		}
 		
 		this.defaultConfig = defaultConfig;
 		defaults = new Properties();
 
 		defaults.put(ConfigKeys.APPLICATION_PATH, appPath);
 		defaults.put(ConfigKeys.INSTALLATION, installKey);
 		defaults.put(ConfigKeys.DEFAULT_CONFIG, defaultConfig);
 		loadDefaultsImpl();
 
 		installation = new Properties(defaults);
 		
 		for (Iterator iter = additionalDefaultsList.iterator(); iter.hasNext(); ) {
 			this.loadAdditionalDefaultsImpl((String)iter.next());
 		}
 	}
 	
 	/**
 	 * Sets a value for some property
 	 * 
 	 * @param field The property name
 	 * @param value The property value 
 	 * @see #getVariableValue(String)
 	 * */
 	public static void setValue(String field, String value)
 	{
 		globals.setValueImpl(field, value);
 	}
 
 	private void setValueImpl(String field, Object value)
 	{
 		installation.put(field, value);
 		expander.clearCache();
 	}
 
 	/**
 	 * Set a transient configuration value (a value that will not be saved) 
 	 * @param field The name of the configuration option
 	 * @param value The value of the configuration option
 	 */
 	public static void setTransientValue(String field, String value)
 	{
 		transientValues.put(field, value);
 	}
 
 	/**
 	 * Load system defaults
 	 * 
 	 * @throws IOException
 	 */
 	public static void loadDefaults() throws IOException
 	{
 		globals.loadDefaultsImpl();
 	}
 	
 	/**
 	 * Merge additional configuration defaults
 	 * 
 	 * @param file File from which to load the additional defaults
 	 * @throws IOException
 	 */
 	public static void loadAdditionalDefaults(String file) throws IOException
 	{
 		globals.loadAdditionalDefaultsImpl(file);
 	}
 	
 	private void loadAdditionalDefaultsImpl(String file) throws IOException
 	{
 		FileInputStream input = new FileInputStream(file);
 		installation.load(input);
 		input.close();
 		
 		if (!additionalDefaultsList.contains(file)) {
 			additionalDefaultsList.add(file);
 		}
 	}
 
 	private void loadDefaultsImpl() throws IOException
 	{
 		FileInputStream input = new FileInputStream(defaultConfig);
 		defaults.load(input);
 		input.close();
 		expander.clearCache();
 	}
 
 	/**
 	 * Save installation defaults
 	 * 
 	 * @throws IOException when the file could not be written
 	 */
 	public static void saveInstallation() throws IOException
 	{
 		globals.saveInstallationImpl();
 	}
 
 	private void saveInstallationImpl() throws IOException
 	{
 		FileOutputStream out = new FileOutputStream(installationConfig);
 		installation.store(out, "Installation specific configuration options\n"
 							+ "# Please restart the application after editing this file.");
 		out.close();
 	}
 
 	/**
 	 * Gets the value of some property
 	 * 
 	 * @param field The property name to retrieve the value
 	 * @return String with the value, or <code>null</code> if not found
 	 * @see #setValue(String, String)
 	 * */
 	public static String getValue(String field)
 	{
 		return globals.getVariableValue(field);
 	}
 	
 	public static String getTransientValue(String field)
 	{
 		return transientValues.getProperty(field);
 	}
 
 	/**
 	 * Retrieve an integer-valued configuration field
 	 * 
 	 * @param field Name of the configuration option
 	 * @return The value of the configuration option
 	 * @exception NullPointerException when the field does not exists
 	 */
 	public static int getIntValue(String field)
 	{
 		return Integer.parseInt(getValue(field));
 	}
 
 	/**
 	 * Retrieve an boolean-values configuration field
 	 * 
 	 * @param field name of the configuration option
 	 * @return The value of the configuration option
 	 * @exception NullPointerException when the field does not exists
 	 */
 	public static boolean getBoolValue(String field)
 	{
 		return getValue(field).equals("true");
 	}
 
 	/**
 	 * Return the value of a configuration value as a variable. Variable expansion is performe
 	 * on the result.
 	 * 
 	 * @param field The field name to retrieve
 	 * @return The value of the field if present  
 	 */
 
 	public String getVariableValue(String field)
 	{
 		String preExpansion = installation.getProperty(field);
 		if (preExpansion == null) {
 			throw new RuntimeException("unknown property: " + field);
 		}
 
 		return expander.expandVariables(preExpansion);
 	}
 
 	/**
 	 * Sets the application's root directory 
 	 * 
 	 * @param ap String containing the complete path to the root dir
 	 * @see #getApplicationPath
 	 * */
 	public static void setApplicationPath(String ap)
 	{
 		setValue(ConfigKeys.APPLICATION_PATH, ap);
 	}
 
 	/**
 	 * Getst the complete path to the application's root dir
 	 * 
 	 * @return String with the path
 	 * @see #setApplicationPath
 	 * */
 	public static String getApplicationPath()
 	{
 		return getValue(ConfigKeys.APPLICATION_PATH);
 	}
 
 	/**
 	 * Gets the path to the resource's directory.
 	 * This method returns the directory name where the config
 	 * files are stored. 
 	 *  Caso queira saber o caminho absoluto do diretorio, voce precisa
 	 * usar
 	 * Note that this method does not return the complete path. If you 
 	 * want the full path, you must use 
 	 * <blockquote><pre>SystemGlobals.getApplicationPath() + SystemGlobals.getApplicationResourcedir()</pre></blockquote>
 	 * 
 	 * @return String with the name of the resource dir, relative 
 	 * to application's root dir.
 	 * @see #setApplicationResourceDir
 	 * @see #getApplicationPath
 	 * */
 	public static String getApplicationResourceDir()
 	{
 		return getValue(ConfigKeys.RESOURCE_DIR);
 	}
 
 	/**
 	 * Load the SQL queries
 	 *
 	 * @param queryFile Complete path to the SQL queries file.
 	 * @throws java.io.IOException
 	 **/
 	public static void loadQueries(String queryFile) throws IOException
 	{
 		globals.loadQueriesImpl(queryFile);
 	}
 
 	private void loadQueriesImpl(String queryFile) throws IOException
 	{
 		queries.load(new FileInputStream(queryFile));
 	}
 
 	/**
 	 * Gets some SQL statement.
 	 * 
 	 * @param sql The query's name, as defined in the file loaded by
 	 * {@link #loadQueries(String)}
 	 * @return The SQL statement, or <code>null</code> if not found.
 	 * */
 	public static String getSql(String sql)
 	{
 		return globals.getSqlImpl(sql);
 	}
 
 	private String getSqlImpl(String sql)
 	{
 		return queries.getProperty(sql);
 	}
 
 	/**
 	 * Retrieve an iterator that iterates over all known configuration keys
 	 * 
 	 * @return An iterator that iterates over all known configuration keys
 	 */
 	public static Iterator fetchConfigKeyIterator()
 	{
 		return globals.fetchConfigKeyIteratorImpl();
 	}
 
 	private Iterator fetchConfigKeyIteratorImpl()
 	{
 		return defaults.keySet().iterator();
 	}
 }
