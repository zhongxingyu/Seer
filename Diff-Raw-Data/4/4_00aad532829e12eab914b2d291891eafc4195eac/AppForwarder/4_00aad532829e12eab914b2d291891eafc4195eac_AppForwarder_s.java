 package lux.appserver;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.eclipse.jetty.http.HttpURI;
 import org.eclipse.jetty.servlets.ProxyServlet;
 
 
 /**
  * This servlet is intended for proxying XQuery requests to a remote Solr/Lux server.
  * It rewrites xquery requests so that the path (the query) is specified as a parameter: lux.xquery , 
  * the way Solr wants it, and also provides any extra path-info following the XQuery file name 
  * as the value of lux.pathInfo.  
  */
 public class AppForwarder extends ProxyServlet {
     
     public static final String LUX_PATHINFO= "lux.pathInfo";
     public static final String LUX_XQUERY = "lux.xquery";
     
     private String solrHost="localhost";
     private String solrPort="80";
     private String resourceBase = "";
     private String servletPath = "/";
     private String forwardPath = "/lux";
     
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init (config);
         _DontProxyHeaders.add("host");
         String p = config.getInitParameter("resourceBase");
         if (p != null) {
             if (!(p.startsWith("file:/") || p.startsWith("lux:"))) {
                 if (p.startsWith("/")) {
                     p = "file:/" + p;
                 } else {
                     try {
                         p = "file:/" + new File(p).getCanonicalPath().replace('\\', '/');
                     } catch (IOException e) {
                         throw new ServletException ("Configured resourceBase does not exist: " + p);
                     }
                 }
             }
             resourceBase = p;
         }
         p = config.getInitParameter("servlet-path");
         if (p != null) {
             servletPath = p;
         }
         p = config.getInitParameter("forward-path");
         if (p != null) {
             forwardPath = p;
         }
         p = config.getInitParameter("solr-host");
         if (p != null) {
             solrHost = p;
         }
         p = config.getInitParameter("solr-port");
         if (p != null) {
             solrPort = p;
         }
         // import a logger jar?
         System.out.println ("Lux AppServer startup resourceBase=" + resourceBase + "; servlet path=" + servletPath);
     }
     
     /* ------------------------------------------------------------ */
     /**
     /** Resolve requested URL to the Proxied HttpURI
      * @param scheme The scheme of the received request.
      * @param serverName The server encoded in the received request(which 
      * may be from an absolute URL in the request line).
      * @param serverPort The server port of the received request (which 
      * may be from an absolute URL in the request line).
      * @param uri The URI of the received request.
      * @return The HttpURI to which the request should be proxied.
      * @throws MalformedURLException
      */
     @Override
     protected HttpURI proxyHttpURI(String scheme, String serverName, int serverPort, String uri)
         throws MalformedURLException
     {
         String [] parts = uri.split("\\?", 2);
         String url = parts[0];
         String queryString = parts.length > 1 ? parts[1] : null;
         // Is this a request for some xquery? (could be .xq, .xqy, .xql, .xqm, .xquery, etc)
         int idot = url.indexOf(".xq");
         if (idot > 0) {
            String ext = url.substring(idot + 1);
             String xquery;
             String pathInfo=null;
             
             // is there a slash following the .xq extension?  If so, provide it as "path-info"
             int islash = url.indexOf('/', idot);
             int xqueryEnd;
             if (islash > 0) {
                pathInfo = ext.substring(islash);
                 xqueryEnd = islash;
             } else {
                 xqueryEnd = url.length();
             }
             if (url.startsWith(servletPath)) {
                 int slen = servletPath.endsWith("/") ? servletPath.length() - 1 : servletPath.length();
                 xquery = url.substring(slen, xqueryEnd);
             } else {
                 xquery = url.substring(0, xqueryEnd);
             }
             StringBuilder urlBuilder = new StringBuilder(forwardPath);
             urlBuilder.append('?');
             if (queryString != null) {
                 urlBuilder.append(queryString).append('&');
             }
             if (pathInfo != null) {
                 urlBuilder.append("lux.pathInfo=");
                 try {
                     urlBuilder.append(URLEncoder.encode(pathInfo, "utf-8"));
                 } catch (UnsupportedEncodingException e) { }
                 urlBuilder.append('&');
             }
             urlBuilder.append("lux.xquery=").append(resourceBase).append(xquery);
             url = urlBuilder.toString();
             HttpURI httpURI =  new HttpURI(scheme+"://" + solrHost + ":" + solrPort + url);
             return httpURI;
         }
         // else: this is a local resource - don't translate
         return new HttpURI(scheme+"://" + serverName + ":"+ serverPort + uri);
     }
 
 }
 
 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this file,
  * You can obtain one at http://mozilla.org/MPL/2.0/. */
