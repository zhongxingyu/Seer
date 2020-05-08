 package org.icemobile.jsp.tags;
 
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 /**
  * This is the Device specific detection and script writing tag.
  */
 public class DeviceResourceTag extends SimpleTagSupport {
 
     // <link href="<c:url value="/resources/form.css" />" rel="stylesheet"  type="text/css" />
     // <script type="text/javascript" src="<c:url value="/resources/jquery/1.6/jquery.js" />"></script>
 
     static Logger log = Logger.getLogger(DeviceResourceTag.class.getName());
 
     // CSS style extension circa 2011
     private static final String CSS_EXT = ".css";
     // compressed css post-fix notation.
     public static final String CSS_COMPRESSION_POSTFIX = "-min";
 
     // iPhone style sheet name found in jar.
     public static final String IPHONE_CSS = TagUtil.DeviceType.iphone.name() + CSS_EXT;
     // iPad style sheet name found in jar.
     public static final String IPAD_CSS = TagUtil.DeviceType.ipad.name() + CSS_EXT;
     // Android style sheet name found in jar.
     public static final String ANDROID_CSS = TagUtil.DeviceType.android.name() + CSS_EXT;
     // Android honeycomb style sheet name found in jar.
     public static final String HONEYCOMB_CSS = TagUtil.DeviceType.honeycomb.name() + CSS_EXT;
     // Blackberry style sheet name found in jar.
     public static final String BBERRY_CSS = TagUtil.DeviceType.bberry.name() + CSS_EXT;
 
     public static final String DEFAULT_RESOURCE_ROOT = "/resources";
 
     // default resource library for a default themes,  if not specified in
     // component definition this library will be loaded.
     private static final String DEFAULT_LIBRARY = "org.icefaces.component.skins";
     // url of a resource that could not be resolved, danger Will Robertson.
     public static final String RESOURCE_URL_ERROR = "RES_NOT_FOUND";
     // key to store if algorithm to detect device type has run. If a device
     // was detected this key used to store the device name in session scope
     // for later retrial on subsequent component renders.
     public static final String MOBILE_DEVICE_TYPE_KEY = "mobile_device_type";
 
 
     private final String EMPTY_STRING = "";
 
     private String name;
 
     private String library;
 
     private String view;
 
     private String jqversion;
 
 
     public void doTag() throws JspTagException {
 
         PageContext pageContext = (PageContext) getJspContext();
         ServletRequest sr = pageContext.getRequest();
 
         String userAgent = null;
         String accept = null;
 
         String resourceRoot = pageContext.getServletContext().
                                                                     getInitParameter("org.icemobile.resource.root");
         resourceRoot = (resourceRoot != null) ? resourceRoot : DEFAULT_RESOURCE_ROOT;
 
         String applicationStage = pageContext.getServletContext().
                                                                         getInitParameter("org.icemobile.project.stage");
 
         if (sr instanceof HttpServletRequest) {
             HttpServletRequest hsr = (HttpServletRequest) sr;
             userAgent = (String) hsr.getHeader("user-agent");
             if (userAgent != null) {
                 userAgent = userAgent.toLowerCase();
             }
             accept = (String) hsr.getHeader("accept");
         }
 
         JspWriter out = pageContext.getOut();
 
         /**
          * The component has three modes in which it executes.
          * 1.) no attributes - then component tries to detect a mobile device
          *     in from the user-agent.  If a mobile device is discovered, then
          *     it will fall into three possible matches, iphone, ipad,  android and
          *     blackberry.  If the mobile device is not not know then ipad
          *     is loaded. Library is always assumed to be DEFAULT_LIBRARY.
          *
          * 2.) name attribute - component will default to using a library name
          *     of DEFAULT_LIBRARY.  The name attribute specifies one of the
          *     possible device themes; iphone.css, android.css or bberry.css.
          *     Error will result if named resource could not be resolved.
          *
          * 3.) name and libraries attributes. - component will use the library
          *     and name specified by the user.  Component is fully manual in this
          *     mode. Error will result if name and library can not generate a
          *     value resource.
          */
 
         // check for the existence of the name and library attributes.
         String nameVal = getName();
         String libVal = getLibrary();
 
         String viewVal = getView();
 
         // check for empty string on name attribute used for auto mode where
         // name value binding is used.
         nameVal = nameVal != null && nameVal.equals(EMPTY_STRING) ? null : name;
 
         // 1.) full automatic device detection.
         if (nameVal == null && libVal == null) {
 
             // the view attribute if specified will apply a small or large
             // theme, large theme's are tablet based, so ipad and honeycomb.
             // small themes are android, iphone, and bberry.
             if (viewVal != null) {
                 nameVal = TagUtil.getDeviceType(userAgent).name();
                 // forces a small view
                 if (viewVal.equalsIgnoreCase(TagUtil.VIEW_TYPE_SMALL)) {
                     if (nameVal.equals(TagUtil.DeviceType.ipad.name())) {
                         nameVal = TagUtil.DeviceType.iphone.name();
                     } else if (nameVal.equals(TagUtil.DeviceType.honeycomb.name())) {
                         nameVal = TagUtil.DeviceType.android.name();
                     }
                 } else if (viewVal.equalsIgnoreCase(TagUtil.VIEW_TYPE_LARGE)) {
                     if (nameVal.equals(TagUtil.DeviceType.iphone.name())) {
                         nameVal = TagUtil.DeviceType.ipad.name();
                     } else if (name.equals(TagUtil.DeviceType.android.name())) {
                         nameVal = TagUtil.DeviceType.honeycomb.name();
                     }
                 } else {
                     log.warning("View type " + viewVal + " is not a recognized view type");
                 }
             } else {
                 nameVal = TagUtil.getDeviceType(userAgent).name();
             }
 
             nameVal.concat(CSS_EXT);
 
             // load compressed css if this is production environment.
             if (applicationStage != null && "production".equalsIgnoreCase(applicationStage)) {
                 nameVal = nameVal.concat(CSS_COMPRESSION_POSTFIX);
             }
             libVal = DEFAULT_LIBRARY;
 
         }
         // 2.) User has specified a named theme they want to load, no auto detect
         else if (nameVal != null && libVal == null) {
             // keep the name but apply default library.
             libVal = DEFAULT_LIBRARY;
         }
         // 3.) User has specified a name and theme of their own, anything goes.
         else {
             // nothing to do, any error will be displayed back to user at runtime
             // if the resource can't be found.
         }
 
 //        String cssfile =  libVal + "/" + nameVal;
         String cssfile = resourceRoot + "/" + libVal + "/" + nameVal;
 
         try {
             out.write("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + cssfile + ".css\" />");
             out.write("<script type=\"text/javascript\" src=\"" + resourceRoot + "/icemobile.js\" > </script>");
 
             String jqv = getJqversion();
             if (jqv != null && !"".equals(jqv)) {
                 out.write("<script type=\"text/javascript\" src=\"/" + resourceRoot + "/jquery/" +
                                  jqv + "/jquery.js\" > </script>");
             }
 
 
         } catch (IOException ioe) {
             log.severe("Exception writing to JSPWriter: " + ioe);
         }
 
     }
 
     public String getLibrary() {
         return library;
     }
 
     public void setLibrary(String library) {
         this.library = library;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getView() {
         return view;
     }
 
     public void setView(String view) {
         this.view = view;
     }
 
     public String getJqversion() {
         return jqversion;
     }
 
     public void setJqversion(String jqversion) {
         this.jqversion = jqversion;
     }
 }
