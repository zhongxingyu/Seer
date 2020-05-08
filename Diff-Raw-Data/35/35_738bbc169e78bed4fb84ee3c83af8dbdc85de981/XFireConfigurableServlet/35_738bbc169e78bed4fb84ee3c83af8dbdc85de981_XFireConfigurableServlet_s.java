 package org.codehaus.xfire.transport.http;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireException;
 import org.codehaus.xfire.spring.XFireConfigLoader;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.WebApplicationContext;
 
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
     
     /**
      * Path to configuration file 
      */
     private String configPath;
     
     /**
      * @return
      */
     private String getConfigPath()
     {
         if (configPath == null || configPath.length() == 0)
         {
             return CONFIG_FILE;
         }
         return configPath;
     }
 
 	public XFire createXFire() throws ServletException 
 	{
         configPath = getInitParameter(PARAM_CONFIG);
         XFire xfire;
         try 
         {
 			xfire = loadConfig(getConfigPath());
 		} 
 		catch (XFireException e) 
 		{
 			throw new ServletException(e);
 		}
         
         if(xfire == null || xfire.getServiceRegistry() == null || xfire.getServiceRegistry().getServices() == null || xfire.getServiceRegistry().getServices().size() == 0)
         {
             xfire = super.createXFire();
         }
 
         return xfire;
     }
     
    // we might want to move this one out if we want to cut the dependency on the javax.servlet package
     public XFire loadConfig(String configPath) throws XFireException
     {
         XFireConfigLoader loader = new XFireConfigLoader();
  
         ServletContext servletCtx = getServletContext();
         ApplicationContext parent = (ApplicationContext) servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
 
         ApplicationContext newCtx = loader.loadContext(configPath, parent);
         if(servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) == null)
         {
              servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, newCtx);
         }
 
         return (XFire) newCtx.getBean("xfire");
     }
 
     
 }
