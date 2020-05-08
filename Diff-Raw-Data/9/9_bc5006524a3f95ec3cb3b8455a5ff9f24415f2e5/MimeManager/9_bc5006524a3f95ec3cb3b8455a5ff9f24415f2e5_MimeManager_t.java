 package com.alcshare.docs;
 
 import com.alcshare.docs.util.AddOnFiles;
 import com.alcshare.docs.util.FileResource;
 import com.alcshare.docs.util.Logging;
 import org.apache.commons.io.IOUtils;
 
 import javax.servlet.ServletContext;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.util.Properties;
 
 /**
  *
  */
 public class MimeManager {
     private static final String DEFAULT_MIME_TYPE = "text/plain";
     private static FileResource MIME_CONFIG;
 
     private static final Properties mappings = new Properties();
 
     public static synchronized String getMimeTypeForExtension(String extension) {
         String result = (String) mappings.get(extension);
         return result != null ? result : DEFAULT_MIME_TYPE;
     }
 
     private static synchronized void loadMimeTypes() {
         mappings.clear();
 
         InputStream is = null;
         try {
             is = MIME_CONFIG.openBufferedStream();
             mappings.load(is);
         } catch (IOException e) {
             Logging.println("Error loading "+MIME_CONFIG, e);
         } finally {
             IOUtils.closeQuietly(is);
         }
     }
 
     public static void initialize(ServletContext context) {
         if (MIME_CONFIG == null) {
             try {
                MIME_CONFIG = new FileResource(new File(AddOnFiles.getConfigDirectory(), "mime.properties"),
                     context.getResource("/WEB-INF/config/mime.properties"), true);
                 loadMimeTypes();
             } catch (MalformedURLException e) {
                 Logging.println("Error getting default mime.properties", e);
             }
         }
     }
 }
