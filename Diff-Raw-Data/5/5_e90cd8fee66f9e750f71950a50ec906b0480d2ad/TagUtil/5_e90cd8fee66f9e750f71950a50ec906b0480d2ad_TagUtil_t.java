 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.jsp.tags;
 
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.PageContext;
 
 public class TagUtil {
     private static String ACCEPT = "Accept";
     private static String HYPERBROWSER = "HyperBrowser";
     private static String COOKIE_FORMAT = "org.icemobile.cookieformat";
 
     public static final String USER_AGENT = "User-Agent";
     public static final String USER_AGENT_COOKIE = "com.icesoft.user-agent";
     public static final String SX_USER_AGENT = "icemobile-sx";
     public static final String CLOUD_PUSH_KEY = "iceCloudPushId";
 
     public static String A_TAG = "<a";
     public static String A_TAG_END = "</a>";
 
     public static String SPAN_TAG = "<span";
     public static String SPAN_TAG_END = "</span>";
 
     public static String DIV_TAG = "<div";
     public static String DIV_TAG_END = "</div>";
 
     public static String UL_TAG = "<ul";
     public static String UL_TAG_END = "</ul>";
 
     public static String LI_TAG = "<li";
     public static String LI_TAG_END = "</li>";
 
     public static String INPUT_TAG = "<input";
     public static String INPUT_TAG_END = "</input>";
 
     public static String HEAD_TAG = "<head";
     public static String HEAD_TAG_END = "</head>";
 
     public static String SCRIPT_TAG = "<script";
     public static String SCRIPT_TAG_END = "</script>";
 
     public static String SECTION_TAG = "<section";
     public static String SECTION_TAG_END = "</section>";
 
     public static String JS_BOILER = " type=\"text/javascript\"";
 
 
     public enum DeviceType {
         android,
         honeycomb,
         bberry,
         iphone,
         ipad;
         public static final DeviceType DEFAULT = DeviceType.ipad;
 
         public boolean equals(String deviceName) {
             return this.name().equals(deviceName);
         }
     }
 
     /**
      * Check to see if the browser supports auxiliary upload
      *
      * @param pageContext
      * @return true if auxiliary upload detected.
      */
     public static boolean isAuxUploadBrowser(PageContext pageContext) {
         HttpServletRequest request = (HttpServletRequest)
             pageContext.getRequest();
         HttpSession httpSession = request.getSession(false);
         if (null != httpSession) {
             String iceUserAgent = (String) httpSession.getAttribute(
                 "com.icesoft.user-agent");
             if ((null != iceUserAgent) && (
                 iceUserAgent.startsWith("HyperBrowser")))  {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Check to see if the browser is a enhanced container browser
      *
      * @param pageContext
      * @return true if hyperbrowser cookie detected.
      */
     public static boolean isEnhancedBrowser(PageContext pageContext) {
         HttpServletRequest request = (HttpServletRequest)
             pageContext.getRequest();
         Cookie[] cookies = request.getCookies();
         if (null == cookies) {
             return false;
         }
         for (int i = 0; i < cookies.length; i++) {
             if (USER_AGENT_COOKIE.equals(cookies[i].getName()) &&
                 cookies[i].getValue().startsWith(HYPERBROWSER)) {
                 return true;
             }
         }
         return false;
     }
     
     public static Cookie getCookie(PageContext pageContext, String cookieName){
     	Cookie cookie = null;
     	HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
         Cookie[] cookies = request.getCookies();
         if ( cookies != null ) {
         	for (int i = 0; i < cookies.length; i++) {
                 if (cookieName.equals(cookies[i].getName()) ) {
                     cookie = cookies[i];
                 }
             }
         }
         return cookie;
     }
 
     public static String getICEmobileSXScript(PageContext pageContext,
                                               String command, String id) {
         HttpServletRequest request = (HttpServletRequest)
             pageContext.getRequest();
         String sessionID = null;
         HttpSession httpSession = request.getSession(false);
         if (null != httpSession) {
             sessionID = httpSession.getId();
         }
         String uploadURL = getUploadURL(request);
         String fullCommand = command + "?id=" + id;
         String script = "window.location=\"icemobile://c=" +
             URLEncoder.encode(fullCommand) +
             "&r=\"+escape(window.location)+\"";
         if (null != sessionID) {
             script += "&JSESSIONID=" + sessionID;
         }
         script += "&u=" + URLEncoder.encode(uploadURL) + "\"";
         return script;
     }
     
     public static String getICEmobileRegisterSXScript(PageContext pageContext){
     	 HttpServletRequest request = (HttpServletRequest)
     	            pageContext.getRequest();
         String sessionID = null;
         HttpSession httpSession = request.getSession(false);
         if (null != httpSession) {
             sessionID = httpSession.getId();
         }
         String uploadURL = getUploadURL(request);
         return "ice.registerAuxUpload('"+sessionID+"','"+uploadURL+"');";
     }
     
     /**
      * Get the SX Register URL. 
      * 
      * Format is icemobile://c=register&r=<current-url>&JSESSIONID=<session-id>&u=<upload-url>
      * @param request The servlet request
      * @return The escaped SX register URL.
      */
     public static String getRegisterSXURL(HttpServletRequest request){
     	String redirectUrl = getBaseURL(request);
     	String forward = (String)request.getAttribute("javax.servlet.forward.servlet_path");
    	if( forward == null ){
    		forward = "";
    	}
    	else if( forward.startsWith("/")){
     		forward = forward.substring(1);
     	}
     	String params = "";
 		if( request.getQueryString() != null ){
 			params = "?"+request.getQueryString();
 		}
 		redirectUrl += forward + params;
 		String uploadUrl = TagUtil.getUploadURL(request);
 		String url = "icemobile://c=register&r="+redirectUrl+"&u="+uploadUrl;
 		return url;
     }
     
     
     public static String getUploadURL(HttpServletRequest request) {
         String serverName = request.getHeader("x-forwarded-host");
         if (null == serverName) {
             serverName = request.getServerName() + ":" +
                 request.getServerPort();
         }
         return "http://" + serverName +
             getUploadPath(request);
     }
 
     public static String getBaseURL(ServletRequest request) {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         String serverName = httpRequest.getHeader("x-forwarded-host");
         if (null == serverName) {
             serverName = httpRequest.getServerName() + ":" +
                 httpRequest.getServerPort();
         }
         return httpRequest.getScheme() + "://" + serverName +
             httpRequest.getContextPath() + "/";
     }
 
 
     public static String getUploadPath(HttpServletRequest request) {
         String upPath = request.getContextPath() + "/icemobile";
         return upPath;
     }
 
     public static String getCloudPushId(PageContext pageContext)  {
         String cloudPushId = null;
         HttpServletRequest request = (HttpServletRequest)
             pageContext.getRequest();
         HttpSession httpSession = request.getSession(false);
         if (null != httpSession) {
             cloudPushId = (String) httpSession.getAttribute(CLOUD_PUSH_KEY);
         }
         return cloudPushId;
     }
 
 
     static Logger log = Logger.getLogger(
         TagUtil.class.getName());
 
     //each device has list of info about device and capabilities
     public static final String DEVICE_IPHONE = "iphone";
     public static final String DEVICE_IPAD = "ipad";
     public static final String DEVICE_IPOD = "ipod";
     public static final String DEVICE_MAC = "macintosh"; //test laptop
     public static final String DEVICE_ANDROID = "android";
     public static final String DEVICE_HONEYCOMB = "android 3.";
     public static final String DEVICE_BLACKB = "blackberry";
     public static final String DEVICE_BB_CURVE = "blackberry89"; //curve2
     public static final String DEVICE_BB_TORCH = "blackberry 98"; //torch
     public static final String VND_RIM = "vnd.rim";  //found when emulating IE or FF on BB
     public static final String DEVICE_IOS4 = " os 4_";
     public static final String DEVICE_IOSS = " os 5_";
     public static final String DEVICE_IOS6 = " os 6_";
     public static final String DEVICE_MOBILE = "mobile";
     public static final String DEVICE_TABLET = "tablet";
     public static final String DEVICE_GALAXY_TABLET = "gt-p1000";
 
     public static final String VIEW_TYPE_SMALL = "small";
     public static final String VIEW_TYPE_LARGE = "large";
 
     public static boolean isTouchEventEnabled(PageContext pageContext) {
         if (sniffAndroidTablet(pageContext)) {
             return false;
         }
         if (isIOS(pageContext) || isAndroid(pageContext)) {
             return true;
         }
         return false;
     }
 
     public static boolean useNative(PageContext pageContext) {
         return isIOS5orHigher(pageContext) || isBlackBerry(pageContext);
     }
 
     public static boolean isIOS5orHigher(PageContext pageContext) {
         return sniffIOS5(pageContext) || sniffIOS6(pageContext);
     }
     
     public static boolean isIOS6orHigher(PageContext pageContext) {
         return sniffIOS6(pageContext);
     }
     
     public static boolean isSX(HttpServletRequest request){
     	return sniffSX(request);
     }
     
 
     public static boolean isIOS(PageContext pageContext) {
         return sniffIOS(pageContext);
     }
 
     public static boolean isAndroid(PageContext pageContext) {
         return sniffAndroid(pageContext);
     }
     
     public static boolean isAndroidOS(PageContext pageContext){
     	String userAgent = getUserAgent(pageContext);
     	if ((null != userAgent) && userAgent.contains("apache-httpclient"))  {
             //hack for android container
             return true;
         } 
     	else if ( userAgent.contains(DEVICE_ANDROID)){
     		return true;
     	}
     	return false;
     }
 
     public static boolean isBlackBerry(PageContext pageContext) {
         return sniffBlackberry(pageContext);
     }
 
     static boolean sniffIpod(PageContext pageContext) {
         return userAgentContains(pageContext, DEVICE_IPOD);
     }
 
     static boolean sniffIphone(PageContext pageContext) {
     	 return userAgentContains(pageContext, DEVICE_IPHONE);
     }
 
     static boolean sniffIOS(PageContext pageContext) {
         return sniffIphone(pageContext) || sniffIpod(pageContext) || sniffIpad(pageContext);
     }
     
     static boolean sniffIOS4(PageContext pageContext) {
         return userAgentContains(pageContext, DEVICE_IOSS);
     }
 
     static boolean sniffIOS5(PageContext pageContext) {
         return userAgentContains(pageContext, DEVICE_IOSS);
     }
     
     static boolean sniffIOS6(PageContext pageContext) {
     	return userAgentContains(pageContext, DEVICE_IOS6);
     }
 
     static boolean sniffIpad(PageContext pageContext) {
     	return userAgentContains(pageContext, DEVICE_IPAD);
     }
     
     static boolean sniffSX(HttpServletRequest request) {
     	return userAgentContains(getUserAgent(request), DEVICE_IPAD);
     }
     
     static boolean sniffAndroid(PageContext pageContext) {
     	boolean foundAndroid = userAgentContains(pageContext, DEVICE_ANDROID) && 
         		userAgentContains(pageContext, DEVICE_MOBILE) && !userAgentContains(pageContext, DEVICE_GALAXY_TABLET) 
         		&& !userAgentContains(pageContext, DEVICE_TABLET);
         return foundAndroid;
     }
 
     static boolean sniffAndroidTablet(PageContext pageContext) {
     	 boolean result = userAgentContains(pageContext, DEVICE_ANDROID) && 
     	        	(!userAgentContains(pageContext, DEVICE_MOBILE) || 
     	        			userAgentContains(pageContext, DEVICE_GALAXY_TABLET) 
     	        	|| userAgentContains(pageContext, DEVICE_TABLET));
         return result;
     }
 
     /**
      * Check for blackberry. This method is called from all paths now and
      * Android devices on POST appear to have no accept header.
      *
      * @param uaString
      * @param httpAccept
      * @return true if Blackberry device detected
      */
     static boolean sniffBlackberry(PageContext pageContext) {
     	boolean blackberry = userAgentContains(pageContext, DEVICE_BLACKB);
     	boolean vnd = false;
     	if( !blackberry ){
     		vnd = acceptContains(pageContext, VND_RIM);
     	}
         boolean result = blackberry || vnd;
         return result;
     }
 
     private static void logSniff(boolean result, String device,
                                  String userAgent) {
         if (log.isLoggable(Level.FINEST)) {
             if (result) {
                 log.finest("Detected " + device + " " + userAgent);
             }
         }
     }
     
     static DeviceType getDeviceTypeNoDefault(PageContext pageContext) {
         return checkUserAgentInfo(pageContext);
     }
 
     static DeviceType getDeviceType(PageContext pageContext) {
         DeviceType device = checkUserAgentInfo(pageContext);
         return device == null ? DeviceType.DEFAULT : device;
     }
 
     private static DeviceType checkUserAgentInfo(PageContext pageContext) {
     	if (sniffIphone(pageContext)) {
             return DeviceType.iphone;
         }
         if (sniffAndroidTablet(pageContext)) {
             return DeviceType.honeycomb;
         }
         if (sniffAndroid(pageContext)) {
             return DeviceType.android;
         }
         if (sniffBlackberry(pageContext)) {
             return DeviceType.bberry;
         }
         if (sniffIpad(pageContext)) {
             return DeviceType.ipad;
         }
         return null;
     }
 
     public void writeAttribute(Writer out, String name, String value) throws
         IOException {
         out.write(" " + name + "=\"" + value + "\"");
     }
     
 
     public boolean isValueBlank(String value) {
         if (value == null) {
             return true;
         }
         return value.trim().equals("");
     }
     
     public static String getUserAgent(PageContext pageContext){
     	HttpServletRequest request = (HttpServletRequest)
                 pageContext.getRequest();
     	String ua = request.getHeader(USER_AGENT);
     	return ua == null ? ua : ua.toLowerCase();
     }
     
     public static String getUserAgent(HttpServletRequest request){
     	String ua = request.getHeader(USER_AGENT);
     	return ua == null ? ua : ua.toLowerCase();
     }
     
     private static String getAccept(PageContext pageContext){
     	HttpServletRequest request = (HttpServletRequest)
                 pageContext.getRequest();
     	String accept = request.getHeader(ACCEPT);
     	return accept == null ? accept : accept.toLowerCase();
     }
     
     private static boolean acceptContains(PageContext pageContext, String contains){
     	boolean result = false;
     	String accept = getAccept(pageContext);
     	if( accept != null ){
     		result = accept.contains(contains);
     	}
     	return result;
     }
     
     private static boolean userAgentContains(PageContext pageContext, String contains){
     	boolean result = false;
     	String ua = getUserAgent(pageContext);
     	if( ua != null ){
     		result = ua.contains(contains);
     	}
     	logSniff(result, contains, ua);
     	return result;   	
     }
     
     private static boolean userAgentContains(String userAgent, String contains){
     	boolean result = false;
     	if( userAgent != null ){
     		result = userAgent.toLowerCase().contains(contains);
     	}
     	logSniff(result, contains, userAgent);
     	return result;   	
     }
     
     /**
 	 * Test from http://detectmobilebrowsers.com
 	 * @param userAgent
 	 * @return true if mobile
 	 */
 	public static boolean isMobileBrowser(String userAgent){
 		String ua=userAgent.toLowerCase();
 		return (ua.matches("(?i).*(android.+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|meego.+mobile|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")
 				||ua.substring(0,4).matches("(?i)1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-"))
 				&& !userAgent.contains(DEVICE_GALAXY_TABLET) ;
 		
 	}
 	
 	/**
 	 * Test from http://detectmobilebrowsers.com
 	 * @param userAgent
 	 * @return true if mobile
 	 */
 	public static boolean isTabletBrowser(String userAgent){
 		String ua=userAgent.toLowerCase();
 		return ua.matches("(?i).*(android|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(ad|hone|od)|iris|kindle|lge |maemo|meego.+mobile|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|playbook|silk|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")
 				|| userAgent.contains(DEVICE_GALAXY_TABLET);
 	}
 	
 	public static boolean isDesktop(PageContext pageContext){
 		String userAgent = getUserAgent(pageContext);
 		if ((null != userAgent) && userAgent.contains("apache-httpclient"))  {
             //hack for android container
             return false;
         } 
 		return !isMobileBrowser(userAgent) && !isTabletBrowser(userAgent);
 	}
 
     /*    protected void writeJavascriptFile(FacesContext facesContext, 
             UIComponent component, String JS_NAME, String JS_MIN_NAME, 
             String JS_LIBRARY) throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = component.getClientId(facesContext);
         writer.startElement(HTML.SPAN_ELEM, component);
         writer.writeAttribute(HTML.ID_ATTR, clientId+"_libJS", HTML.ID_ATTR);
         if (!isScriptLoaded(facesContext, JS_NAME)) {
             String jsFname = JS_NAME;
             if (facesContext.isProjectStage(ProjectStage.Production)){
                 jsFname = JS_MIN_NAME;
             }
             //set jsFname to min if development stage
             Resource jsFile = facesContext.getApplication().getResourceHandler().createResource(jsFname, JS_LIBRARY);
             String src = jsFile.getRequestPath();
             writer.startElement("script", component);
             writer.writeAttribute("text", "text/javascript", null);
             writer.writeAttribute("src", src, null);
             writer.endElement("script");
             setScriptLoaded(facesContext, JS_NAME);
         } 
         writer.endElement(HTML.SPAN_ELEM);
 	} */
 
 }
