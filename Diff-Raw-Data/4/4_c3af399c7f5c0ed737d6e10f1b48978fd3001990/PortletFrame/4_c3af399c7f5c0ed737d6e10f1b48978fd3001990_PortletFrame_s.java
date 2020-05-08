 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletWindow;
 import org.gridlab.gridsphere.portlet.impl.SportletURI;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portletcontainer.ConcretePortlet;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.services.container.registry.PortletRegistryService;
 import org.gridlab.gridsphere.services.container.registry.PortletRegistryServiceException;
 import org.gridlab.gridsphere.services.container.registry.impl.PortletRegistryServiceImpl;
import org.gridlab.gridsphere.services.container.caching.CachingService;
import org.gridlab.gridsphere.services.container.caching.impl.PortletCachingServiceImpl;
 import org.gridlab.gridsphere.event.ActionEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class PortletFrame extends BasePortletComponent {
 
     private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletFrame.class);
 
     private String portletClass;
     private String windowState = SportletWindow.NORMAL.toString();
     private String portletMode = Portlet.Mode.VIEW.toString();
 
     public PortletFrame() {
     }
 
     public void setConcretePortletClass(String portletClass) {
         this.portletClass = portletClass;
     }
 
     public String getConcretePortletClass() {
         return portletClass;
     }
 
     public void setWindowState(String windowState) {
         this.windowState = windowState;
     }
 
     public String getWindowState() {
         return windowState;
     }
 
     public void setPortletMode(String portletMode) {
         this.portletMode = portletMode;
     }
 
     public String getPortletMode() {
         return portletMode;
     }
 
     public void doRender(PortletContext ctx, PortletRequest req, PortletResponse res) throws PortletLayoutException, IOException {
         super.doRender(ctx, req, res);
         log.debug("in doRender()");
         PortletRegistryService registryService = null;
         //CachingService cachingService = null;
         try {
             //cachingService = PortletCachingServiceImpl.getInstance();
             registryService = PortletRegistryServiceImpl.getInstance();
         } catch (PortletRegistryServiceException e) {
             log.error("Failed to get registry instance in PortletFrame: ", e);
             throw new PortletLayoutException("Unable to get portlet instance");
         }
 
         if (border == null) border = new PortletBorder();
 
         System.err.println("contacting registry for portlet: " + portletClass);
         ConcretePortlet concretePortlet = registryService.getConcretePortlet(portletClass);
         AbstractPortlet abstractPortlet = concretePortlet.getAbstractPortlet();
         PortletSettings settings = concretePortlet.getSportletSettings();
 
         // Set the portlet ID
         req.setAttribute(GridSphereProperties.PORTLETID, settings.getConcretePortletID());
         System.err.println("concrete " + settings.getConcretePortletID());
         // Set the portlet window
         PortletWindow p = SportletWindow.getInstance(windowState);
         req.setAttribute(GridSphereProperties.PORTLETWINDOW, p);
 
         // Set the portlet mode
         String prevMode = req.getParameter(GridSphereProperties.PORTLETMODE);
         if (prevMode == null) prevMode = Portlet.Mode.VIEW.toString();
         req.getPortletSession().setAttribute(GridSphereProperties.PREVIOUSMODE, prevMode);
         req.getPortletSession().setAttribute(GridSphereProperties.PORTLETMODE, portletMode);
 
         // Create URI tags that can be used
         PortletURI minimizedModeURI = res.createURI(PortletWindow.State.MINIMIZED);
         PortletURI maximizedModeURI = res.createURI(PortletWindow.State.MAXIMIZED);
         PortletURI closedModeURI = res.createURI(PortletWindow.State.CLOSED);
         PortletURI restoreModeURI = res.createURI(PortletWindow.State.RESIZING);
 
         PortletURI modeURI = res.createURI();
         DefaultPortletAction dpa = new DefaultPortletAction(PortletAction.MODE);
         modeURI.addAction(dpa);
 
         modeURI.addParameter(GridSphereProperties.PORTLETMODE, Portlet.Mode.EDIT.toString());
         String edit = modeURI.toString();
         req.setAttribute(LayoutProperties.EDITURI, edit);
 
         modeURI.addParameter(GridSphereProperties.PORTLETMODE, Portlet.Mode.HELP.toString());
         String help = modeURI.toString();
         req.setAttribute(LayoutProperties.HELPURI, help);
 
         modeURI.addParameter(GridSphereProperties.PORTLETMODE, Portlet.Mode.CONFIGURE.toString());
         String configure = modeURI.toString();
         req.setAttribute(LayoutProperties.CONFIGUREURI, configure);
 
         // set the portlet frame title
         String title = settings.getTitle(req.getLocale(), req.getClient());
         border.setTitle(title);
 
         // render portlet frame
         ///// begin portlet frame
         PrintWriter out = res.getWriter();
         out.println("<table width=\"" + width + "%\"  border=\"0\" cellspacing=\"2\" cellpadding=\"0\" bgcolor=\"#FFFFFF\"><tr><td>");
         out.println("<table width=\"100%\" border=\"0\" cellspacing=\"2\" cellpadding=\"0\" bgcolor=\"#999999\">");
         out.println("<tr><td width=\"100%\">");
 
         border.doRender(ctx, req, res);
 
 
         out.println("</td></tr>");
         out.println("<tr><td valign=\"top\" align=\"left\"><table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\" bgcolor=");
         out.println("\"" + bgColor + "\"<tr><td width=\"25%\" valign=\"center\">");
 
         try {
             if (abstractPortlet != null) {
                 abstractPortlet.service(req, res);
             }
         } catch (PortletException e) {
             log.error("Failed invoking portlet service method: ", e);
             throw new PortletLayoutException("Failed invoking portlet service method");
         }
 
         out.println("</tr></table></td></tr></table></td></tr></table>");
 
 
         ///// end portlet frame
     }
 
     public void doRenderFirst(PortletContext ctx, PortletRequest req, PortletResponse res) throws PortletLayoutException, IOException {
         doRender(ctx, req, res);
     }
 
     public void doRenderLast(PortletContext ctx, PortletRequest req, PortletResponse res) throws PortletLayoutException, IOException {}
 
 }
