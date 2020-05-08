 package org.xtest.runner;
 
import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.ui.IStartup;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class that gets called by Eclipse on startup to intialize the Xtest runner
  * 
  * @author Michael Barry
  */
 public class Startup implements IStartup {
     private static final Logger logger = LoggerFactory.getLogger(Startup.class);
 
     @Override
     public void earlyStartup() {
        // TODO move this configuration to common logging plugin
        PropertyConfigurator.configure(Activator.getDefault().getBundle()
                .getEntry("log4j.properties"));

         logger.info("Initializing Xtest runner service.");
         Activator.getDefault().boot();
     }
 
 }
