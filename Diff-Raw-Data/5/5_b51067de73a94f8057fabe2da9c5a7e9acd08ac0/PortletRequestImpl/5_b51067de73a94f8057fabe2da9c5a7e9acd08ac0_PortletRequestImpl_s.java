 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.jsrimpl;
 
 import org.gridlab.gridsphere.portlet.Portlet;
 import org.gridlab.gridsphere.portlet.PortletWindow;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.Supports;
 
 import javax.portlet.*;
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpSession;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.*;
 
 
 /**
  * The <CODE>PortletRequest</CODE> defines the base interface to provide client
  * request information to a portlet. The portlet container uses two specialized
  * versions of this interface when invoking a portlet, <CODE>ActionRequest</CODE>
  * and <CODE>RenderRequest</CODE>. The portlet container creates these objects and
  * passes them as  arguments to the portlet's <CODE>processAction</CODE> and
  * <CODE>render</CODE> methods.
  *
  * @see ActionRequest
  * @see RenderRequest
  */
 public abstract class PortletRequestImpl extends HttpServletRequestWrapper implements PortletRequest {
 
     protected PortletContext portletContext = null;
     protected PortalContext portalContext = null;
     protected Supports[] supports = null;
     protected String contextPath = "/";
     protected boolean hasReader = false;
     protected boolean included = false;
 
     private PortletSession portletSession = null;
 
     protected Map props = null;
 
     //protected List modesAllowed = null;
 
     protected GridSphereParameters portalParameters = null;
 
     //protected boolean included = true;
 
     /**
      * Constructor creates a proxy for a HttpServletRequest
      * All PortletRequest objects come from request or session attributes
      *
      * @param req the HttpServletRequest
      */
     public PortletRequestImpl(HttpServletRequest req, PortalContext portalContext, PortletContext portletContext) {
         super(req);
         this.portalContext = portalContext;
         this.portletContext = portletContext;
         contextPath = this.portletContext.getRealPath("");
         int l = contextPath.lastIndexOf(File.separator);
         contextPath = contextPath.substring(l);
         //this.supports = supports;
         props = new HashMap();
         /*
         modesAllowed = new ArrayList();
         for (int i = 0; i < supports.length; i++) {
             Supports s = (Supports) supports[i];
             org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode[] modes = (org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode[]) s.getPortletMode();
             for (int j = 0; j < modes.length; j++) {
                 org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode m = modes[j];
                 modesAllowed.add(m.getContent());
             }
         }
         /*
         Enumeration modesEnum = portalContext.getSupportedPortletModes();
         while (modesEnum.hasMoreElements()) {
             PortletMode m = (PortletMode)modesEnum.nextElement();
             modesAllowed.add(m.toString());
         }
         */
         //if (!modesAllowed.contains(PortletMode.VIEW.toString())) modesAllowed.add(PortletMode.VIEW.toString());
 
 
 
         portalParameters = new GridSphereParameters(req);
 
         /*
         System.err.println("============================= PortletRequestImpl =====================================");
         if (getAttribute(SportletProperties.PORTLET_ACTION_METHOD) != null) {
             System.err.println("in action");
         } else {
             System.err.println("in render");
         }
         System.err.println("query string=" + super.getQueryString());
 
         System.err.println("Actual HTTP parameters");
         for (Enumeration parameters = super.getParameterNames(); parameters.hasMoreElements();) {
             String   paramName   = (String)parameters.nextElement();
             String[] paramValues = (String[])super.getParameterValues(paramName);
             System.err.println("\nname=" + paramName + "\nvalues=");
             for (int i = 0; i < paramValues.length; i++) {
                 System.err.print("  " + paramValues[i]);
             }
         }
 
         System.err.println("\n\nPortlet parameters for portlet " );
         for (Enumeration parameters = getParameterNames(); parameters.hasMoreElements();) {
             String   paramName   = (String)parameters.nextElement();
             String[] paramValues = (String[])getParameterValues(paramName);
             System.err.println("\nname=" + paramName + "\nvalues=");
             for (int i = 0; i < paramValues.length; i++) {
                 System.err.print("  " + paramValues[i]);
             }
         }
 
         System.err.println("\n===================================================================");
         */
     }
 
 
     public void setIncluded(boolean included) {
         this.included = included;
     }
 
     public boolean isIncluded() {
         return included;
     }
 
     /**
      * Is this attribute name a reserved name (by the J2EE spec)?.
      * Reserved names begin with "java." or "javax.".
      */
     private boolean isNameReserved(String name) {
         return name.startsWith("java.") || name.startsWith("javax.");
     }
 
     /**
      * Returns true, if the given window state is valid
      * to be set for this portlet in the context
      * of the current request.
      *
      * @param state window state to checked
      * @return true, if it is valid for this portlet
      *         in this request to change to the
      *         given window state
      */
     public boolean isWindowStateAllowed(WindowState state) {
         Enumeration statesEnum = portalContext.getSupportedWindowStates();
         while (statesEnum.hasMoreElements()) {
             WindowState s = (WindowState) statesEnum.nextElement();
             if (s.equals(state)) return true;
         }
         return false;
     }
 
     /**
      * Returns true, if the given portlet mode is a valid
      * one to set for this portlet  in the context
      * of the current request.
      *
      * @param mode portlet mode to check
      * @return true, if it is valid for this portlet
      *         in this request to change to the
      *         given portlet mode
      */
     public boolean isPortletModeAllowed(PortletMode mode) {
         List modesAllowed = (List)this.getHttpServletRequest().getAttribute(SportletProperties.ALLOWED_MODES);
         if (modesAllowed.contains(mode.toString())) return true;
         /*
         Enumeration modesEnum = portalContext.getSupportedPortletModes();
         while (modesEnum.hasMoreElements()) {
             PortletMode m = (PortletMode)modesEnum.nextElement();
             if (m.equals(mode)) {
                 System.err.println("mode " + mode.toString() + " IS allowed");
                 return true;
             }
         }
         System.err.println("mode " + mode.toString() + " not allowed");
         */
         return false;
     }
 
     /**
      * Returns the current portlet mode of the portlet.
      *
      * @return the portlet mode
      */
     public PortletMode getPortletMode() {
         Portlet.Mode mode = (Portlet.Mode) getAttribute(SportletProperties.PORTLET_MODE);
         PortletMode m = PortletMode.VIEW;
         if (mode == Portlet.Mode.VIEW) {
             m = PortletMode.VIEW;
         } else if (mode == Portlet.Mode.EDIT) {
             m = PortletMode.EDIT;
         } else if (mode == Portlet.Mode.HELP) {
             m = PortletMode.HELP;
         } else if (mode == Portlet.Mode.CONFIGURE) {
             m = new PortletMode("config");
         } else {
             m = new PortletMode(mode.toString());
         }
         return m;
     }
 
     /**
      * Returns the current window state of the portlet.
      *
      * @return the window state
      */
     public WindowState getWindowState() {
         PortletWindow.State state = (PortletWindow.State) getAttribute(SportletProperties.PORTLET_WINDOW);
         WindowState ws = WindowState.NORMAL;
         if (state == PortletWindow.State.MAXIMIZED) {
             ws = WindowState.MAXIMIZED;
         } else if (state == PortletWindow.State.MINIMIZED) {
             ws = WindowState.MINIMIZED;
         } else if (state == PortletWindow.State.NORMAL) {
             ws = WindowState.NORMAL;
         }
         return ws;
     }
 
     /**
      * Returns the preferences object associated with the portlet.
      *
      * @return the portlet preferences
      */
     public PortletPreferences getPreferences() {
         return (PortletPreferences) getAttribute(SportletProperties.PORTLET_PREFERENCES);
     }
 
     /**
      * Returns the current portlet session or, if there is no current session,
      * creates one and returns the new session.
      * <p/>
      * Creating a new portlet session will result in creating
      * a new <code>HttpSession</code> on which the portlet session is based on.
      *
      * @return the portlet session
      */
     public PortletSession getPortletSession() {
         return getPortletSession(true);
         //return new PortletSessionImpl(this.getHttpServletRequest().getSession(true), portletContext);
     }
 
     /**
      * Returns the current portlet session or, if there is no current session
      * and the given flag is <CODE>true</CODE>, creates one and returns
      * the new session.
      * <P>
      * If the given flag is <CODE>false</CODE> and there is no current
      * portlet session, this method returns <CODE>null</CODE>.
      * <p/>
      * Creating a new portlet session will result in creating
      * a new <code>HttpSession</code> on which the portlet session is based on.
      *
      * @param create <CODE>true</CODE> to create a new session, <BR>
      *               <CODE>false</CODE> to return <CODE>null</CODE> if there
      *               is no current session
      * @return the portlet session
      */
     public PortletSession getPortletSession(boolean create) {
         /*
          if ((this.getHttpServletRequest().getSession() == null) && (create == false)) {
              return null;
          }
          return new PortletSessionImpl(this.getHttpServletRequest().getSession(true), portletContext);
          */
         // check if the session was invalidated
         HttpSession httpSession = this.getHttpServletRequest().getSession(false);
 
         if ((portletSession != null) && (httpSession == null)) {
             portletSession = null;
         } else if (httpSession != null) {
             create = true;
         }
 
         if (create && portletSession == null) {
             httpSession = this.getHttpServletRequest().getSession(create);
             if (httpSession != null) {
                 portletSession = new PortletSessionImpl(this.getHttpServletRequest(), this.getHttpServletRequest().getSession(true), portletContext);
             }
         }
 
         return portletSession;
     }
 
     /**
      * Returns the value of the specified request property
      * as a <code>String</code>. If the request did not include a property
      * of the specified name, this method returns <code>null</code>.
      * <p/>
      * A portlet can access portal/portlet-container specific properties
      * through this method and, if available, the
      * headers of the HTTP client request.
      * <p/>
      * This method should only be used if the
      * property has only one value. If the property might have
      * more than one value, use {@link #getProperties}.
      * <p/>
      * If this method is used with a multivalued
      * parameter, the value returned is equal to the first value
      * in the Enumeration returned by <code>getProperties</code>.
      *
      * @param name a <code>String</code> specifying the
      *             property name
      * @throws IllegalArgumentException if name is <code>null</code>.
      * @return			a <code>String</code> containing the
      * value of the requested
      * property, or <code>null</code>
      * if the request does not
      * have a property of that name.
      */
     public String getProperty(String name) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         Object o = props.get(name);
         if (o instanceof String) {
             return (String) o;
         } else if (o instanceof List) {
             List l = (List) o;
             if (!l.isEmpty()) {
                 return (String) l.get(0);
             }
         }
         return null;
     }
 
     /**
      * Returns all the values of the specified request property
      * as a <code>Enumeration</code> of <code>String</code> objects.
      * <p/>
      * If the request did not include any propertys
      * of the specified name, this method returns an empty
      * <code>Enumeration</code>.
      * The property name is case insensitive. You can use
      * this method with any request property.
      *
      * @param name a <code>String</code> specifying the
      *             property name
      * @throws IllegalArgumentException if name is <code>null</code>.
      * @return		a <code>Enumeration</code> containing
      * the values of the requested property. If
      * the request does not have any properties of
      * that name return an empty <code>Enumeration</code>.
      */
     public java.util.Enumeration getProperties(String name) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         Object o = props.get(name);
         if (o instanceof List) {
             List l = (List) o;
             return new Enumerator(l.iterator());
         }
         return null;
     }
 
     /**
      * Returns a <code>Enumeration</code> of all the property names
      * this request contains. If the request has no
      * properties, this method returns an empty <code>Enumeration</code>.
      *
      * @return			an <code>Enumeration</code> of all the
      * property names sent with this
      * request; if the request has
      * no properties, an empty <code>Enumeration</code>.
      */
     public java.util.Enumeration getPropertyNames() {
         return new Enumerator(props.keySet().iterator());
     }
 
     /**
      * Returns the context of the calling portal.
      *
      * @return the context of the calling portal
      */
     public PortalContext getPortalContext() {
         return portalContext;
     }
 
     /**
      * Returns the name of the authentication scheme used for the
      * connection between client and portal,
      * for example, <code>BASIC_AUTH</code>, <code>CLIENT_CERT_AUTH</code>,
      * a custom one or <code>null</code> if there was no authentication.
      *
      * @return		one of the static members <code>BASIC_AUTH</code>,
      * <code>FORM_AUTH</code>, <code>CLIENT_CERT_AUTH</code>,
      * <code>DIGEST_AUTH</code> (suitable for == comparison)
      * indicating the authentication scheme,
      * a custom one, or
      * <code>null</code> if the request was
      * not authenticated.
      */
     public String getAuthType() {
         return this.getHttpServletRequest().getAuthType();
     }
 
     /**
      * Returns the context path which is the path prefix associated with the deployed
      * portlet application. If the portlet application is rooted at the
      * base of the web server URL namespace (also known as "default" context),
      * this path must be an empty string. Otherwise, it must be the path the
      * portlet application is rooted to, the path must start with a '/' and
      * it must not end with a '/' character.
      * <p/>
      * To encode a URL the {@link PortletResponse#encodeURL} method must be used.
      *
      * @return		a <code>String</code> specifying the
      * portion of the request URL that indicates the context
      * of the request
      * @see PortletResponse#encodeURL
      */
     public String getContextPath() {
         return contextPath;
     }
 
     /**
      * Returns the login of the user making this request, if the user
      * has been authenticated, or null if the user has not been authenticated.
      *
      * @return		a <code>String</code> specifying the login
      * of the user making this request, or <code>null</code>
      * if the user login is not known.
      */
     public String getRemoteUser() {
         return this.getHttpServletRequest().getRemoteUser();
     }
 
     /**
      * Returns a java.security.Principal object containing the name of the
      * current authenticated user.
      *
      * @return		a <code>java.security.Principal</code> containing
      * the name of the user making this request, or
      * <code>null</code> if the user has not been
      * authenticated.
      */
     public java.security.Principal getUserPrincipal() {
         return this.getHttpServletRequest().getUserPrincipal();
     }
 
     /**
      * Returns a boolean indicating whether the authenticated user is
      * included in the specified logical "role".  Roles and role membership can be
      * defined using deployment descriptors.  If the user has not been
      * authenticated, the method returns <code>false</code>.
      *
      * @param role a <code>String</code> specifying the name
      *             of the role
      * @return		a <code>boolean</code> indicating whether
      * the user making this request belongs to a given role;
      * <code>false</code> if the user has not been
      * authenticated.
      */
     public boolean isUserInRole(String role) {
         return this.getHttpServletRequest().isUserInRole(role);
     }
 
     /**
      * Returns the value of the named attribute as an <code>Object</code>,
      * or <code>null</code> if no attribute of the given name exists.
      * <p/>
      * Attribute names should follow the same conventions as package
      * names. This specification reserves names matching <code>java.*</code>,
      * and <code>javax.*</code>.
      * <p/>
      * In a distributed portlet web application the <code>Object</code>
      * needs to be serializable.
      *
      * @param name a <code>String</code> specifying the name of
      *             the attribute
      * @throws IllegalArgumentException if name is <code>null</code>.
      * @return		an <code>Object</code> containing the value
      * of the attribute, or <code>null</code> if
      * the attribute does not exist.
      */
     public Object getAttribute(String name) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         return this.getHttpServletRequest().getAttribute(name);
     }
 
     /**
      * Returns an <code>Enumeration</code> containing the
      * names of the attributes available to this request.
      * This method returns an empty <code>Enumeration</code>
      * if the request has no attributes available to it.
      *
      * @return		an <code>Enumeration</code> of strings
      * containing the names
      * of the request attributes, or an empty
      * <code>Enumeration</code> if the request
      * has no attributes available to it.
      */
     public java.util.Enumeration getAttributeNames() {
         return this.getHttpServletRequest().getAttributeNames();
     }
 
     /**
      * Returns the value of a request parameter as a <code>String</code>,
      * or <code>null</code> if the parameter does not exist. Request parameters
      * are extra information sent with the request. The returned parameter
      * are "x-www-form-urlencoded" decoded.
      * <p/>
      * Only parameters targeted to the current portlet are accessible.
      * <p/>
      * This method should only be used if the
      * parameter has only one value. If the parameter might have
      * more than one value, use {@link #getParameterValues}.
      * <p/>
      * If this method is used with a multivalued
      * parameter, the value returned is equal to the first value
      * in the array returned by <code>getParameterValues</code>.
      *
      * @param name a <code>String</code> specifying the
      *             name of the parameter
      * @throws IllegalArgumentException if name is <code>null</code>.
      * @return		a <code>String</code> representing the
      * single value of the parameter
      * @see #getParameterValues
      */
     public String getParameter(String name) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         hasReader = true;
         Object val = this.getParameterMap().get(name);
         if (val != null) {
             if (val instanceof String) return (String) val;
             if (val instanceof String[]) {
                 String[] s = (String[]) val;
                 return s[0];
             }
             return (String) val;
         }
         return null;
     }
 
     /**
      * Returns an <code>Enumeration</code> of <code>String</code>
      * objects containing the names of the parameters contained
      * in this request. If the request has
      * no parameters, the method returns an
      * empty <code>Enumeration</code>.
      * <p/>
      * Only parameters targeted to the current portlet are returned.
      *
      * @return		an <code>Enumeration</code> of <code>String</code>
      * objects, each <code>String</code> containing
      * the name of a request parameter; or an
      * empty <code>Enumeration</code> if the
      * request has no parameters.
      */
     public java.util.Enumeration getParameterNames() {
         hasReader = true;
         return Collections.enumeration(this.getParameterMap().keySet());
     }
 
     /**
      * Returns an array of <code>String</code> objects containing
      * all of the values the given request parameter has, or
      * <code>null</code> if the parameter does not exist.
      * The returned parameters are "x-www-form-urlencoded" decoded.
      * <p/>
      * If the parameter has a single value, the array has a length
      * of 1.
      *
      * @param name a <code>String</code> containing the name of
      *             the parameter the value of which is requested
      * @throws IllegalArgumentException if name is <code>null</code>.
      * @return		an array of <code>String</code> objects
      * containing the parameter values.
      * @see		#getParameter
      */
     public String[] getParameterValues(String name) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         hasReader = true;
         return (String[]) this.getParameterMap().get(name);
     }
 
     public Map getParameterMap2() {
         //get control params
         Map portletParameters = new HashMap();
 
         /*
         Iterator iterator = portalParameters.getRenderParamNames();
 
         while (iterator.hasNext())
         {
             String name = (String)iterator.next();
 
             String[] values = portalParameters.getRenderParamValues(name);
 
             portletParameters.put(name, values );
 
         }
 
         //get request params
         String myid = portalParameters.getMyId();
         String cid = portalParameters.getTargetedID();
 
         if (myid.equals(cid)) {
             for (Enumeration parameters = super.getParameterNames(); parameters.hasMoreElements();) {
                 String   paramName   = (String)parameters.nextElement();
                 String[] paramValues = (String[])super.getParameterValues(paramName);
                 String[] values      = (String[])portletParameters.get(paramName);
 
                 if (values != null) {
                     String[] temp = new String[paramValues.length + values.length];
                     System.arraycopy(paramValues, 0, temp, 0, paramValues.length);
                     System.arraycopy(values, 0, temp, paramValues.length, values.length);
                     paramValues = temp;
                 }
                 portletParameters.put(paramName, paramValues);
             }
         }
         */
         return Collections.unmodifiableMap(portletParameters);
     }
 
     /**
      * Returns a <code>Map</code> of the parameters of this request.
      * Request parameters are extra information sent with the request.
      * The returned parameters are "x-www-form-urlencoded" decoded.
      * <p/>
      * The values in the returned <code>Map</code> are from type
      * String array (<code>String[]</code>).
      * <p/>
      * If no parameters exist this method returns an empty <code>Map</code>.
      *
      * @return an immutable <code>Map</code> containing parameter names as
      *         keys and parameter values as map values, or an empty <code>Map</code>
      *         if no parameters exist. The keys in the parameter
      *         map are of type String. The values in the parameter map are of type
      *         String array (<code>String[]</code>).
      */
     public java.util.Map getParameterMap() {
         hasReader = true;
         return portalParameters.getParameterMap();
     }
 
     /**
      * Returns a boolean indicating whether this request was made
      * using a secure channel between client and the portal, such as HTTPS.
      *
      * @return true, if the request was made using a secure channel.
      */
     public boolean isSecure() {
         return this.getHttpServletRequest().isSecure();
     }
 
     /**
      * Stores an attribute in this request.
      * <p/>
      * <p>Attribute names should follow the same conventions as
      * package names. Names beginning with <code>java.*</code>,
      * <code>javax.*</code>, and <code>com.sun.*</code> are
      * reserved for use by Sun Microsystems.
      * <br> If the value passed into this method is <code>null</code>,
      * the effect is the same as calling {@link #removeAttribute}.
      *
      * @param name a <code>String</code> specifying
      *             the name of the attribute
      * @param o    the <code>Object</code> to be stored
      * @throws IllegalArgumentException if name is <code>null</code>.
      */
     public void setAttribute(String name, Object o) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         if (o == null) {
             this.removeAttribute(name);
         } else {
             this.getHttpServletRequest().setAttribute(name, o);
         }
     }
 
     /**
      * Removes an attribute from this request.  This method is not
      * generally needed, as attributes only persist as long as the request
      * is being handled.
      * <p/>
      * <p>Attribute names should follow the same conventions as
      * package names. Names beginning with <code>java.*</code>,
      * <code>javax.*</code>, and <code>com.sun.*</code> are
      * reserved for use by Sun Microsystems.
      *
      * @param name a <code>String</code> specifying
      *             the name of the attribute to be removed
      * @throws IllegalArgumentException if name is <code>null</code>.
      */
     public void removeAttribute(String name) {
         if (name == null) throw new IllegalArgumentException("name is NULL");
         this.getHttpServletRequest().removeAttribute(name);
     }
 
     /**
      * Returns the session ID indicated in the client request.
      * This session ID may not be a valid one, it may be an old
      * one that has expired or has been invalidated.
      * If the client request
      * did not specify a session ID, this method returns
      * <code>null</code>.
      *
      * @return		a <code>String</code> specifying the session
      * ID, or <code>null</code> if the request did
      * not specify a session ID
      * @see		#isRequestedSessionIdValid
      */
     public String getRequestedSessionId() {
         return this.getHttpServletRequest().getRequestedSessionId();
     }
 
     /**
      * Checks whether the requested session ID is still valid.
      *
      * @return			<code>true</code> if this
      * request has an id for a valid session
      * in the current session context;
      * <code>false</code> otherwise
      * @see			#getRequestedSessionId
      * @see			#getPortletSession
      */
     public boolean isRequestedSessionIdValid() {
         return this.getHttpServletRequest().isRequestedSessionIdValid();
     }
 
     /**
      * Returns the portal preferred content type for the response.
      * <p/>
      * The content type only includes the MIME type, not the
      * character set.
      * <p/>
      * Only content types that the portlet has defined in its
      * deployment descriptor are valid return values for
      * this method call. If the portlet has defined
      * <code>'*'</code> or <code>'* / *'</code> as supported content
      * types, these may also be valid return values.
      *
      * @return preferred MIME type of the response
      */
     public String getResponseContentType() {
         Portlet.Mode mode = (Portlet.Mode) getAttribute(SportletProperties.PORTLET_MODE);
         if (supports != null) {
             Supports s = supports[0];
             org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode[] modes = s.getPortletMode();
             for (int j = 0; j < modes.length; j++) {
                 org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode m = modes[j];
                 if (m.getContent().equalsIgnoreCase(mode.toString())) {
                     return s.getMimeType().getContent();
                 }
             }
             return s.getMimeType().getContent();
 
         }
         return null;
     }
 
     /**
      * Gets a list of content types which the portal accepts for the response.
      * This list is ordered with the most preferable types listed first.
      * <p/>
      * The content type only includes the MIME type, not the
      * character set.
      * <p/>
      * Only content types that the portlet has defined in its
      * deployment descriptor are valid return values for
      * this method call. If the portlet has defined
      * <code>'*'</code> or <code>'* / *'</code> as supported content
      * types, these may also be valid return values.
      *
      * @return ordered list of MIME types for the response
      */
     public java.util.Enumeration getResponseContentTypes() {
         List types = new ArrayList();
         Portlet.Mode mode = (Portlet.Mode) getAttribute(SportletProperties.PORTLET_MODE);
         if (supports != null) {
             for (int i = 0; i < supports.length; i++) {
                 Supports s = supports[i];
                 org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode[] modes = s.getPortletMode();
                 if (modes.length == 0) {
                     types.add(s.getMimeType().getContent());
                 } else {
                     for (int j = 0; j < modes.length; j++) {
                         org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletMode m = modes[j];
                         if (m.getContent().equalsIgnoreCase(mode.toString())) {
                             types.add(s.getMimeType().getContent());
                         }
                     }
                 }
             }
         }
         return new Enumerator(types);
     }
 
     /**
      * Returns the preferred Locale in which the portal will accept content.
      * The Locale may be based on the Accept-Language header of the client.
      *
      * @return the prefered Locale in which the portal will accept content.
      */
     public java.util.Locale getLocale() {
         Locale locale = (Locale) this.getPortletSession(true).getAttribute(SportletProperties.LOCALE);
         if (locale != null) return locale;
         locale = this.getHttpServletRequest().getLocale();
         if (locale != null) return locale;
         return Locale.ENGLISH;
     }
 
 
     /**
      * Returns an Enumeration of Locale objects indicating, in decreasing
      * order starting with the preferred locale in which the portal will
      * accept content for this request.
      * The Locales may be based on the Accept-Language header of the client.
      *
      * @return an Enumeration of Locales, in decreasing order, in which
      *         the portal will accept content for this request
      */
     public java.util.Enumeration getLocales() {
         return this.getHttpServletRequest().getLocales();
     }
 
     /**
      * Returns the name of the scheme used to make this request.
      * For example, <code>http</code>, <code>https</code>, or <code>ftp</code>.
      * Different schemes have different rules for constructing URLs,
      * as noted in RFC 1738.
      *
      * @return		a <code>String</code> containing the name
      * of the scheme used to make this request
      */
     public String getScheme() {
         return this.getHttpServletRequest().getScheme();
     }
 
     /**
      * Returns the host name of the server that received the request.
      *
      * @return		a <code>String</code> containing the name
      * of the server to which the request was sent
      */
     public String getServerName() {
         return this.getHttpServletRequest().getServerName();
     }
 
     /**
      * Returns the port number on which this request was received.
      *
      * @return		an integer specifying the port number
      */
     public int getServerPort() {
         return this.getHttpServletRequest().getServerPort();
     }
 
     public int getContentLength() {
         if (included) return 0;
         return this.getHttpServletRequest().getContentLength();
     }
 
     public String getProtocol() {
         return null;
     }
 
     public String getRemoteAddr() {
         return null;
     }
 
     public String getRemoteHost() {
         return null;
     }
 
     public String getRealPath(String s) {
         return null;
     }
 
     public StringBuffer getRequestURL() {
         return null;
     }
 
     public String getCharacterEncoding() {
         return null;
     }
 
     public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
         // do nothing
     }
 
     public String getContentType() {
         if (included) return null;
         return this.getHttpServletRequest().getContentType();
     }
 
     public String getQueryString() {
         String attr = (String) super.getAttribute("javax.servlet.include.query_string");
         return (attr != null) ? attr : super.getQueryString();
     }
 
     public String getPathInfo() {
         String attr = (String) super.getAttribute("javax.servlet.include.path_info");
         return (attr != null) ? attr : super.getPathInfo();
     }
 
     public String getRequestURI() {
         String attr = (String) super.getAttribute("javax.servlet.include.request_uri");
         return (attr != null) ? attr : super.getRequestURI();
     }
 
     public String getServletPath() {
         String attr = (String) super.getAttribute("javax.servlet.include.servlet_path");
         return (attr != null) ? attr : super.getServletPath();
     }
 
     public String getPathTranslated() {
         return null;
     }
 
     public ServletInputStream getInputStream() throws IOException {
         if (included) return null;
 
         javax.servlet.ServletInputStream stream = getHttpServletRequest().getInputStream();
         hasReader = true;
         return stream;
 
     }
 
     public BufferedReader getReader() throws UnsupportedEncodingException, IOException {
         if (included) return null;
 
         BufferedReader reader = getHttpServletRequest().getReader();
 
         hasReader = true;
 
         return reader;
 
     }
 
     private HttpServletRequest getHttpServletRequest() {
         return (HttpServletRequest) super.getRequest();
     }
 
 }
