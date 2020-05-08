 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItemHeaders;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.lang.StringUtils;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.data.HtmlSerializer;
 import org.rapidcontext.core.data.XmlSerializer;
 import org.rapidcontext.core.js.JsSerializer;
 import org.rapidcontext.core.proc.ProcedureException;
 import org.rapidcontext.core.security.SecurityContext;
 import org.rapidcontext.core.security.User;
 import org.rapidcontext.core.storage.Index;
 import org.rapidcontext.core.storage.Metadata;
 import org.rapidcontext.core.storage.Path;
 import org.rapidcontext.core.web.Request;
 import org.rapidcontext.core.web.SessionFileMap;
 import org.rapidcontext.core.web.SessionManager;
 
 /**
  * The main application servlet. This servlet handles all incoming
  * web requests.
  *
  * @author Per Cederberg
  * @version  1.0
  */
 public class ServletApplication extends HttpServlet {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(ServletApplication.class.getName());
 
     /**
      * The web files storage path.
      */
     public static final Path PATH_FILES = new Path("/files/");
 
     /**
      * The MIME type commonly used for requesting HTML data.
      */
     private static final String[] MIME_HTML = {
         "text/html",
         "text/xhtml",
         "application/xhtml",
         "application/xhtml+xml"
     };
 
     /**
      * The MIME types commonly used for requesting JSON data.
      */
     private static final String[] MIME_JSON = {
         "application/json",
         "application/x-javascript",
         "text/json",
         "text/x-json",
         "text/javascript",
         "text/x-javascript"
     };
 
     /**
      * The MIME type commonly used for requesting XML data.
      */
     private static final String[] MIME_XML = {
         "application/xml",
         "text/xml"
     };
 
     /**
      * The context to use for process execution.
      */
     private ApplicationContext ctx = null;
 
     /**
      * The temporary file upload directory.
      */
     private File tempDir = null;
 
     /**
      * Initializes this servlet.
      *
      * @throws ServletException if the initialization failed
      */
     public void init() throws ServletException {
         super.init();
         ctx = ApplicationContext.init(getBaseDir(), getBaseDir(), true);
         tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
         if (tempDir == null) {
             try {
                 tempDir = File.createTempFile("rapidcontext", String.valueOf(System.currentTimeMillis()));
                 tempDir.delete();
             } catch (IOException e) {
                 tempDir = new File(getBaseDir(), "temp");
             }
             tempDir.mkdir();
             tempDir.deleteOnExit();
         }
     }
 
     /**
      * Uninitializes this servlet.
      */
     public void destroy() {
         ApplicationContext.destroy();
         super.destroy();
     }
 
     /**
      * Handles HTTP GET requests.
      *
      * @param request        the servlet request
      * @param response       the servlet response
      *
      * @throws ServletException if an internal error occurred when processing
      *             the request
      * @throws IOException if an IO error occurred when processing the request
      */
     protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
         throws ServletException, IOException {
 
         process(new Request(request, response));
     }
 
     /**
      * Handles HTTP POST requests.
      *
      * @param request        the servlet request
      * @param response       the servlet response
      *
      * @throws ServletException if an internal error occurred when processing
      *             the request
      * @throws IOException if an IO error occurred when processing the request
      */
     protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
         throws ServletException, IOException {
 
         process(new Request(request, response));
     }
 
     /**
      * Processes a servlet request.
      *
      * @param request        the request to process
      *
      * @throws ServletException if an internal error occurred when processing
      *             the request
      * @throws IOException if an IO error occurred when processing the request
      */
     private void process(Request request)
         throws ServletException, IOException {
 
         try {
             processAuth(request);
             if (!request.hasResponse()) {
                 processDefault(request);
             }
             if (!request.hasResponse()) {
                 request.sendError(HttpServletResponse.SC_NOT_FOUND,
                                   "text/plain",
                                   "HTTP 404 Not Found");
             }
             request.commit(getServletContext());
         } catch (IOException e) {
             LOG.info("IO error when processing request: " + request);
         }
         LOG.fine(ip(request) + "Request to " + request.getPath() +
                  " processed in " + request.getProcessTime() +
                  " millisecs");
         request.dispose();
     }
 
     /**
      * Processes authentication for a servlet request. Note that a
      * request response may be set if the request has been handled,
      * for instance by returning an HTTP authentication request.
      * On successful authentication, the current user will be set
      * but the request will not contain a response.
      *
      * @param request        the request to process
      */
     private void processAuth(Request request) {
         HttpSession  session;
         String       authType;
         byte[]       authData;
         String       userName = null;
         User         user = null;
 
         // Authenticate user if provided
         SecurityContext.authClear();
         authType = request.getAuthenticationType();
         authData = request.getAuthenticationData();
         try {
             if (request.hasSession()) {
                 session = request.getSession();
                 SessionManager.setIp(session, request.getRemoteAddr());
                 userName = SessionManager.getUser(session);
             }
             if (userName != null) {
                 SecurityContext.auth(userName);
             } else if (isAuthRequired(request) && "Basic".equals(authType)) {
                 LOG.fine(ip(request) + "Received Basic authentication response");
                 processBasicResponse(request, authData);
                 user = SecurityContext.currentUser();
                 SessionManager.setUser(request.getSession(), user.getName());
             }
         } catch (SecurityException e) {
             LOG.info(ip(request) + e.getMessage());
         }
 
         // Check required authentication
         user = SecurityContext.currentUser();
         if (user == null && isAuthRequired(request)) {
             LOG.fine(ip(request) + "Sending Basic authentication request");
             request.sendAuthenticationRequest("Basic realm=\"RapidContext Basic Auth\"", null);
         }
     }
 
     /**
      * Checks if authentication is required for a specific resource.
      *
      * @param request        the request to check
      *
      * @return true if the request requires authentication, or
      *         false otherwise
      */
     private boolean isAuthRequired(Request request) {
         String   path = request.getPath().toLowerCase();
         boolean  isRoot = path.equals("") || path.equals("/");
 
         // TODO: make this control configurable in some way, probably
         //       by checking which resources are available for
         //       anonymous users
         return (isRoot && request.getUrl().endsWith("/")) ||
                path.equals("index.html") ||
                path.startsWith("/rapidcontext/") ||
                !request.hasMethod("GET");
     }
 
     /**
      * Processes a Basic authentication response.
      *
      * @param request        the request to process
      * @param auth           the input authentication data
      *
      * @throws SecurityException if the user authentication failed
      */
     private void processBasicResponse(Request request, byte[] auth) {
         String  name = null;
         String  password = null;
         int     pos;
         String  str;
 
         str = new String(auth);
         pos = str.indexOf(":");
         if (pos > 0) {
             name = str.substring(0, pos);
             password = str.substring(pos + 1);
             str = "Basic authentication for user " + name;
             LOG.fine(ip(request) + "Received " + str);
             SecurityContext.authPassword(name, password);
             LOG.fine(ip(request) + "Valid " + str);
         } else {
             LOG.info(ip(request) + "Basic authentication invalid: " + str);
             throw new SecurityException("Invalid basic authentication");
         }
     }
 
     /**
      * Processes an authenticated servlet request.
      *
      * @param request        the request to process
      */
     private void processDefault(Request request) {
         String   path = request.getPath();
         String   lowerPath = path.toLowerCase();
         boolean  isRoot = path.equals("") || path.equals("/");
 
         if (isRoot && !request.getUrl().endsWith("/")) {
             request.sendRedirect(request.getUrl() + "/");
         } else if (isRoot || lowerPath.startsWith("/index.htm")) {
             processDownload(request, "index.html", false);
         } else if (lowerPath.startsWith("/rapidcontext/")) {
             path = path.substring(14);
             if (path.startsWith("procedure/") && request.hasMethod("POST")) {
                 processProcedure(request, path.substring(10));
             } else if (path.startsWith("download")) {
                 processDownload(request, path.substring(8), true);
             } else if (path.startsWith("upload")) {
                 processUpload(request, path.substring(6));
             } else if (path.startsWith("query/")) {
                 processQuery(request, path.substring(6));
             }
         } else {
             processDownload(request, path.substring(1), false);
         }
     }
 
     /**
      * Processes a file download request.
      *
      * @param request        the request to process
      * @param path           the file path used
      * @param dynamic        the dynamic processing file flag
      */
     private void processDownload(Request request, String path, boolean dynamic) {
         File     file = null;
         String   fileName = request.getParameter("fileName", path);
         String   fileData = request.getParameter("fileData");
         String   mimeType = request.getParameter("mimeType");
         boolean  cache = ctx.getConfig().getBoolean("responseNoCache", false);
         String   str;
 
         if (request.hasMethod("GET")) {
             file = (File) ctx.getStorage().load(new Path(PATH_FILES, path));
             if (file == null && path.startsWith("doc/")) {
                 // TODO: perhaps the docs should be in the data store?
                 file = new File(this.getBaseDir(), path);
             }
             if (file != null && !file.isDirectory()) {
                 request.sendFile(file, cache);
             }
         } else if (dynamic && request.hasMethod("POST")) {
             if (mimeType == null) {
                 mimeType = getServletContext().getMimeType(fileName);
             }
             if (fileData != null) {
                 request.sendData(mimeType, fileData);
             }
         }
         if (request.hasResponse() && request.getParameter("download") != null) {
             str = fileName;
             if (str.indexOf("/") >= 0) {
                 str = str.substring(str.lastIndexOf("/") + 1);
             }
             if (str.length() > 0) {
                 str = "; filename=" + str;
             }
             request.setResponseHeader("Content-Disposition", "attachment" + str);
         }
     }
 
     /**
      * Processes a file upload request.
      *
      * @param request        the request to process
      * @param id             the file id to use
      */
     private void processUpload(Request request, String id) {
         SessionFileMap   fileMap;
         FileItemStream   stream;
         FileItemHeaders  headers;
         String           fileName;
         String           length = null;
         int              size = -1;
         File             file;
         boolean          trace;
 
         try {
             stream = request.getNextFile();
             if (stream == null) {
                 request.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                   "text/plain",
                                   "HTTP 400 Bad Request, missing file data");
                 return;
             }
             fileName = stream.getName();
             if (fileName.lastIndexOf("/") >= 0) {
                 fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
             }
             if (fileName.lastIndexOf("\\") >= 0) {
                 fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
             }
             while (id != null && id.startsWith("/")) {
                 id = id.substring(1);
             }
             if (id == null || id.trim().length() == 0) {
                 id = fileName;
             }
             headers = stream.getHeaders();
             if (headers != null) {
                 length = headers.getHeader("Content-Length");
             }
             if (length == null) {
                 length = request.getHeader("Content-Length");
             }
             if (length != null && length.length() > 0) {
                 try {
                     size = Integer.parseInt(length);
                 } catch (NumberFormatException ignore) {
                     // Do nothing here
                 }
             }
             file = SessionFileMap.createNewFile(tempDir, fileName);
             fileMap = SessionFileMap.getFiles(request.getSession(), true);
             trace = (request.getParameter("trace", null) != null);
             fileMap.addFile(id, file, size, stream.openStream(), trace ? 5 : 0);
             request.sendData("text/plain", "Session file " + id + " uploaded");
         } catch (IOException e) {
             LOG.log(Level.WARNING, "failed to process file upload", e);
             request.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "text/plain",
                               "HTTP 400 Bad Request, " + e.getMessage());
         }
     }
 
     /**
      * Processes a procedure call servlet request.
      *
      * @param request        the request to process
      * @param name           the function name
      */
     private void processProcedure(Request request, String name) {
         Dict          res = new Dict();
         ArrayList     argList = new ArrayList();
         Object[]      args;
         StringBuffer  trace = null;
         String        str = "";
         Object        obj;
 
         res.set("data", null);
         res.set("trace", null);
         res.set("error", null);
         try {
             for (int i = 0; str != null; i++) {
                 str = request.getParameter("arg" + i, null);
                 if (str != null) {
                     argList.add(JsSerializer.unserialize(str));
                 }
             }
             args = argList.toArray();
             if (request.getParameter("trace", null) != null) {
                 trace = new StringBuffer();
             }
             str = "web [" + request.getRemoteAddr() + "]";
             obj = ctx.execute(name, args, str, trace);
             res.set("data", obj);
         } catch (Exception e) {
             res.set("error", e.getMessage());
         }
         if (trace != null) {
             res.set("trace", trace.toString());
         }
         request.sendData("text/javascript", JsSerializer.serialize(res));
     }
 
     /**
      * Processes a query API servlet request.
      *
      * @param request        the request to process
      * @param query          the query path
      */
     private void processQuery(Request request, String query) {
         Path      path = new Path(query);
         Object    meta = null;
         Object    res = null;
         Dict      dict;
         Array     arr;
         boolean   isHtml;
 
         try {
             // TODO: Implement security authentication for data queries
             if (!SecurityContext.hasAdmin()) {
                 LOG.info("permission denied to Query API from " +
                          request.getRemoteAddr() + " for user " +
                          SecurityContext.currentUser());
                throw new ProcedureException("Permission denied");
             }
             isHtml = isMimeMatch(request, MIME_HTML) ||
                      (!isMimeMatch(request, MIME_JSON) &&
                       !isMimeMatch(request, MIME_XML));
             // TODO: Extend data lookup via plug-ins and/or standardized QL
             meta = ctx.getStorage().lookup(path);
             res = ctx.getStorage().load(path);
             if (res instanceof Index) {
                 dict = new Dict();
                 dict.set("type", "index");
                 arr = ((Index) res).indices();
                 if (isHtml) {
                     arr = arr.copy();
                     arr.sort();
                     for (int i = 0; i < arr.size(); i++) {
                         arr.set(i, "http:" + arr.getString(i, null) + "/");
                     }
                 }
                 dict.set("directories", arr);
                 arr = ((Index) res).objects();
                 if (isHtml) {
                     arr = arr.copy();
                     arr.sort();
                     for (int i = 0; i < arr.size(); i++) {
                         arr.set(i, "http:" + arr.getString(i, null));
                     }
                 }
                 dict.set("objects", arr);
                 res = dict;
             } else if (res instanceof File) {
                 File file = (File) res;
                 dict = new Dict();
                 dict.set("type", "file");
                 dict.set("name", file.getName());
                 dict.set("mimeType", getServletContext().getMimeType(file.getName()));
                 dict.set("size", new Long(file.length()));
                 String url = StringUtils.removeEnd(request.getUrl(), request.getPath()) +
                              StringUtils.removeStart(path.toString(), "/files");
                 if (query.startsWith("/files") || query.startsWith("files")) {
                     dict.set("url", url);
                 }
                 res = dict;
             }
             if (meta instanceof Metadata) {
                 dict = ((Metadata) meta).serialize().copy();
                 dict.set("processTime", new Long(request.getProcessTime()));
                 meta = dict;
             }
 
             // Render result as JSON, XML or HTML
             if (isMimeMatch(request, MIME_JSON) && !isHtml) {
                 dict = new Dict();
                 dict.set("metadata", meta);
                 dict.set("data", res);
                 request.sendData("text/javascript", JsSerializer.serialize(dict));
             } else if (isMimeMatch(request, MIME_XML) && !isHtml) {
                 dict = new Dict();
                 dict.set("metadata", meta);
                 dict.set("data", res);
                 request.sendData("text/xml", XmlSerializer.serialize(dict));
             } else {
                 StringBuffer html = new StringBuffer();
                 html.append("<html>\n<head>\n<link rel='stylesheet' href='");
                 html.append(request.getRootPath());
                 html.append("css/style.css' type='text/css' />\n");
                 html.append("<title>RapidContext Query Response</title>\n");
                 html.append("</head>\n<body>\n<div class='query'>\n");
                 html.append("<h1>RapidContext Query API</h1>\n");
                 html.append("<table class='navigation'>\n<tr>\n");
                 if (path.isRoot()) {
                     html.append("<td class='active'>Start</td>\n");
                 } else {
                     html.append("<td class='prev'><a href='");
                     html.append(StringUtils.repeat("../", path.depth()));
                     html.append(".'>Start</a></td>\n");
                 }
                 for (int i = 0; i < path.length(); i++) {
                     if (i + 1 < path.length()) {
                         html.append("<td class='prev-prev'>&nbsp;</td>\n");
                         html.append("<td class='prev'><a href='");
                         html.append(StringUtils.repeat("../", path.depth() - i - 1));
                         html.append(".'>");
                         html.append(path.name(i));
                         html.append("</a>");
                     } else {
                         html.append("<td class='prev-active'>&nbsp;</td>\n");
                         html.append("<td class='active'>");
                         html.append(path.name(i));
                     }
                     html.append("</td>\n");
                 }
                 html.append("<td class='active-end'>&nbsp;</td>\n");
                 html.append("</tr>\n</table>\n<hr/>\n");
                 html.append("<div class='metadata'>\n");
                 html.append("<h2>Query Metadata</h2>\n");
                 html.append(HtmlSerializer.serialize(meta));
                 html.append("</div>\n");
                 html.append("<h2>Query Results</h2>");
                 html.append(HtmlSerializer.serialize(res));
                 html.append("<hr/><p><strong>Data Formats:</strong>");
                 html.append(" &nbsp;<a href='?mimeType=text/javascript'>JSON</a>");
                 html.append(" &nbsp;<a href='?mimeType=text/xml'>XML</a></p>");
                 html.append("</div>\n</body>\n</html>\n");
                 request.sendData("text/html", html.toString());
             }
         } catch (Exception e) {
             // TODO: How do users want their error messages?
             dict = new Dict();
             dict.set("error", e.getMessage());
             res = dict;
             if (isMimeMatch(request, MIME_JSON)) {
                 request.sendData("text/javascript", JsSerializer.serialize(res));
             } else if (isMimeMatch(request, MIME_XML)) {
                 request.sendData("text/xml", XmlSerializer.serialize(res));
             } else {
                 request.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                   "text/plain",
                                  e.getMessage());
             }
         }
     }
 
     /**
      * Checks if the request accepts one of the listed MIME types as
      * response. Note that this method only accepts exact matches, as
      * it will ignore wildcards and types with quality values.
      *
      * @param request        the request to analyze
      * @param mime           the MIME types to check for
      *
      * @return true if one of the MIME types is accepted, or
      *         false otherwise
      */
     private boolean isMimeMatch(Request request, String[] mime) {
         String    param = request.getParameter("mimeType");
         String    header = request.getHeader("Accept");
         String[]  accept = StringUtils.defaultString(param, header).split(",");
         for (int i = 0; i < accept.length; i++) {
             for (int j = 0; j < mime.length; j++) {
                 if (accept[i].equals(mime[j])) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Returns an IP address tag suitable for logging.
      *
      * @param request        the request to use
      *
      * @return the IP address tag for logging
      */
     private String ip(Request request) {
         return "[" + request.getRemoteAddr() + "] ";
     }
 
     /**
      * Returns the base application directory. This is the directory
      * containing all the application files (i.e. the corresponding
      * webapps directory).
      *
      * @return the base application directory
      */
     private File getBaseDir() {
         return new File(getServletContext().getRealPath("/"));
     }
 }
