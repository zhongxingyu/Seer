 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: FSUtils.java,v 1.3 2008-06-25 05:46:40 qcheng Exp $
  *
  */
 
 package com.sun.identity.federation.common;
 
 import com.sun.identity.common.SystemConfigurationUtil;
 import com.sun.identity.federation.meta.IDFFMetaManager;
 import com.sun.identity.plugin.session.SessionException;
 import com.sun.identity.plugin.session.SessionManager;
 import com.sun.identity.plugin.session.SessionProvider;
 import com.sun.identity.saml.common.SAMLUtils;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.encode.Base64;
 import com.sun.identity.shared.locale.Locale;
 import java.io.ByteArrayInputStream;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import netscape.ldap.util.DN;
 import netscape.ldap.util.RDN;
 
 /**
  * This class contain constants used in the SDK.
  */
 public class FSUtils {
     public static String deploymentURI =
         SystemConfigurationUtil.getProperty(
             "com.iplanet.am.services.deploymentDescriptor");
 
     public static final String BUNDLE_NAME = "libIDFF";
     public static IFSConstants sc;
     public static ResourceBundle bundle =
         Locale.getInstallResourceBundle(BUNDLE_NAME);
     public static Debug debug = Debug.getInstance("libIDFF");    
     private static SecureRandom random = new SecureRandom();
     public static final String FSID_PREFIX = "f"; 
     public static IDFFMetaManager metaInstance = null;    
 
     /**
      * Constructor
      */
     private FSUtils() {
     }
 
     /**
      * Sets the locale of the resource bundle
      *
      */
     public static void setLocale(String localeName){
         try {
             bundle = ResourceBundle.getBundle(
                 BUNDLE_NAME, Locale.getLocale(localeName));
         } catch (MissingResourceException mre) {
             System.err.println(mre.getMessage());
             System.exit(1);
         }
     } 
 
     /**
      * Generates an ID String with length of IFSConstants.ID_LENGTH.
      * @return string the ID String; or null if it fails.
      */
     public static String generateID() {
         if (random == null) {
             return null;
         }
         byte bytes[] = new byte[IFSConstants.ID_LENGTH];
         random.nextBytes(bytes);
         String encodedID = FSID_PREFIX + SAMLUtils.byteArrayToHexString(bytes);
         if (FSUtils.debug.messageEnabled()) {
             FSUtils.debug.message("FSUtils.generateID: generated id is " +
                 encodedID);
         }
 
         return encodedID;
     }
     
     /**
      * Generates source ID String 
      * @param entityID the entity ID of the source site 
      * @return source ID 
      */
     public static String generateSourceID(String entityID) {
         MessageDigest md = null;
         try {
             md = MessageDigest.getInstance("SHA");
         } catch (Exception e) {
             FSUtils.debug.error("FSUtils.generateSourceID: Exception:",e);
             return null;
         }
         char chars[] = entityID.toCharArray();
         byte bytes[] = new byte[chars.length];
         for (int i = 0; i < chars.length; i++) {
             bytes[i] = (byte) chars[i];
         }
 
         md.update(bytes);
         return SAMLUtils.byteArrayToString(md.digest());
     }
     
     /**
      * Generates assertion handle.
      * @return 20-byte random string to be used to form an artifact.
      */
     public static String generateAssertionHandle() {
         String result = null;
         String encodedID = generateID();
         if (encodedID != null) {
             try {
                 result = encodedID.substring(0, 20);
             } catch (Exception e) {
                 FSUtils.debug.error("FSUtil.generateAssertionHandle:", e);
             }
         }
         return result;
     }
     
     /**
      * Converts a string to Base64 encoded string.
      * @param succinctID provider's succinctID string
      * @return Base64 encoded string
      */
     public static String stringToBase64(String succinctID) {
         String encodedID = null;
         try {
             encodedID = Base64.encode(SAMLUtils.stringToByteArray(succinctID))
                 .trim();
         } catch (Exception e) {
             if (FSUtils.debug.messageEnabled()) {
                 FSUtils.debug.message(
                     "FSUtils:stringToBase64: exception encode input:", e);
             }
         }
         if (FSUtils.debug.messageEnabled()) {
             FSUtils.debug.message("base 64 source id is :"+encodedID);
         }
         return encodedID;
 
     }
     /**
      * Checks content length of a http request to avoid dos attack.
      * In case IDFF inter-op with other IDFF vendor who may not provide content
      * length in HttpServletRequest. We decide to support no length restriction
      * for Http communication. Here, we use a special value (e.g. 0) to
      * indicate that no enforcement is required.
      * @param request <code>HttpServletRequest</code> instance to be checked.
      * @exception ServletException if context length of the request exceeds
      *     maximum content length allowed.
      */
 
     public static void checkHTTPRequestLength(HttpServletRequest request)
         throws ServletException
     {
         // avoid the DOS attack for SOAP messaging 
         int maxContentLength = SAMLUtils.getMaxContentLength();
 
         if (maxContentLength != 0) {
             int length =  request.getContentLength();
 
             if (length == -1) {
                 throw new ServletException(bundle.getString("unknownLength"));
             }
 
             if (length > maxContentLength) {
                 if (debug.messageEnabled()) {
                     debug.message("FSUtils.checkHTTPRequestLength: " +
                         "content length too large" + length); 
                 }
                 throw new ServletException(
                     bundle.getString("largeContentLength"));
              }
         }    
     }
     
     /**
      * Forwards or redirects to a new URL. This method will do forwarding
      * if the target url is in  the same web deployment URI as current web 
      * apps. Otherwise will do redirecting.   
      * @param request HttpServletRequest
      * @param response HttpServletResponse
      * @param url the target URL to be forwarded to redirected.  
      */
     public static void forwardRequest(
         HttpServletRequest request,
         HttpServletResponse response,
         String url)
     {
         FSUtils.debug.message("FSUtils.forwardRequest: called");
         String newUrl = null;
         try {
             SessionProvider sessionProvider = SessionManager.getProvider();
             Object token = sessionProvider.getSession(request);
             if ((token != null) && (sessionProvider.isValid(token))) {
                 newUrl = sessionProvider.rewriteURL(token, url);
             }
         } catch (Exception se) {
             if (FSUtils.debug.messageEnabled()) {
                 FSUtils.debug.message(
                     "FSUtils.forwardReqeust: couldn't rewrite url: " +
                     se.getMessage());
             }
             newUrl = null;
         }
         if (newUrl == null) {
             newUrl = url;
         }
 
         try {
             //get source host and port
             String sourceHost = request.getServerName();            
             int sourcePort = request.getServerPort();
             FSUtils.debug.message("FSUtils.forwardRequest: " +
                 "SourceHost=" + sourceHost + " SourcePort="+ sourcePort);
             //get target host and port
             URL target = new URL(newUrl);
             String targetHost = target.getHost();
             int targetPort = target.getPort();            
             FSUtils.debug.message("FSUtils.forwardRequest: targetHost=" 
                 + targetHost + " targetPort=" + targetPort);
  
             /**
              * IBM websphere is not able to handle forwards with long urls.
              */ 
             boolean isWebSphere = false;
             String container = SystemConfigurationUtil.getProperty(
                 Constants.IDENTITY_WEB_CONTAINER);
             if (container != null && (container.indexOf("IBM") != -1)) {
                isWebSphere = true;
             }
             
                         
             int index = newUrl.indexOf(deploymentURI + "/");
             if( !(sourceHost.equals(targetHost)) || 
                 !(sourcePort == targetPort) || 
                 !(index > 0) || isWebSphere)
             {
                 FSUtils.debug.message("FSUtils.forwardRequest: Source and " +
                     "Target are not on the same container." + 
                     "Redirecting to target");            
                 response.sendRedirect(newUrl);
                 return;
             } else {      
                 String resource = newUrl.substring(
                     index + deploymentURI.length());
                 if (FSUtils.debug.messageEnabled()) {
                     FSUtils.debug.message(
                         "FSUtils.forwardRequest: Forwarding to :" + resource);
                 }  
                 RequestDispatcher dispatcher = 
                     request.getRequestDispatcher(resource);
                 try {
                     dispatcher.forward(request, response);
                 } catch (Exception e) {
                     FSUtils.debug.error("FSUtils.forwardRequest: Exception "
                         + "occured while trying to forward to resource:" +
                         resource , e);
                 }
             } 
         } catch (Exception ex) {
             FSUtils.debug.error("FSUtils.forwardRequest: Exception occured",ex);
         }
     }
 
     /**
      * Returns entity ID from the Succinct ID.
      * @param realm The realm under which the entity resides.
      * @param succinctID Succinct ID.
      * @return String entity ID; or <code>null</code> for failure in 
      *  converting the succinct id to entity id.
      */ 
     private static String getProviderIDFromSuccinctID(
         String realm, String succinctID) {
         if (succinctID == null) {
             return null;
         }
         try {
             metaInstance = getIDFFMetaManager();
             if (metaInstance != null) {
                 return metaInstance.getEntityIDBySuccinctID(realm, succinctID);
             }
         } catch(Exception ex) {
             debug.error("FSUtils.getProviderIDFromSuccinctID::", ex);
         }
         return null;
     }
 
     /**
      * Finds the preferred IDP from the HttpServletRequest.
      * @param realm The realm under which the entity resides.
      * @param request HttpServletRequest.
      * @return String preferred IDP entity ID; or <code>null</code> for failure
      *  or unable to find in the request.
      */
     public static String findPreferredIDP(
         String realm, HttpServletRequest request) {
 
         if (request == null) {
             return null;
         }
 
         String succinctID = request.getParameter(IFSConstants.PROVIDER_ID_KEY);
         if ((succinctID == null) || succinctID.length() == 0) { 
            debug.message("FSUtils.findPreferredIDP::Pref IDP not found.");
            return null;
         }
 
         succinctID = succinctID.trim();
         String preferredSuccinctId = null;
         StringTokenizer st = new StringTokenizer(succinctID, " ");
         while(st.hasMoreTokens()){
             preferredSuccinctId = st.nextToken();
            if (preferredSuccinctId.length() < 28) {
                 preferredSuccinctId = 
                     preferredSuccinctId + "+" + st.nextToken();
             }
         }
 
         preferredSuccinctId = SAMLUtils.byteArrayToString(
             Base64.decode(preferredSuccinctId));
 
         return getProviderIDFromSuccinctID(realm, preferredSuccinctId);
     }
 
     /**
      * Removes new line characters (useful for Base64 decoding)
      * @param s String
      * @return result String 
      */
     public static String removeNewLineChars(String s) {
         String retString = null;
         if ((s != null) && (s.length() > 0) && (s.indexOf('\n') != -1)) {
             char[] chars = s.toCharArray();
             int len = chars.length;
             StringBuffer sb = new StringBuffer(len);
             for (int i = 0; i < len; i++) {
                 char c = chars[i];
                 if (c != '\n') {
                     sb.append(c);
                 }
             }
             retString = sb.toString();
         } else {
             retString = s;
         }
         return retString;
     }
 
     /**
      * Returns an instance of the IDFF meta manager class.
      * @return <code>IDFFMetaManager</code> instance; or <code>null</code>
      *  if it cannot retrieve the instance.
      */
     public static IDFFMetaManager getIDFFMetaManager() {
         if (metaInstance == null){
             synchronized (IDFFMetaManager.class) {
                 try {
                     // TODO: generate admin session and pass it in
                     metaInstance = new IDFFMetaManager(null);
                     return metaInstance;
                 } catch (Exception e) {
                     FSUtils.debug.error ("FSUtils.getIDFFMetaManager:"
                         + " Could not create meta Manager", e);
                     return null;
                 }
             }
         }
         return metaInstance;
     }
 
     /*
      * Returns the Authentication Domain URL Mappings for the given 
      * organization.
      * @param orgDN dn of the organization/realm name
      * @return authentication domain
      */
     public static String getAuthDomainURL(String orgDN) {
         if (orgDN == null || orgDN.length() == 0) {
             return "/";
         }
         if (DN.isDN(orgDN)) {
             DN orgdn = new DN(orgDN);
             Vector rdn = orgdn.getRDNs();
             if ((rdn != null) && (rdn.size() > 0)) {
                 return ((RDN)rdn.firstElement()).getValues()[0];
             }
         } else {
             // should be realm name
             if (orgDN.startsWith("/")) {
                 if (orgDN.trim().equals("/")) {
                     return "/";
                 } else if (!orgDN.trim().endsWith("/")) {
                     int loc = orgDN.lastIndexOf("/");
                     return (orgDN.substring(loc + 1).trim());
                 } else {
                     // error case, but allow to continue
                     debug.error("getAuthDomainURL.invalid org URL " + orgDN);
                 }
             } else {
                 // error case, but allow to continue
                 debug.error("getAuthDomainURLList invalid org URL " + orgDN);
             }
         }
         return null;
     }
 }
