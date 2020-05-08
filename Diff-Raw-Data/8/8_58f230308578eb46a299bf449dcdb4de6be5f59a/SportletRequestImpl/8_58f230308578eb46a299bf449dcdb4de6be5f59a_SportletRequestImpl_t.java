 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import java.util.*;
 
 
 /**
  * A <code>SportletRequestImpl</code> provides an implementation of the
  * <code>PortletRequest</code> by following the decorator patterm.
  * A HttpServletRequest object is used in composition to perform most of
  * the required methods.
  */
 public class SportletRequestImpl extends HttpServletRequestWrapper implements SportletRequest {
 
     private static PortletLog log = SportletLog.getInstance(SportletRequest.class);
 
     /**
      * Constructor creates a proxy for a HttpServletRequest
      * All PortletRequest objects come from request or session attributes
      *
      * @param req the HttpServletRequest
      */
     public SportletRequestImpl(HttpServletRequest req) {
         super(req);
     }
 
     /**
      * Returns an object representing the client device that the user connects to the portal with.
      *
      * @return the client device
      */
     public Client getClient() {
         Client client = (Client)this.getHttpServletRequest().getSession().getAttribute(SportletProperties.CLIENT);
         if (client == null) {
             client = new ClientImpl(this.getHttpServletRequest());
             this.getHttpServletRequest().getSession().setAttribute(SportletProperties.CLIENT, client);
         }
         return client;
     }
 
     /**
      * Sets the client device that the user connects to the portal with.
      *
      * @param client the client device
      */
     public void setClient(Client client) {
         this.getHttpServletRequest().getSession().setAttribute(SportletProperties.CLIENT, client);
     }
 
     /**
      * Returns whether this this.getHttpServletRequest()uest was made using a secure channel, such as HTTPS.
      *
      * @return true if channel is secure, false otherwise
      */
     public boolean isSecure() {
         return this.getHttpServletRequest().isSecure();
     }
 
     /**
      * Returns the current session or, if there is no current session, it creates one and returns it.
      *
      * @return the portlet session
      */
     public PortletSession getPortletSession() {
         return new SportletSession(this.getHttpServletRequest().getSession(true));
     }
 
     /**
      * Returns the current session or, if there is no current session and the given flag is true,
      * it creates one and returns it.
      *
      * If the given flag is false and there is no current portlet session, this method returns null.
      *
      * @param create true to create a new session, false to return null if there is no current session
      * @return the portlet session
      */
     public PortletSession getPortletSession(boolean create) {
         if ((this.getHttpServletRequest().getSession() == null) && (create == false)) {
             return null;
         }
         return new SportletSession(this.getHttpServletRequest().getSession(true));
     }
 
     /**
      * Returns an array containing all of the Cookie  objects the client sent with this this.getHttpServletRequest()uest. This method returns null if no cookies were sent.
      *
      * @return an array of all the Cookies  included with this this.getHttpServletRequest()uest, or null  if the this.getHttpServletRequest()uest has no cookies
      */
     public Cookie[] getCookies() {
         return this.getHttpServletRequest().getCookies();
     }
 
     /**
      * Clears all of the this.getHttpServletRequest()uest attributes associated with this this.getHttpServletRequest()uest
      */
     public void invalidate() {
         // clear this.getHttpServletRequest()uest attributes
         Enumeration attrnames = this.getHttpServletRequest().getAttributeNames();
         while (attrnames.hasMoreElements()) {
             String name = (String)attrnames.nextElement();
             this.getHttpServletRequest().setAttribute(name, null);
         }
     }
 
     /**
      * Returns the data of the concrete portlet instance
      * If the portlet is run in CONFIGURE mode, the portlet data is not accessible and this method will return null.
      *
      * @return the PortletData
      */
     public PortletData getData() {
         if (getMode() == Portlet.Mode.CONFIGURE) {
             return null;
         }
         return (PortletData) this.getHttpServletRequest().getAttribute(SportletProperties.PORTLET_DATA);
     }
 
     /**
      * Sets the data of the concrete portlet instance
      * If the portlet is run in CONFIGURE mode, the portlet data is not accessible and this method will return null.
      *
      * @param data the portlet data
      */
     public void setData(PortletData data) {
         if (getMode() != Portlet.Mode.EDIT) return;
         this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_DATA, data);
     }
 
     /**
      * Returns the user object. The user object contains useful information about the user and his or her preferences.
      * If the user has not logged in or does not grant access to the portlet, this method creates a GuestUser
      *
      * @see GuestUser
      *
      * @return the User object
      */
     public User getUser() {
         User user = (User) this.getHttpServletRequest().getAttribute(SportletProperties.PORTLET_USER);
         if (user == null) {
             user = GuestUser.getInstance();
             this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_USER, user);
         }
         return user;
     }
 
     public PortletRole getRole() {
         PortletRole role = (PortletRole)this.getHttpServletRequest().getAttribute(SportletProperties.PORTLET_ROLE);
         if (role == null) {
             return PortletRole.GUEST;
         }
         return role;
     }
 
 
     public void setRole(PortletRole role) {
         this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_ROLE, role);
     }
 
     /**
      * Returns the PortletGroup objects representing the users group membership
      *
      * @param groups an array of PortletGroup objects.
      *
      * @see PortletGroup
      */
     public void setGroup(List groups) {
         this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_GROUP, groups);
     }
 
     /**
      * Returns the locale of the preferred language. The preference is based on the user's
      * choice of language(s) and/or the client's Accept-Language header.
      *
      * If more than one language is preferred, the locale returned by this
      * method is the one with the highest preference.
      *
      * @return the locale of the preferred language
      */
     public Locale getLocale() {
         Locale locale = null;
        locale = (Locale)this.getPortletSession(true).getAttribute(User.LOCALE);
        if (locale != null) return locale;
         User user = getUser();
         if (!(user instanceof GuestUser)) {
             String loc = (String)user.getAttribute(User.LOCALE);
            if (loc != null) {
                 locale = new Locale(loc, "", "");
                 this.getPortletSession(true).setAttribute(User.LOCALE, locale);
                 return locale;
             }
         }
         locale = this.getHttpServletRequest().getLocale();
         if (locale != null) return locale;
         return Locale.ENGLISH;
     }
 
     /**
      * Returns an Enumeration of Locale objects indicating, in decreasing order starting
      * with the preferred locale, the locales that are acceptable to the client based on
      * the Accept-Language header. If the client this.getHttpServletRequest()uest doesn't provide an Accept-Language
      * header, this method returns an Enumeration containing one Locale, the default
      * locale for the server.
      *
      * @return an Enumeration of Locale objects
      */
     public Enumeration getLocales() {
         return this.getHttpServletRequest().getLocales();
     }
 
     /**
      * Returns the PortletSettings object of the concrete portlet.
      *
      * @return the portlet settings
      */
     public PortletSettings getPortletSettings() {
         return (PortletSettings) this.getHttpServletRequest().getAttribute(SportletProperties.PORTLET_SETTINGS);
     }
 
     /**
      * Sets the PortletSettings object of the concrete portlet.
      *
      * @param settings the portlet settings
      */
     public void setPortletSettings(PortletSettings settings) {
         this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_SETTINGS, settings);
     }
 
 
     /**
      * Returns the mode that the portlet was running at last, or Portlet.Mode.VIEW if no previous mode exists.
      *
      * @return the previous portlet mode
      */
     public Portlet.Mode getPreviousMode() {
         Portlet.Mode prev = (Portlet.Mode) this.getHttpServletRequest().getAttribute(SportletProperties.PREVIOUS_MODE);
         if (prev == null) prev = Portlet.Mode.VIEW;
         return prev;
     }
 
     /**
      * Sets the previous portlet mode.
      *
      * @param previousMode the previous portlet mode
      */
     public void setPreviousMode(Portlet.Mode previousMode) {
         this.getHttpServletRequest().setAttribute(SportletProperties.PREVIOUS_MODE, previousMode);
     }
 
     /**
      * Returns the window that the portlet is running in.
      *
      * @return the portlet window
      */
     public PortletWindow.State getWindowState() {
         return (PortletWindow.State) this.getHttpServletRequest().getAttribute(SportletProperties.PORTLET_WINDOW);
     }
 
     /**
      * Returns the window state that the portlet is running in.
      *
      * @param state the portlet window state
      */
     public void setWindowState(PortletWindow.State state) {
         this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_WINDOW, state);
     }
 
     /**
      * Returns the mode that the portlet is running in.
      *
      * @return the portlet mode
      */
     public Portlet.Mode getMode() {
         return (Portlet.Mode) this.getHttpServletRequest().getAttribute(SportletProperties.PORTLET_MODE);
     }
 
     /**
      * Sets the mode that the portlet is running in.
      *
      * @param mode the portlet mode
      */
     public void setMode(Portlet.Mode mode) {
         this.getHttpServletRequest().setAttribute(SportletProperties.PORTLET_MODE, mode);
     }
 
     public void invalidateCache() {
         // XXX: FILL ME IN
     }
 
     // Primarily used for debugging
     public void logRequest() {
         String name, paramvalue, headervals;
         Object attrvalue;
         Enumeration enum, eenum;
 
         log.debug("PortletRequest Information");
         log.debug("\tthis.getHttpServletRequest()uest headers: ");
 
         enum = getHeaderNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             eenum = (Enumeration) getHeaders(name);
             headervals = "";
             while (eenum.hasMoreElements()) {
                 headervals += " " + (String) eenum.nextElement();
             }
             log.debug("\t\tname=" + name + " values=" + headervals);
         }
         log.debug("\tcontent type: " + getContentType());
         if (getCookies() != null)
             log.debug("\tcookies are present");
         log.debug("\tthis.getHttpServletRequest()uest method: " + getMethod());
         log.debug("\tremote host: " + getRemoteHost() + " " + getRemoteAddr());
         log.debug("\tthis.getHttpServletRequest()uest scheme: " + getScheme());
         log.debug("\tthis.getHttpServletRequest()uest attribute names: ");
         enum = getAttributeNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             attrvalue = (Object) getAttribute(name);
             log.debug("\t\tname=" + name + " object type=" + attrvalue.getClass().getName());
         }
         log.debug("\tthis.getHttpServletRequest()uest parameter names: note if a parameter has multiple values, only the first beans is displayed ");
         enum = getParameterNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             paramvalue = getParameter(name);
             log.debug("\t\tname=" + name + " value=" + paramvalue);
         }
         log.debug("\tthis.getHttpServletRequest()uest parameter info: ");
         log.debug("\tthis.getHttpServletRequest()uest path info: " + this.getHttpServletRequest().getPathInfo());
     }
 
     private HttpServletRequest getHttpServletRequest() {
         return (HttpServletRequest)super.getRequest();
     }
 }
