 /********************************************************************************
  * CruiseControl, a Continuous Integration Toolkit
  * Copyright (c) 2001-2003, ThoughtWorks, Inc.
  * 651 W Washington Ave. Suite 600
  * Chicago, IL 60661 USA
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     + Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *
  *     + Redistributions in binary form must reproduce the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer in the documentation and/or other materials provided
  *       with the distribution.
  *
  *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
  *       names of its contributors may be used to endorse or promote
  *       products derived from this software without specific prior
  *       written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ********************************************************************************/
 package net.sourceforge.cruisecontrol;
 
 import net.sourceforge.cruisecontrol.util.Util;
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author <a href="mailto:robertdw@users.sourceforge.net">Robert Watkins</a>
  */
 public class CruiseControlController {
     private static final Logger LOG = Logger.getLogger(CruiseControlController.class);
     public static final String DEFAULT_CONFIG_FILE_NAME = "config.xml";
     private File configFile;
     private List projects = new ArrayList();
     private BuildQueue buildQueue = new BuildQueue();
 
     private List listeners = new ArrayList();
 
     public File getConfigFile() {
         return configFile;
     }
 
     public void setConfigFile(File configFile) throws CruiseControlException {
         this.configFile = configFile;
         if (configFile == null) {
             throw new CruiseControlException("No config file");
         }
         if (!configFile.exists()) {
             throw new CruiseControlException("Config file not found: " + configFile.getName());
         }
         parseConfigFile();
     }
 
     private void parseConfigFile() throws CruiseControlException {
         Element configRoot = Util.loadConfigFile(configFile);
         addPluginsToRootRegistry(configRoot);
         List projectList = getAllProjects(configRoot);
         for (Iterator iterator = projectList.iterator(); iterator.hasNext();) {
             Project project = (Project) iterator.next();
             addProject(project);
         }
     }
 
     private void addPluginsToRootRegistry(Element configRoot) {
         for (Iterator pluginIter = configRoot.getChildren("plugin").iterator(); pluginIter.hasNext(); ) {
             Element pluginElement = (Element) pluginIter.next();
             String pluginName = pluginElement.getAttributeValue("name");
             if (pluginName == null) {
                 LOG.warn(configFile.getName() + " contains plugin without a name-attribute, ignoring it");
                 continue;
             }
             try {
                 PluginRegistry.registerToRoot(pluginElement);
             } catch (CruiseControlException e) {
                 LOG.warn("Can't register plugin '" + pluginName + "':\n" + e.getMessage());
             }
         }
     }
 
     private void addProject(Project project) {
         projects.add(project);
         for (Iterator listenIter = listeners.iterator(); listenIter.hasNext();) {
             LOG.debug("Informing listener of new project");
             Listener listener = (Listener) listenIter.next();
             listener.projectAdded(project);
         }
     }
 
     public void resume() {
         buildQueue.start();
         for (Iterator iterator = projects.iterator(); iterator.hasNext();) {
             Project currentProject = (Project) iterator.next();
             currentProject.setBuildQueue(buildQueue);
             currentProject.start();
         }
     }
 
     public void pause() {
         buildQueue.stop();
         for (Iterator iterator = projects.iterator(); iterator.hasNext();) {
             Project currentProject = (Project) iterator.next();
             currentProject.stop();
         }
     }
 
     public void halt() {
         pause();
         System.exit(0);
     }
 
     public String getBuildQueueStatus() {
         if (buildQueue.isAlive()) {
             if (buildQueue.isWaiting()) {
                 return "waiting";
             } else {
                 return "alive";
             }
         } else {
             return "dead";
         }
     }
 
     public List getProjects() {
         return Collections.unmodifiableList(projects);
     }
 
     private List getAllProjects(Element configRoot) throws CruiseControlException {
         String[] projectNames = getProjectNames(configRoot);
         ArrayList allProjects = new ArrayList(projectNames.length);
         for (int i = 0; i < projectNames.length; i++) {
             String projectName = projectNames[i];
             LOG.info("projectName = [" + projectName + "]");
             Project project = configureProject(projectName);
             allProjects.add(project);
         }
         return allProjects;
     }
 
     protected Project configureProject(String projectName) throws CruiseControlException {
         Project project = readProject(projectName);
         project.setName(projectName);
         project.setConfigFile(configFile);
         project.init();
         return project;
     }
 
     /**
      * Reads project configuration from a previously serialized Project.  The
      * name of the serialized project file is equivalent to the name of the
      * project.
      *
      * @param fileName name of the serialized project file
      * @return Deserialized Project or a new Project if there are any problems
      * reading the serialized Project; should never return null
      */
     Project readProject(String fileName) {
         //look for fileName.ser first
         File serializedProjectFile = new File(fileName + ".ser");
         LOG.debug("Reading serialized project from: " + serializedProjectFile.getAbsolutePath());
 
         if (!serializedProjectFile.exists() || !serializedProjectFile.canRead()) {
             //filename.ser doesn't exist, try finding fileName
             serializedProjectFile = new File(fileName);
             LOG.debug(fileName + ".ser not found, looking for serialized project file: " + fileName);
             if (!serializedProjectFile.exists()
                     || !serializedProjectFile.canRead()
                     || serializedProjectFile.isDirectory()) {
                LOG.warn("No previously serialized project found [" + serializedProjectFile.getAbsolutePath() + "], forcing a build.");
                 Project newProject = new Project();
                 newProject.setBuildForced(true);
                 return newProject;
             }
         }
 
         try {
             ObjectInputStream s = new ObjectInputStream(new FileInputStream(serializedProjectFile));
             Project project = (Project) s.readObject();
             return project;
         } catch (Exception e) {
             LOG.warn("Error deserializing project file from " + serializedProjectFile.getAbsolutePath(), e);
             return new Project();
         }
     }
 
     private String[] getProjectNames(Element rootElement) {
         ArrayList projectNames = new ArrayList();
         Iterator projectIterator = rootElement.getChildren("project").iterator();
         while (projectIterator.hasNext()) {
             Element projectElement = (Element) projectIterator.next();
             String projectName = projectElement.getAttributeValue("name");
             if (projectName == null) {
                 // TODO: will be ignored?
                 LOG.warn("configuration file contains project element with no name");
             } else {
                 projectNames.add(projectName);
             }
         }
         return (String[]) projectNames.toArray(new String[]{});
     }
 
     public void addListener(Listener listener) {
         LOG.debug("Listener added");
         listeners.add(listener);
     }
 
     public static interface Listener extends EventListener {
         void projectAdded(Project project);
     }
 }
