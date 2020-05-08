 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import javax.servlet.ServletContext;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Vector;
 import java.util.ArrayList;
 import java.util.zip.GZIPOutputStream;
 
 /**
  * The <code>PortletLayoutEngine</code> is a singleton that is responsible for managing
  * user's layouts. It also manages portlet web application default layout
  * configurations that can be potentially added to a user layout
  * via the PortletLayout Service.
  * <p/>
  * The portlet layout engine is a higher level manager of portlet containers
  * that represent a users customized layout. The portlet layout engine is used
  * by the {@link org.gridlab.gridsphere.servlets.GridSphereServlet}
  * Expect the PortletLayoutEngine methods to change possibly....
  */
 public class PortletLayoutEngine {
 
     protected static PortletLog log = SportletLog.getInstance(PortletLayoutEngine.class);
     private static PortletLayoutEngine instance = new PortletLayoutEngine();
 
     private PortletPageFactory pageFactory = PortletPageFactory.getInstance();
 
     private PortletFrameRegistry registry = PortletFrameRegistry.getInstance();
 
 
     /**
      * Constructs a concrete instance of the PortletLayoutEngine
      */
     private PortletLayoutEngine() {
     }
 
    public void init(ServletContext ctx) {
        pageFactory.init(ctx);     
     }
 
     /**
      * Returns the single instance of the PortletLayoutEngine
      *
      * @return the PortletLayoutEngine instance
      */
     public static PortletLayoutEngine getInstance() {
         return instance;
     }
 
     public PortletPage getPortletPage(GridSphereEvent event) {
 
         PortletRequest req = event.getPortletRequest();
 
         // Check for framework errors
         Exception portletException = (Exception) req.getAttribute(SportletProperties.ERROR);
         if (portletException != null) {
             PortletPage errorPage = pageFactory.createErrorPage();
             errorPage.init(req, new ArrayList());
             return errorPage;
         }
 
         return pageFactory.getPortletPage(req);
     }
 
     public void setHeaders(GridSphereEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         res.setContentType("text/html; charset=utf-8"); // Necessary to display UTF-8 encoded characters
         res.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
         res.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
         res.setHeader("Expires", "0"); //Causes the proxy cache to see the page as "stale"
         res.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility
         String ae = req.getHeader("accept-encoding");
         if (ae != null && ae.indexOf("gzip") != -1) {
             res.setHeader("Content-Encoding", "gzip");
         }
     }
 
     /**
      * Services a portlet container instance by rendering its presentation
      *
      * @param event the gridsphere event
      * @throws IOException if an I/O error occcurs during processing
      */
     public void service(GridSphereEvent event) throws IOException {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         PortletPage page = getPortletPage(event);
         setHeaders(event);
         StringBuffer pageBuffer = new StringBuffer();
         if (req.getParameter("ajax") != null) {
             String portlet = req.getParameter("portlet");
             System.err.println("it's ajax: " + portlet);
             String cid = req.getParameter(SportletProperties.COMPONENT_ID);
             if ((cid != null) && (cid.startsWith("portlet"))) {
                 portlet = cid.substring("portlet".length()+1);
             }
             if (portlet != null) {
                 PortletFrameRegistry registry = PortletFrameRegistry.getInstance();
                 PortletFrame frame = registry.getPortletFrame("portlet#" + portlet, portlet, event);
                 frame.setInnerPadding("");
                 frame.setOuterPadding("");
                 frame.setTransparent(false);
 
                 req.getPortletSession().setAttribute(SportletProperties.LAYOUT_THEME, "default");
                 req.getPortletSession().setAttribute(SportletProperties.LAYOUT_RENDERKIT, "standard");
 
                 frame.doRender(event);
                 pageBuffer = frame.getBufferedOutput(req);
             } else {
                 PortletComponent comp = page.getActiveComponent(cid);
                 if (comp != null) {
                     String reqRole = comp.getRequiredRole();
                     User user = req.getUser();
                     if (user != null) {
                         if (req.getRoles().contains(reqRole)) comp.doRender(event);
                     } else {
                         if (reqRole.equals("")) comp.doRender(event);
                     }
                     pageBuffer = comp.getBufferedOutput(req);
                     res.setContentType("text/html");
                 }
             }
         } else {
             page.doRender(event);
             pageBuffer = page.getBufferedOutput(req);
         }
 
         try {
             String ae = req.getHeader("accept-encoding");
             if (ae != null && ae.indexOf("gzip") != -1) {
                 GZIPOutputStream gzos = new GZIPOutputStream(res.getOutputStream());
                 gzos.write(pageBuffer.toString().getBytes(req.getCharacterEncoding()));
                 gzos.close();
             }  else {
                 PrintWriter out = res.getWriter();
                 out.print(pageBuffer.toString());
             }
         } catch (IOException e) {
             // means the writer has already been obtained
             log.error("Error writing page!", e);
         }
     }
 
     /**
      * Invoked by the GridSphereServlet to perform portlet login of a users layout
      *
      * @param event the gridsphere event
      * @see org.gridlab.gridsphere.layout.PortletPage#loginPortlets
      */
     public void loginPortlets(GridSphereEvent event) {
         log.debug("in loginPortlets()");
         PortletRequest req = event.getPortletRequest();
         try {
             PortletPage page = getPortletPage(event);
             page.loginPortlets(event);
         } catch (Exception e) {
             log.error("Unable to loadUserLayout for user: " + req.getUser().getUserName(), e);
         }
     }
 
     /**
      * Invoked by the GridSphereServlet to perform portlet logout of a users layout
      *
      * @param event the gridsphere event
      * @see PortletPage#logoutPortlets
      */
     public void logoutPortlets(GridSphereEvent event) {
         log.debug("in logoutPortlets()");
 
         try {
             PortletPage page = getPortletPage(event);
             page.logoutPortlets(event);
             registry.removeAllPortletFrames(event);
             pageFactory.logStatistics();
         } catch (Exception e) {
             log.error("Unable to logout portlets", e);
         }
     }
 
     public void destroy() {
         pageFactory.destroy();
     }
 
     public void doAction(GridSphereEvent event) {
          String cid = event.getPortletComponentID();
          if (!cid.equals("")) {
              PortletFrame frame = registry.getPortletFrame(cid, null, event);
              if (frame != null)  {
                  try {
                      frame.actionPerformed(event);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }
          }
      }
 
     /**
      * Performs an action on the portlet container referenced by the
      * gridsphere event
      *
      * @param event a gridsphere event
      * @throws IOException if an I/O error occurs during rendering
      */
     public void actionPerformed(GridSphereEvent event) throws IOException {
         log.debug("in actionPerformed()");
         PortletPage page = getPortletPage(event);
         //int numcomps = page.getComponentIdentifierList().size();
         /*
         if (event.getPortletComponentID() < 0 || event.getPortletComponentID() > numcomps) {
         event.getPortletRequest().setAttribute(SportletProperties.COMPONENT_ID, "-1");
         }
         */
         if (!event.getPortletComponentID().equals("")) {
             page.actionPerformed(event);
 
             // sometimes the page needs reinitializing
             if (event.getPortletRequest().getAttribute(SportletProperties.INIT_PAGE) != null) {
                 log.info("\n\n\n\n\nreiniting and saving page!!!!!\n\n\n\n\n\n");
                 page.init(event.getPortletRequest(), new Vector());
                 PortletTabbedPane pane = pageFactory.getUserTabbedPane(event.getPortletRequest());
                 if (pane != null) pane.save();
             }
         }
 
         log.debug("Exiting actionPerformed()");
     }
 
     public void doRenderError(PortletRequest req, PortletResponse res, Throwable t) {
         PrintWriter out = null;
         try {
             out = res.getWriter();
         } catch (IOException e) {
             log.error("in doRenderError: ", e);
         }
         out.println("<html><body>");
         out.println("<b>An error occurred: " + t.getMessage() + "</b>");
         t.printStackTrace(out);
         out.println("</body></html>");
     }
 
     public void doRenderError(GridSphereEvent event, Throwable t) {
         PortletRequest req = event.getPortletRequest();
         PortletPage errorpage = pageFactory.createErrorPage();
         errorpage.init(req, new ArrayList());
         req.setAttribute("error", t);
         try {
             errorpage.doRender(event);
         } catch (Exception e) {
             log.error("in doRenderError: ", e);
         }
 
     }
 
     /**
      * Delivers a message to a specified concrete portlet on the current portlet page.
      * The method delegates the message delivery to the PortletPage implementation.
      *
      * @param concPortletID The concrete portlet ID of the target portlet
      * @param msg           The message to deliver
      * @param event         The event associated with the delivery
      */
     public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
         log.debug("in messageEvent()");
         PortletPage page = getPortletPage(event);
         page.messageEvent(concPortletID, msg, event);
         log.debug("Exiting messageEvent()");
 
     }
 
 }
