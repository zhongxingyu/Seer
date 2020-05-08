 package gov.usgs.cida.gdp.wps.servlet;
 
 import gov.usgs.cida.gdp.constants.AppConstant;
 import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
 import gov.usgs.cida.gdp.utilities.FileHelper;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.slf4j.LoggerFactory;
 
 /**
  * Servlet implementation class FileWipeAutomationServlet
  */
 public class FileWipeAutomationServlet implements ServletContextListener {
 
     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FileWipeAutomationServlet.class);
     private static final long serialVersionUID = 1L;
     private static final long WIPER_CHECK_RATE = Long.parseLong(AppConstant.FILE_WIPE_CHECK_RATE.getValue());
     private static Timer task;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public FileWipeAutomationServlet() {
         super();
     }
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
         initializeFilewipeTimer();
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce) {
         FileWipeAutomationServlet.task.cancel();
         FileWipeAutomationServlet.task.purge();
         LOG.info("File Wipe system stopped.");
     }
     
     /**
      * Initializes a timer that will check the file system every hour and wipes any files
      * over 48 hours long.
      */
     private void initializeFilewipeTimer() {
         LOG.info("File Wipe system starting.");
 
         long fileAgeLong = Long.parseLong(AppConstant.FILE_WIPE_MILLIS.getValue());
         File uploadDirName = new File(AppConstant.SHAPEFILE_LOCATION.getValue());
         File userSpaceDir = new File(AppConstant.USERSPACE_LOCATION.getValue());
         File workSpaceDir = new File(AppConstant.WORK_LOCATION.getValue());
 
         // Ensure the directories exist - this won't recreate them if they already exist
         FileHelper.createDir(uploadDirName);
         FileHelper.createDir(userSpaceDir);
         FileHelper.createDir(workSpaceDir);
 
         task = new Timer("File-Wipe-Timer",true);
         task.scheduleAtFixedRate(new ScanFileTask(workSpaceDir, fileAgeLong), 0l, WIPER_CHECK_RATE);
         
         LOG.info("File Wipe system started.");
     }
 
     private static class ScanFileTask extends TimerTask {
         private long hoursToWipe;
         private File workspaceDir;
 
         @Override
         public void run() {
             LOG.info("Running File Wipe Task... ");
 
             try {
                 GeoserverManager gm = new GeoserverManager(AppConstant.WFS_ENDPOINT.getValue(),
                         AppConstant.WFS_USER.getValue(), AppConstant.WFS_PASS.getValue());
                 String[] checkWorkspaces = AppConstant.FILE_WIPE_CHECK_WORKSPACES.getValue().split(",");
                 gm.deleteOutdatedDataStores(hoursToWipe, checkWorkspaces);
             } catch (IOException ex) {
				LOG.error("File Wipe Task Error. Error encountered: " + ex.getMessage());
             } catch (XPathExpressionException ex) {
				LOG.error("File Wipe Task Error. Error encountered: " + ex.getMessage());
             }
             
             if (getWorkspaceDir() != null && getWorkspaceDir().exists()) {
                 LOG.info("Checking work space directory " + getWorkspaceDir().getPath() + " for files older than " + Long.valueOf(this.hoursToWipe) + "ms");
                 Collection<File> filesDeleted = FileHelper.wipeOldFiles(getWorkspaceDir(), Long.valueOf(this.hoursToWipe), false);
                 if (!filesDeleted.isEmpty()) {
                     LOG.info("Finished deleting workspace files. " + filesDeleted.size() + " deleted.");
                 }
             }
 
         }
 
         ScanFileTask(File workspaceDir, long hoursToWipe) {
             this.workspaceDir = workspaceDir;
             this.hoursToWipe = hoursToWipe;
         }
 
         ScanFileTask() {
             this.hoursToWipe = Long.getLong(AppConstant.FILE_WIPE_MILLIS.toString());
         }
 
         /**
          * @return the hoursToWipe
          */
         public long getHoursToWipe() {
             return this.hoursToWipe;
         }
 
         /**
          * @param hoursToWipe the hoursToWipe to set
          */
         public void setHoursToWipe(@SuppressWarnings("hiding") long hoursToWipe) {
             this.hoursToWipe = hoursToWipe;
         }
 
         /**
          * @return the workspaceDir
          */
         public File getWorkspaceDir() {
             return workspaceDir;
         }
 
         /**
          * @param workspaceDir the workspaceDir to set
          */
         public void setWorkspaceDir(File workspaceDir) {
             this.workspaceDir = workspaceDir;
         }
 
     }
 }
