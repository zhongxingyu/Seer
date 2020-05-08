 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.TreeMap;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import javax.net.ssl.HttpsURLConnection;
 import java.net.URL;
 import java.net.Authenticator;
 import java.net.PasswordAuthentication;
 import java.net.MalformedURLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import no.feide.moria.controller.AuthenticationException;
 import no.feide.moria.controller.AuthorizationException;
 import no.feide.moria.controller.DirectoryUnavailableException;
 import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.log.MessageLogger;
 import no.feide.moria.store.MoriaStore;
 import no.feide.moria.store.MoriaStoreException;
 import no.feide.moria.store.MoriaStoreFactory;
 
 /**
  * The StatusServlet shows the status of Moria.
  * @version $Revision$
  */
 public class StatusServlet
 extends MoriaServlet {
 
     /** Used for logging. */
     private final MessageLogger log = new MessageLogger(StatusServlet.class);
 
     /**
      * List of parameters required by <code>StatusServlet</code>.
      * <br>
      * <br>
      * Current required parameters are:
      * <ul>
      * <li><code>RequestUtil.PROP_BACKENDSTATUS_STATUS_XML</code>
      * </ul>
      * @see RequestUtil#PROP_BACKENDSTATUS_STATUS_XML
      */
     private static final String[] REQUIRED_PARAMETERS = {
             RequestUtil.PROP_BACKENDSTATUS_STATUS_XML,
             RequestUtil.PROP_COOKIE_LANG };
     
     /**
      * 
      * @return the required parameters for this servlet.
      */
     public static String[] getRequiredParameters() {
         return REQUIRED_PARAMETERS;
     }
 
     /**
      * A hash map containing the attributes for a test-user.
      * Each item in the hashmap maps from a user name to an
      * backendStatusUser class instance
      */
     private HashMap backendDataUsers = null;
     
     /**
      * Monitor for the status xml file.
      */
     private FileMonitor statusFileMonitor = null;
     
     /**
      * The organization the test user comes from.
      */
     private static final String STATUS_ATTRIBUTE = "eduPersonAffiliation";    
     
     /**
      * The name of the service.
      */
     private static final String STATUS_PRINCIPAL = "status";
     
     /**
      * Implements a simple xml parser that parses the status.xml file
      * into a HashMap with BackendStatusUser instances.
      *
      * @see BackendStatusHandler
      * @see BackendStatusUser
      */
     public final synchronized HashMap getBackendStatusData() {
         if (statusFileMonitor == null || statusFileMonitor.hasChanged()) {
           Properties config = getConfig();
           if (config != null) {
             BackendStatusHandler handler = new BackendStatusHandler();
             SAXParserFactory factory = SAXParserFactory.newInstance();
             try {
                String filename = (String) config.get(RequestUtil.PROP_BACKENDSTATUS_STATUS_XML);
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(new File(filename), handler);
                statusFileMonitor = new FileMonitor(filename);
             } catch (Throwable t) {
               log.logCritical("Error parsing status.xml");
             } finally {
               backendDataUsers = handler.getAttribs();
             }
           }
         }
         return backendDataUsers;
     }
     
     private void printTable() {
         HashMap backendStatusData = getBackendStatusData();
         
     }
 
     /**
      * Checks the config
      * 
      * @param bundle
      * @return
      */
     private Vector checkConfigStatus(ResourceBundle bundle) {
         Vector statusConfig = new Vector();
         // InformationServlet
         try {
             this.getServletConfig(InformationServlet.getRequiredParameters(), log);
         } catch (IllegalStateException e) {
             String errorMsg = bundle.getString("config_info");
             statusConfig.add(errorMsg);
         }
 
         // LoginServlet
         try {
             this.getServletConfig(LoginServlet.getRequiredParameters(), log);
         } catch (IllegalStateException e) {
             String errorMsg = bundle.getString("config_login");
             statusConfig.add(errorMsg);
         }
 
         // StatisticsServlet
         try {
             this.getServletConfig(StatisticsServlet.getRequiredParameters(), log);
         } catch (IllegalStateException e) {
             String errorMsg = bundle.getString("config_statistics");
             statusConfig.add(errorMsg);
         }
 
         return statusConfig; 
     }
     
     /**
      * Checks the modules
      * 
      * @param bundle
      * @return
      */
     private Vector checkModules(ResourceBundle bundle) {
         Map statusMap = MoriaController.getStatus();
         Vector statusMsg = new Vector();
         
         if (statusMap != null) {
 
             String[] states = {"moria", "init", "am", "dm", "sm", "web"};
             Map moduleNames = new HashMap();
             moduleNames.put("moria", "Moria");
             moduleNames.put("init", "Controller");
             moduleNames.put("am", "Authorization manager");
             moduleNames.put("dm", "Directory manager");
             moduleNames.put("sm", "Store manager");
             moduleNames.put("web", "Web application");
 
             for (int i = 0; i < states.length; i++) {
 
                 Object stateObject = statusMap.get(states[i]);
                 Boolean isReady = new Boolean(false);
 
                 if (stateObject instanceof Boolean) {
                     isReady = (Boolean) stateObject;
                 }
 
                 if (states[i].equals("moria") && isReady.booleanValue()) {
                     statusMsg = new Vector(); // return an empty vector
                     break;
                 } else if (!isReady.booleanValue()) {                
                     String errormsg = bundle.getString("module_1") + moduleNames.get(states[i]) + bundle.getString("module_2") + "<br>";
                     statusMsg.add(errormsg);
                 }
             }
         }
         return statusMsg;
     }
     
     /**
      * Checks the SOAP page.
      * 
      * @param bundle
      * @return
      */
     private Vector checkSoap(ResourceBundle bundle) {
         
         Vector soapMsg = new Vector();
         
         try {
             URL url = new URL("https://login.feide.no/moria2/v2_1/Authentication?wsdl");
             java.net.Authenticator.setDefault(new Authenticator() {
                 protected java.net.PasswordAuthentication getPasswordAuthentication() 
                 {
                     char[] passwd = {'d', 'e', 'm', 'o', '_', 's', 'e', 'r', 'v', 'i', 'c', 'e'};
                     return new PasswordAuthentication(new String("demo_service"), passwd);
                 }
             });
                      
             
             HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
             connection.setRequestMethod("GET");
             connection.connect();
             int code = connection.getResponseCode();
             if (code != HttpsURLConnection.HTTP_OK) {
               String err = new String(bundle.getString("soap_error") + new Integer(code).toString());
               soapMsg.add(err);
             }
         //should not happen    
         } catch(MalformedURLException e) {
             String err = bundle.getString("soap_exception1");
             soapMsg.add(err);
         } catch (IOException e) {
             String err = bundle.getString("soap_exception2");
             soapMsg.add(err);
         }
         
         return soapMsg;
     }
     
     public Vector checkBackend(ResourceBundle bundle) {
         
         Vector backendMsg = new Vector();
         
         for (Iterator iterator = backendDataUsers.keySet().iterator(); iterator.hasNext();) {
             String key = (String) iterator.next();
             BackendStatusUser userData = (BackendStatusUser) backendDataUsers.get(key);
             String org = userData.getOrganization();
             try {
                 final Map attributes = MoriaController.directNonInteractiveAuthentication(new String[] {STATUS_ATTRIBUTE},
                         userData.getName(), userData.getPassword(), STATUS_PRINCIPAL);
             } catch (Exception e) {
                 String err = bundle.getString("ldap_error") + org;
                 backendMsg.add(err);
             }
         }
     return backendMsg;
     }
 
     /**
      * Handles the GET requests.
      * @param request
      *            The HTTP request object.
      * @param response
      *            The HTTP response object.
      * @throws java.io.IOException
      *             If an input or output error is detected when the servlet
      *             handles the GET request.
      * @throws javax.servlet.ServletException
      *             If the request for the GET could not be handled.
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
      *      javax.servlet.http.HttpServletResponse)
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
     throws IOException, ServletException {
         getBackendStatusData();
         
         Properties config = getConfig();
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
         String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n";
                 
         /* Resource bundle. */
         String language = null;
         String langFromCookie = null;
         if (config != null && request.getCookies() != null) {
            langFromCookie = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG), request.getCookies());
         }
         // Update cookie if language has changed
         if (request.getParameter(RequestUtil.PARAM_LANG) != null) {
             language = request.getParameter(RequestUtil.PARAM_LANG);
             response.addCookie(RequestUtil.createCookie(
                     (String) config.get(RequestUtil.PROP_COOKIE_LANG),
                     language,
                     new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));
         }
 
         /* Get bundle, using either cookie or language parameter */
         final ResourceBundle bundle = RequestUtil.getBundle(
                 RequestUtil.BUNDLE_STATUSSERVLET,
                 language, langFromCookie, null,
                 request.getHeader("Accept-Language"), 
                 (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE));
         
         // Header
         out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
         out.println(docType + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=" + bundle.getLocale() + ">"); 
         out.println("<head><link rel=\"icon\" href=\"/favicon.ico\" type=\"image/png\">"); 
         out.println("<style type=\"text/css\">\n@import url(\"../resource/stil.css\");\n</style>");
         out.println("<link rel=\"author\" href=\"mailto:" + config.get(RequestUtil.RESOURCE_MAIL)+ "\">");
         out.println("<title>" + bundle.getString("header_title") + "</title></head><body>");
         
         //Layout table
         out.println("<table summary=\"Layout-tabell\" class=\"invers\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
         out.println("<tbody><tr valign=\"middle\">");
         out.println("<td class=\"logo\" width=\"76\"><a href=" + config.get(RequestUtil.RESOURCE_LINK) + ">");
         out.println("<img src=\"../resource/logo.gif\" alt=" + config.get(RequestUtil.PROP_FAQ_OWNER) + " border=\"0\" height=\"41\" width=\"76\"></a></td>");
         out.println("<td width=\"0%\"><a class=\"noline\" href=" + config.get(RequestUtil.RESOURCE_LINK) + ">");
         out.println(bundle.getString("header_feide") + "</a></td>");
         out.println("<td width=\"35%\">&nbsp;</td>");
         
         	// Language selection
             TreeMap languages = (TreeMap)RequestUtil.parseConfig(config, RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON);
             Iterator it = languages.keySet().iterator();
             while(it.hasNext()) {
                 String longName = (String) it.next();
                 String shortName  = (String) languages.get(longName);
                 if (RequestUtil.ATTR_SELECTED_LANG.equals(shortName)) {
                     out.println("[" + longName + "]");
                 } else
                     out.println("<td align=\"centre\"><small><a class=\"invers\" href ="
                             + config.get(RequestUtil.PROP_FAQ_STATUS) + "?" + RequestUtil.PARAM_LANG + "=" + shortName + ">" + longName
                             + "</a></small></td>");
                 }
          
         //More Layout
         out.println("<td class=\"dekor1\" width=\"100%\">&nbsp;</td></tr></tbody></table>");
         out.println("<div class=\"midt\">");
         out.println("<table cellspacing=\"0\">");
         out.println("<tbody><tr valign=\"top\">");
         out.println("<td class=\"kropp\">");
                 
         // Check status
         Vector allerrors = new Vector();
         allerrors.addAll(checkModules(bundle));
         allerrors.addAll(checkConfigStatus(bundle));
         allerrors.addAll(checkSoap(bundle));
         allerrors.addAll(checkBackend(bundle));
         if (allerrors.size()>0) {
             for (int i = 0; i < allerrors.size(); i++) {
            out.println((String)(allerrors.get(i)));
             }
         } else if (allerrors.size() == 0) {
            out.println(bundle.getString("ready_msg"));
         }
         
         // Prepare to check test users.
         out.println("<p><table border=1><tr><th>" + bundle.getString("table_organization") + "</th><th>" + bundle.getString("table_status") + "</th></tr>");
 
         // Start checking a new user.
         for (Iterator iterator = backendDataUsers.keySet().iterator(); iterator.hasNext();) {
             String key = (String) iterator.next();
             BackendStatusUser userData = (BackendStatusUser) backendDataUsers.get(key);
             out.println("<tr><td>" + userData.getOrganization() + "</td>");
             try {
                 final Map attributes = MoriaController.directNonInteractiveAuthentication(new String[] {STATUS_ATTRIBUTE},
                         userData.getName(), userData.getPassword(), STATUS_PRINCIPAL);
             
                 // This test user worked.
                 out.println("<td>OK</td>");
                 
             } catch (AuthenticationException e) {
                 log.logWarn("Authentication failed for: " + userData.getName() + ", contact: " + userData.getContact());
                 String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() 
                 + ":%20" + bundle.getString("subject_authentication") + userData.getName() + ">" + bundle.getString("error_help");
                 out.println("<td>" + bundle.getString("error_authentication") + message + "</a></td></tr>");
             } catch (DirectoryUnavailableException e) {
                 log.logWarn("The directory is unavailable for: " + userData.getName() + ", contact: " + userData.getContact());
                 String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() 
                 + ":%20" + bundle.getString("subject_directory") + userData.getName() + ">" + bundle.getString("error_help");
                 out.println("<td>" + bundle.getString("error_directory") + message + "</a></td></tr>");            
             } catch (AuthorizationException e) {
                 log.logWarn("Authorization failed for: " + userData.getName() + ", contact: " + userData.getContact());
                 String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() 
                 + ":%20" + bundle.getString("subject_authorization") + userData.getName() + ">" + bundle.getString("error_help");
                 out.println("<td>" + bundle.getString("error_authorization") + message + "</a></td></tr>");            
             } catch (IllegalInputException e) {
                 log.logWarn("Illegal input for: " + userData.getName() + ", contact: " + userData.getContact());
                 String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() 
                 + ":%20" + bundle.getString("subject_illegal") + userData.getName() + ">" + bundle.getString("error_help");
                 out.println("<td>" + bundle.getString("error_illegal") + message + "</a></td></tr>");            
             } catch (InoperableStateException e) {
                 log.logWarn("Inoperable state for: " + userData.getName() + ", contact: " + userData.getContact());
                 //Only print moria-support adress if moria is inoperable
                 if (userData.getOrganization().equals("Uninett")){
                     String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() 
                     + ":%20" + bundle.getString("subject_inoperable") + userData.getName() + ">" + bundle.getString("error_help");
                     out.println("<td>" + bundle.getString("error_inoperable") + message + "</a></td></tr>");
                 } else {
                     out.println("<td>" + bundle.getString("error_inoperable"));                
                 }
             } finally {
             
                 // Finish the table row.
                 out.println("</tr>");
             
             }
         }
         
         // Done with all test users.
         out.println("</table></p>");
 
         //Layout
         out.println("</tr>");
         out.println("</table>");
         out.println("</tbody>");
         out.println("</div>");
 
         out.println("<p>");
         out.println("<table summary=\"Layout-tabell\" class=\"invers\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
         out.println("<tbody><tr class=\"bunn\" valign=\"middle\">");
         out.println("<td class=\"invers\" align=\"left\"><small><a class=\"invers\" href=\"mailto:"
                 + config.get(RequestUtil.RESOURCE_MAIL) + "\">" + config.get(RequestUtil.RESOURCE_MAIL) + "</a></small></td>");
         out.println("<td class=\"invers\" align=\"right\"><small>" + config.get(RequestUtil.RESOURCE_DATE) + "</small></td>");
         out.println("</tr></tbody></table></p>");
         
         // Finish up.
         out.println("</body></html>");
         
     }
     
     /**
      * Get this servlet's configuration from the web module, given by
      * <code>RequestUtil.PROP_CONFIG</code>.
      * @return The last valid configuration.
      * @throws IllegalStateException
      *             If unable to read the current configuration from the servlet
      *             context, and there is no previous configuration. Also thrown
      *             if any of the required parameters (given by
      *             <code>REQUIRED_PARAMETERS</code>) are not set.
      * @see #REQUIRED_PARAMETERS
      * @see RequestUtil#PROP_CONFIG
      */
     private Properties getConfig() {
         try {
             return getServletConfig(getRequiredParameters(), log);
         }
         catch (IllegalStateException e) {
          return null;   
         }
     }
     
 }
 
