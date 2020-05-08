 package fedora.server.management;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import javax.xml.transform.stream.*;
 import javax.xml.transform.*;
 
 import fedora.common.*;
 import fedora.server.*;
 import fedora.server.errors.*;
 import fedora.server.search.*;
 import fedora.server.storage.*;
 import fedora.server.storage.types.*;
 import fedora.server.security.*;
 import fedora.server.utilities.*;
 
 /**
  * Servlet for editing the config/beSecurity.xml file in a user-friendly way.
  *
  * A GET request will present an HTML form populated with existing values
  * in beSecurity.xml and non-configured values for all other bMechs
  * and methods that exist in the repository at the time of the request.
  *
  * A POST request will save the results of the form submission over the
  * existing config/beSecurity.xml file, cause an automatic reload of the
  * policies, then describe to the user how/why they may change the servlet
  * password database to reflect the changes.
  *
  * @author cwilper@cs.cornell.edu
  */
 public class BESecurityServlet extends HttpServlet {
 
     /** 
      * If true, changes won't be saved on POST.  Instead, an XML document
      * showing the servlet parameters and the abbreviated and full 
      * beSecurity.xml file will be returned.
      */
     private static final boolean _DEBUG = false;
 
     /** The FieldSearch module of the running Fedora server. */
     private FieldSearch m_fieldSearch;
 
     /** The DOManager module of the running Fedora server. */
     private DOManager m_doManager;
 
     /** The Authorization module of the running Fedora server. */
     private Authorization m_authz;
 
     /** The beSecurity.xml file. */
     private File m_configFile;
 
     /** The stylesheet used to present the form on an HTTP GET request. */
     private File m_styleFile;
 
     /**
      * Respond to an HTTP GET request.
      *
      * Displays an html form for editing backend security configuration, 
      * or an xml document providing enough information to construct the form 
      * (if xml=true).
      */
     public void doGet(HttpServletRequest req,
                       HttpServletResponse res) throws ServletException {
         PrintWriter writer = null;
         try {
 
             // determine the caller's context
             Context context = ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, req);
 
             // load the current beSecurity.xml file
             BESecurityConfig config = null;
             synchronized (m_configFile) {
                 FileInputStream in = new FileInputStream(m_configFile);
                 config = BESecurityConfig.fromStream(in);
             }
 
             // in memory, add empty configs for all bMechs and methods not 
             // already explicitly configured via beSecurity.xml
             config.addEmptyConfigs(getAllBMechMethods(context));
 
             // respond to the request
             String xml = req.getParameter("xml");
             if (xml != null && xml.equals("true")) {
                 // just provide the xml
                 res.setContentType("text/xml; charset=UTF-8");
                 writer = res.getWriter();
                 config.write(false, true, writer);
             } else {
                 // get the xml and transform it
                 ByteArrayOutputStream memOut = new ByteArrayOutputStream();
                 PrintWriter pw = new PrintWriter(memOut);
                 config.write(false, true, pw);
                 pw.flush();
                 Reader xmlReader = new InputStreamReader(
                                        new ByteArrayInputStream(
                                            memOut.toByteArray()));
                 Transformer transformer = TransformerFactory
                                               .newInstance()
                                               .newTemplates(new StreamSource(m_styleFile))
                                               .newTransformer();
                 res.setContentType("text/html; charset=UTF-8");
                 writer = res.getWriter();
                 transformer.transform(new StreamSource(xmlReader),
                                       new StreamResult(res.getWriter()));
             }
         } catch (Exception e) {
             try {
                 e.printStackTrace();
                 res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                               e.getMessage());
             } catch (Exception ex) { }
         } finally {
             if (writer != null) {
                 try { writer.flush(); } catch (Exception e) { }
                 try { writer.close(); } catch (Exception e) { }
             }
         }
     }
 
     /**
      * Respond to an HTTP POST request.
      *
      * This will overwrite the existing config/beSecurity.xml file based on the
      * parameters passed in, re-generate the policies, then inform the user
      * of what needs to be done to tomcat-users_fedoraTemplate.xml.
      */
     public void doPost(HttpServletRequest req,
                        HttpServletResponse res) throws ServletException {
         PrintWriter writer = null;
         try {
             BESecurityConfig config = getConfig(req.getParameterMap());
             if (_DEBUG) {
                 res.setContentType("text/xml");
                 writer = res.getWriter();
                 writeDebugXML(req.getParameterMap(), config, writer);
             } else {
                 synchronized (m_configFile) {
                     // load the original configuration in case of failure
                     BESecurityConfig originalConfig = BESecurityConfig.fromStream(new FileInputStream(m_configFile));
 
                     // save the new configuration
                     FileOutputStream out = new FileOutputStream(m_configFile);
 
                     try {
                         config.toStream(true, out);
 
                         // determine the caller's context
                         Context context = ReadOnlyContext.getContext(Constants.HTTP_REQUEST.REST.uri, req);
 
                         // cause the server to reload the policies
                         m_authz.reloadPolicies(context);
                     } catch (Throwable th) {
                         try { out.close(); } catch (Exception e) { }
                         out = new FileOutputStream(m_configFile);
                         originalConfig.toStream(false, out);
                        String msg = th.getMessage();
                        if (msg == null) msg = "";
                        throw new Exception("Backend policy generation failed. " + msg, th);
                     }
                 }
 
                 // output html
                 res.setContentType("text/html");
                 writer = res.getWriter();
                 writeSavedHTML(writer, config);
             }
         } catch (Throwable th) {
             try {
                 th.printStackTrace();
                 res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                               th.getMessage());
             } catch (Exception ex) { }
         } finally {
             if (writer != null) {
                 try { writer.flush(); } catch (Exception e) { }
                 try { writer.close(); } catch (Exception e) { }
             }
         }
     }
 
     private static void writeSavedHTML(PrintWriter writer, BESecurityConfig config) {
         writer.println("<html><head><title>Saved Backend Security Configuration</title></head><body>");
         writer.println("<table border=\"0\" width=\"850\">");
         writer.println("    <tr>");
         writer.println("       <td><img src=\"/images/newlogo2.jpg\" width=\"70\" height=\"67\"></td>");
         writer.println("       <td valign=\"top\">");
         writer.println("          <center><span style=\"font-weight: bold; color: #000000; margin-top: 4px; margin-bottom: 4px; font-size: 24px; line-height: 110%; padding-top: 8px; padding-bottom: 4px;\">Backend Security Configuration<br/>[Changes Saved]</span>");
         writer.println("             </nobr>");
         writer.println("          </center>");
         writer.println("       </td>");
         writer.println("    </tr>");
         writer.println(" </table>");
         writer.println("<hr size=\"1\"/>");
         writer.println("<h3>Success!</h3><p><ul>");
         writer.println("  <li> Your changes have been successfully saved.</li>");
         writer.println("  <li> Backend policies have been automatically regenerated and applied.</li>");
         writer.println("  <li> To go back to the form, <a href=\"backendSecurity\">click here</a>.</li>");
         writer.println("</ul></p>");
         writer.println("<h3>Note:</h3>");
         writer.println("<p>If you have configured any backend services to authenticate to Fedora using");
         writer.println("basic authentication, you will also need to manually edit the tomcat <code>conf/tomcat-users_fedoraTemplate.xml</code>");
         writer.println("file and restart Fedora.</p>");
         writer.println("<p>For each service role there must be a user and password");
         writer.println("configured in this file so that Fedora can properly authenticate the service.</p>");
         writer.println("<p>For example, if <code>demo:13</code> and <code>demo:2/getHigh</code> must authenticate");
         writer.println("to Fedora, the file must be edited to include the following:</p>");
         writer.println("<pre>");
         writer.println("  &lt;user name=\"demo13user\" password=\"demo13pass\" roles=\"fedoraRole=demo:13\"/&gt;");
         writer.println("  &lt;user name=\"demo2user\" password=\"demo2pass\" roles=\"fedoraRole=demo:2/getHigh\"/&gt;");
         writer.println("</pre>");
         writer.println("<p>The actual usernames and password will be of your own choosing, ");
         writer.println("and should match the credentials that the backend service is configured");
         writer.println("to authenticate to Fedora with during callbacks.</p>");
         writer.println("</body></html>");
     }
 
     /**
      * For debugging purposes, write an XML document detailing the servlet 
      * input parameters and the beSecurity.xml file that would result from them.
      */
     private static void writeDebugXML(Map params,
                                       BESecurityConfig config,
                                       PrintWriter writer) {
         writer.println("<debug>");
         writer.println("<input>");
         Iterator keys = params.keySet().iterator();
         while (keys.hasNext()) {
             String key = (String) keys.next();
             writer.print("<param name=\"" + key + "\" value=\"");
             String[] vals = (String[]) params.get(key);
             for (int i = 0; i < vals.length; i++) {
                 if (i > 0) writer.println(", ");
                 writer.print(vals[i]);
              }
             writer.println("\"/>");
         }
         writer.println("</input>");
         writer.println("<output abbreviated=\"true\">");
         config.write(true, false, writer);
         writer.println("</output>");
         writer.println("<output abbreviated=\"false\">");
         config.write(false, false, writer);
         writer.println("</output>");
         writer.println("</debug>");
     }
 
     /**
      * Get a new BESecurityConfig based only on the given servlet request
      * parameters.
      *
      * <pre>
      * Values (empty means unspecified/inherit):
      *  Bool: 'true' | 'false' | ''
      *  Str:  'value' | ''
      *  StrArray: 'space-delimited value(s)' | ''
      *
      * default/callSSL
      * default/callBasicAuth
      * default/callUsername
      * default/callPassword
      * default/callbackSSL
      * default/callbackBasicAuth
      * default/iplist
      * 
      * internal/ssl
      * internal/basicAuth
      * internal/username
      * internal/password
      * internal/iplist
      * 
      * iplist/inherit = dropdown, 'true' | 'false'
      * iplist/box     = contains selectable list
      * iplist/add     = button to add ips
      * iplist/delete  = button to delete ips
      * </pre>
      */
     private static BESecurityConfig getConfig(Map params) {
         BESecurityConfig config = new BESecurityConfig();
 
         // set default values
         DefaultRoleConfig defaultConfig = new DefaultRoleConfig();
         config.setDefaultConfig(defaultConfig);
         setValues(defaultConfig, params);
 
         // set internal values
         config.setInternalSSL(getBoolean(params, "internal/ssl"));
         config.setInternalBasicAuth(getBoolean(params, "internal/basicAuth"));
         config.setInternalUsername(getString(params, "internal/username"));
         config.setInternalPassword(getString(params, "internal/password"));
         config.setInternalIPList(getStringArray(params, "internal/iplist"));
 
         // set values for each bmech and any methods it depends on
         Map bMechToMethodList = getBMechToMethodList(params.keySet());
         Iterator pids = bMechToMethodList.keySet().iterator();
         while (pids.hasNext()) {
 
             // set the values for the bMechConfig and add it to the bMechConfigs
             String pid = (String) pids.next();
             BMechRoleConfig bMechConfig = new BMechRoleConfig(defaultConfig, pid);
             setValues(bMechConfig, params);
             config.getBMechConfigs().put(pid, bMechConfig);
 
             // then for each method, set the values and add it to the bmech
             List methodNames = (List) bMechToMethodList.get(pid);
             for (int i = 0; i < methodNames.size(); i++) {
                 String methodName = (String) methodNames.get(i);
                 MethodRoleConfig methodConfig = new MethodRoleConfig(bMechConfig, methodName);
                 setValues(methodConfig, params);
                 bMechConfig.getMethodConfigs().put(methodName, methodConfig);
             }
         }
 
         return config;
     }
 
     /**
      * Get a map of bMech pids to lists of method names based on the given
      * servlet parameters names.
      */ 
     private static Map getBMechToMethodList(Set names) {
         Map map = new HashMap();
         Iterator iter = names.iterator();
         while (iter.hasNext()) {
             String name = (String) iter.next();
             if (name.indexOf(":") != -1 && name.endsWith("/iplist/inherit")) {
                 String[] parts = name.split("/");
                 if (parts.length == 3) { // it's a bdef, not a method
                     String pid = parts[0];
                     List methods = new ArrayList();
                     Iterator mIter = names.iterator();
                     while (mIter.hasNext()) {
                         String n = (String) mIter.next();
                         if (n.startsWith(pid + "/") && n.endsWith("/iplist/inherit")) {
                             String[] p = n.split("/");
                             if (p.length == 4) { // it's a method for this bdef
                                 methods.add(p[1]);
                             }
                         }
                     }
                     map.put(pid, methods);
                 }
             }
         }
         return map;
     }
 
     /**
      * Set the values for the given role based on the configuration information
      * in the given servlet request parameter map.
      */
     private static void setValues(BERoleConfig config,
                                   Map params) {
         config.setCallSSL(getBoolean(params, config.getRole() + "/callSSL"));
         config.setCallBasicAuth(getBoolean(params, config.getRole() + "/callBasicAuth"));
         config.setCallUsername(getString(params, config.getRole() + "/callUsername"));
         config.setCallPassword(getString(params, config.getRole() + "/callPassword"));
         config.setCallbackSSL(getBoolean(params, config.getRole() + "/callbackSSL"));
         config.setCallbackBasicAuth(getBoolean(params, config.getRole() + "/callbackBasicAuth"));
         config.setIPList(getStringArray(params, config.getRole() + "/iplist"));
     }
 
     /**
      * Get the given value out of the map as a String.
      * If there is no such value, or the value is empty, return null.
      */
     private static String getString(Map params, String key) {
         if (params.containsKey(key)) {
             String[] vals = (String[]) params.get(key);
             if (vals.length > 0) {
                 String val = vals[0];
                 if (val != null && val.length() > 0) {
                     return val;
                 } else {
                     return null;
                 }
             } else {
                 return null;
             }
         } else {
             return null;
         }
     }
 
     /**
      * Get the given value out of the map as a Boolean.
      * If there is no such value, or the value is empty, return null.
      */
     private static Boolean getBoolean(Map params, String key) {
         String s = getString(params, key);
         if (s != null) {
             if (s.equals("true")) {
                 return Boolean.TRUE;
             } else {
                 return Boolean.FALSE;
             }
         } else {
             return null;
         }
     }
 
     /**
      * Get the given value out of the map as an array of Strings.
      * If there is no such value, or the value is empty, return null.
      * Otherwise, return the array with one element for each space-delimited
      * token.
      */
     private static String[] getStringArray(Map params, String key) {
         String s = getString(params, key);
         if (s != null) {
             return s.split(" +");
         } else {
             return null;
         }
     }
 
     /**
      * Get a map of all BMechs and their associated lists of method names
      * as they currently exist in the repository.
      */
     private Map getAllBMechMethods(Context context) throws Exception {
         Map map = new HashMap();
         String[] resultFields = new String[] { "pid" };
         List conditions = new ArrayList();
         FieldSearchQuery query = new FieldSearchQuery(
                                      Condition.getConditions("fType=M"));
         FieldSearchResult result = m_fieldSearch.findObjects(resultFields,
                                                              100,
                                                              query);
         List rows = result.objectFieldsList();
         boolean exhausted = false;
         while (!exhausted) {
             Iterator iter = rows.iterator();
             while (iter.hasNext()) {
                 ObjectFields fields = (ObjectFields) iter.next();
                 map.put(fields.getPid(), getMethods(fields.getPid(), context));
             }
             if (result.getToken() != null) {
                 result = m_fieldSearch.resumeFindObjects(result.getToken());
                 rows = result.objectFieldsList();
             } else {
                 exhausted = true;
             }
         }
         return map;
     }
 
     /**
      * Get the list of method names for the given bMech as it currently
      * exists in the repository.
      */
     private List getMethods(String bMechPID, Context context) throws Exception {
         List list = new ArrayList();
         BMechReader reader = m_doManager.getBMechReader(false, context, bMechPID);
         MethodDef[] defs = reader.getServiceMethods(null);
         for (int i = 0; i < defs.length; i++) {
             list.add(defs[i].methodName);
         }
         return list;
     }
 
     /**
      * Initialize the servlet by getting a reference to the required modules
      * of the running Fedora instance and making sure the beSecurity.xml and 
      * stylesheet files exist.
      */
     public void init() throws ServletException {
         try {
             File fedoraHome = new File(System.getProperty("fedora.home"));
             Server server = Server.getInstance(fedoraHome, false);
 
             // fieldsearch module
             m_fieldSearch = (FieldSearch) server.getModule("fedora.server.search.FieldSearch");
             if (m_fieldSearch == null) {
                 throw new ServletException("FieldSearch module not loaded");
             }
 
             // domanager module
             m_doManager = (DOManager) server.getModule("fedora.server.storage.DOManager");
             if (m_doManager == null) {
                 throw new ServletException("DOManager module not loaded");
             }
 
             m_authz = (Authorization) server.getModule("fedora.server.security.Authorization");
             if (m_authz == null) {
                 throw new ServletException("Authorization module not loaded");
             }
 
             // config file
             m_configFile = new File(fedoraHome, 
                                    "server/config/beSecurity.xml");
             if (!m_configFile.exists()) {
                 throw new ServletException("Required file missing: " 
                         + m_configFile.getPath());
             }
 
             // style file
             m_styleFile = new File(fedoraHome,
                                    "server/management/backendSecurityConfig.xslt");
             if (!m_styleFile.exists()) {
                 throw new ServletException("Required file missing: " 
                         + m_styleFile.getPath());
             }
         } catch (InitializationException e) {
             throw new ServletException("Unable to get server instance", e);
         }
     }
     
 }
