 package org.codehaus.xfire.transport.http;
 
import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.spring.XFireConfigLoader;
 import org.codehaus.xfire.util.XMLServiceBuilder;
 
 /**
  * XFire Servlet as Dispatcher including a configuration<br>
  * of XFire from services.xml in classpath<br>
  * <p>
  */
 public class XFireConfigurableServlet
     extends XFireServlet
 {
     private static Log log = LogFactory.getLog(XFireConfigurableServlet.class);    
     
     private final static String CONFIG_FILE = "META-INF/xfire/services.xml";
 
     private final static String PARAM_CONFIG="config";
     
     private final static String PARAM_USE_NEW_CONFIG="useNewConfig";
     /**
      * Path to configuration file 
      */
     private String configPath;
     
     /**
      * Determine if new xbean config should be used.
      */
     private boolean useNewConfig;
 
     
     /**
      * @return
      */
     private String getConfigPath(){
         if( configPath == null || configPath.length() == 0){
             return CONFIG_FILE;
         }
         return configPath;
     }
     /**
      * @see javax.servlet.Servlet#init()
      */
     public void init()
         throws ServletException
     {
         String useNewConfigStr = getInitParameter(PARAM_USE_NEW_CONFIG);
         
         if (useNewConfigStr == null) useNewConfigStr = "true";
         
         useNewConfig = Boolean.valueOf(useNewConfigStr).booleanValue();
         configPath = getInitParameter(PARAM_CONFIG);
        Enumeration en ;
         
         try
         {
            
             log.debug("Searching for  "+getConfigPath());
            en = getClass().getClassLoader().getResources(getConfigPath());
            if( !en.hasMoreElements()){
                log.warn("Can't find any configuration file : "+ getConfigPath());
            }
            
             if (!useNewConfig)
             {
                 super.init();
                configureXFire(en);
             }
             else
             {
                if( en.hasMoreElements()){
                  configureXFireNew();
                }else{
                    xfire = createXFire();
                }
                 controller = createController();
             }
 
             log.debug("Loading configuration done.");
         }
         catch (Exception e)
         {
             log.error("Couldn't configure XFire", e);
         }
 
     }
 
     /**
      * @throws Exception
      */
    protected void configureXFire(Enumeration en)
         throws Exception
     {
 
         XMLServiceBuilder builder = new XMLServiceBuilder(getXFire());
         
         while (en.hasMoreElements())
         {
             URL resource = (URL) en.nextElement();
             builder.buildServices(resource.openStream());
         }
     }
 
     /**
      * @throws Exception
      */
     protected void configureXFireNew()
         throws Exception
     {
        
          XFireConfigLoader loader = new XFireConfigLoader();
          xfire = loader.loadConfig(getConfigPath());
        
     }
 }
