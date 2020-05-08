 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.container;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.framework.AdminCommandsService;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * @version $Revision: 1.1 $
  */
 public abstract class DeploySupport implements InitializingBean {
     private static final transient Log LOG = LogFactory.getLog(DeploySupport.class);
 
     private JBIContainer jbiContainer;
     private AdminCommandsService commandsService;
     private boolean deferException;
     private String homeDir;
     private String repositoryDir;
     private String groupId;
     private String artifactId;
     private String version;
     private String type = "-installer.zip";
     private String file;
 
     public void afterPropertiesSet() throws Exception {
     }
 
     public void deploy(JBIContainer container) throws Exception {
         setJbiContainer(container);
         if (container == null) {
             throw new IllegalArgumentException("No JBI container configured!");
         }
         if (getCommandsService() == null) {
             setCommandsService(getJbiContainer().getAdminCommandsService());
         }
         doDeploy();
     }
 
 
     // Properties
     //-------------------------------------------------------------------------
 
     public JBIContainer getJbiContainer() {
         return jbiContainer;
     }
 
     public void setJbiContainer(JBIContainer jbiContainer) {
         this.jbiContainer = jbiContainer;
     }
 
     public AdminCommandsService getCommandsService() {
         return commandsService;
     }
 
     public void setCommandsService(AdminCommandsService commandsService) {
         this.commandsService = commandsService;
     }
 
     public boolean isDeferException() {
         return deferException;
     }
 
     public void setDeferException(boolean deferException) {
         this.deferException = deferException;
     }
 
     public String getArtifactId() {
         if (artifactId == null) {
             throw new IllegalArgumentException("You must specify either a file or a groupId and an artifactId property");
         }
         return artifactId;
     }
 
     public void setArtifactId(String artifactId) {
         this.artifactId = artifactId;
     }
 
     public String getGroupId() {
         if (groupId == null) {
             throw new IllegalArgumentException("You must specify either a file or a groupId and an artifactId property");
         }
         return groupId;
     }
 
     public void setGroupId(String groupId) {
         this.groupId = groupId;
     }
 
     public String getHomeDir() {
         if (homeDir == null) {
             homeDir = System.getProperty("user.home", "~");
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows")) {
                homeDir = homeDir.replace('\\', '/');
                homeDir = homeDir.replaceAll(" ", "%20");
            }
         }
        
         return homeDir;
     }
 
     public void setHomeDir(String homeDir) {
         this.homeDir = homeDir;
     }
 
     public String getRepositoryDir() {
         if (repositoryDir == null) {
             if (System.getProperty("localRepository") != null) {
                 repositoryDir = System.getProperty("localRepository");
             } else {
                 repositoryDir = getHomeDir() + "/.m2/repository";
             }
         }
         return repositoryDir;
     }
 
     public void setRepositoryDir(String repositoryDir) {
         this.repositoryDir = repositoryDir;
     }
 
     public String getVersion() {
         if (version == null) {
             version = createVersion();
         }
         return version;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public String getFile() {
         if (file == null) {
             file = createFile();
         }
         return file;
     }
 
     public void setFile(String file) {
         this.file = file;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     // Implementation methods
     //-------------------------------------------------------------------------
     protected abstract void doDeploy() throws Exception;
 
     protected String createFile() {
         String group = getGroupId();
         String artifact = getArtifactId();
         String v = getVersion();
         if (v == null) {
             throw new IllegalArgumentException(
                     "You must specify a version property as it could not be deduced for "
                             + getGroupId() + ":" + getArtifactId());
         }
         group = group.replace('.', '/');
         return getFilePrefix() + getRepositoryDir() + "/" + group + "/" + artifact + "/" + v + "/" + artifact + "-" + v + type;
     }
 
 
     protected String createVersion() {
         String group = getGroupId();
         String artifact = getArtifactId();
         String key = group + "/" + artifact + "/version";
 
         // now lets load all of the maven dependencies and look for this version
         try {
             Enumeration iter = Thread.currentThread().getContextClassLoader().getResources("META-INF/maven/dependencies.properties");
             while (iter.hasMoreElements()) {
                 URL url = (URL) iter.nextElement();
 
                 LOG.debug("looking into properties file: " + url + " with key: " + key);
                 Properties properties = new Properties();
                 InputStream in = url.openStream();
                 properties.load(in);
                 in.close();
                 String answer = properties.getProperty(key);
                 if (answer != null) {
                     answer = answer.trim();
                     LOG.debug("Found version: " + answer);
                     return answer;
                 }
             }
         } catch (IOException e) {
             LOG.error("Failed: " + e, e);
         }
         return null;
     }
 
     protected String getFilePrefix() {
         String filePrefix = "file://";
         String os = System.getProperty("os.name");
         if (os.startsWith("Windows")) {
             filePrefix = "file:///";
         }
 
         return isFileUrlFormat() ? filePrefix : "";
     }
 
     protected boolean isFileUrlFormat() {
         return true;
     }
 }
