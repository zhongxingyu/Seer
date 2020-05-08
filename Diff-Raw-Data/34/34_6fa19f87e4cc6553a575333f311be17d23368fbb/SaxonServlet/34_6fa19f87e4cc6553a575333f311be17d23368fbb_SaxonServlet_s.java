 package fedora.localservices.saxon;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.xml.transform.*;
 import javax.xml.transform.stream.*;
 
 import com.icl.saxon.expr.StringValue;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.*;
 
 /**
  * A service that transforms a supplied input document using a supplied
  * stylesheet, with stylesheet caching.
  *
  * Adapted from the SaxonServlet.java example file contained in the
  * source distribution of "The SAXON XSLT Processor from Michael Kay".
  *
  * <pre>
  * -----------------------------------------------------------------------------
  * The original code is Copyright &copy; 2001 by Michael Kay. All rights
  * reserved. The current project homepage for Saxon may be found at:
  * <a href="http://saxon.sourceforge.net/">http://saxon.sourceforge.net/</a>.
  *
 * Portions created for the Fedora Repository System are Copyright &copy; 2002-2006
  * by The Rector and Visitors of the University of Virginia and Cornell
  * University. All rights reserved.
  * -----------------------------------------------------------------------------
  * </pre>
  *
  * @author Michael Kay, rlw@virginia.edu, cwilper@cs.cornell.edu
  * @version Saxon 6.5.2
  */
 public class SaxonServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	
     /** time to wait for getting data via http before giving up */
     public final int TIMEOUT_SECONDS = 10;
 
     /** start string for a servlet config parameter name that gives creds */
     private final String CRED_PARAM_START = "credentials for ";
 
     /** urlString-to-Templates map of cached stylesheets */
     private Map m_cache;
 
     /** pathString-to-Credentials map of configured credentials */
     private Map m_creds;
 
     /** provider of http connections */
     private MultiThreadedHttpConnectionManager m_cManager;
 
 
     /**
      * Initialize the servlet by setting up the stylesheet cache, the
      * http connection manager, and configuring credentials for the http client.
      */
     public void init(ServletConfig config) throws ServletException {
         m_cache = new HashMap();
         m_creds = new HashMap();
         m_cManager = new MultiThreadedHttpConnectionManager();
         m_cManager.getParams().setConnectionTimeout(TIMEOUT_SECONDS * 1000);
 
         Enumeration enm = config.getInitParameterNames();
         while (enm.hasMoreElements()) {
             String name = (String) enm.nextElement();
             if (name.startsWith(CRED_PARAM_START)) {
                 String value = config.getInitParameter(name);
                 if (value.indexOf(":") == -1) {
                     throw new ServletException("Malformed credentials for " 
                             + name + " -- expected ':' user/pass delimiter");
                 }
                 String[] parts = value.split(":");
                 String user = parts[0];
                 StringBuffer pass = new StringBuffer();
                 for (int i = 1; i < parts.length; i++) {
                     if (i > 1) pass.append(':');
                     pass.append(parts[i]);
                 }
                 m_creds.put(name.substring(CRED_PARAM_START.length()), 
                             new UsernamePasswordCredentials(user, 
                                                             pass.toString()));
             }
         }
     }
 
     /**
      * Accept a GET request and produce a response.
      * 
      * HTTP Request Parameters: 
      * <ul>
      *   <li>source - URL of source document</li>
      *   <li>style - URL of stylesheet</li>
      *   <li>clear-stylesheet-cache - if set to yes, empties the cache before running.
      * </ul>
      *
      * @param req The HTTP request
      * @param res The HTTP response
      */
     public void doGet(HttpServletRequest req, HttpServletResponse res)
             throws IOException {
         String source = req.getParameter("source");
         String style = req.getParameter("style");
         String clear = req.getParameter("clear-stylesheet-cache");
 
         if (clear != null && clear.equals("yes")) {
             synchronized (m_cache) { m_cache = new HashMap(); }
         }
 
         try {
             apply(style, source, req, res);
         } catch (Exception e) {
             res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
             e.printStackTrace();
         }
     }
 
     /**
      * Accept an POST request and produce a response (same behavior as GET).
      */
     public void doPost(HttpServletRequest req, HttpServletResponse res)
             throws IOException {
         doGet(req, res);
     }
 
     public String getServletInfo() {
         return "Calls SAXON to apply a stylesheet to a source document";
     }
 
     /**
      * Apply stylesheet to source document
      */
     private void apply(String style, 
                        String source,
                        HttpServletRequest req, 
                        HttpServletResponse res) throws Exception {
 
         // Validate parameters
         if (style==null) {
             throw new TransformerException("No style parameter supplied");
         }
         if (source==null) {
             throw new TransformerException("No source parameter supplied");
         }
 
         InputStream sourceStream = null;
         try {
             // Load the stylesheet (adding to cache if necessary)
             Templates pss = tryCache(style);
             Transformer transformer = pss.newTransformer();
             Properties details = pss.getOutputProperties();
 
             Enumeration p = req.getParameterNames();
             while (p.hasMoreElements()) {
                 String name = (String) p.nextElement();
                 if (!(name.equals("style") || name.equals("source"))) {
                     String value = req.getParameter(name);
                     transformer.setParameter(name, new StringValue(value));
                 }
             }
 
             // Start loading the document to be transformed
             sourceStream = getInputStream(source);
 
             // Set the appropriate output mime type
             String mime = pss.getOutputProperties().getProperty(OutputKeys.MEDIA_TYPE);
             if (mime==null) {
                 res.setContentType("text/html");
             } else {
                 res.setContentType(mime);
             }
 
             // Transform
             transformer.transform(new StreamSource(sourceStream), 
                                   new StreamResult(res.getOutputStream()));
 
         } finally {
             if (sourceStream != null) try { sourceStream.close(); } catch (Exception e) { }
         }
 
     }
 
     /**
      * Maintain prepared stylesheets in memory for reuse
      */
     private Templates tryCache(String url) throws Exception {
         Templates x = (Templates) m_cache.get(url);
         if (x == null) {
             synchronized (m_cache) {
                 if (!m_cache.containsKey(url)) {
                     TransformerFactory factory = TransformerFactory.newInstance();
                     x = factory.newTemplates(new StreamSource(getInputStream(url)));
                     m_cache.put(url, x);
                 }
             }
         }
         return x;
     }
 
     /**
      * Get the content at the given location using the configured
      * credentials (if any).
      */
     private InputStream getInputStream(String url) throws Exception {
         GetMethod getMethod = new GetMethod(url);
         HttpClient client = new HttpClient(m_cManager);
         UsernamePasswordCredentials creds = getCreds(url);
         if (creds != null) {
             client.getState().setCredentials(AuthScope.ANY, creds);
             client.getParams().setAuthenticationPreemptive(true);
             getMethod.setDoAuthentication(true);
         }
         getMethod.setFollowRedirects(true);
         HttpInputStream in = new HttpInputStream(client, getMethod, url);
         if (in.getStatusCode() != 200) {
             try { in.close(); } catch (Exception e) { }
             throw new IOException("HTTP request failed.  Got status code " 
                     + in.getStatusCode() 
                     + " from remote server while attempting to GET " + url);
         } else {
             return in;
         }
     }
 
     /**
      * Return the credentials for the realmPath that most closely matches
      * the given url, or null if none found.
      */
     private UsernamePasswordCredentials getCreds(String url) throws Exception {
         url = normalizeURL(url);
         url = url.substring(url.indexOf("/") + 2);
 
         UsernamePasswordCredentials longestMatch = null;
         int longestMatchLength = 0;
 
         Iterator iter = m_creds.keySet().iterator();
         while (iter.hasNext()) {
             String realmPath = (String) iter.next();
             if (url.startsWith(realmPath)) {
                 int matchLength = realmPath.length();
                 if (matchLength > longestMatchLength) {
                     longestMatchLength = matchLength;
                     longestMatch = (UsernamePasswordCredentials) m_creds.get(realmPath);
                 }
             }
         }
         return longestMatch;
     }
 
     /**
      * Return a URL string in which the port is always specified.
      */
     private static String normalizeURL(String urlString) throws MalformedURLException {
         URL url = new URL(urlString);
         if (url.getPort() == -1) {
             return url.getProtocol() 
                     + "://" 
                     + url.getHost() 
                     + ":" 
                     + url.getDefaultPort() 
                     + url.getFile()
                     + ( ( url.getRef() != null ) ? ( "#" + url.getRef() ) : "" );
         } else {
             return urlString;
         }
     }
 
 }
