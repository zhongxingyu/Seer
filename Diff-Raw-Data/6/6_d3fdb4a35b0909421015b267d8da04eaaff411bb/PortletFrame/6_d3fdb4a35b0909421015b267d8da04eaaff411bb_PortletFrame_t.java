 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.layout.event.PortletComponentEvent;
 import org.gridlab.gridsphere.layout.event.PortletFrameEvent;
 import org.gridlab.gridsphere.layout.event.PortletFrameListener;
 import org.gridlab.gridsphere.layout.event.PortletTitleBarEvent;
 import org.gridlab.gridsphere.layout.event.impl.PortletFrameEventImpl;
 import org.gridlab.gridsphere.layout.event.impl.PortletTitleBarEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portletcontainer.PortletDataManager;
 import org.gridlab.gridsphere.portletcontainer.PortletInvoker;
 import org.gridlab.gridsphere.portletcontainer.impl.SportletDataManager;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Collections;
 
 /**
  * <code>PortletFrame</code> provides the visual representation of a portlet. A portlet frame
  * contains a portlet title bar unless visible is set to false.
  */
 public class PortletFrame extends BasePortletComponent implements Serializable, Cloneable {
 
     // renderPortlet is true in doView and false on minimized
     private boolean renderPortlet = true;
     private String portletClass = null;
     private PortletTitleBar titleBar = null;
     private PortletErrorFrame errorFrame = new PortletErrorFrame();
     private boolean transparent = false;
     private String innerPadding = "";   // has to be empty and not 0!
     private String outerPadding = "";   // has to be empty and not 0!
 
     //private PortletRole requiredRole = PortletRole.GUEST;
 
     // keep track of the original width
     private String originalWidth = "";
 
     private transient PortletDataManager dataManager = null;
 
     private boolean hasTitleBarEvent = false;
     /**
      * Constructs an instance of PortletFrame
      */
     public PortletFrame() {
     }
 
     /**
      * Sets the portlet title bar contained by this portlet frame
      *
      * @param titleBar the portlet title bar
      */
     public void setPortletTitleBar(PortletTitleBar titleBar) {
         this.titleBar = titleBar;
     }
 
     /**
      * Returns the portlet title bar contained by this portlet frame
      *
      * @return the portlet title bar
      */
     public PortletTitleBar getPortletTitleBar() {
         return titleBar;
     }
 
     /**
      * Sets the portlet class contained by this portlet frame
      *
      * @param portletClass the fully qualified portlet classname
      */
     public void setPortletClass(String portletClass) {
         this.portletClass = portletClass;
     }
 
     /**
      * Returns the portlet class contained by this portlet frame
      *
      * @return the fully qualified portlet classname
      */
     public String getPortletClass() {
         return portletClass;
     }
 
     /**
      * Sets the inner padding of the portlet frame
      *
      * @param innerPadding the inner padding
      */
     public void setInnerPadding(String innerPadding) {
         this.innerPadding = innerPadding;
     }
 
     /**
      * Returns the inner padding of the portlet frame
      *
      * @return the inner padding
      */
     public String getInnerPadding() {
         return innerPadding;
     }
 
     /**
      * Sets the outer padding of the portlet frame
      *
      * @param outerPadding the outer padding
      */
     public void setOuterPadding(String outerPadding) {
         this.outerPadding = outerPadding;
     }
 
     /**
      * Returns the outer padding of the portlet frame
      *
      * @return the outer padding
      */
     public String getOuterPadding() {
         return outerPadding;
     }
 
     /**
      * If set to <code>true</code> the portlet is rendered transparently without a
      * defining border and title bar. This is used for example for the LogoutPortlet
      *
      * @param transparent if set to <code>true</code>, portlet frame is displayed transparently, <code>false</code> otherwise
      */
     public void setTransparent(boolean transparent) {
         this.transparent = transparent;
     }
 
     /**
      * If set to <code>true</code> the portlet is rendered transparently without a
      * defining border and title bar. This is used for example for the LogoutPortlet
      *
      * @return <code>true</code> if the portlet frame is displayed transparently, <code>false</code> otherwise
      */
     public boolean getTransparent() {
         return this.transparent;
     }
 
     /**
      * Initializes the portlet frame component. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a <code>List</code> of component identifiers
      * @return a <code>List</code> of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(PortletRequest req, List list) {
         list = super.init(req, list);
         dataManager = SportletDataManager.getInstance();
         ComponentIdentifier compId = new ComponentIdentifier();
         compId.setPortletComponent(this);
         compId.setPortletClass(portletClass);
         compId.setComponentID(list.size());
         compId.setComponentLabel(label);
         compId.setClassName(this.getClass().getName());
         list.add(compId);
         hasTitleBarEvent = false;
         this.originalWidth = width;
         // if the portlet frame is transparent then it doesn't get a title bar
         if ((transparent == false) && (titleBar == null)) titleBar = new PortletTitleBar();
         if (titleBar != null) {
             // if title bar is not assigned a label and we have one then use it
             if ((!label.equals("")) && (titleBar.getLabel().equals(""))) titleBar.setLabel(label+"TB");
             titleBar.setPortletClass(portletClass);
             list = titleBar.init(req, list);
             titleBar.addComponentListener(this);
         }
         return list;
     }
 
 
     /**
      * Fires a frame event notification
      *
      * @param event a portlet frame event
      * @throws PortletLayoutException if a layout error occurs
      */
     protected void fireFrameEvent(PortletFrameEvent event) throws PortletLayoutException {
         List slisteners = Collections.synchronizedList(listeners);
         synchronized (slisteners) {
         Iterator it = slisteners.iterator();
         PortletFrameListener l;
         while (it.hasNext()) {
             l = (PortletFrameListener) it.next();
             l.handleFrameEvent(event);
         }
         }
     }
 
     /**
      * Performs an action on this portlet frame component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
         System.err.println("in action performed portlet frame: " + portletClass);
 
         super.actionPerformed(event);
 
         if ((titleBar != null) && (!hasTitleBarEvent)) {
             titleBar.setActive(true);
             PortletTitleBarEvent tbEvent = new PortletTitleBarEventImpl(titleBar, event, COMPONENT_ID);
             if (tbEvent.getAction() != null) {
                 hasTitleBarEvent = true;
                 titleBar.actionPerformed(event);
             }
         }
 
         hasTitleBarEvent = false;
 
         PortletComponentEvent titleBarEvent = event.getLastRenderEvent();
 
         if ((titleBarEvent != null) && (titleBarEvent instanceof PortletTitleBarEvent)) {
             PortletTitleBarEvent tbEvt = (PortletTitleBarEvent)titleBarEvent;
             if (titleBarEvent.getAction() == PortletTitleBarEvent.TitleBarAction.WINDOW_MODIFY) {
                 PortletWindow.State state = tbEvt.getState();
                 PortletFrameEventImpl frameEvent = null;
                 if (state == PortletWindow.State.MINIMIZED) {
                     renderPortlet = false;
                     frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_MINIMIZED, COMPONENT_ID);
                 } else if (state == PortletWindow.State.RESIZING) {
                     renderPortlet = true;
                     frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                     frameEvent.setOriginalWidth(originalWidth);
                 } else if (state == PortletWindow.State.MAXIMIZED) {
                     renderPortlet = true;
                     frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                 }
 
                 List slisteners = Collections.synchronizedList(listeners);
                 synchronized (slisteners) {
                 Iterator it = slisteners.iterator();
                 PortletComponent comp;
                 while (it.hasNext()) {
                     comp = (PortletComponent) it.next();
                     event.addNewRenderEvent(frameEvent);
                     comp.actionPerformed(event);
                 }
                 }
 
             }
 
         } else {
 
             // process events
             PortletRequest req = event.getPortletRequest();
 
             req.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
 
             PortletRole role = req.getRole();
             if (role.compare(role, requiredRole) < 0) return;
 
             PortletResponse res = event.getPortletResponse();
 
             req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
             // Override if user is a guest
             User user = req.getUser();
             if (user instanceof GuestUser) {
                 req.setMode(Portlet.Mode.VIEW);
             } else {
                 if (titleBar != null) {
                     Portlet.Mode mode = titleBar.getPortletMode();
                     //System.err.println("setting mode in " + portletClass + " to " + mode.toString());
                     req.setMode(mode);
                 } else {
                     req.setMode(Portlet.Mode.VIEW);
                 }
             }
 
 
             // Set the portlet data
             PortletData data = null;
             if (!(user instanceof GuestUser)) {
                 try {
                     data = dataManager.getPortletData(req.getUser(), portletClass);
                     req.setAttribute(SportletProperties.PORTLET_DATA, data);
                 } catch (PersistenceManagerException e) {
                     errorFrame.setError("Unable to retrieve user's portlet data!", e);
                 }
             }
 
             // now perform actionPerformed on Portlet if it has an action
 
             //System.err.println("in PortletFrame action invoked for " + portletClass);
             if (event.hasAction()) {
                 DefaultPortletAction action = event.getAction();
                 if (action.getName() != "") {
                     try {
                         PortletInvoker.actionPerformed(portletClass, action, req, res);
                     } catch (PortletException e) {
                         errorFrame.setException(e);
                     }
                     String message = (String)req.getAttribute(SportletProperties.PORTLETERROR+portletClass);
                     if (message != null) {
                         errorFrame.setMessage(message);
                     }
                 }
                 // in case portlet mode got reset
             }
             if (titleBar != null) titleBar.setPortletMode(req.getMode());
 
             List slisteners = Collections.synchronizedList(listeners);
             synchronized (slisteners) {
             Iterator it = slisteners.iterator();
             PortletComponent comp;
             while (it.hasNext()) {
                 comp = (PortletComponent) it.next();
                 event.addNewRenderEvent(titleBarEvent);
                 comp.actionPerformed(event);
             }
             }
         }
 
     }
 
     /**
      * Renders the portlet frame component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
 
         errorFrame.clearError();
         PortletRequest req = event.getPortletRequest();
         PortletRole userRole = req.getRole();
         if (userRole.compare(userRole, requiredRole) < 0) {
             return;
         }
 
         super.doRender(event);
 
         PortletResponse res = event.getPortletResponse();
         PrintWriter out = res.getWriter();
 
         req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
         if (errorFrame.hasError()) {
             errorFrame.doRender(event);
             return;
         }
 
         // Set the portlet data
         User user = req.getUser();
         PortletData data = null;
         if (!(user instanceof GuestUser)) {
             try {
                 data = dataManager.getPortletData(req.getUser(), portletClass);
                 req.setAttribute(SportletProperties.PORTLET_DATA, data);
             } catch (PersistenceManagerException e) {
                 errorFrame.setError("Unable to retrieve user's portlet data", e);
             }
         }
 
         ///// begin portlet frame
         out.println("<!-- PORTLET STARTS HERE -->");
         //out.println("<div class=\"window-main\">");
         out.print("<table  ");
 
         if (getOuterPadding().equals("")) {
            out.print(" cellspacing=\"0\" class=\"window-main\" ");
         } else {
             //out.print("border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"");        // this is the main table around one portlet
             //out.print(" cellspacing=\""+getOuterPadding()+"\" style=\"padding:"+getOuterPadding()+"px\"  class=\"window-main\" ");        // this is the main table around one portlet
             out.print(" cellspacing=\"0\" style=\"margin:"+getOuterPadding()+"px\"  class=\"window-main\" ");        // this is the main table around one portlet
             //out.print("cellpadding=\""+getOuterPadding()+"\" class=\"window-main\" ");        // this is the main table around one portlet
         }
 
         out.println(">");
 
         // Render title bar
         if (titleBar != null) {
             titleBar.doRender(event);
             if (titleBar.hasRenderError()) {
                 errorFrame.setMessage(titleBar.getErrorMessage());
             }
        } else {
            req.setMode(Portlet.Mode.VIEW);
            req.setAttribute(SportletProperties.PREVIOUS_MODE, Portlet.Mode.VIEW);
            req.setAttribute(SportletProperties.PORTLET_WINDOW, PortletWindow.State.NORMAL);
         }

         if (renderPortlet) {
             if (!transparent) {
                 out.print("<tr><td  ");      // now the portlet content begins
                 if (!getInnerPadding().equals("")) {
                     out.print("style=\"padding:" + getInnerPadding() + "px\"");
                 }
                 out.println(" class=\"window-content\"> " );
             } else {
                 out.println("<tr><td>");
             }
 
             if (errorFrame.hasError()) {
                 errorFrame.doRender(event);
             } else {
                 try {
                     PortletInvoker.service(portletClass, req, res);
                 } catch (PortletException e) {
                     errorFrame.setError("Unable to invoke service method", e);
                     errorFrame.doRender(event);
                 }
             }
             out.println("</td></tr>");
         } else {
             out.println("<tr><td class=\"window-content-minimize\">");      // now the portlet content begins
             out.println("</td></tr>");
         }
         out.println("</table>");
         out.println("<!--- PORTLET ENDS HERE -->");
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletFrame f = (PortletFrame)super.clone();
         f.titleBar = (this.titleBar == null) ? null : (PortletTitleBar)this.titleBar.clone();
         f.outerPadding = this.outerPadding;
         f.errorFrame = this.errorFrame;
         f.transparent = this.transparent;
         f.innerPadding = this.innerPadding;
         f.portletClass = this.portletClass;
         f.roleString = this.roleString;
         f.requiredRole = (PortletRole)this.requiredRole.clone();
         f.renderPortlet = this.renderPortlet;
         f.hasTitleBarEvent = false;
         return f;
     }
 
 
     /* (non-Javadoc)
     * @see org.gridlab.gridsphere.layout.PortletComponent#messageEvent(java.lang.String, org.gridlab.gridsphere.portlet.PortletMessage, org.gridlab.gridsphere.portletcontainer.GridSphereEvent)
     */
     public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
 
         if (portletClass.equals(concPortletID)) {
             PortletRequest req = event.getPortletRequest();
 
             req.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
 
             PortletRole role = req.getRole();
             if (role.compare(role, requiredRole) < 0) return;
 
             PortletResponse res = event.getPortletResponse();
 
             req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
             // Override if user is a guest
             User user = req.getUser();
             if (user instanceof GuestUser) {
                 req.setMode(Portlet.Mode.VIEW);
             } else {
                 if (titleBar != null) {
                     Portlet.Mode mode = titleBar.getPortletMode();
                     //System.err.println("setting mode in " + portletClass + " to " + mode.toString());
                     req.setMode(mode);
                 } else {
                     req.setMode(Portlet.Mode.VIEW);
                 }
             }
 
 
             // Set the portlet data
             PortletData data = null;
             if (!(user instanceof GuestUser)) {
                 try {
                     data = dataManager.getPortletData(req.getUser(), portletClass);
                     req.setAttribute(SportletProperties.PORTLET_DATA, data);
                 } catch (PersistenceManagerException e) {
                     errorFrame.setError("Unable to retrieve user's portlet data!", e);
                 }
             }
 
             try {
                 PortletInvoker.messageEvent(portletClass, msg, req, res);
             } catch (IOException ioex) {
                 errorFrame.setException(new PortletException("IO Exception occured:",ioex));
             }
             catch (PortletException e) {
                 errorFrame.setException(e);
             }
             String message = (String)req.getAttribute(SportletProperties.PORTLETERROR+portletClass);
             if (message != null) {
                 errorFrame.setMessage(message);
             }
 
         } else {
             super.messageEvent(concPortletID, msg, event);
         }
     }
 
 }
