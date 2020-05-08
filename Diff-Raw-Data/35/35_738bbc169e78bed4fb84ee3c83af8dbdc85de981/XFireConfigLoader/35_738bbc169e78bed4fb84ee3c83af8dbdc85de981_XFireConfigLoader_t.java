 package org.codehaus.xfire.spring;
 
 import java.io.File;
 import java.util.Collections;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xbean.spring.context.impl.XBeanXmlBeanDefinitionReader;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireException;
 import org.codehaus.xfire.XFireFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.FileSystemResource;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * @author <a href="mailto:mikagoeckel@codehaus.org">Mika Goeckel</a>
  * 
  */
 public class XFireConfigLoader
 {
     private Log log = LogFactory.getLog(XFireConfigLoader.class);
    private File basedir;
    
    public File getBasedir()
    {
        return basedir;
    }

    public void setBasedir(File basedir)
    {
        this.basedir = basedir;
    }
 
     public ApplicationContext loadContext(String configPath, ApplicationContext parent) throws XFireException
     {
         ApplicationContext newCtx = getXFireApplicationContext(configPath, null);
 
         return newCtx;
     }
     
     public XFire loadConfig(String configPath) throws XFireException
     {
         ApplicationContext ctx = loadContext(configPath, null);
         
         return (XFire) ctx.getBean("xfire");
     }
     
     public XFire loadConfig(String configPath, ApplicationContext parent) throws XFireException
     {
         ApplicationContext ctx = loadContext(configPath, parent);
         
         return (XFire) ctx.getBean("xfire");
     }
 
     /**
      * @param configPath may not be full
      * @param parent may be null
      * @throws XFireException  if the configuration file is missing
      */
     private ApplicationContext getXFireApplicationContext(String configPath, ApplicationContext parent) throws XFireException
     {
         if (configPath == null)
         {
             throw new XFireException("Configuration file required");
         }
         
         GenericApplicationContext ctx = new GenericApplicationContext(parent);
         XBeanXmlBeanDefinitionReader xmlReader = new XBeanXmlBeanDefinitionReader(ctx, ctx.getDefaultListableBeanFactory(), Collections.EMPTY_LIST);
 
         if((parent == null) || !parent.containsBean("xfire"))
         {
             xmlReader.loadBeanDefinitions(new ClassPathResource("org/codehaus/xfire/spring/xfire.xml"));
         } 
         else 
         {
             // Include only custom editors, because they are nor inherited from springs parent defs.
             xmlReader.loadBeanDefinitions(new ClassPathResource("org/codehaus/xfire/spring/customEditors.xml"));
         }
 
         String[] configs = null;
         if (configPath.indexOf(",") != -1)
             configs = configPath.split(",");
         else 
             configs = new String[] { configPath };
         
         for (int i = 0; i < configs.length; i++)
         {
             String config = configs[i].trim();
            File file = (basedir != null) ?new File(basedir, config) : new File(config);
            if (file.exists())
             {
                xmlReader.loadBeanDefinitions(new FileSystemResource(file));
             }
             else
             {
                 xmlReader.loadBeanDefinitions(new ClassPathResource(config));
             }
         }
         
         ctx.refresh();
         
         XFire xfire = (XFire) ctx.getBean("xfire");
         log.debug("Setting XFire instance: "+xfire);
         
         // TODO: don't like this
         XFireFactory.newInstance().setXFire(xfire);
         
         return ctx;
     }
 }
