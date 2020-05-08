 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.event.WindowEvent;
 import org.gridlab.gridsphere.event.impl.WindowEventImpl;
 import org.gridlab.gridsphere.layout.event.PortletComponentEvent;
 import org.gridlab.gridsphere.layout.event.PortletTitleBarEvent;
 import org.gridlab.gridsphere.layout.event.PortletTitleBarListener;
 import org.gridlab.gridsphere.layout.event.impl.PortletTitleBarEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.impl.StoredPortletResponseImpl;
 import org.gridlab.gridsphere.portletcontainer.*;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.File;
 import java.io.StringWriter;
 import java.util.*;
 
 /**
  * A <code>PortletTitleBar</code> represents the visual display of the portlet title bar
  * within a portlet frame and is contained by {@link PortletFrame}.
  * The title bar contains portlet mode and window state as well as a title.
  */
 public class PortletTitleBar extends BasePortletComponent implements Serializable, Cloneable {
 
     private String title = "unknown title";
     private String portletClass = null;
     private transient PortletWindow.State windowState = PortletWindow.State.NORMAL;
     private List supportedModes = new Vector();
     private transient Portlet.Mode portletMode = Portlet.Mode.VIEW;
     private transient Portlet.Mode previousMode = null;
     private List allowedWindowStates = new Vector();
     private String errorMessage = "";
     private boolean hasError = false;
     private boolean isActive = false;
     private StringBuffer prebufferedTitle = new StringBuffer();
     private StringBuffer postbufferedTitle = new StringBuffer();
 
 
     /**
      * Link is an abstract representation of a hyperlink with an href, image and
      * alt tags.
      */
     abstract class Link {
         protected String href = "";
         protected String imageSrc = "";
         protected String altTag = "";
 
         /**
          * Returns the image source attribute in the link
          *
          * @return the image source attribute in the link
          */
         public String getImageSrc() {
             return imageSrc;
         }
 
         /**
          * Sets the href attribute in the link
          *
          * @param href the href attribute in the link
          */
         public void setHref(String href) {
             this.href = href;
         }
 
         /**
          * Returns the href attribute in the link
          *
          * @return the href attribute in the link
          */
         public String getHref() {
             return href;
         }
 
         /**
          * Returns the alt tag attribute in the link
          *
          * @return the alt tag attribute in the link
          */
         public String getAltTag() {
             return altTag;
         }
 
         /**
          * Returns a string containing the image src, href and alt tag attributes
          * Used primarily for debugging purposes
          */
         public String toString() {
             StringBuffer sb = new StringBuffer("\n");
             sb.append("image src: " + imageSrc + "\n");
             sb.append("href: " + href + "\n");
             sb.append("alt tag: " + altTag + "\n");
             return sb.toString();
         }
     }
 
     /**
      * PortletModeLink is a concrete instance of a Link used for creating
      * portlet mode hyperlinks
      */
     class PortletModeLink extends Link {
 
         public static final String configImage = "images/window_configure.gif";
         public static final String editImage = "images/window_edit.gif";
         public static final String helpImage = "images/window_help.gif";
         public static final String viewImage = "images/window_view.gif";
 
         /**
          * Constructs an instance of PortletModeLink with the supplied portlet mode
          *
          * @param mode the portlet mode
          */
         public PortletModeLink(Portlet.Mode mode, Locale locale) throws IllegalArgumentException {
             if (mode == null) return;
 
             altTag = mode.getText(locale);
 
             // Set the image src
             if (mode.equals(Portlet.Mode.CONFIGURE)) {
                 imageSrc = configImage;
             } else if (mode.equals(Portlet.Mode.EDIT)) {
                 imageSrc = editImage;
             } else if (mode.equals(Portlet.Mode.HELP)) {
                 imageSrc = helpImage;
             } else if (mode.equals(Portlet.Mode.VIEW)) {
                 imageSrc = viewImage;
             } else {
                 throw new IllegalArgumentException("No matching Portlet.Mode found for received portlet mode: " + mode);
             }
         }
     }
 
     /**
      * PortletStateLink is a concrete instance of a Link used for creating
      * portlet window state hyperlinks
      */
     class PortletStateLink extends Link {
 
         public static final String minimizeImage = "images/window_minimize.gif";
         public static final String maximizeImage = "images/window_maximize.gif";
         public static final String resizeImage = "images/window_resize.gif";
 
         /**
          * Constructs an instance of PortletStateLink with the supplied window state
          *
          * @param state the window state
          */
         public PortletStateLink(PortletWindow.State state, Locale locale) throws IllegalArgumentException {
             if (state == null) return;
 
             altTag = state.getText(locale);
 
             // Set the image src
             if (state.equals(PortletWindow.State.MINIMIZED)) {
                 imageSrc = minimizeImage;
             } else if (state.equals(PortletWindow.State.MAXIMIZED)) {
                 imageSrc = maximizeImage;
             } else if (state.equals(PortletWindow.State.RESIZING)) {
                 imageSrc = resizeImage;
             } else {
                 throw new IllegalArgumentException("No matching PortletWindow.State found for received window mode: " + state);
             }
         }
     }
 
     /**
      * Constructs an instance of PortletTitleBar
      */
     public PortletTitleBar() {
     }
 
     /**
      * Sets the portlet class used to render the title bar
      *
      * @param portletClass the concrete portlet class
      */
     public void setPortletClass(String portletClass) {
         this.portletClass = portletClass;
     }
 
     /**
      * Returns the portlet class used in rendering the title bar
      *
      * @return the concrete portlet class
      */
     public String getPortletClass() {
         return portletClass;
     }
 
     public boolean isActive() {
         return isActive;
     }
 
     public void setActive(boolean isActive) {
         this.isActive = isActive;
     }
 
     /**
      * Returns the title of the portlet title bar
      *
      * @return the portlet title bar
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Sets the title of the portlet title bar
      *
      * @param title the portlet title bar
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * Sets the window state of this title bar
      *
      * @param state the portlet window state expressed as a string
      * @see PortletWindow.State
      */
     public void setWindowState(PortletWindow.State state) {
         if (state != null) this.windowState = state;
     }
 
     /**
      * Returns the window state of this title bar
      *
      * @return the portlet window state expressed as a string
      * @see PortletWindow.State
      */
     public PortletWindow.State getWindowState() {
         return windowState;
     }
 
     /**
      * Sets the window state of this title bar
      *
      * @param state the portlet window state expressed as a string
      * @see PortletWindow.State
      */
     public void setWindowStateAsString(String state) {
         if (state != null) {
             try {
                 this.windowState = PortletWindow.State.toState(state);
             } catch (IllegalArgumentException e) {
                 // do nothing
             }
         }
     }
 
     /**
      * Returns the window state of this title bar
      *
      * @return the portlet window state expressed as a string
      * @see PortletWindow.State
      */
     public String getWindowStateAsString() {
         return windowState.toString();
     }
 
     /**
      * Sets the portlet mode of this title bar
      *
      * @param mode the portlet mode expressed as a string
      * @see Portlet.Mode
      */
     public void setPortletMode(Portlet.Mode mode) {
         if (mode != null) this.portletMode = mode;
     }
 
     /**
      * Returns the portlet mode of this title bar
      *
      * @return the portlet mode expressed as a string
      * @see Portlet.Mode
      */
     public Portlet.Mode getPortletMode() {
         return portletMode;
     }
 
     /**
      * Sets the portlet mode of this title bar
      *
      * @param mode the portlet mode expressed as a string
      * @see Portlet.Mode
      */
     public void setPortletModeAsString(String mode) {
         if (mode == null) return;
         try {
             this.portletMode = Portlet.Mode.toMode(mode);
         } catch (IllegalArgumentException e) {
             // do nothing
         }
     }
 
     /**
      * Returns the portlet mode of this title bar
      *
      * @return the portlet mode expressed as a string
      * @see Portlet.Mode
      */
     public String getPortletModeAsString() {
         return portletMode.toString();
     }
 
     /**
      * Adds a title bar listener to be notified of title bar events
      *
      * @param listener a title bar listener
      * @see PortletTitleBarEvent
      */
     public void addTitleBarListener(PortletTitleBarListener listener) {
         listeners.add(listener);
     }
 
     /**
      * Indicates an error ocurred suring the processing of this title bar
      *
      * @return <code>true</code> if an error occured during rendering,
      * <code>false</code> otherwise
      */
     public boolean hasRenderError() {
         return hasError;
     }
 
     /**
      * Returns any errors associated with the functioning of this title bar
      *
      * @return any title bar errors that occured
      */
     public String getErrorMessage() {
         return errorMessage;
     }
 
     /**
      * Initializes the portlet title bar. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a list of component identifiers
      * @return a list of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(PortletRequest req, List list) {
         list = super.init(req, list);
         ComponentIdentifier compId = new ComponentIdentifier();
         compId.setPortletComponent(this);
         compId.setPortletClass(portletClass);
         compId.setComponentID(list.size());
         compId.setComponentLabel(label);
         compId.setClassName(this.getClass().getName());
         list.add(compId);
         doConfig();
         return list;
     }
 
     /**
      * Sets configuration information about the supported portlet modes,
      * allowed window states and title bar and web app name obtained from {@link PortletSettings}.
      * Information is queried from the {@link PortletRegistry}
      */
     protected void doConfig() {
         PortletRegistry registryManager = PortletRegistry.getInstance();
         String appID = registryManager.getApplicationPortletID(portletClass);
         ApplicationPortlet appPortlet = registryManager.getApplicationPortlet(appID);
         if (appPortlet != null) {
             ApplicationPortletConfig appConfig = appPortlet.getApplicationPortletConfig();
             if (appConfig != null) {
                 // get supported modes from application portlet config
                 supportedModes = sort(appConfig.getSupportedModes());
 
                 // get window states from application portlet config
                 allowedWindowStates = sort(appConfig.getAllowedWindowStates());
             }
         }
     }
 
     /**
      * Simple sorting algoritm that sorts in increasing order a <code>List</code>
      * containing objects that implement <code>Comparator</code>
      * @param list a <code>List</code> to be sorted
      * @return the sorted list
      */
     private List sort(List list) {
         int n = list.size();
         for (int i=0; i < n-1; i++) {
             for (int j=0; j < n-1-i; j++) {
                 Comparator c = (Comparator)list.get(j);
                 Comparator d = (Comparator)list.get(j+1);
                 if (c.compare(c, d) == 1) {
                     Object tmp = list.get(j);
                     list.set(j, d);
                     list.set(j+1, tmp);
                 }
             }
         }
         return list;
     }
 
     /**
      * Creates the portlet window state hyperlinks displayed in the title bar
      *
      * @param event the gridsphere event
      * @return a list of window state hyperlinks
      */
     protected List createWindowLinks(GridSphereEvent event) {
         PortletURI portletURI;
         PortletResponse res = event.getPortletResponse();
         PortletWindow.State tmp;
 
         if (allowedWindowStates.isEmpty()) return null;
 
         //String[] windowStates = new String[allowedWindowStates.size()];
         List windowStates = new ArrayList();
         for (int i = 0; i < allowedWindowStates.size(); i++) {
             tmp = (PortletWindow.State)allowedWindowStates.get(i);
             windowStates.add(tmp);
             // remove current state from list
             if (tmp.equals(windowState)) {
                 windowStates.remove(i);
             }
         }
 
         // get rid of resized if window state is normal
         if (windowState.equals(PortletWindow.State.NORMAL)) {
             windowStates.remove(PortletWindow.State.RESIZING);
         }
 
         // Localize the window state names
         PortletRequest req = event.getPortletRequest();
 
         Locale locale = req.getLocale();
 
         // create a URI for each of the window states
         PortletStateLink stateLink;
         List stateLinks = new Vector();
         for (int i = 0; i < windowStates.size(); i++) {
             tmp = (PortletWindow.State)windowStates.get(i);
             portletURI = res.createURI();
             portletURI.addParameter(SportletProperties.COMPONENT_ID, this.componentIDStr);
             //portletURI.addParameter(SportletProperties.PORTLETID, portletClass);
             try {
                 stateLink = new PortletStateLink(tmp, locale);
                 portletURI.addParameter(SportletProperties.PORTLET_WINDOW, tmp.toString());
                 stateLink.setHref(portletURI.toString());
                 stateLinks.add(stateLink);
             } catch (IllegalArgumentException e) {
                 // do nothing
             }
 
         }
         return stateLinks;
     }
 
     /**
      * Creates the portlet mode hyperlinks displayed in the title bar
      *
      * @param event the gridsphere event
      * @return a list of portlet mode hyperlinks
      */
     public List createModeLinks(GridSphereEvent event) {
         int i;
         PortletResponse res = event.getPortletResponse();
         PortletRequest req = event.getPortletRequest();
         // make modes from supported modes
         if (supportedModes.isEmpty()) return null;
 
 
         // Unless user is a super they should not see configure mode
         boolean hasConfigurePermission = false;
         PortletRole role = req.getRole();
         if (role.isAdmin() || role.isSuper()) {
             hasConfigurePermission = true;
         }
         List smodes = new ArrayList();
         Portlet.Mode mode;
         for (i = 0; i < supportedModes.size(); i++) {
             mode = (Portlet.Mode)supportedModes.get(i);
             if (mode.equals(Portlet.Mode.CONFIGURE)) {
                 if (hasConfigurePermission) {
                     smodes.add(mode);
                 }
             } else {
                 smodes.add(mode);
             }
 
             // remove current mode from list
             smodes.remove(portletMode);
         }
 
          // Localize the portlet mode names
         Locale locale = req.getLocale();
 
         List portletLinks = new ArrayList();
         for (i = 0; i < smodes.size(); i++) {
             // create a URI for each of the portlet modes
             PortletURI portletURI;
             PortletModeLink modeLink;
             mode = (Portlet.Mode)smodes.get(i);
             portletURI = res.createURI();
             portletURI.addParameter(SportletProperties.COMPONENT_ID, this.componentIDStr);
             //portletURI.addParameter(SportletProperties.PORTLETID, portletClass);
             try {
                 modeLink = new PortletModeLink(mode, locale);
                 portletURI.addParameter(SportletProperties.PORTLET_MODE, mode.toString());
                 modeLink.setHref(portletURI.toString());
                 portletLinks.add(modeLink);
             } catch (IllegalArgumentException e) {
                 //log.debug("Unable to get mode for : " + mode.toString());
             }
 
         }
 
         return portletLinks;
     }
 
     /**
      * Performs an action on this portlet title bar component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.actionPerformed(event);
         isActive = true;
 
         PortletRequest req = event.getPortletRequest();
         req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
         PortletComponentEvent lastEvent = event.getLastRenderEvent();
 
         PortletTitleBarEvent titleBarEvent = new PortletTitleBarEventImpl(this, event, COMPONENT_ID);
 
         User user = req.getUser();
         if (!(user instanceof GuestUser)) {
             if (titleBarEvent.hasAction()) {
             if (titleBarEvent.getAction().getID() == PortletTitleBarEvent.TitleBarAction.WINDOW_MODIFY.getID()) {
                 PortletResponse res = event.getPortletResponse();
                 windowState = titleBarEvent.getState();
                 WindowEvent winEvent = null;
 
                 if (windowState == PortletWindow.State.MAXIMIZED) {
                     winEvent = new WindowEventImpl(req, WindowEvent.WINDOW_MAXIMIZED);
                 } else if (windowState == PortletWindow.State.MINIMIZED) {
                     winEvent = new WindowEventImpl(req, WindowEvent.WINDOW_MINIMIZED);
                 } else if (windowState == PortletWindow.State.RESIZING) {
                     winEvent = new WindowEventImpl(req, WindowEvent.WINDOW_RESTORED);
                 }
                 if (winEvent != null) {
                     try {
                         PortletInvoker.windowEvent(portletClass, winEvent, req, res);
                     } catch (PortletException e) {
                         hasError = true;
                         errorMessage += "Failed to invoke window event method of portlet: " + portletClass;
                     }
                 }
             } else if (titleBarEvent.getAction().getID() == PortletTitleBarEvent.TitleBarAction.MODE_MODIFY.getID()) {
                 previousMode = portletMode;
                 portletMode = titleBarEvent.getMode();
 
             }
             }
         }
 
         req.setAttribute(SportletProperties.PORTLET_WINDOW, windowState);
         req.setMode(portletMode);
         req.setAttribute(SportletProperties.PREVIOUS_MODE, previousMode);
 
         Iterator it = listeners.iterator();
         PortletComponent comp;
         while (it.hasNext()) {
             comp = (PortletComponent) it.next();
             event.addNewRenderEvent(titleBarEvent);
             comp.actionPerformed(event);
         }
 
         //if (evt != null) fireTitleBarEvent(evt);
     }
 
     /**
      * Fires a title bar event notification
      *
      * @param event a portlet title bar event
      * @throws PortletLayoutException if a layout error occurs
      */
     protected void fireTitleBarEvent(PortletTitleBarEvent event) throws PortletLayoutException {
         Iterator it = listeners.iterator();
         PortletTitleBarListener l;
         while (it.hasNext()) {
             l = (PortletTitleBarListener) it.next();
             l.handleTitleBarEvent(event);
         }
     }
 
     /**
      * Renders the portlet title bar component
      *
      * @param event a gridsphere event
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
 
         hasError = false;
 
         // title bar: configure, edit, help, title, min, max
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
 
         // get the appropriate title for this client
         Client client = req.getClient();
         Locale locale = req.getLocale();
 
         /*
         if (settings != null) {
             title = settings.getTitle(locale, client);
         }
         */
 
         List modeLinks = null, windowLinks = null;
         User user = req.getUser();
         if (!(user instanceof GuestUser)) {
             if (portletClass != null) {
                 modeLinks = createModeLinks(event);
                 windowLinks = createWindowLinks(event);
             }
         }
 
         req.setMode(portletMode);
         req.setAttribute(SportletProperties.PREVIOUS_MODE, previousMode);
         req.setAttribute(SportletProperties.PORTLET_WINDOW, windowState);
 
         // TODO try to cache portlet's rendering---
         StringWriter storedWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(storedWriter);
         PortletResponse wrappedResponse = new StoredPortletResponseImpl(res, writer);
 
         if (isActive) {
             writer.println("<tr><td class=\"window-title-active\">");
         } else {
             writer.println("<tr><td class=\"window-title-inactive\">");
         }
         isActive = false;
         writer.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
 
         // Output portlet mode icons
         if (modeLinks != null) {
             Iterator modesIt = modeLinks.iterator();
             writer.println("<td class=\"window-icon-left\">");
             PortletModeLink mode;
             while (modesIt.hasNext()) {
                 mode = (PortletModeLink) modesIt.next();
                 writer.println("<a href=\"" + mode.getHref() + "\"><img border=\"0\" src=\"themes" + File.separator + theme + File.separator + mode.getImageSrc() + "\" title=\"" + mode.getAltTag() + "\"/></a>");
             }
             writer.println("</td>");
         }
 
         // Invoke doTitle of portlet whose action was perfomed
         //String actionStr = req.getParameter(SportletProperties.DEFAULT_PORTLET_ACTION);
         writer.println("<td class=\"window-title-name\">");
 
         prebufferedTitle = storedWriter.getBuffer();
 
         storedWriter = new StringWriter();
         writer = new PrintWriter(storedWriter);
         wrappedResponse = new StoredPortletResponseImpl(res, writer);
 
         try {
             //System.err.println("invoking  doTitle:" + title);
             PortletInvoker.doTitle(portletClass, req, wrappedResponse);
             //out.println(" (" + portletMode.toString() + ") ");
             title = storedWriter.toString();
         } catch (PortletException e) {
             String pname = portletClass.substring(0, portletClass.length() - 1);
             pname = pname.substring(0, pname.lastIndexOf("."));
             pname = pname.substring(pname.lastIndexOf(".")+1);
             ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
             String value = bundle.getString("PORTLET_UNAVAILABLE");
            title = value + " : " + pname;
            //out.println(title);
            errorMessage = portletClass + " is currently unavailable!\n";
             hasError = true;
         }
 
         storedWriter = new StringWriter();
         writer = new PrintWriter(storedWriter);
 
         writer.println("</td>");
 
         // Output window state icons
         if (windowLinks != null) {
             Iterator windowsIt = windowLinks.iterator();
             PortletStateLink state;
             writer.println("<td class=\"window-icon-right\">");
             while (windowsIt.hasNext()) {
                 state = (PortletStateLink) windowsIt.next();
                 writer.println("<a href=\"" + state.getHref() + "\"><img border=\"0\" src=\"themes/" + theme + File.separator + state.getImageSrc() + "\" title=\"" + state.getAltTag() + "\"/></a>");
             }
             writer.println("</td>");
         }
         writer.println("</tr></table>");
         writer.println("</td></tr>");
 
         postbufferedTitle = storedWriter.getBuffer();
     }
 
     public StringBuffer getPreBufferedTitle() {
         return prebufferedTitle;
     }
 
     public StringBuffer getPostBufferedTitle() {
         return postbufferedTitle;
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletTitleBar t = (PortletTitleBar)super.clone();
         t.title = this.title;
         t.portletClass = this.portletClass;
         t.portletMode = Portlet.Mode.toMode(this.portletMode.toString());
         t.windowState = PortletWindow.State.toState(this.windowState.toString());
         t.previousMode = this.previousMode;
         t.errorMessage = this.errorMessage;
         t.hasError = this.hasError;
         // don't clone settings for now
         //t.settings = this.settings;
         t.supportedModes = new ArrayList(this.supportedModes.size());
         for (int i = 0; i < this.supportedModes.size(); i++) {
             Portlet.Mode mode = (Portlet.Mode)supportedModes.get(i);
             t.supportedModes.add(mode.clone());
         }
         t.allowedWindowStates = new ArrayList(this.allowedWindowStates.size());
         for (int i = 0; i < this.allowedWindowStates.size(); i++) {
             PortletWindow.State state = (PortletWindow.State)allowedWindowStates.get(i);
             t.allowedWindowStates.add(state.clone());
         }
         return t;
 
     }
 }
