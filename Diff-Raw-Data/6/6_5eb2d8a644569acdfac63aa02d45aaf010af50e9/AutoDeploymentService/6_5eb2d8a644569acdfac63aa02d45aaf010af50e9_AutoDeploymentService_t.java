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
 package org.apache.servicemix.jbi.framework;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.zip.ZipFile;
 
 import javax.jbi.JBIException;
 import javax.jbi.management.DeploymentException;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.container.EnvironmentContext;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.deployment.Component;
 import org.apache.servicemix.jbi.deployment.Descriptor;
 import org.apache.servicemix.jbi.deployment.DescriptorFactory;
 import org.apache.servicemix.jbi.deployment.ServiceAssembly;
 import org.apache.servicemix.jbi.event.DeploymentEvent;
 import org.apache.servicemix.jbi.event.DeploymentListener;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.BaseSystemService;
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.apache.servicemix.jbi.util.XmlPersistenceSupport;
 
 /**
  * Monitors install and deploy directories to auto install/deploy archives
  * 
  * @version $Revision$
  */
 public class AutoDeploymentService extends BaseSystemService implements AutoDeploymentServiceMBean {
 
     private static final Log LOG = LogFactory.getLog(AutoDeploymentService.class);
         
     private static String filePrefix = "file:///";
     private EnvironmentContext environmentContext;
     private DeploymentService deploymentService;
     private InstallationService installationService;
     private boolean monitorInstallationDirectory = true;
     private boolean monitorDeploymentDirectory = true;
     private int monitorInterval = 10;
     private String extensions = ".zip,.jar";
     private AtomicBoolean started = new AtomicBoolean(false);
     private Timer statsTimer;
     private TimerTask timerTask;
     private Map<File, ArchiveEntry> pendingComponents = new ConcurrentHashMap<File, ArchiveEntry>();
     private Map<File, ArchiveEntry> pendingSAs = new ConcurrentHashMap<File, ArchiveEntry>();
     private Map<String, ArchiveEntry> installFileMap;
     private Map<String, ArchiveEntry> deployFileMap;
 
     /**
      * @return the extensions
      */
     public String getExtensions() {
         return extensions;
     }
 
     /**
      * @param extensions
      *            the extensions to set
      */
     public void setExtensions(String extensions) {
         this.extensions = extensions;
     }
 
     /**
      * @return a description of this
      */
     public String getDescription() {
         return "automatically installs and deploys JBI Archives";
     }
 
     /**
      * @return Returns the monitorInstallationDirectory.
      */
     public boolean isMonitorInstallationDirectory() {
         return monitorInstallationDirectory;
     }
 
     /**
      * @param monitorInstallationDirectory
      *            The monitorInstallationDirectory to set.
      */
     public void setMonitorInstallationDirectory(boolean monitorInstallationDirectory) {
         this.monitorInstallationDirectory = monitorInstallationDirectory;
     }
 
     /**
      * @return Returns the monitorDeploymentDirectory.
      */
     public boolean isMonitorDeploymentDirectory() {
         return monitorDeploymentDirectory;
     }
 
     /**
      * @param monitorDeploymentDirectory
      *            The monitorDeploymentDirectory to set.
      */
     public void setMonitorDeploymentDirectory(boolean monitorDeploymentDirectory) {
         this.monitorDeploymentDirectory = monitorDeploymentDirectory;
     }
 
     /**
      * @return Returns the monitorInterval (number in secs)
      */
     public int getMonitorInterval() {
         return monitorInterval;
     }
 
     /**
      * @param monitorInterval
      *            The monitorInterval to set (in secs)
      */
     public void setMonitorInterval(int monitorInterval) {
         this.monitorInterval = monitorInterval;
     }
 
     public void start() throws javax.jbi.JBIException {
         super.start();
         if (started.compareAndSet(false, true)) {
             scheduleDirectoryTimer();
         }
     }
 
     /**
      * Stop the item. This suspends current messaging activities.
      * 
      * @exception javax.jbi.JBIException
      *                if the item fails to stop.
      */
     public void stop() throws javax.jbi.JBIException {
         if (started.compareAndSet(true, false)) {
             super.stop();
             if (timerTask != null) {
                 timerTask.cancel();
             }
         }
     }
 
     /**
      * Initialize the Service
      * 
      * @param container
      * @throws JBIException
      */
     public void init(JBIContainer container) throws JBIException {
         super.init(container);
         this.environmentContext = container.getEnvironmentContext();
         this.installationService = container.getInstallationService();
         this.deploymentService = container.getDeploymentService();
         // clean-up tmp directory
         if (environmentContext.getTmpDir() != null) {
             FileUtil.deleteFile(environmentContext.getTmpDir());
         }
         initializeFileMaps();
     }
 
     protected Class<AutoDeploymentServiceMBean> getServiceMBean() {
         return AutoDeploymentServiceMBean.class;
     }
 
     /**
      * load an archive from an external location
      * 
      * @param location
      * @param autoStart
      * @throws DeploymentException
      */
     public ArchiveEntry updateExternalArchive(String location, boolean autoStart) throws DeploymentException {
         ArchiveEntry entry = new ArchiveEntry();
         entry.location = location;
         entry.lastModified = new Date();
         updateArchive(location, entry, autoStart);
         return entry;
     }
 
     /**
      * Update an archive
      * 
      * @param location
      * @param autoStart
      * @throws DeploymentException
      */
     public void updateArchive(String location, ArchiveEntry entry, boolean autoStart) throws DeploymentException {
         // Call listeners
         try {
             DeploymentListener[] listeners = (DeploymentListener[]) container.getListeners(DeploymentListener.class);
             DeploymentEvent event = new DeploymentEvent(new File(location), DeploymentEvent.FILE_CHANGED);
             for (int i = 0; i < listeners.length; i++) {
                 if (listeners[i].fileChanged(event)) {
                     return;
                 }
             }
         } catch (IOException e) {
             throw failure("deploy", "Error when deploying: " + location, e);
         }
         // Standard processing
         File tmpDir = null;
         try {
             tmpDir = AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(), location);
         } catch (Exception e) {
             throw failure("deploy", "Unable to unpack archive: " + location, e);
         }
         // unpackLocation returns null if no jbi descriptor is found
         if (tmpDir == null) {
             throw failure("deploy", "Unable to find jbi descriptor: " + location);
         }
         Descriptor root = null;
         try {
             root = DescriptorFactory.buildDescriptor(tmpDir);
         } catch (Exception e) {
             throw failure("deploy", "Unable to build jbi descriptor: " + location, e);
         }
         if (root == null) {
             throw failure("deploy", "Unable to find jbi descriptor: " + location);
         }
         if (root != null) {
             try {
                 container.getBroker().suspend();
                 if (root.getComponent() != null) {
                     updateComponent(entry, autoStart, tmpDir, root);
                 } else if (root.getSharedLibrary() != null) {
                     updateSharedLibrary(entry, tmpDir, root);
                 } else if (root.getServiceAssembly() != null) {
                     updateServiceAssembly(entry, autoStart, tmpDir, root);
                 }
             } finally {
                 container.getBroker().resume();
             }
         }
     }
 
     protected void updateComponent(ArchiveEntry entry, boolean autoStart, File tmpDir, Descriptor root) throws DeploymentException {
         Component comp = root.getComponent();
         String componentName = comp.getIdentification().getName();
         entry.type = "component";
         entry.name = componentName;
         try {
             if (container.getRegistry().getComponent(componentName) != null) {
                 installationService.loadInstaller(componentName);
                 installationService.unloadInstaller(componentName, true);
             }
             // See if shared libraries are installed
             entry.dependencies = getSharedLibraryNames(comp);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Component dependencies: " + entry.dependencies);
             }
             String missings = null;
             boolean canInstall = true;
             for (String libraryName : entry.dependencies) {
                 if (container.getRegistry().getSharedLibrary(libraryName) == null) {
                     canInstall = false;
                     if (missings != null) {
                         missings += ", " + libraryName;
                     } else {
                         missings = libraryName;
                     }
                 }
             }
             if (canInstall) {
                 installationService.install(tmpDir, null, root, autoStart);
                 checkPendingSAs();
             } else {
                 entry.pending = true;
                 LOG.warn("Shared libraries " + missings + " are not installed yet: the component" + componentName
                                 + " installation is suspended and will be resumed once the listed shared libraries are installed");
                 pendingComponents.put(tmpDir, entry);
             }
         } catch (Exception e) {
             String errStr = "Failed to update Component: " + componentName;
             LOG.error(errStr, e);
             throw new DeploymentException(errStr, e);
         }
     }
 
     protected void updateSharedLibrary(ArchiveEntry entry, File tmpDir, Descriptor root) throws DeploymentException {
         String libraryName = root.getSharedLibrary().getIdentification().getName();
         entry.type = "library";
         entry.name = libraryName;
         try {
             if (container.getRegistry().getSharedLibrary(libraryName) != null) {
                 container.getRegistry().unregisterSharedLibrary(libraryName);
                 environmentContext.removeSharedLibraryDirectory(libraryName);
             }
             installationService.doInstallSharedLibrary(tmpDir, root.getSharedLibrary());
             checkPendingComponents();
         } catch (Exception e) {
             String errStr = "Failed to update SharedLibrary: " + libraryName;
             LOG.error(errStr, e);
             throw new DeploymentException(errStr, e);
         }
     }
 
     protected void updateServiceAssembly(ArchiveEntry entry, boolean autoStart, File tmpDir, Descriptor root) throws DeploymentException {
         ServiceAssembly sa = root.getServiceAssembly();
         String name = sa.getIdentification().getName();
         entry.type = "assembly";
         entry.name = name;
         try {
             if (deploymentService.isSaDeployed(name)) {
                 deploymentService.shutDown(name);
                 deploymentService.undeploy(name);
             }
             // see if components are installed
             entry.dependencies = getComponentNames(sa);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("SA dependencies: " + entry.dependencies);
             }
             String missings = null;
             boolean canDeploy = true;
             for (String componentName : entry.dependencies) {
                 if (container.getComponent(componentName) == null) {
                     canDeploy = false;
                     if (missings != null) {
                         missings += ", " + componentName;
                     } else {
                         missings = componentName;
                     }
                 }
             }
             if (canDeploy) {
                 deploymentService.deployServiceAssembly(tmpDir, sa);
                 if (autoStart) {
                     deploymentService.start(name);
                 }
             } else {
                 // TODO: check that the assembly is not already
                 // pending
                 entry.pending = true;
                 LOG.warn("Components " + missings + " are not installed yet: the service assembly " + name
                                 + " deployment is suspended and will be resumed once the listed components are installed");
                 pendingSAs.put(tmpDir, entry);
             }
         } catch (Exception e) {
             String errStr = "Failed to update Service Assembly: " + name;
             LOG.error(errStr, e);
             throw new DeploymentException(errStr, e);
         }
     }
 
     protected DeploymentException failure(String task, String info) {
         return failure(task, info, null, null);
     }
 
     protected DeploymentException failure(String task, String info, Exception e) {
         return failure(task, info, e, null);
     }
 
     protected DeploymentException failure(String task, String info, Exception e, List componentResults) {
         ManagementSupport.Message msg = new ManagementSupport.Message();
         msg.setTask(task);
         msg.setResult("FAILED");
         msg.setType("ERROR");
         msg.setException(e);
         msg.setMessage(info);
         return new DeploymentException(ManagementSupport.createFrameworkMessage(msg, componentResults));
     }
 
     protected Set<String> getComponentNames(ServiceAssembly sa) {
         Set<String> names = new HashSet<String>();
         if (sa.getServiceUnits() != null && sa.getServiceUnits().length > 0) {
             for (int i = 0; i < sa.getServiceUnits().length; i++) {
                 names.add(sa.getServiceUnits()[i].getTarget().getComponentName());
             }
         }
         return names;
     }
 
     protected Set<String> getSharedLibraryNames(Component comp) {
         Set<String> names = new HashSet<String>();
         if (comp.getSharedLibraries() != null && comp.getSharedLibraries().length > 0) {
             for (int i = 0; i < comp.getSharedLibraries().length; i++) {
                 names.add(comp.getSharedLibraries()[i].getName());
             }
         }
         return names;
     }
 
     /**
      * Remove an archive location
      * 
      * @param location
      * @throws DeploymentException
      */
     public void removeArchive(ArchiveEntry entry) throws DeploymentException {
         // Call listeners
         try {
             DeploymentListener[] listeners = (DeploymentListener[]) container.getListeners(DeploymentListener.class);
             DeploymentEvent event = new DeploymentEvent(new File(entry.location), DeploymentEvent.FILE_REMOVED);
             for (int i = 0; i < listeners.length; i++) {
                 if (listeners[i].fileRemoved(event)) {
                     return;
                 }
             }
         } catch (IOException e) {
             throw failure("deploy", "Error when deploying: " + entry.location, e);
         }
         // Standard processing
         LOG.info("Attempting to remove archive at: " + entry.location);
         try {
             container.getBroker().suspend();
             if ("component".equals(entry.type)) {
                 LOG.info("Uninstalling component: " + entry.name);
                 // Ensure installer is loaded
                 installationService.loadInstaller(entry.name);
                 // Uninstall and delete component
                 installationService.unloadInstaller(entry.name, true);
             }
             if ("library".equals(entry.type)) {
                 LOG.info("Removing shared library: " + entry.name);
                 installationService.uninstallSharedLibrary(entry.name);
             }
             if ("assembly".equals(entry.type)) {
                 LOG.info("Undeploying service assembly " + entry.name);
                 try {
                     if (deploymentService.isSaDeployed(entry.name)) {
                         deploymentService.shutDown(entry.name);
                         deploymentService.undeploy(entry.name);
                     }
                 } catch (Exception e) {
                     String errStr = "Failed to update service assembly: " + entry.name;
                     LOG.error(errStr, e);
                     throw new DeploymentException(errStr, e);
                 }
             }
         } finally {
             container.getBroker().resume();
         }
     }
 
     /**
      * Called when a component has been installed to see if pending service
      * assemblies have all component installed.
      */
     private void checkPendingSAs() {
         Set<File> deployedSas = new HashSet<File>();
         for (Map.Entry<File, ArchiveEntry> me : pendingSAs.entrySet()) {
             ArchiveEntry entry = me.getValue();
             boolean canDeploy = true;
             for (String componentName : entry.dependencies) {
                 if (container.getComponent(componentName) == null) {
                     canDeploy = false;
                     break;
                 }
             }
             if (canDeploy) {
                 File tmp = (File) me.getKey();
                 deployedSas.add(tmp);
                 try {
                     Descriptor root = DescriptorFactory.buildDescriptor(tmp);
                     deploymentService.deployServiceAssembly(tmp, root.getServiceAssembly());
                     deploymentService.start(root.getServiceAssembly().getIdentification().getName());
                 } catch (Exception e) {
                     String errStr = "Failed to update Service Assembly: " + tmp.getName();
                     LOG.error(errStr, e);
                 }
             }
         }
         if (!deployedSas.isEmpty()) {
             // Remove SA from pending SAs
             for (File f : deployedSas) {
                 ArchiveEntry entry = pendingSAs.remove(f);
                 entry.pending = false;
             }
             // Store new state
             persistState(environmentContext.getDeploymentDir(), deployFileMap);
             persistState(environmentContext.getInstallationDir(), installFileMap);
         }
     }
 
     private void checkPendingComponents() {
         Set<File> installedComponents = new HashSet<File>();
         for (Map.Entry<File, ArchiveEntry> me : pendingComponents.entrySet()) {
             ArchiveEntry entry = me.getValue();
             boolean canInstall = true;
             for (String libraryName : entry.dependencies) {
                 if (container.getRegistry().getSharedLibrary(libraryName) == null) {
                     canInstall = false;
                     break;
                 }
             }
             if (canInstall) {
                 File tmp = me.getKey();
                 installedComponents.add(tmp);
                 try {
                     Descriptor root = DescriptorFactory.buildDescriptor(tmp);
                     installationService.install(tmp, null, root, true);
                 } catch (Exception e) {
                     String errStr = "Failed to update Component: " + tmp.getName();
                     LOG.error(errStr, e);
                 }
             }
         }
         if (!installedComponents.isEmpty()) {
             // Remove SA from pending SAs
             for (File f : installedComponents) {
                 ArchiveEntry entry = pendingComponents.remove(f);
                 entry.pending = false;
             }
             // Store new state
             persistState(environmentContext.getDeploymentDir(), deployFileMap);
             persistState(environmentContext.getInstallationDir(), installFileMap);
             // Check for pending SAs
             checkPendingSAs();
         }
     }
 
     /**
      * Get an array of MBeanAttributeInfo
      * 
      * @return array of AttributeInfos
      * @throws JMException
      */
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "monitorInstallationDirectory", "Periodically monitor the Installation directory");
         helper.addAttribute(getObjectToManage(), "monitorInterval", "Interval (secs) before monitoring");
         return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
     }
 
     /**
      * Unpack a location into a temp file directory. If the location does not
      * contain a jbi descritor, no unpacking occurs.
      * 
      * @param location
      * @return tmp directory (if location contains a jbi descriptor)
      * @throws DeploymentException
      */
     protected static File unpackLocation(File tmpRoot, String location) throws DeploymentException {
         File tmpDir = null;
         File file = null;
         try {   
             if (location.startsWith(filePrefix)) {
                String os = System.getProperty("os.name");
                if (os.startsWith("Windows")) {
                    
                    location = location.replace('\\', '/');
                    location = location.replaceAll(" ", "%20");
                }
                 URI uri = new URI(location);
                 file = new File(uri);
             } else {
                 file = new File(location);
             }
             if (file.isDirectory()) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Deploying an exploded jar/zip, we will create a temporary jar for it.");
                 }
                 // If we have a directory then we should move it over
                 File newFile = new File(tmpRoot.getAbsolutePath() + "/exploded.jar");
                 newFile.delete();
                 FileUtil.zipDir(file.getAbsolutePath(), newFile.getAbsolutePath());
                 file = newFile;
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Deployment will now work from " + file.getAbsolutePath());
                 }
             }
             if (!file.exists()) {
                 // assume it's a URL
                 try {
                     URL url = new URL(location);
                     String fileName = url.getFile();
                     if (fileName == null) {
                         throw new DeploymentException("Location: " + location + " is not an archive");
                     }
                     file = FileUtil.unpackArchive(url, tmpRoot);
                 } catch (MalformedURLException e) {
                     throw new DeploymentException(e);
                 }
             }
             if (FileUtil.archiveContainsEntry(file, DescriptorFactory.DESCRIPTOR_FILE)) {
                 tmpDir = FileUtil.createUniqueDirectory(tmpRoot, file.getName());
                 FileUtil.unpackArchive(file, tmpDir);
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Unpacked archive " + location + " to " + tmpDir);
                 }
             }
         } catch (IOException e) {
             throw new DeploymentException(e);
         } catch (URISyntaxException ex) {
             throw new DeploymentException(ex);
         }
         return tmpDir;
     }
 
     private void scheduleDirectoryTimer() {
         if (!container.isEmbedded() && (isMonitorInstallationDirectory() || isMonitorDeploymentDirectory())) {
             if (statsTimer == null) {
                 statsTimer = new Timer(true);
             }
             if (timerTask != null) {
                 timerTask.cancel();
             }
             timerTask = new TimerTask() {
                 public void run() {
                     if (isMonitorInstallationDirectory()) {
                         monitorDirectory(environmentContext.getInstallationDir(), installFileMap);
                     }
                     if (isMonitorDeploymentDirectory()) {
                         monitorDirectory(environmentContext.getDeploymentDir(), deployFileMap);
                     }
                 }
             };
             long interval = monitorInterval * 1000;
             statsTimer.scheduleAtFixedRate(timerTask, 0, interval);
         }
     }
 
     private void monitorDirectory(final File root, final Map<String, ArchiveEntry> fileMap) {
         /*
          * if (log.isTraceEnabled()) { if (root != null) log.trace("Monitoring
          * directory " + root.getAbsolutePath() + " for new or modified
          * archives"); else log.trace("No directory to monitor for new or
          * modified archives for " + ((fileMap==installFileMap) ? "Installation" :
          * "Deployment") + "."); }
          */
         List<String> tmpList = new ArrayList<String>();
         if (root != null && root.exists() && root.isDirectory()) {
             File[] files = root.listFiles();
             if (files != null) {
                 for (int i = 0; i < files.length; i++) {
                     final File file = files[i];
                     tmpList.add(file.getName());
                     if (isAllowedExtension(file.getName()) && isAvailable(file)) {
                         ArchiveEntry lastEntry = fileMap.get(file.getName());
                         if (lastEntry == null || file.lastModified() > lastEntry.lastModified.getTime()) {
                             try {
                                 final ArchiveEntry entry = new ArchiveEntry();
                                 entry.location = file.getName();
                                 entry.lastModified = new Date(file.lastModified());
                                 fileMap.put(file.getName(), entry);
                                 LOG.info("Directory: " + root.getName() + ": Archive changed: processing " + file.getName() + " ...");
                                 updateArchive(file.getAbsolutePath(), entry, true);
                                 LOG.info("Directory: " + root.getName() + ": Finished installation of archive:  " + file.getName());
                             } catch (Exception e) {
                                 LOG.warn("Directory: " + root.getName() + ": Automatic install of " + file + " failed", e);
                             } finally {
                                 persistState(root, fileMap);
                             }
                         }
                     }
                 }
             }
             // now remove any locations no longer here
             Map<String, ArchiveEntry> map = new HashMap<String, ArchiveEntry>(fileMap);
             for (String location : map.keySet()) {
                 if (!tmpList.contains(location)) {
                     ArchiveEntry entry = fileMap.remove(location);
                     try {
                         LOG.info("Location " + location + " no longer exists - removing ...");
                         removeArchive(entry);
                     } catch (DeploymentException e) {
                         LOG.error("Failed to removeArchive: " + location, e);
                     }
                 }
             }
             if (!map.equals(fileMap)) {
                 persistState(root, fileMap);
             }
         }
     }
 
     private boolean isAvailable(File file) {
         // First check to see if the file is still growing
         long targetLength = file.length();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             //Do nothing
         }
         long target2Length = file.length();
 
         if (targetLength != target2Length) {
             LOG.warn("File is still being copied, deployment deferred to next cycle: " + file.getName());
             return false;
         }
 
         // If file size is consistent, do a foolproof check of the zip file
         try {
             ZipFile zip = new ZipFile(file);
             zip.size();
             zip.close();
         } catch (IOException e) {
             LOG.warn("Unable to open deployment file, deployment deferred to next cycle: " + file.getName());
             return false;
         }
 
         return true;
     }
 
     private boolean isAllowedExtension(String file) {
         String[] ext = this.extensions.split(",");
         for (int i = 0; i < ext.length; i++) {
             if (file.endsWith(ext[i])) {
                 return true;
             }
         }
         return false;
     }
 
     private void persistState(File root, Map<String, ArchiveEntry> map) {
         try {
             File file = new File(environmentContext.getJbiRootDir(), root.getName() + ".xml");
             XmlPersistenceSupport.write(file, map);
         } catch (IOException e) {
             LOG.error("Failed to persist file state to: " + root, e);
         }
     }
 
     @SuppressWarnings("unchecked")
     private Map<String, ArchiveEntry> readState(File root) {
         Map<String, ArchiveEntry> result = new HashMap<String, ArchiveEntry>();
         try {
             File file = new File(environmentContext.getJbiRootDir(), root.getName() + ".xml");
             if (file.exists()) {
                 result = (Map<String, ArchiveEntry>) XmlPersistenceSupport.read(file);
             } else {
                 LOG.debug("State file doesn't exist: " + file.getPath());
             }
         } catch (Exception e) {
             LOG.error("Failed to read file state from: " + root, e);
         }
         return result;
     }
 
     private void initializeFileMaps() {
         if (isMonitorInstallationDirectory() && !container.isEmbedded()) {
             try {
                 installFileMap = readState(environmentContext.getInstallationDir());
                 removePendingEntries(installFileMap);
             } catch (Exception e) {
                 LOG.error("Failed to read installed state", e);
             }
         }
         if (isMonitorDeploymentDirectory() && !container.isEmbedded()) {
             try {
                 deployFileMap = readState(environmentContext.getDeploymentDir());
                 removePendingEntries(deployFileMap);
             } catch (Exception e) {
                 LOG.error("Failed to read deployed state", e);
             }
         }
     }
 
     private void removePendingEntries(Map<String, ArchiveEntry> map) {
         Set<String> pendings = new HashSet<String>();
         for (Map.Entry<String, ArchiveEntry> e : map.entrySet()) {
             if (e.getValue().pending) {
                 pendings.add(e.getKey());
             }
         }
         for (String s : pendings) {
             map.remove(s);
         }
     }
 
     public static class ArchiveEntry {
         
         private String location;
         private Date lastModified;
         private String type;
         private String name;
         private boolean pending;
         private transient Set<String> dependencies;
 
         public String getLocation() {
             return location;
         }
 
         public void setLocation(String location) {
             this.location = location;
         }
 
         public Date getLastModified() {
             return lastModified;
         }
 
         public void setLastModified(Date lastModified) {
             this.lastModified = lastModified;
         }
 
         public String getType() {
             return type;
         }
 
         public void setType(String type) {
             this.type = type;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public boolean isPending() {
             return pending;
         }
 
         public void setPending(boolean pending) {
             this.pending = pending;
         }
 
         public Set<String> getDependencies() {
             return dependencies;
         }
 
         public void setDependencies(Set<String> dependencies) {
             this.dependencies = dependencies;
         }
     }
 }
