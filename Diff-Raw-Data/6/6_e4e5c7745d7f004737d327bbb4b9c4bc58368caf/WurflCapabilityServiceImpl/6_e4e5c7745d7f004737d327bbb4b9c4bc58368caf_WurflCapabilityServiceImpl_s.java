 /*
  * Copyright (C) 2010  Catalyst IT Limited
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package nz.net.catalyst.mobile.mdl.device;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.servlet.ServletContext;
 
 import org.apache.commons.jci.listeners.FileChangeListener;
 import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.context.ServletContextAware;
 
 import net.sourceforge.wurfl.core.CapabilityNotDefinedException;
 import net.sourceforge.wurfl.core.CustomWURFLHolder;
 import net.sourceforge.wurfl.core.Device;
 import net.sourceforge.wurfl.core.WURFLHolder;
 
 /**
  * Capability service implementation that uses the new wurfl java api
  * This also wathces the wurfl files and reloads them when changed
  * 
  * @author jun yamog
  *
  */
 
 public class WurflCapabilityServiceImpl implements CapabilityService, ServletContextAware {
    
    private final static Log logger = LogFactory.getLog(WurflCapabilityServiceImpl.class);
    
    private final static String WURFL_FILENAME = "wurfl.xml";
 
    // the variables below is declared as volatile to guarentee read consistency
    // across threads, writing is done inside a ReentrantLock
    // we only do the write on a single thread, see reloadWurfl method
    private volatile WURFLHolder wurflHolder; 
    private volatile StatusInfo statusInfo;
    private Lock wurflReloadLock = new ReentrantLock();
 
    private ServletContext servletContext;
    private List<FilesystemAlterationMonitor> famList = new ArrayList<FilesystemAlterationMonitor> ();
 
    private String wurflDirPath;
    private File wurflFile;
    private File[] wurflPatchFiles;
    
    /**
     * init will properly initialize this service.  this would look for wurfl files
     * and instantiate a wurfl holder
     */
    public void init() {
       
       // before running init check to see if attributes has been injected/set
       if (wurflDirPath == null)
          throw new IllegalStateException ("wurflDirPath not properly set");
       
       // look for wurfl file and patches
       File wurflDir = new File(wurflDirPath);
       // if file does not exists, it might be relative to the servlet
       if (!wurflDir.exists() && servletContext != null) 
          wurflDir = new File(servletContext.getRealPath(wurflDirPath));
       // check again
       if (!wurflDir.exists())
          throw new IllegalArgumentException("wurflDirPath " + wurflDir.getAbsolutePath() + " does not exists");
       
       if (!wurflDir.isDirectory()) 
          throw new IllegalArgumentException("wurflDirPath " + wurflDir.getAbsolutePath() + " is not a directory");
       
       ArrayList<File> patchFiles = new ArrayList<File> ();
 
       logger.info("search for wurfl file and patches on: " + wurflDir.getAbsolutePath());
       for (String filePath:wurflDir.list()) {
          File file = new File(wurflDir.getAbsoluteFile() + "/" + filePath);
          if (WURFL_FILENAME.equals(file.getName())) {
             wurflFile = file;
             logger.debug("wurfl file: " + wurflFile.getAbsolutePath());
          } else if (file.getName().endsWith(".xml")) {
             patchFiles.add(file);
             logger.debug("wurfl patch file: " + file.getAbsolutePath());
          }
                   
       }
       this.wurflPatchFiles = patchFiles.toArray(new File[] {});
 
       // initialize wurfl holder
       this.wurflHolder = new CustomWURFLHolder(this.wurflFile, this.wurflPatchFiles);
       
       startWatchingFiles();
       this.statusInfo = getNewStatusInfo("");
    }
    
    public void cleanup() {
       logger.info("stopping watching wurfl");
       stopWatchingFiles();
    }
    
    @Override
    public Map<String, Object> getCapabilitiesForDevice(RequestInfo requestInfo,
          List<String> capabilities) {
       Device device = wurflHolder.getWURFLManager().getDeviceForRequest(requestInfo.getUserAgent());
       Map<String, Object> capabilitiesMap = new HashMap<String, Object> (); 
       
       for (String capability:capabilities) {
          String capabilityStr;
       
          try {
             if ("device_id".equals(capability))
                capabilityStr = device.getId();
             else
                capabilityStr = device.getCapability(capability);
          } catch (CapabilityNotDefinedException e) {
             logger.warn(e);
             continue;
          }
          
          try {
             Integer capabilityInt = Integer.parseInt(capabilityStr);
             capabilitiesMap.put(capability, capabilityInt);
          } catch (NumberFormatException e) {
             if ("true".equalsIgnoreCase(capabilityStr) || "false".equalsIgnoreCase(capabilityStr)) {
                Boolean capabilityBoolean = Boolean.parseBoolean(capabilityStr);
                capabilitiesMap.put(capability, capabilityBoolean);
             } else {
                capabilitiesMap.put(capability, capabilityStr);
             }
          }
       }
       
       return capabilitiesMap;
    }
 
    @Override
    public StatusInfo getStatusInfo() {
       return this.statusInfo;
    }
    
    protected StatusInfo getNewStatusInfo(String wurflError) {
       // look for the last modified date of files
       long lastModified = wurflFile.lastModified();
 
       for (File patchFile:wurflPatchFiles) {
          long wurflPatchLastModified = patchFile.lastModified();
          if (lastModified < wurflPatchLastModified) lastModified = wurflPatchLastModified;
       }
 
       StatusInfo statusInfo = new StatusInfo(lastModified, wurflError);
       return statusInfo;
       
    }
 
    /**
     * create listeners and monitors for wurfl and its patches
     */
    protected synchronized void startWatchingFiles() {
       famList.clear();
 
       FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();
       FileChangeListener wurflListener = new WurflFileListener();
       fam.addListener(wurflFile, wurflListener);
       fam.start();
       famList.add(fam);
       logger.debug("watching " + wurflFile.getAbsolutePath());
 
       for (File patchFile : wurflPatchFiles) {
          fam = new FilesystemAlterationMonitor();
          FileChangeListener wurflPatchListener = new WurflFileListener();
          fam.addListener(patchFile, wurflPatchListener);
          fam.start();
          famList.add(fam);
          logger.debug("watching " + patchFile.getAbsolutePath());
       }
 
    }
 
    protected synchronized void stopWatchingFiles() {
       for (FilesystemAlterationMonitor fam : famList)
          fam.stop();
    }
 
    protected void reloadWurfl() {
       if (!wurflReloadLock.tryLock()) {
          logger.warn("unable to obtain wurflReloadLock, not reloading");
          return;
       }
 
       stopWatchingFiles(); // stop watching the files, so we dont keep on
                            // calling the callback it while reload is
                            // happening
       try {
          WURFLHolder tempWurflHolder = new CustomWURFLHolder(this.wurflFile, this.wurflPatchFiles);
          wurflHolder = tempWurflHolder;
          this.statusInfo = getNewStatusInfo("");
          logger.info("reload successful");
       } catch (Exception e) {
          logger.error("error in reloading wurfl", e);
         String errorMessage = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
          this.statusInfo = getNewStatusInfo(errorMessage);
       } finally {
          startWatchingFiles();
          wurflReloadLock.unlock();
       }
    }
 
    public void setWurflDirPath(String wurflDirPath) {
       this.wurflDirPath = wurflDirPath;
    }
 
    @Override
    public void setServletContext(ServletContext servletContext) {
       this.servletContext = servletContext;
       
    }
 
    protected class WurflFileListener extends FileChangeListener {
 
       public void onFileChange(File pFile) {
          super.onFileChange(pFile);
          if (hasChanged()) {
             logger.info(pFile.getAbsolutePath() + " file changed, reloading"); 
             reloadWurfl();
          }
       }
    }
 
 
 }
