 /*
  * Copyright 1999,2004 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package plugins.SiteToolPlugin.fproxy.dav.methods;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import freenet.clients.http.Toadlet;
 import freenet.clients.http.ToadletContext;
 import freenet.clients.http.ToadletContextClosedException;
 import freenet.support.Logger;
 import freenet.support.MultiValueTable;
 import freenet.support.api.Bucket;
 import freenet.support.api.HTTPRequest;
 
 import plugins.SiteToolPlugin.fproxy.dav.api.ILockedObject;
 import plugins.SiteToolPlugin.fproxy.dav.api.IMethodExecutor;
 import plugins.SiteToolPlugin.fproxy.dav.api.IResourceLocks;
 import plugins.SiteToolPlugin.fproxy.dav.api.IStoredObject;
 import plugins.SiteToolPlugin.fproxy.dav.api.ITransaction;
 import plugins.SiteToolPlugin.fproxy.dav.api.WebDAVStatus;
 import plugins.SiteToolPlugin.fproxy.dav.exceptions.LockFailedException;
 import plugins.SiteToolPlugin.fproxy.dav.exceptions.WebDAVException;
 import plugins.SiteToolPlugin.fproxy.dav.util.URLEncoder;
 import plugins.SiteToolPlugin.fproxy.dav.util.XMLWriter;
 import plugins.SiteToolPlugin.fproxy.dav.util.XMLWriter.TAG;
 
 public abstract class AbstractMethod implements IMethodExecutor {
 
     /**
      * Array containing the safe characters set.
      */
     protected static URLEncoder URL_ENCODER;
 
     /**
      * Default depth is infite.
      */
     protected static final int INFINITY = 3;
 
     /**
      * Simple date format for the creation date ISO 8601 representation
      * (partial).
      */
     protected static final SimpleDateFormat CREATION_DATE_FORMAT = new SimpleDateFormat(
             "yyyy-MM-dd'T'HH:mm:ss'Z'");
 
     /**
      * Simple date format for the last modified date. (RFC 822 updated by RFC
      * 1123)
      */
     protected static final SimpleDateFormat LAST_MODIFIED_DATE_FORMAT = new SimpleDateFormat(
             "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
 
     static {
         CREATION_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
         LAST_MODIFIED_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
         /**
          * GMT timezone - all HTTP dates are on GMT
          */
         URL_ENCODER = new URLEncoder();
         URL_ENCODER.addSafeCharacter('-');
         URL_ENCODER.addSafeCharacter('_');
         URL_ENCODER.addSafeCharacter('.');
         URL_ENCODER.addSafeCharacter('*');
         URL_ENCODER.addSafeCharacter('/');
     }
 
     /**
      * size of the io-buffer
      */
     protected static int BUF_SIZE = 65536;
 
     /**
      * Default lock timeout value.
      */
     protected static final int DEFAULT_TIMEOUT = 3600;
 
     /**
      * Maximum lock timeout.
      */
     protected static final int MAX_TIMEOUT = 604800;
 
     /**
      * Boolean value to temporary lock resources (for method locks)
      */
     protected static final boolean TEMPORARY = true;
 
     /**
      * Timeout for temporary locks
      */
     protected static final int TEMP_TIMEOUT = 10;
     
     private final Toadlet _parent;
 
     AbstractMethod(Toadlet parent) {
     	_parent = parent;
     }
 
     /**
      * Return the relative path associated with this servlet.
      * 
      * @param request
      *      The servlet request we are processing
      */
     protected String getRelativePath(HTTPRequest request) {
     	// TODO / FIXME get the prefix somewhere from!
         // No, extract the desired path directly from the request
         String result = request.getPath().substring(_parent.path().length());
         Logger.error(this, "TODO: path="+result + "(origin="+request.getPath()+')', new Exception("TODO"));
         if ((result == null) || (result.equals(""))) {
             result = "/";
         }
         return (result);
     }
 
     /**
      * creates the parent path from the given path by removing the last '/' and
      * everything after that
      * 
      * @param path
      *      the path
      * @return parent path
      */
     protected String getParentPath(String path) {
         int slash = path.lastIndexOf('/');
         if (slash != -1) {
             return path.substring(0, slash);
         }
         return null;
     }
 
     /**
      * removes a / at the end of the path string, if present
      * 
      * @param path
      *      the path
      * @return the path without trailing /
      */
     protected String getCleanPath(String path) {
 
         if (path.endsWith("/") && path.length() > 1)
             path = path.substring(0, path.length() - 1);
         return path;
     }
 
     /**
      * Return JAXP document builder instance.
      */
     protected DocumentBuilder getDocumentBuilder() throws WebDAVException {
         DocumentBuilder documentBuilder = null;
         DocumentBuilderFactory documentBuilderFactory = null;
         try {
             documentBuilderFactory = DocumentBuilderFactory.newInstance();
             documentBuilderFactory.setNamespaceAware(true);
             documentBuilder = documentBuilderFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             throw new WebDAVException("jaxp failed");
         }
         return documentBuilder;
     }
 
     /**
      * reads the depth header from the request and returns it as a int
      * 
      * @param req
      * @return the depth from the depth header
      */
     protected int getDepth(HTTPRequest req) {
         int depth = INFINITY;
         String depthStr = req.getHeader("Depth");
         if (depthStr != null) {
             if (depthStr.equals("0")) {
                 depth = 0;
             } else if (depthStr.equals("1")) {
                 depth = 1;
             }
         }
         return depth;
     }
 
     /**
      * URL rewriter.
      * 
      * @param path
      *      Path which has to be rewiten
      * @return the rewritten path
      */
     protected String rewriteUrl(String path) {
         return URL_ENCODER.encode(path);
     }
 
     /**
      * Get the ETag associated with a file.
      * 
      * @param StoredObject
      *      StoredObject to get resourceLength, lastModified and a hashCode of
      *      StoredObject
      * @return the ETag
      */
     protected String getETag(IStoredObject so) {
 
         String resourceLength = "";
         String lastModified = "";
 
         if (so != null && so.isResource()) {
             resourceLength = new Long(so.getResourceLength()).toString();
             lastModified = new Long(so.getLastModified().getTime()).toString();
         }
 
         return "W/\"" + resourceLength + "-" + lastModified + "\"";
 
     }
 
     protected String[] getLockIdFromIfHeader(HTTPRequest req) {
         String[] ids = new String[2];
         String id = req.getHeader("If");
 
         if (id != null && !id.equals("")) {
             if (id.indexOf(">)") == id.lastIndexOf(">)")) {
                 id = id.substring(id.indexOf("(<"), id.indexOf(">)"));
 
                 if (id.indexOf("locktoken:") != -1) {
                     id = id.substring(id.indexOf(':') + 1);
                 }
                 ids[0] = id;
             } else {
                 String firstId = id.substring(id.indexOf("(<"), id
                         .indexOf(">)"));
                 if (firstId.indexOf("locktoken:") != -1) {
                     firstId = firstId.substring(firstId.indexOf(':') + 1);
                 }
                 ids[0] = firstId;
 
                 String secondId = id.substring(id.lastIndexOf("(<"), id
                         .lastIndexOf(">)"));
                 if (secondId.indexOf("locktoken:") != -1) {
                     secondId = secondId.substring(secondId.indexOf(':') + 1);
                 }
                 ids[1] = secondId;
             }
 
         } else {
             ids = null;
         }
         return ids;
     }
 
     protected String getLockIdFromLockTokenHeader(HTTPRequest req) {
         String id = req.getHeader("Lock-Token");
         if (id != null) {
             id = id.substring(id.indexOf(":") + 1, id.indexOf(">"));
         }
         return id;
     }
 
     /**
      * Checks if locks on resources at the given path exists and if so checks
      * the If-Header to make sure the If-Header corresponds to the locked
      * resource. Returning true if no lock exists or the If-Header is
      * corresponding to the locked resource
      * 
      * @param req
      *      Servlet request
      * @param resp
      *      Servlet response
      * @param resourceLocks
      * @param path
      *      path to the resource
      * @param errorList
      *      List of error to be displayed
      * @return true if no lock on a resource with the given path exists or if
      *  the If-Header corresponds to the locked resource
      * @throws IOException
      * @throws LockFailedException
      */
     protected boolean checkLocks(ITransaction transaction,
             HTTPRequest req, ToadletContext ctx,
             IResourceLocks resourceLocks, String path) throws IOException,
             LockFailedException {
 
         ILockedObject loByPath = resourceLocks.getLockedObjectByPath(
                 transaction, path);
         if (loByPath != null) {
 
             if (loByPath.isShared())
                 return true;
 
             // the resource is locked
             String[] lockTokens = getLockIdFromIfHeader(req);
             String lockToken = null;
             if (lockTokens != null)
                 lockToken = lockTokens[0];
             else {
                 return false;
             }
             if (lockToken != null) {
                 ILockedObject loByIf = resourceLocks.getLockedObjectByID(
                         transaction, lockToken);
                 if (loByIf == null) {
                     // no locked resource to the given lockToken
                     return false;
                 }
                 if (!loByIf.equals(loByPath)) {
                     loByIf = null;
                     return false;
                 }
                 loByIf = null;
             }
 
         }
         loByPath = null;
         return true;
     }
 
     /**
      * Send a multistatus element containing a complete error report to the
      * client.
      * 
      * @param req
      *      Servlet request
      * @param resp
      *      Servlet response
      * @param errorList
      *      List of error to be displayed
      * @param mvt 
      * @throws ToadletContextClosedException 
      */
     protected void sendReport(HTTPRequest req, Hashtable<String, Integer> errorList, MultiValueTable<String, String> mvt, ToadletContext ctx) throws IOException, ToadletContextClosedException {
 
     	String absoluteUri = req.getPath();
         // String relativePath = getRelativePath(req);
 
         HashMap<String, String> namespaces = new HashMap<String, String>();
         namespaces.put("DAV:", "D");
 
         Bucket xmlBucket = ctx.getBucketFactory().makeBucket(-1);
 		OutputStream os = xmlBucket.getOutputStream();
 		XMLWriter xmlWriter = new XMLWriter(os, namespaces);
 
 		xmlWriter.writeXMLHeader();
 
         xmlWriter.writeElement("DAV::multistatus", TAG.OPENING);
 
         Enumeration<String> pathList = errorList.keys();
         while (pathList.hasMoreElements()) {
 
            String errorPath = (String) pathList.nextElement();
            int errorCode = ((Integer) errorList.get(errorPath)).intValue();
 
             xmlWriter.writeElement("DAV::response", TAG.OPENING);
 
             xmlWriter.writeElement("DAV::href", TAG.OPENING);
             String toAppend = null;
             if (absoluteUri.endsWith(errorPath)) {
                 toAppend = absoluteUri;
 
             } else if (absoluteUri.contains(errorPath)) {
 
                 int endIndex = absoluteUri.indexOf(errorPath)
                         + errorPath.length();
                 toAppend = absoluteUri.substring(0, endIndex);
             }
             if (!toAppend.startsWith("/") && !toAppend.startsWith("http:"))
                 toAppend = "/" + toAppend;
             xmlWriter.writeText(errorPath);
             xmlWriter.writeElement("DAV::href", TAG.CLOSING);
             xmlWriter.writeElement("DAV::status", TAG.OPENING);
             xmlWriter.writeText("HTTP/1.1 " + errorCode + " " + WebDAVStatus.getStatusText(errorCode));
             xmlWriter.writeElement("DAV::status", TAG.CLOSING);
 
             xmlWriter.writeElement("DAV::response", TAG.CLOSING);
 
         }
 
         xmlWriter.writeElement("DAV::multistatus", TAG.CLOSING);
         
         xmlWriter.close();
         os.close();
 
         sendMultiStatus(xmlBucket, mvt, ctx);
     }
 
 	protected void sendMultiStatus(Bucket data, MultiValueTable<String, String> mvt, ToadletContext ctx) throws ToadletContextClosedException, IOException {
 		sendXMLStatus(WebDAVStatus.SC_MULTI_STATUS, data, mvt, ctx);
 	}
 
 	protected void sendXMLStatus(int status, Bucket data, MultiValueTable<String, String> mvt, ToadletContext ctx) throws ToadletContextClosedException, IOException {
 		ctx.sendReplyHeaders(status, WebDAVStatus.getStatusText(status), mvt, "text/xml; charset=UTF-8", data.size());
 		ctx.writeData(data);
 	}
 
 	protected void sendError(int code, ToadletContext ctx) throws ToadletContextClosedException, IOException {
 		sendError(code,  null, ctx);
 	}
 
 	protected void sendError(int code,  MultiValueTable<String, String> mvt, ToadletContext ctx) throws ToadletContextClosedException, IOException {
 		ctx.sendReplyHeaders(code, WebDAVStatus.getStatusText(code), mvt, null, -1);
 	}
 }
