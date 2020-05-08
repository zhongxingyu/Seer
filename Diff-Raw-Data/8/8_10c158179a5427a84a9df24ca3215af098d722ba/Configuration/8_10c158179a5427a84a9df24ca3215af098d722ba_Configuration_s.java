 /**
  *  Copyright 2011 Carsten Gräf
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * 
  */
 
 package de.twenty11.skysail.server.ext.dbviewer.internal;
 
 import java.io.IOException;
 
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.component.ComponentContext;
 import org.restlet.Server;
 import org.restlet.data.Protocol;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.twenty11.skysail.server.Constants;
 
 public class Configuration {
 
     private static Logger logger = LoggerFactory.getLogger(Configuration.class);
     private static ConfigurationAdmin configadmin;
     private DbViewerComponent dbViewerComponent;
     private Server server;
 
     protected void activate(ComponentContext ctxt) {
         
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        StatusPrinter.print(lc);

        logger.info("Activating DbViewer Config");
         if (startStandaloneServer()) {
             String port = "8554";// configService.getString(Constants.STANDALONE_PORT, "8554");
            logger.info("starting standalone dbviewer server on port {}", port);
             dbViewerComponent = new DbViewerComponent();
             startStandaloneServer(port);
         }
 
     }
 
     protected void deactivate(ComponentContext ctxt) {
         try {
             server.stop();
         } catch (Exception e) {
             logger.error("Exception when trying to stop standalone server", e);
         }
     }
 
     private boolean startStandaloneServer() {
         // String standalone = configService.getString(Constants.STANDALONE, "false");
         // if (!"true".equals(standalone)) {
         // logger.info("not starting standalone server, as {} is set to false or not configured", Constants.STANDALONE);
         // return false;
         // }
         return true;
 
     }
 
     private void startStandaloneServer(String portAsString) {
         try {
             server = new Server(Protocol.HTTP, Integer.valueOf(portAsString), dbViewerComponent);
             server.start();
         } catch (Exception e) {
             logger.error("Exception when starting standalone server", e);
         }
     }
 
     public synchronized void setConfigAdmin(ConfigurationAdmin configadmin) {
         Configuration.configadmin = configadmin;
     }
 
     public static String getDriverClassName() {
         return getStringFromConfigAdmin(Constants.SKYSAIL_DB_DRIVERCLASSNAME);
     }
 
     public static String getUsername() {
         return getStringFromConfigAdmin(Constants.SKYSAIL_DB_USERNAME);
     }
 
     public static String getPasswort() {
         return getStringFromConfigAdmin(Constants.SKYSAIL_DB_PASSWORD);
     }
 
     public static String getUrl() {
         return getStringFromConfigAdmin(Constants.SKYSAIL_DB_URL);
     }
 
     private static String getStringFromConfigAdmin(String configElementName) {
         if (!isConfigAdminAvailable(configElementName))
             return "";
         try {
             return getConfigFromAdmin(configElementName);
         } catch (IOException e) {
             logger.error("could not retrieve key '{}' from configuration", e, configElementName);
             return null;
         }
     }
 
     private static String getConfigFromAdmin(String configElementName) throws IOException {
         org.osgi.service.cm.Configuration config = configadmin
                 .getConfiguration("de.twenty11.skysail.server.config.Configuration");
         Object configObject = config.getProperties().get(configElementName);
         if (configObject == null) {
             logger.warn("could not retrieve configuration for key '{}'", configElementName);
             return null;
         }
         return configObject.toString();
     }
 
     private static boolean isConfigAdminAvailable(String configElementName) {
         if (configadmin == null) {
             logger.warn("could not retrieve key '{}' for skysail database as configadmin service does not exist",
                     configElementName);
             return false;
         }
         return true;
     }
 
 }
