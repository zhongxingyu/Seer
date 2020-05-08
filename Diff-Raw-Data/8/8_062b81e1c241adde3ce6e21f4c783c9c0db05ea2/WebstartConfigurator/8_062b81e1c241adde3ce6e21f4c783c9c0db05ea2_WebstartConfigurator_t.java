 
 
 package edu.duke.cabig.catrip.gui.webstart;
 
 import edu.duke.cabig.catrip.gui.util.GUIConstants;
 import edu.duke.cabig.catrip.gui.util.UnZip;
 import edu.duke.catrip.config.CatripConfigurationDocument;
 import edu.duke.catrip.config.GuiConfiguration;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 
 /**
  *
  * @author Sanjeev Agarwal
  */
 public class WebstartConfigurator {
     
     
     public static final String CATRIP_HOME = GUIConstants.CATRIP_HOME;//System.getProperty("user.home") + File.separator + ".caTRIP";
     public static final String CATRIP_CONFIG_FILE_LOCATION = CATRIP_HOME + File.separator + "catrip-config.xml";
     public static final String CATRIP_CONF_HOME = CATRIP_HOME + File.separator + "conf";
     
    private static String GLOBUS_ROOT_LOCATION = System.getProperty("user.home") + File.separator + ".globus";
     private static String ROOT_CERT_LOCATION = System.getProperty("user.home") + File.separator + ".globus" + File.separator+ "certificates";
     
     /** Creates a new instance of WebstartConfigurator */
     public WebstartConfigurator() {
     }
     
     public static void configure(){
         try{
             
             String versionFile = CATRIP_HOME + File.separator + GUIConstants.caTRIPVersion;
             File confDir = new File(CATRIP_HOME);
             
             if (confDir.exists() && isOldVersion()){
 //                confDir.renameTo(new File(CATRIP_HOME+"_backup_"+GUIConstants.caTRIPVersion));
                 deleteDir(confDir);
 //                confDir.mkdir();
                 configureForWebstart();
                 File verFile = new File(versionFile);
                 verFile.createNewFile();
             }
             
             if (!confDir.exists()){
                 configureForWebstart();
                 File verFile = new File(versionFile);
                 verFile.createNewFile();
             }
             
             File certDir = new File(ROOT_CERT_LOCATION);
             // versionFile = CATRIP_HOME + File.separator + GUIConstants.caTRIPVersion;
             
             if (certDir.exists()){
                 deleteDir(certDir);
 //                confDir.mkdir();
                 copyCertificate();
             }
             
             if (!certDir.exists()){
                 copyCertificate();
             }
             
             
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
     
     
     private static void copyCertificate(){
         try {
            
            File globusDir = new File(GLOBUS_ROOT_LOCATION);
            if (!globusDir.exists()){
                globusDir.mkdir();
            }
            
             File certDir = new File(ROOT_CERT_LOCATION);
             certDir.mkdir();
             
             InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("rootCA_cert.0");
             String outFilename = ROOT_CERT_LOCATION+File.separator+"rootCA_cert.0";
             File certFile = new File(outFilename);
             certFile.createNewFile();
             
             OutputStream out = new FileOutputStream(certFile);
             
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
             
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
     }
     
     public static void configureForWebstart(){
         try {
             
             // sanjeev: locate the zip file containing the configuration files.
             String inFilename = "conf.zip";
             //String dir  = System.getProperty("user.home")+File.separator+".caTRIP";
             File confDir = new File(CATRIP_HOME);confDir.mkdir();
             InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(inFilename);;//ClassLoader.getSystemClassLoader().getResourceAsStream(inFilename);
             String outFilename = CATRIP_HOME+File.separator+"conf.zip";
             OutputStream out = new FileOutputStream(outFilename);
             
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             out.close();
             in.close();
             
             UnZip.unzip(outFilename,CATRIP_HOME);
             new File(outFilename).delete();
             
             
             
             // sanjeev: replace the conf dir location.
             CatripConfigurationDocument conf = null;
             String configXMLFile = CATRIP_CONFIG_FILE_LOCATION;
             try {
                 conf = CatripConfigurationDocument.Factory.parse(new File(configXMLFile));
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             GuiConfiguration guiConfig =  conf.getCatripConfiguration().getGuiConfiguration();
             guiConfig.setRootDirectory(CATRIP_CONF_HOME);
             conf.save(new File(CATRIP_CONFIG_FILE_LOCATION));
             
             
             // sanjeev: replace the conf dir location.
             String fqeFile = CATRIP_HOME + File.separator + "query_engine_services_config.xml";
             Properties properties = new Properties();
             try {
                 properties.loadFromXML(new FileInputStream(new File(fqeFile )));
             } catch (IOException e) {
                 e.printStackTrace();
             }
             String wsddFile = CATRIP_HOME+File.separator+"conf"+File.separator+"client-config.wsdd";
             properties.setProperty("clientConfigWsdd",wsddFile);
             OutputStream os = new FileOutputStream(fqeFile);
             properties.storeToXML(os,"Modified via webstart.");
             
             
             
             // log file setup.
 //            File logFile = new File("C:\\caTRIP_logs.txt");
 //            if (!logFile.exists()){
 //                logFile.createNewFile();
 //            }
             
             
             
             
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     
     
     
     // code to check the config file version..
     
     private static boolean isOldVersion(){
         boolean olderVersion = false;
         String virsionFile = CATRIP_HOME + File.separator + GUIConstants.caTRIPVersion;
         File confDir = new File(virsionFile);
         if (!confDir.exists()){
             olderVersion = true;
         }
         return olderVersion;
     }
     
     
     
     
     
     
     
     
     
     
     
     // Deletes all files and subdirectories under dir.
     // Returns true if all deletions were successful.
     // If a deletion fails, the method stops attempting to delete and returns false.
     public static boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         
         // The directory is now empty so delete it
         return dir.delete();
     }
     
     
     
     
 }
