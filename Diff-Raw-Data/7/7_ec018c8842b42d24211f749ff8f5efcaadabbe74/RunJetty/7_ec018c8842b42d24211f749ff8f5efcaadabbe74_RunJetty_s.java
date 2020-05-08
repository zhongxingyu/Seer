 package org.monkey.web.server;
 
 import org.monkey.common.utils.WrapperUtils;
 import org.monkey.server.JettyServer;
 import org.tanukisoftware.wrapper.WrapperListener;
 import org.tanukisoftware.wrapper.WrapperManager;
 
 public class RunJetty implements WrapperListener {
 
     private JettyServer server;
 
     public static void main(String[] args) {
         WrapperUtils.startApplication(new RunJetty(), args);
     }
 
 
     @Override
     public Integer start(String[] strings) {
         try {
             doStart();
             return null;
         } catch (Exception e) {
             e.printStackTrace();
             return 1;
         }
     }
 
     private void doStart() throws Exception {
         WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_STATUS, "------------- Monkey Start Initiated -------------");
         WrapperUtils.registerServiceWrapperMBean();
 
         // todo - need to figure out a better way to specify the port number
        server = new JettyServer(8899, "Core Service", "Provides core application features");
         server.start();
 
         WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_STATUS, "------------- Monkey Started Successfully -------------");
     }
 
     @Override
     public int stop(int exitCode) {
         WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_STATUS, "------------- Monkey Shutdown Initiated -------------");
         try {
             server.stop();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return exitCode;
     }
 
     @Override
     public void controlEvent(int event) {
         WrapperUtils.controlEvent(event);
     }
 }
