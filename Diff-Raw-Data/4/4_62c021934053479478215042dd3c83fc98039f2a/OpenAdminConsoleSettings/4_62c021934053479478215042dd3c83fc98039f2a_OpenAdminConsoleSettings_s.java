 package org.pentaho.pac.server.common;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.pentaho.pac.server.i18n.Messages;
 import org.pentaho.platform.engine.core.system.SystemSettings;
 import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
 
 /**
  * Reuse of {@link SystemSettings} where settings are read from <code>resource/config/console.xml</code>.
  * 
  * @author mlowery
  */
 public class OpenAdminConsoleSettings extends SystemSettings {
 
   private static final long serialVersionUID = -5912709145466266140L;
   private static final Log logger = LogFactory.getLog(OpenAdminConsoleSettings.class);
   public static final String DEFAULT_PROPERTIES_FILE_NAME = "console.xml"; //$NON-NLS-1$
   public OpenAdminConsoleSettings() {
     super();
   }
 
   /**
    * Need to override since default implementation points to solution path (e.g. pentaho-solutions).
    */
   @Override
   protected String getAbsolutePath(String path) {
     try {
       File file = new File(ClassLoader.getSystemResource(path).toURI());
       return file.getAbsolutePath();
     } catch(Exception e) {
       logger.info( Messages.getErrorString("AppConfigProperties.ERROR_0009_UNABLE_TO_GET_ABSOLUTE_PATH", path, e.getLocalizedMessage())); //$NON-NLS-1$
       return "resource/config/" + path; //$NON-NLS-1$  
     }
   }
 
   /**
    * Need to override since default implementation points to pentaho.xml.
    */
   @Override
   public String getSystemSetting(final String settingName, final String defaultValue) {
     return getSystemSetting(DEFAULT_PROPERTIES_FILE_NAME, settingName, defaultValue);
   }
 
   /**
    * Need to override to stop caching behavior. Caching is disabled to allow this object to see changes to
    * the console.xml file that are made by other objects.
    */
   @Override
   public Document getSystemSettingsDocument(final String actionPath) {
     
     File f = new File(getAbsolutePath(actionPath));
     if (!f.exists()) {
       return null;
     }
     Document systemSettingsDocument = null;
 
     try {
      systemSettingsDocument = XmlDom4JHelper.getDocFromFile(f, null);
     } catch (DocumentException e) {
       logger.error(e);
     } catch (IOException e) {
       logger.error(e);
     }
 
     return systemSettingsDocument;
   }
 
 }
 
 
