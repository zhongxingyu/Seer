 /* Copyright 2009-2013 Tracy Flynn
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.verymuchme.appconfig;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.StringReader;
 import java.util.HashMap;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.DefaultConfigurationBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * <p>Application configuration the easy way - a thin wrapper for Apache Commons Configuration</p>
  *
  * <p>Typical usage</p>
  * <pre>AppConfig.sConfigure();</pre>
  * 
  * <p>Automatically names, finds and loads environment-specific and default application, logging (log4j) and database configuration files. If an external directory is specified, that directory is searched for environment-specific files only.
  * Then the user directory, current and system classpaths are searched in that order. Missing default configuration files for application and logging configurations will cause an exception if not present.  The default database configuration file is by default disabled. This behavior can be changed with appropriate option settings.</p>
  * 
 * <p>The order of loading of the files follows the Apache Commons Configuration convention. The environment-specific files are loaded first, followed by the defaults. That ensures that the environment-specific settings override the defaults.</p>
  * <p> THe file name convention is [application|database|log4j]-[development|test|production|defaults].properties.</p>
  * 
 * <p>The following internal settings are taken starting with internal options, command-line and then the environment value.</p>
  * 
  * <ul>
  * <li>'com.verymuchme.appconfig.consoleLog' - Enable/disable bootstrap console logging</li>
  * <li>'com.verymuchme.appconfig.systemPropertiesOverride' - enable/disable the ability for system properties to override others</li>
  * <li>'com.verymuchme.appconfig.runTimeEnvironment' - If specified, one of 'development','production','test'. Defaults to 'development'</li>
  * <li>'com.verymuchme.appconfig.systemPropertiesOverride' - enable/disable the ability for system properties to override others</li>
  * <li>'com.verymuchme.appconfig.runTimeEnvironment' - If specified, one of 'development','production','test'. Defaults to 'development'</li>
  * <li>'com.verymuchme.appconfig.externalConfigurationDirectory' - If specified, defines the external directory location for configuration files specific to particular run-time environments</li>
  * <li>'com.verymuchme.appconfig.applicationConfigurationPrefix' - If specified, defines the prefix for application configuration property files. Defaults to 'application'</li>
  * <li>'com.verymuchme.appconfig.databaseConfigurationPrefix' - If specified, defines the prefix for database configuration property files. Defaults to 'database'</li>
  * <li>'com.verymuchme.appconfig.log4jConfigurationPrefix' - If specified, defines the prefix for log4j configuration property files. Defaults to 'log4j'</li>
  * <li>'com.verymuchme.appconfig.configurationNameSuffix' - If specified, defines the suffix for configuration property files. Defaults to 'properties'</li>
  * <li>'com.verymuchme.appconfig.defaultConfigurationName' - If specified, defines the prefix for default configuration property files. Defaults to 'defaults'</li>
  * <li>'com.verymuchme.appconfig.database.defaultConfigurationEnabled' - If set to true, a default database configuration file ('database-defaults.properties') must be present. Defaults to 'false'</li>
  * <li>'com.verymuchme.appconfig.log4j.defaultConfigurationEnabled' - If set to true, a default log4j configuration file ('log4j-defaults.properties') must be present. Defaults to 'true'</li>
  * </ul> 
  * 
  * 
  * @author Tracy Flynn
  * @version 1.0-SNAPSHOT
  * @since 1.0-SNAPSHOT
  */
 public class AppConfig {
   
   //TODO logging for AppConfig initialization separate from APP being configured
 
   /*
    * Singleton AppConfig instance for invocation via singleton shortcuts
    */
   private static final AppConfig singletonInstance = new AppConfig(null);
 
   /*
    * Bootstrap logger instance 
    */
   private BootstrapLogger bootstrapLogger = null;
   
   /*
    * Configuration options
    */
   private HashMap<String,String> configurationOptions = null;
   
   
   /**
    * Create a new AppConfig instance
    */
   public AppConfig(HashMap<String,String> configOptions) {
     this.configurationOptions = configOptions;
   }
   
   /**
    * Configure AppConfig
    * @throws ConfigurationException 
    */
   public void configure() throws Exception {
     this.bootstrapLogger = new BootstrapLogger();
 
     // Get the configuration definition
     String configurationDefinition = this.buildConfigurationDefinition();
      
     File tempFile = null;
     
     try {
       // Write it to a temporary file
       File temp = File.createTempFile("tempfile", ".xml");
       BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
       bw.write(configurationDefinition);
       bw.close();
   
       // Create a handle to the temporary file
       tempFile = temp.getAbsoluteFile();
       String tempFileName = tempFile.getAbsolutePath();
       this.bootstrapLogger.debug(String.format("AppConfig.configure temporary file name %s", tempFileName));
       
       // Load configuration definition in as a file
       DefaultConfigurationBuilder defaultConfigurationBuilder = new DefaultConfigurationBuilder();
       defaultConfigurationBuilder.load(tempFile);
       this.bootstrapLogger.debug(String.format("AppConfig.configure configuration definition loaded"));
       defaultConfigurationBuilder.getConfiguration();
       this.bootstrapLogger.debug(String.format("AppConfig.configure configuration generated successfully"));
     }
     catch (Exception e) {
       throw e;
     }
     finally {
       try {
         // Delete the temporary file
         tempFile.delete();
       }
       catch (Exception ee) {
         // Ignore
       }
     }
   }
   
   /**
    * Build the Apache Commons Configuration-compliant definition
    * 
    * @return XML string with configuration definition
    */
   public String buildConfigurationDefinition() {
     ConfigurationDefinitionBuilder configurationBuilder = new ConfigurationDefinitionBuilder(this.configurationOptions);
     configurationBuilder.setBootstrapLogger(this.bootstrapLogger);
     String configurationDefinition = configurationBuilder.build();
     return configurationDefinition;
   }
   
   
   /**
    * Configure the singleton AppConfig instance
    * @throws ConfigurationException 
    */
   public static void sConfigure() throws Exception {
     AppConfig.singletonInstance.configure();
   }
 
   /**
    * Utility function to get system or environment variable
    *
    * @return Named system variable, then environment variable then null
    */
   public static String sSystemOrEnvironmentValue(String variableName) {
     String variableValue = System.getProperty(variableName);
     if (variableValue == null) {
       variableValue = System.getenv(variableName);
     }
     return variableValue;
   }
   
   
   /**
    * Set configuration options
    * 
    * @param configOpts
    */
   public void setOptions(HashMap<String,String> configOpts) {
     this.configurationOptions = configOpts;
   }
   
   /**
    * Get configuration options
    * 
    * @return Configuration options
    */
    public HashMap<String,String> getOptions() {
      return this.configurationOptions;
    }
    
    public static void main(String[] args) throws Exception {
      AppConfig.sConfigure();
    }
 
 }
