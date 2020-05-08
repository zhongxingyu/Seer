 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.DescriptorException;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.GuestUser;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletRequest;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * The portlet layout engine is responsible for managing user's layouts. It also manages
  * portlet web application default layout configurations that can be potentially added to a user layout
  * via the Layout Service
  */
 public class PortletLayoutEngine {
 
     private static PortletLog log = SportletLog.getInstance(PortletLayoutEngine.class);
 
     private static PortletLayoutEngine instance = new PortletLayoutEngine();
 
     private String layoutMappingPath;
 
     private PortletContainer guestContainer;
 
 
     private String newuserLayoutPath;
     private PortletContainer newuserContainer;
 
     private String userLayoutDir = null;
 
     private String error = "";
 
     // Store user layouts in a hash
     private Map userLayouts = new HashMap();
 
     // Store application tabs in a hash
     private Map applicationTabs = new HashMap();
 
     private PortletLayoutEngine() {}
 
     /**
      *
      */
     public static PortletLayoutEngine getInstance() {
         return instance;
     }
 
     public void addApplicationTab(String webApplicationName, PortletTab tab) {
         applicationTabs.put(webApplicationName, tab);
     }
 
     public void removeApplicationTab(String webApplicationName) {
         applicationTabs.remove(webApplicationName);
     }
 
     public void init() throws IOException, DescriptorException {
         userLayoutDir = GridSphereConfig.getProperty(GridSphereConfigProperties.USER_LAYOUT_DIR);
         if (userLayoutDir == null) {
             throw new DescriptorException("Unable to get user layout directory info from web.xml. Please specify user-layouts-dir in web.xml.");
         }
         File layDir = new File(userLayoutDir);
         if (!layDir.exists()) {
             layDir.mkdir();
         }
         layoutMappingPath = GridSphereConfig.getProperty(GridSphereConfigProperties.LAYOUT_MAPPING_XML);
         String guestLayoutPath =  GridSphereConfig.getProperty(GridSphereConfigProperties.LAYOUT_XML);
         guestContainer = PortletLayoutDescriptor.loadPortletContainer(guestLayoutPath, layoutMappingPath);
         guestContainer.init(new ArrayList());
 
         newuserLayoutPath =  GridSphereConfig.getProperty(GridSphereConfigProperties.NEW_USER_LAYOUT_XML);
         newuserContainer = PortletLayoutDescriptor.loadPortletContainer(newuserLayoutPath, layoutMappingPath);
         newuserContainer.init(new ArrayList());
     }
 
     public void removeUser(User user) {
         System.err.println("REMOVING USER: " + user.getFullName());
         if (userLayouts.containsKey(user)) {
             userLayouts.remove(user);
         }
     }
 
     public PortletContainer getPortletContainer(GridSphereEvent event) throws PortletLayoutException {
         // if user is guest then use guest template
         PortletContainer pc = null;
 
         User user = event.getSportletRequest().getUser();
         System.err.println(user.getFamilyName());
        if (user instanceof GuestUser) {
             return guestContainer;
 
             // Check if we have user's layout already
         } else if (userLayouts.containsKey(user)) {
 
             pc = (PortletContainer)userLayouts.get(user);
             // If not we try to load it in (creating new one if necessary)
         } else {
             try {
                 pc = createNewUserLayout(user);
                 pc.init(new ArrayList());
                 pc.loginPortlets(event);
             } catch (Exception e) {
                 log.error("Unable to loadUserLayout for user: " + user, e);
                 throw new PortletLayoutException("Unable to deserialize user layout from layout descriptor: " + e.getMessage());
             }
             userLayouts.put(user, pc);
         }
         return pc;
     }
 
     public void service(GridSphereEvent event) throws IOException {
         log.debug("in service()");
         boolean doLayoutAction = false;
         PortletContainer pc = null;
 
         // XXX: How do we signal a user has logged out so we can userLayouts.remove(user)???
         try {
             pc = getPortletContainer(event);
             pc.doRender(event);
         } catch (PortletLayoutException e) {
             error = e.getMessage();
             SportletRequest req = event.getSportletRequest();
             req.logRequest();
             log.error("Caught LayoutException: ", e);
         }
     }
 
     public PortletContainer getUserLayout(User user) {
         return (PortletContainer)userLayouts.get(user);
     }
 
     public void actionPerformed(GridSphereEvent event) throws IOException {
         log.debug("in service()");
         boolean doLayoutAction = false;
         PortletContainer pc = null;
 
         // XXX: How do we signal a user has logged out so we can userLayouts.remove(user)???
         try {
             pc = getPortletContainer(event);
             pc.actionPerformed(event);
         } catch (PortletLayoutException e) {
             error = e.getMessage();
             SportletRequest req = event.getSportletRequest();
             req.logRequest();
             log.error("Caught LayoutException: ", e);
         }
     }
 
     public void doRenderError(PortletRequest req, PortletResponse res, Throwable t) {
         PrintWriter out = null;
         try {
             out = res.getWriter();
         } catch (IOException e) {
             log.error("in doRenderError: ", e);
         }
         out.println("<h>Portlet Layout Engine unable to render!</h>");
         out.println("<b>" + error + "</b>");
     }
 
     protected PortletContainer createNewUserLayout(User user) throws DescriptorException, IOException {
 
         String layoutPath = getUserLayoutPath(user);
 
         File f = new File(layoutPath);
 
         // if no layout file exists for user, make new one from template
         if (!f.exists()) {
             f.createNewFile();
             copyFile(new File(newuserLayoutPath), f);
         }
         return PortletLayoutDescriptor.loadPortletContainer(layoutPath, layoutMappingPath);
     }
 
     public void saveUserLayout(User user) throws DescriptorException, IOException {
 
         PortletContainer pc = (PortletContainer)userLayouts.get(user);
         if (pc == null) {
             throw new DescriptorException("Layout does not exist for user: " + user.getID());
         }
         String userLayoutPath = getUserLayoutPath(user);
         PortletLayoutDescriptor.savePortletContainer(pc, userLayoutPath, layoutMappingPath);
     }
 
     protected String getUserLayoutPath(User user) {
         return userLayoutDir + "/" + user.getID();
     }
 
     protected void copyFile(File oldFile, File newFile) throws IOException {
         // Destination and streams
         log.info("in copyFile(): oldFile: " + oldFile.getAbsolutePath() + " newFile: " + newFile.getCanonicalPath());
         FileInputStream fis = new FileInputStream(oldFile);
         FileOutputStream fos = new FileOutputStream(newFile);
 
         // Amount of data to copy
         long fileLength = oldFile.length();
         byte[] bytes = new byte[1024]; // 1K at a time
         int length = 0;
         long totalLength = 0;
         while (length > -1) {
             length = fis.read(bytes);
             if (length > 0) {
                 fos.write(bytes, 0, length);
                 totalLength += length;
             }
         }
         // Test that we copied all the data
         if (fileLength != totalLength) {
             throw new IOException("File copy size missmatch");
         }
         fos.flush();
         fos.close();
         fis.close();
     }
 
 }
