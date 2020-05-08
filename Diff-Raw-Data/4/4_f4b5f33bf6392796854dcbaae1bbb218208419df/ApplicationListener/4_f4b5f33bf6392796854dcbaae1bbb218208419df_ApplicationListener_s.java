 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package listener;
 
 import bean.ApplicationLogger;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 
 /**
  * Web application lifecycle listener.
  *
  * @author Brian GOHIER
  */
 @WebListener()
 public class ApplicationListener implements ServletContextListener
 {
    
     @Override
     public void contextInitialized(ServletContextEvent sce)
     {
         ApplicationLogger.displayInfo("Démarrage de l'application");
         ApplicationLogger.start("application");
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce)
     {
         ApplicationLogger.destroy();
         ApplicationLogger.displayInfo("Fermeture de l'application");
     }
 }
