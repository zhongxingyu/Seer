 package org.codehaus.xfire.spring;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireFactory;
 import org.xbean.spring.context.ClassPathXmlApplicationContext;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * 
  */
 public class XFireConfigLoader
 {
     private Log log = LogFactory.getLog(XFireConfigLoader.class);
     
     public XFire loadConfig(String[] configFiles)
     {
         List configList = new ArrayList();
         for (int i = 0; i < configFiles.length; i++)
             configList.add(configFiles[i]);
         
        configList.add("org/codehaus/xfire/spring/xfire.xml");
         
         ClassPathXmlApplicationContext ctx = 
             new ClassPathXmlApplicationContext((String[]) configList.toArray(new String[configList.size()]));
 
         XFire xfire = (XFire) ctx.getBean("xfire");
         log.debug("Setting XFire instance: "+xfire);
         
         // TODO: don't like this
         XFireFactory.newInstance().setXFire(xfire);
 
         return xfire;
     }
 
     public XFire loadConfig(String configPath)
     {
         return loadConfig(new String[] {configPath});
     }
 
 }
