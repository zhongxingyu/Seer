 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.webserver;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.jar.JarInputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.glassfish.api.admin.ParameterNames;
 import org.glassfish.embed.App;
 import org.jdesktop.wonderland.modules.Module;
 import org.jdesktop.wonderland.modules.ModulePart;
 import org.jdesktop.wonderland.modules.spi.ModuleDeployerSPI;
 import org.jdesktop.wonderland.utils.RunUtil;
 
 /**
  * Deploy web modules to the app server
  * @author jkaplan
  */
 public class WebDeployer implements ModuleDeployerSPI {
     private static final Logger logger =
             Logger.getLogger(WebDeployer.class.getName());
     
     /** list of deployed wars to avoid duplicate deploys */
     private static final List<DeployRecord> deployed =
             new ArrayList<DeployRecord>();
     
     /**
      * Get the name of this deployer
      * @return the deployer
      */
     public String getName() {
         return "Web";
     }
 
     /**
      * This deployer supports the web part of modules
      * @return the types
      */
     public String[] getTypes() {
         return new String[] { "web" };
     }
 
     /**
      * Web modules are always deployable if the server is running
      */
     public boolean isDeployable(String type, Module module, ModulePart part) {
         return RunAppServer.getAppServer().isStarted();
     }
 
     /**
      * Web modules are always undeployable
      */
     public boolean isUndeployable(String arg0, Module arg1, ModulePart arg2) {
         return true;
     }
     
     /**
      * Deploy a web app
      * @param type the type of app
      * @param module the module to deploy wars from
      * @param part the web module part
      */
     public void deploy(String type, Module module, ModulePart part) {
         File deployDir = RunUtil.createTempDir("webmodule", "deploy");
         
         for (File war : getWebApps(part.getFile())) {
             
             DeployRecord record = new DeployRecord(module.getName(), 
                                                    war.getName());
             
             
             // make sure the war isn't already deployed
             boolean undeploy = false;
             synchronized (deployed) {
                 if (deployed.contains(record)) {
                     logger.warning("Duplicate deploy " + record + ", undeploying");
                     undeploy = true;
                 }
             }
             if (undeploy) {
                 undeploy(module, war);
             }
             
             logger.warning("Web deploy " + record);
             File extractDir = new File(deployDir, war.getName());
             
             // create a context root for this app.  The context root is
             // <module-name>/<war-name>, where <war-name> is the name of
             // the .war file with ".war" taken off.
             String contextRoot = module.getName() + "/" + war.getName();
             if (contextRoot.endsWith(".war")) {
                 contextRoot = contextRoot.substring(0, 
                                         contextRoot.length() - ".war".length());
             }
             Properties props = new Properties();
             props.put(ParameterNames.CONTEXT_ROOT, contextRoot);
                    
             try {
                JarInputStream jin = new JarInputStream(new FileInputStream(war));
                 File f = RunUtil.extractZip(jin, extractDir);
                 App app = RunAppServer.getAppServer().deploy(f, props);
                 
                 // record that the app is installed
                 synchronized (deployed) {
                     record.setApp(app);
                     deployed.add(record);
                 }
             } catch (IOException ioe) {
                 logger.log(Level.WARNING, "Unable to deploy " + war, ioe);
             }
         }
     }
 
     /**
      * Undeploy a web app
      * @param type the type of app
      * @param module the module to undeploy wars from
      * @param part the web module part
      */
     public void undeploy(String type, Module module, ModulePart part) {
         for (File war : getWebApps(part.getFile())) {
             undeploy(module, war);
         }
     }
     
     /**
      * Undeploy a war file from the given module
      * @param module the module undeploy from
      * @param war the war to undeploy
      */
     protected void undeploy(Module module, File war) {
         DeployRecord record = new DeployRecord(module.getName(), war.getName());
 
         // remove the .war
         synchronized (deployed) {
             int deployIdx = deployed.indexOf(record);
             if (deployIdx != -1) {
                 record = deployed.get(deployIdx);
                 deployed.remove(deployIdx);
 
                 logger.warning("Web undeploy " + record);
             } else {
                 logger.warning("Not found on undeploy " + record);
             }
         }
 
         // undeploy the app
         if (record.getApp() != null) {
             record.getApp().undeploy();
         }
     }
     
     /**
      * Get the list of .wars in a directory
      * @param dir the directory to search
      * @return a list of .war files in the directory, or an empty list
      * if there are no .wars in the directory
      */
     protected File[] getWebApps(File dir) {
         File[] wars = dir.listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 return !pathname.isDirectory() &&
                         pathname.getName().endsWith(".war");
             }         
         });
         
         if (wars == null) {
             return new File[0];
         }
         
         return wars;
     }
     
     class DeployRecord {
         private String moduleName;
         private String warName;
         // the deployed app
         private App app;
         
         public DeployRecord(String moduleName, String warName) {
             this.moduleName = moduleName;
             this.warName = warName;
         }
 
         public void setApp(App app) {
             this.app = app;
         }
         
         public App getApp() {
             return app;
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final DeployRecord other = (DeployRecord) obj;
             if (this.moduleName != other.moduleName && (this.moduleName == null || !this.moduleName.equals(other.moduleName))) {
                 return false;
             }
             if (this.warName != other.warName && (this.warName == null || !this.warName.equals(other.warName))) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 53 * hash + (this.moduleName != null ? this.moduleName.hashCode() : 0);
             hash = 53 * hash + (this.warName != null ? this.warName.hashCode() : 0);
             return hash;
         }
         
         @Override
         public String toString() {
             return "[ module: " + moduleName + ", war: " + warName +
                    "  app: " + ((app == null)?"no":"yes") + " ]";
         }
     }
 }
