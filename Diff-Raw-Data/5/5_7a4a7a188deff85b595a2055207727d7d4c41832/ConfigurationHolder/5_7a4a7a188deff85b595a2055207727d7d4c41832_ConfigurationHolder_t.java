 /**
  * Copyright (C) 2000 - 2013 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
  * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
  * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
  * text describing the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package org.silverpeas.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.silverpeas.util.file.FileUtil;
 
 /**
  * This class holds all of the settings and parameters for the different applications used in the
  * configuration of the Silverpeas portal.
  *
  * @author mmoquillon
  */
 public class ConfigurationHolder {
 
   private static final String SILVERPEAS_HOME_PROP_KEY = "silverpeas.home";
   private static final String SILVERPEAS_HOME_ENV_KEY = "SILVERPEAS_HOME";
   private static final String JCR_PROPERTIES = "org/silverpeas/util/jcr.properties";
   private static final String THREADS_KEY = "threads";
   private static final String JCR_HOME_KEY = "jcr.home.dir.url";
   private static String silverpeasHome = null;
   private static GestionVariables configuration = null;
   private static boolean abortOnError = true;
   private static final Console console = new Console(ConfigurationHolder.class);
 
   static {
     try {
       defineHome();
       configuration = loadConfiguration();
     } catch (IOException ex) {
       console.printWarning("Error loading configuration", ex);
     }
   }
 
   /**
    * For test purpose only.
    */
   protected static synchronized void reloadConfiguration() {
     try {
       defineHome();
       configuration = loadConfiguration();
     } catch (IOException ex) {
       console.printWarning("Error loading configuration", ex);
     }
   }
 
   /**
    * If set to TRUE, program is exited if Silverpeas install location cannot be found. Else, a
    * message is issued and execution goes on
    */
   public static void setAbortOnError(boolean on) {
     abortOnError = on;
   }
 
   /**
    * If TRUE, program is exited if Silverpeas install location cannot be found. Else, a message is
    * issued and execution goes on
    */
   public static boolean getAbortOnError() {
     return abortOnError;
   }
 
   /**
    * Finds Silverpeas install directory Silverpeas install location may be set by using
    * -Dsilverpeas.home=<i>location</i> on java command line
    *
    * @return the silverpeas home directory
    */
   public static String getHome() {
     if (silverpeasHome == null) {
       defineHome();
     }
     return silverpeasHome;
   }
 
   private static void defineHome() {
     silverpeasHome = System.getProperty(SILVERPEAS_HOME_PROP_KEY);
     if (!StringUtil.isDefined(silverpeasHome)) {
       silverpeasHome = System.getenv(SILVERPEAS_HOME_ENV_KEY);
       if (!StringUtil.isDefined(silverpeasHome)) {
         console.printError("### CANNOT FIND SILVERPEAS INSTALL LOCATION ###");
         console.printError("please use \"-D" + SILVERPEAS_HOME_PROP_KEY
             + "=install_location\" on the command line");
         console.printError("or define the SILVERPEAS_HOME environment variable.");
         if (getAbortOnError()) {
           console.printError("### ABORTED ###");
           System.exit(1);
         }
       } else {
         System.setProperty(SILVERPEAS_HOME_PROP_KEY, silverpeasHome);
       }
     }
   }
 
   public static String getDataHome() throws IOException {
     return configuration.resolveAndEvalString("${SILVERPEAS_DATA_HOME}");
   }
 
   /**
    * Gets the home directory of the JCR repository in Silverpeas.
    *
    * @return the absolute path of the JCR repository home directory.
    * @throws IOException if an errors occurs while getting the parameter.
    */
   public static String getJCRRepositoryHome() throws IOException {
     String repositoryHome = ConfigurationHolder.getDataHome() + File.separatorChar
         + "jackrabbit";
     Properties props = FileUtil.loadResource(JCR_PROPERTIES);
     repositoryHome = props.getProperty(JCR_HOME_KEY, repositoryHome);
     File repositoryHomeDir;
     if (repositoryHome.toLowerCase().startsWith("file:")) {
      repositoryHomeDir = new File(URI.create(FilenameUtils.separatorsToUnix(repositoryHome)));
     } else {
       repositoryHomeDir = new File(repositoryHome);
     }
     if (!repositoryHomeDir.isDirectory()) {
       throw new FileNotFoundException("The JCR home directory '" + repositoryHome + "' isn't found");
     }
     return repositoryHomeDir.getAbsolutePath();
   }
 
   /**
    * Gets the location of the JCR repository configuration file. Usually this configuration is done
    * in an XML file repository.xml.
    *
    * @return the absolute path of the configuration file of the JCR repository.
    * @throws java.io.IOException if an error occurs while getting the configuration file.
    */
   public static String getJCRRepositoryConfiguration() throws IOException {
     String conf = ConfigurationHolder.getHome() + File.separatorChar + "setup"
         + File.separatorChar + "jackrabbit" + File.separatorChar + "repository.xml";
     File confFile;
     if (conf.toLowerCase().startsWith("file:")) {
       confFile = new File(URI.create(conf));
     } else {
       confFile = new File(conf);
     }
     if (!confFile.exists()) {
       throw new FileNotFoundException("The JCR Configuration file '" + conf + "' isn't found");
     }
     return confFile.getAbsolutePath();
   }
 
   /**
    * Gets the maximum number of threads the executors of parallelized configuration tasks are
    * permitted to allocate with their threads pool.
    *
    * @return the number of threads to use in pools.
    */
   public static int getMaxThreadsCount() {
     return Integer.valueOf(System.getProperty(THREADS_KEY, "10"));
   }
 
   private static GestionVariables loadConfiguration() throws IOException {
     Properties defaultConfig = new Properties();
     InputStream in = ConfigurationHolder.class.getResourceAsStream("/default_config.properties");
     try {
       defaultConfig.load(in);
     } finally {
       IOUtils.closeQuietly(in);
     }
     Properties config = new Properties(defaultConfig);
     File configFile = new File(getHome() + File.separatorChar + "setup" + File.separatorChar
         + "settings", "config.properties");
     if (configFile.exists() && configFile.isFile()) {
       in = new FileInputStream(configFile);
       try {
         config.load(in);
       } finally {
         IOUtils.closeQuietly(in);
       }
     }
     return new GestionVariables(config);
   }
 
   private ConfigurationHolder() {
   }
 }
