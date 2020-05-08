 package org.gridlab.gridsphere.services.core.messaging.impl;
 
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.services.core.messaging.TextMessagingService;
 import org.gridlab.gridsphere.tmf.TmfConfig;
 import org.gridlab.gridsphere.tmf.TmfCore;
 import org.gridlab.gridsphere.tmf.TmfMessage;
 import org.gridlab.gridsphere.tmf.TextMessagingException;
 import org.gridlab.gridsphere.tmf.config.TmfUser;
 
 import java.util.List;
 
 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 public class TextMessagingServiceImpl implements TextMessagingService, PortletServiceProvider {
 
     private static PortletLog log = SportletLog.getInstance(TextMessagingServiceImpl.class);
 
     private TmfCore core = null;
 
     public TmfMessage createNewMessage() {
         return core.getNewMessage();
     }
 
     public void send(TmfMessage message) {
         core.send(message);
     }
 
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         String configfile = config.getServletContext().getRealPath("WEB-INF/tmfconfig");
 
         log.info("Starting up TextMessagingService with config " + configfile);
 
         core = TmfCore.getInstance();
         try {
             core.loadConfig(configfile);
         } catch (TextMessagingException e) {
            log.info("Could not load configfile."+e);
         }
         core.startupServices();
     }
 
     public void destroy() {
         core.shutdown();
     }
 
     /**
      * Returns a list of tmf users objects
      *
      * @return a list of tmf users
      */
     public List getUsers() {
         TmfConfig config = core.getTmfConfig();
         return config.getUserlist().getUserlist();
     }
 
     public List getActiveServices() {
         TmfConfig config = core.getTmfConfig();
         return config.getActiveServices();
     }
 
     public void saveUser(TmfUser user) {
         TmfConfig config = core.getTmfConfig();
         try {
             config.setUser(user);
         } catch (TextMessagingException e) {
             log.error("Error saving users."+e);
         }
     }
 
     public TmfUser getUser(String userid) {
         TmfConfig config = core.getTmfConfig();
         return config.getUser(userid);
     }
 
     public boolean isUserOnService(String userid, String messagetype) {
         TmfConfig config = core.getTmfConfig();
         TmfUser u = config.getUser(userid);
        if (u==null) return false;
        String name = u.getUserNameForMessagetype(messagetype);
        boolean result = false;
        if (name!=null && !name.equals("")) result = true; // so the user entered something in there
         return result;
     }
 
     public boolean isUserOnline(String userid, String messagetype) {
         return false;
     }
 
 }
